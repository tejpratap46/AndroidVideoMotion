package com.tejpratapsingh.motionlib.core.motion

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.MotionTransition
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.VideoProducerAdapter
import com.tejpratapsingh.motionlib.core.adapter.AndroidVideoProducerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

open class MotionVideoProducer private constructor(
    val context: Context,
    val videoProducerAdapter: VideoProducerAdapter,
    val motionComposerView: MotionComposerView,
    val motionAudio: MutableList<MotionAudio> = mutableListOf(),
) : IMotionVideoProducer {
    private var lastMotionView: (MotionView)? = null
    private var pendingTransition: Pair<MotionTransition, Int>? = null

    var totalFrames: Int = 0
        private set

    companion object {
        @JvmStatic
        fun with(
            context: Context,
            plugins: List<MotionPlugin> = emptyList(),
            motionAudio: List<MotionAudio> = emptyList(),
            videoProducerAdapter: VideoProducerAdapter = AndroidVideoProducerAdapter(),
        ) = MotionVideoProducer(
            context = context,
            videoProducerAdapter = videoProducerAdapter,
            motionComposerView =
                MotionComposerView(
                    context = context,
                    plugins = plugins,
                ),
            motionAudio = motionAudio.toMutableList(),
        )
    }

    fun addAudio(audio: MotionAudio): MotionVideoProducer {
        motionAudio.add(audio)
        return this
    }

    override fun addTransition(
        transition: MotionTransition,
        duration: Int,
    ): MotionVideoProducer {
        pendingTransition = Pair(transition, duration)
        return this
    }

    override fun <T> addMotionViewToSequence(motionView: T): MotionVideoProducer where T : MotionView, T : ViewGroup {
        val currentLastView = lastMotionView
        val currentPendingTransition = pendingTransition

        if (currentLastView != null && currentPendingTransition != null) {
            val (transition, duration) = currentPendingTransition
            transition.apply(currentLastView, motionView, duration)
            pendingTransition = null
        }

        lastMotionView = motionView

        totalFrames = maxOf(totalFrames, motionView.endFrame)
        motionComposerView.apply {
            val layoutInfo = motionView.layoutInfo
            motionView.layoutBy(
                x =
                    if (layoutInfo.gravity and android.view.Gravity.CENTER_HORIZONTAL == android.view.Gravity.CENTER_HORIZONTAL) {
                        centerHorizontallyTo { parent.centerX() }
                    } else if (layoutInfo.gravity and android.view.Gravity.LEFT == android.view.Gravity.LEFT) {
                        leftTo { parent.left() + layoutInfo.margin.left.toXInt() }
                    } else if (layoutInfo.gravity and android.view.Gravity.RIGHT == android.view.Gravity.RIGHT) {
                        rightTo { parent.right() - layoutInfo.margin.right.toXInt() }
                    } else {
                        centerHorizontallyTo { parent.centerX() }
                    },
                y =
                    if (layoutInfo.gravity and android.view.Gravity.CENTER_VERTICAL == android.view.Gravity.CENTER_VERTICAL) {
                        centerVerticallyTo { parent.centerY() }
                    } else if (layoutInfo.gravity and android.view.Gravity.TOP == android.view.Gravity.TOP) {
                        topTo { parent.top() + layoutInfo.margin.top.toYInt() }
                    } else if (layoutInfo.gravity and android.view.Gravity.BOTTOM == android.view.Gravity.BOTTOM) {
                        bottomTo { parent.bottom() - layoutInfo.margin.bottom.toYInt() }
                    } else {
                        centerVerticallyTo { parent.centerY() }
                    },
                addToViewGroup = true,
            )
        }
        return this
    }

    override suspend fun produceVideo(
        context: Context,
        outputFile: File,
        progressListener: (suspend (progress: Int, bitmap: Bitmap) -> Unit)?,
    ): File =
        withContext(Dispatchers.Default) {
            if (outputFile.exists()) {
                outputFile.delete()
            }

            videoProducerAdapter.produceVideo(
                context = context,
                motionComposerView = motionComposerView,
                motionAudio = motionAudio,
                totalFrames = totalFrames,
                outputFile = outputFile,
                progressListener = progressListener,
            )

            outputFile
        }
}
