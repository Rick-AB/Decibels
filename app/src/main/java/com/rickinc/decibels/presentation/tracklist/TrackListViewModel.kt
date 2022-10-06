package com.rickinc.decibels.presentation.tracklist

import com.rickinc.decibels.presentation.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrackListViewModel {
    private val _uiState = MutableStateFlow<TrackListState>(TrackListState.Loading)
    val uiState = _uiState.asStateFlow()

    fun getAudioFiles() {
        _uiState.value = TrackListState.DataLoaded(Track.createDummyTracks())
    }
}
