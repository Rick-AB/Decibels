package com.rickinc.decibels.presentation.nowplaying

import androidx.media3.common.PlaybackException

sealed class NowPlayingEvent {
    data class OnError(val error: PlaybackException?) : NowPlayingEvent()
    data class OnProgressChanged(val progress: Long) : NowPlayingEvent()
    data class OnPlaybackStateChanged(val playbackState: Int) : NowPlayingEvent()
}
