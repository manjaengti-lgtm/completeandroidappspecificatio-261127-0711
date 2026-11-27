package com.arin.tube18.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.arin.tube18.model.SearchChip
import com.arin.tube18.model.Video
import com.arin.tube18.ui.components.AppTopBar
import com.arin.tube18.ui.components.FullScreenMessage
import com.arin.tube18.ui.components.PagedVideoGrid
import com.arin.tube18.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(vm: SearchViewModel, initialQuery: String?, onVideoClick: (Video) -> Unit) {
    LaunchedEffect(initialQuery) { vm.applyInitialQuery(initialQuery) }

    val text by vm.text.collectAsStateWithLifecycle()
    val chip by vm.chip.collectAsStateWithLifecycle()
    val submitted by vm.submitted.collectAsStateWithLifecycle()
    val blocked by vm.blocked.collectAsStateWithLifecycle()
    val inputError by vm.inputError.collectAsStateWithLifecycle()
    val results = vm.results.collectAsLazyPagingItems()
    val focus = LocalFocusManager.current

    Scaffold(
        topBar = { AppTopBar(title = "Search") },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            val err = inputError
            OutlinedTextField(
                value = text,
                onValueChange = vm::onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = { Text("Search videos") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (text.isNotEmpty()) {
                    {
                        IconButton(onClick = { vm.clearText() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                } else null,
                singleLine = true,
                isError = err != null,
                supportingText = if (err != null) {
                    { Text(err) }
                } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focus.clearFocus()
                    vm.submit()
                }),
                shape = RoundedCornerShape(14.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SearchChip.entries) { c ->
                    val selected = chip == c
                    FilterChip(
                        selected = selected,
                        onClick = {
                            focus.clearFocus()
                            vm.toggleChip(c)
                        },
                        label = { Text(c.label) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                val current = submitted
                when {
                    blocked -> FullScreenMessage(
                        icon = Icons.Filled.Block,
                        title = "Search term blocked",
                        message = "This search contains a blocked term. Try a different keyword."
                    )
                    current == null -> FullScreenMessage(
                        icon = Icons.Filled.Search,
                        title = "Search the catalogue",
                        message = "Type a keyword and press search, or pick a category chip above."
                    )
                    else -> PullToRefreshBox(
                        isRefreshing = results.loadState.refresh is LoadState.Loading && results.itemCount > 0,
                        onRefresh = { results.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        PagedVideoGrid(
                            items = results,
                            onVideoClick = onVideoClick,
                            empty = {
                                val label = current.search ?: current.chip?.label ?: ""
                                FullScreenMessage(
                                    icon = Icons.Filled.SearchOff,
                                    title = "No results",
                                    message = "Nothing matched \"$label\". Try other words or another chip.",
                                    actionLabel = "Retry",
                                    onAction = { results.refresh() }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
