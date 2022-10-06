package com.rickinc.decibels.tracklist

import com.rickinc.decibels.presentation.model.Track
import com.rickinc.decibels.presentation.tracklist.TrackListState
import com.rickinc.decibels.presentation.tracklist.TrackListViewModel
import com.rickinc.decibels.util.CoroutineTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TrackListTest {
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Test
    fun initializeViewModel_stateShouldBeLoading() {
        val viewModel = TrackListViewModel()

        assert(viewModel.uiState.value is TrackListState.Loading)
    }

    @Test
    fun getAudioFiles_ifNoErrors_stateShouldBeDataLoaded() {
        val viewModel = TrackListViewModel()
        viewModel.getAudioFiles()

        val tracks = Track.createDummyTracks()
        val expectedState = TrackListState.DataLoaded(tracks)
        val actualState = viewModel.uiState.value
        assertEquals(expectedState, actualState)
    }

    @Test
    fun getAudioFiles_ifError_stateShouldBeError() {
        val viewModel = TrackListViewModel()
        viewModel.shouldThrowException = true
        viewModel.getAudioFiles()

        val expectedState = TrackListState.Error
        val actualState = viewModel.uiState.value
        assertEquals(expectedState, actualState)
    }
}