package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import com.tejpratapsingh.motionlib.R
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import timber.log.Timber

abstract class BaseLinearMotionView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayoutCompat(context, attrs, defStyleAttr),
        MotionView {
        override var startFrame: Int = 0
        override var endFrame: Int = 0
        override var loop: Pair<Int, Int> = Pair(0, 0)
        override val effects: MutableList<MotionEffect> = mutableListOf()
        override var layoutInfo: MotionLayoutInfo = MotionLayoutInfo()

        override fun addEffect(effect: MotionEffect) {
            effect.motionView = this
            effects.add(effect)
        }

        companion object;

        init {
            context.theme
                .obtainStyledAttributes(
                    attrs,
                    R.styleable.MotionView,
                    defStyleAttr,
                    0,
                ).apply {
                    try {
                        startFrame = getInt(R.styleable.MotionView_startFrame, 0)
                        endFrame = getInt(R.styleable.MotionView_endFrame, 0)
                        val loopStart = getInt(R.styleable.MotionView_loopStart, 0)
                        val loopEnd = getInt(R.styleable.MotionView_loopEnd, 0)
                        loop = Pair(loopStart, loopEnd)
                    } finally {
                        recycle()
                    }
                }
        }

        @CallSuper
        override fun forFrame(frame: Int): MotionView {
            if (frame < startFrame) {
                visibility = INVISIBLE
                return this
            }
            if (frame > endFrame) {
                visibility = INVISIBLE
                return this
            }
            visibility = VISIBLE

            Timber.d("forFrame: isVisible: $isVisible")

            for (i in 0 until this.childCount) {
                val view = this.getChildAt(i)

                if (view is MotionView) {
                    view.forFrame(frame)
                }
            }

            effects.forEach { it.forFrame(frame) }

            return this
        }

        override fun getViewBitmap() = this.toBitmap()

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)

            val desiredWidth = if (layoutInfo.width == MotionLayoutInfo.MATCH_PARENT && widthMode != MeasureSpec.UNSPECIFIED) {
                widthSize
            } else {
                provideCurrentConfig().aspectRatio.width
            }

            val desiredHeight = if (layoutInfo.height == MotionLayoutInfo.MATCH_PARENT && heightMode != MeasureSpec.UNSPECIFIED) {
                heightSize
            } else {
                provideCurrentConfig().aspectRatio.height
            }

            val widthSpec = MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.EXACTLY)
            val heightSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY)

            super.onMeasure(widthSpec, heightSpec)
            setMeasuredDimension(desiredWidth, desiredHeight)
        }

        override fun onLayout(
            changed: Boolean,
            l: Int,
            t: Int,
            r: Int,
            b: Int,
        ) {
            super.onLayout(changed, l, t, r, b)
        }
    }
