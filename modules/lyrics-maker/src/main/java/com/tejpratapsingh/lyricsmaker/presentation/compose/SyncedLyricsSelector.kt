package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
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
        val distFromTop = pointerYInRoot - listTopInRoot
        val distFromBottom = listBottomInRoot - pointerYInRoot
        return when {
            distFromTop in 0f..edgeZone -> {
                -maxScrollPxPerTick * (1f - distFromTop / edgeZone)
            }

            distFromBottom in 0f..edgeZone -> {
                maxScrollPxPerTick * (1f - distFromBottom / edgeZone)
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

/** Pixel-drag offset → list-item-index delta using average visible item height. */
private fun computeDeltaItems(
    listState: LazyListState,
    dragOffsetY: Float,
): Int {
    val visibleItems = listState.layoutInfo.visibleItemsInfo
    val avgItemHeight =
        if (visibleItems.isNotEmpty()) {
            visibleItems.sumOf { it.size } / visibleItems.size
        } else {
            56
        }
    return (dragOffsetY / avgItemHeight).roundToInt()
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
    onSelectionChanged: (List<SyncedLyricFrame>) -> Unit = {},
    onFinalize: (List<SyncedLyricFrame>) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val fps = provideCurrentConfig().fps
    val autoScroll = remember { AutoScrollState() }

    // Committed indices — only written on drag-end or reset.
    var startLyricIndex by remember { mutableIntStateOf(0) }
    var endLyricIndex by remember { mutableIntStateOf(initialEndIndex(viewModel.lyrics, fps)) }

    // Live pixel offsets, accumulated during an active drag.
    // Kept separate from the committed indices so a cancelled drag rolls back cleanly.
    var startDragOffsetY by remember { mutableFloatStateOf(0f) }
    var endDragOffsetY by remember { mutableFloatStateOf(0f) }

    var moveMode by remember { mutableStateOf(false) }

    // ── Live (in-flight) indices ──────────────────────────────────────────────
    // Recomputed on every drag event — these drive everything visible in the UI.
    val liveStartIndex by remember {
        derivedStateOf {
            val delta = computeDeltaItems(listState, startDragOffsetY)
            if (moveMode) {
                // In move mode the end moves with start, so clamp against (lastIndex - rangeSize)
                val rangeSize = endLyricIndex - startLyricIndex
                (startLyricIndex + delta).coerceIn(0, viewModel.lyrics.lastIndex - rangeSize)
            } else {
                (startLyricIndex + delta).coerceIn(0, endLyricIndex)
            }
        }
    }
    val liveEndIndex by remember {
        derivedStateOf {
            val endDelta = computeDeltaItems(listState, endDragOffsetY)
            if (moveMode && startDragOffsetY != 0f) {
                // End tracks start exactly in move mode
                val rangeSize = endLyricIndex - startLyricIndex
                liveStartIndex + rangeSize
            } else {
                (endLyricIndex + endDelta).coerceIn(liveStartIndex, viewModel.lyrics.lastIndex)
            }
        }
    }

    val selection by remember { derivedStateOf { RangeSelection(liveStartIndex, liveEndIndex) } }
    val selected by remember {
        derivedStateOf {
            if (viewModel.lyrics.isEmpty()) {
                emptyList()
            } else {
                viewModel.lyrics.subList(selection.minIndex, selection.maxIndex + 1)
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
                viewModel.lyrics.forEachIndexed { i, frame ->
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
            if (delta != 0f) listState.scrollBy(delta)
            delay(16L)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // ── Summary bar ───────────────────────────────────────────────────────
        if (viewModel.lyrics.isNotEmpty()) {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
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
                    Row {
                        TextButton(onClick = { onFinalize(selected) }) { Text("Finalize") }
                        TextButton(onClick = {
                            startLyricIndex = 0
                            endLyricIndex = initialEndIndex(viewModel.lyrics, fps)
                            startDragOffsetY = 0f
                            endDragOffsetY = 0f
                            moveMode = false
                        }) { Text("Reset") }
                    }
                }
            }
            HorizontalDivider()
            onSelectionChanged(selected)
        }

        if (viewModel.lyrics.isEmpty()) {
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
                contentPadding = PaddingValues(vertical = 8.dp),
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
                                color = MaterialTheme.colorScheme.primary,
                                listState = listState,
                                autoScroll = autoScroll,
                                moveMode = moveMode,
                                onMoveModeToggle = { moveMode = !moveMode },
                                onDrag = { dy -> startDragOffsetY += dy },
                                onDragEnd = {
                                    // Commit live index, reset offset
                                    startLyricIndex = liveStartIndex
                                    endLyricIndex = liveEndIndex // also commit end in move mode
                                    startDragOffsetY = 0f
                                },
                                onDragCancel = { startDragOffsetY = 0f },
                            )
                        }

                        is ListItem.EndHandle -> {
                            DragHandle(
                                label = "END",
                                color = MaterialTheme.colorScheme.tertiary,
                                listState = listState,
                                autoScroll = autoScroll,
                                onDrag = { dy -> endDragOffsetY += dy },
                                onDragEnd = {
                                    endLyricIndex = liveEndIndex
                                    endDragOffsetY = 0f
                                },
                                onDragCancel = { endDragOffsetY = 0f },
                            )
                        }

                        is ListItem.LyricItem -> {
                            LyricRow(
                                line = item.frame,
                                isSelected = selection.contains(item.index),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Handle composables ───────────────────────────────────────────────────────

/**
 * Start handle with move-mode toggle.
 *
 * Callbacks are intentionally thin — the parent owns all index arithmetic:
 * - [onDrag]      called every frame with the raw Y pixel delta
 * - [onDragEnd]   called on finger-up; parent commits and resets offset
 * - [onDragCancel] called on gesture cancel; parent resets offset to roll back
 */
@Composable
private fun StartDragHandle(
    color: Color,
    listState: LazyListState,
    autoScroll: AutoScrollState,
    moveMode: Boolean,
    onMoveModeToggle: () -> Unit,
    onDrag: (dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var selfTopInRoot by remember { mutableFloatStateOf(0f) }

    val bgAlpha =
        when {
            isDragging && moveMode -> 0.35f
            isDragging -> 0.25f
            moveMode -> 0.20f
            else -> 0.12f
        }

    Row(
        modifier =
            Modifier
                .zIndex(if (isDragging) 1f else 0f)
                .offset { IntOffset(0, dragOffsetY.roundToInt()) }
                .fillMaxWidth()
                .onGloballyPositioned { selfTopInRoot = it.positionInRoot().y }
                .background(color.copy(alpha = bgAlpha))
                .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onMoveModeToggle,
            colors = IconButtonDefaults.iconButtonColors(contentColor = color),
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
                                isDragging = true
                                autoScroll.isDragging = true
                                autoScroll.pointerYInRoot = selfTopInRoot + localOffset.y
                            },
                            onDrag = { _, dragAmount ->
                                dragOffsetY += dragAmount.y
                                autoScroll.pointerYInRoot += dragAmount.y
                                onDrag(dragAmount.y) // ← live update every frame
                            },
                            onDragEnd = {
                                onDragEnd()
                                dragOffsetY = 0f
                                isDragging = false
                                autoScroll.isDragging = false
                            },
                            onDragCancel = {
                                onDragCancel()
                                dragOffsetY = 0f
                                isDragging = false
                                autoScroll.isDragging = false
                            },
                        )
                    }.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.DragHandle, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (moveMode) "START  ·  drag moves range" else "START",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.DragHandle, null, tint = color, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * Generic draggable separator (END handle).
 * Same thin-callback design as [StartDragHandle].
 */
@Composable
private fun DragHandle(
    label: String,
    color: Color,
    listState: LazyListState,
    autoScroll: AutoScrollState,
    onDrag: (dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var selfTopInRoot by remember { mutableFloatStateOf(0f) }

    Box(
        modifier =
            Modifier
                .zIndex(if (isDragging) 1f else 0f)
                .offset { IntOffset(0, dragOffsetY.roundToInt()) }
                .fillMaxWidth()
                .onGloballyPositioned { selfTopInRoot = it.positionInRoot().y }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { localOffset ->
                            isDragging = true
                            autoScroll.isDragging = true
                            autoScroll.pointerYInRoot = selfTopInRoot + localOffset.y
                        },
                        onDrag = { _, dragAmount ->
                            dragOffsetY += dragAmount.y
                            autoScroll.pointerYInRoot += dragAmount.y
                            onDrag(dragAmount.y) // ← live update every frame
                        },
                        onDragEnd = {
                            onDragEnd()
                            dragOffsetY = 0f
                            isDragging = false
                            autoScroll.isDragging = false
                        },
                        onDragCancel = {
                            onDragCancel()
                            dragOffsetY = 0f
                            isDragging = false
                            autoScroll.isDragging = false
                        },
                    )
                }.background(color.copy(alpha = if (isDragging) 0.25f else 0.12f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.DragHandle, "Drag $label handle", tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.DragHandle, null, tint = color, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Lyric row ────────────────────────────────────────────────────────────────

@Composable
private fun LyricRow(
    line: SyncedLyricFrame,
    isSelected: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    },
                ).padding(12.dp),
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
