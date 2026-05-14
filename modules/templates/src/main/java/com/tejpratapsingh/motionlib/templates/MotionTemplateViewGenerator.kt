package com.tejpratapsingh.motionlib.templates

import android.content.Context
import com.tejpratapsingh.motion.sdui.infra.toMotionView
import com.tejpratapsingh.motionlib.core.MotionView

/**
 * Generates [MotionView] from [MotionTemplate] and dynamic data.
 */
object MotionTemplateViewGenerator {

    /**
     * Generates a [MotionView] instance.
     * @param context Android context.
     * @param template The template to use.
     * @param data Dynamic data to apply to the template.
     * @return A [MotionView] created from the SDUI definition.
     */
    fun generate(context: Context, template: MotionTemplate, data: Map<String, Any>): MotionView {
        val appliedJson = MotionTemplateApplier.apply(template, data)
        return appliedJson.toMotionView(context)
    }
}
