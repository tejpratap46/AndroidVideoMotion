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
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motion.metadataextractor.data.SocialMeta
import com.tejpratapsingh.motion.metadataextractor.presentation.ShareReceiverActivity
import org.hamcrest.Matchers.containsString
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

    @get:Rule
    val activityRule =
        ActivityScenarioRule<LyricsActivity>(
            Intent(ApplicationProvider.getApplicationContext<Context>(), LyricsActivity::class.java).apply {
                putExtra(LyricsActivity.SONG, songName)
                putExtra(ShareReceiverActivity.EXTRA_METADATA, socialMeta)
                putParcelableArrayListExtra(LyricsActivity.LYRICS, lyrics)
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
