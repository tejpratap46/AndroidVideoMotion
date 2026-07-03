package com.tejpratapsingh.ividemo.motion

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import timber.log.Timber
import java.io.IOException
import java.io.InputStream

class Road(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {
    companion object {
        const val IMAGE_ASSET_SUB_FOLDER = "road"
    }

    private val imageView: ImageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

    private val assetManager = context.assets

    init {
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

        // Determine which image to show based on the current frame
        val imageName = "$IMAGE_ASSET_SUB_FOLDER/$frame.png"

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
