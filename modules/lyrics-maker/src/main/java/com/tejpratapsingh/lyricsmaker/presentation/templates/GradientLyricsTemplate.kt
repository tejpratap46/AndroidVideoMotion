package com.tejpratapsingh.lyricsmaker.presentation.templates

import android.graphics.Color
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.motion.transitions.CrossFadeTransition
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.gradientView
import com.tejpratapsingh.motionlib.templates.extensions.popUpTextView
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.ui.custom.background.Orientation

val GradientLyricsTemplate: MotionTemplate =
    motionTemplate("Gradient Lyrics Template") {
        parameters {
            string("songName")
            string("image", defaultValue = null)
        }

        content {
            val songName = data.getString("songName") ?: ""
            val image = data.getString("image")
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                val startFrame = lyrics.first().frame
                val endFrame = lyrics.last().frame

                gradientView(
                    startFrame = startFrame,
                    endFrame = endFrame,
                    orientation = Orientation.CIRCULAR,
                    colors =
                        intArrayOf(
                            "#2568ff".toColorInt(),
                            "#7048ff".toColorInt(),
                            "#ba28ff".toColorInt(),
                        ),
                )

                lyrics.zipWithNext().forEach { (current, next) ->
                    popUpTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(32, 32, 32, 32)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                                setShadowLayer(10f, 0f, 0f, Color.BLACK)
                            },
                    )
                    transition(CrossFadeTransition(), duration = 15)
                }
            }
        }

        preview {
            val songName = data.getString("songName") ?: ""
            val image = data.getString("image")
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                val previewLyrics = lyrics.take(3)
                val startFrame = lyrics.first().frame
                val endFrame = previewLyrics.last().frame

                gradientView(
                    startFrame = startFrame,
                    endFrame = endFrame,
                    orientation = Orientation.CIRCULAR,
                    colors =
                        intArrayOf(
                            "#2568ff".toColorInt(),
                            "#7048ff".toColorInt(),
                            "#ba28ff".toColorInt(),
                        ),
                )

                previewLyrics.zipWithNext().forEach { (current, next) ->
                    popUpTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(32, 32, 32, 32)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                                setShadowLayer(10f, 0f, 0f, Color.BLACK)
                            },
                    )
                    transition(CrossFadeTransition(), duration = 15)
                }
            }
        }
    }
