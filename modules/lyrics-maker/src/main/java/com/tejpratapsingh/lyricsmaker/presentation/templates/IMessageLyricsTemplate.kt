package com.tejpratapsingh.lyricsmaker.presentation.templates

import androidx.core.net.toUri
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.motionImageView
import com.tejpratapsingh.motionlib.templates.extensions.translucentMotionView
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.ui.effects.BlurEffect

val IMessageLyricsTemplate: MotionTemplate =
    motionTemplate("iMessage Lyrics Template") {
        parameters {
            string("songName")
            string("image", defaultValue = null)
            string("bubbleColor", defaultValue = "#007AFF")
            boolean("isAlternating", defaultValue = false)
        }

        content {
            val songName = data.getString("songName") ?: ""
            val image = data.getString("image")
            val bubbleColor = data.getString("bubbleColor") ?: "#007AFF"
            val isAlternating = data.getBoolean("isAlternating") ?: false
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

                translucentMotionView(
                    color = "#000000",
                    alpha = 0.6f,
                    startFrame = startFrame,
                    endFrame = endFrame,
                    effects =
                        listOf(
                            BlurEffect(
                                startFrame,
                                endFrame,
                            ),
                        ),
                )

                iMessageChatMotionView(
                    lyrics = lyrics,
                    startFrame = startFrame,
                    endFrame = endFrame,
                    bubbleColor = bubbleColor,
                    textColor = "#FFFFFF",
                    incomingBubbleColor = "#26252A",
                    incomingTextColor = "#FFFFFF",
                    writingSpeed = 1.2f,
                    unwrittenTextAlpha = 0.0f,
                    isAlternating = isAlternating,
                    textSizeVariant = MotionTextVariant.H5,
                )
            }
        }

        preview {
            val songName = data.getString("songName") ?: ""
            val image = data.getString("image")
            val bubbleColor = data.getString("bubbleColor") ?: "#007AFF"
            val isAlternating = data.getBoolean("isAlternating") ?: false
            val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

            if (lyrics.isNotEmpty()) {
                val previewLyrics = lyrics.take(5)
                val startFrame = previewLyrics.first().frame
                val endFrame = previewLyrics.last().frame

                image?.let {
                    motionImageView(
                        startFrame = startFrame,
                        endFrame = endFrame,
                        imageUri = image.toUri(),
                    )
                }

                translucentMotionView(
                    color = "#000000",
                    alpha = 0.6f,
                    startFrame = startFrame,
                    endFrame = endFrame,
                    effects =
                        listOf(
                            BlurEffect(
                                startFrame,
                                endFrame,
                            ),
                        ),
                )

                iMessageChatMotionView(
                    lyrics = previewLyrics,
                    startFrame = startFrame,
                    endFrame = endFrame,
                    bubbleColor = bubbleColor,
                    textColor = "#FFFFFF",
                    incomingBubbleColor = "#26252A",
                    incomingTextColor = "#FFFFFF",
                    writingSpeed = 1.2f,
                    unwrittenTextAlpha = 0.0f,
                    isAlternating = isAlternating,
                    textSizeVariant = MotionTextVariant.H5,
                )
            }
        }
    }
