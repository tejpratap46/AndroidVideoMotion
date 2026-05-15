package com.tejpratapsingh.lyricsmaker.presentation.templates

import android.graphics.Color
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.core.motion.transitions.CrossFadeTransition
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.accentMiddlePopUpTextView
import com.tejpratapsingh.motionlib.templates.extensions.motionImageView
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate

val AccentLyricsTemplate: MotionTemplate =
    motionTemplate("Accent Lyrics Template") {
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
                    )
                }

                lyrics.zipWithNext().forEach { (current, next) ->
                    accentMiddlePopUpTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        writingSpeed = 1.0f,
                        unwrittenTextAlpha = 0.4f,
                        maxTranslationY = 40f,
                        accentColor = "#FFC107".toColorInt(), // Amber color
                        textSizeVariant = MotionTextVariant.H2,
                        textColor = "#F5F5F5", // Off-white
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(48, 48, 48, 48)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                                setShadowLayer(12f, 0f, 4f, Color.BLACK)
                            },
                    )
                    transition(CrossFadeTransition(), duration = 12)
                }
            }
        }

        preview {
            val image = data.getString("image")
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                val previewLyrics = lyrics.take(4)
                val startFrame = lyrics.first().frame
                val endFrame = previewLyrics.last().frame

                image?.let {
                    motionImageView(
                        startFrame = startFrame,
                        endFrame = endFrame,
                        imageUri = image.toUri(),
                    )
                }

                previewLyrics.zipWithNext().forEach { (current, next) ->
                    accentMiddlePopUpTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        writingSpeed = 1.0f,
                        unwrittenTextAlpha = 0.4f,
                        maxTranslationY = 40f,
                        accentColor = "#FFC107".toColorInt(),
                        textSizeVariant = MotionTextVariant.H2,
                        textColor = "#F5F5F5",
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(48, 48, 48, 48)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                                setShadowLayer(12f, 0f, 4f, Color.BLACK)
                            },
                    )
                    transition(CrossFadeTransition(), duration = 12)
                }
            }
        }
    }
