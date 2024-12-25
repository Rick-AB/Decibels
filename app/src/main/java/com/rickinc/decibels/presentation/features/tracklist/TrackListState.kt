package com.rickinc.decibels.presentation.features.tracklist

import androidx.media3.common.MediaItem
import com.rickinc.decibels.domain.model.Track

sealed class TrackListState {
    object Loading : TrackListState()
    data class DataLoaded(
        val tracks: List<Track>,
        val tracksAsMediaItems: List<MediaItem>
    ) : TrackListState()

    data class Error(val errorMessage: String) : TrackListState()
}
