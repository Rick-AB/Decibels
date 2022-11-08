package com.rickinc.decibels.presentation.tracklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.util.TrackConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val audioRepo: AudioRepository,
    private val trackConverter: TrackConverter
) : ViewModel() {

    private val _nowPlayingTrack = MutableStateFlow<Track?>(null)
    val nowPlayingTrack = _nowPlayingTrack.asStateFlow()

    private val _uiState = MutableStateFlow<TrackListState>(TrackListState.Loading)
    val uiState = _uiState.asStateFlow()

    private val tracks = mutableListOf<Track>()

    fun getAudioFiles() {
        viewModelScope.launch {
            val result = audioRepo.getAudioFiles()
            result.fold(
                onSuccess = { tracks ->
                    _uiState.update { TrackListState.DataLoaded(tracks) }
                    this@TrackListViewModel.tracks.addAll(tracks)
                },
                onFailure = { error ->
                    _uiState.update { TrackListState.Error(error.errorMessage) }
                }
            )
        }
    }

    fun setNowPlaying(selectedTrack: Track) {
        _nowPlayingTrack.update { selectedTrack }
    }

    fun getMediaItem(track: Track): MediaItem {
        return trackConverter.toMediaItem(track)
    }

    fun getMediaItems(tracks: List<Track>): List<MediaItem> {
        return trackConverter.toMediaItems(tracks)
    }

    fun getIndexOfTrack(trackId: Long): Int {
        return tracks.indexOfFirst { it.trackId == trackId }
    }
}
