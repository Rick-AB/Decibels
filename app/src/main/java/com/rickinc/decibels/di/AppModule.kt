package com.rickinc.decibels.di

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.PowerManager
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.preference.PreferenceManager
import com.rickinc.decibels.presentation.util.hasPermission
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providePlayer(
        @ApplicationContext context: Context
    ): Player {
        val hasWakeLockPermission = context.hasPermission(Manifest.permission.WAKE_LOCK)
        return ExoPlayer.Builder(context)
            .apply {
                if (hasWakeLockPermission) setWakeMode(PowerManager.PARTIAL_WAKE_LOCK)
            }
            .build()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SharedPreferencesEntryPoint {
        val sharedPreferences: SharedPreferences
    }
}