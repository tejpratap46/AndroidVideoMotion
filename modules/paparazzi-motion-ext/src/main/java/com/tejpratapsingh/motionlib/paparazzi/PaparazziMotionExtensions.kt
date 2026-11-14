package com.tejpratapsingh.motionlib.paparazzi

import android.graphics.Bitmap
import android.view.View
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.tejpratapsingh.motionlib.core.motion.MotionComposerView
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Creates a Paparazzi [DeviceConfig] with explicit width and height in pixels.
 *
 * @param widthPx Canvas width in pixels.
 * @param heightPx Canvas height in pixels.
 * @param density Target display density. Defaults to [Density.XXHIGH].
 */
fun motionDeviceConfig(
    widthPx: Int,
    heightPx: Int,
    density: Density = Density.XXHIGH,
): DeviceConfig = DeviceConfig.PIXEL_5.copy(
    screenWidth = widthPx,
    screenHeight = heightPx,
    density = density,
)

/**
 * Captures a screenshot snapshot of a [MotionComposerView] at a specified [frame] and asserts
 * that the image bitmap is successfully generated with valid dimensions.
 *
 * @param composerView The [MotionComposerView] instance to render and snapshot.
 * @param frame Target frame index to evaluate via [MotionComposerView.forFrame]. Defaults to frame 0.
 * @param widthPx Target snapshot width in pixels. Defaults to [MotionComposerView]'s aspect ratio width.
 * @param heightPx Target snapshot height in pixels. Defaults to [MotionComposerView]'s aspect ratio height.
 * @param name Optional name for the captured snapshot file. Defaults to `"frame_$frame"`.
 * @return The generated frame [Bitmap].
 */
fun Paparazzi.snapshot(
    composerView: MotionComposerView,
    frame: Int = 0,
    widthPx: Int = composerView.motionConfig.aspectRatio.width,
    heightPx: Int = composerView.motionConfig.aspectRatio.height,
    name: String? = null,
): Bitmap {
    composerView.forFrame(frame)

    if ((composerView.width != widthPx) || (composerView.height != heightPx)) {
        composerView.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY),
        )
        composerView.layout(0, 0, widthPx, heightPx)
    }

    val bitmap = composerView.getViewBitmap()

    assertNotNull("Generated frame bitmap should not be null", bitmap)
    assertTrue("Generated frame bitmap width must be > 0", bitmap.width > 0)
    assertTrue("Generated frame bitmap height must be > 0", bitmap.height > 0)

    val snapshotName = name ?: "frame_$frame"
    snapshot(view = composerView, name = snapshotName)

    return bitmap
}

/**
 * Captures screenshot snapshots for multiple frame indices of a [MotionComposerView] and asserts
 * that each frame image is successfully generated.
 *
 * @param composerView The [MotionComposerView] instance to render and snapshot.
 * @param frames List of frame indices to evaluate and capture.
 * @param widthPx Target snapshot width in pixels. Defaults to [MotionComposerView]'s aspect ratio width.
 * @param heightPx Target snapshot height in pixels. Defaults to [MotionComposerView]'s aspect ratio height.
 * @param namePrefix Optional prefix for generated snapshot names.
 * @return List of generated [Bitmap]s for each evaluated frame.
 */
fun Paparazzi.snapshotFrames(
    composerView: MotionComposerView,
    frames: List<Int>,
    widthPx: Int = composerView.motionConfig.aspectRatio.width,
    heightPx: Int = composerView.motionConfig.aspectRatio.height,
    namePrefix: String? = null,
): List<Bitmap> {
    return frames.map { frame ->
        val snapshotName = if (namePrefix != null) "${namePrefix}_frame_$frame" else "frame_$frame"
        snapshot(
            composerView = composerView,
            frame = frame,
            widthPx = widthPx,
            heightPx = heightPx,
            name = snapshotName,
        )
    }
}
