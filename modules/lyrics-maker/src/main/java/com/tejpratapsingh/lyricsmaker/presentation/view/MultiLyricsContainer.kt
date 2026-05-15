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
import com.tejpratapsingh.motionlib.core.extensions.toBitmap
import com.tejpratapsingh.motionlib.core.motion.BaseFrameMotionView
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
    val fakeChartView: FakeAudioChartView

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
                    val bitmap = repository.getAlbumArtBitmap(image)
                    bitmap?.let {
                        setImageBitmap(it)
                    }
                } else {
                    Timber.i("Fetching from musicbrainz")
                    val songDetails = songName.split(" - ")
                    if (songDetails.size >= 2) {
                        val url =
                            repository.getAlbumArtUrl(
                                songDetails[0],
                                songDetails[1],
                            )

                        url?.let { url ->
                            val bitmap = repository.getAlbumArtBitmap(url)
                            bitmap?.let {
                                setImageBitmap(it)
                            }
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
