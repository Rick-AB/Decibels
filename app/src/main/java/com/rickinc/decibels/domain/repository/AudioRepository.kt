package com.rickinc.decibels.domain.repository

import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.presentation.model.Track

interface AudioRepository {
    fun getAudioFiles(): Result<List<Track>>
}