package com.tejpratapsingh.motion.assetstore.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.motion.assetstore.model.AssetFileType
import com.tejpratapsingh.motion.assetstore.ui.components.LocalDeviceFilePicker
import com.tejpratapsingh.motion.assetstore.ui.components.PexelsAssetGrid
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetPickerBottomSheet(
    fileType: AssetFileType,
    onAssetSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AssetPickerViewModel = koinViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentFileType by viewModel.fileType.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val imagesState by viewModel.imagesState.collectAsState()
    val videosState by viewModel.videosState.collectAsState()
    val downloadingProgress by viewModel.downloadingProgress.collectAsState()

    LaunchedEffect(fileType) {
        viewModel.initForFileType(fileType)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.fillMaxHeight(0.9f),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (currentFileType == AssetFileType.IMAGE) "Choose Image Asset" else "Choose Video Asset",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tabs: Device vs Pexels
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text("Local Device") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("Pexels / Online") },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                LocalDeviceFilePicker(
                    fileType = currentFileType,
                    onFileSelected = { uri ->
                        onAssetSelected(uri)
                        onDismiss()
                    },
                )
            } else {
                // Online search section
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    placeholder = { Text("Search Pexels assets...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (currentFileType == AssetFileType.IMAGE) {
                    PexelsAssetGrid(
                        uiState = imagesState,
                        downloadProgress = downloadingProgress,
                        onAssetSelected = { item ->
                            viewModel.downloadAndSelectAsset(item) { selectedUrl ->
                                onAssetSelected(selectedUrl)
                                onDismiss()
                            }
                        },
                        onLoadMore = { viewModel.loadNextPage() },
                    )
                } else {
                    PexelsAssetGrid(
                        uiState = videosState,
                        downloadProgress = downloadingProgress,
                        onAssetSelected = { item ->
                            viewModel.downloadAndSelectAsset(item) { selectedUrl ->
                                onAssetSelected(selectedUrl)
                                onDismiss()
                            }
                        },
                        onLoadMore = { viewModel.loadNextPage() },
                    )
                }
            }
        }
    }
}
