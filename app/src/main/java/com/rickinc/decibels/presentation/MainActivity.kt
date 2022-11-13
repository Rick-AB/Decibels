package com.rickinc.decibels.presentation

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
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
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var controller by mutableStateOf<MediaController?>(null)

    private val nowPlayingViewModel: NowPlayingViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())


    private val updateNowPlayingAction = Runnable { updateNowPlayingProgress() }

    companion object {
        const val PLAYER_EVENT_INTENT = "player_event_intent"
        const val PLAYER_EVENT = "player_event_receiver"
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

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val playbackState = intent?.extras?.getInt(PLAYER_EVENT)
                handler.postDelayed(object : Runnable {
                    override fun run() {
                        val currentPos = (controller?.currentPosition ?: 0)
                        val duration = controller?.duration ?: 0
                        val progress = (currentPos.times(100)).div(duration)
                        nowPlayingViewModel.onEvent(NowPlayingEvent.OnProgressChanged(currentPos))

                        handler.removeCallbacks(this)
                        // Schedule an update if necessary.
                        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                            var delayMs: Long
                            if (controller?.playWhenReady!! && playbackState == Player.STATE_READY) {
                                delayMs = 1000 - progress % 1000
                                if (delayMs < 200) {
                                    delayMs += 1000
                                }
                            } else {
                                delayMs = 1000
                            }
                            handler.postDelayed(this, delayMs)
                        }
                    }
                }, 1000)
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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setControllerListener() {
        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                getPlaybackStateName(playbackState)
//                updateNowPlayingProgress()
            }

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
                Timber.d("onIsPlayingChanged ${controller?.currentPosition}")
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
        }
        controller?.addListener(listener)
    }

    private fun updateNowPlayingProgress() {
//        val currentPos = (controller?.currentPosition ?: 0)
//        val duration = controller?.duration ?: 0
//        val progress = (currentPos.times(100)).div(duration)
//        nowPlayingViewModel.onEvent(NowPlayingEvent.OnProgressChanged(currentPos))
//
//        handler.removeCallbacks(updateNowPlayingAction)
//        // Schedule an update if necessary.
//        val playbackState = controller?.playbackState ?: Player.STATE_IDLE
//        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
//            var delayMs: Long
//            if (controller?.playWhenReady!! && playbackState == Player.STATE_READY) {
//                delayMs = 1000 - progress % 1000
//                if (delayMs < 200) {
//                    delayMs += 1000
//                }
//            } else {
//                delayMs = 1000
//            }
//            handler.postDelayed(updateNowPlayingAction, delayMs)
//        }
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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        initializeController()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver, IntentFilter(
                PLAYER_EVENT_INTENT
            )
        )
    }

    override fun onStop() {
        MediaController.releaseFuture(controllerFuture)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onStop()
    }
}

