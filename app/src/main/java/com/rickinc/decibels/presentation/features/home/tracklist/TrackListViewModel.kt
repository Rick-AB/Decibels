package com.rickinc.decibels.presentation.features.home.tracklist

import android.app.Application
import android.database.ContentObserver
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.TrackRepository
import com.rickinc.decibels.domain.util.TrackConverter
import com.rickinc.decibels.presentation.util.registerObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackListViewModel(
    private val application: Application,
    private val audioRepo: TrackRepository,
    private val trackConverter: TrackConverter,
) : ViewModel() {

    private val tracksFlow = MutableStateFlow(emptyList<Track>())
    val state: StateFlow<TrackListState> = tracksFlow.mapLatest { tracks ->
        if (tracks.isEmpty()) return@mapLatest TrackListState.Empty
        TrackListState.Content(tracks)
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
            is TrackListEvent.PlayTrack -> playTrack(event.track, event.mediaController)
        }
    }

    private fun playTrack(track: Track, mediaController: MediaController) {
        mediaController.clearMediaItems()
        mediaController.setMediaItem(trackConverter.trackToMediaItem(track))
        mediaController.prepare()
        mediaController.play()

        setQueue(track, mediaController)
    }

    private fun setQueue(selectedTrack: Track, mediaController: MediaController) {
        val tracks = tracksFlow.value
        val indexOfSelectedTrack = tracks.indexOf(selectedTrack)
        val mediaItems = trackConverter.toMediaItems(tracks)
        val tracksBeforeCurrent = mediaItems.subList(0, indexOfSelectedTrack)
        val tracksAfterCurrent = mediaItems.subList(indexOfSelectedTrack + 1, mediaItems.lastIndex)
        mediaController.addMediaItems(0, tracksBeforeCurrent)
        mediaController.addMediaItems(tracksAfterCurrent)
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

    override fun onCleared() {
        super.onCleared()
        application.contentResolver.unregisterContentObserver(contentObserver)
    }
}
