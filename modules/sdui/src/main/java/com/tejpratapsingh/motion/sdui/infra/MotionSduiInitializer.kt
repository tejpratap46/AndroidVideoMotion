package com.tejpratapsingh.motion.sdui.infra

import android.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.assettype.FontAsset
import com.tejpratapsingh.motionlib.assettype.ImageAsset
import com.tejpratapsingh.motionlib.assettype.SimpleMotionAsset
import com.tejpratapsingh.motionlib.assettype.VideoAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.motion.transitions.BlurTransition
import com.tejpratapsingh.motionlib.core.motion.transitions.CrossFadeTransition
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideDirection
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideTransition
import com.tejpratapsingh.motionlib.ui.custom.audio.CircularAudioWaveformView
import com.tejpratapsingh.motionlib.ui.custom.audio.RadialAudioWaveformView
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation
import com.tejpratapsingh.motionlib.ui.custom.background.TranslucentMotionView
import com.tejpratapsingh.motionlib.ui.custom.image.CircularMotionImageView
import com.tejpratapsingh.motionlib.ui.custom.image.MotionImageView
import com.tejpratapsingh.motionlib.ui.custom.progress.MotionProgressBar
import com.tejpratapsingh.motionlib.ui.custom.progress.MotionProgressBarStyle
import com.tejpratapsingh.motionlib.ui.custom.stack.HorizontalStackMotionView
import com.tejpratapsingh.motionlib.ui.custom.stack.StackSection
import com.tejpratapsingh.motionlib.ui.custom.stack.VerticalStackMotionView
import com.tejpratapsingh.motionlib.ui.custom.text.AccentMiddlePopUpTextView
import com.tejpratapsingh.motionlib.ui.custom.text.PopUpTextView
import com.tejpratapsingh.motionlib.ui.custom.text.RainbowPopUpTextView
import com.tejpratapsingh.motionlib.ui.custom.text.TransparentTextView
import com.tejpratapsingh.motionlib.ui.custom.text.TypeWriterTextView
import com.tejpratapsingh.motionlib.ui.custom.text.WordBlinkTextView
import com.tejpratapsingh.motionlib.ui.custom.text.WordVibrateMotionTextView
import com.tejpratapsingh.motionlib.ui.custom.text.WordWriterTextView
import com.tejpratapsingh.motionlib.ui.custom.video.VideoFrameView
import com.tejpratapsingh.motionlib.ui.effects.BlurEffect
import com.tejpratapsingh.motionlib.ui.effects.FadeInEffect
import com.tejpratapsingh.motionlib.ui.effects.FadeOutEffect
import com.tejpratapsingh.motionlib.ui.effects.GlitchEffect
import com.tejpratapsingh.motionlib.ui.effects.SlideBottomToTopEffect
import com.tejpratapsingh.motionlib.ui.effects.SlideEffect
import com.tejpratapsingh.motionlib.ui.effects.SlideLeftToRightEffect
import com.tejpratapsingh.motionlib.ui.effects.SlideRightToLeftEffect
import com.tejpratapsingh.motionlib.ui.effects.SlideTopToBottomEffect
import com.tejpratapsingh.motionlib.ui.effects.VibrateEffect
import com.tejpratapsingh.motionlib.ui.effects.VintageEffect
import com.tejpratapsingh.motionlib.ui.effects.ZoomInEffect
import com.tejpratapsingh.motionlib.ui.effects.ZoomOutEffect

/**
 * Initializer for [MotionSdui] registry.
 * Registers common [MotionView] and [MotionEffect] types.
 */
