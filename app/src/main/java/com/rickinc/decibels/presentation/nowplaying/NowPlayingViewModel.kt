package com.rickinc.decibels.presentation.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.util.TrackConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val trackConverter: TrackConverter
) : ViewModel() {
    private val isPlayingFlow = MutableStateFlow(false)
    private val isShuffleActiveFlow = MutableStateFlow(false)
    private val repeatModeFlow = MutableStateFlow(RepeatMode.REPEAT_MODE_OFF)
    private val currentTrackFlow = MutableStateFlow<Track?>(null)
    private val errorFlow = MutableStateFlow<PlaybackException?>(null)

    private val _uiState = MutableStateFlow<NowPlayingState?>(null)
    val uiState = combine(
        isPlayingFlow,
        isShuffleActiveFlow,
        repeatModeFlow,
        currentTrackFlow,
        errorFlow
    ) { isPlaying, isShuffleActive, repeatMode, currentTrack, error ->
        Timber.d("CURRENNENRNE::::$currentTrack")
        if (currentTrack != null) {
            NowPlayingState.TrackLoaded(
                currentTrack,
                isPlaying,
                repeatMode,
                isShuffleActive,
            )
        } else NowPlayingState.ErrorLoadingTrack(Result.Error(error?.localizedMessage.toString()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    fun onEvent(event: NowPlayingEvent) {
        when (event) {
            is NowPlayingEvent.OnIsPlayingChanged -> isPlayingFlow.update { it }
            is NowPlayingEvent.OnMediaItemChanged ->
                event.mediaItem?.let { mediaItem ->
                    currentTrackFlow.update {
                        trackConverter.toTrack(
                            mediaItem
                        )
                    }
                }

            is NowPlayingEvent.OnRepeatModeChanged -> repeatModeFlow.update { it }
            is NowPlayingEvent.OnShuffleActiveChanged -> isShuffleActiveFlow.update { it }
            is NowPlayingEvent.OnError -> errorFlow.update { it }
        }
    }

    enum class RepeatMode {
        REPEAT_MODE_OFF, REPEAT_MODE_ONCE, REPEAT_MODE_INFINITE
    }
}