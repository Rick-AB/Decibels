package com.rickinc.decibels.presentation.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val player: Player,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NowPlayingState?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        player.prepare()
    }

    fun setSelectedTrack(trackId: String) {
        viewModelScope.launch {
            val result = audioRepository.getSingleAudioFile(trackId)
            result.fold(
                onSuccess = { track ->
                    _uiState.update { NowPlayingState.TrackLoaded(track) }
                    play(track)
                },
                onFailure = { error ->
                    _uiState.update { NowPlayingState.ErrorLoadingTrack(error) }
                }
            )
        }
    }

    private fun play(track: Track) {
        track.contentUri?.let {
            player.setMediaItem(MediaItem.fromUri(it))
            player.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}