package com.rickinc.decibels.presentation.features.tracklist

import android.app.Application
import android.database.ContentObserver
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.util.TrackConverter
import com.rickinc.decibels.presentation.util.registerObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val application: Application,
    private val audioRepo: AudioRepository,
    private val trackConverter: TrackConverter
) : ViewModel() {

    private val _nowPlayingTrack = MutableStateFlow<Track?>(null)
    val nowPlayingTrack = _nowPlayingTrack.asStateFlow()

    private val _uiState = MutableStateFlow<TrackListState>(TrackListState.Loading)
    val uiState = _uiState.asStateFlow()

    private val tracks = mutableListOf<Track>()
    private var contentObserver: ContentObserver

    init {
        contentObserver =
            application.contentResolver.registerObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                getAudioFiles()
            }
    }

    fun getAudioFiles() {
        viewModelScope.launch {
            val result = audioRepo.getAudioFiles()
            result.fold(
                onSuccess = { tracks ->
                    val tracksAsMediaItems = trackConverter.toMediaItems(tracks)
                    this@TrackListViewModel.tracks.addAll(tracks)
                    _uiState.update { TrackListState.DataLoaded(tracks, tracksAsMediaItems) }
                },
                onFailure = { error ->
                    _uiState.update { TrackListState.Error(error.message) }
                }
            )
        }
    }

    fun setNowPlaying(selectedTrack: Track) = _nowPlayingTrack.update { selectedTrack }

    override fun onCleared() {
        super.onCleared()
        application.contentResolver.unregisterContentObserver(contentObserver)
    }
}
