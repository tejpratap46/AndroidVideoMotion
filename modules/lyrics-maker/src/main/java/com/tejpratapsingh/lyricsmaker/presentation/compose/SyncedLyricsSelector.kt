package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.ThemeBlue
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.ThemePink
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ─── Domain model ─────────────────────────────────────────────────────────────

data class RangeSelection(
    val start: Int,
    val end: Int,
) {
    val minIndex get() = min(start, end)
    val maxIndex get() = max(start, end)

    fun contains(index: Int) = index in minIndex..maxIndex
}

// ─── Display list item types ──────────────────────────────────────────────────

private sealed interface ListItem {
    data class LyricItem(
        val index: Int,
        val frame: SyncedLyricFrame,
    ) : ListItem

    data object StartHandle : ListItem

    data object EndHandle : ListItem
}

// ─── Auto-scroll state ────────────────────────────────────────────────────────

/**
 * Shared mutable state that coordinates auto-scrolling between the drag handles
 * and the [LazyColumn].
 *
 * Each handle reports its pointer Y in root (screen) coordinates while a drag
 * is in progress. A [LaunchedEffect] in [SyncedLyricsSelector] wakes up whenever
 * [isDragging] becomes true, then ticks at ~60 fps and calls [scrollDeltaForTick]
 * to decide how far and in which direction to scroll.
 *
 * Edge zone: the top and bottom [edgeFraction] of the list height trigger
 * scrolling. Scroll speed ramps linearly from 0 at the zone boundary to
 * [maxScrollPxPerTick] at the very edge.
 */
private class AutoScrollState {
    var isDragging by mutableStateOf(false)
    var pointerYInRoot by mutableFloatStateOf(0f)
    var listTopInRoot by mutableFloatStateOf(0f)
    var listBottomInRoot by mutableFloatStateOf(0f)

    val edgeFraction = 0.10f
    val maxScrollPxPerTick = 18f

