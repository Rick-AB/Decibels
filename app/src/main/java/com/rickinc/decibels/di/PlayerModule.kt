package com.rickinc.decibels.di

import android.Manifest
import android.os.PowerManager
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.rickinc.decibels.domain.util.TrackConverter
import com.rickinc.decibels.presentation.util.hasPermission
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val playerModule = module {
    single<Player> {
        val context = androidContext()
        val hasWakeLockPermission = context.hasPermission(Manifest.permission.WAKE_LOCK)
        ExoPlayer.Builder(context)
            .apply {
                if (hasWakeLockPermission) setWakeMode(PowerManager.PARTIAL_WAKE_LOCK)
            }
            .build()
    }

    factoryOf(::TrackConverter)
}
