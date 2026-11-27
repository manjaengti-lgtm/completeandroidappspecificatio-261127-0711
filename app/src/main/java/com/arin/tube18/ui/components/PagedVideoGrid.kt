package com.arin.tube18.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.arin.tube18.model.Video

@Composable
fun PagedVideoGrid(
    items: LazyPagingItems<Video>,
    onVideoClick: (Video) -> Unit,
    empty: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val refresh = items.loadState.refresh
    val append = items.loadState.append
    val count = items.itemCount

    when {
        count == 0 && refresh is LoadState.Error ->
            ErrorState(refresh.error, onRetry = { items.retry() }, modifier = modifier)
        count == 0 && refresh is LoadState.Loading ->
            LoadingState(modifier)
        count == 0 && append is LoadState.Error ->
            ErrorState(append.error, onRetry = { items.retry() }, modifier = modifier)
        count == 0 && refresh is LoadState.NotLoading && append.endOfPaginationReached ->
            empty()
        count == 0 ->
            LoadingState(modifier)
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (refresh is LoadState.Error) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "__refresh_error__", contentType = "banner") {
                    val info = describeError(refresh.error)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Refresh failed: ${info.title}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { items.refresh() }) { Text("Retry") }
                        }
                    }
                }
            }
            items(
                count = count,
                key = items.itemKey { it.id },
                contentType = items.itemContentType { "video" }
            ) { index ->
                val video = items[index]
                if (video != null) {
                    VideoCard(video = video, onClick = { onVideoClick(video) })
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }, key = "__footer__", contentType = "footer") {
                when (append) {
                    is LoadState.Loading -> Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Loading more…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    is LoadState.Error -> Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val info = describeError(append.error)
                        Text(info.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            info.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { items.retry() }) { Text("Retry") }
                    }
                    is LoadState.NotLoading -> if (append.endOfPaginationReached) {
                        Text(
                            "End of feed: the site returned no more videos",
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
