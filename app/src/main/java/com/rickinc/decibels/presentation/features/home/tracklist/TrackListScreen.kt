package com.rickinc.decibels.presentation.features.home.tracklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.features.home.tracklist.components.InfoText
import com.rickinc.decibels.presentation.features.home.tracklist.components.NowPlayingPreview
import com.rickinc.decibels.presentation.features.home.tracklist.components.TrackItem
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingState
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingViewModel
import com.rickinc.decibels.presentation.util.LocalController
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackListScreen(
    trackListState: TrackListState,
    goToNowPlayingScreen: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val viewModel: TrackListViewModel = koinViewModel()
        val nowPlayingViewModel: NowPlayingViewModel = koinViewModel()
        val nowPlayingState = nowPlayingViewModel.uiState.collectAsStateWithLifecycle().value

        LaunchedEffect(key1 = Unit) {
            viewModel.getAudioFiles()
        }

        when (trackListState) {
            is TrackListState.Loading -> {}
            is TrackListState.Content -> TrackList(
                tracks = trackListState.tracks,
                nowPlayingState = nowPlayingState,
                goToNowPlayingScreen = goToNowPlayingScreen,
            )

            is TrackListState.Error -> InfoText(R.string.error_loading_audio_files)
            is TrackListState.Empty -> InfoText(R.string.empty_track_list)
        }
    }
}

@Composable
private fun TrackList(
    tracks: List<TrackItem>,
    nowPlayingState: NowPlayingState?,
    goToNowPlayingScreen: (Long) -> Unit,
) {
    val trackContentDescription = stringResource(id = R.string.track_list)
    val mediaController = LocalController.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = trackContentDescription
                }
        ) {
            itemsIndexed(
                items = tracks,
                key = { _, track -> track.id }
            ) { index, track ->

                val actionTrackClick = {
                    playTrack(mediaController, track.mediaItem)
                    //Todo fix addPlaylist(mediaController, index, tracksAsMediaItems)
                    goToNowPlayingScreen(track.id)
                }

                TrackItem(
                    track = track,
                    mediaController = mediaController,
                    modifier = Modifier.animateItem(),
                    onClick = actionTrackClick
                )
            }
        }

        if (nowPlayingState is NowPlayingState.TrackLoaded) {
            NowPlayingPreview(nowPlayingState, Modifier.align(Alignment.BottomCenter)) {
                goToNowPlayingScreen(nowPlayingState.track.id)
            }
        }
    }
}

private fun playTrack(mediaController: MediaController?, mediaItem: MediaItem) {
    mediaController?.clearMediaItems()
    mediaController?.setMediaItem(mediaItem)
    mediaController?.prepare()
    mediaController?.play()
}

private fun setTracksQueue(
    mediaController: MediaController?,
    currentTrackIndex: Int,
    tracksAsMediaItems: List<MediaItem>,
) {
    val tracksBeforeCurrent = tracksAsMediaItems.subList(0, currentTrackIndex)
    mediaController?.addMediaItems(0, tracksBeforeCurrent)

    val tracksAfterCurrent = tracksAsMediaItems
        .subList(currentTrackIndex + 1, tracksAsMediaItems.lastIndex)

    mediaController?.addMediaItems(tracksAfterCurrent)
}