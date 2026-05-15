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
                val startFrame = lyrics.first().frame
                val endFrame = lyrics.last().frame

                image?.let {
                    motionImageView(
                        startFrame = startFrame,
                        endFrame = endFrame,
                        imageUri = image.toUri(),
                    )
                }

                lyrics.zipWithNext().forEachIndexed { index, (current, next) ->
                    if (index % 2 == 0) {
                        rainbowPopUpTextView(
                            text = current.text,
                            startFrame = current.frame,
                            endFrame = next.frame,
                            textSizeVariant = MotionTextVariant.H1,
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
