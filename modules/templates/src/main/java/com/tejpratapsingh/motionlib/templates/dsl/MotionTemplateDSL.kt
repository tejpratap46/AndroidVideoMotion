package com.tejpratapsingh.motionlib.templates.dsl

import android.content.Context
import android.net.Uri
import com.tejpratapsingh.motionlib.assettype.SimpleMotionAsset
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.core.MotionTransition
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.templates.model.ParameterType
import com.tejpratapsingh.motionlib.templates.model.TemplateData
import com.tejpratapsingh.motionlib.templates.model.TemplateParameter
import java.io.File

@DslMarker
annotation class MotionTemplateDsl

@MotionTemplateDsl
class ContentScope(
    val context: Context,
    val producer: MotionVideoProducer,
    val data: TemplateData,
) {
    fun <T> addView(view: T) where T : MotionView, T : android.view.ViewGroup {
        producer.addMotionViewToSequence(view)
    }

    fun transition(
        transition: MotionTransition,
        duration: Int,
    ) {
        producer.addTransition(transition, duration)
    }

    fun layoutInfo(
        width: Int = MotionLayoutInfo.WRAP_CONTENT,
        height: Int = MotionLayoutInfo.WRAP_CONTENT,
        gravity: Int = android.view.Gravity.NO_GRAVITY,
        padding: MotionLayoutInfo.Padding = MotionLayoutInfo.Padding(),
        margin: MotionLayoutInfo.Margin = MotionLayoutInfo.Margin(),
    ): MotionLayoutInfo = MotionLayoutInfo(width, height, padding, margin, gravity)

    fun audio(
        audioUri: Uri,
        startFrame: Int = 0,
        endFrame: Int = -1,
        delayFrame: Int = 0,
    ) {
        producer.addAudio(
            MotionAudio(
                asset = SimpleMotionAsset(audioUri),
                startFrame = startFrame,
                endFrame = endFrame,
                delayFrame = delayFrame,
            ),
        )
    }

    fun audio(
        file: File,
        startFrame: Int = 0,
        endFrame: Int = -1,
        delayFrame: Int = 0,
    ) {
        audio(Uri.fromFile(file), startFrame, endFrame, delayFrame)
    }
}

@MotionTemplateDsl
class EffectBuilder {
    private val effects = mutableListOf<MotionEffect>()

    fun add(effect: MotionEffect) {
        effects.add(effect)
    }

    fun build(): List<MotionEffect> = effects
}

@MotionTemplateDsl
class TemplateParameterBuilder {
    private val parameters = mutableListOf<TemplateParameter<*>>()

    fun string(
        name: String,
        defaultValue: String? = null,
        description: String? = null,
    ) {
        parameters.add(TemplateParameter(name, ParameterType.STRING, defaultValue, description))
    }

    fun int(
        name: String,
        defaultValue: Int? = null,
        description: String? = null,
    ) {
        parameters.add(TemplateParameter(name, ParameterType.INTEGER, defaultValue, description))
    }

    fun float(
        name: String,
        defaultValue: Float? = null,
        description: String? = null,
    ) {
        parameters.add(TemplateParameter(name, ParameterType.FLOAT, defaultValue, description))
    }

    fun color(
        name: String,
        defaultValue: Int? = null,
        description: String? = null,
    ) {
        parameters.add(TemplateParameter(name, ParameterType.COLOR, defaultValue, description))
    }

    fun boolean(
        name: String,
        defaultValue: Boolean? = null,
        description: String? = null,
    ) {
        parameters.add(TemplateParameter(name, ParameterType.BOOLEAN, defaultValue, description))
    }

    fun image(
        name: String,
        defaultValue: String? = null,
        description: String? = null,
    ) {
        parameters.add(TemplateParameter(name, ParameterType.IMAGE, defaultValue, description))
    }

    fun video(
        name: String,
        defaultValue: String? = null,
        description: String? = null,
    ) {
        parameters.add(TemplateParameter(name, ParameterType.VIDEO, defaultValue, description))
    }

    fun build(): List<TemplateParameter<*>> = parameters
}

@MotionTemplateDsl
class MotionTemplateBuilder(
    var name: String = "",
) {
    private var parameterList = listOf<TemplateParameter<*>>()
    private var contentBlock: (ContentScope.() -> Unit)? = null
    private var previewBlock: (ContentScope.() -> Unit)? = null

    fun parameters(block: TemplateParameterBuilder.() -> Unit) {
        parameterList = TemplateParameterBuilder().apply(block).build()
    }

    fun content(block: ContentScope.() -> Unit) {
        contentBlock = block
    }

    fun preview(block: ContentScope.() -> Unit) {
        previewBlock = block
    }

    fun build(): MotionTemplate {
        val finalContentBlock =
            contentBlock
                ?: throw IllegalStateException("Content block must be defined for template $name")
        val finalPreviewBlock = previewBlock
        return object : MotionTemplate(name, parameterList) {
            override fun buildContent(scope: ContentScope) {
                scope.finalContentBlock()
            }

            override fun buildPreview(scope: ContentScope) {
                if (finalPreviewBlock != null) {
                    scope.finalPreviewBlock()
                } else {
                    super.buildPreview(scope)
                }
            }
        }
    }
}

fun motionTemplate(
    name: String,
    block: MotionTemplateBuilder.() -> Unit,
): MotionTemplate = MotionTemplateBuilder(name).apply(block).build()
