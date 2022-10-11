package com.rickinc.decibels.presentation.tracklist

import androidx.lifecycle.ViewModel
import com.rickinc.decibels.domain.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val audioRepo: AudioRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<TrackListState>(TrackListState.Loading)
    val uiState = _uiState.asStateFlow()

    fun getAudioFiles() {
        val result = audioRepo.getAudioFiles()
        result.fold(
            onSuccess = { tracks ->
                _uiState.update { TrackListState.DataLoaded(tracks) }
            },
            onFailure = { error ->
                _uiState.update { TrackListState.Error(error.errorMessage) }
            })
    }
}
