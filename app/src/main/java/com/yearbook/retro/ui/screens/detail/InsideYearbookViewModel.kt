package com.yearbook.retro.ui.screens.detail

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yearbook.retro.data.model.PhotoEntry
import com.yearbook.retro.data.model.User
import com.yearbook.retro.data.model.Yearbook
import com.yearbook.retro.domain.repository.AuthRepository
import com.yearbook.retro.domain.repository.PhotoRepository
import com.yearbook.retro.domain.repository.YearbookRepository
import com.yearbook.retro.media.DateStampRenderer
import com.yearbook.retro.util.PdfScrapbookExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class InsideYearbookUiState(
    val isLoading: Boolean = false,
    val yearbook: Yearbook? = null,
    val photosGroupedByDate: Map<String, List<PhotoEntry>> = emptyMap(),
    val rawPhotosList: List<PhotoEntry> = emptyList(),
    val members: List<User> = emptyList(),
    val hasPostedToday: Boolean = false,
    val isCompletedOrArchived: Boolean = false,
    val daysUntilCloudDeletion: Long = 30L,
    val isUploadingDrop: Boolean = false,
    val isExportingPdf: Boolean = false,
    val pdfExportProgress: String? = null,
    val currentUser: User? = null,
    val errorMessage: String? = null
)

class InsideYearbookViewModel(
    private val yearbookId: String,
    private val authRepository: AuthRepository,
    private val yearbookRepository: YearbookRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    private val _isExportingPdf = MutableStateFlow(false)
    private val _pdfExportProgress = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val currentUser: StateFlow<User?> = authRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.getCurrentUser())

    private val yearbookAndMembersFlow = combine(
        yearbookRepository.getYearbookById(yearbookId),
        yearbookRepository.getYearbookMembers(yearbookId)
    ) { yearbook, members ->
        Pair(yearbook, members)
    }

    private val coreDataFlow = combine(
        yearbookAndMembersFlow,
        photoRepository.getPhotosForYearbook(yearbookId)
    ) { (yearbook, members), photos ->
        Triple(yearbook, members, photos)
    }

    private val exportStateFlow = combine(
        _isExportingPdf,
        _pdfExportProgress
    ) { exporting, progress ->
        Pair(exporting, progress)
    }

    private val transientFlow = combine(
        _isUploading,
        exportStateFlow,
        _errorMessage
    ) { isUp, (isExp, prog), err ->
        Triple(isUp, Pair(isExp, prog), err)
    }

    val uiState: StateFlow<InsideYearbookUiState> = combine(
        coreDataFlow,
        transientFlow,
        currentUser
    ) { (yearbook, members, photos), (isUploading, exportPair, errorMsg), user ->
        val userId = user?.uid ?: ""
        val todayStr = DateStampRenderer.getTodayDateString()

        val isCompleted = yearbook?.isAlbumSealed == true
        val daysUntilDeletion = yearbook?.getDaysUntilDeletion() ?: 30L
        val hasPosted = photos.any { it.authorId == userId && it.dateString == todayStr }

        // Group photos by date string descending
        val grouped = photos.groupBy { it.dateString }
            .toSortedMap(compareByDescending { it })

        InsideYearbookUiState(
            isLoading = false,
            yearbook = yearbook,
            photosGroupedByDate = grouped,
            rawPhotosList = photos,
            members = members,
            hasPostedToday = hasPosted,
            isCompletedOrArchived = isCompleted,
            daysUntilCloudDeletion = daysUntilDeletion,
            isUploadingDrop = isUploading,
            isExportingPdf = exportPair.first,
            pdfExportProgress = exportPair.second,
            currentUser = user,
            errorMessage = errorMsg
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, InsideYearbookUiState(isLoading = true))

    fun dropTodayPhoto(bitmap: Bitmap, caption: String) {
        val currentYearbook = uiState.value.yearbook
        if (currentYearbook == null || currentYearbook.isAlbumSealed) {
            _errorMessage.value = "This yearbook has ended and is sealed. No further memories can be added."
            return
        }

        val user = authRepository.getCurrentUser()
        val userId = user?.uid ?: "usr_${System.currentTimeMillis()}"
        val userName = user?.displayName ?: "Friend"
        val userAvatar = user?.photoUrl ?: ""
        val todayStr = DateStampRenderer.getTodayDateString()

        viewModelScope.launch {
            _isUploading.value = true
            _errorMessage.value = null

            val result = photoRepository.uploadDailyPhoto(
                yearbookId = yearbookId,
                authorId = userId,
                authorName = userName,
                authorAvatar = userAvatar,
                bitmap = bitmap,
                caption = caption,
                dateString = todayStr
            )

            _isUploading.value = false
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to upload photo"
            }
        }
    }

    fun exportScrapbookPdf(context: Context, onComplete: (File?) -> Unit) {
        val yearbook = uiState.value.yearbook ?: return
        val members = uiState.value.members
        val rawEntries = uiState.value.rawPhotosList

        viewModelScope.launch {
            _isExportingPdf.value = true
            _pdfExportProgress.value = "Initializing PDF Scrapbook..."

            val result = PdfScrapbookExporter.generateScrapbookPdf(
                context = context,
                yearbook = yearbook,
                members = members,
                photos = rawEntries,
                onProgress = { progressText ->
                    _pdfExportProgress.value = progressText
                }
            )

            _isExportingPdf.value = false
            _pdfExportProgress.value = null

            if (result.isSuccess) {
                val file = result.getOrNull()
                if (file != null) {
                    PdfScrapbookExporter.sharePdf(context, file, "${yearbook.title} PDF Scrapbook")
                    onComplete(file)
                }
            } else {
                _errorMessage.value = "Failed to export PDF: " + result.exceptionOrNull()?.localizedMessage
                onComplete(null)
            }
        }
    }

    fun sealYearbook() {
        viewModelScope.launch {
            yearbookRepository.sealYearbook(yearbookId)
        }
    }

    fun deleteYearbook(onDeleted: () -> Unit) {
        viewModelScope.launch {
            yearbookRepository.deleteYearbook(yearbookId)
            onDeleted()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(
        private val yearbookId: String,
        private val authRepository: AuthRepository,
        private val yearbookRepository: YearbookRepository,
        private val photoRepository: PhotoRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InsideYearbookViewModel(
                yearbookId,
                authRepository,
                yearbookRepository,
                photoRepository
            ) as T
        }
    }
}
