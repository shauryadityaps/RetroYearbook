package com.yearbook.retro.domain.repository

import com.yearbook.retro.data.model.User
import kotlinx.coroutines.flow.Flow

class IncorrectPasswordException(val email: String, message: String) : Exception(message)

interface AuthRepository {
    val currentUserFlow: Flow<User?>
    fun getCurrentUser(): User?
    suspend fun checkExistingUser(email: String): Result<User?>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun createAccountWithEmail(email: String, displayName: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithGoogleAccount(
        displayName: String,
        email: String,
        photoUrl: String,
        idToken: String?
    ): Result<User>
    suspend fun signInAnonymously(displayName: String): Result<User>
    suspend fun updateUserProfile(displayName: String, photoUrl: String): Result<User>
    suspend fun signOut()
}
