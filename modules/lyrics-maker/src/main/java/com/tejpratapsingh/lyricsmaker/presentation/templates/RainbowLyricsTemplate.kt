package com.tejpratapsingh.lyricsmaker.presentation.templates

import android.graphics.Color
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.motion.transitions.CrossFadeTransition
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.accentMiddlePopUpTextView
import com.tejpratapsingh.motionlib.templates.extensions.motionImageView
import com.tejpratapsingh.motionlib.templates.extensions.rainbowPopUpTextView
import com.tejpratapsingh.motionlib.templates.extensions.translucentMotionView
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate

val RainbowLyricsTemplate: MotionTemplate =
    motionTemplate("Rainbow Lyrics Template") {
        parameters {
            string("songName")
            string("image", defaultValue = null)
        }

        content {
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

                lyrics.zipWithNext().forEachIndexed { index, (current, next) ->
                    if (index % 2 == 0) {
                        rainbowPopUpTextView(
                            text = current.text,
                            startFrame = current.frame,
                            endFrame = next.frame,
                            textSizeVariant = MotionTextVariant.H1,
                            writingSpeed = 1.5f,
                            textView =
                                AppCompatTextView(context).apply {
                                    setPadding(32, 32, 32, 32)
                                    textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                    gravity = Gravity.CENTER
                                    setShadowLayer(10f, 0f, 0f, Color.BLACK)
                                },
                        )
                    } else {
                        accentMiddlePopUpTextView(
                            text = current.text,
                            startFrame = current.frame,
                            endFrame = next.frame,
                            textSizeVariant = MotionTextVariant.H1,
                            accentColor = Color.CYAN,
                            writingSpeed = 1.5f,
                            textView =
                                AppCompatTextView(context).apply {
                                    setPadding(32, 32, 32, 32)
                                    textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                    gravity = Gravity.CENTER
                                    setShadowLayer(10f, 0f, 0f, Color.BLACK)
                                },
                        )
                    }
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

                previewLyrics.zipWithNext().forEachIndexed { index, (current, next) ->
                    if (index % 2 == 0) {
                        rainbowPopUpTextView(
                            text = current.text,
                            startFrame = current.frame,
                            endFrame = next.frame,
                            textSizeVariant = MotionTextVariant.H1,
                            writingSpeed = 1.5f,
                            textView =
                                AppCompatTextView(context).apply {
                                    setPadding(32, 32, 32, 32)
                                    textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                    gravity = Gravity.CENTER
                                    setShadowLayer(10f, 0f, 0f, Color.BLACK)
                                },
                        )
                    } else {
                        accentMiddlePopUpTextView(
                            text = current.text,
                            startFrame = current.frame,
                            endFrame = next.frame,
                            textSizeVariant = MotionTextVariant.H1,
                            writingSpeed = 1.5f,
                            accentColor = Color.CYAN,
                            textView =
                                AppCompatTextView(context).apply {
                                    setPadding(32, 32, 32, 32)
                                    textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                    gravity = Gravity.CENTER
                                    setShadowLayer(10f, 0f, 0f, Color.BLACK)
                                },
                        )
                    }
                    transition(CrossFadeTransition(), duration = 15)
                }
            }
        }
    }
