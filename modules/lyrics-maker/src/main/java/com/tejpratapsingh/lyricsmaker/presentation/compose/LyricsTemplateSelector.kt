package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.presentation.motion.createLyricsVideoPreviewProducer
import com.tejpratapsingh.lyricsmaker.presentation.templates.LyricsTemplateRegistry
import com.tejpratapsingh.motionlib.core.motion.MotionVideoProducer
import com.tejpratapsingh.motionlib.templates.model.MotionTemplate
import com.tejpratapsingh.motionlib.ui.custom.video.MotionVideoPlayerCompose
import com.tejpratapsingh.motionstore.tables.MotionProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LyricsTemplateSelector(
    project: MotionProject,
    onBack: () -> Unit,
    onTemplateSelected: (MotionTemplate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val templates = LyricsTemplateRegistry.templates
    val pagerState = rememberPagerState { templates.size }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        // Full Screen Vertical Pager
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            TemplatePreviewItem(
                project = project,
                template = templates[page],
                isActive = pagerState.currentPage == page,
            )
        }

        // Overlay Header
        Surface(
            color = Color.Transparent,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier =
                        Modifier.background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                        ),
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
                Text(
                    text = "Choose Template",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }

        // Overlay Bottom Action
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(24.dp),
        ) {
            Button(
                onClick = {
                    onTemplateSelected(templates[pagerState.currentPage])
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    "Select Template",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun TemplatePreviewItem(
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
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = 100.dp),
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
