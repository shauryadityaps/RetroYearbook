package com.yearbook.retro.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.yearbook.retro.data.model.PhotoEntry
import com.yearbook.retro.data.remote.SupabaseRestSource
import com.yearbook.retro.data.remote.SupabaseStorageSource
import com.yearbook.retro.domain.repository.PhotoRepository
import com.yearbook.retro.media.DateStampRenderer
import com.yearbook.retro.media.ImageCompressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Production implementation of PhotoRepository.
 * Handles client-side WebP compression, amber LED date stamp rendering,
 * Supabase Cloud Storage uploads, Supabase PostgreSQL metadata synchronization, and local caching.
 */
class PhotoRepositoryImpl(
    private val context: Context,
    private val supabaseRest: SupabaseRestSource = SupabaseRestSource(),
    private val storageSource: SupabaseStorageSource = SupabaseStorageSource()
) : PhotoRepository {

    private val localPhotos = MutableStateFlow<Map<String, List<PhotoEntry>>>(emptyMap())
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getPhotosForYearbook(yearbookId: String): Flow<List<PhotoEntry>> {
        // Trigger background fetch from Supabase
        repositoryScope.launch {
            refreshPhotosForYearbook(yearbookId)
        }

        return localPhotos.map { map ->
            (map[yearbookId] ?: emptyList()).sortedByDescending { it.timestamp }
        }
    }

    override suspend fun hasUserPostedToday(yearbookId: String, userId: String, dateString: String): Boolean {
        val photos = localPhotos.value[yearbookId] ?: emptyList()
        if (photos.any { it.authorId == userId && it.dateString == dateString }) return true
        val remote = supabaseRest.getPhotosForYearbook(yearbookId).getOrNull() ?: emptyList()
        return remote.any { it.authorId == userId && it.dateString == dateString }
    }

    override suspend fun uploadDailyPhoto(
        yearbookId: String,
        authorId: String,
        authorName: String,
        authorAvatar: String,
        bitmap: Bitmap,
        caption: String,
        dateString: String
    ): Result<PhotoEntry> = withContext(Dispatchers.IO) {
        try {
            // Strictly scoped per-yearbook to prevent primary key collision across multiple albums
            val docId = "${yearbookId}_${dateString}_${authorId}"

            // 1. Render vintage camera date stamp and compress to WebP locally
            val stampedBitmap = DateStampRenderer.applyDateStamp(context, bitmap)
            val photosDir = File(context.filesDir, "yearbook_photos").apply { mkdirs() }
            val localFile = File(photosDir, "$docId.webp")
            FileOutputStream(localFile).use { out ->
                val bytes = ImageCompressor.bitmapToWebPBytes(stampedBitmap)
                out.write(bytes)
            }

            // 2. Upload to Supabase Storage (Generates public CDN URL)
            val remoteUploadResult = storageSource.uploadPhoto(yearbookId, docId, stampedBitmap)
            val finalPhotoUrl = if (remoteUploadResult.isSuccess) {
                remoteUploadResult.getOrThrow()
            } else {
                return@withContext Result.failure(
                    remoteUploadResult.exceptionOrNull() ?: Exception("Photo upload to cloud storage failed")
                )
            }

            val entry = PhotoEntry(
                id = docId,
                yearbookId = yearbookId,
                authorId = authorId,
                authorName = authorName,
                authorAvatar = authorAvatar,
                photoUrl = finalPhotoUrl,
                dateString = dateString,
                caption = caption,
                timestamp = System.currentTimeMillis()
            )

            // 3. Save photo metadata to Supabase public.photos table
            val saveResult = supabaseRest.savePhotoEntry(entry)
            if (saveResult.isFailure) {
                return@withContext Result.failure(
                    saveResult.exceptionOrNull() ?: Exception("Failed to save memory to database")
                )
            }

            // 4. Update reactive local cache
            val currentList = (localPhotos.value[yearbookId] ?: emptyList()).toMutableList()
            currentList.removeAll { it.id == docId }
            currentList.add(0, entry)

            val updatedMap = localPhotos.value.toMutableMap()
            updatedMap[yearbookId] = currentList
            localPhotos.value = updatedMap

            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePhoto(yearbookId: String, photoId: String): Result<Unit> {
        storageSource.deletePhoto(yearbookId, photoId)
        supabaseRest.deletePhotoEntry(yearbookId, photoId)
        val currentList = (localPhotos.value[yearbookId] ?: emptyList()).toMutableList()
        currentList.removeAll { it.id == photoId }
        val updatedMap = localPhotos.value.toMutableMap()
        updatedMap[yearbookId] = currentList
        localPhotos.value = updatedMap
        return Result.success(Unit)
    }

    private suspend fun refreshPhotosForYearbook(yearbookId: String) {
        val remoteResult = supabaseRest.getPhotosForYearbook(yearbookId)
        if (remoteResult.isSuccess) {
            val remote = remoteResult.getOrThrow()
            val remoteIds = remote.map { it.id }.toSet()
            val localList = localPhotos.value[yearbookId] ?: emptyList()
            val combined = remote + localList.filter { it.id !in remoteIds }
            val sorted = combined.sortedByDescending { it.timestamp }

            val updatedMap = localPhotos.value.toMutableMap()
            updatedMap[yearbookId] = sorted
            localPhotos.value = updatedMap
        }
    }
}
