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

val WordwriterLyricsTemplate: MotionTemplate =
    motionTemplate("Wordwriter Lyrics Template") {
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
                    wordWriterTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        writingSpeed = 1.5f,
                        unwrittenTextAlpha = 0.3f,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(16, 16, 16, 16)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                            },
                    )
                    transition(SlideTransition(SlideDirection.RIGHT_TO_LEFT), duration = 10)
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
                    wordWriterTextView(
                        text = current.text,
                        startFrame = current.frame,
                        endFrame = next.frame,
                        writingSpeed = 1.5f,
                        unwrittenTextAlpha = 0.3f,
                        textSizeVariant = MotionTextVariant.H1,
                        textColor = "#FFFFFF",
                        textView =
                            AppCompatTextView(context).apply {
                                setPadding(16, 16, 16, 16)
                                textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
                                gravity = Gravity.CENTER
                            },
                    )
                    transition(SlideTransition(SlideDirection.LEFT_TO_RIGHT), duration = 10)
                }
            }
        }
    }
