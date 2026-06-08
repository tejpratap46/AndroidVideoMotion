package com.tejpratapsingh.lyricsmaker.presentation.compose.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
internal fun StartDragHandle(
    color: Color,
    autoScroll: AutoScrollState,
    moveMode: Boolean,
    isBeingDragged: Boolean,
    livePointerYInRoot: Float,
    onMoveModeToggle: () -> Unit,
    onDragStart: (y: Float) -> Unit,
    onDrag: (y: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    var selfTopInRoot by remember { mutableFloatStateOf(0f) }

    val visualOffset by remember {
        derivedStateOf {
            if (isBeingDragged) {
                // The handle should be centered under livePointerYInRoot.
                // We calculate how much to offset it from its "natural" position in the list.
                (livePointerYInRoot - selfTopInRoot).roundToInt()
            } else {
                0
            }
        }
    }

    val bgAlpha =
        when {
            isBeingDragged && moveMode -> 0.8f
            isBeingDragged -> 0.7f
            moveMode -> 0.6f
            else -> 0.5f
        }

    Row(
        modifier =
            Modifier
                .zIndex(if (isBeingDragged) 1f else 0f)
                .offset { IntOffset(0, visualOffset) }
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    // We only update selfTopInRoot when NOT dragging to avoid feedback loops,
                    // OR we calculate natural position by subtracting current visualOffset.
                    selfTopInRoot = coords.positionInRoot().y - visualOffset
                }.background(color.copy(alpha = bgAlpha))
                .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onMoveModeToggle,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
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
                    }.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.DragHandle, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (moveMode) "START  ·  drag moves range" else "START",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.DragHandle, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}
