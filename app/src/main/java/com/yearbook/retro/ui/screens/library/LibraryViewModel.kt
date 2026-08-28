package com.yearbook.retro.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yearbook.retro.data.model.Yearbook
import com.yearbook.retro.domain.repository.AuthRepository
import com.yearbook.retro.domain.repository.YearbookRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class LibraryUiState(
    val isLoading: Boolean = false,
    val activeYearbooks: List<Yearbook> = emptyList(),
    val archivedYearbooks: List<Yearbook> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val authRepository: AuthRepository,
    private val yearbookRepository: YearbookRepository
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = authRepository.currentUserFlow.flatMapLatest { user ->
        val uid = user?.uid
        if (uid.isNullOrBlank()) {
            flowOf(LibraryUiState(isLoading = false))
        } else {
            yearbookRepository.getYearbooksForUser(uid).map { list ->
                val active = list.filter { !it.isAlbumSealed }
                val archived = list.filter { it.isAlbumSealed }
                LibraryUiState(
                    isLoading = false,
                    activeYearbooks = active,
                    archivedYearbooks = archived
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LibraryUiState(isLoading = true))

    class Factory(
        private val authRepository: AuthRepository,
        private val yearbookRepository: YearbookRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(authRepository, yearbookRepository) as T
        }
    }
}
