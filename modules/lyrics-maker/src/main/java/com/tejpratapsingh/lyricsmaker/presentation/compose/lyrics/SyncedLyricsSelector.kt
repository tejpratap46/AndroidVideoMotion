package com.tejpratapsingh.lyricsmaker.presentation.compose.lyrics

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.ThemeBlue
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.ThemePink
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun SyncedLyricsSelector(
    viewModel: LyricsViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSelectionChanged: (List<SyncedLyricFrame>) -> Unit = {},
    onFinalize: (List<SyncedLyricFrame>) -> Unit = {},
) {
    val selectedLyric by viewModel.selectedLyric.collectAsState()
    val lyrics = remember(selectedLyric) { viewModel.lyrics }

    val listState = rememberLazyListState()
    val fps = provideCurrentConfig().fps
    val autoScroll = remember { AutoScrollState() }
    val haptic = LocalHapticFeedback.current

    // Committed indices — only written on drag-end or reset.
    var startLyricIndex by remember { mutableIntStateOf(0) }
    var endLyricIndex by remember { mutableIntStateOf(initialEndIndex(lyrics, fps)) }

    // Clamp committed indices when lyrics change
    LaunchedEffect(lyrics.size) {
        val lastIdx = lyrics.lastIndex
        if (lastIdx < 0) {
            startLyricIndex = 0
            endLyricIndex = 0
        } else {
            startLyricIndex = startLyricIndex.coerceIn(0, lastIdx)
            endLyricIndex = endLyricIndex.coerceIn(0, lastIdx)
        }
    }

    // Which handle is actively being dragged
    var activeHandle by remember { mutableStateOf<ListItem?>(null) }

    // Live finger position in root coordinates
    var livePointerYInRoot by remember { mutableFloatStateOf(0f) }

    var moveMode by remember { mutableStateOf(false) }

    // ── Live (in-flight) indices ──────────────────────────────────────────────
    val selection by remember {
        derivedStateOf {
            val lastIdx = lyrics.lastIndex
            if (lastIdx < 0) return@derivedStateOf RangeSelection(0, 0)

            val currentStart = startLyricIndex.coerceIn(0, lastIdx)
            val currentEnd = endLyricIndex.coerceIn(0, lastIdx)
            val safeStart = min(currentStart, currentEnd)
            val safeEnd = max(currentStart, currentEnd)
            val rangeSize = safeEnd - safeStart

            when (activeHandle) {
                ListItem.StartHandle -> {
                    val found = findLyricIndexAt(listState, autoScroll.listTopInRoot, livePointerYInRoot)
                    if (found != null) {
                        if (moveMode) {
                            val newStart = found.coerceIn(0, lastIdx - rangeSize)
                            RangeSelection(newStart, newStart + rangeSize)
                        } else {
                            val newStart = found.coerceIn(0, safeEnd)
                            RangeSelection(newStart, safeEnd)
                        }
                    } else {
                        RangeSelection(currentStart, currentEnd)
                    }
                }

                ListItem.EndHandle -> {
                    val found = findLyricIndexAt(listState, autoScroll.listTopInRoot, livePointerYInRoot)
                    if (found != null) {
                        if (moveMode) {
                            val newEnd = found.coerceIn(rangeSize, lastIdx)
                            RangeSelection(newEnd - rangeSize, newEnd)
                        } else {
                            val newEnd = found.coerceIn(safeStart, lastIdx)
                            RangeSelection(safeStart, newEnd)
                        }
                    } else {
                        RangeSelection(currentStart, currentEnd)
                    }
                }

                else -> {
                    RangeSelection(currentStart, currentEnd)
                }
            }
        }
    }

    val selected by remember {
        derivedStateOf {
            if (lyrics.isEmpty()) {
                emptyList()
            } else {
                lyrics.subList(selection.minIndex, (selection.maxIndex + 1).coerceAtMost(lyrics.size))
            }
        }
    }
    val selectedDurationLabel by remember {
        derivedStateOf {
            if (selected.size < 2) {
                "0:00"
            } else {
                formatDuration(selected.last().frame - selected.first().frame, fps)
            }
        }
    }
    val displayItems: List<ListItem> by remember {
        derivedStateOf {
            buildList {
                val lo = selection.minIndex
                val hi = selection.maxIndex
                lyrics.forEachIndexed { i, frame ->
                    if (i == lo) add(ListItem.StartHandle)
                    add(ListItem.LyricItem(i, frame))
                    if (i == hi) add(ListItem.EndHandle)
                }
            }
        }
    }

    // ── Auto-scroll loop ──────────────────────────────────────────────────────
    LaunchedEffect(autoScroll.isDragging) {
        if (!autoScroll.isDragging) return@LaunchedEffect
        while (isActive) {
            val delta = autoScroll.scrollDeltaForTick()
            if (delta != 0f) {
                listState.scrollBy(delta)
                // When scrolling, the list items move, but the finger stays at the same root Y.
                // findLyricIndexAt will re-run because it's in a derivedStateOf that depends
                // on listState.layoutInfo (indirectly, we need to make sure it reacts).
                // LazyListState's layoutInfo is observable, so derivedStateOf will re-evaluate.
            }
            delay(16L)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Summary bar ───────────────────────────────────────────────────────
            if (lyrics.isNotEmpty()) {
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${selected.size} line(s)  ·  $selectedDurationLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "selected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                HorizontalDivider()
                onSelectionChanged(selected)
            }

            if (lyrics.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("No Lyrics Selected", modifier = Modifier.align(Alignment.Center))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coords ->
                                val topLeft = coords.positionInRoot()
                                autoScroll.listTopInRoot = topLeft.y
                                autoScroll.listBottomInRoot = topLeft.y + coords.size.height
                            },
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                ) {
                    itemsIndexed(
                        displayItems,
                        key = { _, item ->
                            when (item) {
                                is ListItem.LyricItem -> "lyric_${item.index}"
                                ListItem.StartHandle -> "handle_start"
                                ListItem.EndHandle -> "handle_end"
                            }
                        },
                    ) { _, item ->
                        when (item) {
                            is ListItem.StartHandle -> {
                                StartDragHandle(
                                    color = ThemeBlue,
                                    autoScroll = autoScroll,
                                    moveMode = moveMode,
                                    isBeingDragged = activeHandle == ListItem.StartHandle,
                                    livePointerYInRoot = livePointerYInRoot,
                                    onMoveModeToggle = { moveMode = !moveMode },
                                    onDragStart = { y ->
                                        activeHandle = ListItem.StartHandle
                                        livePointerYInRoot = y
                                    },
                                    onDrag = { y -> livePointerYInRoot = y },
                                    onDragEnd = {
                                        startLyricIndex = selection.start
                                        endLyricIndex = selection.end
                                        activeHandle = null
                                    },
                                    onDragCancel = { activeHandle = null },
                                )
                            }

                            is ListItem.EndHandle -> {
                                DragHandle(
                                    label = "END",
                                    color = ThemePink,
                                    autoScroll = autoScroll,
                                    isBeingDragged = activeHandle == ListItem.EndHandle,
                                    livePointerYInRoot = livePointerYInRoot,
                                    onDragStart = { y ->
                                        activeHandle = ListItem.EndHandle
                                        livePointerYInRoot = y
                                    },
                                    onDrag = { y -> livePointerYInRoot = y },
                                    onDragEnd = {
                                        startLyricIndex = selection.start
                                        endLyricIndex = selection.end
                                        activeHandle = null
                                    },
                                    onDragCancel = { activeHandle = null },
                                )
                            }

                            is ListItem.LyricItem -> {
                                val isItemSelected = selection.contains(item.index)
                                val backgroundColor =
                                    if (isItemSelected) {
                                        val totalSelected = selection.maxIndex - selection.minIndex
                                        val ratio =
                                            if (totalSelected > 0) {
                                                (item.index - selection.minIndex).toFloat() / totalSelected
                                            } else {
                                                0f
                                            }
                                        lerp(ThemeBlue, ThemePink, ratio).copy(alpha = 0.2f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    }

                                LyricRow(
                                    line = item.frame,
                                    isSelected = isItemSelected,
                                    backgroundColor = backgroundColor,
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val currentStart = startLyricIndex
                                        val currentEnd = endLyricIndex

                                        if (item.index < currentStart) {
                                            startLyricIndex = item.index
                                        } else if (item.index > currentEnd) {
                                            endLyricIndex = item.index
                                        } else {
                                            // Inside selection, move closer boundary
                                            if (kotlin.math.abs(item.index - currentStart) <=
                                                kotlin.math.abs(item.index - currentEnd)
                                            ) {
                                                startLyricIndex = item.index
                                            } else {
                                                endLyricIndex = item.index
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── FABs ──────────────────────────────────────────────────────────────
        if (lyrics.isNotEmpty()) {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        startLyricIndex = 0
                        endLyricIndex = initialEndIndex(lyrics, fps)
                        activeHandle = null
                        moveMode = false
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Selection")
                }

                ExtendedFloatingActionButton(
                    onClick = { onFinalize(selected) },
                    icon = { Icon(Icons.Default.NavigateNext, contentDescription = null) },
                    text = { Text("Next") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
