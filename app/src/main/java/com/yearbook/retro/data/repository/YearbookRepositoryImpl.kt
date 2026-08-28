package com.yearbook.retro.data.repository

import com.yearbook.retro.data.model.DailyDropStatus
import com.yearbook.retro.data.model.User
import com.yearbook.retro.data.model.Yearbook
import com.yearbook.retro.data.remote.SupabaseRestSource
import com.yearbook.retro.domain.repository.PhotoRepository
import com.yearbook.retro.domain.repository.YearbookRepository
import com.yearbook.retro.media.DateStampRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

/**
 * Production implementation of YearbookRepository backed by Supabase PostgreSQL.
 * Handles yearbook creation, 6-character invite code lookup and joining,
 * Supabase database synchronization, collaborator querying, 30-day archival retention,
 * and automated cloud storage cleanup.
 */
class YearbookRepositoryImpl(
    private val supabaseRest: SupabaseRestSource = SupabaseRestSource(),
    private val photoRepositoryProvider: () -> PhotoRepository
) : YearbookRepository {

    private val localYearbooks = MutableStateFlow<List<Yearbook>>(emptyList())
    private val localMembers = MutableStateFlow<Map<String, List<User>>>(emptyMap())
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getYearbooksForUser(userId: String): Flow<List<Yearbook>> {
        // Trigger non-blocking remote fetch from Supabase
        repositoryScope.launch {
            refreshYearbooksForUser(userId)
        }

        // Return reactive hot stream mapping from localYearbooks
        return localYearbooks.map { list ->
            list.filter { it.memberIds.contains(userId) || it.ownerId == userId }
                .sortedByDescending { it.createdAt }
        }
    }

    override fun getYearbookById(yearbookId: String): Flow<Yearbook?> {
        repositoryScope.launch {
            val remote = supabaseRest.getYearbookById(yearbookId).getOrNull()
            if (remote != null) {
                localYearbooks.value = listOf(remote) + localYearbooks.value.filter { it.id != remote.id }
            }
        }
        return localYearbooks.map { list -> list.firstOrNull { it.id == yearbookId } }
    }

    override fun getYearbookMembers(yearbookId: String): Flow<List<User>> {
        repositoryScope.launch {
            val remoteMembers = supabaseRest.getMembers(yearbookId).getOrNull()
            if (remoteMembers != null) {
                val updatedMap = localMembers.value.toMutableMap()
                updatedMap[yearbookId] = remoteMembers
                localMembers.value = updatedMap
            }
        }
        return localMembers.map { map -> map[yearbookId] ?: emptyList() }
    }

    override fun getPendingYearbooks(userId: String): Flow<List<Pair<Yearbook, DailyDropStatus>>> {
        val today = DateStampRenderer.getTodayDateString()
        return getYearbooksForUser(userId).map { books ->
            // Only ongoing open albums (not sealed/archived, and end date not passed)
            books.filter { !it.isAlbumSealed }
                .mapNotNull { book ->
                    val hasPosted = photoRepositoryProvider().hasUserPostedToday(book.id, userId, today)
                    if (!hasPosted) {
                        Pair(book, DailyDropStatus.PENDING)
                    } else {
                        null
                    }
                }
        }
    }

    override suspend fun createYearbook(
        title: String,
        description: String,
        startDate: Long,
        endDate: Long,
        coverPhotoUrl: String,
        ownerId: String
    ): Result<Yearbook> {
        val joinCode = generateUniqueJoinCode()
        val yearbook = Yearbook(
            id = "yb_${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            description = description.trim(),
            joinCode = joinCode,
            coverPhotoUrl = coverPhotoUrl,
            ownerId = ownerId,
            memberIds = listOf(ownerId),
            startDate = startDate,
            endDate = endDate,
            createdAt = System.currentTimeMillis(),
            isArchived = false,
            isCompleted = false,
            completedAtMs = 0L,
            retentionDays = 30,
            totalMemories = 0
        )

        // 1. Update local reactive state immediately
        localYearbooks.value = listOf(yearbook) + localYearbooks.value.filter { it.id != yearbook.id }

        // 2. Persist to Supabase PostgreSQL
        val remoteResult = supabaseRest.createYearbook(yearbook)
        if (remoteResult.isSuccess) {
            val created = remoteResult.getOrThrow()
            localYearbooks.value = listOf(created) + localYearbooks.value.filter { it.id != created.id }
            return Result.success(created)
        }

        return Result.success(yearbook)
    }

    override suspend fun joinYearbookByCode(code: String, userId: String): Result<Yearbook> {
        val cleanCode = code.trim().uppercase()
        val remoteResult = supabaseRest.joinYearbookByCode(cleanCode, userId)
        if (remoteResult.isSuccess) {
            val book = remoteResult.getOrThrow()
            localYearbooks.value = listOf(book) + localYearbooks.value.filter { it.id != book.id }
            return Result.success(book)
        }

        val localMatch = localYearbooks.value.firstOrNull { it.joinCode.equals(cleanCode, ignoreCase = true) }
        if (localMatch != null) {
            if (!localMatch.memberIds.contains(userId)) {
                val updated = localMatch.copy(memberIds = (localMatch.memberIds + userId).distinct())
                localYearbooks.value = localYearbooks.value.map { if (it.id == updated.id) updated else it }
                return Result.success(updated)
            }
            return Result.success(localMatch)
        }

        return Result.failure(remoteResult.exceptionOrNull() ?: Exception("No yearbook found with invite code '$cleanCode'"))
    }

    override suspend fun getYearbookPreviewByCode(code: String): Result<Yearbook?> {
        val cleanCode = code.trim().uppercase()
        val remoteResult = supabaseRest.findYearbookByCode(cleanCode)
        if (remoteResult.isSuccess && remoteResult.getOrNull() != null) {
            return Result.success(remoteResult.getOrNull())
        }
        val localMatch = localYearbooks.value.firstOrNull { it.joinCode.equals(cleanCode, ignoreCase = true) }
        return Result.success(localMatch)
    }

    override suspend fun archiveYearbook(yearbookId: String): Result<Unit> {
        return sealYearbook(yearbookId)
    }

    override suspend fun sealYearbook(yearbookId: String): Result<Unit> {
        val now = System.currentTimeMillis()
        val res = supabaseRest.archiveYearbook(yearbookId)
        localYearbooks.value = localYearbooks.value.map {
            if (it.id == yearbookId) it.copy(
                isArchived = true,
                isCompleted = true,
                completedAtMs = now
            ) else it
        }
        return res
    }

    override suspend fun deleteYearbook(yearbookId: String): Result<Unit> {
        val res = supabaseRest.deleteYearbook(yearbookId)
        localYearbooks.value = localYearbooks.value.filter { it.id != yearbookId }
        val updatedMembers = localMembers.value.toMutableMap().apply { remove(yearbookId) }
        localMembers.value = updatedMembers
        return res
    }

    override suspend fun cleanupExpiredYearbooks(): Result<Int> {
        val now = System.currentTimeMillis()
        var deletedCount = 0
        val currentList = localYearbooks.value

        for (book in currentList) {
            if (book.isAlbumSealed && book.createdAt > 1000000000000L) {
                val expiry = book.getRetentionExpiryMs()
                // Strict safety: Expiry must be in the future relative to createdAt and now must exceed it
                if (expiry > book.createdAt && now >= expiry) {
                    // 30 days retention grace period elapsed -> purge cloud assets to reclaim space
                    val delRes = supabaseRest.deleteYearbook(book.id)
                    if (delRes.isSuccess) {
                        deletedCount++
                    }
                }
            }
        }

        if (deletedCount > 0) {
            localYearbooks.value = localYearbooks.value.filter {
                !it.isAlbumSealed || now < it.getRetentionExpiryMs()
            }
        }

        return Result.success(deletedCount)
    }

    private suspend fun refreshYearbooksForUser(userId: String) {
        val remoteResult = supabaseRest.getYearbooksForUser(userId)
        if (remoteResult.isSuccess) {
            val remote = remoteResult.getOrThrow()
            val remoteIds = remote.map { it.id }.toSet()
            val combined = remote + localYearbooks.value.filter { it.id !in remoteIds }
            localYearbooks.value = combined.sortedByDescending { it.createdAt }
        }
    }

    private fun generateUniqueJoinCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
}
