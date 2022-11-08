package com.rickinc.decibels.presentation

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.*
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.rickinc.decibels.domain.service.DecibelPlaybackService
import com.rickinc.decibels.presentation.nowplaying.NowPlayingEvent
import com.rickinc.decibels.presentation.nowplaying.NowPlayingViewModel
import com.rickinc.decibels.presentation.ui.theme.DecibelsTheme
import com.rickinc.decibels.presentation.ui.theme.LocalController
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var controller by mutableStateOf<MediaController?>(null)

    private val nowPlayingViewModel: NowPlayingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DecibelsTheme(darkTheme = true) {
                CompositionLocalProvider(LocalController provides controller) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainActivityLayout()
                    }
                }
            }
        }
    }

    private fun initializeController() {
        val sessionToken =
            SessionToken(this, ComponentName(this, DecibelPlaybackService::class.java))

        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            controller = controllerFuture.get()
            setControllerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setControllerListener() {
        controller?.addListener(object : Player.Listener {

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                Timber.d("onMediaMetadataChanged")
            }

            override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onPlaylistMetadataChanged(mediaMetadata)
                Timber.d("onPlaylistMetadataChanged")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Timber.d("onPlaybackStateChanged ${getPlaybackStateName(playbackState)}")
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                Timber.d("onMediaItemTransition ${mediaItem?.mediaMetadata?.displayTitle}")
                nowPlayingViewModel.onEvent(
                    NowPlayingEvent.OnMediaItemChanged(
                        controller?.currentMediaItem
                    )
                )
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Timber.d("onIsPlayingChanged")
                nowPlayingViewModel.onEvent(NowPlayingEvent.OnIsPlayingChanged(isPlaying))
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                super.onRepeatModeChanged(repeatMode)
                Timber.d("onRepeatModeChanged")
                nowPlayingViewModel.onEvent(NowPlayingEvent.OnRepeatModeChanged(repeatMode))
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                Timber.d("onShuffleModeEnabledChanged")
                nowPlayingViewModel.onEvent(
                    NowPlayingEvent.OnShuffleActiveChanged(
                        shuffleModeEnabled
                    )
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Timber.d("onPlayerError")
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                nowPlayingViewModel.onEvent(NowPlayingEvent.OnError(error))
            }
        })
    }

    private fun getPlaybackStateName(i: Int): String? {
        return when (i) {
            1 -> "STATE_IDLE"
            2 -> "STATE_BUFFERING"
            3 -> "STATE_READY"
            4 -> "STATE_ENDED"
            else -> null
        }
    }

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    override fun onStop() {
        MediaController.releaseFuture(controllerFuture)
        super.onStop()
    }
}

