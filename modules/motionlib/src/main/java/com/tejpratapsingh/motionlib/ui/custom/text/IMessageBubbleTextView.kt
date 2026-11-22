package com.tejpratapsingh.motionlib.ui.custom.text

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
import androidx.core.graphics.withTranslation
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.PropertySpec
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.getFontSize
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.utils.getWebFont

/**
 * A [MotionView] that displays a line of text inside an Apple iMessage-styled chat bubble.
 * Sized according to the words it contains with word-by-word typewriter animation.
 *
 * @param context Android context.
 * @param text The text to be displayed.
 * @param startFrame The frame when the bubble starts appearing.
 * @param endFrame The frame when the bubble stops appearing (-1 for indefinite).
 * @param writingSpeed Speed factor for word-by-word reveal (0 = auto-fit to duration).
 * @param unwrittenTextAlpha Opacity for words not yet revealed.
 * @param bubbleColor Hex color for the bubble background (defaults to Apple iMessage blue #007AFF).
 * @param textColor Hex color for the revealed text (defaults to #FFFFFF).
 * @param isOutgoing True for sent message (right tail, right aligned), false for received message (left tail).
 * @param fontAsset Optional custom web font asset.
 * @param textSizeVariant Text scale variant (defaults to H3).
 * @param effects Effects list applied to this view.
 */
