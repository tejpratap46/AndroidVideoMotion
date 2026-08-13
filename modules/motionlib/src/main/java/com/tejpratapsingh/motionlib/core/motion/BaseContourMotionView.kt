package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import com.squareup.contour.ContourLayout
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import timber.log.Timber

open class BaseContourMotionView(
    context: Context,
    override val startFrame: Int,
    override val endFrame: Int,
    override var loop: Pair<Int, Int> = Pair(0, 0),
    effects: List<MotionEffect> = emptyList(),
) : ContourLayout(context),
    MotionView {
    override val effects: MutableList<MotionEffect> = mutableListOf()
    override var layoutInfo: MotionLayoutInfo = MotionLayoutInfo()

    init {
        effects.forEach { addEffect(it) }

        contourWidthOf {
            if (layoutInfo.width > 0) {
                layoutInfo.width.toXInt()
            } else {
                provideCurrentConfig().aspectRatio.width.toXInt()
            }
        }
        contourHeightOf {
            if (layoutInfo.height > 0) {
                layoutInfo.height.toYInt()
            } else {
                provideCurrentConfig().aspectRatio.height.toYInt()
            }
        }
    }

    private val minStartFrame: Int
        get() = minOf((effects.minOfOrNull { it.startFrame } ?: Int.MAX_VALUE), startFrame)

    private val maxEndFrame: Int
        get() = maxOf((effects.maxOfOrNull { it.endFrame } ?: Int.MIN_VALUE), endFrame)

    override fun addEffect(effect: MotionEffect) {
        effect.motionView = this
        effects.add(effect)
    }

    @CallSuper
    override fun forFrame(frame: Int): MotionView {
        if (frame < minStartFrame) {
            isVisible = false
            return this
        }
        if (frame > maxEndFrame) {
            isVisible = false
            return this
        }
        isVisible = true

        Timber.d("forFrame: isVisible: $isVisible")

        for (i in 0 until this.childCount) {
            val view = this.getChildAt(i)

            if (view is MotionView) {
                view.forFrame(frame)
            }
        }

        runEffects(frame)

        return this
    }

    fun runEffects(frame: Int) =
        effects.forEach { effect ->
            effect.forFrame(frame)
        }

    override fun getViewBitmap() = this.toBitmap()
}
