package com.rickinc.decibels.presentation.nowplaying

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException

sealed class NowPlayingEvent {
    data class OnMediaItemChanged(val mediaItem: MediaItem?) : NowPlayingEvent()
    data class OnIsPlayingChanged(val isPlaying: Boolean) : NowPlayingEvent()
    data class OnShuffleActiveChanged(val shuffleActive: Boolean) : NowPlayingEvent()
    data class OnRepeatModeChanged(val repeatMode: Int) : NowPlayingEvent()
    data class OnError(val error: PlaybackException?) : NowPlayingEvent()
    data class OnProgressChanged(val progress: Long) : NowPlayingEvent()
    data class OnPlaybackStateChanged(val playbackState: Int) : NowPlayingEvent()
}
