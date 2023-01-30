package com.rickinc.decibels.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NowPlaying(
    val track: Track,
    val isPlaying: Boolean,
    val repeatMode: Int,
    val shuffleActive: Boolean
)
