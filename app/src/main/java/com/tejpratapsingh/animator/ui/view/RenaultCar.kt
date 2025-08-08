import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import com.tejpratapsingh.motionlib.pytorch.ImageAIProcessor
import java.io.IOException
import java.io.InputStream

class RenaultCar(context: Context, startFrame: Int, endFrame: Int) :
    BaseMotionView(context, startFrame, endFrame) {

    companion object {
        const val imageAssetSubFolder = "renault_kiger"
    }

    private val imageView: ImageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

    private val assetManager = context.assets

    val backgroundRemover = ImageAIProcessor.backgroundRemoverPlugin

    init {
        imageView.layoutBy(
            x = leftTo {
                parent.left()
            }.rightTo {
                parent.right()
            },
            y = topTo {
                parent.top()
            }.bottomTo {
                parent.bottom()
            }
        )

        contourHeightOf {
            motionConfig.height.toYInt()
        }
        contourWidthOf {
            motionConfig.width.toXInt()
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

        // Determine which image to show based on the current frame
        val imageName = "$imageAssetSubFolder/$frame.jpg"

        try {
            val inputStream: InputStream = assetManager.open(imageName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(backgroundRemover.apply(bitmap))
//                imageView.setImageBitmap(backgroundRemover.removeBackgroundTiled(bitmap))
            inputStream.close()
        } catch (e: IOException) {
            Log.e("RenaultCar", "Error loading image from asset: $imageName", e)
        }

        return this
    }
}
