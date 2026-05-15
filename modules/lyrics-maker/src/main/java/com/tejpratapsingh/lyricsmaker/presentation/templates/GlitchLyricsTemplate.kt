package com.tejpratapsingh.lyricsmaker.presentation.templates

import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.motion.transitions.CrossFadeTransition
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideDirection
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideTransition
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.wordBlinkTextView
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.ui.effects.GlitchEffect

val GlitchLyricsTemplate: MotionTemplate =
    motionTemplate("Glitch Lyrics Template") {
        parameters {
            string("songName")
            string("image", defaultValue = null)
        }

        content {
            val songName = data.getString("songName") ?: ""
            val image = data.getString("image")
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                multiLyricsContainer(
                    songName = songName,
                    startFrame = lyrics.first().frame,
                    endFrame = lyrics.last().frame,
                    image = image,
                )

                lyrics.zipWithNext().forEach { (current, next) ->
                    wordBlinkTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        effects = listOf(GlitchEffect(current.frame, next.frame, intensity = 30f)),
                    ) {
                        textView.apply {
                            setPadding(16, 16, 16, 16)
                            textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                            gravity = Gravity.CENTER
                        }
                    }
                    transition(SlideTransition(SlideDirection.BOTTOM_TO_TOP), duration = 5)
                }
            }
        }

        preview {
            val songName = data.getString("songName") ?: ""
            val image = data.getString("image")
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                val previewLyrics = lyrics.take(5)
                val endFrame = previewLyrics.last().frame
                multiLyricsContainer(
                    songName = songName,
                    startFrame = lyrics.first().frame,
                    endFrame = endFrame,
                    image = image,
                )

                previewLyrics.zipWithNext().forEach { (current, next) ->
                    wordBlinkTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        effects = listOf(GlitchEffect(current.frame, next.frame, intensity = 10f)),
                    ) {
                        textView.apply {
                            setPadding(16, 16, 16, 16)
                            textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                            gravity = Gravity.CENTER
                        }
                    }
                    transition(CrossFadeTransition(), duration = 10)
                }
            }
        }
    }
