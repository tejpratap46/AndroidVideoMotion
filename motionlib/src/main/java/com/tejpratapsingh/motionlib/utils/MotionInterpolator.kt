package com.tejpratapsingh.motionlib.utils

import android.animation.ArgbEvaluator
import android.graphics.Color
import android.util.Log
import android.view.animation.Interpolator

//https://gist.github.com/mik9/9528041

class MotionInterpolator {

    companion object {
        private const val TAG = "MotionInterpolator"

        /**
         * Get Interpolated value in correspondence of current frame and interpolator
         *
         * eg. for LinearInterpolator and frameRange(1,10) and valueRange(100, 200)
         *
         * return value for currentFrame(7) will be 170
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

            if (currentFrame < frameRange.first) {
                return valueRange.first
            }
            if (currentFrame > frameRange.second) {
                return valueRange.second
            }

            Log.i(TAG, "interpolateForRange: currentFrame: $currentFrame")

            val framePercent =
                (currentFrame.toFloat() - frameRange.first.toFloat()) / (frameRange.second.toFloat() - frameRange.first.toFloat())

            Log.i(TAG, "interpolateForRange: framePercent: $framePercent")

            val interpolatedCurrentFrame: Float =
                interpolator.getInterpolation(framePercent)

            Log.i(TAG, "interpolateForRange: interpolatedCurrentFrame: $interpolatedCurrentFrame")

            val valueFromPercent =
                framePercent * (valueRange.second - valueRange.first) + valueRange.first

            Log.i(TAG, "interpolateForRange: valueFromPercent: $valueFromPercent")

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
            valueRange: Pair<Int, Int>
        ): Int {
            if (currentFrame < frameRange.first) {
                return valueRange.first
            }
            if (currentFrame > frameRange.second) {
                return valueRange.second
            }

            Log.i(TAG, "interpolateForRange: currentFrame: $currentFrame")

            val framePercent =
                (currentFrame.toFloat() - frameRange.first.toFloat()) / (frameRange.second.toFloat() - frameRange.first.toFloat())

            Log.i(TAG, "interpolateForRange: framePercent: $framePercent")

            val interpolatedCurrentFrame: Float =
                interpolator.getInterpolation(framePercent)

            val evaluatedColor: Int = ArgbEvaluator().evaluate(
                interpolatedCurrentFrame,
                valueRange.first,
                valueRange.second
            ) as Int

            return evaluatedColor
        }

        fun getComplementaryColor(colorToInvert: Int): Int {
            val hsv = FloatArray(3)
            Color.RGBToHSV(
                Color.red(colorToInvert), Color.green(colorToInvert),
                Color.blue(colorToInvert), hsv
            )
            hsv[0] = (hsv[0] + 180) % 360
            return Color.HSVToColor(hsv)
        }
    }
}