package com.rickinc.decibels.presentation.features.home.tracklist

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.media3.common.MediaItem
import com.rickinc.decibels.domain.model.Track

sealed class TrackListState {
    data object Loading : TrackListState()
    data class Content(val tracks: List<TrackItem>) : TrackListState()
    data class Error(val errorMessage: String) : TrackListState()
}

@Immutable
data class TrackItem(
    val id: Long,
    val title: String,
    val trackLength: Int,
    val artist: String,
    val albumId: Long,
    val contentUri: Uri?,
    val mediaItem: MediaItem
)
