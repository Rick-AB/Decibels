package com.rickinc.decibels.presentation.nowplaying

import com.rickinc.decibels.domain.exception.ErrorHolder
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track

sealed interface NowPlayingState {
    data class TrackLoaded(
        val track: Track,
        val isPlaying: Boolean,
        val repeatMode: Int,
        val isShuffleActive: Boolean,
        val progress: Long,
        val playbackState: Int
    ) : NowPlayingState

    data class ErrorLoadingTrack(val error: ErrorHolder) : NowPlayingState
}

