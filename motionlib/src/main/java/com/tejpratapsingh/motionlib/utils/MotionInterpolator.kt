package com.tejpratapsingh.motionlib.utils

import android.view.animation.Interpolator
import kotlin.math.cos
import kotlin.math.sin

//https://gist.github.com/mik9/9528041

class MotionInterpolator {

    companion object {
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
            fun getEaseIn(DOMAIN: Float, START: Float, DURATION: Float) = Interpolator { input ->
                DOMAIN * (input.apply {
                    this / DURATION
                }) * input * input + START
            }

            fun getEaseOut(DOMAIN: Float, START: Float, DURATION: Float) =
                Interpolator { input ->
                    var input = input
                    DOMAIN * ((input / DURATION - 1.also {
                        input = it.toFloat()
                    }) * input * input + 1) + START
                }

            fun getEaseInOut(DOMAIN: Float, START: Float, DURATION: Float) =
                Interpolator { input ->
                    var input = input
                    if (DURATION / 2.let { input /= it; input } < 1.0f) DOMAIN / 2 * input * input * input + START else DOMAIN / 2 * (2.let { input -= it; input } * input * input + 2) + START
                }
        }

        internal object Quad {
            fun getEaseIn(DOMAIN: Float, START: Float, DURATION: Float) = Interpolator { input ->
                var input = input
                DOMAIN * DURATION.let { input /= it; input } * input + START
            }

            fun getEaseOut(DOMAIN: Float, START: Float, DURATION: Float) =
                Interpolator { input ->
                    var input = input
                    -DOMAIN * DURATION.let { input /= it; input } * (input - 2) + START
                }

            fun getEaseInOut(DOMAIN: Float, START: Float, DURATION: Float) =
                Interpolator { input ->
                    var input = input
                    if (DURATION / 2.let { input /= it; input } < 1) DOMAIN / 2 * input * input + START else -DOMAIN / 2 * (--input * (input - 2) - 1) + START
                }
        }

        internal object Quart {
            fun getEaseIn(DOMAIN: Float, START: Float, DURATION: Float) = Interpolator { input ->
                var input = input
                DOMAIN * DURATION.let { input /= it; input } * input * input * input + START
            }

            fun getEaseOut(DOMAIN: Float, START: Float, DURATION: Float) =
                Interpolator { input ->
                    var input = input
                    -DOMAIN * ((input / DURATION - 1.also {
                        input = it.toFloat()
                    }) * input * input * input - 1) + START
                }

            fun getEaseInOut(DOMAIN: Float, START: Float, DURATION: Float) =
                Interpolator { input ->
                    var input = input
                    if (DURATION / 2.let { input /= it; input } < 1) DOMAIN / 2 * input * input * input * input + START else -DOMAIN / 2 * (2.let { input -= it; input } * input * input * input - 2) + START
                }
        }

        internal object Quint {
            fun getEaseIn(DOMAIN: Float, START: Float, DURATION: Float) = Interpolator { input ->
                var input = input
                DOMAIN * DURATION.let { input /= it; input } * input * input * input * input + START
            }

            fun getEaseOut(DOMAIN: Float, START: Float, DURATION: Float) =
                Interpolator { input ->
                    var input = input
                    DOMAIN * ((input / DURATION - 1.also {
                        input = it.toFloat()
                    }) * input * input * input * input + 1) + START
                }

            fun getEaseInOut(DOMAIN: Float, START: Float, DURATION: Float) =
                Interpolator { input ->
                    var input = input
                    if (DURATION / 2.let { input /= it; input } < 1) DOMAIN / 2 * input * input * input * input * input + START else DOMAIN / 2 * (2.let { input -= it; input } * input * input * input * input + 2) + START
                }
        }

        internal object Sine {
            fun getEaseIn(DOMAIN: Float, START: Float, DURATION: Float) = Interpolator { input ->
                -DOMAIN * cos(input / DURATION * (Math.PI / 2)).toFloat() + DOMAIN + START
            }

            fun getEaseOut(DOMAIN: Float, START: Float, DURATION: Float) = Interpolator { input ->
                DOMAIN * sin(input / DURATION * (Math.PI / 2))
                    .toFloat() + START
            }

            fun getEaseInOut(DOMAIN: Float, START: Float, DURATION: Float) = Interpolator { input ->
                -DOMAIN / 2 * (cos(Math.PI * input / DURATION)
                    .toFloat() - 1.0f) + START
            }
        }
    }
}