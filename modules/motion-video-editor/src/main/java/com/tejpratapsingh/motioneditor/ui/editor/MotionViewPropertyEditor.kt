package com.tejpratapsingh.motioneditor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.motioneditor.ui.editor.components.ColorPickerInput
import com.tejpratapsingh.motioneditor.ui.editor.components.DropdownSelector
import com.tejpratapsingh.motioneditor.ui.editor.components.ExpandingTextInput
import com.tejpratapsingh.motioneditor.ui.editor.components.FloatStepInput
import com.tejpratapsingh.motioneditor.ui.editor.components.NumberStepInput
import com.tejpratapsingh.motioneditor.ui.editor.components.OptionChipGroup

private val MOTION_TEXT_VARIANTS = listOf("H1", "H2", "H3", "H4", "H5", "H6", "P")
private val GRAVITY_OPTIONS =
    listOf("center", "top", "bottom", "left", "right", "center_horizontal", "center_vertical")
private val LAYOUT_DIMENSION_MODES = listOf("wrap_content", "match_parent", "custom")
private val AVAILABLE_EFFECT_TYPES =
    listOf(
        "FadeInEffect",
        "FadeOutEffect",
        "BlurEffect",
        "ZoomInEffect",
        "ZoomOutEffect",
        "GlitchEffect",
        "VibrateEffect",
        "VintageEffect",
        "SlideEffect",
    )

