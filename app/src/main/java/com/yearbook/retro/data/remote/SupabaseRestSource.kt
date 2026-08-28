package com.yearbook.retro.data.remote

import com.yearbook.retro.data.model.PhotoEntry
import com.yearbook.retro.data.model.User
import com.yearbook.retro.data.model.Yearbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Native Supabase REST Client interfacing directly with PostgreSQL PostgREST tables.
 *
 * Tables Managed:
 * - public.profiles (Users)
 * - public.yearbooks (Albums)
 * - public.yearbook_members (Explicit Access Control Junction)
 * - public.photos (Daily Polaroid Memories)
 */
class SupabaseRestSource(
    private val projectUrl: String = "https://vqahvognmtqsojeoxacs.supabase.co",
    private val anonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZxYWh2b2dubXRxc29qZW94YWNzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc3NTYyNTAsImV4cCI6MjEwMzMzMjI1MH0.anBa3LpByvowXQG34HxQYA6q3VC_yxnfdE8qPl0NdRc"
) {

    // ==========================================
    // 👤 PROFILES & USERS
    // ==========================================

    suspend fun getProfile(userId: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val encodedId = URLEncoder.encode(userId, "UTF-8")
            val endpoint = "$projectUrl/rest/v1/profiles?id=eq.$encodedId&select=*"
            val jsonArray = executeGet(endpoint)
            if (jsonArray.length() == 0) {
                return@withContext Result.success(null)
            }
            val obj = jsonArray.getJSONObject(0)
            val user = User(
                uid = obj.optString("id"),
                displayName = obj.optString("display_name"),
                email = obj.optString("email"),
                photoUrl = obj.optString("photo_url", ""),
                createdAt = obj.optLong("created_at_ms", System.currentTimeMillis())
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfileByEmail(email: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val encodedEmail = URLEncoder.encode(email.trim().lowercase(), "UTF-8")
            val endpoint = "$projectUrl/rest/v1/profiles?email=eq.$encodedEmail&select=*"
            val jsonArray = executeGet(endpoint)
            if (jsonArray.length() == 0) {
                return@withContext Result.success(null)
            }
            val obj = jsonArray.getJSONObject(0)
            val user = User(
                uid = obj.optString("id"),
                displayName = obj.optString("display_name"),
                email = obj.optString("email"),
                photoUrl = obj.optString("photo_url", ""),
                createdAt = obj.optLong("created_at_ms", System.currentTimeMillis())
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProfile(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "$projectUrl/rest/v1/profiles"
            val body = JSONObject().apply {
                put("id", user.uid)
                put("email", user.email.trim().lowercase())
                put("display_name", user.displayName)
                put("photo_url", user.photoUrl)
                put("created_at_ms", user.createdAt)
            }
            executePost(endpoint, body.toString(), upsert = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(userId: String, displayName: String, photoUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encodedId = URLEncoder.encode(userId, "UTF-8")
            val endpoint = "$projectUrl/rest/v1/profiles?id=eq.$encodedId"
            val body = JSONObject().apply {
                if (displayName.isNotBlank()) put("display_name", displayName)
                if (photoUrl.isNotBlank()) put("photo_url", photoUrl)
            }
            executePatch(endpoint, body.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 📚 YEARBOOKS & ALBUM MEMBERSHIP
    // ==========================================

    suspend fun getYearbooksForUser(userId: String): Result<List<Yearbook>> = withContext(Dispatchers.IO) {
        try {
            val encodedUser = URLEncoder.encode(userId, "UTF-8")
            val memberEndpoint = "$projectUrl/rest/v1/yearbook_members?user_id=eq.$encodedUser&select=yearbook_id"
            val memberRows = executeGet(memberEndpoint)

            if (memberRows.length() == 0) {
                return@withContext Result.success(emptyList())
            }

            val ybIds = (0 until memberRows.length()).map {
                memberRows.getJSONObject(it).getString("yearbook_id")
            }

            val idListFormatted = ybIds.joinToString(",") { "\"$it\"" }
            val ybEndpoint = "$projectUrl/rest/v1/yearbooks?id=in.($idListFormatted)&select=*"
            val ybRows = executeGet(ybEndpoint)

            val yearbooks = mutableListOf<Yearbook>()
            for (i in 0 until ybRows.length()) {
                val obj = ybRows.getJSONObject(i)
                val ybId = obj.getString("id")

                // Fetch member IDs for this album
                val allMembersEndpoint = "$projectUrl/rest/v1/yearbook_members?yearbook_id=eq.$ybId&select=user_id"
                val allMemberRows = executeGet(allMembersEndpoint)
                val memberIds = (0 until allMemberRows.length()).map {
                    allMemberRows.getJSONObject(it).getString("user_id")
                }

                // Fetch total memories count
                val photosCountEndpoint = "$projectUrl/rest/v1/photos?yearbook_id=eq.$ybId&select=id"
                val photosRows = executeGet(photosCountEndpoint)

                yearbooks.add(
                    Yearbook(
                        id = ybId,
                        title = obj.optString("title"),
                        description = obj.optString("description", ""),
                        joinCode = obj.optString("join_code"),
                        coverPhotoUrl = obj.optString("cover_photo_url", ""),
                        ownerId = obj.optString("owner_id"),
                        memberIds = memberIds,
                        startDate = obj.optLong("start_date", System.currentTimeMillis()),
                        endDate = obj.optLong("end_date", System.currentTimeMillis() + (90L * 86400000L)),
                        createdAt = obj.optLong("created_at_ms", System.currentTimeMillis()),
                        isArchived = obj.optBoolean("is_archived", false),
                        totalMemories = photosRows.length()
                    )
                )
            }

            Result.success(yearbooks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getYearbookById(yearbookId: String): Result<Yearbook?> = withContext(Dispatchers.IO) {
        try {
            val encodedId = URLEncoder.encode(yearbookId, "UTF-8")
            val ybEndpoint = "$projectUrl/rest/v1/yearbooks?id=eq.$encodedId&select=*"
            val ybRows = executeGet(ybEndpoint)
            if (ybRows.length() == 0) return@withContext Result.success(null)

            val obj = ybRows.getJSONObject(0)
            val allMembersEndpoint = "$projectUrl/rest/v1/yearbook_members?yearbook_id=eq.$yearbookId&select=user_id"
            val allMemberRows = executeGet(allMembersEndpoint)
            val memberIds = (0 until allMemberRows.length()).map {
                allMemberRows.getJSONObject(it).getString("user_id")
            }

            val yb = Yearbook(
                id = yearbookId,
                title = obj.optString("title"),
                description = obj.optString("description", ""),
                joinCode = obj.optString("join_code"),
                coverPhotoUrl = obj.optString("cover_photo_url", ""),
                ownerId = obj.optString("owner_id"),
                memberIds = memberIds,
                startDate = obj.optLong("start_date", System.currentTimeMillis()),
                endDate = obj.optLong("end_date", System.currentTimeMillis() + (90L * 86400000L)),
                createdAt = obj.optLong("created_at_ms", System.currentTimeMillis()),
                isArchived = obj.optBoolean("is_archived", false)
            )
            Result.success(yb)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findYearbookByCode(code: String): Result<Yearbook?> = withContext(Dispatchers.IO) {
        try {
            val cleanCode = code.trim().uppercase()
            val encodedCode = URLEncoder.encode(cleanCode, "UTF-8")
            val ybEndpoint = "$projectUrl/rest/v1/yearbooks?join_code=eq.$encodedCode&select=*"
            val rows = executeGet(ybEndpoint)
            if (rows.length() == 0) return@withContext Result.success(null)

            val obj = rows.getJSONObject(0)
            val ybId = obj.getString("id")

            val allMembersEndpoint = "$projectUrl/rest/v1/yearbook_members?yearbook_id=eq.$ybId&select=user_id"
            val allMemberRows = executeGet(allMembersEndpoint)
            val memberIds = (0 until allMemberRows.length()).map {
                allMemberRows.getJSONObject(it).getString("user_id")
            }

            val yb = Yearbook(
                id = ybId,
                title = obj.optString("title"),
                description = obj.optString("description", ""),
                joinCode = obj.optString("join_code"),
                coverPhotoUrl = obj.optString("cover_photo_url", ""),
                ownerId = obj.optString("owner_id"),
                memberIds = memberIds,
                startDate = obj.optLong("start_date", System.currentTimeMillis()),
                endDate = obj.optLong("end_date", System.currentTimeMillis() + (90L * 86400000L)),
                createdAt = obj.optLong("created_at_ms", System.currentTimeMillis()),
                isArchived = obj.optBoolean("is_archived", false)
            )
            Result.success(yb)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createYearbook(yearbook: Yearbook): Result<Yearbook> = withContext(Dispatchers.IO) {
        try {
            // 1. Insert into public.yearbooks
            val ybEndpoint = "$projectUrl/rest/v1/yearbooks"
            val ybBody = JSONObject().apply {
                put("id", yearbook.id)
                put("title", yearbook.title)
                put("description", yearbook.description)
                put("join_code", yearbook.joinCode)
                put("cover_photo_url", yearbook.coverPhotoUrl)
                put("owner_id", yearbook.ownerId)
                put("start_date", yearbook.startDate)
                put("end_date", yearbook.endDate)
                put("created_at_ms", yearbook.createdAt)
                put("is_archived", yearbook.isArchived)
            }
            executePost(ybEndpoint, ybBody.toString(), upsert = true)

            // 2. Register owner in public.yearbook_members
            val memberEndpoint = "$projectUrl/rest/v1/yearbook_members"
            val memberBody = JSONObject().apply {
                put("yearbook_id", yearbook.id)
                put("user_id", yearbook.ownerId)
            }
            executePost(memberEndpoint, memberBody.toString(), upsert = true)

            Result.success(yearbook.copy(memberIds = listOf(yearbook.ownerId)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinYearbookByCode(code: String, userId: String): Result<Yearbook> = withContext(Dispatchers.IO) {
        val ybResult = findYearbookByCode(code)
        if (ybResult.isFailure || ybResult.getOrNull() == null) {
            return@withContext Result.failure(Exception("No yearbook found with invite code '$code'"))
        }
        val yb = ybResult.getOrNull()!!
        val addRes = addMemberToYearbook(yb.id, userId)
        if (addRes.isFailure) {
            return@withContext Result.failure(addRes.exceptionOrNull() ?: Exception("Failed to join yearbook"))
        }
        val updated = yb.copy(memberIds = (yb.memberIds + userId).distinct())
        Result.success(updated)
    }

    suspend fun addMemberToYearbook(yearbookId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val memberEndpoint = "$projectUrl/rest/v1/yearbook_members"
            val memberBody = JSONObject().apply {
                put("yearbook_id", yearbookId)
                put("user_id", userId)
            }
            executePost(memberEndpoint, memberBody.toString(), upsert = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveYearbook(yearbookId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encodedId = URLEncoder.encode(yearbookId, "UTF-8")
            val endpoint = "$projectUrl/rest/v1/yearbooks?id=eq.$encodedId"
            val body = JSONObject().apply {
                put("is_archived", true)
                put("end_date", System.currentTimeMillis())
            }
            executePatch(endpoint, body.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteYearbook(yearbookId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encodedId = URLEncoder.encode(yearbookId, "UTF-8")
            // 1. Delete all photos for this yearbook
            executeDelete("$projectUrl/rest/v1/photos?yearbook_id=eq.$encodedId")
            // 2. Delete all member associations for this yearbook
            executeDelete("$projectUrl/rest/v1/yearbook_members?yearbook_id=eq.$encodedId")
            // 3. Delete the yearbook record
            executeDelete("$projectUrl/rest/v1/yearbooks?id=eq.$encodedId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMembers(yearbookId: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val encodedYb = URLEncoder.encode(yearbookId, "UTF-8")
            val memberEndpoint = "$projectUrl/rest/v1/yearbook_members?yearbook_id=eq.$encodedYb&select=user_id"
            val rows = executeGet(memberEndpoint)

            if (rows.length() == 0) {
                return@withContext Result.success(emptyList())
            }

            val userIds = (0 until rows.length()).map {
                rows.getJSONObject(it).getString("user_id")
            }

            val idListFormatted = userIds.joinToString(",") { "\"$it\"" }
            val profileEndpoint = "$projectUrl/rest/v1/profiles?id=in.($idListFormatted)&select=*"
            val profileRows = executeGet(profileEndpoint)

            val users = mutableListOf<User>()
            for (i in 0 until profileRows.length()) {
                val p = profileRows.getJSONObject(i)
                users.add(
                    User(
                        uid = p.optString("id"),
                        displayName = p.optString("display_name"),
                        email = p.optString("email"),
                        photoUrl = p.optString("photo_url", ""),
                        createdAt = p.optLong("created_at_ms", System.currentTimeMillis())
                    )
                )
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 📸 PHOTOS & MEMORIES
    // ==========================================

    suspend fun getPhotosForYearbook(yearbookId: String): Result<List<PhotoEntry>> = withContext(Dispatchers.IO) {
        try {
            val encodedYb = URLEncoder.encode(yearbookId, "UTF-8")
            val endpoint = "$projectUrl/rest/v1/photos?yearbook_id=eq.$encodedYb&select=*&order=timestamp.desc"
            val rows = executeGet(endpoint)

            val photos = mutableListOf<PhotoEntry>()
            for (i in 0 until rows.length()) {
                val obj = rows.getJSONObject(i)
                photos.add(
                    PhotoEntry(
                        id = obj.optString("id"),
                        yearbookId = obj.optString("yearbook_id"),
                        authorId = obj.optString("author_id"),
                        authorName = obj.optString("author_name"),
                        authorAvatar = obj.optString("author_avatar", ""),
                        photoUrl = obj.optString("photo_url"),
                        dateString = obj.optString("date_string"),
                        caption = obj.optString("caption", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }

            Result.success(photos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePhotoEntry(entry: PhotoEntry): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "$projectUrl/rest/v1/photos"
            val body = JSONObject().apply {
                put("id", entry.id)
                put("yearbook_id", entry.yearbookId)
                put("author_id", entry.authorId)
                put("author_name", entry.authorName)
                put("author_avatar", entry.authorAvatar)
                put("photo_url", entry.photoUrl)
                put("date_string", entry.dateString)
                put("caption", entry.caption)
                put("timestamp", entry.timestamp)
            }
            executePost(endpoint, body.toString(), upsert = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePhotoEntry(yearbookId: String, photoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encodedId = URLEncoder.encode(photoId, "UTF-8")
            val encodedYb = URLEncoder.encode(yearbookId, "UTF-8")
            val endpoint = "$projectUrl/rest/v1/photos?id=eq.$encodedId&yearbook_id=eq.$encodedYb"
            executeDelete(endpoint)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 🌐 HTTP HELPER UTILITIES
    // ==========================================

    private fun executeGet(endpoint: String): JSONArray {
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15000
            readTimeout = 15000
        }
        val code = conn.responseCode
        if (code in 200..299) {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            return if (text.trim().startsWith("[")) JSONArray(text) else JSONArray().put(JSONObject(text))
        } else {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
            throw Exception(err)
        }
    }

    private fun executePost(endpoint: String, jsonBody: String, upsert: Boolean = false) {
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            setRequestProperty("Content-Type", "application/json")
            if (upsert) {
                setRequestProperty("Prefer", "resolution=merge-duplicates")
            }
            connectTimeout = 15000
            readTimeout = 15000
        }
        conn.outputStream.use { os ->
            os.write(jsonBody.toByteArray(Charsets.UTF_8))
            os.flush()
        }
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
            throw Exception(err)
        }
    }

    private fun executePatch(endpoint: String, jsonBody: String) {
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "PATCH"
            doOutput = true
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 15000
            readTimeout = 15000
        }
        conn.outputStream.use { os ->
            os.write(jsonBody.toByteArray(Charsets.UTF_8))
            os.flush()
        }
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
            throw Exception(err)
        }
    }

    private fun executeDelete(endpoint: String) {
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            connectTimeout = 15000
            readTimeout = 15000
        }
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
            throw Exception(err)
        }
    }
}
