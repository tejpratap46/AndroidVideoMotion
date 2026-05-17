package com.tejpratapsingh.motionlib.templates.extensions

import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.ui.custom.text.AccentMiddlePopUpTextView
import com.tejpratapsingh.motionlib.ui.custom.text.PopUpTextView
import com.tejpratapsingh.motionlib.ui.custom.text.RainbowPopUpTextView
import com.tejpratapsingh.motionlib.ui.custom.text.TransparentTextView
import com.tejpratapsingh.motionlib.ui.custom.text.TypeWriterTextView
import com.tejpratapsingh.motionlib.ui.custom.text.WordBlinkTextView
import com.tejpratapsingh.motionlib.ui.custom.text.WordWriterTextView

fun ContentScope.popUpTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    writingSpeed: Float = 0f,
    unwrittenTextAlpha: Float = 0f,
    maxTranslationY: Float = 50f,
    textView: AppCompatTextView = AppCompatTextView(context),
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    highlightColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (PopUpTextView.() -> Unit)? = null,
) = PopUpTextView(
    context,
    text,
    startFrame,
    endFrame,
    writingSpeed,
    unwrittenTextAlpha,
    maxTranslationY,
    textView,
    textSizeVariant,
    textColor,
    highlightColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.typeWriterTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    writingSpeed: Float = 0f,
    unwrittenTextAlpha: Float = 0f,
    textView: AppCompatTextView = AppCompatTextView(context),
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (TypeWriterTextView.() -> Unit)? = null,
) = TypeWriterTextView(
    context,
    text,
    startFrame,
    endFrame,
    writingSpeed,
    unwrittenTextAlpha,
    textView,
    textSizeVariant,
    textColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.wordWriterTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    writingSpeed: Float = 0f,
    unwrittenTextAlpha: Float = 0f,
    textView: AppCompatTextView = AppCompatTextView(context),
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    highlightColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (WordWriterTextView.() -> Unit)? = null,
) = WordWriterTextView(
    context,
    text,
    startFrame,
    endFrame,
    writingSpeed,
    unwrittenTextAlpha,
    textView,
    textSizeVariant,
    textColor,
    highlightColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.transparentTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (TransparentTextView.() -> Unit)? = null,
) = TransparentTextView(context, text, startFrame, endFrame, textSizeVariant, textColor, effects = effects)
    .apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.wordBlinkTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    writingSpeed: Float = 0f,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (WordBlinkTextView.() -> Unit)? = null,
) = WordBlinkTextView(
    context,
    text,
    startFrame,
    endFrame,
    writingSpeed,
    textSizeVariant = textSizeVariant,
    textColor = textColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.rainbowPopUpTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    writingSpeed: Float = 0f,
    unwrittenTextAlpha: Float = 0f,
    maxTranslationY: Float = 50f,
    textView: AppCompatTextView = AppCompatTextView(context),
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    highlightColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (RainbowPopUpTextView.() -> Unit)? = null,
) = RainbowPopUpTextView(
    context,
    text,
    startFrame,
    endFrame,
    writingSpeed,
    unwrittenTextAlpha,
    maxTranslationY,
    textView,
    textSizeVariant,
    textColor,
    highlightColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.accentMiddlePopUpTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    writingSpeed: Float = 0f,
    unwrittenTextAlpha: Float = 0f,
    maxTranslationY: Float = 50f,
    accentColor: Int = android.graphics.Color.YELLOW,
    textView: AppCompatTextView = AppCompatTextView(context),
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    highlightColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (AccentMiddlePopUpTextView.() -> Unit)? = null,
) = AccentMiddlePopUpTextView(
    context,
    text,
    startFrame,
    endFrame,
    writingSpeed,
    unwrittenTextAlpha,
    maxTranslationY,
    accentColor,
    textView,
    textSizeVariant,
    textColor,
    highlightColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
