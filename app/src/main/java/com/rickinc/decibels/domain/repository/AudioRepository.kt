package com.rickinc.decibels.domain.repository

import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track

interface AudioRepository {
    suspend fun getAudioFiles(): Result<List<Track>>
    suspend fun getSingleAudioFile(trackId: String): Result<Track>
}