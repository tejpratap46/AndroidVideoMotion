package com.tejpratapsingh.motionlib.utils

import android.view.animation.Interpolator
import kotlin.math.cos
import kotlin.math.sin

//https://gist.github.com/mik9/9528041

class MotionInterpolator {

    companion object {
        private const val DOMAIN = 1.0f
        private const val DURATION = 1.0f
        private const val START = 0.0f

        fun interpolateForRange(
            interpolator: Interpolator,
            currentFrame: Float,
            frameRange: Pair<Float, Float>,
            valueRange: Pair<Float, Float>
        ): Float {
            val interpolatedCurrentFrame: Float = interpolator.getInterpolation(currentFrame)
            val currentPercent =
                ((interpolatedCurrentFrame - frameRange.first) / (frameRange.second - frameRange.first)) * 100
            val valueFromPercent = ((valueRange.second - valueRange.first) / 100) * currentPercent
            return valueFromPercent
        }

        /**
         * Interpolator implementation
         */
        internal object Linear {
            val easeNone = Interpolator { input -> input }
        }

        internal object Cubic {
            val easeIn = Interpolator { input ->
                var input = input
                DOMAIN * DURATION.let { input /= it; input } * input * input + START
            }
            val easeOut =
                Interpolator { input ->
                    var input = input
                    DOMAIN * ((input / DURATION - 1.also {
                        input = it.toFloat()
                    }) * input * input + 1) + START
                }
            val easeInOut =
                Interpolator { input ->
                    var input = input
                    if (DURATION / 2.let { input /= it; input } < 1.0f) DOMAIN / 2 * input * input * input + START else DOMAIN / 2 * (2.let { input -= it; input } * input * input + 2) + START
                }
        }

        internal object Quad {
            val easeIn = Interpolator { input ->
                var input = input
                DOMAIN * DURATION.let { input /= it; input } * input + START
            }
            val easeOut =
                Interpolator { input ->
                    var input = input
                    -DOMAIN * DURATION.let { input /= it; input } * (input - 2) + START
                }
            val easeInOut =
                Interpolator { input ->
                    var input = input
                    if (DURATION / 2.let { input /= it; input } < 1) DOMAIN / 2 * input * input + START else -DOMAIN / 2 * (--input * (input - 2) - 1) + START
                }
        }

        internal object Quart {
            val easeIn = Interpolator { input ->
                var input = input
                DOMAIN * DURATION.let { input /= it; input } * input * input * input + START
            }
            val easeOut =
                Interpolator { input ->
                    var input = input
                    -DOMAIN * ((input / DURATION - 1.also {
                        input = it.toFloat()
                    }) * input * input * input - 1) + START
                }
            val easeInOut =
                Interpolator { input ->
                    var input = input
                    if (DURATION / 2.let { input /= it; input } < 1) DOMAIN / 2 * input * input * input * input + START else -DOMAIN / 2 * (2.let { input -= it; input } * input * input * input - 2) + START
                }
        }

        internal object Quint {
            val easeIn = Interpolator { input ->
                var input = input
                DOMAIN * DURATION.let { input /= it; input } * input * input * input * input + START
            }
            val easeOut =
                Interpolator { input ->
                    var input = input
                    DOMAIN * ((input / DURATION - 1.also {
                        input = it.toFloat()
                    }) * input * input * input * input + 1) + START
                }
            val easeInOut =
                Interpolator { input ->
                    var input = input
                    if (DURATION / 2.let { input /= it; input } < 1) DOMAIN / 2 * input * input * input * input * input + START else DOMAIN / 2 * (2.let { input -= it; input } * input * input * input * input + 2) + START
                }
        }

        internal object Sine {
            val easeIn = Interpolator { input ->
                -DOMAIN * cos(input / DURATION * (Math.PI / 2)).toFloat() + DOMAIN + START
            }
            val easeOut = Interpolator { input ->
                DOMAIN * sin(input / DURATION * (Math.PI / 2))
                    .toFloat() + START
            }
            val easeInOut = Interpolator { input ->
                -DOMAIN / 2 * (cos(Math.PI * input / DURATION)
                    .toFloat() - 1.0f) + START
            }
        }
    }
}