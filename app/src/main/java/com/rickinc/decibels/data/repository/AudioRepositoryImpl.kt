package com.rickinc.decibels.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.rickinc.decibels.data.datasource.local.database.DecibelsDatabase
import com.rickinc.decibels.data.datasource.local.device.DeviceDataSource
import com.rickinc.decibels.data.datasource.network.LyricsScraper
import com.rickinc.decibels.domain.exception.LyricsNotFoundException
import com.rickinc.decibels.domain.model.NowPlaying
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import timber.log.Timber


class AudioRepositoryImpl(
    private val deviceDataSource: DeviceDataSource,
    private val lyricsScraper: LyricsScraper,
    decibelsDatabase: DecibelsDatabase
) : AudioRepository {
    private val dao = decibelsDatabase.dao

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun getAudioFiles(): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            Result.Success(deviceDataSource.getDeviceAudioFiles())
        }
    }

    override suspend fun updateNowPlaying(nowPlaying: NowPlaying) {
        dao.updateNowPlaying(nowPlaying)
    }

    override fun getNowPlayingFlow(): Flow<NowPlaying?> = dao.getNowPlaying()

    override fun deleteTrack(context: Context, track: Track) {
        deviceDataSource.deleteAudioFileFromDevice(context, track)
    }

    override suspend fun getLyricsForTrack(track: Track): Result<String> {
        return try {
            Result.Success(lyricsScraper.getLyrics(track))
        } catch (e: Exception) {
            Timber.e(e)
            when (e) {
                is LyricsNotFoundException -> {
                    Result.Error("Could not find lyrics for the song ${track.trackTitle}")
                }
                else -> Result.Error("")
            }
        }
    }
}