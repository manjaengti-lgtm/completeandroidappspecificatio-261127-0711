package com.arin.tube18.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arin.tube18.data.Blocklist
import com.arin.tube18.data.VideoPagingSource
import com.arin.tube18.data.VideoRepository
import com.arin.tube18.data.videoPagingConfig
import com.arin.tube18.model.FeedQuery
import com.arin.tube18.model.SearchChip
import com.arin.tube18.model.Video
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class SearchViewModel(private val repository: VideoRepository) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _chip = MutableStateFlow<SearchChip?>(null)
    val chip: StateFlow<SearchChip?> = _chip.asStateFlow()

    private val _submitted = MutableStateFlow<FeedQuery?>(null)
    val submitted: StateFlow<FeedQuery?> = _submitted.asStateFlow()

    private val _blocked = MutableStateFlow(false)
    val blocked: StateFlow<Boolean> = _blocked.asStateFlow()

    private val _inputError = MutableStateFlow<String?>(null)
    val inputError: StateFlow<String?> = _inputError.asStateFlow()

    private var initialApplied = false

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: Flow<PagingData<Video>> = _submitted
        .flatMapLatest { q ->
            if (q == null) flowOf(PagingData.empty())
            else Pager(videoPagingConfig()) { VideoPagingSource(repository, q) }.flow
        }
        .cachedIn(viewModelScope)

    fun applyInitialQuery(query: String?) {
        if (initialApplied) return
        initialApplied = true
        if (!query.isNullOrBlank()) {
            _text.value = query.take(MAX_LENGTH)
            submit()
        }
    }

    fun onTextChange(value: String) {
        _text.value = value.take(MAX_LENGTH)
        _inputError.value = null
    }

    fun clearText() {
        _text.value = ""
        _inputError.value = null
        _blocked.value = false
        if (_chip.value == null) _submitted.value = null else submit()
    }

    fun toggleChip(chip: SearchChip) {
        _chip.value = if (_chip.value == chip) null else chip
        submit()
    }

    fun submit() {
        val q = _text.value.trim()
        val chip = _chip.value
        if (q.isNotEmpty() && q.length < 2) {
            _inputError.value = "Type at least 2 characters"
            return
        }
        _inputError.value = null
        if (q.isNotEmpty() && Blocklist.isBlocked(q)) {
            _blocked.value = true
            _submitted.value = null
            return
        }
        _blocked.value = false
        _submitted.value = if (q.isEmpty() && chip == null) null else FeedQuery(q.ifEmpty { null }, chip)
    }

    companion object {
        const val MAX_LENGTH = 100
    }
}
