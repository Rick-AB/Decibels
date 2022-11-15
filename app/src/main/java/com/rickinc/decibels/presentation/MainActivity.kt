package com.rickinc.decibels.presentation

import android.content.*
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.rickinc.decibels.domain.service.DecibelPlaybackService
import com.rickinc.decibels.presentation.nowplaying.NowPlayingEvent
import com.rickinc.decibels.presentation.nowplaying.NowPlayingViewModel
import com.rickinc.decibels.presentation.ui.theme.DecibelsTheme
import com.rickinc.decibels.presentation.ui.theme.LocalController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var player: Player
    private lateinit var decibelService: DecibelPlaybackService
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var controller by mutableStateOf<MediaController?>(null)
    private var bound: Boolean = false

    private val nowPlayingViewModel: NowPlayingViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())
    private val updateNowPlayingAction = Runnable { updateNowPlayingProgress() }
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = DecibelPlaybackService.binder as DecibelPlaybackService.LocalBinder
            decibelService = binder.getService()
            player = decibelService.player
            bound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            bound = false
        }
    }

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
            setPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setControllerListener() {
        controller?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                nowPlayingViewModel.onEvent(
                    NowPlayingEvent.OnMediaItemChanged(
                        controller?.currentMediaItem
                    )
                )
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                nowPlayingViewModel.onEvent(
                    NowPlayingEvent.OnIsPlayingChanged(
                        controller?.isPlaying ?: false
                    )
                )
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                super.onRepeatModeChanged(repeatMode)
                nowPlayingViewModel.onEvent(NowPlayingEvent.OnRepeatModeChanged(repeatMode))
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                nowPlayingViewModel.onEvent(
                    NowPlayingEvent.OnShuffleActiveChanged(
                        shuffleModeEnabled
                    )
                )
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                nowPlayingViewModel.onEvent(NowPlayingEvent.OnError(error))
            }
        })
    }

    private fun setPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                getPlaybackStateName(playbackState)
                updateNowPlayingProgress()
            }
        })
    }

    private fun updateNowPlayingProgress() {
        val currentPos = player.currentPosition
        val duration = player.duration
        val progress = (currentPos.times(100)).div(duration)
        nowPlayingViewModel.onEvent(NowPlayingEvent.OnProgressChanged(currentPos))

        handler.removeCallbacks(updateNowPlayingAction)
        // Schedule an update if necessary.
        val playbackState = player.playbackState
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            var delayMs: Long
            if (player.playWhenReady && playbackState == Player.STATE_READY) {
                delayMs = 1000 - progress % 1000
                if (delayMs < 200) {
                    delayMs += 1000
                }
            } else {
                delayMs = 1000
            }
            handler.postDelayed(updateNowPlayingAction, delayMs)
        }
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
        Intent(this, DecibelPlaybackService::class.java).also { intent ->
            intent.action = MediaSessionService.SERVICE_INTERFACE
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        MediaController.releaseFuture(controllerFuture)
        unbindService(connection)
        bound = false
        super.onStop()
    }
}

