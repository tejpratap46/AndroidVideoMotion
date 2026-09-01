package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.lyricsmaker.R
import com.tejpratapsingh.lyricsmaker.data.api.albumart.client.AlbumArtRemoteDataSourceImpl
import com.tejpratapsingh.lyricsmaker.data.api.albumart.client.AlbumArtRepositoryImpl
import com.tejpratapsingh.lyricsmaker.data.lrc.LrcHelper
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.lyricsmaker.di.OkHttpProvider
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.extensions.fetchBitmap
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.fontSizeH3
import com.tejpratapsingh.motionlib.core.fontSizeH5
import com.tejpratapsingh.motionlib.core.motion.BaseFrameMotionView
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class LyricsContainer(
    context: Context,
    val songName: String,
    startFrame: Int,
    endFrame: Int,
    val lyrics: List<SyncedLyricFrame>,
    val asset: MotionAsset? = null,
    effects: List<MotionEffect> = emptyList(),
) : BaseFrameMotionView(context) {
    /**
     * For backward compatibility, the image path.
     */
    val image: String? get() = asset?.getUri()?.toString()

    private val cvLyrics: ViewGroup
    private val tvSongName: TextView
    private val ivAlbumArt: ImageView
    private val tvLyricsLine1: TextView
    private val tvLyricsLine2: TextView
    private val progress: SeekBar
    private val fakeChartView: FakeAudioChartView

    private val okHttp = OkHttpProvider.httpClient
    private val remote = AlbumArtRemoteDataSourceImpl(okHttp)
    private val repository = AlbumArtRepositoryImpl(remote)

    init {
        super.startFrame = startFrame
        super.endFrame = endFrame
        effects.forEach { addEffect(it) }

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
            animationSpeed = 0.3f
        }

        ivAlbumArt.apply {
            runBlocking {
                if (image != null) {
                    val client = HttpClient(CIO)
                    Timber.i("Using image from social meta: $image")
                    setImageBitmap(client.fetchBitmap(image!!))
                    client.close()
                    return@runBlocking
                } else {
                    Timber.i("Fetching from musicbrainz")
                    val songDetails = songName.split(" - ")
                    val url =
                        repository.getAlbumArtUrl(
                            songDetails[0],
                            songDetails[1],
                        )
                    url?.let { repository.getAlbumArtBitmap(it) }?.also {
                        setImageBitmap(it)
                    }
                }
            }
        }
    }

    private var isTextSizeInitialized = false

    override fun forFrame(frame: Int): MotionView {
        if (!isTextSizeInitialized) {
            tvSongName.setTextSize(TypedValue.COMPLEX_UNIT_PX, motionConfig.fontSizeH5)
            tvLyricsLine1.setTextSize(TypedValue.COMPLEX_UNIT_PX, motionConfig.fontSizeH3)
            tvLyricsLine2.setTextSize(TypedValue.COMPLEX_UNIT_PX, motionConfig.fontSizeH3)
            isTextSizeInitialized = true
        }

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

    override val assets: List<MotionAsset>
        get() = asset?.let { listOf(it) } ?: emptyList()
}
