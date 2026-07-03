package com.tejpratapsingh.motionlib.core.motion

import android.graphics.Bitmap
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionLayoutInfo
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.motion.transitions.CrossFadeTransition
import com.tejpratapsingh.motionlib.ui.effects.FadeInEffect
import com.tejpratapsingh.motionlib.ui.effects.FadeOutEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionTransitionTest {
    class MockMotionView(
        override val startFrame: Int,
        override val endFrame: Int,
    ) : MotionView {
        override var loop: Pair<Int, Int> = Pair(0, 0)
        override val effects: MutableList<MotionEffect> = mutableListOf()
        override var layoutInfo: MotionLayoutInfo = MotionLayoutInfo()

        override fun addEffect(effect: MotionEffect) {
            effects.add(effect)
        }

        override fun getViewBitmap(): Bitmap = throw UnsupportedOperationException()

        override fun forFrame(frame: Int): MotionView = this
    }

    @Test
    fun testCrossFadeTransitionOverlap() {
        val view1 = MockMotionView(0, 100)
        val view2 = MockMotionView(101, 200)

        val transition = CrossFadeTransition()
        val duration = 20

        transition.apply(view1, view2, duration)

        // startFrame and endFrame should NOT be adjusted
        assertEquals(0, view1.startFrame)
        assertEquals(100, view1.endFrame)
        assertEquals(101, view2.startFrame)
        assertEquals(200, view2.endFrame)

        // Effects should be added
        assertTrue(view1.effects.any { it is FadeOutEffect })
        assertTrue(view2.effects.any { it is FadeInEffect })

        val fadeOut = view1.effects.first { it is FadeOutEffect }
        val fadeIn = view2.effects.first { it is FadeInEffect }

        // Transition centered at boundary 101: [101-10, 101+10-1] = [91, 110]
        assertEquals(91, fadeOut.startFrame)
        assertEquals(110, fadeOut.endFrame)

        assertEquals(91, fadeIn.startFrame)
        assertEquals(110, fadeIn.endFrame)
    }
}
