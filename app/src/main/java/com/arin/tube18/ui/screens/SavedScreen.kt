package com.arin.tube18.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arin.tube18.model.Video
import com.arin.tube18.ui.components.AppTopBar
import com.arin.tube18.ui.components.FullScreenMessage
import com.arin.tube18.ui.components.LoadingState
import com.arin.tube18.ui.components.VideoCard
import com.arin.tube18.viewmodel.SavedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(vm: SavedViewModel, onVideoClick: (Video) -> Unit) {
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var confirmClear by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Saved",
                actions = {
                    if (tab == 1 && !history.isNullOrEmpty()) {
                        IconButton(onClick = { confirmClear = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear history")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text("Favourites" + (favorites?.let { " (${it.size})" } ?: "")) },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = null) }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("History" + (history?.let { " (${it.size})" } ?: "")) },
                    icon = { Icon(Icons.Filled.History, contentDescription = null) }
                )
            }
            val list = if (tab == 0) favorites else history
            when {
                list == null -> LoadingState()
                list.isEmpty() -> if (tab == 0) {
                    FullScreenMessage(
                        icon = Icons.Filled.FavoriteBorder,
                        title = "No favourites yet",
                        message = "Open a video and tap Favourite to keep it here."
                    )
                } else {
                    FullScreenMessage(
                        icon = Icons.Filled.History,
                        title = "No history",
                        message = "Videos you play will show up here."
                    )
                }
                else -> {
                    Text(
                        "Long-press a video to remove it",
                        modifier = Modifier.padding(start = 16.dp, top = 10.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val isFavTab = tab == 0
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list, key = { it.id }) { video ->
                            VideoCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (isFavTab) vm.removeFavorite(video) else vm.removeHistory(video)
                                    scope.launch {
                                        snackbar.currentSnackbarData?.dismiss()
                                        val result = snackbar.showSnackbar(
                                            message = "Removed \"${video.title.take(40)}\"",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            if (isFavTab) vm.restoreFavorite(video) else vm.restoreHistory(video)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            icon = { Icon(Icons.Filled.DeleteSweep, contentDescription = null) },
            title = { Text("Clear watch history?") },
            text = { Text("All videos in your history will be removed. Favourites are kept.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearHistory()
                    confirmClear = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}