object MotionSduiInitializer {
    fun initialize() {
        // Register SimpleMotionAsset
        MotionSdui.registerAsset(SimpleMotionAsset::class.java.simpleName) { _, json ->
            val uri = json.get("uri")?.asString ?: throw IllegalArgumentException("uri required for SimpleMotionAsset")
            val metadata = json.get("metadata")?.asJsonObject
            SimpleMotionAsset(uri.toUri(), metadata)
        }
        MotionSdui.registerAssetSerializer(SimpleMotionAsset::class.java) { _, _ -> }

        // Register ImageAsset
        MotionSdui.registerAsset(ImageAsset::class.java.simpleName) { _, json ->
            val uri = json.get("uri")?.asString ?: throw IllegalArgumentException("uri required for ImageAsset")
            val metadata = json.get("metadata")?.asJsonObject
            ImageAsset(uri.toUri(), metadata)
        }
        MotionSdui.registerAssetSerializer(ImageAsset::class.java) { _, _ -> }

        // Register VideoAsset
        MotionSdui.registerAsset(VideoAsset::class.java.simpleName) { _, json ->
            val uri = json.get("uri")?.asString ?: throw IllegalArgumentException("uri required for VideoAsset")
            val metadata = json.get("metadata")?.asJsonObject
            VideoAsset(uri.toUri(), metadata)
        }
        MotionSdui.registerAssetSerializer(VideoAsset::class.java) { _, _ -> }

        // Register FontAsset
        MotionSdui.registerAsset(FontAsset::class.java.simpleName) { _, json ->
            val uri = json.get("uri")?.asString ?: throw IllegalArgumentException("uri required for FontAsset")
            val fontName = json.get("fontName")?.asString
            val metadata = json.get("metadata")?.asJsonObject
            FontAsset(uri.toUri(), fontName, metadata)
        }
        MotionSdui.registerAssetSerializer(FontAsset::class.java) { asset, json ->
            asset.fontName?.let { json.addProperty("fontName", it) }
        }

        // Register MotionProgressBar
        MotionSdui.registerView(MotionProgressBar::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val style =
                json.get("style")?.asString?.let { MotionProgressBarStyle.valueOf(it) }
                    ?: MotionProgressBarStyle.HORIZONTAL
            val color =
                json.get("color")?.asString?.let { it.toColorInt() }
                    ?: Color.WHITE
            MotionProgressBar(
                context = context,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
                style = style,
                color = color,
            )
        }
        MotionSdui.registerViewSerializer(MotionProgressBar::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("style", view.style.name)
            json.addProperty("color", String.format("#%06X", 0xFFFFFF and view.color))
        }

        // Register TransparentTextView
        MotionSdui.registerView(TransparentTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val text = json.get("text")?.asString ?: ""
            val fontAsset = json.get("fontAsset")?.asJsonObject?.toMotionAsset(context)
            val textSizeVariant = json.get("textSizeVariant")?.asString?.let { MotionTextVariant.valueOf(it) }
            val textColor = json.get("textColor")?.asString
            TransparentTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                fontAsset = fontAsset,
                textSizeVariant = textSizeVariant,
                textColor = textColor,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(TransparentTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            view.fontAsset?.let { json.add("fontAsset", it.toJson()) }
            view.textSizeVariant?.let { json.addProperty("textSizeVariant", it.name) }
            view.textColor?.let { json.addProperty("textColor", it) }
            view.highlightColor?.let { json.addProperty("highlightColor", it) }
        }

        // Register TypeWriterTextView
        MotionSdui.registerView(TypeWriterTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            val unwrittenTextAlpha = json.get("unwrittenTextAlpha")?.asFloat ?: 0f
            val cursorChar = if (json.has("cursorChar")) json.get("cursorChar")?.asString else "|"
            val blinkFrameRate = json.get("blinkFrameRate")?.asInt ?: 10
            val fontAsset = json.get("fontAsset")?.asJsonObject?.toMotionAsset(context)
            val textSizeVariant = json.get("textSizeVariant")?.asString?.let { MotionTextVariant.valueOf(it) }
            val textColor = json.get("textColor")?.asString
            TypeWriterTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                unwrittenTextAlpha = unwrittenTextAlpha,
                cursorChar = cursorChar,
                blinkFrameRate = blinkFrameRate,
                fontAsset = fontAsset,
                textSizeVariant = textSizeVariant,
                textColor = textColor,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(TypeWriterTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            view.fontAsset?.let { json.add("fontAsset", it.toJson()) }
            json.addProperty("writingSpeed", view.writingSpeed)
            json.addProperty("unwrittenTextAlpha", view.unwrittenTextAlpha)
            json.addProperty("cursorChar", view.cursorChar)
            json.addProperty("blinkFrameRate", view.blinkFrameRate)
            view.textSizeVariant?.let { json.addProperty("textSizeVariant", it.name) }
            view.textColor?.let { json.addProperty("textColor", it) }
            view.highlightColor?.let { json.addProperty("highlightColor", it) }
        }

        // Register WordWriterTextView
        MotionSdui.registerView(WordWriterTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            val unwrittenTextAlpha = json.get("unwrittenTextAlpha")?.asFloat ?: 0f
            val fontAsset = json.get("fontAsset")?.asJsonObject?.toMotionAsset(context)
            val textSizeVariant = json.get("textSizeVariant")?.asString?.let { MotionTextVariant.valueOf(it) }
            val textColor = json.get("textColor")?.asString
            val highlightColor = json.get("highlightColor")?.asString
            WordWriterTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                unwrittenTextAlpha = unwrittenTextAlpha,
                fontAsset = fontAsset,
                textSizeVariant = textSizeVariant,
                textColor = textColor,
                highlightColor = highlightColor,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(WordWriterTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            view.fontAsset?.let { json.add("fontAsset", it.toJson()) }
            json.addProperty("writingSpeed", view.writingSpeed)
            json.addProperty("unwrittenTextAlpha", view.unwrittenTextAlpha)
            view.textSizeVariant?.let { json.addProperty("textSizeVariant", it.name) }
            view.textColor?.let { json.addProperty("textColor", it) }
            view.highlightColor?.let { json.addProperty("highlightColor", it) }
        }

        // Register WordBlinkTextView
        MotionSdui.registerView(WordBlinkTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            val fontAsset = json.get("fontAsset")?.asJsonObject?.toMotionAsset(context)
            val textSizeVariant = json.get("textSizeVariant")?.asString?.let { MotionTextVariant.valueOf(it) }
            val textColor = json.get("textColor")?.asString
            WordBlinkTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                fontAsset = fontAsset,
                textSizeVariant = textSizeVariant,
                textColor = textColor,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(WordBlinkTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            view.fontAsset?.let { json.add("fontAsset", it.toJson()) }
            json.addProperty("writingSpeed", view.writingSpeed)
            view.textSizeVariant?.let { json.addProperty("textSizeVariant", it.name) }
            view.textColor?.let { json.addProperty("textColor", it) }
            view.highlightColor?.let { json.addProperty("highlightColor", it) }
        }

        // Register PopUpTextView
        MotionSdui.registerView(PopUpTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            val unwrittenTextAlpha = json.get("unwrittenTextAlpha")?.asFloat ?: 0f
            val maxTranslationY = json.get("maxTranslationY")?.asFloat ?: 50f
            val fontAsset = json.get("fontAsset")?.asJsonObject?.toMotionAsset(context)
            val textSizeVariant = json.get("textSizeVariant")?.asString?.let { MotionTextVariant.valueOf(it) }
            val textColor = json.get("textColor")?.asString
            val highlightColor = json.get("highlightColor")?.asString
            PopUpTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                unwrittenTextAlpha = unwrittenTextAlpha,
                maxTranslationY = maxTranslationY,
                fontAsset = fontAsset,
                textSizeVariant = textSizeVariant,
                textColor = textColor,
                highlightColor = highlightColor,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(PopUpTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            view.fontAsset?.let { json.add("fontAsset", it.toJson()) }
            json.addProperty("writingSpeed", view.writingSpeed)
            json.addProperty("unwrittenTextAlpha", view.unwrittenTextAlpha)
            json.addProperty("maxTranslationY", view.maxTranslationY)
            view.textSizeVariant?.let { json.addProperty("textSizeVariant", it.name) }
            view.textColor?.let { json.addProperty("textColor", it) }
            view.highlightColor?.let { json.addProperty("highlightColor", it) }
        }

        // Register RainbowPopUpTextView
        MotionSdui.registerView(RainbowPopUpTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            val unwrittenTextAlpha = json.get("unwrittenTextAlpha")?.asFloat ?: 0f
            val maxTranslationY = json.get("maxTranslationY")?.asFloat ?: 50f
            val fontAsset = json.get("fontAsset")?.asJsonObject?.toMotionAsset(context)
            val textSizeVariant = json.get("textSizeVariant")?.asString?.let { MotionTextVariant.valueOf(it) }
            val textColor = json.get("textColor")?.asString
            val highlightColor = json.get("highlightColor")?.asString
            RainbowPopUpTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                unwrittenTextAlpha = unwrittenTextAlpha,
                maxTranslationY = maxTranslationY,
                fontAsset = fontAsset,
                textSizeVariant = textSizeVariant,
                textColor = textColor,
                highlightColor = highlightColor,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(RainbowPopUpTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            view.fontAsset?.let { json.add("fontAsset", it.toJson()) }
            json.addProperty("writingSpeed", view.writingSpeed)
            json.addProperty("unwrittenTextAlpha", view.unwrittenTextAlpha)
            json.addProperty("maxTranslationY", view.maxTranslationY)
            view.textSizeVariant?.let { json.addProperty("textSizeVariant", it.name) }
            view.textColor?.let { json.addProperty("textColor", it) }
            view.highlightColor?.let { json.addProperty("highlightColor", it) }
        }

        // Register AccentMiddlePopUpTextView
        MotionSdui.registerView(AccentMiddlePopUpTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            val unwrittenTextAlpha = json.get("unwrittenTextAlpha")?.asFloat ?: 0f
            val maxTranslationY = json.get("maxTranslationY")?.asFloat ?: 50f
            val accentColor = json.get("accentColor")?.asString
            val fontAsset = json.get("fontAsset")?.asJsonObject?.toMotionAsset(context)
            val textSizeVariant = json.get("textSizeVariant")?.asString?.let { MotionTextVariant.valueOf(it) }
            val textColor = json.get("textColor")?.asString
            val highlightColor = json.get("highlightColor")?.asString
            AccentMiddlePopUpTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                unwrittenTextAlpha = unwrittenTextAlpha,
                maxTranslationY = maxTranslationY,
                accentColor = accentColor,
                fontAsset = fontAsset,
                textSizeVariant = textSizeVariant,
                textColor = textColor,
                highlightColor = highlightColor,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(AccentMiddlePopUpTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            view.fontAsset?.let { json.add("fontAsset", it.toJson()) }
            json.addProperty("writingSpeed", view.writingSpeed)
            json.addProperty("unwrittenTextAlpha", view.unwrittenTextAlpha)
            json.addProperty("maxTranslationY", view.maxTranslationY)
            view.accentColor?.let { json.addProperty("accentColor", it) }
            view.textSizeVariant?.let { json.addProperty("textSizeVariant", it.name) }
            view.textColor?.let { json.addProperty("textColor", it) }
            view.highlightColor?.let { json.addProperty("highlightColor", it) }
        }

        // Register WordVibrateMotionTextView
        MotionSdui.registerView(WordVibrateMotionTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val text = json.get("text")?.asString ?: ""
            val amplitude = json.get("amplitude")?.asFloat ?: 5f
            val frequency = json.get("frequency")?.asFloat ?: 0.5f
            val phaseShiftPerWord = json.get("phaseShiftPerWord")?.asFloat ?: 1.0f
            val fontAsset = json.get("fontAsset")?.let { it.asJsonObject.toMotionAsset(context) }
            val textSizeVariant =
                json.get("textSizeVariant")?.asString?.let { MotionTextVariant.valueOf(it) }
            val textColor = json.get("textColor")?.asString
            val highlightColor = json.get("highlightColor")?.asString

            WordVibrateMotionTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                amplitude = amplitude,
                frequency = frequency,
                phaseShiftPerWord = phaseShiftPerWord,
                fontAsset = fontAsset,
                textSizeVariant = textSizeVariant,
                textColor = textColor,
                highlightColor = highlightColor,
                effects = props.effects,
            ).apply {
                this.layoutInfo = props.layoutInfo
            }
        }

        MotionSdui.registerViewSerializer(WordVibrateMotionTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            json.addProperty("amplitude", view.amplitude)
            json.addProperty("frequency", view.frequency)
            json.addProperty("phaseShiftPerWord", view.phaseShiftPerWord)
            view.textColor?.let { json.addProperty("textColor", it) }
            view.highlightColor?.let { json.addProperty("highlightColor", it) }
            view.textSizeVariant?.let { json.addProperty("textSizeVariant", it.name) }
            view.fontAsset?.let { json.add("fontAsset", it.toJson()) }
            view.layoutInfo.let { json.add("layoutInfo", it.toJson()) }
        }

        // Register CircularMotionImageView
        MotionSdui.registerView(CircularMotionImageView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val asset = json.get("asset").asJsonObject.toMotionAsset(context)
            CircularMotionImageView(
                context = context,
                asset = asset,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(CircularMotionImageView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.add("asset", view.asset.toJson())
        }

        // Register MotionImageView
        MotionSdui.registerView(MotionImageView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val asset = json.get("asset").asJsonObject.toMotionAsset(context)
            MotionImageView(
                context = context,
                asset = asset,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(MotionImageView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.add("asset", view.asset.toJson())
        }

        // Register VideoFrameView
        MotionSdui.registerView(VideoFrameView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val asset = json.get("asset").asJsonObject.toMotionAsset(context)
            VideoFrameView(
                context = context,
                asset = asset,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(VideoFrameView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.add("asset", view.asset.toJson())
        }

        // Register GradientView
        MotionSdui.registerView(GradientView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val orientationStr = json.get("orientation")?.asString ?: "VERTICAL"
            val orientation =
                try {
                    Orientation.valueOf(orientationStr)
                } catch (_: Exception) {
                    Orientation.VERTICAL
                }

            val colorsJson = json.getAsJsonArray("colors")
            val colors =
                if (colorsJson != null) {
                    IntArray(colorsJson.size()) { i ->
                        val colorStr = colorsJson.get(i).asString
                        colorStr.toColorInt()
                    }
                } else {
                    intArrayOf(android.graphics.Color.BLACK, android.graphics.Color.WHITE)
                }

            GradientView(
                context = context,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                orientation = orientation,
                colors = colors,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(GradientView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("orientation", view.orientation.name)
            val colorsArray = JsonArray()
            view.colors.forEach { color ->
                colorsArray.add(String.format("#%06X", 0xFFFFFF and color))
            }
            json.add("colors", colorsArray)
        }

        // Register TranslucentMotionView
        MotionSdui.registerView(TranslucentMotionView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val color = json.get("color")?.asString ?: "#00000000"
            val alpha = json.get("alpha")?.asFloat ?: 1.0f
            TranslucentMotionView(
                context = context,
                color = color,
                alpha = alpha,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            ).apply {
                this.layoutInfo = props.layoutInfo
            }
        }
        MotionSdui.registerViewSerializer(TranslucentMotionView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("color", view.color)
            json.addProperty("alpha", view.alpha)
        }

        // Register CircularAudioWaveformView
        MotionSdui.registerView(CircularAudioWaveformView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val amplitudesJson = json.getAsJsonArray("amplitudes")
            val amplitudes = mutableListOf<Float>()
            amplitudesJson?.forEach { amplitudes.add(it.asFloat) }

            CircularAudioWaveformView(
                context = context,
                amplitudes = amplitudes,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(CircularAudioWaveformView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            val amplitudesArray = JsonArray()
            view.amplitudes.forEach { amplitudesArray.add(it) }
            json.add("amplitudes", amplitudesArray)
        }

        // Register RadialAudioWaveformView
        MotionSdui.registerView(RadialAudioWaveformView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val amplitudesJson = json.getAsJsonArray("amplitudes")
            val amplitudes = mutableListOf<Float>()
            amplitudesJson?.forEach { amplitudes.add(it.asFloat) }

            RadialAudioWaveformView(
                context = context,
                amplitudes = amplitudes,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(RadialAudioWaveformView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            val amplitudesArray = JsonArray()
            view.amplitudes.forEach { amplitudesArray.add(it) }
            json.add("amplitudes", amplitudesArray)
        }

        // Register HorizontalStackMotionView
        MotionSdui.registerView(HorizontalStackMotionView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val sectionsJson = json.getAsJsonArray("sections")
            val sections = mutableListOf<StackSection>()
            sectionsJson?.forEach { sectionElement ->
                val sectionObject = sectionElement.asJsonObject
                val view = sectionObject.get("view").asJsonObject.toMotionView(context)
                val percentage = sectionObject.get("percentage").asFloat
                sections.add(StackSection(view, percentage))
            }
            HorizontalStackMotionView(
                context = context,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                sections = sections,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(HorizontalStackMotionView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            val sectionsArray = JsonArray()
            view.sections.forEach { section ->
                val sectionObject = JsonObject()
                sectionObject.add("view", section.view.toJson())
                sectionObject.addProperty("percentage", section.percentage)
                sectionsArray.add(sectionObject)
            }
            json.add("sections", sectionsArray)
        }

        // Register VerticalStackMotionView
        MotionSdui.registerView(VerticalStackMotionView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps(context)
            val sectionsJson = json.getAsJsonArray("sections")
            val sections = mutableListOf<StackSection>()
            sectionsJson?.forEach { sectionElement ->
                val sectionObject = sectionElement.asJsonObject
                val view = sectionObject.get("view").asJsonObject.toMotionView(context)
                val percentage = sectionObject.get("percentage").asFloat
                sections.add(StackSection(view, percentage))
            }
            VerticalStackMotionView(
                context = context,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                sections = sections,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(VerticalStackMotionView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            val sectionsArray = JsonArray()
            view.sections.forEach { section ->
                val sectionObject = JsonObject()
                sectionObject.add("view", section.view.toJson())
                sectionObject.addProperty("percentage", section.percentage)
                sectionsArray.add(sectionObject)
            }
            json.add("sections", sectionsArray)
        }

        // Register SlideRightToLeftEffect
        MotionSdui.registerEffect(SlideRightToLeftEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            SlideRightToLeftEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(SlideRightToLeftEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
        }

        // Register SlideLeftToRightEffect
        MotionSdui.registerEffect(SlideLeftToRightEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            SlideLeftToRightEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(SlideLeftToRightEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
        }

        // Register SlideTopToBottomEffect
        MotionSdui.registerEffect(SlideTopToBottomEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            SlideTopToBottomEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(SlideTopToBottomEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
        }

        // Register SlideBottomToTopEffect
        MotionSdui.registerEffect(SlideBottomToTopEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            SlideBottomToTopEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(SlideBottomToTopEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
        }

        // Register ZoomInEffect
        MotionSdui.registerEffect(ZoomInEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val startScale = json.get("startScale")?.asFloat ?: 1f
            val endScale = json.get("endScale")?.asFloat ?: 2f
            ZoomInEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                startScale = startScale,
                endScale = endScale,
            )
        }
        MotionSdui.registerEffectSerializer(ZoomInEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
            json.addProperty("startScale", effect.startScale)
            json.addProperty("endScale", effect.endScale)
        }

        // Register ZoomOutEffect
        MotionSdui.registerEffect(ZoomOutEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val startScale = json.get("startScale")?.asFloat ?: 2f
            val endScale = json.get("endScale")?.asFloat ?: 1f
            ZoomOutEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                startScale = startScale,
                endScale = endScale,
            )
        }
        MotionSdui.registerEffectSerializer(ZoomOutEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
            json.addProperty("startScale", effect.startScale)
            json.addProperty("endScale", effect.endScale)
        }

        // Register FadeInEffect
        MotionSdui.registerEffect(FadeInEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            FadeInEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(FadeInEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
        }

        // Register FadeOutEffect
        MotionSdui.registerEffect(FadeOutEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            FadeOutEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
            )
        }
        MotionSdui.registerEffectSerializer(FadeOutEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
        }

        MotionSdui.registerEffect(BlurEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val maxBlurRadius = json.get("maxBlurRadius")?.asFloat ?: 20f
            BlurEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                fromBlurRadius = 0.1f,
                toBlurRadius = maxBlurRadius,
            )
        }
        MotionSdui.registerEffectSerializer(BlurEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
            json.addProperty("maxBlurRadius", effect.toBlurRadius)
        }

        // Register GlitchEffect
        MotionSdui.registerEffect(GlitchEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val intensity = json.get("intensity")?.asFloat ?: 10f
            GlitchEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                intensity = intensity,
            )
        }
        MotionSdui.registerEffectSerializer(GlitchEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
            json.addProperty("intensity", effect.intensity)
        }

        // Register VibrateEffect
        MotionSdui.registerEffect(VibrateEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val amplitude = json.get("amplitude")?.asFloat ?: 5f
            val frequency = json.get("frequency")?.asFloat ?: 1f
            VibrateEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                amplitude = amplitude,
                frequency = frequency,
            )
        }
        MotionSdui.registerEffectSerializer(VibrateEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
            json.addProperty("amplitude", effect.amplitude)
            json.addProperty("frequency", effect.frequency)
        }

        // Register VintageEffect
        MotionSdui.registerEffect(VintageEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            val fromIntensity = json.get("fromIntensity")?.asFloat ?: 0.0f
            val toIntensity = json.get("toIntensity")?.asFloat ?: 1.0f
            VintageEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                fromIntensity = fromIntensity,
                toIntensity = toIntensity,
            )
        }
        MotionSdui.registerEffectSerializer(VintageEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
            json.addProperty("fromIntensity", effect.fromIntensity)
            json.addProperty("toIntensity", effect.toIntensity)
        }

        // Register SlideEffect
        MotionSdui.registerEffect(SlideEffect::class.java.simpleName) { json ->
            val props = json.parseMotionEffectProps()
            SlideEffect(
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                fromX = json.get("fromX")?.asFloat,
                toX = json.get("toX")?.asFloat,
                fromY = json.get("fromY")?.asFloat,
                toY = json.get("toY")?.asFloat,
            )
        }
        MotionSdui.registerEffectSerializer(SlideEffect::class.java) { effect, json ->
            json.addProperty("type", effect.javaClass.simpleName)
            effect.fromX?.let { json.addProperty("fromX", it) }
            effect.toX?.let { json.addProperty("toX", it) }
            effect.fromY?.let { json.addProperty("fromY", it) }
            effect.toY?.let { json.addProperty("toY", it) }
        }

        // Register CrossFadeTransition
        MotionSdui.registerTransition(CrossFadeTransition::class.java.simpleName) { _ ->
            CrossFadeTransition()
        }
        MotionSdui.registerTransitionSerializer(CrossFadeTransition::class.java) { _, json ->
            json.addProperty("type", CrossFadeTransition::class.java.simpleName)
        }

        // Register BlurTransition
        MotionSdui.registerTransition(BlurTransition::class.java.simpleName) { json ->
            val maxBlurRadius = json.get("maxBlurRadius")?.asFloat ?: 20f
            BlurTransition(maxBlurRadius = maxBlurRadius)
        }
        MotionSdui.registerTransitionSerializer(BlurTransition::class.java) { transition, json ->
            json.addProperty("type", BlurTransition::class.java.simpleName)
            json.addProperty("maxBlurRadius", transition.maxBlurRadius)
        }

        // Register SlideTransition
        MotionSdui.registerTransition(SlideTransition::class.java.simpleName) { json ->
            val directionStr = json.get("direction")?.asString ?: "LEFT_TO_RIGHT"
            val direction =
                try {
                    SlideDirection.valueOf(directionStr)
                } catch (e: Exception) {
                    SlideDirection.LEFT_TO_RIGHT
                }
            SlideTransition(direction = direction)
        }
        MotionSdui.registerTransitionSerializer(SlideTransition::class.java) { transition, json ->
            json.addProperty("type", SlideTransition::class.java.simpleName)
            json.addProperty("direction", transition.direction.name)
        }
    }
}
