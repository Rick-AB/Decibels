package com.rickinc.decibels.presentation.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.util.TrackConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val trackConverter: TrackConverter
) : ViewModel() {
    private val isPlayingFlow = MutableStateFlow(false)
    private val isShuffleActiveFlow = MutableStateFlow(false)
    private val repeatModeFlow = MutableStateFlow(Player.REPEAT_MODE_OFF)
    private val currentTrackFlow = MutableStateFlow<Track?>(null)
    private val errorFlow = MutableStateFlow<PlaybackException?>(null)
    private val progressFlow = MutableStateFlow(0L)

    val uiState = combine(
        isPlayingFlow,
        isShuffleActiveFlow,
        repeatModeFlow,
        currentTrackFlow,
        progressFlow,
        errorFlow
    ) { valuesArray ->
        if (valuesArray[3] != null) {
            NowPlayingState.TrackLoaded(
                valuesArray[3] as Track,
                valuesArray[0] as Boolean,
                valuesArray[2] as Int,
                valuesArray[1] as Boolean,
                valuesArray[4] as Long
            )
        } else {
            if (valuesArray[5] != null) {
                NowPlayingState.ErrorLoadingTrack(
                    Result.Error(
                        (valuesArray[5] as PlaybackException).localizedMessage ?: ""
                    )
                )
            } else {
                NowPlayingState.ErrorLoadingTrack(
                    Result.Error("Something went wrong")
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    fun onEvent(event: NowPlayingEvent) {
        when (event) {
            is NowPlayingEvent.OnIsPlayingChanged -> isPlayingFlow.update { event.isPlaying }
            is NowPlayingEvent.OnMediaItemChanged ->
                event.mediaItem?.let { mediaItem ->
                    currentTrackFlow.update {
                        trackConverter.toTrack(
                            mediaItem
                        )
                    }
                }

            is NowPlayingEvent.OnRepeatModeChanged -> repeatModeFlow.update { event.repeatMode }
            is NowPlayingEvent.OnShuffleActiveChanged -> isShuffleActiveFlow.update { event.shuffleActive }
            is NowPlayingEvent.OnError -> errorFlow.update { event.error }
            is NowPlayingEvent.OnProgressChanged -> progressFlow.update { event.progress }


        }
    }
}