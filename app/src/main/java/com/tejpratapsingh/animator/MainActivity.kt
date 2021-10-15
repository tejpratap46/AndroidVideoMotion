package com.tejpratapsingh.animator

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.tejpratapsingh.animator.ui.theme.AnimatorTheme
import com.tejpratapsingh.animator.ui.view.ContourDevice
import com.tejpratapsingh.animator.ui.view.MotionVideoContainer
import com.tejpratapsingh.motionlib.ui.MotionComposerView
import com.tejpratapsingh.motionlib.ui.MotionView
import com.tejpratapsingh.motionlib.utils.MotionConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContent {
//            AnimatorTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(color = MaterialTheme.colors.background) {
//                    Greeting("Android")
//                }
//            }
//        }

        val motionView: MotionView = ContourDevice(applicationContext).apply {
            setBackgroundColor(Color.MAGENTA)
        }

        val motionComposerView: MotionComposerView = MotionComposerView(
            context = applicationContext,
            MotionConfig(
                width = 768,
                height = 1366,
                fps = 60,
                totalFrames = 90
            )
        ).apply {
            setBackgroundColor(Color.WHITE)

            motionView.layoutBy(
                x = leftTo {
                    parent.centerX() - 5.toXInt()
                }.widthOf { 10.toXInt() },
                y = topTo {
                    parent.centerY() - 5.toYInt()
                }.heightOf { 10.toYInt() }
            )
        }

        setContentView(
            MotionVideoContainer(
                context = this,
                motionComposerView = motionComposerView
            )
        )
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AnimatorTheme {
        Greeting("Android")
    }
}