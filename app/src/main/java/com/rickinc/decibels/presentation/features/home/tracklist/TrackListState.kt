package com.rickinc.decibels.presentation.features.home.tracklist

import androidx.compose.runtime.Immutable
import com.rickinc.decibels.domain.model.Track

@Immutable
sealed class TrackListState {
    data object Loading : TrackListState()
    data object Empty : TrackListState()
    data class Content(val tracks: List<Track>) : TrackListState()
    data class Error(val errorMessage: String) : TrackListState()
}
