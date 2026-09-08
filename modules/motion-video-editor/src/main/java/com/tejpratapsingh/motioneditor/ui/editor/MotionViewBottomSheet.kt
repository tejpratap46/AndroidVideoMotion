package com.tejpratapsingh.motioneditor.ui.editor

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.gson.JsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotionViewBottomSheet(
    viewJson: JsonObject,
    onViewJsonChange: (JsonObject) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        MotionViewPropertyEditor(
            viewJson = viewJson,
            onViewJsonChange = onViewJsonChange,
            onClose = onDismissRequest,
            modifier = Modifier.fillMaxHeight(0.85f),
        )
    }
}
