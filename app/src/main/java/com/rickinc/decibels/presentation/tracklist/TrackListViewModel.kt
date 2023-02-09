package com.rickinc.decibels.presentation.tracklist

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickinc.decibels.BuildConfig
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.util.TrackConverter
import com.rickinc.decibels.presentation.util.getRealPathFromURI
import com.rickinc.decibels.presentation.util.registerObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
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
                    _uiState.update { TrackListState.Error(error.errorMessage) }
                }
            )
        }
    }

    fun setNowPlaying(selectedTrack: Track) = _nowPlayingTrack.update { selectedTrack }

    fun deleteTrack(context: Context, track: Track): Pair<Track, IntentSenderRequest>? {
        try {
            audioRepo.deleteTrack(context, track)
        } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val recoverableSecurityException =
                    securityException as? RecoverableSecurityException
                        ?: throw securityException

                val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                return Pair(track, intentSenderRequest)
            } else {
                throw securityException
            }
        }
        return null
    }

    fun shareFile(context: Context, track: Track) {
        val trackPath = track.contentUri?.let {
            getRealPathFromURI(context, it)
        } ?: return
        val requestFile = File(trackPath)

        // Use the FileProvider to get a content URI
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider",
            requestFile
        )

        Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            setDataAndType(fileUri, context.contentResolver.getType(fileUri))
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.also {
            context.startActivity(Intent.createChooser(it, "Share Audio File"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        application.contentResolver.unregisterContentObserver(contentObserver)
    }
}
