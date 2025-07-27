package com.tejpratapsingh.motionlib.ui.custom.background

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.motion.OrientedMotionView

enum class Orientation {
    HORIZONTAL,
    VERTICAL,
    CIRCULAR
}

class GradientView(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    private val orientation: Orientation,
    private val colors: IntArray
) : OrientedMotionView(
    context = context,
    startFrame = startFrame,
    endFrame = endFrame
) {
    private companion object {
        // const val TAG = "GradientView" // For logging if needed
    }

    private val paint: Paint = Paint().apply {
        isAntiAlias = true // Good practice for smoother gradients
    }
    private var currentFrame: Int = 0

    private val interpolator: Interpolators = Interpolators(Easings.LINEAR) // Assuming Interpolators handles this well
    private var frameRange = Pair(first = startFrame, second = endFrame)

    // Option 1: Fixed value range (if this is always the case)
    private var valueRange = Pair(first = 200f, second = 2000f)
    // Option 2: Dynamic value range (see onSizeChanged and forFrame)
    // private var valueRange = Pair(first = 0f, second = 1f) // Initialize, will be updated

    private var gradientShader: Shader? = null
    private var lastInterpolatedValue: Float = Float.NaN

    init {
        // If motionConfig is meant to set the *initial* or *target* size
        // and the view can be resized by its parent, this is fine.
        // If the view *is* the size of motionConfig, then using width/height
        // directly in onDraw/onSizeChanged is also good.
        contourWidthOf {
            motionConfig.width.toXInt()
        }
        contourHeightOf {
            motionConfig.height.toYInt()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // If valueRange depends on the view's actual dimensions:
        /*
        valueRange = when (orientation) {
            Orientation.CIRCULAR -> Pair(first = 0f, second = w.toFloat().coerceAtLeast(1f))
            Orientation.VERTICAL -> Pair(first = 0f, second = h.toFloat().coerceAtLeast(1f))
            Orientation.HORIZONTAL -> Pair(first = 0f, second = w.toFloat().coerceAtLeast(1f))
        }
        */
        gradientShader = null // Invalidate shader if size changes affect it
    }

    override fun forFrame(frame: Int): View {
        super.forFrame(frame)
        currentFrame = frame

        // If valueRange is fixed as Pair(200f, 2000f), no update needed here.
        // If it's dynamic and based on motionConfig that might change *per frame*
        // (unlikely for width/height, but possible for other properties), update here.
        // For now, assuming valueRange is either fixed or set in onSizeChanged.

        invalidate() // Request a redraw for the new frame
        return this
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // if (BuildConfig.DEBUG) {
        //     Log.d(TAG, "onDraw: called : $currentFrame, width: $width, height: $height, valueRange: $valueRange")
        // }

        val interpolatedValue: Float = MotionInterpolator.interpolateForRange(
            interpolator = interpolator,
            currentFrame = currentFrame,
            frameRange = frameRange,
            valueRange = valueRange
        )

        // Only recreate shader if necessary (value changed or not created yet)
        if (gradientShader == null || interpolatedValue != lastInterpolatedValue) {
            lastInterpolatedValue = interpolatedValue
            gradientShader = when (orientation) {
                Orientation.CIRCULAR -> RadialGradient(
                    (width / 2).toFloat(),
                    (height / 2).toFloat(),
                    interpolatedValue.coerceAtLeast(0.1f), // Ensure radius is positive
                    colors,
                    null, // Positions: null means evenly distributed
                    Shader.TileMode.CLAMP
                )
                Orientation.VERTICAL -> LinearGradient(
                    0f,
                    0f,
                    0f,
                    interpolatedValue.coerceAtLeast(0.1f), // Ensure height is positive
                    colors,
                    null,
                    Shader.TileMode.CLAMP
                )
                Orientation.HORIZONTAL -> LinearGradient(
                    0f,
                    0f,
                    interpolatedValue.coerceAtLeast(0.1f), // Ensure width is positive
                    0f,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            paint.shader = gradientShader
        }

        canvas.drawPaint(paint)
    }
}
