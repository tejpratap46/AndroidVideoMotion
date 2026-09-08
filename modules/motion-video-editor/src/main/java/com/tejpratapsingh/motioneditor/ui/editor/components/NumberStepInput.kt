package com.tejpratapsingh.motioneditor.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NumberStepInput(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1,
    minValue: Int = Int.MIN_VALUE,
    maxValue: Int = Int.MAX_VALUE,
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledIconButton(
                onClick = {
                    val newValue = (value - step).coerceIn(minValue, maxValue)
                    onValueChange(newValue)
                },
                enabled = value > minValue,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrement",
                )
            }

            OutlinedTextField(
                value = textValue,
                onValueChange = { newValueStr ->
                    textValue = newValueStr
                    val parsed = newValueStr.toIntOrNull()
                    if (parsed != null) {
                        onValueChange(parsed.coerceIn(minValue, maxValue))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            )

            FilledIconButton(
                onClick = {
                    val newValue = (value + step).coerceIn(minValue, maxValue)
                    onValueChange(newValue)
                },
                enabled = value < maxValue,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increment",
                )
            }
        }
    }
}

@Composable
fun FloatStepInput(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    step: Float = 0.1f,
    minValue: Float = Float.MIN_VALUE,
    maxValue: Float = Float.MAX_VALUE,
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledIconButton(
                onClick = {
                    val newValue = (value - step).coerceIn(minValue, maxValue)
                    val rounded = (Math.round(newValue * 100) / 100.0).toFloat()
                    onValueChange(rounded)
                },
                enabled = value > minValue,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrement",
                )
            }

            OutlinedTextField(
                value = textValue,
                onValueChange = { newValueStr ->
                    textValue = newValueStr
                    val parsed = newValueStr.toFloatOrNull()
                    if (parsed != null) {
                        onValueChange(parsed.coerceIn(minValue, maxValue))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            )

            FilledIconButton(
                onClick = {
                    val newValue = (value + step).coerceIn(minValue, maxValue)
                    val rounded = (Math.round(newValue * 100) / 100.0).toFloat()
                    onValueChange(rounded)
                },
                enabled = value < maxValue,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increment",
                )
            }
        }
    }
}
