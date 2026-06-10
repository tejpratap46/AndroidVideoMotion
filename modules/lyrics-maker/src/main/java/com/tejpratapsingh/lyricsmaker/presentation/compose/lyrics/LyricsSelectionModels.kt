package com.tejpratapsingh.lyricsmaker.presentation.compose.lyrics

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import kotlin.math.max
import kotlin.math.min

data class RangeSelection(
    val start: Int,
    val end: Int,
) {
    val minIndex get() = min(start, end)
    val maxIndex get() = max(start, end)

    fun contains(index: Int) = index in minIndex..maxIndex
}

// ─── Display list item types ──────────────────────────────────────────────────

internal sealed interface ListItem {
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
internal class AutoScrollState {
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

internal fun initialEndIndex(
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
internal fun findLyricIndexAt(
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

internal fun formatDuration(
    frames: Int,
    fps: Int,
): String {
    val totalSecs = (frames / fps)
    val m = totalSecs / 60
    val s = totalSecs % 60
    return "$m:${s.toString().padStart(2, '0')}"
}
