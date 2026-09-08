package com.tejpratapsingh.lyricsmaker.presentation.compose.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

/**
 * A generic, highly smooth number scrubber bar displayed above the soft keyboard or bottom sheet.
 *
 * Allows real-time adjustment of any numeric property by dragging left/right.
 * The scale is dynamically set to 10x the value's magnitude at drag start.
 * Upon releasing the drag, the thumb springs back to center and scale recalibrates.
 *
 * @param label Label of the field being edited (e.g. "Start Time", "End Time").
 * @param value Current numeric value.
 * @param minValue Minimum allowed value (default 0.0).
 * @param step Step increment for snapping (e.g. 1.0f for whole numbers, 0.1f for tenths, null for continuous).
 * @param unit Display unit string (e.g. "s", "px", "%").
 * @param onValueChange Callback during drag with updated value in real-time.
 * @param onValueChangeFinished Callback when drag ends to commit the final value.
 */
@Composable
fun NumberScrubberBar(
    label: String,
    value: Float,
    modifier: Modifier = Modifier,
    minValue: Float = 0f,
    step: Float? = null,
    unit: String = "s",
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    // Always keep updated references to callbacks & value without restarting gesture input
    val latestValue by rememberUpdatedState(value)
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    val latestOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)

    // State locked during drag gesture
    var isDragging by remember { mutableStateOf(false) }
    var dragStartValue by remember { mutableFloatStateOf(value) }
    var scaleAtStart by remember { mutableFloatStateOf(max(10f, abs(value) * 10f)) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }

    // Animatable offset for smooth spring back when drag finishes
    val dragOffset = remember { Animatable(0f) }

    // Helper to snap raw values to step increments if specified
    fun snapToStep(rawVal: Float): Float {
        val clamped = max(minValue, rawVal)
        return if (step != null && step > 0f) {
            round(clamped / step) * step
        } else {
            clamped
        }
    }

    // Display value: during drag calculate dynamically from dragOffset, otherwise use value
    val displayValue =
        if (isDragging) {
            val scaleFactor = scaleAtStart / max(1f, trackWidthPx)
            val delta = dragOffset.value * scaleFactor
            snapToStep(dragStartValue + delta)
        } else {
            snapToStep(value)
        }

    // Format text string depending on whether step is a whole number or fraction
    val formattedDisplayValue =
        if (step != null && step % 1f == 0f) {
            String.format(Locale.US, "%.0f%s", displayValue, unit)
        } else {
            String.format(Locale.US, "%.2f%s", displayValue, unit)
        }

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header Row: Field Label & Realtime Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formattedDisplayValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Scrubber Drag Track
            val primaryColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.outlineVariant
            val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp),
                        ).onGloballyPositioned { coords ->
                            trackWidthPx = coords.size.width.toFloat()
                        }.pointerInput(Unit) { // MUST use Unit so pointerInput is NEVER cancelled mid-drag!
                            detectDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    dragStartValue = latestValue
                                    scaleAtStart = max(10f, abs(latestValue) * 10f)
                                    coroutineScope.launch {
                                        dragOffset.snapTo(0f)
                                    }
                                },
                                onDragEnd = {
                                    val scaleFactor = scaleAtStart / max(1f, trackWidthPx)
                                    val delta = dragOffset.value * scaleFactor
                                    val finalVal = snapToStep(dragStartValue + delta)

                                    isDragging = false
                                    latestOnValueChangeFinished(finalVal)

                                    coroutineScope.launch {
                                        dragOffset.animateTo(0f, spring())
                                    }
                                },
                                onDragCancel = {
                                    isDragging = false
                                    coroutineScope.launch {
                                        dragOffset.animateTo(0f, spring())
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        val newOffset = dragOffset.value + dragAmount.x
                                        dragOffset.snapTo(newOffset)

                                        val scaleFactor = scaleAtStart / max(1f, trackWidthPx)
                                        val delta = newOffset * scaleFactor
                                        val calculatedVal = snapToStep(dragStartValue + delta)

                                        latestOnValueChange(calculatedVal)
                                    }
                                },
                            )
                        },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f
                    val centerX = width / 2f

                    // Draw background horizontal track line
                    drawLine(
                        color = trackColor,
                        start = Offset(16.dp.toPx(), centerY),
                        end = Offset(width - 16.dp.toPx(), centerY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )

                    // Draw scale ticks along the track
                    val numTicks = 11
                    val tickSpacing = (width - 32.dp.toPx()) / (numTicks - 1)
                    for (i in 0 until numTicks) {
                        val tickX = 16.dp.toPx() + (i * tickSpacing)
                        val isCenterTick = i == numTicks / 2
                        val tickHeight = if (isCenterTick) 16.dp.toPx() else 8.dp.toPx()
                        drawLine(
                            color = if (isCenterTick) primaryColor else tickColor,
                            start = Offset(tickX, centerY - (tickHeight / 2)),
                            end = Offset(tickX, centerY + (tickHeight / 2)),
                            strokeWidth = if (isCenterTick) 3.dp.toPx() else 1.5f.dp.toPx(),
                        )
                    }

                    // Thumb position relative to center
                    val thumbX = (centerX + dragOffset.value).coerceIn(16.dp.toPx(), width - 16.dp.toPx())

                    // Draw track fill line from center to thumb
                    drawLine(
                        color = primaryColor,
                        start = Offset(centerX, centerY),
                        end = Offset(thumbX, centerY),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )

                    // Draw draggable thumb indicator circle
                    drawCircle(
                        color = primaryColor,
                        radius = 12.dp.toPx(),
                        center = Offset(thumbX, centerY),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = Offset(thumbX, centerY),
                    )
                }
            }

            // Legend / instructions
            Text(
                text = "Drag left/right to adjust value • Release to set & reset center",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
