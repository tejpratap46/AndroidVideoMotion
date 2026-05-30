package com.tejpratapsingh.lyricsmaker.presentation.templates

import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideDirection
import com.tejpratapsingh.motionlib.core.motion.transitions.SlideTransition
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.motionImageView
import com.tejpratapsingh.motionlib.templates.extensions.translucentMotionView
import com.tejpratapsingh.motionlib.templates.extensions.wordWriterTextView
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.ui.effects.VibrateEffect
import com.tejpratapsingh.motionlib.ui.effects.VintageEffect

val VintageLyricsTemplate: MotionTemplate =
    motionTemplate("Vintage Lyrics Template") {
        parameters {
            string("songName")
            string("image", defaultValue = null)
        }

        content {
            val image = data.getString("image")
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                val startFrame = lyrics.first().frame
                val endFrame = lyrics.last().frame

                image?.let {
                    motionImageView(
                        startFrame = startFrame,
                        endFrame = endFrame,
                        imageUri = image.toUri(),
                        effects =
                            listOf(
                                VibrateEffect(startFrame, endFrame, amplitude = 10f, frequency = 0.05f),
                                VintageEffect(startFrame, endFrame, fromIntensity = 0.8f, toIntensity = 1.0f),
                            ),
                    )
                }

                translucentMotionView(
                    color = "#000000",
                    alpha = 0.3f,
                    startFrame = startFrame,
                    endFrame = endFrame,
                )

                lyrics.zipWithNext().forEach { (current, next) ->
                    wordWriterTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        writingSpeed = 1.0f,
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(32, 32, 32, 32)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                            },
                    )
                    transition(SlideTransition(SlideDirection.RIGHT_TO_LEFT), duration = 15)
                }
            }
        }

        preview {
            val image = data.getString("image")
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                val previewLyrics = lyrics.take(3)
                val startFrame = previewLyrics.first().frame
                val endFrame = previewLyrics.last().frame

                image?.let {
                    motionImageView(
                        startFrame = startFrame,
                        endFrame = endFrame,
                        imageUri = image.toUri(),
                        effects =
                            listOf(
                                VibrateEffect(startFrame, endFrame, amplitude = 10f, frequency = 0.05f),
                                VintageEffect(startFrame, endFrame, fromIntensity = 0.8f, toIntensity = 1.0f),
                            ),
                    )
                }

                translucentMotionView(
                    color = "#000000",
                    alpha = 0.3f,
                    startFrame = startFrame,
                    endFrame = endFrame,
                )

                previewLyrics.zipWithNext().forEach { (current, next) ->
                    wordWriterTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        writingSpeed = 1.0f,
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(32, 32, 32, 32)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                            },
                    )
                    transition(SlideTransition(SlideDirection.RIGHT_TO_LEFT), duration = 15)
                }
            }
        }
    }
