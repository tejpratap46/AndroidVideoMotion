package com.tejpratapsingh.motionlib.core.animation

import android.animation.ArgbEvaluator
import android.graphics.Color
import android.util.Log
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import com.tejpratapsingh.motionlib.BuildConfig

object MotionInterpolator {
    private const val TAG = "MotionInterpolator"
    private val argbEvaluator = ArgbEvaluator() // Reuse ArgbEvaluator instance

    /**
     * Get Interpolated value in correspondence of current frame and interpolator
     *
     * eg. for LinearInterpolator and frameRange(1,10) and valueRange(100, 200)
     *
     * return value for currentFrame(7) will be 170 (if using LinearInterpolator directly on framePercent)
     *
     * Note: The original implementation was not correctly applying the interpolator to the final value.
     * This version applies the interpolator to the framePercent and then calculates the value.
     *
     * @param interpolator interpolator from Interpolators(Easing)
     * @param currentFrame current frame value
     * @param frameRange Range of frame this interpolation start and end
     * @param valueRange Range of corresponding value in respect to frame value
     */
    fun interpolateForRange(
        interpolator: Interpolator,
        currentFrame: Int,
        frameRange: Pair<Int, Int>,
        valueRange: Pair<Float, Float>
    ): Float {
        val (startFrame, endFrame) = frameRange
        val (startValue, endValue) = valueRange

        if (currentFrame <= startFrame) { // Use <= for start to include the boundary
            return startValue
        }
        if (currentFrame >= endFrame) { // Use >= for end to include the boundary
            return endValue
        }

        // Calculate the raw percentage of completion within the frame range
        val framePercent =
            (currentFrame.toFloat() - startFrame.toFloat()) / (endFrame.toFloat() - startFrame.toFloat())

        // Apply the interpolator to the framePercent
        val interpolatedFramePercent: Float = interpolator.getInterpolation(framePercent)

        // Calculate the final value based on the interpolated percentage
        val valueFromPercent =
            startValue + interpolatedFramePercent * (endValue - startValue)

        // Logging can be expensive, consider using it only in debug builds or when necessary
        if (BuildConfig.DEBUG) { // Example: Only log in debug builds
            Log.d(
                TAG,
                "interpolateForRange: currentFrame: $currentFrame, framePercent: $framePercent, interpolatedFramePercent: $interpolatedFramePercent, valueFromPercent: $valueFromPercent"
            )
        }

        return valueFromPercent
    }

    /**
     * Similar to interpolateForRange, this function will return color interpolated color
     *
     * @param interpolator interpolator from Interpolators(Easing)
     * @param currentFrame current frame value
     * @param frameRange Range of frame this interpolation start and end
     * @param valueRange Range is accepted as @ColorInt , you can pass Color.parseColor("#color")
     */
    fun interpolateColorForRange(
        interpolator: Interpolator,
        currentFrame: Int,
        frameRange: Pair<Int, Int>,
        @ColorInt valueRange: Pair<Int, Int> // Added @ColorInt for clarity
    ): Int {
        val (startFrame, endFrame) = frameRange
        val (startColor, endColor) = valueRange

        if (currentFrame <= startFrame) {
            return startColor
        }
        if (currentFrame >= endFrame) {
            return endColor
        }

        val framePercent =
            (currentFrame.toFloat() - startFrame.toFloat()) / (endFrame.toFloat() - startFrame.toFloat())

        val interpolatedFramePercent: Float = interpolator.getInterpolation(framePercent)

        // Use the reused ArgbEvaluator instance
        return argbEvaluator.evaluate(
            interpolatedFramePercent,
            startColor,
            endColor
        ) as Int
    }

    /**
     * Calculates the complementary color for a given color.
     * @param colorToInvert The @ColorInt to invert.
     * @return The complementary @ColorInt.
     */
    fun getComplementaryColor(@ColorInt colorToInvert: Int): Int {
        val hsv = FloatArray(3)
        Color.RGBToHSV(
            Color.red(colorToInvert), Color.green(colorToInvert),
            Color.blue(colorToInvert), hsv
        )
        hsv[0] = (hsv[0] + 180) % 360 // Add 180 degrees to hue for complementary color
        return Color.HSVToColor(hsv)
    }
}
