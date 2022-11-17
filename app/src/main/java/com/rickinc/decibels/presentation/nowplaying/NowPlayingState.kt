package com.rickinc.decibels.presentation.nowplaying

import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track

sealed interface NowPlayingState {
    data class TrackLoaded(
        val currentTrack: Track,
        val isPlaying: Boolean,
        val repeatMode: Int,
        val isShuffleActive: Boolean,
        val progress: Long
    ) : NowPlayingState

    data class ErrorLoadingTrack(val error: Result.Error) : NowPlayingState
}

