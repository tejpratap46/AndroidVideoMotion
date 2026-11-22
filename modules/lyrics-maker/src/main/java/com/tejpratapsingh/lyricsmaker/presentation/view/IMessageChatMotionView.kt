package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.PropertySpec
import com.tejpratapsingh.motionlib.core.getFontSize
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.utils.getWebFont

/**
 * A [MotionView] that displays lyrics flowing as an Apple iMessage chat conversation.
 * Multiple chat bubbles are visible on screen simultaneously, anchored to the bottom.
 * The active bubble's width grows with each new visible word, and the conversation
 * pushes upwards as new lines appear.
 */
class IMessageChatMotionView(
    context: Context,
    val lyrics: List<SyncedLyricFrame>,
    startFrame: Int,
    endFrame: Int,
    val bubbleColor: String = "#007AFF",
    val textColor: String = "#FFFFFF",
    val incomingBubbleColor: String = "#26252A",
    val incomingTextColor: String = "#FFFFFF",
    val writingSpeed: Float = 1.0f,
    val unwrittenTextAlpha: Float = 0.0f,
    val isAlternating: Boolean = false,
    val fontAsset: MotionAsset? = null,
    val textSizeVariant: MotionTextVariant = MotionTextVariant.H5,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {

    private val textPaint: TextPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            val fontSize = motionConfig.getFontSize(textSizeVariant)
            textSize = fontSize
            color = Color.WHITE
            fontAsset?.let {
                typeface = context.getWebFont(it.getUri().toString())
            }
        }
    }

    private val outgoingBubblePaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            try {
                color = bubbleColor.toColorInt()
            } catch (e: Exception) {
                color = Color.parseColor("#007AFF")
            }
        }
    }

    private val incomingBubblePaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            try {
                color = incomingBubbleColor.toColorInt()
            } catch (e: Exception) {
                color = Color.parseColor("#26252A")
            }
        }
    }

    // Cached measurements for completed full lines
    private data class MeasuredBubble(
        val bubbleWidth: Float,
        val bubbleHeight: Float,
        val textWidth: Float,
        val textHeight: Float,
        val words: List<String>,
    )

    private var measuredBubbles: List<MeasuredBubble> = emptyList()
    private var isMeasured = false

    private var horizontalPadding: Float = 0f
    private var verticalPadding: Float = 0f
    private var tailWidth: Float = 0f
    private var tailHeight: Float = 0f
    private var cornerRadius: Float = 0f
    private var bubbleSpacing: Float = 0f
    private var maxAllowedTextWidth: Float = 0f

    private var currentFrameIndex: Int = startFrame

    init {
        setWillNotDraw(false)

        contourWidthOf {
            if (layoutInfo.width > 0) layoutInfo.width.toXInt()
            else motionConfig.aspectRatio.width.toXInt()
        }
        contourHeightOf {
            if (layoutInfo.height > 0) layoutInfo.height.toYInt()
            else motionConfig.aspectRatio.height.toYInt()
        }
    }

    private fun ensureMeasurements() {
        if (isMeasured || lyrics.isEmpty()) return

        val fontSize = textPaint.textSize
        horizontalPadding = fontSize * 0.65f
        verticalPadding = fontSize * 0.45f
        tailWidth = fontSize * 0.35f
        tailHeight = fontSize * 0.45f
        cornerRadius = (fontSize * 0.6f).coerceIn(16f, 36f)
        bubbleSpacing = fontSize * 0.45f

        val screenWidth = motionConfig.aspectRatio.width.toFloat()
        maxAllowedTextWidth = (screenWidth * 0.72f) - (horizontalPadding * 2 + tailWidth)

        measuredBubbles = lyrics.map { lyric ->
            val words = if (lyric.text.isEmpty()) emptyList() else lyric.text.split(" ")
            val measuredSingleWidth = textPaint.measureText(lyric.text)
            val layoutWidth = if (measuredSingleWidth > maxAllowedTextWidth) {
                maxAllowedTextWidth.toInt().coerceAtLeast(1)
            } else {
                (measuredSingleWidth + 4).toInt()
            }

            val layout = createStaticLayout(lyric.text, layoutWidth)
            val actualLineWidth = if (layout.lineCount > 0) {
                (0 until layout.lineCount).maxOf { layout.getLineWidth(it) }
            } else {
                measuredSingleWidth
            }

            val bWidth = actualLineWidth + horizontalPadding * 2 + tailWidth
            val bHeight = layout.height + verticalPadding * 2

            MeasuredBubble(
                bubbleWidth = bWidth,
                bubbleHeight = bHeight,
                textWidth = actualLineWidth,
                textHeight = layout.height.toFloat(),
                words = words,
            )
        }
        isMeasured = true
    }

    private fun createStaticLayout(content: CharSequence, width: Int): StaticLayout {
        val safeWidth = width.coerceAtLeast(1)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(content, 0, content.length, textPaint, safeWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(content, textPaint, safeWidth, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false)
        }
    }

    private fun buildBubblePath(width: Float, height: Float, isOutgoing: Boolean): Path {
        val path = Path()
        val r = cornerRadius
        val tw = tailWidth
        val th = tailHeight

        if (isOutgoing) {
            val rightBody = width - tw
            path.moveTo(r, 0f)
            path.lineTo(rightBody - r, 0f)
            path.arcTo(RectF(rightBody - 2 * r, 0f, rightBody, 2 * r), -90f, 90f)
            path.lineTo(rightBody, height - th)
            path.quadTo(rightBody, height - th * 0.25f, width, height)
            path.quadTo(rightBody - tw * 0.3f, height, rightBody - r, height)
            path.lineTo(r, height)
            path.arcTo(RectF(0f, height - 2 * r, 2 * r, height), 90f, 90f)
            path.lineTo(0f, r)
            path.arcTo(RectF(0f, 0f, 2 * r, 2 * r), 180f, 90f)
            path.close()
        } else {
            val leftBody = tw
            path.moveTo(leftBody + r, 0f)
            path.lineTo(width - r, 0f)
            path.arcTo(RectF(width - 2 * r, 0f, width, 2 * r), -90f, 90f)
            path.lineTo(width, height - r)
            path.arcTo(RectF(width - 2 * r, height - 2 * r, width, height), 0f, 90f)
            path.lineTo(leftBody + r, height)
            path.quadTo(leftBody + tw * 0.3f, height, 0f, height)
            path.quadTo(leftBody, height - th * 0.25f, leftBody, height - th)
            path.lineTo(leftBody, r)
            path.arcTo(RectF(leftBody, 0f, leftBody + 2 * r, 2 * r), 180f, 90f)
            path.close()
        }
        return path
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        currentFrameIndex = frame
        invalidate()
        return this
    }

    private data class ActiveBubbleRenderData(
        val index: Int,
        val text: String,
        val isOutgoing: Boolean,
        val bubbleWidth: Float,
        val bubbleHeight: Float,
        val staticLayout: StaticLayout,
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        ensureMeasurements()
        if (lyrics.isEmpty() || measuredBubbles.isEmpty()) return

        val screenWidth = width.toFloat().takeIf { it > 0 } ?: motionConfig.aspectRatio.width.toFloat()
        val screenHeight = height.toFloat().takeIf { it > 0 } ?: motionConfig.aspectRatio.height.toFloat()

        // 1. Determine all lyrics arrived up to currentFrame
        val visibleIndices = lyrics.indices.filter { lyrics[it].frame <= currentFrameIndex }
        if (visibleIndices.isEmpty()) return

        val activeIndex = visibleIndices.last()

        // 2. Build list of bubbles to render (with active line width growing per visible word)
        val bubblesToDraw = mutableListOf<ActiveBubbleRenderData>()

        visibleIndices.forEach { index ->
            val lyric = lyrics[index]
            val isOutgoing = if (isAlternating) (index % 2 == 0) else true

            if (index == activeIndex) {
                val words = measuredBubbles[index].words
                val nextFrame = if (index + 1 < lyrics.size) lyrics[index + 1].frame else (lyric.frame + 60)
                val duration = (nextFrame - lyric.frame).coerceAtLeast(1)
                val elapsed = (currentFrameIndex - lyric.frame).coerceAtLeast(0)
                val progress = if (writingSpeed > 0f) {
                    (elapsed.toFloat() * writingSpeed / duration).coerceIn(0f, 1f)
                } else {
                    (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                }

                val visibleWordCount = (progress * words.size).toInt().coerceIn(0, words.size)
                if (visibleWordCount > 0) {
                    val currentText = words.take(visibleWordCount).joinToString(" ")
                    val singleLineWidth = textPaint.measureText(currentText)
                    val layoutWidth = if (singleLineWidth > maxAllowedTextWidth) {
                        maxAllowedTextWidth.toInt().coerceAtLeast(1)
                    } else {
                        (singleLineWidth + 4).toInt()
                    }

                    val layout = createStaticLayout(currentText, layoutWidth)
                    val actualLineWidth = if (layout.lineCount > 0) {
                        (0 until layout.lineCount).maxOf { layout.getLineWidth(it) }
                    } else {
                        singleLineWidth
                    }

                    val bWidth = actualLineWidth + horizontalPadding * 2 + tailWidth
                    val bHeight = layout.height + verticalPadding * 2

                    bubblesToDraw.add(
                        ActiveBubbleRenderData(
                            index = index,
                            text = currentText,
                            isOutgoing = isOutgoing,
                            bubbleWidth = bWidth,
                            bubbleHeight = bHeight,
                            staticLayout = layout,
                        )
                    )
                }
            } else {
                val bubble = measuredBubbles[index]
                val layout = createStaticLayout(lyric.text, (bubble.textWidth + 4).toInt().coerceAtLeast(1))
                bubblesToDraw.add(
                    ActiveBubbleRenderData(
                        index = index,
                        text = lyric.text,
                        isOutgoing = isOutgoing,
                        bubbleWidth = bubble.bubbleWidth,
                        bubbleHeight = bubble.bubbleHeight,
                        staticLayout = layout,
                    )
                )
            }
        }

        if (bubblesToDraw.isEmpty()) return

        // 3. Compute vertical placement anchored to the BOTTOM
        val sideMargin = 48f
        val topSafeArea = screenHeight * 0.15f
        val bottomSafeArea = screenHeight * 0.85f
        val maxAvailableHeight = bottomSafeArea - topSafeArea

        val totalChatHeight = bubblesToDraw.map { it.bubbleHeight }.sum() +
            (bubblesToDraw.size - 1) * bubbleSpacing

        // Start at bottom: bottom of newest bubble anchored at bottomSafeArea
        val scrollOffset = if (totalChatHeight > maxAvailableHeight) {
            totalChatHeight - maxAvailableHeight
        } else {
            0f
        }

        var currentY = bottomSafeArea - totalChatHeight + scrollOffset

        // 4. Draw each bubble
        bubblesToDraw.forEach { item ->
            val bubbleWidth = item.bubbleWidth
            val bubbleHeight = item.bubbleHeight
            val isOutgoing = item.isOutgoing

            val bubbleX = if (isOutgoing) {
                screenWidth - sideMargin - bubbleWidth
            } else {
                sideMargin
            }

            val bubbleTop = currentY
            val bubbleBottom = currentY + bubbleHeight

            if (bubbleBottom > 0 && bubbleTop < screenHeight) {
                // Fade out as message approaches/exceeds topSafeArea
                var alpha = 1.0f
                if (bubbleTop < topSafeArea) {
                    val fadeDistance = topSafeArea * 0.6f
                    alpha = ((bubbleBottom - (topSafeArea - fadeDistance)) / (fadeDistance + bubbleHeight)).coerceIn(0f, 1f)
                }

                val savedOutgoingAlpha = outgoingBubblePaint.alpha
                val savedIncomingAlpha = incomingBubblePaint.alpha
                outgoingBubblePaint.alpha = (255 * alpha).toInt()
                incomingBubblePaint.alpha = (255 * alpha).toInt()

                val textHex = if (isOutgoing) textColor else incomingTextColor
                val baseTextColor = try { textHex.toColorInt() } catch (e: Exception) { Color.WHITE }
                textPaint.color = Color.argb(
                    (255 * alpha).toInt(),
                    Color.red(baseTextColor),
                    Color.green(baseTextColor),
                    Color.blue(baseTextColor),
                )

                canvas.withTranslation(bubbleX, bubbleTop) {
                    // Draw bubble path
                    val bubblePath = buildBubblePath(bubbleWidth, bubbleHeight, isOutgoing)
                    drawPath(bubblePath, if (isOutgoing) outgoingBubblePaint else incomingBubblePaint)

                    // Draw text inside bubble
                    val textX = if (isOutgoing) horizontalPadding else tailWidth + horizontalPadding
                    val textY = verticalPadding

                    withTranslation(textX, textY) {
                        item.staticLayout.draw(this)
                    }
                }

                outgoingBubblePaint.alpha = savedOutgoingAlpha
                incomingBubblePaint.alpha = savedIncomingAlpha
            }

            currentY += bubbleHeight + bubbleSpacing
        }
    }

    override fun getEditableProperties(): List<PropertySpec> =
        super.getEditableProperties() + listOf(
            PropertySpec.color("bubbleColor", label = "Bubble Color"),
            PropertySpec.color("textColor", label = "Text Color"),
            PropertySpec.color("incomingBubbleColor", label = "Incoming Bubble Color"),
            PropertySpec.color("incomingTextColor", label = "Incoming Text Color"),
            PropertySpec.dropdown("textSizeVariant", label = "Text Size", options = MotionTextVariant.entries.map { it.name }),
            PropertySpec.slider("writingSpeed", label = "Writing Speed", min = 0.0f, max = 5.0f, step = 0.1f),
            PropertySpec.slider("unwrittenTextAlpha", label = "Unwritten Text Opacity", min = 0.0f, max = 1.0f, step = 0.05f),
            PropertySpec.boolean("isAlternating", label = "Is Alternating")
        )
}
