package com.tejpratapsingh.lyricsmaker.presentation.activity

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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

    @Test
    fun testDialogShowsLyricsLineCount() {
        // Verify that the dialog message reports the correct number of lyrics lines.
        onView(withText(containsString("${lyrics.size} lines of lyrics"))).check(matches(isDisplayed()))
    }

    @Test
    fun testDialogMessageContainsStartFrame() {
        // Start frame is the minimum frame from the lyrics list.
        val startFrame = lyrics.minBy { it.frame }.frame
        onView(withText(containsString("Start Frame: $startFrame"))).check(matches(isDisplayed()))
    }

    @Test
    fun testCancelButtonDismissesDialog() {
        // The dialog must be visible first.
        onView(withText("Lyrics")).check(matches(isDisplayed()))

        // Click the Cancel button.
        onView(withText("Cancel")).perform(click())

        // After dismissal the dialog title should no longer be in the view hierarchy.
        onView(withText("Lyrics")).check(doesNotExist())
    }

    @Test
    fun testProjectIdLoadsSongNameFromDatabase() {
        // The dialog message should contain the song name sourced from the DB project,
        // confirming that LyricsActivity reads the MotionProject via projectId.
        onView(withText(containsString(songName))).check(matches(isDisplayed()))
    }
}