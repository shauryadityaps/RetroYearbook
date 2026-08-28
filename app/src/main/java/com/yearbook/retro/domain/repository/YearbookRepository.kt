package com.yearbook.retro.domain.repository

import com.yearbook.retro.data.model.DailyDropStatus
import com.yearbook.retro.data.model.User
import com.yearbook.retro.data.model.Yearbook
import kotlinx.coroutines.flow.Flow

interface YearbookRepository {
    fun getYearbooksForUser(userId: String): Flow<List<Yearbook>>
    fun getYearbookById(yearbookId: String): Flow<Yearbook?>
    fun getYearbookMembers(yearbookId: String): Flow<List<User>>
    fun getPendingYearbooks(userId: String): Flow<List<Pair<Yearbook, DailyDropStatus>>>
    suspend fun createYearbook(
        title: String,
        description: String,
        startDate: Long,
        endDate: Long,
        coverPhotoUrl: String,
        ownerId: String
    ): Result<Yearbook>
    suspend fun joinYearbookByCode(code: String, userId: String): Result<Yearbook>
    suspend fun getYearbookPreviewByCode(code: String): Result<Yearbook?>
    suspend fun archiveYearbook(yearbookId: String): Result<Unit>
    suspend fun sealYearbook(yearbookId: String): Result<Unit>
    suspend fun deleteYearbook(yearbookId: String): Result<Unit>
    suspend fun cleanupExpiredYearbooks(): Result<Int>
}
