package com.tejpratapsingh.motioneditor.ui.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

private val DEFAULT_PALETTE =
    listOf(
        "#FFFFFF",
        "#000000",
        "#FF3B30",
        "#FF9500",
        "#FFCC00",
        "#34C759",
        "#00C7BE",
        "#30B0C7",
        "#32ADE6",
        "#007AFF",
        "#5856D6",
        "#AF52DE",
        "#FF2D55",
        "#E5E5EA",
    )

private val RAINBOW_COLORS =
    listOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color.Cyan,
        Color.Blue,
        Color.Magenta,
        Color.Red,
    )

@Composable
fun ColorPickerInput(
    label: String,
    colorHex: String?,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    presetPalette: List<String> = DEFAULT_PALETTE,
) {
    var textValue by remember(colorHex) { mutableStateOf(colorHex ?: "#FFFFFF") }
    var showSpectrumPicker by remember { mutableStateOf(false) }

    val parsedColorInt: Int =
        remember(textValue) {
            try {
                textValue.toColorInt()
            } catch (e: Exception) {
                android.graphics.Color.WHITE
            }
        }
    val parsedColor = Color(parsedColorInt)

    // HSV State for Spectrum Picker
    val initialHsv = remember(parsedColorInt) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(parsedColorInt, hsv)
        hsv
    }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    val currentOnColorChange by rememberUpdatedState(onColorChange)

    LaunchedEffect(parsedColorInt) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(parsedColorInt, hsv)
        if (hsv[1] > 0.01f) {
            hue = hsv[0]
        }
        saturation = hsv[1]
        value = hsv[2]
    }

    // Local state update during dragging (ultra-fast 120 FPS)
    val updateColorFromHsv = { h: Float, s: Float, v: Float ->
        hue = h
        saturation = s
        value = v
        val hsvArr = floatArrayOf(h, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))
        val colorInt = android.graphics.Color.HSVToColor(hsvArr)
        val hex = String.format("#%06X", 0xFFFFFF and colorInt)
        textValue = hex
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Live Preview Color Box (Clickable to toggle spectrum picker)
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(parsedColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { showSpectrumPicker = !showSpectrumPicker },
            )

            // Hex Input Field
            OutlinedTextField(
                value = textValue,
                onValueChange = { inputHex ->
                    var formatted = inputHex
                    if (!formatted.startsWith("#") && formatted.isNotEmpty()) {
                        formatted = "#$formatted"
                    }
                    textValue = formatted
                    try {
                        formatted.toColorInt()
                        currentOnColorChange(formatted)
                    } catch (e: Exception) {
                        // Invalid hex code string so far
                    }
                },
                label = { Text("Hex Color") },
                placeholder = { Text("#RRGGBB") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        // Toggle Spectrum Button
        OutlinedButton(
            onClick = { showSpectrumPicker = !showSpectrumPicker },
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
        ) {
            Icon(Icons.Default.Palette, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (showSpectrumPicker) "Hide Spectrum Picker" else "Open Spectrum Picker")
        }

        // Expanded Spectrum Picker (2D Saturation/Value Box + Rainbow Hue Slider)
        if (showSpectrumPicker) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(12.dp),
                        ).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Color Spectrum",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                // 2D Saturation / Value Gradient Canvas
                val pureHueColor = remember(hue) {
                    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
                }
                val horizontalBrush = remember(pureHueColor) {
                    Brush.horizontalGradient(colors = listOf(Color.White, pureHueColor))
                }
                val verticalBrush = remember {
                    Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black))
                }

                val currentHueState by rememberUpdatedState(hue)
                val currentSatState by rememberUpdatedState(saturation)
                val currentValState by rememberUpdatedState(value)

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                ) {
                    Canvas(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val s = (offset.x / size.width).coerceIn(0f, 1f)
                                        val v = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                                        updateColorFromHsv(currentHueState, s, v)
                                        currentOnColorChange(textValue)
                                    }
                                }.pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val s = (offset.x / size.width).coerceIn(0f, 1f)
                                            val v = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                                            updateColorFromHsv(currentHueState, s, v)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val s = (change.position.x / size.width).coerceIn(0f, 1f)
                                            val v = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                                            updateColorFromHsv(currentHueState, s, v)
                                        },
                                        onDragEnd = {
                                            currentOnColorChange(textValue)
                                        },
                                        onDragCancel = {
                                            currentOnColorChange(textValue)
                                        },
                                    )
                                },
                    ) {
                        // Horizontal gradient: White -> Pure Hue Color
                        drawRect(brush = horizontalBrush)
                        // Vertical gradient: Transparent -> Black
                        drawRect(brush = verticalBrush)

                        // Selector Crosshair/Circle
                        val selectorX = currentSatState * size.width
                        val selectorY = (1f - currentValState) * size.height

                        drawCircle(
                            color = Color.White,
                            radius = 12.dp.toPx(),
                            center = Offset(selectorX, selectorY),
                            style = Stroke(width = 3.dp.toPx()),
                        )
                        drawCircle(
                            color = parsedColor,
                            radius = 9.dp.toPx(),
                            center = Offset(selectorX, selectorY),
                        )
                    }
                }

                // Rainbow Hue Slider Bar
                Text(
                    text = "Hue Spectrum",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val rainbowBrush = remember {
                    Brush.horizontalGradient(colors = RAINBOW_COLORS)
                }

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                ) {
                    Canvas(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val newHue = ((offset.x / size.width) * 360f).coerceIn(0f, 360f)
                                        updateColorFromHsv(newHue, currentSatState, currentValState)
                                        currentOnColorChange(textValue)
                                    }
                                }.pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val newHue = ((offset.x / size.width) * 360f).coerceIn(0f, 360f)
                                            updateColorFromHsv(newHue, currentSatState, currentValState)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val newHue = ((change.position.x / size.width) * 360f).coerceIn(0f, 360f)
                                            updateColorFromHsv(newHue, currentSatState, currentValState)
                                        },
                                        onDragEnd = {
                                            currentOnColorChange(textValue)
                                        },
                                        onDragCancel = {
                                            currentOnColorChange(textValue)
                                        },
                                    )
                                },
                    ) {
                        drawRect(brush = rainbowBrush)

                        val thumbX = (currentHueState / 360f) * size.width
                        drawCircle(
                            color = Color.White,
                            radius = 12.dp.toPx(),
                            center = Offset(thumbX, size.height / 2),
                            style = Stroke(width = 3.dp.toPx()),
                        )
                        drawCircle(
                            color = pureHueColor,
                            radius = 9.dp.toPx(),
                            center = Offset(thumbX, size.height / 2),
                        )
                    }
                }
            }
        }

        // Preset Color Palette Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            items(presetPalette) { hex ->
                val presetColor =
                    try {
                        Color(hex.toColorInt())
                    } catch (e: Exception) {
                        Color.Transparent
                    }

                val isSelected = textValue.equals(hex, ignoreCase = true)

                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(presetColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = CircleShape,
                            ).clickable {
                                textValue = hex
                                currentOnColorChange(hex)
                            },
                )
            }
        }
    }
}

/**
 * Utility to convert Color Int to Hex String.
 */
fun Int.toHexColorString(): String = String.format("#%08X", this)
