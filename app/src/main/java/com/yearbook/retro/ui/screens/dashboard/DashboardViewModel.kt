package com.yearbook.retro.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yearbook.retro.data.model.DailyDropStatus
import com.yearbook.retro.data.model.User
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
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val pendingYearbooks: List<Pair<Yearbook, DailyDropStatus>> = emptyList(),
    val currentUser: User? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val yearbookRepository: YearbookRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.getCurrentUser())

    val uiState: StateFlow<DashboardUiState> = currentUser.flatMapLatest { user ->
        val uid = user?.uid
        if (uid.isNullOrBlank()) {
            flowOf(DashboardUiState(isLoading = false, currentUser = null, pendingYearbooks = emptyList()))
        } else {
            yearbookRepository.getPendingYearbooks(uid).map { pendingList ->
                DashboardUiState(
                    isLoading = false,
                    pendingYearbooks = pendingList,
                    currentUser = user
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DashboardUiState(isLoading = true))

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

    class Factory(
        private val authRepository: AuthRepository,
        private val yearbookRepository: YearbookRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(authRepository, yearbookRepository) as T
        }
    }
}
