package com.rickinc.decibels.di

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun providePlayer(
        @ApplicationContext context: Context
    ): Player {
        return ExoPlayer.Builder(context).build()
    }

//    @Provides
//    fun provideMediaSession(
//        @ApplicationContext context: Context,
//        player: ExoPlayer
//    ): MediaSession {
//        return MediaSession.Builder(context, player).build()
//    }
}