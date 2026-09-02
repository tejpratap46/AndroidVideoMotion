package com.tejpratapsingh.motioneditor.ui

import java.lang.reflect.Field
import java.lang.reflect.Modifier as JavaModifier
import java.util.Locale
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView

private enum class PropertyCategory(val title: String) {
    ALL("All"),
    GENERAL("General"),
    LAYOUT("Layout"),
    EFFECTS("Effects")
}

@Composable
fun PropertyEditor(
    motionView: MotionView,
    sdui: JsonObject,
    modifier: Modifier = Modifier,
    onRefresh: (JsonObject) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PropertyCategory.ALL) }

    val effectsJson = if (sdui.has("effects") && sdui.get("effects").isJsonArray) {
        sdui.getAsJsonArray("effects")
    } else {
        null
    }
    val effectsCount = effectsJson?.size() ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. View Header Card
        item {
            PropertyEditorHeader(
                motionView = motionView,
                effectsCount = effectsCount
            )
        }

        // 2. Search & Category Filter Bar
        item {
            PropertyFilterBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
                hasEffects = effectsCount > 0
            )
        }

        // 3. General Properties Section
        if (selectedCategory == PropertyCategory.ALL || selectedCategory == PropertyCategory.GENERAL) {
            item {
                SectionCard(
                    title = "General Properties",
                    icon = Icons.Rounded.Tune,
                    initialExpanded = true
                ) {
                    ObjectProperties(
                        obj = motionView,
                        sdui = sdui,
                        searchQuery = searchQuery,
                        onRefresh = onRefresh
                    )
                }
            }
        }

        // 4. Layout Properties Section
        if (selectedCategory == PropertyCategory.ALL || selectedCategory == PropertyCategory.LAYOUT) {
            item {
                SectionCard(
                    title = "Layout & Spacing",
                    icon = Icons.Rounded.AspectRatio,
                    initialExpanded = false
                ) {
                    LayoutPropertySection(
                        sdui = sdui,
                        searchQuery = searchQuery,
                        onRefresh = onRefresh
                    )
                }
            }
        }

        // 5. Effects Section
        if ((selectedCategory == PropertyCategory.ALL || selectedCategory == PropertyCategory.EFFECTS) && effectsCount > 0 && effectsJson != null) {
            item {
                SectionCard(
                    title = "Effects ($effectsCount)",
                    icon = Icons.Rounded.AutoFixHigh,
                    initialExpanded = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        motionView.effects.forEachIndexed { index, effect ->
                            if (index < effectsJson.size()) {
                                val effectSdui = effectsJson.get(index).asJsonObject
                                EffectItemCard(
                                    index = index,
                                    effect = effect,
                                    effectSdui = effectSdui,
                                    searchQuery = searchQuery,
                                    onRefreshEffect = { updatedEffectSdui ->
                                        val updatedSdui = sdui.deepCopy()
                                        updatedSdui.getAsJsonArray("effects").set(index, updatedEffectSdui)
                                        onRefresh(updatedSdui)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyEditorHeader(
    motionView: MotionView,
    effectsCount: Int
) {
    val className = motionView.javaClass.simpleName
    val (icon, subtitle) = getIconAndCategoryForView(className)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = className,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = className.toHumanReadableName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (effectsCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = {},
                    label = { Text("$effectsCount FX", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropertyFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: PropertyCategory,
    onCategorySelect: (PropertyCategory) -> Unit,
    hasEffects: Boolean
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search properties...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        onSearchQueryChange("")
                        focusManager.clearFocus()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PropertyCategory.entries.forEach { category ->
                if (category == PropertyCategory.EFFECTS && !hasEffects) return@forEach
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category.title) },
                    leadingIcon = if (selectedCategory == category) {
                        { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    initialExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    content()
                }
            }
        }
    }
}

@Composable
private fun EffectItemCard(
    index: Int,
    effect: Any,
    effectSdui: JsonObject,
    searchQuery: String,
    onRefreshEffect: (JsonObject) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        text = effect.javaClass.simpleName.toHumanReadableName(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded || searchQuery.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ObjectProperties(
                        obj = effect,
                        sdui = effectSdui,
                        searchQuery = searchQuery,
                        onRefresh = onRefreshEffect
                    )
                }
            }
        }
    }
}

@Composable
fun LayoutPropertySection(
    sdui: JsonObject,
    searchQuery: String = "",
    onRefresh: (JsonObject) -> Unit
) {
    val layoutSdui = if (sdui.has("layout") && sdui.get("layout").isJsonObject) {
        sdui.getAsJsonObject("layout")
    } else {
        JsonObject()
    }

    val paddingSdui = if (layoutSdui.has("padding") && layoutSdui.get("padding").isJsonObject) {
        layoutSdui.getAsJsonObject("padding")
    } else {
        JsonObject()
    }

    val marginSdui = if (layoutSdui.has("margin") && layoutSdui.get("margin").isJsonObject) {
        layoutSdui.getAsJsonObject("margin")
    } else {
        JsonObject()
    }

    val currentWidth = if (layoutSdui.has("width")) layoutSdui.get("width").asString else "wrap_content"
    val currentHeight = if (layoutSdui.has("height")) layoutSdui.get("height").asString else "wrap_content"

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Dimensions
        if (matchesSearch("Width Height Dimension", searchQuery)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dimensions",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                DimensionSelector(
                    label = "Width",
                    value = currentWidth,
                    onValueChange = { newValue ->
                        val updatedLayout = layoutSdui.deepCopy()
                        updatedLayout.addProperty("width", newValue)
                        val updatedSdui = sdui.deepCopy()
                        updatedSdui.add("layout", updatedLayout)
                        onRefresh(updatedSdui)
                    }
                )

                DimensionSelector(
                    label = "Height",
                    value = currentHeight,
                    onValueChange = { newValue ->
                        val updatedLayout = layoutSdui.deepCopy()
                        updatedLayout.addProperty("height", newValue)
                        val updatedSdui = sdui.deepCopy()
                        updatedSdui.add("layout", updatedLayout)
                        onRefresh(updatedSdui)
                    }
                )
            }
        }

        // Visual Box Model for Padding & Margin
        if (matchesSearch("Padding Margin Spacing", searchQuery)) {
            BoxModelEditor(
                paddingLeft = if (paddingSdui.has("left")) paddingSdui.get("left").asInt else 0,
                paddingTop = if (paddingSdui.has("top")) paddingSdui.get("top").asInt else 0,
                paddingRight = if (paddingSdui.has("right")) paddingSdui.get("right").asInt else 0,
                paddingBottom = if (paddingSdui.has("bottom")) paddingSdui.get("bottom").asInt else 0,
                marginLeft = if (marginSdui.has("left")) marginSdui.get("left").asInt else 0,
                marginTop = if (marginSdui.has("top")) marginSdui.get("top").asInt else 0,
                marginRight = if (marginSdui.has("right")) marginSdui.get("right").asInt else 0,
                marginBottom = if (marginSdui.has("bottom")) marginSdui.get("bottom").asInt else 0,
                onPaddingChange = { left, top, right, bottom ->
                    val updatedPadding = paddingSdui.deepCopy()
                    updatedPadding.addProperty("left", left)
                    updatedPadding.addProperty("top", top)
                    updatedPadding.addProperty("right", right)
                    updatedPadding.addProperty("bottom", bottom)
                    val updatedLayout = layoutSdui.deepCopy()
                    updatedLayout.add("padding", updatedPadding)
                    val updatedSdui = sdui.deepCopy()
                    updatedSdui.add("layout", updatedLayout)
                    onRefresh(updatedSdui)
                },
                onMarginChange = { left, top, right, bottom ->
                    val updatedMargin = marginSdui.deepCopy()
                    updatedMargin.addProperty("left", left)
                    updatedMargin.addProperty("top", top)
                    updatedMargin.addProperty("right", right)
                    updatedMargin.addProperty("bottom", bottom)
                    val updatedLayout = layoutSdui.deepCopy()
                    updatedLayout.add("margin", updatedMargin)
                    val updatedSdui = sdui.deepCopy()
                    updatedSdui.add("layout", updatedLayout)
                    onRefresh(updatedSdui)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DimensionSelector(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val presets = listOf("wrap_content", "match_parent", "Custom")
    val selectedPresetIndex = when (value) {
        "wrap_content" -> 0
        "match_parent" -> 1
        else -> 2
    }

    var customText by remember(value) {
        mutableStateOf(if (selectedPresetIndex == 2) value else "100")
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            presets.forEachIndexed { index, presetLabel ->
                SegmentedButton(
                    selected = selectedPresetIndex == index,
                    onClick = {
                        when (index) {
                            0 -> onValueChange("wrap_content")
                            1 -> onValueChange("match_parent")
                            else -> onValueChange(customText)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = presets.size)
                ) {
                    Text(presetLabel, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (selectedPresetIndex == 2) {
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedTextField(
                value = customText,
                onValueChange = {
                    customText = it
                    onValueChange(it)
                },
                label = { Text("Custom $label Value") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun BoxModelEditor(
    paddingLeft: Int,
    paddingTop: Int,
    paddingRight: Int,
    paddingBottom: Int,
    marginLeft: Int,
    marginTop: Int,
    marginRight: Int,
    marginBottom: Int,
    onPaddingChange: (Int, Int, Int, Int) -> Unit,
    onMarginChange: (Int, Int, Int, Int) -> Unit
) {
    var isPaddingLocked by remember { mutableStateOf(false) }
    var isMarginLocked by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // MARGIN OUTER BOX
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Margin (Outer)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { isMarginLocked = !isMarginLocked },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isMarginLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = "Lock margin sides",
                            modifier = Modifier.size(16.dp),
                            tint = if (isMarginLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Top Margin
                CompactNumberInput(
                    value = marginTop,
                    onValueChange = { v ->
                        if (isMarginLocked) onMarginChange(v, v, v, v)
                        else onMarginChange(marginLeft, v, marginRight, marginBottom)
                    },
                    modifier = Modifier.width(90.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Margin
                    CompactNumberInput(
                        value = marginLeft,
                        onValueChange = { v ->
                            if (isMarginLocked) onMarginChange(v, v, v, v)
                            else onMarginChange(v, marginTop, marginRight, marginBottom)
                        },
                        modifier = Modifier.width(70.dp)
                    )

                    // PADDING INNER BOX
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Padding (Inner)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                IconButton(
                                    onClick = { isPaddingLocked = !isPaddingLocked },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPaddingLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                        contentDescription = "Lock padding sides",
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isPaddingLocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Top Padding
                            CompactNumberInput(
                                value = paddingTop,
                                onValueChange = { v ->
                                    if (isPaddingLocked) onPaddingChange(v, v, v, v)
                                    else onPaddingChange(paddingLeft, v, paddingRight, paddingBottom)
                                },
                                modifier = Modifier.width(80.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Padding
                                CompactNumberInput(
                                    value = paddingLeft,
                                    onValueChange = { v ->
                                        if (isPaddingLocked) onPaddingChange(v, v, v, v)
                                        else onPaddingChange(v, paddingTop, paddingRight, paddingBottom)
                                    },
                                    modifier = Modifier.width(64.dp)
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "CONTENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Right Padding
                                CompactNumberInput(
                                    value = paddingRight,
                                    onValueChange = { v ->
                                        if (isPaddingLocked) onPaddingChange(v, v, v, v)
                                        else onPaddingChange(paddingLeft, paddingTop, v, paddingBottom)
                                    },
                                    modifier = Modifier.width(64.dp)
                                )
                            }

                            // Bottom Padding
                            CompactNumberInput(
                                value = paddingBottom,
                                onValueChange = { v ->
                                    if (isPaddingLocked) onPaddingChange(v, v, v, v)
                                    else onPaddingChange(paddingLeft, paddingTop, paddingRight, v)
                                },
                                modifier = Modifier.width(80.dp)
                            )
                        }
                    }

                    // Right Margin
                    CompactNumberInput(
                        value = marginRight,
                        onValueChange = { v ->
                            if (isMarginLocked) onMarginChange(v, v, v, v)
                            else onMarginChange(marginLeft, marginTop, v, marginBottom)
                        },
                        modifier = Modifier.width(70.dp)
                    )
                }

                // Bottom Margin
                CompactNumberInput(
                    value = marginBottom,
                    onValueChange = { v ->
                        if (isMarginLocked) onMarginChange(v, v, v, v)
                        else onMarginChange(marginLeft, marginTop, marginRight, v)
                    },
                    modifier = Modifier.width(90.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactNumberInput(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var textState by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textState,
        onValueChange = { input ->
            textState = input
            input.toIntOrNull()?.let { onValueChange(it) }
        },
        modifier = modifier.height(40.dp),
        textStyle = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun ObjectProperties(
    obj: Any,
    sdui: JsonObject,
    searchQuery: String = "",
    onRefresh: (JsonObject) -> Unit
) {
    val sduiKeys = remember(sdui) { sdui.keySet() }
    val fields = remember(obj) { getAllFields(obj.javaClass) }

    val excludedNames = setOf("motionView", "effects", "assets", "motionConfig", "layoutInfo", "loop", "type")

    val visibleFields = fields.filter { field ->
        !JavaModifier.isStatic(field.modifiers) &&
            !field.name.contains("$") &&
            field.name !in excludedNames &&
            field.name in sduiKeys &&
            matchesSearch(field.name, searchQuery)
    }

    if (visibleFields.isEmpty() && searchQuery.isNotEmpty()) {
        Text(
            text = "No properties match '$searchQuery'",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        visibleFields.forEach { field ->
            field.isAccessible = true
            val jsonElement = sdui.get(field.name)

            if (jsonElement != null && jsonElement.isJsonPrimitive) {
                val primitive = jsonElement.asJsonPrimitive
                val fieldLabel = field.name.toHumanReadableName()

                when {
                    primitive.isBoolean -> {
                        BooleanProperty(
                            label = fieldLabel,
                            value = primitive.asBoolean
                        ) { newValue ->
                            val updatedSdui = sdui.deepCopy()
                            updatedSdui.addProperty(field.name, newValue)
                            onRefresh(updatedSdui)
                        }
                    }

                    primitive.isNumber -> {
                        val numValue = primitive.asNumber
                        val isInteger = numValue is Int || numValue is Long || numValue is Short || numValue is Byte

                        NumericProperty(
                            fieldName = field.name,
                            label = fieldLabel,
                            value = numValue.toString(),
                            isInteger = isInteger
                        ) { newValue ->
                            val updatedSdui = sdui.deepCopy()
                            if (isInteger) {
                                updatedSdui.addProperty(field.name, newValue.toIntOrNull() ?: 0)
                            } else {
                                updatedSdui.addProperty(field.name, newValue.toFloatOrNull() ?: 0f)
                            }
                            onRefresh(updatedSdui)
                        }
                    }

                    primitive.isString -> {
                        val strValue = primitive.asString
                        val isEnumField = field.type.isEnum || field.name == "textSizeVariant"

                        if (isEnumField) {
                            val options: List<String> = if (field.type.isEnum) {
                                field.type.enumConstants?.map { (it as Enum<*>).name }
                                    ?: MotionTextVariant.entries.map { it.name }
                            } else {
                                MotionTextVariant.entries.map { it.name }
                            }
                            DropdownProperty(
                                label = fieldLabel,
                                selectedValue = strValue,
                                options = options
                            ) { newValue ->
                                val updatedSdui = sdui.deepCopy()
                                updatedSdui.addProperty(field.name, newValue)
                                onRefresh(updatedSdui)
                            }
                        } else if (isColorHex(strValue)) {
                            ColorProperty(
                                label = fieldLabel,
                                value = strValue
                            ) { newValue ->
                                val updatedSdui = sdui.deepCopy()
                                updatedSdui.addProperty(field.name, newValue)
                                onRefresh(updatedSdui)
                            }
                        } else {
                            StringProperty(
                                label = fieldLabel,
                                value = strValue
                            ) { newValue ->
                                val updatedSdui = sdui.deepCopy()
                                updatedSdui.addProperty(field.name, newValue)
                                onRefresh(updatedSdui)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun matchesSearch(fieldName: String, query: String): Boolean {
    if (query.isBlank()) return true
    val humanName = fieldName.toHumanReadableName()
    return fieldName.contains(query, ignoreCase = true) || humanName.contains(query, ignoreCase = true)
}

private fun String.toHumanReadableName(): String {
    if (isEmpty()) return this
    val result = StringBuilder()
    result.append(this[0].uppercaseChar())
    for (i in 1 until length) {
        val c = this[i]
        if (c.isUpperCase()) {
            result.append(' ').append(c)
        } else {
            result.append(c)
        }
    }
    return result.toString()
}

private fun getIconAndCategoryForView(className: String): Pair<ImageVector, String> {
    return when {
        className.contains("Text", ignoreCase = true) || className.contains("Title", ignoreCase = true) ->
            Icons.Rounded.TextFields to "Text & Typography View"
        className.contains("Image", ignoreCase = true) || className.contains("Photo", ignoreCase = true) ->
            Icons.Rounded.Image to "Graphic Image View"
        className.contains("Video", ignoreCase = true) || className.contains("Player", ignoreCase = true) ->
            Icons.Rounded.Movie to "Video Media View"
        className.contains("Stack", ignoreCase = true) || className.contains("Container", ignoreCase = true) ->
            Icons.Rounded.Layers to "Layout Container View"
        className.contains("Background", ignoreCase = true) ->
            Icons.Rounded.Palette to "Background View"
        else ->
            Icons.Rounded.Code to "Motion View Element"
    }
}

private fun isColorHex(value: String): Boolean {
    return value.startsWith("#") && (value.length == 7 || value.length == 9)
}

private fun getAllFields(clazz: Class<*>): List<Field> {
    val fields = mutableListOf<Field>()
    var current: Class<*>? = clazz
    while (current != null && current != Any::class.java) {
        fields.addAll(current.declaredFields)
        current = current.superclass
    }
    return fields
}

@Composable
fun BooleanProperty(
    label: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    var checkedState by remember(value) { mutableStateOf(value) }

    Surface(
        onClick = {
            val next = !checkedState
            checkedState = next
            onValueChange(next)
        },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checkedState,
                onCheckedChange = {
                    checkedState = it
                    onValueChange(it)
                }
            )
        }
    }
}

@Composable
fun NumericProperty(
    fieldName: String,
    label: String,
    value: String,
    isInteger: Boolean,
    onValueChange: (String) -> Unit
) {
    var textState by remember(value) { mutableStateOf(value) }

    val isAlpha = fieldName.equals("alpha", ignoreCase = true) || fieldName.equals("opacity", ignoreCase = true)
    val isRotation = fieldName.contains("rotation", ignoreCase = true) || fieldName.contains("angle", ignoreCase = true)
    val isScale = fieldName.contains("scale", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (isInteger) {
                                val current = textState.toIntOrNull() ?: 0
                                val next = (current - 1).toString()
                                textState = next
                                onValueChange(next)
                            } else {
                                val current = textState.toFloatOrNull() ?: 0f
                                val next = String.format(Locale.ROOT, "%.2f", (current - 0.1f).coerceAtLeast(0f))
                                textState = next
                                onValueChange(next)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    OutlinedTextField(
                        value = textState,
                        onValueChange = {
                            textState = it
                            onValueChange(it)
                        },
                        modifier = Modifier.width(76.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal
                        )
                    )

                    IconButton(
                        onClick = {
                            if (isInteger) {
                                val current = textState.toIntOrNull() ?: 0
                                val next = (current + 1).toString()
                                textState = next
                                onValueChange(next)
                            } else {
                                val current = textState.toFloatOrNull() ?: 0f
                                val next = String.format(Locale.ROOT, "%.2f", current + 0.1f)
                                textState = next
                                onValueChange(next)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Slider for Range Properties
            if (isAlpha) {
                val floatVal = textState.toFloatOrNull() ?: 1f
                Slider(
                    value = floatVal.coerceIn(0f, 1f),
                    onValueChange = { newF ->
                        val formatted = String.format(Locale.ROOT, "%.2f", newF)
                        textState = formatted
                        onValueChange(formatted)
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (isRotation) {
                val floatVal = textState.toFloatOrNull() ?: 0f
                Slider(
                    value = floatVal.coerceIn(-360f, 360f),
                    onValueChange = { newF ->
                        val formatted = if (isInteger) newF.toInt().toString() else String.format(Locale.ROOT, "%.1f", newF)
                        textState = formatted
                        onValueChange(formatted)
                    },
                    valueRange = -360f..360f,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (isScale) {
                val floatVal = textState.toFloatOrNull() ?: 1f
                Slider(
                    value = floatVal.coerceIn(0.1f, 5f),
                    onValueChange = { newF ->
                        val formatted = String.format(Locale.ROOT, "%.2f", newF)
                        textState = formatted
                        onValueChange(formatted)
                    },
                    valueRange = 0.1f..5f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownProperty(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (options.size <= 4) {
                // Choice Chips / Segmented buttons for short option sets
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { option ->
                        FilterChip(
                            selected = selectedValue.equals(option, ignoreCase = true),
                            onClick = { onValueChange(option) },
                            label = { Text(option, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedValue,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = option) },
                                onClick = {
                                    expanded = false
                                    onValueChange(option)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StringProperty(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var textState by remember(value) { mutableStateOf(value) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = textState,
                onValueChange = {
                    textState = it
                    onValueChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = false,
                maxLines = 4,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
fun ColorProperty(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val color = remember(value) {
        try {
            Color(AndroidColor.parseColor(value))
        } catch (_: Exception) {
            Color.Transparent
        }
    }

    Surface(
        onClick = { showPicker = true },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value.uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color, CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
        }
    }

    if (showPicker) {
        ColorPickerDialog(
            initialColor = value,
            onColorSelected = {
                showPicker = false
                onValueChange(it)
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hexInput by remember { mutableStateOf(initialColor) }

    var currentColor by remember {
        mutableStateOf(
            try {
                Color(AndroidColor.parseColor(initialColor))
            } catch (_: Exception) {
                Color.Red
            }
        )
    }

    val hsv = remember(currentColor) {
        val hsvArr = FloatArray(3)
        AndroidColor.colorToHSV(currentColor.toArgb(), hsvArr)
        hsvArr
    }

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }

    fun updateFromHsv() {
        currentColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value)))
        hexInput = String.format(Locale.ROOT, "#%06X", 0xFFFFFF and currentColor.toArgb())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Color Picker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(currentColor, RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Hex Input Field
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input
                        if (isColorHex(input)) {
                            try {
                                val parsed = Color(AndroidColor.parseColor(input))
                                currentColor = parsed
                                val hsvArr = FloatArray(3)
                                AndroidColor.colorToHSV(parsed.toArgb(), hsvArr)
                                hue = hsvArr[0]
                                saturation = hsvArr[1]
                                value = hsvArr[2]
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("Hex Color Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    shape = RoundedCornerShape(8.dp)
                )

                // Saturation / Value Box
                SaturationValueBox(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onValueChange = { s, v ->
                        saturation = s
                        value = v
                        updateFromHsv()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(12.dp))
                )

                // Hue Slider
                HueSlider(
                    hue = hue,
                    onHueChange = { h ->
                        hue = h
                        updateFromHsv()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                HorizontalDivider()

                Text(
                    text = "Preset Palette",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val commonColors = listOf(
                    "#000000", "#FFFFFF", "#FF3B30", "#34C759", "#007AFF",
                    "#FFCC00", "#5AC8FA", "#AF52DE", "#8E8E93", "#FF9500",
                    "#5856D6", "#FF2D55", "#A2845E", "#E5E5EA", "#1C1C1E"
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(36.dp),
                    modifier = Modifier.height(90.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(commonColors) { hex ->
                        val color = Color(AndroidColor.parseColor(hex))
                        val isSelected = String.format(Locale.ROOT, "#%06X", 0xFFFFFF and currentColor.toArgb()).equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable {
                                    hexInput = hex
                                    currentColor = color
                                    val hsvArr = FloatArray(3)
                                    AndroidColor.colorToHSV(color.toArgb(), hsvArr)
                                    hue = hsvArr[0]
                                    saturation = hsvArr[1]
                                    value = hsvArr[2]
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalHex = if (hexInput.startsWith("#")) hexInput else "#$hexInput"
                    onColorSelected(finalHex)
                }
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SaturationValueBox(
    hue: Float,
    saturation: Float,
    value: Float,
    onValueChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .pointerInput(hue) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val v = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                    onValueChange(s, v)
                }
            }
            .pointerInput(hue) {
                detectTapGestures { offset ->
                    val s = (offset.x / size.width).coerceIn(0f, 1f)
                    val v = (1f - offset.y / size.height).coerceIn(0f, 1f)
                    onValueChange(s, v)
                }
            }
    ) {
        val hueColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White, hueColor)
            )
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black)
            )
        )

        val x = saturation * size.width
        val y = (1f - value) * size.height
        drawCircle(
            color = if (value > 0.5f) Color.Black else Color.White,
            radius = 8.dp.toPx(),
            center = Offset(x, y),
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}

@Composable
private fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val h = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                    onHueChange(h)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val h = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                    onHueChange(h)
                }
            }
    ) {
        val hueColors = listOf(
            Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
        )
        drawRect(
            brush = Brush.horizontalGradient(hueColors)
        )

        val x = (hue / 360f) * size.width
        drawLine(
            color = Color.White,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 3.dp.toPx()
        )
    }
}
