package com.rickinc.decibels.tracklist

import com.rickinc.decibels.data.repository.TestAudioRepository
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.util.TrackConverter
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
        val viewModel = TrackListViewModel(TestAudioRepository(), TrackConverter())

        assert(viewModel.uiState.value is TrackListState.Loading)
    }

    @Test
    fun getAudioFiles_ifNoErrors_stateShouldBeDataLoaded() {
        val trackConverter = TrackConverter()
        val viewModel = TrackListViewModel(TestAudioRepository(), trackConverter)
        viewModel.getAudioFiles()
        runCoroutine()

        val tracks = Track.getUniqueTrackList()
        val expectedState = TrackListState.DataLoaded(tracks, trackConverter.toMediaItems(tracks))
        val actualState = viewModel.uiState.value
        assertEquals(expectedState, actualState)
    }

    @Test
    fun getAudioFiles_ifError_stateShouldBeError() {
        val repo = TestAudioRepository()
        repo.shouldThrowException = true

        val viewModel = TrackListViewModel(repo, TrackConverter())
        viewModel.getAudioFiles()
        runCoroutine()

        val expectedState = TrackListState.Error("Error reading audio files")
        val actualState = viewModel.uiState.value
        assertEquals(expectedState, actualState)
    }

    @Test
    fun ifTrackSelected_nowPlayingStateShouldNotBeNull() {
        val selectedTrack = Track.getSingleTrack()
        val viewModel = TrackListViewModel(TestAudioRepository(), TrackConverter())
        viewModel.setNowPlaying(selectedTrack)

        assert(viewModel.nowPlayingTrack.value != null)
    }

    @Test
    fun selectingDifferentTrack_nowPlayingStateShouldBeSelectedTrack() {
        val initialSelectedTrack = Track.getSingleTrack()
        val viewModel = TrackListViewModel(TestAudioRepository(), TrackConverter())
        viewModel.setNowPlaying(initialSelectedTrack)

        val newSelectedTrack = Track.getSingleTrack(1)
        viewModel.setNowPlaying(newSelectedTrack)

        val actualState = viewModel.nowPlayingTrack.value
        assertEquals(newSelectedTrack, actualState)
    }

    private fun runCoroutine() = coroutineTestRule.testDispatcher.scheduler.runCurrent()
}