package com.tejpratapsingh.motionlib.templates.extensions

import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.motionlib.core.MotionAsset
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
import com.tejpratapsingh.motionlib.ui.custom.text.WordVibrateMotionTextView
import com.tejpratapsingh.motionlib.ui.custom.text.WordWriterTextView

fun ContentScope.popUpTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    writingSpeed: Float = 0f,
    unwrittenTextAlpha: Float = 0f,
    maxTranslationY: Float = 50f,
    textView: AppCompatTextView = AppCompatTextView(context),
    fontAsset: MotionAsset? = null,
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
    fontAsset,
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
    cursorChar: String? = "|",
    blinkFrameRate: Int = 10,
    textView: AppCompatTextView = AppCompatTextView(context),
    fontAsset: MotionAsset? = null,
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
    cursorChar,
    blinkFrameRate,
    textView,
    fontAsset,
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
    fontAsset: MotionAsset? = null,
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
    fontAsset,
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
    fontAsset: MotionAsset? = null,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (TransparentTextView.() -> Unit)? = null,
) = TransparentTextView(
    context,
    text,
    startFrame,
    endFrame,
    fontAsset,
    textSizeVariant,
    textColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.wordBlinkTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    writingSpeed: Float = 0f,
    fontAsset: MotionAsset? = null,
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
    fontAsset = fontAsset,
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
    fontAsset: MotionAsset? = null,
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
    fontAsset,
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
    accentColor: String = "#FFFF00",
    textView: AppCompatTextView = AppCompatTextView(context),
    fontAsset: MotionAsset? = null,
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
    fontAsset,
    textSizeVariant,
    textColor,
    highlightColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }

fun ContentScope.wordVibrateMotionTextView(
    text: String,
    startFrame: Int,
    endFrame: Int,
    amplitude: Float = 5f,
    frequency: Float = 0.5f,
    phaseShiftPerWord: Float = 1.0f,
    textView: AppCompatTextView = AppCompatTextView(context),
    fontAsset: MotionAsset? = null,
    textSizeVariant: MotionTextVariant? = null,
    textColor: String? = null,
    highlightColor: String? = null,
    effects: List<MotionEffect> = emptyList(),
    layoutInfo: MotionLayoutInfo = MotionLayoutInfo(),
    block: (WordVibrateMotionTextView.() -> Unit)? = null,
) = WordVibrateMotionTextView(
    context,
    text,
    startFrame,
    endFrame,
    amplitude,
    frequency,
    phaseShiftPerWord,
    textView,
    fontAsset,
    textSizeVariant,
    textColor,
    highlightColor,
    effects = effects,
).apply { this.layoutInfo = layoutInfo }
    .apply { block?.invoke(this) }
    .also { addView(it) }
