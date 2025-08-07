import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.motion.BaseMotionView
import com.tejpratapsingh.motionlib.pytorch.removebg.RemoveBg
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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

    //    private val remover = CarBgRemover(context)
    val remover = RemoveBg(context)

//    val backgroundRemover = ImageAIProcessor.backgroundRemover

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
            Interpolators(Easings.LINEAR),
            frame,
            Pair(startFrame, endFrame),
            Pair("#2568ff".toColorInt(), "#ba28ff".toColorInt())
        )

        setBackgroundColor(
            backgroundColor
        )

        // Determine which image to show based on the current frame
        val imageName = "$imageAssetSubFolder/$frame.jpg"

        try {
            val inputStream: InputStream = assetManager.open(imageName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            runBlocking {
                imageView.setImageBitmap(bitmap.removeBackground())
//                imageView.setImageBitmap(backgroundRemover.removeBackgroundTiled(bitmap))
            }
            inputStream.close()
        } catch (e: IOException) {
            Log.e("RenaultCar", "Error loading image from asset: $imageName", e)
        }

        return this
    }

    private suspend fun Bitmap.removeBackground(): Bitmap? {
        // Collect the first value from the flow and assign it to outputImage.
        // The coroutine will suspend until a value is emitted.
        return remover.clearBackground(this).first()
    }
}
