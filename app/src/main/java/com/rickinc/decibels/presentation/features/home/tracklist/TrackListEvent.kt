package com.rickinc.decibels.presentation.features.home.tracklist

import androidx.media3.session.MediaController
import com.rickinc.decibels.domain.model.Track

sealed interface TrackListEvent {
    data class PlayTrack(val track: Track, val mediaController: MediaController) :
        TrackListEvent
}