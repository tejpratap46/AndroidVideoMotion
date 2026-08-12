package com.tejpratapsingh.lyricsmaker.presentation.templates

import com.tejpratapsingh.motionlib.templates.model.MotionTemplate

object LyricsTemplateRegistry {
    val templates =
        listOf(
            PopupLyricsTemplate,
            TypewriterLyricsTemplate,
            WordwriterLyricsTemplate,
            GlitchLyricsTemplate,
            ZoomLyricsTemplate,
            VibrateLyricsTemplate,
            WordVibrateLyricsTemplate,
            GradientLyricsTemplate,
            RainbowLyricsTemplate,
            AccentLyricsTemplate,
            VintageLyricsTemplate,
            StackedLyricsTemplate,
        )

    fun getTemplate(name: String?): MotionTemplate = templates.find { it.name == name } ?: PopupLyricsTemplate
}
