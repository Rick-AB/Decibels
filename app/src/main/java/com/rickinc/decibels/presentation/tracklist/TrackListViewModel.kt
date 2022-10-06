package com.rickinc.decibels.presentation.tracklist

import com.rickinc.decibels.presentation.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrackListViewModel {
    var shouldThrowException: Boolean = false
    private val _uiState = MutableStateFlow<TrackListState>(TrackListState.Loading)
    val uiState = _uiState.asStateFlow()

    fun getAudioFiles() {
        if (shouldThrowException) _uiState.update { TrackListState.Error }
        else _uiState.update { TrackListState.DataLoaded(Track.createDummyTracks()) }
    }
}
