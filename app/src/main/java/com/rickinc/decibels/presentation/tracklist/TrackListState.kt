package com.rickinc.decibels.presentation.tracklist

import com.rickinc.decibels.presentation.model.Track

sealed class TrackListState {
    data class DataLoaded(val tracks: List<Track>) : TrackListState()

    object Loading : TrackListState()
}
