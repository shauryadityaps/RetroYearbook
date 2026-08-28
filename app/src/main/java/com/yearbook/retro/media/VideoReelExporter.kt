package com.yearbook.retro.media

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.RgbMatrix
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.yearbook.retro.R
import com.yearbook.retro.data.model.PhotoEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class VideoExportProgress {
    data class Progress(val percentage: Int, val message: String) : VideoExportProgress()
    data class Success(val videoUri: Uri, val videoFile: File) : VideoExportProgress()
    data class Error(val message: String) : VideoExportProgress()
}

object VideoReelExporter {

    private const val SLIDE_DURATION_US = 2_500_000L // 2.5 seconds per slide in microseconds

    /**
     * Exports a nostalgic video reel from a list of photo entries.
     */
    fun exportReel(
        context: Context,
        yearbookTitle: String,
        photos: List<PhotoEntry>
    ): Flow<VideoExportProgress> = flow {
        if (photos.isEmpty()) {
            emit(VideoExportProgress.Error("No photos to export into a video reel"))
            return@flow
        }

        emit(VideoExportProgress.Progress(5, "Preparing nostalgic soundtrack and memories..."))

        try {
            val workingDir = File(context.cacheDir, "reel_${System.currentTimeMillis()}").apply { mkdirs() }
            val downloadedImageFiles = mutableListOf<File>()

            // 1. Download or load cached photo files
            photos.forEachIndexed { index, photo ->
                val progress = 5 + ((index + 1) * 30 / photos.size)
                emit(VideoExportProgress.Progress(progress, "Developing memory ${index + 1} of ${photos.size}..."))

                val imageFile = File(workingDir, "frame_${index}.png")
                val bitmap = loadOrFetchBitmap(context, photo.photoUrl)
                if (bitmap != null) {
                    val stamped = DateStampRenderer.applyDateStamp(context, bitmap)
                    FileOutputStream(imageFile).use { out ->
                        stamped.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    downloadedImageFiles.add(imageFile)
                }
            }

            if (downloadedImageFiles.isEmpty()) {
                emit(VideoExportProgress.Error("Failed to process photos for video reel"))
                return@flow
            }

            emit(VideoExportProgress.Progress(40, "Composing sepia film effects..."))

            // 2. Prepare audio soundtrack from res/raw/nostalgic_acoustic_loop.mp3
            val audioFile = File(workingDir, "soundtrack.mp3")
            context.resources.openRawResource(R.raw.nostalgic_acoustic_loop).use { input ->
                FileOutputStream(audioFile).use { output ->
                    input.copyTo(output)
                }
            }

            // 3. Build edited media items for slides with Media3 Transformer
            val sepiaRgbMatrix = RgbMatrix { _, _ ->
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            }

            val effects = Effects(
                /* audioProcessors = */ emptyList(),
                /* videoEffects = */ listOf(sepiaRgbMatrix)
            )

            val editedMediaItems = downloadedImageFiles.map { file ->
                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.fromFile(file))
                    .setImageDurationMs(2500)
                    .build()

                EditedMediaItem.Builder(mediaItem)
                    .setEffects(effects)
                    .setDurationUs(SLIDE_DURATION_US)
                    .build()
            }

            val sequence = EditedMediaItemSequence(editedMediaItems)

            // Audio track sequence
            val audioMediaItem = MediaItem.fromUri(Uri.fromFile(audioFile))
            val audioSequence = EditedMediaItemSequence(
                listOf(EditedMediaItem.Builder(audioMediaItem).build())
            )

            val composition = Composition.Builder(listOf(sequence, audioSequence))
                .build()

            val sanitizedTitle = yearbookTitle.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val outputFileName = "Yearbook_${sanitizedTitle}_${System.currentTimeMillis()}.mp4"
            val outputFile = File(context.cacheDir, outputFileName)

            emit(VideoExportProgress.Progress(50, "Rendering 1080p nostalgic video reel..."))

            // 4. Run Transformer
            var isExportDone = false
            var exportError: Throwable? = null

            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        isExportDone = true
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        exportError = exportException
                        isExportDone = true
                    }
                })
                .build()

            withContext(Dispatchers.Main) {
                transformer.start(composition, outputFile.absolutePath)
            }

            val progressHolder = ProgressHolder()
            while (!isExportDone) {
                delay(300)
                withContext(Dispatchers.Main) {
                    val progressState: Int = transformer.getProgress(progressHolder)
                    if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                        val pct = 50 + (progressHolder.progress * 45 / 100)
                        emit(VideoExportProgress.Progress(pct, "Rendering reel... ${progressHolder.progress}%"))
                    }
                }
            }

            if (exportError != null) {
                emit(VideoExportProgress.Error("Video export error: ${exportError?.localizedMessage}"))
                return@flow
            }

            emit(VideoExportProgress.Progress(98, "Saving to media gallery..."))

            // 5. Save to MediaStore (Gallery)
            val savedUri = saveVideoToGallery(context, outputFile, outputFileName)

            emit(VideoExportProgress.Progress(100, "Yearbook Reel Ready!"))
            emit(VideoExportProgress.Success(savedUri, outputFile))

        } catch (e: Exception) {
            emit(VideoExportProgress.Error(e.localizedMessage ?: "Failed to generate video reel"))
        }
    }

    private suspend fun loadOrFetchBitmap(context: Context, urlOrUriString: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (urlOrUriString.startsWith("http://") || urlOrUriString.startsWith("https://")) {
                val url = URL(urlOrUriString)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                return@withContext BitmapFactory.decodeStream(input)
            } else if (urlOrUriString.startsWith("file://") || urlOrUriString.startsWith("content://")) {
                val uri = Uri.parse(urlOrUriString)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    return@withContext BitmapFactory.decodeStream(stream)
                }
            } else if (File(urlOrUriString).exists()) {
                return@withContext BitmapFactory.decodeFile(urlOrUriString)
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun saveVideoToGallery(context: Context, videoFile: File, displayName: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/RetroYearbook")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val itemUri = context.contentResolver.insert(collection, values)
            ?: Uri.fromFile(videoFile)

        context.contentResolver.openOutputStream(itemUri)?.use { out ->
            videoFile.inputStream().use { input ->
                input.copyTo(out)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            context.contentResolver.update(itemUri, values, null, null)
        }

        return itemUri
    }

    /**
     * Triggers native Android Share Sheet for sharing video reel to WhatsApp, Instagram Stories, etc.
     */
    fun shareVideo(context: Context, videoFile: File, title: String) {
        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", videoFile)
        } catch (e: Exception) {
            Uri.fromFile(videoFile)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$title - Retro Yearbook Reel")
            putExtra(Intent.EXTRA_TEXT, "Look back at our shared memories in '$title' \uD83D\uDCD6✨")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Yearbook Reel"))
    }
}
