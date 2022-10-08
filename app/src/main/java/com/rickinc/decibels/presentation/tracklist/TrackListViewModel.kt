package com.rickinc.decibels.presentation.tracklist

import com.rickinc.decibels.domain.repository.AudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrackListViewModel(
    private val audioRepo: AudioRepository
) {
    private val _uiState = MutableStateFlow<TrackListState>(TrackListState.Loading)
    val uiState = _uiState.asStateFlow()

    fun getAudioFiles() {
        val result = audioRepo.getAudioFiles()
        result.fold(
            onSuccess = { tracks ->
                _uiState.update { TrackListState.DataLoaded(tracks) }
            },
            onFailure = {
                _uiState.update { TrackListState.Error }
            })
    }
}
