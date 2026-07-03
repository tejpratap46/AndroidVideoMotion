package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tejpratapsingh.lyricsmaker.presentation.compose.details.ProjectDetailsScreen
import com.tejpratapsingh.motionstore.tables.MotionProject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for [ProjectDetailsScreen], which is a new file added in this PR.
 *
 * These tests verify the UI surface of the composable in isolation by supplying
 * fake [MotionProject] data and spy callbacks.
 *
 * Note: [ProjectDetailsScreen] internally creates a [MotionVideoPlayer] which
 * requires a real Surface / GPU context. To keep tests fast and hermetic we
 * only assert on the text / button nodes that are rendered in the info panel
 * and avoid driving the video player itself.
 */
@RunWith(AndroidJUnit4::class)
class ProjectDetailsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildProject(
        id: String = "test-id",
        name: String = "My Test Song",
        startTime: Int = 5,
        imageUrl: String = "https://example.com/art.png",
    ): MotionProject =
        MotionProject(
            id = id,
            name = name,
            path = "/$id",
            metadata =
                JsonObject().apply {
                    addProperty("image", imageUrl)
                    addProperty("startTime", startTime)
                    add(
                        "lyrics",
                        JsonArray().apply {
                            add(
                                JsonObject().apply {
                                    addProperty("frame", 0)
                                    addProperty("text", "Hello")
                                },
                            )
                        },
                    )
                },
        )

    // ------------------------------------------------------------------
    // Project name
    // ------------------------------------------------------------------

    @Test
    fun projectName_isDisplayed() {
        val project = buildProject(name = "Awesome Track")

        composeTestRule.setContent {
            ProjectDetailsScreen(
                project = project,
                onBackClick = {},
                onEditClick = {},
                onShareClick = {},
            )
        }

        composeTestRule.onNodeWithText("Awesome Track").assertIsDisplayed()
    }

    // ------------------------------------------------------------------
    // Start time label
    // ------------------------------------------------------------------

    @Test
    fun startTime_isDisplayedWithCorrectValue() {
        val project = buildProject(startTime = 7)

        composeTestRule.setContent {
            ProjectDetailsScreen(
                project = project,
                onBackClick = {},
                onEditClick = {},
                onShareClick = {},
            )
        }

        composeTestRule.onNodeWithText("Starts at: 7s").assertIsDisplayed()
    }

    @Test
    fun startTime_showsZeroWhenMetadataIsAbsent() {
        // Create a project whose metadata does not have a "startTime" entry.
        val project =
            MotionProject(
                id = "no-start-time",
                name = "Track Without Start",
                path = "/no-start-time",
                metadata = JsonObject(), // empty – no startTime key
            )

        composeTestRule.setContent {
            ProjectDetailsScreen(
                project = project,
                onBackClick = {},
                onEditClick = {},
                onShareClick = {},
            )
        }

        // Falls back to asInt which returns 0 for a missing/null key.
        composeTestRule.onNodeWithText("Starts at: 0s").assertIsDisplayed()
    }

    // ------------------------------------------------------------------
    // Share button
    // ------------------------------------------------------------------

    @Test
    fun shareButton_isDisplayed() {
        val project = buildProject()

        composeTestRule.setContent {
            ProjectDetailsScreen(
                project = project,
                onBackClick = {},
                onEditClick = {},
                onShareClick = {},
            )
        }

        composeTestRule.onNodeWithText("Share Project").assertIsDisplayed()
    }

    @Test
    fun shareButton_invokesOnShareClickWithProject() {
        val project = buildProject(id = "share-test")
        var sharedProject: MotionProject? = null

        composeTestRule.setContent {
            ProjectDetailsScreen(
                project = project,
                onBackClick = {},
                onEditClick = {},
                onShareClick = { sharedProject = it },
            )
        }

        composeTestRule.onNodeWithText("Share Project").performClick()

        assertEquals(project.id, sharedProject?.id)
    }

    // ------------------------------------------------------------------
    // Back button
    // ------------------------------------------------------------------

    @Test
    fun backButton_isDisplayed() {
        val project = buildProject()

        composeTestRule.setContent {
            ProjectDetailsScreen(
                project = project,
                onBackClick = {},
                onEditClick = {},
                onShareClick = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun backButton_invokesOnBackClick() {
        val project = buildProject()
        var backClicked = false

        composeTestRule.setContent {
            ProjectDetailsScreen(
                project = project,
                onBackClick = { backClicked = true },
                onEditClick = {},
                onShareClick = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(true, backClicked)
    }

    // ------------------------------------------------------------------
    // Edit button
    // ------------------------------------------------------------------

    @Test
    fun editButton_isDisplayed() {
        val project = buildProject()

        composeTestRule.setContent {
            ProjectDetailsScreen(
                project = project,
                onBackClick = {},
                onEditClick = {},
                onShareClick = {},
            )
        }

        composeTestRule.onNodeWithText("Edit Project").assertIsDisplayed()
    }

    @Test
    fun editButton_invokesOnEditClickWithProject() {
        val project = buildProject(id = "edit-test")
        var editedProject: MotionProject? = null

        composeTestRule.setContent {
            ProjectDetailsScreen(
                project = project,
                onBackClick = {},
                onEditClick = { editedProject = it },
                onShareClick = {},
            )
        }

        composeTestRule.onNodeWithText("Edit Project").performClick()

        assertEquals(project.id, editedProject?.id)
    }
}
