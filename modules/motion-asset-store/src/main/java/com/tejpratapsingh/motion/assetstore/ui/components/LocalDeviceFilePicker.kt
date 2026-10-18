package com.tejpratapsingh.motion.assetstore.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.motion.assetstore.model.AssetFileType

@Composable
fun LocalDeviceFilePicker(
    fileType: AssetFileType,
    onFileSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visualMediaPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            uri?.let { onFileSelected(it.toString()) }
        }

    val contentPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let { onFileSelected(it.toString()) }
        }

    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = "Select File",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (fileType == AssetFileType.IMAGE) "Select Image from Device" else "Select Video from Device",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val mediaType =
                    if (fileType == AssetFileType.IMAGE) {
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    } else {
                        ActivityResultContracts.PickVisualMedia.VideoOnly
                    }
                visualMediaPicker.launch(PickVisualMediaRequest(mediaType))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Open Gallery Picker")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                val mimeType = if (fileType == AssetFileType.IMAGE) "image/*" else "video/*"
                contentPicker.launch(mimeType)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Browse Device Files")
        }
    }
}
