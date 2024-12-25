package com.rickinc.decibels.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NowPlaying(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val isPlaying: Boolean,
    val repeatMode: Int,
    val shuffleActive: Boolean
)
