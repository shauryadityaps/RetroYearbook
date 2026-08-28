package com.yearbook.retro.ui.screens.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yearbook.retro.data.model.Yearbook
import com.yearbook.retro.domain.repository.AuthRepository
import com.yearbook.retro.domain.repository.YearbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ManageUiState(
    val isCreating: Boolean = false,
    val isJoining: Boolean = false,
    val createSuccessYearbook: Yearbook? = null,
    val previewYearbook: Yearbook? = null,
    val joinSuccessYearbook: Yearbook? = null,
    val errorMessage: String? = null
)

class ManageViewModel(
    private val authRepository: AuthRepository,
    private val yearbookRepository: YearbookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageUiState())
    val uiState: StateFlow<ManageUiState> = _uiState.asStateFlow()

    fun createYearbook(
        title: String,
        description: String,
        startDate: Long,
        endDate: Long,
        coverPhotoUrl: String = ""
    ) {
        val user = authRepository.getCurrentUser()
        val userId = user?.uid ?: "usr_${System.currentTimeMillis()}"

        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter an album title")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)
            try {
                val result = yearbookRepository.createYearbook(
                    title = title.trim(),
                    description = description.trim(),
                    startDate = startDate,
                    endDate = endDate,
                    coverPhotoUrl = coverPhotoUrl,
                    ownerId = userId
                )

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createSuccessYearbook = result.getOrNull()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to create yearbook"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = e.localizedMessage ?: "Error creating album"
                )
            }
        }
    }

    fun lookupCodePreview(code: String) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length != 6) {
            _uiState.value = _uiState.value.copy(previewYearbook = null)
            return
        }

        viewModelScope.launch {
            val result = yearbookRepository.getYearbookPreviewByCode(cleanCode)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(previewYearbook = result.getOrNull(), errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(previewYearbook = null)
            }
        }
    }

    fun joinYearbook(code: String) {
        val cleanCode = code.trim().uppercase()
        val user = authRepository.getCurrentUser()
        val userId = user?.uid ?: "usr_${System.currentTimeMillis()}"

        if (cleanCode.length != 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid 6-character code")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isJoining = true, errorMessage = null)
            try {
                val result = yearbookRepository.joinYearbookByCode(cleanCode, userId)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isJoining = false,
                        joinSuccessYearbook = result.getOrNull()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isJoining = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "No yearbook found with this code"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isJoining = false,
                    errorMessage = e.localizedMessage ?: "Failed to join album"
                )
            }
        }
    }

    fun resetCreateState() {
        _uiState.value = _uiState.value.copy(
            isCreating = false,
            createSuccessYearbook = null,
            errorMessage = null
        )
    }

    fun clearState() {
        _uiState.value = ManageUiState()
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val yearbookRepository: YearbookRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ManageViewModel(authRepository, yearbookRepository) as T
        }
    }
}
