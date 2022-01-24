package com.tejpratapsingh.motionlib.ui.custom.background

import android.content.Context
import android.graphics.*
import android.util.Log
import com.tejpratapsingh.motionlib.ui.MotionView
import com.tejpratapsingh.motionlib.ui.Orientation
import com.tejpratapsingh.motionlib.utils.Easings
import com.tejpratapsingh.motionlib.utils.Interpolators
import com.tejpratapsingh.motionlib.utils.MotionInterpolator

class GradientView(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    private val orientation: Orientation,
    private val colors: IntArray
) :
    MotionView(
        context = context,
        startFrame = startFrame,
        endFrame = endFrame,
        orientation = orientation
    ) {
    private val TAG = "GradientView"

    private var paint: Paint = Paint()
    private var currentFrame: Int = 0

    private val interpolator: Interpolators = Interpolators(Easings.LINEAR)
    private var frameRange = Pair(first = startFrame, second = endFrame)
    private var valueRange = Pair(first = 0f, second = 0f)

    init {
        contourWidthOf {
            motionConfig.width.toXInt()
        }
        contourHeightOf {
            motionConfig.height.toYInt()
        }
    }

    override fun forFrame(frame: Int) {
        super.forFrame(frame)
        currentFrame = frame

        when (orientation) {
            Orientation.CIRCULAR -> {
                valueRange = Pair(first = 0f, second = motionConfig.width.toFloat())
            }
            Orientation.VERTICAL -> {
                valueRange = Pair(first = 0f, second = motionConfig.height.toFloat())
            }
            Orientation.HORIZONTAL -> {
                valueRange = Pair(first = 0f, second = motionConfig.width.toFloat())
            }
        }
        valueRange = Pair(first = 200f, second = 2000f)
    }

    override fun onDraw(canvas: Canvas?) {
        Log.d(TAG, "onDraw: called : $currentFrame")
        super.onDraw(canvas)

        when (orientation) {
            Orientation.CIRCULAR -> {

                val interpolatedRadius: Float = MotionInterpolator.interpolateForRange(
                    interpolator = interpolator,
                    currentFrame = currentFrame,
                    frameRange = frameRange,
                    valueRange = valueRange
                )

                paint.shader = RadialGradient(
                    (width / 2).toFloat(),
                    (height / 2).toFloat(),
                    interpolatedRadius,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
                )

            }
            Orientation.VERTICAL -> {

                val interpolatedHeight: Float = MotionInterpolator.interpolateForRange(
                    interpolator = interpolator,
                    currentFrame = currentFrame,
                    frameRange = frameRange,
                    valueRange = valueRange
                )

                paint.shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    interpolatedHeight,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            Orientation.HORIZONTAL -> {

                val interpolatedWidth: Float = MotionInterpolator.interpolateForRange(
                    interpolator = interpolator,
                    currentFrame = currentFrame,
                    frameRange = frameRange,
                    valueRange = valueRange
                )

                paint.shader = LinearGradient(
                    0f,
                    0f,
                    interpolatedWidth,
                    0f,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
                )
            }
        }

        canvas?.drawPaint(paint)
    }
}