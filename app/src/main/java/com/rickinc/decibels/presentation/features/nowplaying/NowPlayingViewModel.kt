package com.rickinc.decibels.presentation.features.nowplaying

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import com.rickinc.decibels.domain.exception.ErrorHolder
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NowPlayingViewModel(private val audioRepository: AudioRepository) : ViewModel() {
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
//                nowPlaying != null -> {
//                    NowPlayingState.TrackLoaded(
//                        nowPlaying.track,
//                        nowPlaying.isPlaying,
//                        nowPlaying.repeatMode,
//                        nowPlaying.shuffleActive,
//                        progress,
//                        playbackState
//                    )
//                }

                exception != null -> {
                    val errorMessage = exception.localizedMessage ?: ""
                    NowPlayingState.ErrorLoadingTrack(ErrorHolder.Local(errorMessage))
                }

                else -> null
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    private val _bottomSheetUiState =
        MutableStateFlow<NowPlayingBottomSheetState>(NowPlayingBottomSheetState.Loading)
    val bottomSheetUiState = _bottomSheetUiState.asStateFlow()

    private var lyricsJob = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }

    fun onEvent(event: NowPlayingEvent) {
        when (event) {
            is NowPlayingEvent.OnError -> errorFlow.update { event.error }
            is NowPlayingEvent.OnProgressChanged -> progressFlow.update { event.progress }
            is NowPlayingEvent.OnPlaybackStateChanged -> playbackStateFlow.update { event.playbackState }
            is NowPlayingEvent.OnGetLyrics -> getLyricsForTrack(event.context, event.track)
        }
    }

    private fun getLyricsForTrack(context: Context, track: Track) {
        lyricsJob.cancel()
        viewModelScope.launch(lyricsJob) {
            _bottomSheetUiState.update { NowPlayingBottomSheetState.Loading }
            val result = audioRepository.getLyricsForTrack(context, track)
            result.fold(
                onSuccess = { lyrics ->
                    _bottomSheetUiState.update { NowPlayingBottomSheetState.LyricsLoaded(lyrics) }
                },
                onFailure = { error ->
                    _bottomSheetUiState.update { NowPlayingBottomSheetState.ErrorLoadingLyrics(error) }
                }
            )
        }
    }
}