package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import com.tejpratapsingh.lyricsmaker.data.api.lrclib.client.LyricsRepository
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.GetParams
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.LyricsResponse
import com.tejpratapsingh.lyricsmaker.data.api.lrclib.model.SearchParams
import com.tejpratapsingh.lyricsmaker.data.lrc.SyncedLyricFrame
import com.tejpratapsingh.motionlib.core.MotionConfig
import com.tejpratapsingh.motionlib.core.setCurrentConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [LyricsViewModel] covering the changed selectedLyrics property
 * and the new selectedStartTimeInSeconds field introduced in this PR.
 */
class LyricsViewModelTest {
    private lateinit var viewModel: LyricsViewModel

    @Before
    fun setUp() {
        // Ensure a deterministic fps so time-based assertions are stable.
        setCurrentConfig(MotionConfig(fps = 24))
        viewModel = LyricsViewModel(object : LyricsRepository {
            override suspend fun searchLyrics(params: SearchParams): Result<List<LyricsResponse>> = Result.success(emptyList())
            override suspend fun getLyrics(params: GetParams): Result<LyricsResponse> = Result.failure(Exception("Not implemented"))
        })
    }

    // ------------------------------------------------------------------
    // selectedLyrics getter – empty guard (new code path added in PR)
    // ------------------------------------------------------------------

    @Test
    fun `selectedLyrics getter returns empty list when field is empty`() {
        // Default state – no lyrics assigned yet.
        val result = viewModel.selectedLyrics
        assertTrue("Expected empty list when no lyrics are set", result.isEmpty())
    }

    // ------------------------------------------------------------------
    // selectedLyrics getter – frame normalisation
    // ------------------------------------------------------------------

    @Test
    fun `selectedLyrics getter normalises frames by subtracting the first frame`() {
        viewModel.selectedLyrics =
            listOf(
                SyncedLyricFrame(frame = 48, text = "Hello"),
                SyncedLyricFrame(frame = 96, text = "World"),
            )

        val result = viewModel.selectedLyrics

        assertEquals(2, result.size)
        // First frame should be offset to 0 (48 - 48 = 0)
        assertEquals(0, result[0].frame)
        // Second frame should be offset by the same delta (96 - 48 = 48)
        assertEquals(48, result[1].frame)
    }

    @Test
    fun `selectedLyrics getter preserves texts after frame normalisation`() {
        viewModel.selectedLyrics =
            listOf(
                SyncedLyricFrame(frame = 10, text = "Line A"),
                SyncedLyricFrame(frame = 20, text = "Line B"),
            )

        val result = viewModel.selectedLyrics

        assertEquals("Line A", result[0].text)
        assertEquals("Line B", result[1].text)
    }

    @Test
    fun `selectedLyrics getter sorts frames in ascending order`() {
        // Provide frames out of order to verify sorting.
        viewModel.selectedLyrics =
            listOf(
                SyncedLyricFrame(frame = 100, text = "Second"),
                SyncedLyricFrame(frame = 50, text = "First"),
            )

        val result = viewModel.selectedLyrics

        // After normalisation the former frame-50 entry becomes frame 0 and
        // should appear first; frame-100 becomes frame 50 and should be second.
        assertEquals(0, result[0].frame)
        assertEquals("First", result[0].text)
        assertEquals(50, result[1].frame)
        assertEquals("Second", result[1].text)
    }

    @Test
    fun `selectedLyrics getter returns single item with frame normalised to zero`() {
        viewModel.selectedLyrics =
            listOf(
                SyncedLyricFrame(frame = 72, text = "Only line"),
            )

        val result = viewModel.selectedLyrics

        assertEquals(1, result.size)
        assertEquals(0, result[0].frame)
        assertEquals("Only line", result[0].text)
    }

    // ------------------------------------------------------------------
    // selectedStartTimeInSeconds (new field added in PR)
    // ------------------------------------------------------------------

    @Test
    fun `selectedStartTimeInSeconds is zero when empty lyrics are assigned`() {
        viewModel.selectedLyrics = emptyList()

        assertEquals(0f, viewModel.selectedStartTimeInSeconds, 0.001f)
    }

    @Test
    fun `selectedStartTimeInSeconds is computed from first frame divided by fps`() {
        // fps = 24 (set in setUp), first frame = 48 → expected = 48 / 24 = 2.0 seconds
        viewModel.selectedLyrics =
            listOf(
                SyncedLyricFrame(frame = 48, text = "Start"),
                SyncedLyricFrame(frame = 72, text = "End"),
            )

        assertEquals(2.0f, viewModel.selectedStartTimeInSeconds, 0.001f)
    }

    @Test
    fun `selectedStartTimeInSeconds updates when lyrics are replaced`() {
        // First assignment: frame 24 → 1.0s
        viewModel.selectedLyrics = listOf(SyncedLyricFrame(frame = 24, text = "A"))
        assertEquals(1.0f, viewModel.selectedStartTimeInSeconds, 0.001f)

        // Second assignment: frame 48 → 2.0s
        viewModel.selectedLyrics = listOf(SyncedLyricFrame(frame = 48, text = "B"))
        assertEquals(2.0f, viewModel.selectedStartTimeInSeconds, 0.001f)
    }

    @Test
    fun `selectedStartTimeInSeconds resets to zero when lyrics are cleared`() {
        viewModel.selectedLyrics = listOf(SyncedLyricFrame(frame = 96, text = "Line"))
        // Now clear
        viewModel.selectedLyrics = emptyList()

        assertEquals(0f, viewModel.selectedStartTimeInSeconds, 0.001f)
    }

    @Test
    fun `selectedStartTimeInSeconds uses first frame even when lyrics are unsorted`() {
        // When the list is unsorted, the setter still uses value.first(), which is
        // the raw first element — NOT the minimum frame. Verify this contract.
        val fps = 24
        val firstFrame = 72 // This will be value.first()
        viewModel.selectedLyrics =
            listOf(
                SyncedLyricFrame(frame = firstFrame, text = "C"),
                SyncedLyricFrame(frame = 24, text = "A"),
            )

        val expected = firstFrame.toFloat() / fps
        assertEquals(expected, viewModel.selectedStartTimeInSeconds, 0.001f)
    }
}
