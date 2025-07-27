import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import com.tejpratapsingh.motionlib.core.animation.Easings
import com.tejpratapsingh.motionlib.core.animation.Interpolators
import com.tejpratapsingh.motionlib.core.animation.MotionInterpolator
import com.tejpratapsingh.motionlib.core.motion.MotionView
import java.io.IOException
import java.io.InputStream

class RenaultCar(context: Context, startFrame: Int, endFrame: Int) :
    MotionView(context, startFrame, endFrame) {

    companion object {
        const val imageAssetSubFolder = "renault_kiger"
    }

    val imageView: ImageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

    val assetManager = context.assets

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

    override fun forFrame(frame: Int): View {
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
            imageView.setImageBitmap(bitmap)
            inputStream.close()
        } catch (e: IOException) {
            Log.e("RenaultCar", "Error loading image from asset: $imageName", e)
        }

        return this
    }
}
