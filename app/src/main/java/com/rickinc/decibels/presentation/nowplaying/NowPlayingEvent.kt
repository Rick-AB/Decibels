package com.rickinc.decibels.presentation.nowplaying

import android.content.Context
import androidx.media3.common.PlaybackException
import com.rickinc.decibels.domain.model.Track

sealed class NowPlayingEvent {
    data class OnError(val error: PlaybackException?) : NowPlayingEvent()
    data class OnProgressChanged(val progress: Long) : NowPlayingEvent()
    data class OnPlaybackStateChanged(val playbackState: Int) : NowPlayingEvent()

    data class OnTrackChanged(val context: Context, val track: Track) : NowPlayingEvent()
}
