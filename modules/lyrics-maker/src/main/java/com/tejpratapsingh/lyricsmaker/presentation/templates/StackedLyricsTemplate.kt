package com.tejpratapsingh.lyricsmaker.presentation.templates

import androidx.core.net.toUri
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.view.SyncedLyricsMotionTextView
import com.tejpratapsingh.motionlib.assettype.SimpleMotionAsset
import com.tejpratapsingh.motionlib.core.MotionTextVariant
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.verticalStackMotionView
import com.tejpratapsingh.motionlib.ui.custom.image.MotionImageView
import com.tejpratapsingh.motionlib.ui.custom.progress.MotionProgressBar
import com.tejpratapsingh.motionlib.ui.custom.stack.StackSection
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate

val StackedLyricsTemplate: MotionTemplate = motionTemplate("Stacked Lyrics Template") {
    parameters {
        image("image")
        string("lyrics") // Assuming lyrics are passed as a string or handled elsewhere
    }

    content {
        val image = data.getString("image")
        val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

        if (lyrics.isNotEmpty()) {
            val startFrame = 0 // Always start from 0 for the main container
            val endFrame = lyrics.last().frame

            val sections = mutableListOf<StackSection>()

            if (!image.isNullOrBlank()) {
                sections.add(
                    StackSection(
                        view = MotionImageView(context, SimpleMotionAsset(image.toUri()), startFrame, endFrame),
                        percentage = 50f
                    )
                )
            }

            sections.add(
                StackSection(
                    view = SyncedLyricsMotionTextView(
                        context = context,
                        lyrics = lyrics,
                        startFrame = startFrame,
                        endFrame = endFrame,
                        textSizeVariant = MotionTextVariant.H6,
                        textColor = "#FFFFFF"
                    ),
                    percentage = if (!image.isNullOrBlank()) 30f else 80f
                )
            )

            sections.add(
                StackSection(
                    view = MotionProgressBar(context, startFrame, endFrame),
                    percentage = 20f
                )
            )

            verticalStackMotionView(
                startFrame = startFrame,
                endFrame = endFrame,
                sections = sections
            )
        }
    }

    preview {
        val image = data.getString("image")
        val lyrics = data.get<List<SyncedLyricFrame>>("lyrics") ?: emptyList()

        if (lyrics.isNotEmpty()) {
            val previewLyrics = lyrics.take(10)
            val startFrame = 0 // Always start from 0 for the main container
            val endFrame = previewLyrics.last().frame

            val sections = mutableListOf<StackSection>()

            if (!image.isNullOrBlank()) {
                sections.add(
                    StackSection(
                        view = MotionImageView(context, SimpleMotionAsset(image.toUri()), startFrame, endFrame),
                        percentage = 50f
                    )
                )
            }

            sections.add(
                StackSection(
                    view = SyncedLyricsMotionTextView(
                        context = context,
                        lyrics = previewLyrics,
                        startFrame = startFrame,
                        endFrame = endFrame,
                        textSizeVariant = MotionTextVariant.H6,
                        textColor = "#FFFFFF"
                    ),
                    percentage = if (!image.isNullOrBlank()) 30f else 80f
                )
            )

            sections.add(
                StackSection(
                    view = MotionProgressBar(context, startFrame, endFrame),
                    percentage = 20f
                )
            )

            verticalStackMotionView(
                startFrame = startFrame,
                endFrame = endFrame,
                sections = sections
            )
        }
    }
}