@Composable
fun MotionViewPropertyEditor(
    viewJson: JsonObject,
    onViewJsonChange: (JsonObject) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val type = viewJson.get("type")?.asString ?: "MotionView"

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Property Editor",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Section 1: Timing & Range
            EditorSectionCard(title = "Timing & Loop") {
                val startFrame = viewJson.get("startFrame")?.asInt ?: 0
                val endFrame = viewJson.get("endFrame")?.asInt ?: 100

                NumberStepInput(
                    label = "Start Frame",
                    value = startFrame,
                    minValue = 0,
                    onValueChange = { newStart ->
                        val updated = viewJson.deepCopy()
                        updated.addProperty("startFrame", newStart)
                        onViewJsonChange(updated)
                    },
                )

                NumberStepInput(
                    label = "End Frame",
                    value = endFrame,
                    minValue = startFrame,
                    onValueChange = { newEnd ->
                        val updated = viewJson.deepCopy()
                        updated.addProperty("endFrame", newEnd)
                        onViewJsonChange(updated)
                    },
                )

                // Loop Range
                val loopObj = viewJson.getAsJsonObject("loop")
                val loopStart = loopObj?.get("start")?.asInt ?: 0
                val loopEnd = loopObj?.get("end")?.asInt ?: 0

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NumberStepInput(
                        label = "Loop Start",
                        value = loopStart,
                        minValue = 0,
                        onValueChange = { newLoopStart ->
                            val updated = viewJson.deepCopy()
                            val newLoopObj = updated.getAsJsonObject("loop") ?: JsonObject()
                            newLoopObj.addProperty("start", newLoopStart)
                            newLoopObj.addProperty("end", maxOf(loopEnd, newLoopStart))
                            updated.add("loop", newLoopObj)
                            onViewJsonChange(updated)
                        },
                        modifier = Modifier.weight(1f),
                    )

                    NumberStepInput(
                        label = "Loop End",
                        value = loopEnd,
                        minValue = loopStart,
                        onValueChange = { newLoopEnd ->
                            val updated = viewJson.deepCopy()
                            val newLoopObj = updated.getAsJsonObject("loop") ?: JsonObject()
                            newLoopObj.addProperty("start", loopStart)
                            newLoopObj.addProperty("end", newLoopEnd)
                            updated.add("loop", newLoopObj)
                            onViewJsonChange(updated)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Section 2: Layout Info
            EditorSectionCard(title = "Layout & Positioning") {
                val layoutObj = viewJson.getAsJsonObject("layout") ?: JsonObject()

                val widthVal = layoutObj.get("width")?.asString ?: "wrap_content"
                val heightVal = layoutObj.get("height")?.asString ?: "wrap_content"
                val gravityVal = layoutObj.get("gravity")?.asString ?: "center"

                val widthMode =
                    if (widthVal in listOf("wrap_content", "match_parent")) widthVal else "custom"
                val heightMode =
                    if (heightVal in listOf("wrap_content", "match_parent")) heightVal else "custom"

                OptionChipGroup(
                    label = "Width Mode",
                    options = LAYOUT_DIMENSION_MODES,
                    selectedOption = widthMode,
                    onOptionSelected = { selected ->
                        val updated = viewJson.deepCopy()
                        val newLayout = updated.getAsJsonObject("layout") ?: JsonObject()
                        if (selected != "custom") {
                            newLayout.addProperty("width", selected)
                        } else {
                            newLayout.addProperty("width", "200")
                        }
                        updated.add("layout", newLayout)
                        onViewJsonChange(updated)
                    },
                )

                if (widthMode == "custom") {
                    val customWidth = widthVal.toIntOrNull() ?: 200
                    NumberStepInput(
                        label = "Custom Width (px)",
                        value = customWidth,
                        minValue = 10,
                        onValueChange = { newW ->
                            val updated = viewJson.deepCopy()
                            val newLayout = updated.getAsJsonObject("layout") ?: JsonObject()
                            newLayout.addProperty("width", newW.toString())
                            updated.add("layout", newLayout)
                            onViewJsonChange(updated)
                        },
                    )
                }

                OptionChipGroup(
                    label = "Height Mode",
                    options = LAYOUT_DIMENSION_MODES,
                    selectedOption = heightMode,
                    onOptionSelected = { selected ->
                        val updated = viewJson.deepCopy()
                        val newLayout = updated.getAsJsonObject("layout") ?: JsonObject()
                        if (selected != "custom") {
                            newLayout.addProperty("height", selected)
                        } else {
                            newLayout.addProperty("height", "200")
                        }
                        updated.add("layout", newLayout)
                        onViewJsonChange(updated)
                    },
                )

                if (heightMode == "custom") {
                    val customHeight = heightVal.toIntOrNull() ?: 200
                    NumberStepInput(
                        label = "Custom Height (px)",
                        value = customHeight,
                        minValue = 10,
                        onValueChange = { newH ->
                            val updated = viewJson.deepCopy()
                            val newLayout = updated.getAsJsonObject("layout") ?: JsonObject()
                            newLayout.addProperty("height", newH.toString())
                            updated.add("layout", newLayout)
                            onViewJsonChange(updated)
                        },
                    )
                }

                OptionChipGroup(
                    label = "Gravity",
                    options = GRAVITY_OPTIONS,
                    selectedOption = gravityVal,
                    onOptionSelected = { selectedGravity ->
                        val updated = viewJson.deepCopy()
                        val newLayout = updated.getAsJsonObject("layout") ?: JsonObject()
                        newLayout.addProperty("gravity", selectedGravity)
                        updated.add("layout", newLayout)
                        onViewJsonChange(updated)
                    },
                )

                // Margin
                val marginObj = layoutObj.getAsJsonObject("margin") ?: JsonObject()
                Text(
                    text = "Margins (px)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NumberStepInput(
                        label = "Left",
                        value = marginObj.get("left")?.asInt ?: 0,
                        onValueChange = { updateMargin(viewJson, "left", it, onViewJsonChange) },
                        modifier = Modifier.weight(1f),
                    )
                    NumberStepInput(
                        label = "Top",
                        value = marginObj.get("top")?.asInt ?: 0,
                        onValueChange = { updateMargin(viewJson, "top", it, onViewJsonChange) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    NumberStepInput(
                        label = "Right",
                        value = marginObj.get("right")?.asInt ?: 0,
                        onValueChange = { updateMargin(viewJson, "right", it, onViewJsonChange) },
                        modifier = Modifier.weight(1f),
                    )
                    NumberStepInput(
                        label = "Bottom",
                        value = marginObj.get("bottom")?.asInt ?: 0,
                        onValueChange = { updateMargin(viewJson, "bottom", it, onViewJsonChange) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Section 3: View Specific Content
            EditorSectionCard(title = "Content & Styling") {
                val hasText = viewJson.has("text")
                if (hasText) {
                    val currentText = viewJson.get("text")?.asString ?: ""
                    ExpandingTextInput(
                        label = "Text Content",
                        value = currentText,
                        onValueChange = { newText ->
                            val updated = viewJson.deepCopy()
                            updated.addProperty("text", newText)
                            onViewJsonChange(updated)
                        },
                    )

                    val textColor = viewJson.get("textColor")?.asString ?: "#FFFFFF"
                    ColorPickerInput(
                        label = "Text Color",
                        colorHex = textColor,
                        onColorChange = { newColor ->
                            val updated = viewJson.deepCopy()
                            updated.addProperty("textColor", newColor)
                            onViewJsonChange(updated)
                        },
                    )

                    val textVariant = viewJson.get("textSizeVariant")?.asString ?: "H1"
                    DropdownSelector(
                        label = "Text Variant",
                        options = MOTION_TEXT_VARIANTS,
                        selectedOption = textVariant,
                        onOptionSelected = { selectedVariant ->
                            val updated = viewJson.deepCopy()
                            updated.addProperty("textSizeVariant", selectedVariant)
                            onViewJsonChange(updated)
                        },
                    )

                    if (viewJson.has("writingSpeed")) {
                        val writingSpeed = viewJson.get("writingSpeed")?.asFloat ?: 1.0f
                        FloatStepInput(
                            label = "Writing Speed",
                            value = writingSpeed,
                            minValue = 0.1f,
                            maxValue = 10.0f,
                            step = 0.1f,
                            onValueChange = { newSpeed ->
                                val updated = viewJson.deepCopy()
                                updated.addProperty("writingSpeed", newSpeed)
                                onViewJsonChange(updated)
                            },
                        )
                    }

                    if (viewJson.has("highlightColor")) {
                        val highlightColor = viewJson.get("highlightColor")?.asString ?: "#FFCC00"
                        ColorPickerInput(
                            label = "Highlight Color",
                            colorHex = highlightColor,
                            onColorChange = { newColor ->
                                val updated = viewJson.deepCopy()
                                updated.addProperty("highlightColor", newColor)
                                onViewJsonChange(updated)
                            },
                        )
                    }
                }

                if (viewJson.has("style")) { // Progress bar style
                    val styleVal = viewJson.get("style")?.asString ?: "HORIZONTAL"
                    OptionChipGroup(
                        label = "Progress Bar Style",
                        options = listOf("HORIZONTAL", "CIRCULAR"),
                        selectedOption = styleVal,
                        onOptionSelected = { newStyle ->
                            val updated = viewJson.deepCopy()
                            updated.addProperty("style", newStyle)
                            onViewJsonChange(updated)
                        },
                    )
                }

                if ((viewJson.has("color") && !hasText) || type == "TranslucentMotionView") { // Color picker
                    val barColor = viewJson.get("color")?.asString ?: "#000000"
                    ColorPickerInput(
                        label = if (type == "TranslucentMotionView") "Background Color" else "Progress Bar Color",
                        colorHex = barColor,
                        onColorChange = { newColor ->
                            val updated = viewJson.deepCopy()
                            updated.addProperty("color", newColor)
                            onViewJsonChange(updated)
                        },
                    )
                }

                if ((type == "TranslucentMotionView") || viewJson.has("alpha")) {
                    val alphaVal = viewJson.get("alpha")?.asFloat ?: 1.0f
                    FloatStepInput(
                        label = "Alpha (Opacity)",
                        value = alphaVal,
                        minValue = 0.0f,
                        maxValue = 1.0f,
                        step = 0.1f,
                        onValueChange = { newAlpha ->
                            val updated = viewJson.deepCopy()
                            updated.addProperty("alpha", newAlpha)
                            onViewJsonChange(updated)
                        },
                    )
                }

                if (viewJson.has("orientation")) { // Gradient view orientation
                    val orientationVal = viewJson.get("orientation")?.asString ?: "HORIZONTAL"
                    OptionChipGroup(
                        label = "Gradient Orientation",
                        options = listOf("HORIZONTAL", "VERTICAL", "CIRCULAR"),
                        selectedOption = orientationVal,
                        onOptionSelected = { newOrientation ->
                            val updated = viewJson.deepCopy()
                            updated.addProperty("orientation", newOrientation)
                            onViewJsonChange(updated)
                        },
                    )
                }

                if (viewJson.has("asset")) {
                    var assetUrl = ""
                    val assetElem = viewJson.get("asset")
                    if (assetElem != null && assetElem.isJsonObject) {
                        assetUrl = assetElem.asJsonObject.get("url")?.asString ?: ""
                    } else if (assetElem != null && assetElem.isJsonPrimitive) {
                        assetUrl = assetElem.asString
                    }

                    OutlinedTextField(
                        value = assetUrl,
                        onValueChange = { newUrl ->
                            val updated = viewJson.deepCopy()
                            val newAssetObj = JsonObject()
                            newAssetObj.addProperty("type", "URL")
                            newAssetObj.addProperty("url", newUrl)
                            updated.add("asset", newAssetObj)
                            onViewJsonChange(updated)
                        },
                        label = { Text("Asset URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Section 4: Motion Effects
            EditorSectionCard(title = "Motion Effects") {
                val effectsArray = viewJson.getAsJsonArray("effects") ?: JsonArray()

                effectsArray.forEachIndexed { index, effectElem ->
                    if (effectElem.isJsonObject) {
                        val effectObj = effectElem.asJsonObject
                        val effectType = effectObj.get("type")?.asString ?: "Effect"
                        val eStart = effectObj.get("startFrame")?.asInt ?: 0
                        val eEnd = effectObj.get("endFrame")?.asInt ?: 100

                        Card(
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                ),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = "${index + 1}. $effectType",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(
                                        onClick = {
                                            val updated = viewJson.deepCopy()
                                            val newArray =
                                                updated.getAsJsonArray("effects") ?: JsonArray()
                                            newArray.remove(index)
                                            updated.add("effects", newArray)
                                            onViewJsonChange(updated)
                                        },
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove effect",
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    NumberStepInput(
                                        label = "Effect Start",
                                        value = eStart,
                                        onValueChange = { newStart ->
                                            val updated = viewJson.deepCopy()
                                            val arr = updated.getAsJsonArray("effects")
                                            val eff = arr[index].asJsonObject
                                            eff.addProperty("startFrame", newStart)
                                            onViewJsonChange(updated)
                                        },
                                        modifier = Modifier.weight(1f),
                                    )

                                    NumberStepInput(
                                        label = "Effect End",
                                        value = eEnd,
                                        onValueChange = { newEnd ->
                                            val updated = viewJson.deepCopy()
                                            val arr = updated.getAsJsonArray("effects")
                                            val eff = arr[index].asJsonObject
                                            eff.addProperty("endFrame", newEnd)
                                            onViewJsonChange(updated)
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }

                // Add New Effect
                var showAddEffectMenu by remember { mutableStateOf(false) }

                if (!showAddEffectMenu) {
                    OutlinedButton(
                        onClick = { showAddEffectMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Motion Effect")
                    }
                } else {
                    DropdownSelector(
                        label = "Select Effect to Add",
                        options = AVAILABLE_EFFECT_TYPES,
                        selectedOption = null,
                        onOptionSelected = { selectedEffectType ->
                            val updated = viewJson.deepCopy()
                            val arr = updated.getAsJsonArray("effects") ?: JsonArray()
                            val newEffect =
                                JsonObject().apply {
                                    addProperty("type", selectedEffectType)
                                    addProperty("startFrame", viewJson.get("startFrame")?.asInt ?: 0)
                                    addProperty("endFrame", viewJson.get("endFrame")?.asInt ?: 100)
                                }
                            arr.add(newEffect)
                            updated.add("effects", arr)
                            showAddEffectMenu = false
                            onViewJsonChange(updated)
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun EditorSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

private fun updateMargin(
    viewJson: JsonObject,
    key: String,
    valPx: Int,
    onViewJsonChange: (JsonObject) -> Unit,
) {
    val updated = viewJson.deepCopy()
    val layout = updated.getAsJsonObject("layout") ?: JsonObject()
    val margin = layout.getAsJsonObject("margin") ?: JsonObject()
    margin.addProperty(key, valPx)
    layout.add("margin", margin)
    updated.add("layout", layout)
    onViewJsonChange(updated)
}
