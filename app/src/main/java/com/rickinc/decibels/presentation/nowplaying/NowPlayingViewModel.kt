package com.rickinc.decibels.presentation.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickinc.decibels.domain.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NowPlayingState?>(null)
    val uiState = _uiState.asStateFlow()

    fun setSelectedTrack(trackId: String) {
        viewModelScope.launch {
            val result = audioRepository.getSingleAudioFile(trackId)
            result.fold(
                onSuccess = { track ->
                    _uiState.update { NowPlayingState.TrackLoaded(track) }
                    Timber.d("#################################${track.thumbnail}")
                },
                onFailure = { error ->
                    _uiState.update { NowPlayingState.ErrorLoadingTrack(error) }
                }
            )
        }
    }
}