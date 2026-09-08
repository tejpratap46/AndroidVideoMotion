package com.tejpratapsingh.lyricsmaker.presentation.compose.lrc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.tejpratapsingh.lyricsmaker.data.lrc.LrcEditorLine
import com.tejpratapsingh.lyricsmaker.data.lrc.formatLrcEditorLinesToLrcString
import com.tejpratapsingh.lyricsmaker.data.lrc.parseTextToLrcEditorLines
import com.tejpratapsingh.lyricsmaker.presentation.compose.common.NumberScrubberBar
import java.util.Locale
import kotlin.math.max

enum class FieldType {
    START,
    END,
}

data class FocusedTimeField(
    val lineId: String,
    val fieldType: FieldType,
)

@Composable
fun LrcEditorScreen(
    modifier: Modifier = Modifier,
    initialRawText: String = "",
    onBack: () -> Unit = {},
    onFinalize: (lrcContent: String) -> Unit = {},
) {
    var rawText by remember { mutableStateOf(initialRawText) }
    var lines by remember { mutableStateOf(parseTextToLrcEditorLines(initialRawText)) }
    var showPasteDialog by remember { mutableStateOf(lines.isEmpty()) }
    var focusedTimeField by remember { mutableStateOf<FocusedTimeField?>(null) }

    val listState = rememberLazyListState()

    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    // Automatically scroll focused row into view above keyboard / NumberScrubberBar
    LaunchedEffect(focusedTimeField) {
        val current = focusedTimeField
        if (current != null) {
            val targetIdx = lines.indexOfFirst { it.id == current.lineId }
            if (targetIdx != -1) {
                listState.animateScrollToItem(targetIdx)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .imePadding(), // Resizes main Column when IME keyboard pops up
        ) {
            // ── Top Header ────────────────────────────────────────────────────────
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.statusBarsPadding(),
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
                    Text(
                        text = "LRC Editor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { showPasteDialog = true }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste / Raw")
                    }
                }
            }
            HorizontalDivider()

            // ── Main Content: Table View or Empty Prompt ───────────────────────────
            if (lines.isEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "No Lyrics Loaded",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Paste plain text or raw LRC format lyrics to start timing each line.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showPasteDialog = true }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Paste Lyrics")
                            }
                        }
                    }
                }
            } else {
                // Table Header Row: | Start Time | Lyrics Line | End Time |
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 1.dp,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Start (s)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(72.dp),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lyrics Line",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "End (s)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(72.dp),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.width(40.dp)) // Delete button column width
                    }
                }
                HorizontalDivider()

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding =
                        PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = if (focusedTimeField != null) 240.dp else 120.dp, // Buffer for NumberScrubberBar & FAB
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(
                        items = lines,
                        key = { _, line -> line.id },
                    ) { index, line ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp),
                                    ).padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Column 1: Start Time Target
                            val isStartFocused =
                                focusedTimeField?.lineId == line.id && focusedTimeField?.fieldType == FieldType.START
                            TimeInputField(
                                value = line.startTime,
                                isFocused = isStartFocused,
                                modifier = Modifier.width(72.dp),
                                onFocus = {
                                    focusedTimeField = FocusedTimeField(line.id, FieldType.START)
                                },
                                onValueEntered = { newStart ->
                                    val updatedLines = lines.toMutableList()
                                    updatedLines[index] = line.copy(startTime = newStart)
                                    lines = updatedLines
                                },
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Column 2: Editable Lyric Text
                            OutlinedTextField(
                                value = line.text,
                                onValueChange = { newText ->
                                    val updatedLines = lines.toMutableList()
                                    updatedLines[index] = line.copy(text = newText)
                                    lines = updatedLines
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("Line text...") },
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Column 3: End Time Target (With Auto-placement for next line!)
                            val isEndFocused =
                                focusedTimeField?.lineId == line.id && focusedTimeField?.fieldType == FieldType.END
                            TimeInputField(
                                value = line.endTime,
                                isFocused = isEndFocused,
                                modifier = Modifier.width(72.dp),
                                onFocus = {
                                    focusedTimeField = FocusedTimeField(line.id, FieldType.END)
                                },
                                onValueEntered = { newEnd ->
                                    val updatedLines = lines.toMutableList()
                                    updatedLines[index] = line.copy(endTime = newEnd)

                                    // Feature 1: Auto place next line after endtime + 1
                                    if (index + 1 < updatedLines.size) {
                                        val nextLine = updatedLines[index + 1]
                                        val nextStart = newEnd + 1.0f
                                        val nextEnd = max(nextLine.endTime, nextStart)
                                        updatedLines[index + 1] = nextLine.copy(startTime = nextStart, endTime = nextEnd)
                                    }

                                    lines = updatedLines
                                },
                            )

                            // Action: Delete Line
                            IconButton(
                                onClick = {
                                    val updatedLines = lines.toMutableList()
                                    updatedLines.removeAt(index)
                                    lines = updatedLines
                                    if (focusedTimeField?.lineId == line.id) {
                                        focusedTimeField = null
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Line",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }

                    // Add Line Button at bottom of list
                    item {
                        Button(
                            onClick = {
                                val lastEnd = lines.lastOrNull()?.endTime ?: 0f
                                val nextStart = if (lines.isNotEmpty()) lastEnd + 1.0f else 0f
                                val newLines = lines.toMutableList()
                                newLines.add(
                                    LrcEditorLine(
                                        startTime = nextStart,
                                        text = "",
                                        endTime = nextStart,
                                    ),
                                )
                                lines = newLines
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add New Line")
                        }
                    }
                }
            }
        }

        // ── Custom IME-Attached Scrubber Bar (NumberScrubberBar) ──────────────
        val currentFocused = focusedTimeField
        if (currentFocused != null) {
            val targetLineIndex = lines.indexOfFirst { it.id == currentFocused.lineId }
            if (targetLineIndex != -1) {
                val targetLine = lines[targetLineIndex]
                val isStart = currentFocused.fieldType == FieldType.START
                val currentValue = if (isStart) targetLine.startTime else targetLine.endTime
                val label = "Line ${targetLineIndex + 1} ${if (isStart) "Start Time" else "End Time"}"

                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    NumberScrubberBar(
                        label = label,
                        value = currentValue,
                        step = 1f,
                        unit = "s",
                        onValueChange = { updatedVal ->
                            val updatedLines = lines.toMutableList()
                            if (isStart) {
                                updatedLines[targetLineIndex] = targetLine.copy(startTime = updatedVal)
                            } else {
                                updatedLines[targetLineIndex] = targetLine.copy(endTime = updatedVal)
                                // Feature 1: Auto place next line during realtime drag too!
                                if (targetLineIndex + 1 < updatedLines.size) {
                                    val nextLine = updatedLines[targetLineIndex + 1]
                                    val nextStart = updatedVal + 1.0f
                                    val nextEnd = max(nextLine.endTime, nextStart)
                                    updatedLines[targetLineIndex + 1] = nextLine.copy(startTime = nextStart, endTime = nextEnd)
                                }
                            }
                            lines = updatedLines
                        },
                        onValueChangeFinished = { finalVal ->
                            val updatedLines = lines.toMutableList()
                            if (isStart) {
                                updatedLines[targetLineIndex] = targetLine.copy(startTime = finalVal)
                            } else {
                                updatedLines[targetLineIndex] = targetLine.copy(endTime = finalVal)
                                if (targetLineIndex + 1 < updatedLines.size) {
                                    val nextLine = updatedLines[targetLineIndex + 1]
                                    val nextStart = finalVal + 1.0f
                                    val nextEnd = max(nextLine.endTime, nextStart)
                                    updatedLines[targetLineIndex + 1] = nextLine.copy(startTime = nextStart, endTime = nextEnd)
                                }
                            }
                            lines = updatedLines
                        },
                    )
                }
            }
        }

        // ── FAB: Finalize / Done ──────────────────────────────────────────────
        if (lines.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = {
                    val lrcString = formatLrcEditorLinesToLrcString(lines)
                    onFinalize(lrcString)
                },
                icon = { Icon(Icons.Default.Done, contentDescription = null) },
                text = { Text("Done (${lines.size} lines)") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = if (focusedTimeField != null) 160.dp else 0.dp),
            )
        }

        // ── Paste Raw Lyrics Dialog ────────────────────────────────────────────
        if (showPasteDialog) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                "Paste Lyrics Text",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Paste plain lyrics line-by-line or LRC content with timestamps.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            OutlinedTextField(
                                value = rawText,
                                onValueChange = { rawText = it },
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                placeholder = { Text("Line 1\nLine 2\nLine 3...") },
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextButton(onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrEmpty()) {
                                        rawText = clip
                                    }
                                }) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("From Clipboard")
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                TextButton(onClick = { showPasteDialog = false }) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        lines = parseTextToLrcEditorLines(rawText)
                                        showPasteDialog = false
                                    },
                                    enabled = rawText.isNotBlank(),
                                ) {
                                    Text("Load Lyrics")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeInputField(
    value: Float,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    onFocus: () -> Unit,
    onValueEntered: (Float) -> Unit,
) {
    val formattedValue = if (value % 1f == 0f) String.format(Locale.US, "%.0f", value) else String.format(Locale.US, "%.2f", value)
    var textState by remember(value) { mutableStateOf(formattedValue) }

    val borderColor =
        if (isFocused) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }

    Box(
        modifier =
            modifier
                .height(56.dp)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp),
                ).clickable { onFocus() }
                .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedTextField(
            value = textState,
            onValueChange = { input ->
                textState = input
                val parsed = input.toFloatOrNull()
                if (parsed != null) {
                    onValueEntered(parsed)
                }
            },
            modifier = Modifier.fillMaxSize().onFocusChanged { if (it.isFocused) onFocus() },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}
