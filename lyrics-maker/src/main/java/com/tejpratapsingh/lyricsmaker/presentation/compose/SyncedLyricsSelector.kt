package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import kotlin.math.max
import kotlin.math.min

data class RangeSelection(val start: Int, val end: Int) {
    val minIndex get() = min(start, end)
    val maxIndex get() = max(start, end)
    fun contains(index: Int) = index in minIndex..maxIndex
}

@Composable
fun SyncedLyricsSelector(
    viewModel: LyricsViewModel,
    modifier: Modifier = Modifier,
    onSelectionChanged: (List<SyncedLyricFrame>) -> Unit = {},
    onFinalize: (List<SyncedLyricFrame>) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    var selection by remember { mutableStateOf<RangeSelection?>(null) }

    Column(modifier = modifier.fillMaxSize()) {

        // Selection summary bar
        if (selection != null) {
            val selected = viewModel.lyrics.subList(selection!!.minIndex, selection!!.maxIndex + 1)
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Selected ${selected.size} line(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        TextButton(onClick = { onFinalize(selected) }) {
                            Text("Finalize")
                        }
                        TextButton(onClick = { selection = null }) {
                            Text("Clear")
                        }
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(viewModel.lyrics) { index, line ->
                    val isSelected = selection?.contains(index) == true

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .combinedClickable(
                                onClick = {
                                    if (selection != null) {
                                        selection = selection!!.copy(end = index)
                                    }
                                },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selection = RangeSelection(index, index)
                                }
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Frame number
                        Text(
                            "[${line.frame}]",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.width(64.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        // Lyric text
                        Text(
                            text = line.text.ifEmpty { "…" },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}