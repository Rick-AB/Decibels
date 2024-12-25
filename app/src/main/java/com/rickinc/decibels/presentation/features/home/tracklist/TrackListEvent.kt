package com.rickinc.decibels.presentation.features.home.tracklist

import androidx.media3.common.MediaItem

sealed interface TrackListEvent {
    data class PlayTrack(val mediaItem: MediaItem) : TrackListEvent
}