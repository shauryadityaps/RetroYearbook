package com.yearbook.retro.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yearbook.retro.data.model.User
import com.yearbook.retro.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.getCurrentUser())

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithEmail(
        email: String,
        password: String,
        onUserNotFound: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.signInWithEmail(email, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState(isSuccess = true)
            } else {
                val exception = result.exceptionOrNull()
                _uiState.value = AuthUiState(
                    isLoading = false,
                    errorMessage = exception?.localizedMessage ?: "Sign in failed"
                )
                if (exception is com.yearbook.retro.data.repository.UserNotFoundException && onUserNotFound != null) {
                    onUserNotFound(email)
                }
            }
        }
    }

    fun createAccountWithEmail(
        email: String,
        displayName: String,
        password: String,
        onAccountExists: ((email: String, existingName: String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.createAccountWithEmail(email, displayName, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState(isSuccess = true)
            } else {
                val exception = result.exceptionOrNull()
                _uiState.value = AuthUiState(
                    isLoading = false,
                    errorMessage = exception?.localizedMessage ?: "Account creation failed"
                )
                if (exception is com.yearbook.retro.data.repository.EmailAlreadyExistsException && onAccountExists != null) {
                    onAccountExists(exception.email, exception.existingName)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun checkExistingUser(email: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.checkExistingUser(email)
            onResult(result.getOrNull())
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.signInWithGoogle(idToken)
            if (result.isSuccess) {
                _uiState.value = AuthUiState(isSuccess = true)
            } else {
                _uiState.value = AuthUiState(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signInWithGoogleAccount(displayName: String, email: String, photoUrl: String, idToken: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.signInWithGoogleAccount(displayName, email, photoUrl, idToken)
            if (result.isSuccess) {
                _uiState.value = AuthUiState(isSuccess = true)
            } else {
                _uiState.value = AuthUiState(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Google sign in failed")
            }
        }
    }

    fun signInAsGuest(name: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.signInAnonymously(name)
            if (result.isSuccess) {
                _uiState.value = AuthUiState(isSuccess = true)
            } else {
                _uiState.value = AuthUiState(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Guest login failed")
            }
        }
    }

    fun updateProfile(displayName: String, photoUrl: String = "") {
        viewModelScope.launch {
            authRepository.updateUserProfile(displayName, photoUrl)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
