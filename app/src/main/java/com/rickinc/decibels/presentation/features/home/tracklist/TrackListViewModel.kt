package com.rickinc.decibels.presentation.features.home.tracklist

import android.app.Application
import android.database.ContentObserver
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.TrackRepository
import com.rickinc.decibels.domain.util.TrackConverter
import com.rickinc.decibels.presentation.util.registerObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackListViewModel(
    private val application: Application,
    private val audioRepo: TrackRepository,
    private val trackConverter: TrackConverter
) : ViewModel() {

    private val _nowPlayingTrack = MutableStateFlow<Track?>(null)
    val nowPlayingTrack = _nowPlayingTrack.asStateFlow()

    private val tracksFlow = MutableStateFlow(emptyList<Track>())
    private val mediaItemsFlow = tracksFlow.mapLatest { tracks ->
        trackConverter.toMediaItems(tracks)
    }.flowOn(Dispatchers.IO)

    val state: StateFlow<TrackListState> = tracksFlow.mapLatest { tracks ->
        if (tracks.isEmpty()) return@mapLatest TrackListState.Empty
        val trackItems = tracks.map { track ->
            TrackItem(
                id = track.id,
                title = track.title,
                trackLength = track.trackLength,
                artist = track.artist,
                albumId = track.albumId,
                contentUri = track.contentUri,
                mediaItem = trackConverter.trackToMediaItem(track),
                thumbnailUri = track.thumbnailUri
            )
        }
        TrackListState.Content(trackItems)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TrackListState.Loading
    )

    private var contentObserver: ContentObserver

    init {
        contentObserver = application
            .contentResolver
            .registerObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                getAudioFiles()
            }
    }

    fun onEvent(event: TrackListEvent) {
        when (event) {
            is TrackListEvent.PlayTrack -> {}
        }
    }

    fun getAudioFiles() {
        viewModelScope.launch {
            val result = audioRepo.getAudioFiles()
            result.fold(
                onSuccess = { tracks ->
                    tracksFlow.update { tracks.toList() }
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
