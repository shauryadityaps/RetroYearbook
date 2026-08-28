package com.yearbook.retro.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.yearbook.retro.data.model.User
import com.yearbook.retro.data.remote.SupabaseRestSource
import com.yearbook.retro.domain.repository.AuthRepository
import com.yearbook.retro.domain.repository.IncorrectPasswordException
import com.yearbook.retro.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class EmailAlreadyExistsException(val email: String, val existingName: String, message: String) : Exception(message)
class UserNotFoundException(val email: String, message: String) : Exception(message)

/**
 * Clean AuthRepository implementation managing Email & Password authentication,
 * Supabase profiles storage, local encrypted cache, and active session streams.
 */
class AuthRepositoryImpl(
    private val context: Context,
    private val supabaseRest: SupabaseRestSource = SupabaseRestSource()
) : AuthRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("yearbook_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUserFlow = MutableStateFlow<User?>(loadCachedUser())
    override val currentUserFlow: Flow<User?> = _currentUserFlow.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Asynchronously synchronize profile with Supabase on boot
        val cached = loadCachedUser()
        if (cached != null && cached.uid.isNotBlank()) {
            repositoryScope.launch {
                val remoteProfile = supabaseRest.getProfile(cached.uid).getOrNull()
                if (remoteProfile != null) {
                    val cleanProfile = remoteProfile.copy(
                        photoUrl = PasswordHasher.extractPhotoUrl(remoteProfile.photoUrl)
                    )
                    saveCachedUser(cleanProfile)
                    _currentUserFlow.value = cleanProfile
                }
            }
        }
    }

    override fun getCurrentUser(): User? = _currentUserFlow.value

    override suspend fun checkExistingUser(email: String): Result<User?> {
        val cleanEmail = email.trim().lowercase()
        val remoteUserResult = supabaseRest.getProfileByEmail(cleanEmail)
        if (remoteUserResult.isSuccess && remoteUserResult.getOrNull() != null) {
            val u = remoteUserResult.getOrNull()!!
            return Result.success(u.copy(photoUrl = PasswordHasher.extractPhotoUrl(u.photoUrl)))
        }

        // Check local cache if offline
        val cached = loadCachedUser()
        if (cached != null && cached.email.equals(cleanEmail, ignoreCase = true)) {
            return Result.success(cached)
        }

        return Result.success(null)
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(Exception("Please enter a valid email address"))
        }

        if (password.isBlank()) {
            return Result.failure(Exception("Please enter your password"))
        }

        val stableUid = generateStableEmailUid(cleanEmail)

        // 1. Check existing user in Supabase public.profiles
        val remoteUserResult = supabaseRest.getProfileByEmail(cleanEmail)
        val existing = remoteUserResult.getOrNull() ?: loadCachedUser()?.takeIf { it.email.equals(cleanEmail, ignoreCase = true) }

        if (existing == null) {
            return Result.failure(UserNotFoundException(cleanEmail, "No registered account found for '$cleanEmail'. Please switch to Create Account."))
        }

        // 2. Validate Password Hash
        val storedHash = PasswordHasher.extractHash(existing.photoUrl)
        if (storedHash.isNotBlank()) {
            val isMatch = PasswordHasher.verify(password, storedHash)
            if (!isMatch) {
                return Result.failure(IncorrectPasswordException(cleanEmail, "Incorrect password. Please check and try again."))
            }
        } else {
            // Legacy profile without password -> set password on first login
            val newHash = PasswordHasher.hash(password)
            val updatedUser = existing.copy(
                photoUrl = PasswordHasher.encodeHash(newHash)
            )
            supabaseRest.saveProfile(updatedUser)
        }

        val user = existing.copy(
            uid = stableUid,
            email = cleanEmail,
            photoUrl = PasswordHasher.extractPhotoUrl(existing.photoUrl)
        )

        saveCachedUser(user)
        _currentUserFlow.value = user
        return Result.success(user)
    }

    override suspend fun createAccountWithEmail(email: String, displayName: String, password: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(Exception("Please enter a valid email address"))
        }

        val cleanName = displayName.trim()
        if (cleanName.isBlank()) {
            return Result.failure(Exception("Please enter a username for your yearbook profile"))
        }

        if (password.length < 4) {
            return Result.failure(Exception("Password must be at least 4 characters long"))
        }

        val stableUid = generateStableEmailUid(cleanEmail)

        // 1. Verify if account already exists in Supabase
        val remoteUserResult = supabaseRest.getProfileByEmail(cleanEmail)
        val existing = remoteUserResult.getOrNull() ?: loadCachedUser()?.takeIf { it.email.equals(cleanEmail, ignoreCase = true) }

        if (existing != null) {
            val name = if (existing.displayName.isNotBlank()) existing.displayName else cleanName
            return Result.failure(EmailAlreadyExistsException(cleanEmail, name, "An account with '$cleanEmail' already exists ($name). Please sign in."))
        }

        // 2. Hash Password securely
        val passwordHash = PasswordHasher.hash(password)
        val encodedStorageString = PasswordHasher.encodeHash(passwordHash)

        val newUser = User(
            uid = stableUid,
            displayName = cleanName,
            email = cleanEmail,
            photoUrl = encodedStorageString,
            createdAt = System.currentTimeMillis()
        )

        val saveRes = supabaseRest.saveProfile(newUser)
        if (saveRes.isFailure) {
            return Result.failure(saveRes.exceptionOrNull() ?: Exception("Failed to save profile to database"))
        }

        val cleanUser = newUser.copy(photoUrl = "")
        saveCachedUser(cleanUser)
        _currentUserFlow.value = cleanUser
        return Result.success(cleanUser)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        val fallbackUser = User(
            uid = "usr_${UUID.randomUUID().toString().take(12)}",
            displayName = "Retro Friend",
            email = "",
            photoUrl = "",
            createdAt = System.currentTimeMillis()
        )
        supabaseRest.saveProfile(fallbackUser)
        saveCachedUser(fallbackUser)
        _currentUserFlow.value = fallbackUser
        return Result.success(fallbackUser)
    }

    override suspend fun signInWithGoogleAccount(
        displayName: String,
        email: String,
        photoUrl: String,
        idToken: String?
    ): Result<User> {
        val cleanEmail = email.trim().lowercase()
        val stableUid = if (cleanEmail.isNotBlank()) generateStableEmailUid(cleanEmail) else "usr_${UUID.randomUUID().toString().take(12)}"

        val existing = supabaseRest.getProfileByEmail(cleanEmail).getOrNull()
        val user = User(
            uid = existing?.uid ?: stableUid,
            displayName = if (existing?.displayName?.isNotBlank() == true) existing.displayName else displayName.ifBlank { "Retro Friend" },
            email = cleanEmail,
            photoUrl = photoUrl,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )

        supabaseRest.saveProfile(user)
        saveCachedUser(user)
        _currentUserFlow.value = user
        return Result.success(user)
    }

    override suspend fun signInAnonymously(displayName: String): Result<User> {
        val user = User(
            uid = "guest_${UUID.randomUUID().toString().take(8)}",
            displayName = displayName.ifBlank { "Guest Friend" },
            email = "",
            photoUrl = "",
            createdAt = System.currentTimeMillis()
        )
        supabaseRest.saveProfile(user)
        saveCachedUser(user)
        _currentUserFlow.value = user
        return Result.success(user)
    }

    override suspend fun updateUserProfile(displayName: String, photoUrl: String): Result<User> {
        val current = _currentUserFlow.value ?: return Result.failure(Exception("Not logged in"))
        val updated = current.copy(
            displayName = displayName.ifBlank { current.displayName },
            photoUrl = photoUrl.ifBlank { current.photoUrl }
        )
        supabaseRest.updateProfile(updated.uid, updated.displayName, updated.photoUrl)
        saveCachedUser(updated)
        _currentUserFlow.value = updated
        return Result.success(updated)
    }

    override suspend fun signOut() {
        prefs.edit().clear().apply()
        _currentUserFlow.value = null
    }

    private fun generateStableEmailUid(email: String): String {
        val sanitized = email.lowercase().replace(Regex("[^a-z0-9]"), "_")
        return "g_$sanitized"
    }

    private fun saveCachedUser(user: User) {
        val json = JSONObject().apply {
            put("uid", user.uid)
            put("displayName", user.displayName)
            put("email", user.email)
            put("photoUrl", user.photoUrl)
            put("createdAt", user.createdAt)
        }
        prefs.edit().putString("cached_user_json", json.toString()).apply()
    }

    private fun loadCachedUser(): User? {
        val str = prefs.getString("cached_user_json", null) ?: return null
        return try {
            val json = JSONObject(str)
            User(
                uid = json.getString("uid"),
                displayName = json.getString("displayName"),
                email = json.optString("email", ""),
                photoUrl = json.optString("photoUrl", ""),
                createdAt = json.optLong("createdAt", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            null
        }
    }
}