    fun scrollDeltaForTick(): Float {
        if (!isDragging) return 0f
        val listHeight = listBottomInRoot - listTopInRoot
        if (listHeight <= 0f) return 0f
        val edgeZone = listHeight * edgeFraction

        return when {
            pointerYInRoot <= listTopInRoot + edgeZone -> {
                // Inside or ABOVE the top edge zone
                val distFromTop = pointerYInRoot - listTopInRoot
                // If distFromTop is negative, it means we're above the list;
                // clamp it to 0 for max speed.
                val ratio = (distFromTop / edgeZone).coerceIn(0f, 1f)
                -maxScrollPxPerTick * (1f - ratio)
            }

            pointerYInRoot >= listBottomInRoot - edgeZone -> {
                // Inside or BELOW the bottom edge zone
                val distFromBottom = listBottomInRoot - pointerYInRoot
                // If distFromBottom is negative, it means we're below the list;
                // clamp it to 0 for max speed.
                val ratio = (distFromBottom / edgeZone).coerceIn(0f, 1f)
                maxScrollPxPerTick * (1f - ratio)
            }

            else -> {
                0f
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun initialEndIndex(
    lyrics: List<SyncedLyricFrame>,
    fps: Int,
    targetSeconds: Int = 60,
): Int {
    if (lyrics.isEmpty()) return 0
    val targetFrames = targetSeconds * fps
    val idx = lyrics.indexOfLast { it.frame <= targetFrames }
    return if (idx < 0) 0 else idx
}

/**
 * Maps a screen-space Y coordinate [pointerYInRoot] to a lyric index.
 * It searches the [LazyColumn] visible items to find which one is closest to the pointer.
 */
private fun findLyricIndexAt(
    listState: LazyListState,
    listTopInRoot: Float,
    pointerYInRoot: Float,
): Int? {
    val visibleItems = listState.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return null

    // Convert screen pointer Y to list-relative Y.
    val relativeY = pointerYInRoot - listTopInRoot

    // If pointer is above the list, return the first lyric (0)
    if (relativeY < 0) return 0
    // If pointer is below the list, return the last lyric (total items - 1)
    // Note: This is an approximation since handles are also items, but clamping in
    // the caller will handle the exact lyrics count correctly.
    if (relativeY > listState.layoutInfo.viewportSize.height) {
        return listState.layoutInfo.totalItemsCount - 1
    }

    // Filter to just lyric items
    val lyricItems = visibleItems.filter { it.key.toString().startsWith("lyric_") }
    if (lyricItems.isEmpty()) return null

    // Find the lyric item that contains the relativeY, or the closest one.
    var closestIndex: Int? = null
    var minDistance = Float.MAX_VALUE

    for (item in lyricItems) {
        val itemTop = item.offset.toFloat()
        val itemBottom = (item.offset + item.size).toFloat()
        val itemCenter = (itemTop + itemBottom) / 2f

        if (relativeY >= itemTop && relativeY <= itemBottom) {
            return item.key
                .toString()
                .removePrefix("lyric_")
                .toInt()
        }

        val distance = kotlin.math.abs(relativeY - itemCenter)
        if (distance < minDistance) {
            minDistance = distance
            closestIndex =
                item.key
                    .toString()
                    .removePrefix("lyric_")
                    .toInt()
        }
    }

    return closestIndex
}

private fun formatDuration(
    frames: Int,
    fps: Int,
): String {
    val totalSecs = (frames / fps)
    val m = totalSecs / 60
    val s = totalSecs % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

// ─── Main composable ──────────────────────────────────────────────────────────

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
                Surface(tonalElevation = 2.dp) {
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

// ─── Handle composables ───────────────────────────────────────────────────────

/**
 * Start handle with move-mode toggle.
 */
@Composable
private fun StartDragHandle(
    color: Color,
    autoScroll: AutoScrollState,
    moveMode: Boolean,
    isBeingDragged: Boolean,
    livePointerYInRoot: Float,
    onMoveModeToggle: () -> Unit,
    onDragStart: (y: Float) -> Unit,
    onDrag: (y: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    var selfTopInRoot by remember { mutableFloatStateOf(0f) }

    val visualOffset by remember {
        derivedStateOf {
            if (isBeingDragged) {
                // The handle should be centered under livePointerYInRoot.
                // We calculate how much to offset it from its "natural" position in the list.
                (livePointerYInRoot - selfTopInRoot).roundToInt()
            } else {
                0
            }
        }
    }

    val bgAlpha =
        when {
            isBeingDragged && moveMode -> 0.8f
            isBeingDragged -> 0.7f
            moveMode -> 0.6f
            else -> 0.5f
        }

    Row(
        modifier =
            Modifier
                .zIndex(if (isBeingDragged) 1f else 0f)
                .offset { IntOffset(0, visualOffset) }
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    // We only update selfTopInRoot when NOT dragging to avoid feedback loops,
                    // OR we calculate natural position by subtracting current visualOffset.
                    selfTopInRoot = coords.positionInRoot().y - visualOffset
                }.background(color.copy(alpha = bgAlpha))
                .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onMoveModeToggle,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
        ) {
            Icon(
                imageVector = if (moveMode) Icons.Default.OpenWith else Icons.Default.LockOpen,
                contentDescription =
                    if (moveMode) {
                        "Move range (tap to resize only)"
                    } else {
                        "Resize start (tap to move whole range)"
                    },
                modifier = Modifier.size(18.dp),
            )
        }

        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { localOffset ->
                                val startY = selfTopInRoot + localOffset.y
                                autoScroll.isDragging = true
                                autoScroll.pointerYInRoot = startY
                                onDragStart(startY)
                            },
                            onDrag = { _, dragAmount ->
                                val newY = autoScroll.pointerYInRoot + dragAmount.y
                                autoScroll.pointerYInRoot = newY
                                onDrag(newY)
                            },
                            onDragEnd = {
                                autoScroll.isDragging = false
                                onDragEnd()
                            },
                            onDragCancel = {
                                autoScroll.isDragging = false
                                onDragCancel()
                            },
                        )
                    }.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.DragHandle, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (moveMode) "START  ·  drag moves range" else "START",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.DragHandle, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * Generic draggable separator (END handle).
 */
@Composable
private fun DragHandle(
    label: String,
    color: Color,
    autoScroll: AutoScrollState,
    isBeingDragged: Boolean,
    livePointerYInRoot: Float,
    onDragStart: (y: Float) -> Unit,
    onDrag: (y: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    var selfTopInRoot by remember { mutableFloatStateOf(0f) }

    val visualOffset by remember {
        derivedStateOf {
            if (isBeingDragged) {
                (livePointerYInRoot - selfTopInRoot).roundToInt()
            } else {
                0
            }
        }
    }

    Box(
        modifier =
            Modifier
                .zIndex(if (isBeingDragged) 1f else 0f)
                .offset { IntOffset(0, visualOffset) }
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    selfTopInRoot = coords.positionInRoot().y - visualOffset
                }.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { localOffset ->
                            val startY = selfTopInRoot + localOffset.y
                            autoScroll.isDragging = true
                            autoScroll.pointerYInRoot = startY
                            onDragStart(startY)
                        },
                        onDrag = { _, dragAmount ->
                            val newY = autoScroll.pointerYInRoot + dragAmount.y
                            autoScroll.pointerYInRoot = newY
                            onDrag(newY)
                        },
                        onDragEnd = {
                            autoScroll.isDragging = false
                            onDragEnd()
                        },
                        onDragCancel = {
                            autoScroll.isDragging = false
                            onDragCancel()
                        },
                    )
                }.background(color.copy(alpha = if (isBeingDragged) 0.8f else 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.DragHandle, "Drag $label handle", tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.DragHandle, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Lyric row ────────────────────────────────────────────────────────────────

@Composable
private fun LyricRow(
    line: SyncedLyricFrame,
    isSelected: Boolean,
    backgroundColor: Color,
    onLongClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(MaterialTheme.shapes.medium)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick,
                ).background(backgroundColor)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("[${line.frame}]", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(64.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = line.text.ifEmpty { "…" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "[${line.frame / provideCurrentConfig().fps} sec]",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(64.dp),
        )
    }
}
