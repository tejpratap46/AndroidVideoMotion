package com.tejpratapsingh.ividemo.extension

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
fun View.enableSwipeSeek(
    maxProgress: Int,
    initialProgress: () -> Int,
    onProgressChanged: (Int) -> Unit,
    sensitivity: Float = 2f,
) {
    var lastX = 0f
    var progressOnStart = 0

    setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                progressOnStart = initialProgress()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                val change = (dx / sensitivity).toInt()
                val newProgress = max(0, min(maxProgress, progressOnStart + change))
                onProgressChanged(newProgress)
                true
            }

            else -> {
                false
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
fun View.enableSwipeSeekReverse(
    maxProgress: Int,
    initialProgress: () -> Int,
    onProgressChanged: (Int) -> Unit,
    sensitivity: Float = 2f,
) {
    var lastX = 0f
    var progressOnStart = 0

    setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                progressOnStart = initialProgress()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                val change = (-dx / sensitivity).toInt() // 👈 reversed here
                val newProgress = max(0, min(maxProgress, progressOnStart + change))
                onProgressChanged(newProgress)
                true
            }

            else -> {
                false
            }
        }
    }
}
