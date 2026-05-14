package com.tejpratapsingh.motion.sdui.infra

import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.google.gson.JsonArray
import com.tejpratapsingh.motionlib.ui.custom.audio.CircularAudioWaveformView
import com.tejpratapsingh.motionlib.ui.custom.audio.RadialAudioWaveformView
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation
import com.tejpratapsingh.motionlib.ui.custom.image.CircularMotionImageView
import com.tejpratapsingh.motionlib.ui.custom.image.MotionImageView
import com.tejpratapsingh.motionlib.ui.custom.text.PopUpTextView
import com.tejpratapsingh.motionlib.ui.custom.text.TransparentTextView
import com.tejpratapsingh.motionlib.ui.custom.text.TypeWriterTextView
import com.tejpratapsingh.motionlib.ui.custom.text.WordBlinkTextView
import com.tejpratapsingh.motionlib.ui.custom.text.WordWriterTextView
import com.tejpratapsingh.motionlib.ui.custom.video.VideoFrameView
import com.tejpratapsingh.motionlib.ui.effects.SlideRightToLeftEffect

/**
 * Initializer for [MotionSdui] registry.
 * Registers common [MotionView] and [MotionEffect] types.
 */
object MotionSduiInitializer {
    fun initialize() {
        // Register TransparentTextView
        MotionSdui.registerView(TransparentTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val text = json.get("text")?.asString ?: ""
            TransparentTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(TransparentTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
        }

        // Register TypeWriterTextView
        MotionSdui.registerView(TypeWriterTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            val unwrittenTextAlpha = json.get("unwrittenTextAlpha")?.asFloat ?: 0f
            TypeWriterTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                unwrittenTextAlpha = unwrittenTextAlpha,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(TypeWriterTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            json.addProperty("writingSpeed", view.writingSpeed)
            json.addProperty("unwrittenTextAlpha", view.unwrittenTextAlpha)
        }

        // Register WordWriterTextView
        MotionSdui.registerView(WordWriterTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            val unwrittenTextAlpha = json.get("unwrittenTextAlpha")?.asFloat ?: 0f
            WordWriterTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                unwrittenTextAlpha = unwrittenTextAlpha,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(WordWriterTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            json.addProperty("writingSpeed", view.writingSpeed)
            json.addProperty("unwrittenTextAlpha", view.unwrittenTextAlpha)
        }

        // Register WordBlinkTextView
        MotionSdui.registerView(WordBlinkTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            WordBlinkTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(WordBlinkTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            json.addProperty("writingSpeed", view.writingSpeed)
        }

        // Register PopUpTextView
        MotionSdui.registerView(PopUpTextView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val text = json.get("text")?.asString ?: ""
            val writingSpeed = json.get("writingSpeed")?.asFloat ?: 0f
            val unwrittenTextAlpha = json.get("unwrittenTextAlpha")?.asFloat ?: 0f
            val maxTranslationY = json.get("maxTranslationY")?.asFloat ?: 50f
            PopUpTextView(
                context = context,
                text = text,
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                writingSpeed = writingSpeed,
                unwrittenTextAlpha = unwrittenTextAlpha,
                maxTranslationY = maxTranslationY,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(PopUpTextView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("text", view.text)
            json.addProperty("writingSpeed", view.writingSpeed)
            json.addProperty("unwrittenTextAlpha", view.unwrittenTextAlpha)
            json.addProperty("maxTranslationY", view.maxTranslationY)
        }

        // Register CircularMotionImageView
        MotionSdui.registerView(CircularMotionImageView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val imageUriStr =
                json.get("imageUri")?.asString
                    ?: throw IllegalArgumentException("imageUri required for CircularMotionImageView")
            CircularMotionImageView(
                context = context,
                imageUri = imageUriStr.toUri(),
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(CircularMotionImageView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("imageUri", view.imageUri.toString())
        }

        // Register MotionImageView
        MotionSdui.registerView(MotionImageView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val imageUriStr =
                json.get("imageUri")?.asString
                    ?: throw IllegalArgumentException("imageUri required for MotionImageView")
            MotionImageView(
                context = context,
                imageUri = imageUriStr.toUri(),
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(MotionImageView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("imageUri", view.imageUri.toString())
        }

        // Register VideoFrameView
        MotionSdui.registerView(VideoFrameView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
            val videoUriStr = json.get("videoUri")?.asString ?: throw IllegalArgumentException("videoUri required for VideoFrameView")
            VideoFrameView(
                context = context,
                videoUri = videoUriStr.toUri(),
                startFrame = props.startFrame,
                endFrame = props.endFrame,
                effects = props.effects,
            )
        }
        MotionSdui.registerViewSerializer(VideoFrameView::class.java) { view, json ->
            json.addProperty("type", view.javaClass.simpleName)
            json.addProperty("videoUri", view.videoUri.toString())
        }

        // Register GradientView
        MotionSdui.registerView(GradientView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
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

        // Register CircularAudioWaveformView
        MotionSdui.registerView(CircularAudioWaveformView::class.java.simpleName) { context, json ->
            val props = json.parseMotionViewProps()
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
            val props = json.parseMotionViewProps()
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
    }
}
