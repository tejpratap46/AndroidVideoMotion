package com.tejpratapsingh.animator.ui.compose

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.animator.ui.theme.AnimatorTheme
import com.tejpratapsingh.animator.worker.SampleMotionWorker

@Composable
fun HomeScreen() {
    val applicationContext = LocalContext.current.applicationContext

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                SampleMotionWorker.startWork(applicationContext)
            }
        ) {
            Text(text = "Render Video")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    AnimatorTheme {
        HomeScreen()
    }
}