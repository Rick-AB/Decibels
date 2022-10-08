package com.rickinc.decibels.data.repository

import com.rickinc.decibels.domain.exception.ReadAudioFilesException
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.presentation.model.Track

class TestAudioRepository : AudioRepository {
    var shouldThrowException: Boolean = false
    override fun getAudioFiles(): Result<List<Track>> {
        if (shouldThrowException) throw ReadAudioFilesException()
        else return Result.Success(Track.createDummyTracks())
    }
}
