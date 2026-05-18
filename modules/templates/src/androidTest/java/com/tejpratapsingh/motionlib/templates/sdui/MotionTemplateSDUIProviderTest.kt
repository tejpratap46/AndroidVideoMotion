package com.tejpratapsingh.motionlib.templates.sdui

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tejpratapsingh.motionlib.templates.dsl.motionTemplate
import com.tejpratapsingh.motionlib.templates.extensions.popUpTextView
import com.tejpratapsingh.motionlib.templates.model.TemplateData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MotionTemplateSDUIProviderTest {

    @Test
    fun testProvideSDUI() {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

        val template = motionTemplate("Test Template") {
            parameters {
                string("title")
            }
            content {
                val title = data.getString("title") ?: "Default"
                popUpTextView(
                    text = title,
                    startFrame = 0,
                    endFrame = 100
                )
                audio(File(context.cacheDir, "test.mp3"), startFrame = 0, endFrame = 100)
            }
        }

        val data = TemplateData(mapOf(
            "title" to "Hello SDUI"
        ))

        val sdui = MotionTemplateSDUIProvider.provideSDUI(
            context = appContext,
            template = template,
            data = data
        )

        assertNotNull(sdui)
        assertTrue(sdui.has("views"))
        assertTrue(sdui.has("audios"))
        assertTrue(sdui.has("config"))

        val viewsArray = sdui.getAsJsonArray("views")
        assertEquals(1, viewsArray.size())
        
        val firstView = viewsArray[0].asJsonObject
        assertTrue(firstView.has("type"))
        
        val audiosArray = sdui.getAsJsonArray("audios")
        assertEquals(1, audiosArray.size())
    }
}
