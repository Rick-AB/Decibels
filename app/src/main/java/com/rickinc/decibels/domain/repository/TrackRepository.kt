package com.rickinc.decibels.domain.repository

import android.content.Context
import android.net.Uri
import com.rickinc.decibels.domain.model.NowPlaying
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    suspend fun getAudioFiles(): Result<List<Track>>
    suspend fun updateNowPlaying(nowPlaying: NowPlaying)
    fun getNowPlayingFlow(): Flow<NowPlaying?>
    fun deleteTrack(context: Context, trackId: Long, contentUri: Uri)
    suspend fun getLyricsForTrack(context: Context, track: Track): Result<String>
}