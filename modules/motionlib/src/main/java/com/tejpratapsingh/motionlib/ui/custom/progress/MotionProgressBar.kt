package com.tejpratapsingh.motionlib.ui.custom.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import timber.log.Timber
import kotlin.math.min

/**
 * Style of the progress bar.
 */
enum class MotionProgressBarStyle {
    HORIZONTAL,
    CIRCULAR
}

/**
 * A [MotionView] that displays a progress bar corresponding to the video progress.
 */
class MotionProgressBar(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
    val style: MotionProgressBarStyle = MotionProgressBarStyle.HORIZONTAL,
    val color: Int = Color.WHITE,
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {

    private var currentProgressPercent: Int = 0

    private val progressView: View = object : View(context) {
        private val paint = Paint().apply {
            isAntiAlias = true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val max = 100
            val progress = currentProgressPercent
            val density = context.resources.displayMetrics.density

            when (style) {
                MotionProgressBarStyle.HORIZONTAL -> {
                    // Draw background (30% opacity)
                    paint.style = Paint.Style.FILL
                    paint.color = this@MotionProgressBar.color
                    paint.alpha = 77
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                    // Draw progress
                    paint.alpha = 255
                    val progressWidth = (progress.toFloat() / max) * width
                    canvas.drawRect(0f, 0f, progressWidth, height.toFloat(), paint)
                }
                MotionProgressBarStyle.CIRCULAR -> {
                    val strokeWidth = 4 * density
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = strokeWidth
                    paint.color = this@MotionProgressBar.color

                    val centerX = width / 2f
                    val centerY = height / 2f
                    val radius = (min(width, height) - strokeWidth) / 2f

                    // Draw background circle (30% opacity)
                    paint.alpha = 77
                    canvas.drawCircle(centerX, centerY, radius, paint)

                    // Draw progress arc
                    paint.alpha = 255
                    val sweepAngle = (progress.toFloat() / max) * 360f
                    canvas.drawArc(
                        centerX - radius,
                        centerY - radius,
                        centerX + radius,
                        centerY + radius,
                        -90f,
                        sweepAngle,
                        false,
                        paint
                    )
                }
            }
        }
    }

    init {
        // Ensure internal coordinate system matches layoutInfo if set (e.g. when used in stacks)
        contourWidthOf {
            if (layoutInfo.width > 0) {
                layoutInfo.width.toXInt()
            } else {
                motionConfig.aspectRatio.width.toXInt()
            }
        }
        contourHeightOf {
            if (layoutInfo.height > 0) {
                layoutInfo.height.toYInt()
            } else {
                motionConfig.aspectRatio.height.toYInt()
            }
        }

        when (style) {
            MotionProgressBarStyle.HORIZONTAL -> {
                progressView.layoutBy(
                    x = leftTo { parent.left() }.rightTo { parent.right() },
                    y = bottomTo { parent.bottom() }.heightOf { 12.toYInt() },
                )
            }
            MotionProgressBarStyle.CIRCULAR -> {
                progressView.layoutBy(
                    x = centerHorizontallyTo { parent.centerX() }.widthOf { 60.toXInt() },
                    y = centerVerticallyTo { parent.centerY() }.heightOf { 60.toYInt() },
                )
            }
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        val totalFrames = endFrame - startFrame
        currentProgressPercent = if (totalFrames > 0) {
            val progress = (frame - startFrame).coerceIn(0, totalFrames)
            (progress.toFloat() / totalFrames * 100).toInt()
        } else {
            0
        }
        Timber.d("MotionProgressBar: frame=$frame, progress=$currentProgressPercent")
        progressView.invalidate()
        return this
    }
}
