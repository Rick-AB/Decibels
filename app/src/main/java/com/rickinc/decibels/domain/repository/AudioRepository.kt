package com.rickinc.decibels.domain.repository

import com.rickinc.decibels.domain.model.NowPlaying
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    suspend fun getAudioFiles(): Result<List<Track>>
    suspend fun updateNowPlaying(nowPlaying: NowPlaying)
    fun getNowPlayingFlow(): Flow<NowPlaying?>
}