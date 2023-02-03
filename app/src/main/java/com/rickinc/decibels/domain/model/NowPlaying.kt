package com.rickinc.decibels.domain.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NowPlaying(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    @Embedded
    val track: Track,
    val isPlaying: Boolean,
    val repeatMode: Int,
    val shuffleActive: Boolean
)
