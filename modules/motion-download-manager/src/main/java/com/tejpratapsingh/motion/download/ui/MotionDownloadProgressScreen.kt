package com.tejpratapsingh.motion.download.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.motion.download.model.AssetDownloadProgress

@Composable
fun MotionDownloadProgressScreen(
    viewModel: MotionDownloadViewModel,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    MotionDownloadProgressContent(
        uiState = uiState,
        onRetryAsset = { viewModel.retryAsset(it.id) },
        onRetryAll = { /* Handled by caller or viewModel.startDownload with original JSON */ },
        onNext = { if (uiState is MotionDownloadUiState.Success) onNext() },
        modifier = modifier,
    )
}

@Composable
fun MotionDownloadProgressContent(
    uiState: MotionDownloadUiState,
    onRetryAsset: (AssetDownloadProgress) -> Unit,
    onRetryAll: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeaderSection(uiState, onRetryAll)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            val assetList =
                when (uiState) {
                    is MotionDownloadUiState.Downloading -> uiState.assetProgressList
                    is MotionDownloadUiState.Success -> uiState.assetProgressList
                    is MotionDownloadUiState.Error -> uiState.assetProgressList
                    else -> emptyList()
                }

            if (assetList.isNotEmpty()) {
                Text(
                    "Asset Status",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(assetList, key = { it.url }) { asset ->
                        AssetDownloadItem(asset, onRetry = { onRetryAsset(asset) })
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text =
                                if (uiState is MotionDownloadUiState.Idle) {
                                    "Ready to download assets"
                                } else {
                                    "0 assets found to download"
                                },
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNext,
                enabled = uiState is MotionDownloadUiState.Success,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun HeaderSection(
    uiState: MotionDownloadUiState,
    onRetry: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (uiState) {
            is MotionDownloadUiState.Downloading -> {
                Text(
                    "Downloading Assets",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "File ${uiState.downloadedFiles} of ${uiState.totalFiles}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { uiState.progress / 100f },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${uiState.progress}%",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            is MotionDownloadUiState.Success -> {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF4CAF50),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Download Complete!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            is MotionDownloadUiState.Error -> {
                Icon(
                    Icons.Rounded.Error,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Download Failed",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    uiState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Retry All")
                }
            }
            else -> {}
        }
    }
}

@Composable
fun AssetDownloadItem(asset: AssetDownloadProgress, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector =
                        when (asset.status) {
                            "SUCCESS" -> Icons.Rounded.CheckCircle
                            "FAILED" -> Icons.Rounded.Error
                            else -> Icons.Rounded.CloudDownload
                        },
                    contentDescription = null,
                    tint =
                        when (asset.status) {
                            "SUCCESS" -> Color(0xFF4CAF50)
                            "FAILED" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.fileName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = asset.url,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (asset.status == "FAILED") {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Retry",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "${asset.progress}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (asset.status == "PROGRESS" || asset.status == "QUEUED" || asset.status == "STARTED") {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { asset.progress / 100f },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                )
            }
            if (asset.error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = asset.error,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDownloading() {
    val sampleAssets =
        listOf(
            AssetDownloadProgress(1, "https://test.com/v1.mp4", "v1.mp4", 100, "SUCCESS"),
            AssetDownloadProgress(2, "https://test.com/v2.mp4", "v2.mp4", 45, "PROGRESS"),
            AssetDownloadProgress(3, "https://test.com/v3.mp4", "v3.mp4", 0, "QUEUED"),
            AssetDownloadProgress(4, "https://test.com/v4.mp4", "v4.mp4", 0, "FAILED", "Connection Timeout"),
        )
    MaterialTheme {
        MotionDownloadProgressContent(
            uiState = MotionDownloadUiState.Downloading(4, 1, 36, sampleAssets),
            onRetryAsset = {},
            onRetryAll = {},
            onNext = {},
        )
    }
}

@Preview
@Composable
fun PreviewSuccess() {
    val sampleAssets =
        listOf(
            AssetDownloadProgress(1, "https://test.com/v1.mp4", "v1.mp4", 100, "SUCCESS"),
            AssetDownloadProgress(2, "https://test.com/v2.mp4", "v2.mp4", 100, "SUCCESS"),
        )
    MaterialTheme {
        MotionDownloadProgressContent(
            uiState = MotionDownloadUiState.Success(sampleAssets),
            onRetryAsset = {},
            onRetryAll = {},
            onNext = {},
        )
    }
}
