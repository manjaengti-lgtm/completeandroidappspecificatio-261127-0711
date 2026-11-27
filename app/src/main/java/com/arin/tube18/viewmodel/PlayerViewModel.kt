package com.arin.tube18.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.tube18.data.SavedRepository
import com.arin.tube18.data.VideoRepository
import com.arin.tube18.model.Video
import kotlinx.coroutines.launch

class PlayerViewModel(
    val videoId: String,
    private val repository: VideoRepository,
    private val saved: SavedRepository
) : ViewModel() {

    private val video: Video? = repository.cached(videoId)

    val title: String = video?.title ?: "Player"

    init {
        viewModelScope.launch {
            saved.addHistory(video ?: Video(videoId, "Video $videoId", null, "", ""))
        }
    }

    fun isOnline(): Boolean = repository.isOnline()
}
