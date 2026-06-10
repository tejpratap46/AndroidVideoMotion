package com.tejpratapsingh.lyricsmaker.presentation.compose.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Composable
internal fun DragHandle(
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
