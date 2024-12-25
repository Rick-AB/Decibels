package com.rickinc.decibels.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.rickinc.decibels.domain.service.DecibelPlaybackService
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingEvent
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingViewModel
import com.rickinc.decibels.presentation.theme.DecibelsTheme
import com.rickinc.decibels.presentation.theme.LocalController
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
            DecibelsTheme(useDarkTheme = true, dynamicColor = false) {
                CompositionLocalProvider(LocalController provides controller) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavigation()
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
            setPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                nowPlayingViewModel.onEvent(NowPlayingEvent.OnPlaybackStateChanged(playbackState))
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

