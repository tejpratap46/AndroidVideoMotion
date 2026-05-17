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

class RenaultCar(
    context: Context,
    startFrame: Int,
    endFrame: Int,
    effects: List<MotionEffect> = emptyList(),
) : BaseContourMotionView(context, startFrame, endFrame, effects = effects) {
    companion object {
        const val imageAssetSubFolder = "renault_kiger_bg"
    }

    private val imageView: ImageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

    private val assetManager = context.assets

//    val superResolutionPlugin = TensorFlowImageProcessor.superResolutionPlugin
    //    val backgroundRemover = TensorFlowImageProcessor.backgroundRemovalPlugin
    //    val backgroundRemover = PyTorchImageProcessor.backgroundRemoverPlugin

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

        // Determine which image to show based on the current frame
        val imageName = "$imageAssetSubFolder/$frame.png"

        try {
            val inputStream: InputStream = assetManager.open(imageName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
//            imageView.setImageBitmap(superResolutionPlugin.apply(bitmap))
            inputStream.close()
        } catch (e: IOException) {
            Timber.e(e, "Error loading image from asset: $imageName")
        }

        return this
    }
}
