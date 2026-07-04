package com.tejpratapsingh.motionlib.templates.model

import com.tejpratapsingh.motionlib.templates.dsl.ContentScope

abstract class MotionTemplate(
    val name: String,
    val parameters: List<TemplateParameter<*>>,
) {
    abstract fun buildContent(scope: ContentScope)

    open fun buildPreview(scope: ContentScope) {
        buildContent(scope)
    }
}
