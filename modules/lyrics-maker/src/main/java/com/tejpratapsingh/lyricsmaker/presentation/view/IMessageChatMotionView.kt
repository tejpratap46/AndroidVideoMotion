package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
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

    private var parsedTextColor: Int = Color.WHITE
    private var parsedIncomingTextColor: Int = Color.WHITE
    private var parsedBubbleColor: Int = "#007AFF".toColorInt()
    private var parsedIncomingBubbleColor: Int = "#26252A".toColorInt()

    private val outgoingBubblePaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = parsedBubbleColor
        }
    }

    private val incomingBubblePaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = parsedIncomingBubbleColor
        }
    }

    private val reusableBubblePath = Path()
    private val reusableArcRect = RectF()

    private class MutableColorSpan(
        var color: Int = Color.WHITE,
    ) : CharacterStyle(),
        UpdateAppearance {
        override fun updateDrawState(tp: TextPaint) {
            tp.color = color
        }
    }

    private data class PrefixLayoutData(
        val layout: StaticLayout,
        val bubbleWidth: Float,
        val bubbleHeight: Float,
        val prevSpan: MutableColorSpan?,
        val wordSpan: MutableColorSpan,
    )

    // Cached measurements for completed full lines and word-by-word prefixes
    private data class MeasuredBubble(
        val bubbleWidth: Float,
        val bubbleHeight: Float,
        val words: List<String>,
        val wordDimensions: List<Pair<Float, Float>>,
        val prefixLayouts: List<PrefixLayoutData>,
        val fullLayout: StaticLayout,
        val fullSpan: MutableColorSpan,
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

        parsedTextColor =
            try {
                textColor.toColorInt()
            } catch (e: Exception) {
                Color.WHITE
            }
        parsedIncomingTextColor =
            try {
                incomingTextColor.toColorInt()
            } catch (e: Exception) {
                Color.WHITE
            }
        parsedBubbleColor =
            try {
                bubbleColor.toColorInt()
            } catch (e: Exception) {
                "#007AFF".toColorInt()
            }
        parsedIncomingBubbleColor =
            try {
                incomingBubbleColor.toColorInt()
            } catch (e: Exception) {
                "#26252A".toColorInt()
            }

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

        measuredBubbles =
            lyrics.map { lyric ->
                val words = if (lyric.text.isEmpty()) emptyList() else lyric.text.split(" ")
                val wordDimensions = mutableListOf<Pair<Float, Float>>()
                val prefixLayouts = mutableListOf<PrefixLayoutData>()

                for (wordIdx in words.indices) {
                    val prefixText = words.take(wordIdx + 1).joinToString(" ")
                    val measuredSingleWidth = textPaint.measureText(prefixText)
                    val layoutWidth =
                        if (measuredSingleWidth > maxAllowedTextWidth) {
                            maxAllowedTextWidth.toInt().coerceAtLeast(1)
                        } else {
                            (measuredSingleWidth + 4).toInt()
                        }

                    val activeWordStart =
                        if (wordIdx > 0) {
                            words.take(wordIdx).joinToString(" ").length + 1
                        } else {
                            0
                        }
                    val activeWordEnd = prefixText.length

                    val spannable = SpannableString(prefixText)
                    var prevSpan: MutableColorSpan? = null
                    if (activeWordStart > 0) {
                        prevSpan = MutableColorSpan(Color.WHITE)
                        spannable.setSpan(prevSpan, 0, activeWordStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    val wordSpan = MutableColorSpan(Color.WHITE)
                    spannable.setSpan(wordSpan, activeWordStart, activeWordEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    val layout = createStaticLayout(spannable, layoutWidth)
                    val actualLineWidth =
                        if (layout.lineCount > 0) {
                            (0 until layout.lineCount).maxOf { layout.getLineWidth(it) }
                        } else {
                            measuredSingleWidth
                        }

                    val bWidth = actualLineWidth + horizontalPadding * 2 + tailWidth
                    val bHeight = layout.height + verticalPadding * 2
                    val dims = Pair(bWidth, bHeight)
                    wordDimensions.add(dims)
                    prefixLayouts.add(PrefixLayoutData(layout, bWidth, bHeight, prevSpan, wordSpan))
                }

                val fullWidth = wordDimensions.lastOrNull()?.first ?: (cornerRadius * 2 + tailWidth)
                val fullHeight = wordDimensions.lastOrNull()?.second ?: (textPaint.textSize + verticalPadding * 2)

                val fullSpannable = SpannableString(lyric.text)
                val fullSpan = MutableColorSpan(Color.WHITE)
                if (lyric.text.isNotEmpty()) {
                    fullSpannable.setSpan(fullSpan, 0, lyric.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                val fullMeasuredWidth = textPaint.measureText(lyric.text)
                val fullLayoutWidth =
                    if (fullMeasuredWidth > maxAllowedTextWidth) {
                        maxAllowedTextWidth.toInt().coerceAtLeast(1)
                    } else {
                        (fullMeasuredWidth + 4).toInt()
                    }
                val fullLayout = createStaticLayout(fullSpannable, fullLayoutWidth)

                MeasuredBubble(
                    bubbleWidth = fullWidth,
                    bubbleHeight = fullHeight,
                    words = words,
                    wordDimensions = wordDimensions,
                    prefixLayouts = prefixLayouts,
                    fullLayout = fullLayout,
                    fullSpan = fullSpan,
                )
            }
        isMeasured = true
    }

    private fun getBubbleDimension(
        index: Int,
        wordCount: Int,
    ): Pair<Float, Float> {
        val defaultMin =
            Pair(
                cornerRadius * 2 + tailWidth,
                textPaint.textSize + verticalPadding * 2,
            )
        val bubble = measuredBubbles.getOrNull(index) ?: return defaultMin
        if (wordCount <= 0 || bubble.wordDimensions.isEmpty()) {
            return defaultMin
        }
        val idx = (wordCount - 1).coerceIn(0, bubble.wordDimensions.size - 1)
        return bubble.wordDimensions[idx]
    }

    private fun createStaticLayout(
        content: CharSequence,
        width: Int,
    ): StaticLayout {
        val safeWidth = width.coerceAtLeast(1)
        return StaticLayout.Builder
            .obtain(content, 0, content.length, textPaint, safeWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)
            .build()
    }

    private fun buildBubblePath(
        path: Path,
        width: Float,
        height: Float,
        isOutgoing: Boolean,
    ) {
        path.reset()
        val r = cornerRadius.coerceAtMost(height / 2f).coerceAtMost(width / 2f)
        val tw = tailWidth.coerceAtMost(width * 0.3f)
        val th = tailHeight.coerceAtMost(height * 0.4f)

        if (isOutgoing) {
            val rightBody = width - tw
            path.moveTo(r, 0f)
            path.lineTo(rightBody - r, 0f)
            reusableArcRect.set(rightBody - 2 * r, 0f, rightBody, 2 * r)
            path.arcTo(reusableArcRect, -90f, 90f)
            path.lineTo(rightBody, height - th)
            path.quadTo(rightBody, height - th * 0.25f, width, height)
            path.quadTo(rightBody - tw * 0.3f, height, rightBody - r, height)
            path.lineTo(r, height)
            reusableArcRect.set(0f, height - 2 * r, 2 * r, height)
            path.arcTo(reusableArcRect, 90f, 90f)
            path.lineTo(0f, r)
            reusableArcRect.set(0f, 0f, 2 * r, 2 * r)
            path.arcTo(reusableArcRect, 180f, 90f)
            path.close()
        } else {
            path.moveTo(tw + r, 0f)
            path.lineTo(width - r, 0f)
            reusableArcRect.set(width - 2 * r, 0f, width, 2 * r)
            path.arcTo(reusableArcRect, -90f, 90f)
            path.lineTo(width, height - r)
            reusableArcRect.set(width - 2 * r, height - 2 * r, width, height)
            path.arcTo(reusableArcRect, 0f, 90f)
            path.lineTo(tw + r, height)
            path.quadTo(tw + tw * 0.3f, height, 0f, height)
            path.quadTo(tw, height - th * 0.25f, tw, height - th)
            path.lineTo(tw, r)
            reusableArcRect.set(tw, 0f, tw + 2 * r, 2 * r)
            path.arcTo(reusableArcRect, 180f, 90f)
            path.close()
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        currentFrameIndex = frame
        invalidate()
        return this
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        ensureMeasurements()
        if (lyrics.isEmpty() || measuredBubbles.isEmpty()) return

        val screenWidth = width.toFloat().takeIf { it > 0 } ?: motionConfig.aspectRatio.width.toFloat()
        val screenHeight = height.toFloat().takeIf { it > 0 } ?: motionConfig.aspectRatio.height.toFloat()

        // 1. Find last visible lyric index without allocations
        var lastVisibleIndex = -1
        for (i in lyrics.indices) {
            if (lyrics[i].frame <= currentFrameIndex) {
                lastVisibleIndex = i
            } else {
                break
            }
        }
        if (lastVisibleIndex < 0) return

        // 2. Active bubble layout & dimensions
        val activeLyric = lyrics[lastVisibleIndex]
        val activeBubble = measuredBubbles[lastVisibleIndex]
        val totalWords = activeBubble.words.size

        var activeBubbleWidth = activeBubble.bubbleWidth
        var activeBubbleHeight = activeBubble.bubbleHeight
        var activeWordIndex = totalWords - 1
        var activeWordAnimAlpha = 1.0f
        var isFirstWord = false

        if (totalWords > 0) {
            val nextFrame = if (lastVisibleIndex + 1 < lyrics.size) lyrics[lastVisibleIndex + 1].frame else (activeLyric.frame + 60)
            val duration = (nextFrame - activeLyric.frame).coerceAtLeast(1)
            val elapsed = (currentFrameIndex - activeLyric.frame).coerceAtLeast(0)
            val speed = if (writingSpeed > 0f) writingSpeed else 1.0f
            val lineTypingDuration = (duration / speed).coerceAtLeast(1f)
            val lineProgress = (elapsed / lineTypingDuration).coerceIn(0f, 1f)

            if (lineProgress < 1f) {
                val continuousWordIndex = lineProgress * totalWords
                activeWordIndex = continuousWordIndex.toInt().coerceIn(0, totalWords - 1)
                val wordSubProgress = (continuousWordIndex - activeWordIndex).coerceIn(0f, 1f)

                val framesPerWord = lineTypingDuration / totalWords
                val animFrames = minOf(6f, framesPerWord * 0.75f).coerceAtLeast(2f)
                val animDurationFraction = (animFrames / framesPerWord).coerceIn(0.2f, 0.9f)
                val wordAnimT = (wordSubProgress / animDurationFraction).coerceIn(0f, 1f)
                val f = 1f - wordAnimT
                val easedAnimT = 1f - f * f * f

                val prevDim = getBubbleDimension(lastVisibleIndex, activeWordIndex)
                val targetDim = getBubbleDimension(lastVisibleIndex, activeWordIndex + 1)

                activeBubbleWidth = prevDim.first + (targetDim.first - prevDim.first) * easedAnimT
                activeBubbleHeight = prevDim.second + (targetDim.second - prevDim.second) * easedAnimT
                activeWordAnimAlpha = easedAnimT
                isFirstWord = (activeWordIndex == 0)
            }
        }

        // 3. Compute vertical placement anchored to the BOTTOM (zero allocations)
        val sideMargin = 48f
        val topSafeArea = screenHeight * 0.12f
        val bottomSafeArea = screenHeight * 0.88f

        var prevChatHeight = 0f
        for (i in 0 until lastVisibleIndex) {
            prevChatHeight += measuredBubbles[i].bubbleHeight
            if (i > 0) prevChatHeight += bubbleSpacing
        }
        if (lastVisibleIndex > 0) {
            prevChatHeight += bubbleSpacing
        }

        val startY =
            if (lastVisibleIndex > 0) {
                val elapsedSinceLineStart = (currentFrameIndex - activeLyric.frame).coerceAtLeast(0)
                val scrollAnimDuration = 8f
                val scrollProgress = (elapsedSinceLineStart.toFloat() / scrollAnimDuration).coerceIn(0f, 1f)
                val f = 1f - scrollProgress
                val easedScrollProgress = 1f - f * f * f

                val animatedTotalHeight = prevChatHeight + activeBubbleHeight * easedScrollProgress
                bottomSafeArea - animatedTotalHeight
            } else {
                bottomSafeArea - activeBubbleHeight
            }

        var currentY = startY

        // 4. Draw bubbles directly without allocating wrappers
        for (i in 0..lastVisibleIndex) {
            val isLast = (i == lastVisibleIndex)
            val bubble = measuredBubbles[i]
            val bubbleWidth = if (isLast) activeBubbleWidth else bubble.bubbleWidth
            val bubbleHeight = if (isLast) activeBubbleHeight else bubble.bubbleHeight
            val isOutgoing = !isAlternating || (i % 2 == 0)

            val bubbleX =
                if (isOutgoing) {
                    screenWidth - sideMargin - bubbleWidth
                } else {
                    sideMargin
                }

            val bubbleTop = currentY
            val bubbleBottom = currentY + bubbleHeight

            if (bubbleBottom > 0 && bubbleTop < screenHeight) {
                // Fade out as message moves above topSafeArea
                var scrollAlpha = 1.0f
                if (bubbleTop < topSafeArea) {
                    val fadeDistance = topSafeArea * 0.8f
                    scrollAlpha = ((bubbleBottom - (topSafeArea - fadeDistance)) / (fadeDistance + bubbleHeight)).coerceIn(0f, 1f)
                }

                val bubbleAlpha =
                    if (isLast && isFirstWord) {
                        scrollAlpha * activeWordAnimAlpha
                    } else {
                        scrollAlpha
                    }

                val bubblePaint = if (isOutgoing) outgoingBubblePaint else incomingBubblePaint
                val savedBubbleAlpha = bubblePaint.alpha
                bubblePaint.alpha = (255 * bubbleAlpha).toInt().coerceIn(0, 255)

                val baseTextColor = if (isOutgoing) parsedTextColor else parsedIncomingTextColor
                val r = Color.red(baseTextColor)
                val g = Color.green(baseTextColor)
                val b = Color.blue(baseTextColor)

                val layout: StaticLayout
                if (isLast && bubble.prefixLayouts.isNotEmpty() && activeWordIndex < bubble.words.size - 1) {
                    val prefixData = bubble.prefixLayouts.getOrNull(activeWordIndex) ?: bubble.prefixLayouts.last()
                    prefixData.prevSpan?.color = Color.argb((255 * scrollAlpha).toInt().coerceIn(0, 255), r, g, b)
                    prefixData.wordSpan.color = Color.argb((255 * scrollAlpha * activeWordAnimAlpha).toInt().coerceIn(0, 255), r, g, b)
                    layout = prefixData.layout
                } else {
                    bubble.fullSpan.color = Color.argb((255 * scrollAlpha).toInt().coerceIn(0, 255), r, g, b)
                    layout = bubble.fullLayout
                }

                buildBubblePath(reusableBubblePath, bubbleWidth, bubbleHeight, isOutgoing)

                canvas.withTranslation(bubbleX, bubbleTop) {
                    drawPath(reusableBubblePath, bubblePaint)

                    val textX = if (isOutgoing) horizontalPadding else tailWidth + horizontalPadding
                    val textY = verticalPadding

                    withSave {
                        clipPath(reusableBubblePath)
                        withTranslation(textX, textY) {
                            layout.draw(this)
                        }
                    }
                }

                bubblePaint.alpha = savedBubbleAlpha
            }

            currentY += bubbleHeight + bubbleSpacing
        }
    }

    override fun getEditableProperties(): List<PropertySpec> =
        super.getEditableProperties() +
            listOf(
                PropertySpec.color("bubbleColor", label = "Bubble Color"),
                PropertySpec.color("textColor", label = "Text Color"),
                PropertySpec.color("incomingBubbleColor", label = "Incoming Bubble Color"),
                PropertySpec.color("incomingTextColor", label = "Incoming Text Color"),
                PropertySpec.dropdown("textSizeVariant", label = "Text Size", options = MotionTextVariant.entries.map { it.name }),
                PropertySpec.slider("writingSpeed", label = "Writing Speed", min = 0.0f, max = 5.0f, step = 0.1f),
                PropertySpec.slider("unwrittenTextAlpha", label = "Unwritten Text Opacity", min = 0.0f, max = 1.0f, step = 0.05f),
                PropertySpec.boolean("isAlternating", label = "Is Alternating"),
            )
}
