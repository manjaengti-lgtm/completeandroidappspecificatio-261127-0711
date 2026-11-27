package com.arin.tube18.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.tube18.data.SavedRepository
import com.arin.tube18.model.Video
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(private val saved: SavedRepository) : ViewModel() {

    val favorites: StateFlow<List<Video>?> = saved.favorites
        .map<List<Video>, List<Video>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val history: StateFlow<List<Video>?> = saved.history
        .map<List<Video>, List<Video>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun removeFavorite(v: Video) {
        viewModelScope.launch { saved.removeFavorite(v.id) }
    }

    fun removeHistory(v: Video) {
        viewModelScope.launch { saved.removeHistory(v.id) }
    }

    fun restoreFavorite(v: Video) {
        viewModelScope.launch { saved.addFavorite(v) }
    }

    fun restoreHistory(v: Video) {
        viewModelScope.launch { saved.addHistory(v) }
    }

    fun clearHistory() {
        viewModelScope.launch { saved.clearHistory() }
    }
}
