package com.rickinc.decibels.presentation.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
) : ViewModel() {
    private val errorFlow = MutableStateFlow<PlaybackException?>(null)
    private val progressFlow = MutableStateFlow(0L)
    private val playbackStateFlow = MutableStateFlow(-1)
    private val nowPlayingFlow = audioRepository.getNowPlayingFlow()
    val uiState =
        combine(
            nowPlayingFlow,
            progressFlow,
            playbackStateFlow,
            errorFlow
        ) { nowPlaying, progress, playbackState, exception ->
            when {
                nowPlaying != null -> {
                    NowPlayingState.TrackLoaded(
                        nowPlaying.track,
                        nowPlaying.isPlaying,
                        nowPlaying.repeatMode,
                        nowPlaying.shuffleActive,
                        progress,
                        playbackState
                    )
                }

                exception != null -> {
                    val errorMessage = exception.localizedMessage ?: ""
                    NowPlayingState.ErrorLoadingTrack(Result.Error(errorMessage))
                }

                else -> null
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    private val _bottomSheetUiState =
        MutableStateFlow<NowPlayingBottomSheetState>(NowPlayingBottomSheetState.Loading)
    val bottomSheetUiState = _bottomSheetUiState.asStateFlow()

    fun onEvent(event: NowPlayingEvent) {
        when (event) {
            is NowPlayingEvent.OnError -> errorFlow.update { event.error }
            is NowPlayingEvent.OnProgressChanged -> progressFlow.update { event.progress }
            is NowPlayingEvent.OnPlaybackStateChanged -> playbackStateFlow.update { event.playbackState }
            is NowPlayingEvent.OnTrackChanged -> getLyricsForTrack(event.track)
        }
    }

    private fun getLyricsForTrack(track: Track) {
        viewModelScope.launch {
            _bottomSheetUiState.update { NowPlayingBottomSheetState.Loading }
            val result = audioRepository.getLyricsForTrack(track)
            result.fold(
                onSuccess = { lyrics ->
                    _bottomSheetUiState.update { NowPlayingBottomSheetState.LyricsLoaded(lyrics) }
                },
                onFailure = { error ->
                    _bottomSheetUiState.update { NowPlayingBottomSheetState.ErrorLoadingLyrics(error.errorMessage) }
                }
            )
        }
    }
}