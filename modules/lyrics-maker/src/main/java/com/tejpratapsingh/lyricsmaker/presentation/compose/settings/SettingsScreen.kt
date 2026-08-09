package com.tejpratapsingh.lyricsmaker.presentation.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.R
import com.tejpratapsingh.lyricsmaker.presentation.compose.common.GradientText

@Composable
fun SettingsScreen(
    onManageCacheClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GradientText(text = stringResource(R.string.settings))
                Spacer(modifier = Modifier.height(8.dp))
            }

            ListItem(
                headlineContent = { Text("Manage Cache") },
                supportingContent = { Text("View and delete downloaded assets") },
                leadingContent = {
                    Icon(
                        Icons.Rounded.Storage,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable { onManageCacheClick() },
            )
            HorizontalDivider()
        }
    }
}
