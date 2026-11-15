package com.tejpratapsingh.motionlib.mlkit.effects

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.sdkinternal.MlKitContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.nio.FloatBuffer

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class SubjectSegmentationEffectTest {
    private lateinit var context: Context
    private lateinit var mockSegmenter: SubjectSegmenter
    private lateinit var testView: TestMotionView

    class TestMotionView(context: Context) : View(context), MotionView {
        var bitmapAllocations = 0

        override val startFrame: Int = 0
        override val endFrame: Int = 100
        override val loop: Pair<Int, Int> = Pair(0, 100)
        override val effects: MutableList<MotionEffect> = mutableListOf()
        override val motionConfig: MotionConfig = MotionConfig()

        override fun addEffect(effect: MotionEffect) {
            effects.add(effect)
        }

        override fun forFrame(frame: Int): MotionView = this

        override fun getViewBitmap(): Bitmap {
            bitmapAllocations++
            return Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        }
    }

    private lateinit var mockImage: InputImage

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        MlKitContext.initializeIfNeeded(context)
        mockSegmenter = mock(SubjectSegmenter::class.java)
        mockImage = mock(InputImage::class.java)
        testView = TestMotionView(context)
    }

    private fun createMockResult(width: Int = 2, height: Int = 2): SubjectSegmentationResult {
        val mockResult = mock(SubjectSegmentationResult::class.java)
        val buffer = FloatBuffer.wrap(FloatArray(width * height) { 0.9f })
        doReturn(buffer).`when`(mockResult).foregroundConfidenceMask
        return mockResult
    }

    @Test
    fun testExportModeAppliesBackpressureAndSynchronouslyPopulatesCache() {
        val mockResult = createMockResult()
        doReturn(Tasks.forResult(mockResult)).`when`(mockSegmenter).process(any(InputImage::class.java))

        val effect =
            SubjectSegmentationEffect(
                startFrame = 0,
                endFrame = 10,
                isExport = true,
                segmenter = mockSegmenter,
                imageConverter = { mockImage },
            )
        effect.motionView = testView

        // Calling forFrame in export mode should process synchronously
        effect.forFrame(1)
        assertNotNull("Frame 1 should be immediately cached in export mode", effect.maskCache[1])

        effect.forFrame(2)
        assertNotNull("Frame 2 should be immediately cached in export mode", effect.maskCache[2])

        assertEquals(2, effect.maskCache.size)
    }

    @Test
    fun testPreviewModeBoundsInFlightRequestsAndAppliesLatestFramePolicy() {
        val tcs1 = TaskCompletionSource<SubjectSegmentationResult>()
        val tcs2 = TaskCompletionSource<SubjectSegmentationResult>()

        var callCount = 0
        doAnswer {
            callCount++
            if (callCount == 1) tcs1.task else tcs2.task
        }.`when`(mockSegmenter).process(any(InputImage::class.java))

        val effect =
            SubjectSegmentationEffect(
                startFrame = 0,
                endFrame = 20,
                isExport = false,
                segmenter = mockSegmenter,
                imageConverter = { mockImage },
            )
        effect.motionView = testView

        // 1. Submit frame 1 -> should become in-flight immediately
        effect.forFrame(1)
        assertTrue(effect.isProcessing)
        assertEquals(1, effect.inFlightFrame)
        assertNull(effect.pendingLatestFrame)
        assertEquals(1, callCount)

        // 2. Submit frame 2 while frame 1 is in flight -> buffered as pendingLatestFrame
        effect.forFrame(2)
        assertTrue(effect.isProcessing)
        assertEquals(1, effect.inFlightFrame)
        assertEquals(2, effect.pendingLatestFrame)
        val pendingBitmapForFrame2 = effect.pendingLatestBitmap
        assertNotNull(pendingBitmapForFrame2)
        assertEquals(1, callCount) // No new ML Kit call yet!

        // 3. Submit frame 3 while frame 1 is still in flight -> drops frame 2, replaces with frame 3
        effect.forFrame(3)
        assertTrue(effect.isProcessing)
        assertEquals(1, effect.inFlightFrame)
        assertEquals(3, effect.pendingLatestFrame)
        assertTrue("Previous pending bitmap should be recycled", pendingBitmapForFrame2!!.isRecycled)
        val pendingBitmapForFrame3 = effect.pendingLatestBitmap
        assertNotNull(pendingBitmapForFrame3)
        assertFalse(pendingBitmapForFrame3!!.isRecycled)
        assertEquals(1, callCount) // Still bounded to 1 in-flight!

        // 4. Complete frame 1 -> frame 3 should automatically be submitted next
        tcs1.setResult(createMockResult())
        ShadowLooper.idleMainLooper()
        assertNotNull(effect.maskCache[1])
        assertEquals(2, callCount) // Frame 3 is now in-flight!
        assertEquals(3, effect.inFlightFrame)
        assertNull(effect.pendingLatestFrame)

        // 5. Complete frame 3 -> all queues clear
        tcs2.setResult(createMockResult())
        ShadowLooper.idleMainLooper()
        assertNotNull(effect.maskCache[3])
        assertFalse(effect.isProcessing)
        assertNull(effect.inFlightFrame)
        assertNull(effect.pendingLatestFrame)
    }

    @Test
    fun testCacheHitReusesMaskWithoutNewSegmentation() {
        val mockResult = createMockResult()
        doReturn(Tasks.forResult(mockResult)).`when`(mockSegmenter).process(any(InputImage::class.java))

        val effect =
            SubjectSegmentationEffect(
                startFrame = 0,
                endFrame = 10,
                isExport = true,
                segmenter = mockSegmenter,
                imageConverter = { mockImage },
            )
        effect.motionView = testView

        effect.forFrame(1)
        val allocationsAfterFirstCall = testView.bitmapAllocations

        // Calling again for the same frame should hit the cache
        effect.forFrame(1)
        assertEquals(allocationsAfterFirstCall, testView.bitmapAllocations)
    }

    @Test
    fun testFramesOutsideRangeClearPendingBitmaps() {
        val tcs = TaskCompletionSource<SubjectSegmentationResult>()
        doReturn(tcs.task).`when`(mockSegmenter).process(any(InputImage::class.java))

        val effect =
            SubjectSegmentationEffect(
                startFrame = 5,
                endFrame = 10,
                isExport = false,
                segmenter = mockSegmenter,
                imageConverter = { mockImage },
            )
        effect.motionView = testView

        effect.forFrame(5) // In flight
        effect.forFrame(6) // Pending latest
        val pendingBitmap = effect.pendingLatestBitmap
        assertNotNull(pendingBitmap)

        // Calling for frame outside range (e.g. frame 15 > 10)
        effect.forFrame(15)
        assertNull(effect.pendingLatestFrame)
        assertTrue(pendingBitmap!!.isRecycled)
    }
}
