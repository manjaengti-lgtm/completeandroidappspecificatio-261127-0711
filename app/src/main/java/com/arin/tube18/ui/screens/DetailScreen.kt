package com.arin.tube18.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.arin.tube18.data.SiteUrls
import com.arin.tube18.model.Video
import com.arin.tube18.ui.components.AppTopBar
import com.arin.tube18.ui.components.ErrorState
import com.arin.tube18.ui.components.LoadingState
import com.arin.tube18.ui.components.VideoCard
import com.arin.tube18.ui.components.describeError
import com.arin.tube18.ui.openUrl
import com.arin.tube18.ui.shareUrl
import com.arin.tube18.viewmodel.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    vm: DetailViewModel,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onTagClick: (String) -> Unit,
    onVideoClick: (Video) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val isFavorite by vm.isFavorite.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val watchUrl = SiteUrls.watchUrl(vm.videoId)

    Scaffold(
        topBar = { AppTopBar(title = "Details", onBack = onBack) },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        val video = state.video
        val error = state.error
        when {
            video == null && error != null ->
                ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            video == null ->
                LoadingState(Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item(key = "thumb") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color(0xFF0E0E12))
                            .clickable(onClick = onPlay)
                    ) {
                        if (video.thumbnailUrl != null) {
                            AsyncImage(
                                model = video.thumbnailUrl,
                                contentDescription = video.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Surface(
                            modifier = Modifier.align(Alignment.Center).size(72.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", modifier = Modifier.size(44.dp))
                            }
                        }
                        if (video.duration.isNotBlank()) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.78f),
                                contentColor = Color.White
                            ) {
                                Text(
                                    video.duration,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
                item(key = "info") {
                    Column(Modifier.padding(16.dp)) {
                        Text(video.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                video.duration.ifBlank { "Unknown length" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Filled.Visibility, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (video.views.isBlank()) "Views unknown" else "${video.views} views",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item(key = "actions") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onPlay) {
                            Icon(Icons.Filled.PlayArrow, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Play")
                        }
                        FilledTonalButton(onClick = vm::toggleFavorite) {
                            Icon(
                                if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                null,
                                Modifier.size(18.dp),
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (isFavorite) "Favourited" else "Favourite")
                        }
                        OutlinedButton(onClick = { shareUrl(context, video.title, watchUrl) }) {
                            Icon(Icons.Filled.Share, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share")
                        }
                        OutlinedButton(onClick = { openUrl(context, watchUrl) }) {
                            Icon(Icons.Filled.OpenInBrowser, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Browser")
                        }
                    }
                }
                item(key = "tags_header") {
                    SectionHeader("Tags")
                }
                item(key = "tags") {
                    when {
                        state.loading -> SmallLoading()
                        error != null -> InlineError(error, onRetry = vm::load)
                        state.tags.isEmpty() -> EmptyNote("No tags found on the page.")
                        else -> FlowRow(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            state.tags.forEach { tag ->
                                AssistChip(
                                    onClick = { onTagClick(tag) },
                                    label = { Text(tag) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
                item(key = "related_header") {
                    SectionHeader("Related videos")
                }
                item(key = "related") {
                    when {
                        state.loading -> SmallLoading()
                        error != null -> EmptyNote("Related videos couldn't be loaded. Tap Retry above.")
                        state.related.isEmpty() -> EmptyNote("No related videos found.")
                        else -> LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.related, key = { it.id }) { rel ->
                                VideoCard(
                                    video = rel,
                                    onClick = { onVideoClick(rel) },
                                    modifier = Modifier.width(220.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SmallLoading() {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(10.dp))
        Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text,
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun InlineError(error: Throwable, onRetry: () -> Unit) {
    val info = describeError(error)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(info.icon, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(info.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(info.message, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
