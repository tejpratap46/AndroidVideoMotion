package com.tejpratapsingh.lyricsmaker.presentation.templates

import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.motion.transitions.CrossFadeTransition
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.motionImageView
import com.tejpratapsingh.motionlib.templates.extensions.popUpTextView
import com.tejpratapsingh.motionlib.templates.extensions.translucentMotionView
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.ui.effects.FadeInEffect
import com.tejpratapsingh.motionlib.ui.effects.ZoomInEffect

val ZoomLyricsTemplate: MotionTemplate =
    motionTemplate("Zoom Lyrics Template") {
        parameters {
            string("songName")
            string("image", defaultValue = null)
        }

        content {
            val songName = data.getString("songName") ?: ""
            val image = data.getString("image")
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                image?.let {
                    motionImageView(
                        startFrame = lyrics.first().frame,
                        endFrame = lyrics.last().frame,
                        imageUri = image.toUri(),
                    )
                }

                translucentMotionView(
                    color = "#000000",
                    alpha = 0.4f,
                    startFrame = lyrics.first().frame,
                    endFrame = lyrics.last().frame,
                )

                lyrics.zipWithNext().forEach { (current, next) ->
                    popUpTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        writingSpeed = 1.5f,
                        effects =
                            listOf(
                                ZoomInEffect(current.frame, next.frame, startScale = 0.8f, endScale = 1.5f),
                                FadeInEffect(current.frame, current.frame + 15),
                            ),
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(16, 16, 16, 16)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
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
                image?.let {
                    motionImageView(
                        startFrame = previewLyrics.first().frame,
                        endFrame = previewLyrics.last().frame,
                        imageUri = image.toUri(),
                    )
                }

                translucentMotionView(
                    color = "#000000",
                    alpha = 0.4f,
                    startFrame = previewLyrics.first().frame,
                    endFrame = previewLyrics.last().frame,
                )

                previewLyrics.zipWithNext().forEach { (current, next) ->
                    popUpTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        writingSpeed = 1.5f,
                        effects =
                            listOf(
                                ZoomInEffect(current.frame, next.frame, startScale = 0.8f, endScale = 1.5f),
                                FadeInEffect(current.frame, current.frame + 15),
                            ),
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(16, 16, 16, 16)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                            },
                    )
                    transition(CrossFadeTransition(), duration = 15)
                }
            }
        }
    }
