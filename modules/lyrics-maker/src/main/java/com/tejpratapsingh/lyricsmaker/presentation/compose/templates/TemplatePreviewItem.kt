package com.tejpratapsingh.lyricsmaker.presentation.compose.templates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.presentation.motion.createLyricsVideoPreviewProducer
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.ui.custom.video.MotionVideoPlayerCompose
import com.tejpratapsingh.motionstore.tables.MotionProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun TemplatePreviewItem(
    project: MotionProject,
    template: MotionTemplate,
    isActive: Boolean,
) {
    val context = LocalContext.current

    val motionVideoProducer by
        produceState<MotionVideoProducer?>(initialValue = null, project.id, template.name) {
            value =
                withContext(Dispatchers.Default) {
                    createLyricsVideoPreviewProducer(context, project, template)
                }
        }

    Surface(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(vertical = 112.dp)
                .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            motionVideoProducer?.let {
                MotionVideoPlayerCompose(
                    motionVideoProducer = it,
                    modifier = Modifier.fillMaxSize(),
                    isPlaying = isActive,
                    showControls = false,
                )
            } ?: run {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Template Name Overlay
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp),
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}
