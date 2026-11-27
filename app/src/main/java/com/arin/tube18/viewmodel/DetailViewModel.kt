package com.arin.tube18.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.tube18.data.SavedRepository
import com.arin.tube18.data.VideoRepository
import com.arin.tube18.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class DetailUiState(
    val loading: Boolean = true,
    val video: Video? = null,
    val tags: List<String> = emptyList(),
    val related: List<Video> = emptyList(),
    val error: Throwable? = null
)

class DetailViewModel(
    val videoId: String,
    private val repository: VideoRepository,
    private val saved: SavedRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState(video = repository.cached(videoId)))
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    val isFavorite: StateFlow<Boolean> = saved.isFavorite(videoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val detail = repository.fetchDetail(videoId)
                _state.value = DetailUiState(
                    loading = false,
                    video = detail.video,
                    tags = detail.tags,
                    related = detail.related,
                    error = null
                )
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e) }
            }
        }
    }

    fun toggleFavorite() {
        val video = _state.value.video ?: return
        viewModelScope.launch {
            if (isFavorite.value) saved.removeFavorite(videoId) else saved.addFavorite(video)
        }
    }
}
