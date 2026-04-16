package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.util.AttributeSet
import timber.log.Timber
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import com.tejpratapsingh.motionlib.R
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.provideCurrentConfig

abstract class BaseFrameMotionView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr),
        MotionView {
        override var startFrame: Int = 0
        override var endFrame: Int = 0
        override var loop: Pair<Int, Int> = Pair(0, 0)

        init {
            context.theme
                .obtainStyledAttributes(
                    attrs,
                    R.styleable.BaseFrameMotionView,
                    defStyleAttr,
                    0,
                ).apply {
                    try {
                        startFrame = getInt(R.styleable.BaseFrameMotionView_startFrame, 0)
                        endFrame = getInt(R.styleable.BaseFrameMotionView_endFrame, 0)
                        val loopStart = getInt(R.styleable.BaseFrameMotionView_loopStart, 0)
                        val loopEnd = getInt(R.styleable.BaseFrameMotionView_loopEnd, 0)
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

            Timber.v("forFrame: $frame isVisible: $isVisible")

            for (i in 0 until this.childCount) {
                val view = this.getChildAt(i)

                if (view is MotionView) {
                    view.forFrame(frame)
                }
            }

            return this
        }

        override fun getViewBitmap() = this.toBitmap()

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val desiredWidth = provideCurrentConfig().aspectRatio.width
            val desiredHeight = provideCurrentConfig().aspectRatio.height
            setMeasuredDimension(desiredWidth, desiredHeight)
            getChildAt(0)?.measure(
                MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY),
            )
        }

        override fun onLayout(
            changed: Boolean,
            l: Int,
            t: Int,
            r: Int,
            b: Int,
        ) {
            getChildAt(0)?.layout(0, 0, r - l, b - t)
        }
    }
