package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.lyricsmaker.data.api.client.AlbumArtFetcher
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import kotlinx.coroutines.runBlocking

class LyricsContainer(
    context: Context,
    songName: String,
    lyrics: List<SyncedLyricFrame>,
    startFrame: Int,
    endFrame: Int,
) : BaseMotionView(context, startFrame, endFrame) {

    companion object {
        private const val TAG = "LyricsContainer"
    }

    private val songNameTextView: SongNameTextView = SongNameTextView(
        context = context, songName = songName, startFrame = startFrame, endFrame = endFrame
    ).apply {
        textView.textSize = 24f
        textView.gravity = Gravity.CENTER
    }

    private val albumArtImageView: ImageView = ImageView(context).apply {
        alpha = 0.5f
        runBlocking {
            AlbumArtFetcher.fetchAlbumArtUrl(songName.split(" - ")[0], songName.split(" - ")[1])
                ?.let { url ->
                    Log.i(TAG, "cover art found: $url")
                    setImageBitmap(AlbumArtFetcher.fetchAlbumArtBitmap(url))
                }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

//    private val rotatingMotionView: RotatingMotionView = RotatingMotionView(
//        context = context,
//        startFrame = startFrame,
//        endFrame = endFrame,
//        view = albumArtImageView
//    ).apply {
//        contourHeightOf {
//            motionConfig.aspectRatio.height.toYInt()
//        }
//        contourWidthOf {
//            motionConfig.aspectRatio.width.toXInt()
//        }
//    }

    private val lyricsTextView: LyricsTextView = LyricsTextView(
        context = context,
        lyrics = lyrics,
        startFrame = startFrame,
        endFrame = endFrame,
        fontUrl = "https://www.fontmirror.com/app_public/files/t/1/Poppins-Regular_684471b5ff3c204b8d3b3da3bd4e082d.ttf"
    ).apply {
        textView.textSize = 18f
        textView.gravity = Gravity.CENTER
    }

    init {
//        bgVideoView.layoutBy(
//            x = leftTo {
//                parent.left()
//            }.rightTo {
//                parent.right()
//            }, y = topTo {
//                parent.top()
//            }.bottomTo {
//                parent.bottom()
//            }
//        )

        albumArtImageView.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            }, y = topTo {
                parent.bottom()
            }.bottomTo {
                parent.top()
            }
        )

        songNameTextView.layoutBy(x = leftTo {
            parent.left()
        }.rightTo {
            parent.right()
        }, y = topTo {
            parent.top()
        })

        lyricsTextView.layoutBy(x = leftTo {
            parent.left()
        }.rightTo {
            parent.right()
        }, y = topTo {
            songNameTextView.bottom()
        }.bottomTo {
            parent.bottom()
        })

        contourHeightOf {
            motionConfig.aspectRatio.height.toYInt()
        }
        contourWidthOf {
            motionConfig.aspectRatio.width.toXInt()
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        val backgroundColor: Int = MotionInterpolator.interpolateColorForRange(
            Interpolators(Easings.LINEAR),
            frame,
            Pair(startFrame, endFrame),
            Pair("#2568ff".toColorInt(), "#ba28ff".toColorInt())
        )

        setBackgroundColor(Color.BLACK)

        songNameTextView.textView.setTextColor(
            MotionInterpolator.getComplementaryColor(
                backgroundColor
            )
        )

        lyricsTextView.textView.setTextColor(
            MotionInterpolator.getComplementaryColor(
                backgroundColor
            )
        )

        return this
    }

    override fun getViewBitmap(): Bitmap = this.toBitmap()
}