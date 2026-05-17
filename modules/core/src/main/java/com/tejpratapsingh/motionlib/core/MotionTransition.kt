package com.tejpratapsingh.motionlib.core

import android.view.ViewGroup

/**
 * Interface for defining a transition between two [MotionView]s.
 * A transition usually overlaps the end of the first view and the start of the second view.
 */
interface MotionTransition {
    /**
     * Applies the transition between [from] and [to].
     *
     * @param from The outgoing view.
     * @param to The incoming view.
     * @param duration The duration of the transition in frames.
     */
    fun apply(from: MotionView, to: MotionView, duration: Int)
}
