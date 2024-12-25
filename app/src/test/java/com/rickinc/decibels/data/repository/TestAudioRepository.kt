package com.rickinc.decibels.data.repository

import android.content.Context
import com.rickinc.decibels.domain.exception.ErrorHolder
import com.rickinc.decibels.domain.model.NowPlaying
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.model.Track
import kotlinx.coroutines.flow.Flow

class TestAudioRepository : AudioRepository {
    var shouldThrowException: Boolean = false
    override suspend fun getAudioFiles(): Result<List<Track>> {
        return if (shouldThrowException) Result.Error(ErrorHolder.Local("Error reading audio files"))
        else Result.Success(Track.getUniqueTrackList())
    }

    override suspend fun updateNowPlaying(nowPlaying: NowPlaying) {
        TODO("Not yet implemented")
    }

    override fun getNowPlayingFlow(): Flow<NowPlaying?> {
        TODO("Not yet implemented")
    }

    override fun deleteTrack(context: Context, track: Track) {
        TODO("Not yet implemented")
    }

    override suspend fun getLyricsForTrack(context: Context, track: Track): Result<String> {
        TODO("Not yet implemented")
    }
}
