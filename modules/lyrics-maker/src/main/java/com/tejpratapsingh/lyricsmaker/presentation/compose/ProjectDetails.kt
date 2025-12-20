package com.tejpratapsingh.lyricsmaker.presentation.compose

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.GsonBuilder
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.motion.imageloader.BuildConfig
import com.tejpratapsingh.motion.imageloader.ImageLoader
import com.tejpratapsingh.motion.ongoing.domain.CurrentProject
import com.tejpratapsingh.motionlib.core.MotionConfig
import kotlin.let
import kotlin.text.ifEmpty
@Composable
fun ProjectDetails(
    lyricsViewModel: LyricsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val project = lyricsViewModel.currentSelectedProject!!
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    LaunchedEffect(project.url) {
        project.image?.let {
            bitmap = ImageLoader.loadBitmap(context, it)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {

        // 🔹 HEADER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.20f)
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = project.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = project.trackName ?: "",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = project.title ?: "",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        //  LYRICS
        itemsIndexed(project.selectedLyrics?.split(",") ?: emptyList()) { _, lyric ->
            LyricRow(lyric)
        }
    }
}
@Composable
fun LyricRow(lyric: String) {
    Log.d("lyric","$lyric /end")
    val frame = lyric.substringAfter(":")[0].code
    val text = lyric.substringAfter(":")[1].toString()
    val line = SyncedLyricFrame(frame = frame, text)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "[${line.frame}]",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(64.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = line.text.ifEmpty { "…" },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            "[${line.frame / MotionConfig.fps} sec]",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(64.dp)
        )
    }
}
