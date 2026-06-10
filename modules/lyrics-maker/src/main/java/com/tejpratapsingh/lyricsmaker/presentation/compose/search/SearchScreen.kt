package com.tejpratapsingh.lyricsmaker.presentation.compose.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.store.RecentSearchHelper
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsUiState
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: LyricsViewModel,
    onLyricsSelected: (LyricsResponse) -> Unit = {},
) {
    val context = LocalContext.current
    val query = viewModel.query.collectAsState()
    val uiState = viewModel.uiState.collectAsState()
    val recentSearches = remember { mutableStateOf(RecentSearchHelper.getSearches(context)) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var headerHeightPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(headerHeightPx) {
        scrollBehavior.state.heightOffsetLimit = -headerHeightPx
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        // Main Content
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (headerHeightPx + scrollBehavior.state.heightOffset).toInt(),
                        )
                    }
                    .padding(horizontal = 16.dp),
        ) {
            when (val state = uiState.value) {
                is LyricsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            Card(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    ),
                            ) {
                                state.lyrics.forEachIndexed { index, lyrics ->
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = "${lyrics.trackName} - ${lyrics.artistName}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        },
                                        supportingContent = {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Timer,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = lyrics.getReadableDuration(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                    )
                                                }
                                                Text(
                                                    text = lyrics.getLyrics(),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                        },
                                        leadingContent = {
                                            Icon(
                                                imageVector = Icons.Rounded.MusicNote,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        },
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                        modifier = Modifier.clickable { onLyricsSelected(lyrics) },
                                    )
                                    if (index < state.lyrics.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (recentSearches.value.isNotEmpty()) {
                                Text("Recent Searches:", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        ),
                                ) {
                                    recentSearches.value.forEachIndexed { index, search ->
                                        ListItem(
                                            headlineContent = { Text(search) },
                                            leadingContent = {
                                                Icon(
                                                    imageVector = Icons.Rounded.History,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                            modifier =
                                                Modifier.clickable {
                                                    viewModel.query.tryEmit(search)
                                                    keyboardController?.hide()
                                                    viewModel.searchLyrics(search)
                                                },
                                        )
                                        if (index < recentSearches.value.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Header
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        headerHeightPx = it.size.height.toFloat()
                    }
                    .offset {
                        IntOffset(
                            x = 0,
                            y = scrollBehavior.state.heightOffset.toInt(),
                        )
                    }
                    .statusBarsPadding()
                    .zIndex(1f),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Search Lyrics",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier =
                        Modifier
                            .align(CenterHorizontally)
                            .padding(16.dp),
                )
                OutlinedTextField(
                    value = query.value,
                    onValueChange = { viewModel.query.tryEmit(it) },
                    label = { Text("Search") },
                    singleLine = true,
                    trailingIcon = {
                        if (uiState.value is LyricsUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions =
                        KeyboardActions(
                            onSearch = {
                                val searchQuery = query.value.trim()
                                if (searchQuery.isNotBlank()) {
                                    keyboardController?.hide()
                                    RecentSearchHelper.saveSearch(context, searchQuery)
                                    recentSearches.value = RecentSearchHelper.getSearches(context)
                                    viewModel.searchLyrics(query = searchQuery)
                                }
                            },
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
