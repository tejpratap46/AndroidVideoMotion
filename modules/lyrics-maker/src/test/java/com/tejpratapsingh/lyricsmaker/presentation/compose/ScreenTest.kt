package com.tejpratapsingh.lyricsmaker.presentation.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the [Screen] sealed class, focused on the newly added
 * [Screen.ProjectDetails] object and its [Screen.ProjectDetails.createRoute]
 * helper introduced in this PR.
 */
class ScreenTest {

    // ------------------------------------------------------------------
    // Screen.ProjectDetails.route template
    // ------------------------------------------------------------------

    @Test
    fun `ProjectDetails route contains projectId path parameter placeholder`() {
        assertTrue(
            "Route should contain the {projectId} placeholder",
            Screen.ProjectDetails.route.contains("{projectId}"),
        )
    }

    @Test
    fun `ProjectDetails route starts with expected prefix`() {
        assertTrue(
            "Route should start with 'project_details/'",
            Screen.ProjectDetails.route.startsWith("project_details/"),
        )
    }

    // ------------------------------------------------------------------
    // Screen.ProjectDetails.createRoute()
    // ------------------------------------------------------------------

    @Test
    fun `createRoute returns correct path for a given projectId`() {
        val projectId = "abc123"
        val expected = "project_details/abc123"

        assertEquals(expected, Screen.ProjectDetails.createRoute(projectId))
    }

    @Test
    fun `createRoute embeds projectId containing special characters correctly`() {
        val projectId = "song-name_2024"
        val result = Screen.ProjectDetails.createRoute(projectId)

        assertEquals("project_details/song-name_2024", result)
    }

    @Test
    fun `createRoute embeds md5-style projectId correctly`() {
        // md5 IDs are 32-character hex strings – ensure they are embedded verbatim.
        val md5Id = "d41d8cd98f00b204e9800998ecf8427e"
        val result = Screen.ProjectDetails.createRoute(md5Id)

        assertEquals("project_details/$md5Id", result)
        assertTrue(result.endsWith(md5Id))
    }

    @Test
    fun `createRoute does not reference the route template placeholder literal`() {
        val result = Screen.ProjectDetails.createRoute("myId")
        assertTrue(
            "createRoute should replace the placeholder, not keep it",
            !result.contains("{projectId}"),
        )
    }

    @Test
    fun `createRoute output is consistent with route template structure`() {
        val projectId = "testProject"
        val created = Screen.ProjectDetails.createRoute(projectId)
        // The template is "project_details/{projectId}"; replacing placeholder gives the expected value.
        val expected = Screen.ProjectDetails.route.replace("{projectId}", projectId)

        assertEquals(expected, created)
    }

    // ------------------------------------------------------------------
    // Other Screen subclasses – sanity checks for existing routes (regression)
    // ------------------------------------------------------------------

    @Test
    fun `Projects screen has route 'projects'`() {
        assertEquals("projects", Screen.Projects.route)
    }

    @Test
    fun `Search screen has route 'search'`() {
        assertEquals("search", Screen.Search.route)
    }

    @Test
    fun `Lyrics screen has route 'lyrics'`() {
        assertEquals("lyrics", Screen.Lyrics.route)
    }
}