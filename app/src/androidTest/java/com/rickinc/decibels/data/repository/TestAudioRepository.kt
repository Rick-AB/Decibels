package com.rickinc.decibels.data.repository

import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.model.Track

class TestAudioRepository : AudioRepository {
    var shouldThrowException: Boolean = false
    override suspend fun getAudioFiles(): Result<List<Track>> {
        return if (shouldThrowException) Result.Error("Error reading audio files")
        else Result.Success(Track.getUniqueTrackList())
    }
}
