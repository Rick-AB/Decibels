package com.rickinc.decibels.domain.service

import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.NotificationUtil.IMPORTANCE_HIGH
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.util.TrackConverter.Companion.CONTENT_URI_KEY
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DecibelPlaybackService : MediaSessionService(), MediaSession.Callback {

    companion object {
        lateinit var binder: Binder
    }

    @Inject
    lateinit var player: Player
    private var mediaSession: MediaSession? = null
    private lateinit var notificationManager: PlayerNotificationManager

    override fun onCreate() {
        super.onCreate()
        initMediaSession()
        setPlayerAttributes()
//        initNotificationManager()
    }

    private fun initMediaSession() {
        mediaSession = MediaSession.Builder(this, player).setCallback(this).build()
    }

    private fun setPlayerAttributes() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        (player as ExoPlayer).setAudioAttributes(audioAttributes, true)
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun initNotificationManager() {
        val channelId = resources.getString(R.string.appName) + "Music channel"
        val notificationID = 1998
        notificationManager = PlayerNotificationManager.Builder(this, notificationID, channelId)
            .setChannelImportance(IMPORTANCE_HIGH)
            .setSmallIconResourceId(R.drawable.ic_baseline_audio_file_24)
            .setChannelDescriptionResourceId(R.string.appName)
            .setNextActionIconResourceId(R.drawable.ic_next)
            .setPreviousActionIconResourceId(R.drawable.ic_previous)
            .setPauseActionIconResourceId(R.drawable.ic_pause)
            .setPlayActionIconResourceId(R.drawable.ic_play)
            .setChannelNameResourceId(R.string.appName)
            .build()

        notificationManager.setPlayer(player)
        notificationManager.setPriority(NotificationCompat.PRIORITY_MAX)
        notificationManager.setUseRewindAction(false)
        notificationManager.setUseFastForwardAction(false)
    }

    override fun onBind(intent: Intent?): IBinder? {
        binder = LocalBinder()
        return super.onBind(intent)
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        val updatedMediaItems =
            mediaItems.map { mediaItem ->
                val extra = mediaItem.mediaMetadata.extras
                val uri = extra?.getString(CONTENT_URI_KEY)
                mediaItem.buildUpon().setUri(Uri.parse(uri)).build()
            }.toMutableList()

        return Futures.immediateFuture(updatedMediaItems)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
            if (::notificationManager.isInitialized) notificationManager.setPlayer(null)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@DecibelPlaybackService
    }
}