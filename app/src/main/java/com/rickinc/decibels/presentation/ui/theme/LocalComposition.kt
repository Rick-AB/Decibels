package com.rickinc.decibels.presentation.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController

val LocalController = staticCompositionLocalOf<MediaController?> {
    throw RuntimeException("Controller not initialized yet")
}

val LocalPlayer = staticCompositionLocalOf<Player> {
    throw RuntimeException("Player not initialized yet")
}