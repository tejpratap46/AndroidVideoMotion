import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.motion.BaseContourMotionView
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import kotlin.math.min

class RenaultCar(context: Context, startFrame: Int, endFrame: Int) :
    BaseContourMotionView(context, startFrame, endFrame) {

    companion object {
        private const val TAG = "RenaultCar"
        const val imageAssetSubFolder = "renault_kiger_bg"
        const val roadAssetSubFolder = "road"
    }

    private val imageViewBg: ImageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.FIT_XY
    }

    private val imageView: ImageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

    private val assetManager = context.assets
    private val files = assetManager.list(imageAssetSubFolder)
    private val roadFiles = assetManager.list(roadAssetSubFolder)

    init {
        imageViewBg.layoutBy(x = leftTo {
            parent.left()
        }.rightTo {
            parent.right()
        }, y = topTo {
            parent.top()
        }.bottomTo {
            parent.bottom()
        })

        imageView.layoutBy(x = leftTo {
            parent.left()
        }.rightTo {
            parent.right()
        }, y = topTo {
            parent.top()
        }.bottomTo {
            parent.bottom()
        })

        contourHeightOf {
            MotionConfig.aspectRatio.height.toYInt()
        }
        contourWidthOf {
            MotionConfig.aspectRatio.width.toXInt()
        }
    }

    override fun forFrame(frame: Int): MotionView {
        super.forFrame(frame)

        val backgroundColor: Int = MotionInterpolator.interpolateColorForRange(
            interpolator = Interpolators(Easings.LINEAR),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair("#2568ff".toColorInt(), "#ba28ff".toColorInt())
        )

        setBackgroundColor(
            backgroundColor
        )

        val scaleInterpolator = MotionInterpolator.interpolateForRange(
            interpolator = Interpolators(Easings.BACK_IN_OUT),
            currentFrame = frame,
            frameRange = Pair(startFrame, endFrame),
            valueRange = Pair(0.5f, 1.0f)
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
//            roadAssetSubFolder,
//            min(roadInterpolator, (roadFiles?.size ?: 1) - 1)
//        )
//
//        try {
//            val inputStream: InputStream = assetManager.open(road)
//            val bitmap = BitmapFactory.decodeStream(inputStream)
//            imageViewBg.setImageBitmap(bitmap)
//            inputStream.close()
//        } catch (e: IOException) {
//            Log.e(TAG, "Error loading image from asset: $road", e)
//        }

        val imageName = String.format(
            Locale.getDefault(),
            "%s/%d.png",
            imageAssetSubFolder,
            min(frame, (files?.size ?: 1) - 1)
        )

        try {
            val inputStream: InputStream = assetManager.open(imageName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
            inputStream.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error loading image from asset: $imageName", e)
        }

        return this
    }
}
