package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.ThemeBlue
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.ThemePink

@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 3000),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "shimmerOffset",
    )

    val gradientColors =
        listOf(
            ThemeBlue,
            ThemePink,
        )

    Text(
        text = text,
        style =
            MaterialTheme.typography.displayMedium.copy(
                brush =
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(offset, offset),
                        end = Offset(offset + 5f, offset + 5f),
                    ),
            ),
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier,
    )
}
