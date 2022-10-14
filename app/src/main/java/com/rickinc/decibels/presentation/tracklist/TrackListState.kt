package com.rickinc.decibels.presentation.tracklist

import com.rickinc.decibels.presentation.model.Track

sealed class TrackListState {
    object Loading : TrackListState()
    data class DataLoaded(val tracks: List<Track>) : TrackListState()
    data class Error(val errorMessage: String) : TrackListState()
}
