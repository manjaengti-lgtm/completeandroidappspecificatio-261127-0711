package com.arin.tube18.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arin.tube18.data.VideoPagingSource
import com.arin.tube18.data.VideoRepository
import com.arin.tube18.data.videoPagingConfig
import com.arin.tube18.model.FeedQuery
import com.arin.tube18.model.Video
import kotlinx.coroutines.flow.Flow

class HomeViewModel(repository: VideoRepository) : ViewModel() {
    val feed: Flow<PagingData<Video>> = Pager(videoPagingConfig()) {
        VideoPagingSource(repository, FeedQuery())
    }.flow.cachedIn(viewModelScope)
}
