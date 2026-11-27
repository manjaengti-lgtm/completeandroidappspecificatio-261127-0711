package com.arin.tube18.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.arin.tube18.model.Video
import com.arin.tube18.ui.components.AppTopBar
import com.arin.tube18.ui.components.FullScreenMessage
import com.arin.tube18.ui.components.PagedVideoGrid
import com.arin.tube18.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: HomeViewModel, onVideoClick: (Video) -> Unit) {
    val items = vm.feed.collectAsLazyPagingItems()
    Scaffold(
        topBar = { AppTopBar(title = "Tube18 · Popular") },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = items.loadState.refresh is LoadState.Loading && items.itemCount > 0,
            onRefresh = { items.refresh() },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            PagedVideoGrid(
                items = items,
                onVideoClick = onVideoClick,
                empty = {
                    FullScreenMessage(
                        icon = Icons.Filled.VideoLibrary,
                        title = "No videos",
                        message = "The site returned an empty feed. Pull down or tap Retry to try again.",
                        actionLabel = "Retry",
                        onAction = { items.refresh() }
                    )
                }
            )
        }
    }
}
