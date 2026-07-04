package com.tejpratapsingh.lyricsmaker.presentation.compose.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.provideCurrentConfig

@Composable
@Suppress("FunctionName")
internal fun LyricRow(
    line: SyncedLyricFrame,
    isSelected: Boolean,
    backgroundColor: Color,
    onLongClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(MaterialTheme.shapes.medium)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick,
                ).background(backgroundColor)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("[${line.frame}]", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(64.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = line.text.ifEmpty { "…" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "[${line.frame / provideCurrentConfig().fps} sec]",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(64.dp),
        )
    }
}
