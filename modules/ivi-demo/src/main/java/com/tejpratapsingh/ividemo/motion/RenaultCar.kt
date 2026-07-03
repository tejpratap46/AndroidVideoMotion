package com.tejpratapsingh.ividemo.motion

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import kotlin.math.min

class RenaultCar(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {
    companion object {
        const val IMAGE_ASSET_SUB_FOLDER = "renault_kiger_bg"
        const val ROAD_ASSET_SUB_FOLDER = "road"
    }

    private val imageViewBg: ImageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_XY
        }

    private val imageView: ImageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

    private val assetManager = context.assets
    private val files = assetManager.list(IMAGE_ASSET_SUB_FOLDER)
    private val roadFiles = assetManager.list(ROAD_ASSET_SUB_FOLDER)

    init {
        imageViewBg.layoutBy(
            x =
                leftTo {
                    parent.left()
                }.rightTo {
                    parent.right()
                },
            y =
                topTo {
                    parent.top()
                }.bottomTo {
                    parent.bottom()
                },
        )

        imageView.layoutBy(
            x =
                leftTo {
                    parent.left()
                }.rightTo {
                    parent.right()
                },
            y =
                topTo {
                    parent.top()
                }.bottomTo {
                    parent.bottom()
                },
        )

        contourHeightOf {
            provideCurrentConfig()
                .aspectRatio.height
                .toYInt()
        }
        contourWidthOf {
            provideCurrentConfig()
                .aspectRatio.width
                .toXInt()
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        val backgroundColor: Int =
            MotionInterpolator.interpolateColorForRange(
                interpolator = Interpolators(Easings.LINEAR),
                currentFrame = frame,
                frameRange = Pair(startFrame, endFrame),
                valueRange = Pair("#2568ff".toColorInt(), "#ba28ff".toColorInt()),
            )

        setBackgroundColor(
            backgroundColor,
        )

        val scaleInterpolator =
            MotionInterpolator.interpolateForRange(
                interpolator = Interpolators(Easings.BACK_IN_OUT),
                currentFrame = frame,
                frameRange = Pair(startFrame, endFrame),
                valueRange = Pair(0.5f, 1.0f),
            )

        imageView.scaleX = scaleInterpolator
        imageView.scaleY = scaleInterpolator

//        val roadInterpolator = MotionInterpolator.interpolateForRange(
//            interpolator = Interpolators(Easings.LINEAR),
//            currentFrame = frame,
//            frameRange = Pair(startFrame, endFrame),
//            valueRange = Pair(1f, 10f)
//        ).toInt()
//
//        // Determine which image to show based on the current frame
//        val road = String.format(
//            Locale.getDefault(),
//            "%s/%02d.png",
//            ROAD_ASSET_SUB_FOLDER,
//            min(roadInterpolator, (roadFiles?.size ?: 1) - 1)
//        )
//
//        try {
//            val inputStream: InputStream = assetManager.open(road)
//            val bitmap = BitmapFactory.decodeStream(inputStream)
//            imageViewBg.setImageBitmap(bitmap)
//            inputStream.close()
//        } catch (e: IOException) {
//            Timber.e(e, "Error loading image from asset: $road")
//        }

        val imageName =
            String.format(
                Locale.getDefault(),
                "%s/%d.png",
                IMAGE_ASSET_SUB_FOLDER,
                min(frame, (files?.size ?: 1) - 1),
            )

        try {
            val inputStream: InputStream = assetManager.open(imageName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
            inputStream.close()
        } catch (e: IOException) {
            Timber.e(e, "Error loading image from asset: $imageName")
        }

        return this
    }
}
