package com.tejpratapsingh.motionlib.templates.json

import android.view.ViewGroup
import com.google.gson.JsonObject
import com.tejpratapsingh.motion.sdui.infra.toMotionView
import com.tejpratapsingh.motionlib.templates.dsl.ContentScope
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.templates.model.TemplateParameter
import com.tejpratapsingh.motionlib.templates.serialization.TemplateSerialization

class JsonMotionTemplate(
    name: String,
    parameters: List<TemplateParameter<*>>,
    val rawContent: JsonObject
) : MotionTemplate(name, parameters) {

    override fun buildContent(scope: ContentScope) {
        // 1. Apply data to content JSON
        val appliedJson = TemplateSerialization.applyData(rawContent, scope.data).asJsonObject
        
        // 2. Content can be a single view or a container with children
        // For simplicity, let's assume it's a list of views under "views" key or a single view
        if (appliedJson.has("views")) {
            val viewsArray = appliedJson.getAsJsonArray("views")
            viewsArray.forEach { viewJson ->
                val motionView = viewJson.asJsonObject.toMotionView(scope.context)
                if (motionView is ViewGroup) {
                    scope.addView(motionView)
                }
            }
        } else {
            val motionView = appliedJson.toMotionView(scope.context)
            if (motionView is ViewGroup) {
                scope.addView(motionView)
            }
        }
    }
}
