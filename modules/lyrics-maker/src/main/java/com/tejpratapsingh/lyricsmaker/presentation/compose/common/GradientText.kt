package com.tejpratapsingh.lyricsmaker.presentation.compose.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.ThemeBlue
import com.tejpratapsingh.lyricsmaker.presentation.ui.theme.ThemePink

@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val malamPoek =
        remember(context) {
            FontFamily(
                Font(path = "fonts/Malam_Poek.ttf", assetManager = context.assets),
            )
        }

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
        remember {
            listOf(
                ThemePink,
                ThemeBlue,
            )
        }

    Text(
        text = text,
        style =
            MaterialTheme.typography.displayMedium.copy(
                fontFamily = malamPoek,
                fontSize = 48.sp,
            ),
        fontWeight = FontWeight.ExtraBold,
        modifier =
            modifier
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithCache {
                    val brush =
                        Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(offset, offset),
                            end = Offset(offset + 5f, offset + 5f),
                        )
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush, blendMode = BlendMode.SrcAtop)
                    }
                },
    )
}
