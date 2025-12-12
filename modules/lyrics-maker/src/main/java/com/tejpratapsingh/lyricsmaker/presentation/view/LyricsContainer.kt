package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.lyricsmaker.R
import com.tejpratapsingh.lyricsmaker.data.api.client.AlbumArtFetcher
import com.tejpratapsingh.lyricsmaker.data.lrc.LrcHelper
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.extensions.fetchBitmap
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.motion.BaseFrameMotionView
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking

class LyricsContainer(
    context: Context,
    songName: String,
    startFrame: Int,
    endFrame: Int,
    val lyrics: List<SyncedLyricFrame>,
    image: String? = null,
) : BaseFrameMotionView(context) {
    companion object {
        private const val TAG = "LyricsContainer"
    }

    private val cvLyrics: ViewGroup
    private val tvSongName: TextView
    private val ivAlbumArt: ImageView
    private val tvLyricsLine1: TextView
    private val tvLyricsLine2: TextView
    private val progress: ProgressBar
    private val fakeChartView: FakeAudioChartView
    override val effects: List<MotionEffect> = emptyList()

    init {
        super.startFrame = startFrame
        super.endFrame = endFrame

        val view = inflate(getContext(), R.layout.lyrics_container, this)
        cvLyrics = view.findViewById(R.id.cv_lyrics)
        ivAlbumArt = view.findViewById(R.id.iv_back)
        tvSongName = view.findViewById(R.id.tv_song_name)
        tvLyricsLine1 = view.findViewById(R.id.tv_lyrics_line1)
        tvLyricsLine2 = view.findViewById(R.id.tv_lyrics_line2)
        progress = view.findViewById(R.id.pb_progress)
        fakeChartView = view.findViewById(R.id.fake_chart_view)

        tvSongName.text = songName

        progress.progress = startFrame
        progress.max = endFrame

        fakeChartView.apply {
            bars = 8
            barWidthPx = 10f
            speedFactor = 0.3f
        }

        ivAlbumArt.apply {
            runBlocking {
                if (image != null) {
                    val client = HttpClient(CIO)
                    Log.i(TAG, "Using image from social meta: $image")
                    setImageBitmap(client.fetchBitmap(image))
                    client.close()
                    return@runBlocking
                } else {
                    Log.i(TAG, "Fetching from musicbrainz")
                    AlbumArtFetcher
                        .fetchAlbumArtUrl(
                            songName.split(" - ")[0],
                            songName.split(" - ")[1],
                        )?.let { url ->
                            Log.i(TAG, "cover art found: $url")
                            setImageBitmap(AlbumArtFetcher.fetchAlbumArtBitmap(url))
                            AlbumArtFetcher.close()
                        }
                }
            }
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        val backgroundColor: Int =
            MotionInterpolator.interpolateColorForRange(
                Interpolators(Easings.LINEAR),
                frame,
                Pair(startFrame, endFrame),
                Pair("#2568ff".toColorInt(), "#ba28ff".toColorInt()),
            )

        setBackgroundColor(Color.BLACK)

        MotionInterpolator
            .getComplementaryColor(
                backgroundColor,
            ).also {
                tvSongName.setTextColor(it)
                tvLyricsLine1.setTextColor(it)
                tvLyricsLine2.setTextColor(it)
            }

        fakeChartView.setFrame(frame)

        val currentLyric = LrcHelper.getCurrentLyric(lyrics = lyrics, currentFrame = frame)
        val nextLyric = LrcHelper.getNextLyric(lyrics = lyrics, currentFrame = frame)
        tvLyricsLine1.text = currentLyric?.text ?: ""
        tvLyricsLine2.text = nextLyric?.text ?: ""

        progress.progress = frame

        return this
    }

    override fun getViewBitmap(): Bitmap = this.toBitmap()
}