class IMessageBubbleTextView(
    context: Context,
    val text: String,
    startFrame: Int = 0,
    endFrame: Int = -1,
    val writingSpeed: Float = 0f,
    val unwrittenTextAlpha: Float = 0f,
    val bubbleColor: String = "#007AFF",
    val textColor: String = "#FFFFFF",
    val isOutgoing: Boolean = true,
    val fontAsset: MotionAsset? = null,
    val textSizeVariant: MotionTextVariant? = MotionTextVariant.H3,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {
    private val wordArray: List<String> = if (text.isEmpty()) emptyList() else text.split(" ")
    private val wordCount: Int = wordArray.size

    private val inferredEndFrame: Int =
        if (endFrame != -1 && writingSpeed > 0) {
            (startFrame + (endFrame - startFrame) / writingSpeed).toInt()
        } else {
            endFrame
        }

    private var parsedTextColor: Int = Color.WHITE
    private var parsedBubbleColor: Int = Color.parseColor("#007AFF")

    private val textPaint: TextPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            val fontSize = motionConfig.getFontSize(textSizeVariant ?: MotionTextVariant.H3)
            textSize = fontSize
            color = parsedTextColor
            fontAsset?.let {
                typeface = context.getWebFont(it.getUri().toString())
            }
        }
    }

    private val bubblePaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = parsedBubbleColor
        }
    }

    private class MutableColorSpan(var color: Int = Color.WHITE) : CharacterStyle(), UpdateAppearance {
        override fun updateDrawState(tp: TextPaint) {
            tp.color = color
        }
    }

    private val reusableBubblePath = Path()
    private val reusableArcRect = RectF()
    private var isPathBuilt = false
    private var cachedLayout: StaticLayout? = null

    private val writtenSpan = MutableColorSpan(Color.WHITE)
    private val unwrittenSpan = MutableColorSpan(Color.TRANSPARENT)
    private val preallocatedSpannable: SpannableString by lazy {
        SpannableString(text).apply {
            if (text.isNotEmpty()) {
                setSpan(writtenSpan, 0, 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(unwrittenSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
    private var wordEndCharIndices: IntArray = IntArray(0)

    private var calculatedBubbleWidth: Int = 0
    private var calculatedBubbleHeight: Int = 0
    private var horizontalPadding: Float = 0f
    private var verticalPadding: Float = 0f
    private var tailWidth: Float = 0f
    private var tailHeight: Float = 0f
    private var cornerRadius: Float = 0f
    private var maxLayoutWidth: Int = 0
    private var isMeasured = false

    init {
        setWillNotDraw(false)

        parsedTextColor =
            try {
                textColor.toColorInt()
            } catch (e: Exception) {
                Color.WHITE
            }
        parsedBubbleColor =
            try {
                bubbleColor.toColorInt()
            } catch (e: Exception) {
                Color.parseColor("#007AFF")
            }

        writtenSpan.color = parsedTextColor

        contourWidthOf {
            if (layoutInfo.width > 0) {
                layoutInfo.width.toXInt()
            } else {
                ensureMeasurements()
                calculatedBubbleWidth.toXInt()
            }
        }

        contourHeightOf {
            if (layoutInfo.height > 0) {
                layoutInfo.height.toYInt()
            } else {
                ensureMeasurements()
                calculatedBubbleHeight.toYInt()
            }
        }
    }

    private fun ensureMeasurements() {
        if (isMeasured || text.isEmpty()) return

        val fontSize = textPaint.textSize
        horizontalPadding = fontSize * 0.7f
        verticalPadding = fontSize * 0.5f
        tailWidth = fontSize * 0.35f
        tailHeight = fontSize * 0.45f
        cornerRadius = (fontSize * 0.65f).coerceIn(16f, 48f)

        val maxAllowedTextWidth = (motionConfig.aspectRatio.width * 0.72f) - (horizontalPadding * 2 + tailWidth)
        val measuredSingleLineWidth = textPaint.measureText(text)
        maxLayoutWidth =
            if (measuredSingleLineWidth > maxAllowedTextWidth) {
                maxAllowedTextWidth.toInt().coerceAtLeast(1)
            } else {
                (measuredSingleLineWidth + 4).toInt()
            }

        val layout = createStaticLayout(text)
        val actualLineWidth =
            if (layout.lineCount > 0) {
                (0 until layout.lineCount).maxOf { layout.getLineWidth(it) }
            } else {
                measuredSingleLineWidth
            }

        calculatedBubbleWidth = (actualLineWidth + horizontalPadding * 2 + tailWidth).toInt()
        calculatedBubbleHeight = (layout.height + verticalPadding * 2).toInt()
        wordEndCharIndices =
            IntArray(wordArray.size) { i ->
                wordArray.take(i + 1).joinToString(" ").length
            }
        isMeasured = true
    }

    private fun createStaticLayout(content: CharSequence): StaticLayout {
        val width = maxLayoutWidth.coerceAtLeast(1)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(content, 0, content.length, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(content, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false)
        }
    }

    private fun buildBubblePath(
        path: Path,
        width: Float,
        height: Float,
    ) {
        path.reset()
        val r = cornerRadius.coerceAtMost(height / 2f).coerceAtMost(width / 2f)
        val tw = tailWidth.coerceAtMost(width * 0.3f)
        val th = tailHeight.coerceAtMost(height * 0.4f)

        if (isOutgoing) {
            // Outgoing message: tail on bottom-right
            val rightBody = width - tw
            path.moveTo(r, 0f)
            path.lineTo(rightBody - r, 0f)
            reusableArcRect.set(rightBody - 2 * r, 0f, rightBody, 2 * r)
            path.arcTo(reusableArcRect, -90f, 90f)
            path.lineTo(rightBody, height - th)
            // Tail curving out to bottom-right corner
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
            // Incoming message: tail on bottom-left
            val leftBody = tw
            path.moveTo(leftBody + r, 0f)
            path.lineTo(width - r, 0f)
            reusableArcRect.set(width - 2 * r, 0f, width, 2 * r)
            path.arcTo(reusableArcRect, -90f, 90f)
            path.lineTo(width, height - r)
            reusableArcRect.set(width - 2 * r, height - 2 * r, width, height)
            path.arcTo(reusableArcRect, 0f, 90f)
            path.lineTo(leftBody + r, height)
            // Tail curving out to bottom-left corner
            path.quadTo(leftBody + tw * 0.3f, height, 0f, height)
            path.quadTo(leftBody, height - th * 0.25f, leftBody, height - th)
            path.lineTo(leftBody, r)
            reusableArcRect.set(leftBody, 0f, leftBody + 2 * r, 2 * r)
            path.arcTo(reusableArcRect, 180f, 90f)
            path.close()
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        ensureMeasurements()

        if (wordCount > 0) {
            val progress: Float =
                MotionInterpolator.interpolateForRange(
                    Interpolators(Easings.LINEAR),
                    frame,
                    Pair(startFrame, inferredEndFrame),
                    Pair(0f, wordCount.toFloat()),
                )

            val visibleWordCount = progress.toInt().coerceIn(0, wordCount)
            val visibleCharsCount =
                if (visibleWordCount >= wordCount) {
                    text.length
                } else if (visibleWordCount <= 0) {
                    0
                } else {
                    wordEndCharIndices[visibleWordCount - 1]
                }

            val unwrittenColor =
                Color.argb(
                    (unwrittenTextAlpha * 255).toInt().coerceIn(0, 255),
                    Color.red(parsedTextColor),
                    Color.green(parsedTextColor),
                    Color.blue(parsedTextColor),
                )

            writtenSpan.color = parsedTextColor
            unwrittenSpan.color = unwrittenColor

            if (text.isNotEmpty()) {
                preallocatedSpannable.setSpan(
                    writtenSpan,
                    0,
                    visibleCharsCount,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                preallocatedSpannable.setSpan(
                    unwrittenSpan,
                    visibleCharsCount,
                    text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }

        invalidate()
        return this
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        ensureMeasurements()

        val w = width.toFloat().takeIf { it > 0 } ?: calculatedBubbleWidth.toFloat()
        val h = height.toFloat().takeIf { it > 0 } ?: calculatedBubbleHeight.toFloat()
        if (w <= 0f || h <= 0f || text.isEmpty()) return

        // 1. Draw Apple iMessage bubble
        if (!isPathBuilt) {
            buildBubblePath(reusableBubblePath, w, h)
            isPathBuilt = true
        }
        canvas.drawPath(reusableBubblePath, bubblePaint)

        // 2. Draw word-by-word text inside bubble
        val textX = if (isOutgoing) horizontalPadding else tailWidth + horizontalPadding
        val textY = verticalPadding

        val layout = cachedLayout ?: createStaticLayout(preallocatedSpannable).also { cachedLayout = it }
        canvas.withTranslation(textX, textY) {
            layout.draw(canvas)
        }
    }

    override fun getEditableProperties(): List<PropertySpec> =
        super.getEditableProperties() +
            listOf(
                PropertySpec.text("text", label = "Text Content"),
                PropertySpec.color("bubbleColor", label = "Bubble Color"),
                PropertySpec.color("textColor", label = "Text Color"),
                PropertySpec.dropdown("textSizeVariant", label = "Text Size", options = MotionTextVariant.entries.map { it.name }),
                PropertySpec.slider("writingSpeed", label = "Writing Speed", min = 0.0f, max = 5.0f, step = 0.1f),
                PropertySpec.slider("unwrittenTextAlpha", label = "Unwritten Text Opacity", min = 0.0f, max = 1.0f, step = 0.05f),
                PropertySpec.boolean("isOutgoing", label = "Is Outgoing"),
            )
}
