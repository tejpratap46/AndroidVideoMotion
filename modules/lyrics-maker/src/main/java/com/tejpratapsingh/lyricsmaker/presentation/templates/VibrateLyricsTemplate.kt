package com.tejpratapsingh.lyricsmaker.presentation.templates

import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideDirection
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideTransition
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.wordWriterTextView
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.ui.effects.VibrateEffect

val VibrateLyricsTemplate: MotionTemplate =
    motionTemplate("Vibrate Lyrics Template") {
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
                    wordWriterTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H2,
                        textColor = "#FFFF00",
                        effects = listOf(VibrateEffect(current.frame, next.frame, amplitude = 10f, frequency = 0.5f)),
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(16, 16, 16, 16)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                            },
                    )
                    transition(SlideTransition(SlideDirection.TOP_TO_BOTTOM), duration = 8)
                }
            }
        }

        preview {
            val songName = data.getString("songName") ?: ""
            val image = data.getString("image")
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                val previewLyrics = lyrics.take(3)
                val endFrame = previewLyrics.last().frame
                multiLyricsContainer(
                    songName = songName,
                    startFrame = lyrics.first().frame,
                    endFrame = endFrame,
                    image = image,
                )

                previewLyrics.zipWithNext().forEach { (current, next) ->
                    wordWriterTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H2,
                        textColor = "#FFFF00",
                        effects = listOf(VibrateEffect(current.frame, next.frame, amplitude = 10f, frequency = 0.5f)),
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(16, 16, 16, 16)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                            },
                    )
                    transition(SlideTransition(SlideDirection.TOP_TO_BOTTOM), duration = 8)
                }
            }
        }
    }
