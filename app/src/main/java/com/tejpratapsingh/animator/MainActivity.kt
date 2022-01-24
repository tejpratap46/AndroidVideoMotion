package com.tejpratapsingh.animator

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.tejpratapsingh.animator.ui.theme.AnimatorTheme
import com.tejpratapsingh.animator.ui.view.ContourDevice
import com.tejpratapsingh.animator.ui.view.MotionVideoContainer
import com.tejpratapsingh.motionlib.di.AppContainer
import com.tejpratapsingh.motionlib.ui.MotionView
import com.tejpratapsingh.motionlib.ui.Orientation
import com.tejpratapsingh.motionlib.ui.custom.background.GradientView
import com.tejpratapsingh.motionlib.ui.custom.video.VideoView
import com.tejpratapsingh.motionlib.utils.MotionConfig
import com.tejpratapsingh.motionlib.utils.MotionVideo

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

        val motionConfig = MotionConfig(
            width = 768,
            height = 1366,
            fps = 30
        )

        val motionView: MotionView = ContourDevice(
            context = applicationContext,
            startFrame = 1,
            endFrame = motionConfig.fps * 3
        )

        val motionView2: MotionView = GradientView(
            context = applicationContext,
            startFrame = motionConfig.fps * 3 + 1,
            endFrame = motionConfig.fps * 4,
            orientation = Orientation.CIRCULAR,
            intArrayOf(
                Color.parseColor("#2568ff"),
                Color.parseColor("#7048ff"),
                Color.parseColor("#ba28ff")
            )
        ).apply {
            setBackgroundColor(Color.WHITE)
        }

        val motionView5: MotionView =
            VideoView(applicationContext, 1, motionConfig.fps * 4).apply {
//                setVideoFromAssets(fileName = "1.mp4")

                setVideoFromUrl(Uri.parse("https://screenlane.com/media/screenshots/RPReplay_Final1641926664.mov.mp4"), AppContainer.httpClient)

                contourWidthOf {
                    motionConfig.width.toXInt()
                }
                contourHeightOf {
                    motionConfig.height.toYInt()
                }
            }

        val motionVideo = MotionVideo.with(applicationContext, motionConfig)
//            .addMotionViewToSequence(motionView)
//            .addMotionViewToSequence(motionView2)
//            .addMotionViewToSequence(motionView3)
//            .addMotionViewToSequence(motionView4)
            .addMotionViewToSequence(motionView5)

        setContentView(
            MotionVideoContainer(
                context = this,
                motionVideo = motionVideo
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