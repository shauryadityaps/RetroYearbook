package com.yearbook.retro.ui.screens.recap

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yearbook.retro.data.model.PhotoEntry
import com.yearbook.retro.data.model.Yearbook
import com.yearbook.retro.domain.repository.PhotoRepository
import com.yearbook.retro.domain.repository.YearbookRepository
import com.yearbook.retro.media.VideoExportProgress
import com.yearbook.retro.media.VideoReelExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class RecapUiState(
    val isLoading: Boolean = false,
    val yearbook: Yearbook? = null,
    val photos: List<PhotoEntry> = emptyList(),
    val exportProgress: Int = 0,
    val exportStatusMessage: String = "",
    val isExporting: Boolean = false,
    val exportedVideoUri: Uri? = null,
    val exportedVideoFile: File? = null,
    val exportError: String? = null
)

class RecapViewModel(
    private val yearbookId: String,
    private val yearbookRepository: YearbookRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _exportState = MutableStateFlow(Triple(0, "", false))
    private val _exportedResult = MutableStateFlow<Pair<Uri?, File?>?>(null)
    private val _exportError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RecapUiState> = combine(
        yearbookRepository.getYearbookById(yearbookId),
        photoRepository.getPhotosForYearbook(yearbookId),
        _exportState,
        _exportedResult,
        _exportError
    ) { yearbook, photos, exportState, result, error ->
        RecapUiState(
            isLoading = false,
            yearbook = yearbook,
            photos = photos,
            exportProgress = exportState.first,
            exportStatusMessage = exportState.second,
            isExporting = exportState.third,
            exportedVideoUri = result?.first,
            exportedVideoFile = result?.second,
            exportError = error
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, RecapUiState(isLoading = true))

    fun startVideoExport(context: Context) {
        val yearbook = uiState.value.yearbook ?: return
        val photos = uiState.value.photos
        if (photos.isEmpty()) {
            _exportError.value = "No photos in this yearbook to export"
            return
        }

        viewModelScope.launch {
            _exportError.value = null
            _exportState.value = Triple(0, "Starting export...", true)

            VideoReelExporter.exportReel(context, yearbook.title, photos).collect { progress ->
                when (progress) {
                    is VideoExportProgress.Progress -> {
                        _exportState.value = Triple(progress.percentage, progress.message, true)
                    }
                    is VideoExportProgress.Success -> {
                        _exportState.value = Triple(100, "Yearbook Reel Ready!", false)
                        _exportedResult.value = Pair(progress.videoUri, progress.videoFile)
                    }
                    is VideoExportProgress.Error -> {
                        _exportState.value = Triple(0, "", false)
                        _exportError.value = progress.message
                    }
                }
            }
        }
    }

    fun shareExportedVideo(context: Context) {
        val file = uiState.value.exportedVideoFile ?: return
        val title = uiState.value.yearbook?.title ?: "Yearbook"
        VideoReelExporter.shareVideo(context, file, title)
    }

    class Factory(
        private val yearbookId: String,
        private val yearbookRepository: YearbookRepository,
        private val photoRepository: PhotoRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecapViewModel(yearbookId, yearbookRepository, photoRepository) as T
        }
    }
}
