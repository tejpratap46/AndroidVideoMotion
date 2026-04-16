package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.lyricsmaker.asLyricsApp
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motion.metadataextractor.data.SocialMeta
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionstore.tables.MotionProject
import org.hamcrest.Matchers.containsString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LyricsActivityTest {
    private val songName = "Test Song"
    private val lyrics =
        arrayListOf(
            SyncedLyricFrame(0, "Line 1"),
            SyncedLyricFrame(100, "Line 2"),
        )
    private val socialMeta = SocialMeta(title = "Social Title", image = "https://example.com/image.png")
    private val projectId = songName.md5()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val project =
            MotionProject(
                id = projectId,
                name = songName,
                path = "/$projectId",
                metadata =
                    JsonObject().apply {
                        addProperty("image", socialMeta.image)
                        addProperty("startTime", 0f)
                        add(
                            "lyrics",
                            JsonArray().apply {
                                lyrics.forEach { frame ->
                                    add(
                                        JsonObject().apply {
                                            addProperty("frame", frame.frame)
                                            addProperty("text", frame.text)
                                        },
                                    )
                                }
                            },
                        )
                    },
            )
        context.asLyricsApp().motionStoreDao.upsert(project)
    }

    @get:Rule
    val activityRule =
        ActivityScenarioRule<LyricsActivity>(
            Intent(ApplicationProvider.getApplicationContext<Context>(), LyricsActivity::class.java).apply {
                putExtra(LyricsActivity.PROJECT_ID, projectId)
            },
        )

    @Test
    fun testLyricsActivityLaunch() {
        // Verify that the dialog with the title "Lyrics" is displayed
        onView(withText("Lyrics")).check(matches(isDisplayed()))

        // Verify that the dialog message contains the song name
        // Note: The message in LyricsActivity uses literal backslashes for quotes
        onView(withText(containsString("""Rendering video for \"$songName\""""))).check(matches(isDisplayed()))

        // Verify that the OK and Cancel buttons are present
        onView(withText("OK")).check(matches(isDisplayed()))
        onView(withText("Cancel")).check(matches(isDisplayed()))
    }
}
