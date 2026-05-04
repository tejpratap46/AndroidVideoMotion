package com.tejpratapsingh.lyricsmaker.presentation.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import com.tejpratapsingh.lyricsmaker.R
import com.tejpratapsingh.lyricsmaker.data.api.albumart.client.AlbumArtRemoteDataSourceImpl
import com.tejpratapsingh.lyricsmaker.data.api.albumart.client.AlbumArtRepositoryImpl
import com.tejpratapsingh.lyricsmaker.di.OkHttpProvider
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.extensions.fetchBitmap
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.motion.BaseFrameMotionView
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class MultiLyricsContainer(
    context: Context,
    val songName: String,
    startFrame: Int,
    endFrame: Int,
    val image: String? = null,
    effects: List<MotionEffect> = emptyList(),
) : BaseFrameMotionView(context) {
    private val ivAlbumArt: ImageView
    private val fakeChartView: FakeAudioChartView

    private val okHttp = OkHttpProvider.httpClient
    private val remote = AlbumArtRemoteDataSourceImpl(okHttp)
    private val repository = AlbumArtRepositoryImpl(remote)

    init {
        super.startFrame = startFrame
        super.endFrame = endFrame
        effects.forEach { addEffect(it) }

        setBackgroundColor(Color.BLACK)

        val view = inflate(getContext(), R.layout.multi_lyrics_container, this)
        ivAlbumArt = view.findViewById(R.id.iv_back)
        fakeChartView = view.findViewById(R.id.fake_chart_view)

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
                    setImageBitmap(client.fetchBitmap(image))
                    client.close()
                    return@runBlocking
                } else {
                    Timber.i("Fetching from musicbrainz")
                    val songDetails = songName.split(" - ")
                    if (songDetails.size >= 2) {
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
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)
        fakeChartView.setFrame(frame)

        return this
    }

    override fun getViewBitmap(): Bitmap = this.toBitmap()
}
