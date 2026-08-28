package com.yearbook.retro.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageCompressor {

    private const val MAX_DIMENSION = 1920
    private const val WEBP_QUALITY = 80

    /**
     * Compresses, scales, stamps date, and converts an image to efficient WebP.
     */
    suspend fun compressAndStamp(
        context: Context,
        imageUri: Uri
    ): Result<Pair<File, Bitmap>> = withContext(Dispatchers.IO) {
        try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate scale
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            val maxDim = max(originalWidth, originalHeight)
            var sampleSize = 1
            if (maxDim > MAX_DIMENSION) {
                sampleSize = (maxDim.toFloat() / MAX_DIMENSION).toInt().coerceAtLeast(1)
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            inputStream = context.contentResolver.openInputStream(imageUri)
            val decodedBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (decodedBitmap == null) {
                return@withContext Result.failure(Exception("Unable to decode selected image"))
            }

            // Apply vintage amber date stamp
            val stampedBitmap = DateStampRenderer.applyDateStamp(context, decodedBitmap)

            // Compress to WebP
            val outputFile = File(context.cacheDir, "drop_${System.currentTimeMillis()}.webp")
            val outputStream = FileOutputStream(outputFile)

            val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }

            stampedBitmap.compress(compressFormat, WEBP_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()

            Result.success(Pair(outputFile, stampedBitmap))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun bitmapToWebPBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
        bitmap.compress(compressFormat, WEBP_QUALITY, stream)
        return stream.toByteArray()
    }
}
