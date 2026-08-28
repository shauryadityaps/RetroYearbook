package com.yearbook.retro.domain.repository

import android.graphics.Bitmap
import com.yearbook.retro.data.model.PhotoEntry
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun getPhotosForYearbook(yearbookId: String): Flow<List<PhotoEntry>>
    suspend fun hasUserPostedToday(yearbookId: String, userId: String, dateString: String): Boolean
    suspend fun uploadDailyPhoto(
        yearbookId: String,
        authorId: String,
        authorName: String,
        authorAvatar: String,
        bitmap: Bitmap,
        caption: String,
        dateString: String
    ): Result<PhotoEntry>
    suspend fun deletePhoto(yearbookId: String, photoId: String): Result<Unit>
}
