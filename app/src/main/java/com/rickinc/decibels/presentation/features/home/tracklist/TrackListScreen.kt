package com.rickinc.decibels.presentation.features.home.tracklist

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.components.accomponistpermision.findActivity
import com.rickinc.decibels.presentation.components.accomponistpermision.isPermissionPermanentlyDenied
import com.rickinc.decibels.presentation.components.accomponistpermision.setShouldShowRationaleStatus
import com.rickinc.decibels.presentation.features.home.tracklist.components.InfoText
import com.rickinc.decibels.presentation.features.home.tracklist.components.NowPlayingPreview
import com.rickinc.decibels.presentation.features.home.tracklist.components.PermissionRequiredBody
import com.rickinc.decibels.presentation.features.home.tracklist.components.TrackItem
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingState
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingViewModel
import com.rickinc.decibels.presentation.theme.light_title
import com.rickinc.decibels.presentation.util.LocalController
import com.rickinc.decibels.presentation.util.hasPermission
import com.rickinc.decibels.presentation.util.openAppSettings
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackListScreen(goToNowPlayingScreen: (Long) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TrackListTopAppBar() }
    ) { padding ->
        val context = LocalContext.current
        val permissionString = getRequiredPermission()
        var hasStoragePermission by remember { mutableStateOf(context.hasPermission(permissionString)) }
        var shouldShowRationale by remember { mutableStateOf(false) }
        val preferences = koinInject<SharedPreferences>()
        val isPermanentlyDenied: () -> Boolean =
            { isPermissionPermanentlyDenied(context, preferences, permissionString) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) hasStoragePermission = true
            else setShouldShowRationaleStatus(preferences, permissionString)

            val showRationale = context.findActivity()
                .shouldShowRequestPermissionRationale(permissionString)
            if (showRationale || isPermanentlyDenied()) shouldShowRationale = true
        }

        LifecycleStartEffect(Unit) {
            when {
                isPermanentlyDenied() -> shouldShowRationale = true
                else -> permissionLauncher.launch(permissionString)
            }

            onStopOrDispose { }
        }

        when {
            hasStoragePermission -> TrackListBody(padding, goToNowPlayingScreen)
            shouldShowRationale -> PermissionRequiredBody(
                isPermanentlyDenied = isPermanentlyDenied(),
                onClick = {
                    shouldShowRationale = false

                    if (isPermanentlyDenied()) context.openAppSettings()
                    else permissionLauncher.launch(permissionString)
                }
            )
        }
    }
}

@Composable
private fun TrackListBody(
    innerPadding: PaddingValues,
    goToNowPlayingScreen: (Long) -> Unit
) {
    Box(modifier = Modifier.padding(innerPadding)) {
        val viewModel: TrackListViewModel = koinViewModel()
        val nowPlayingViewModel: NowPlayingViewModel = koinViewModel()
        val trackListScreenState = viewModel.uiState.collectAsStateWithLifecycle().value
        val nowPlayingState = nowPlayingViewModel.uiState.collectAsStateWithLifecycle().value

        LaunchedEffect(key1 = Unit) {
            viewModel.getAudioFiles()
        }

        when (trackListScreenState) {
            is TrackListState.Content -> TrackList(
                tracks = trackListScreenState.tracks,
                nowPlayingState = nowPlayingState,
                goToNowPlayingScreen = goToNowPlayingScreen,
            )

            is TrackListState.Error -> InfoText(R.string.error_loading_audio_files)
            else -> {}
        }
    }
}

@Composable
private fun TrackListTopAppBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = light_title
        )
    )
}

@Composable
private fun TrackList(
    tracks: List<TrackItem>,
    nowPlayingState: NowPlayingState?,
    goToNowPlayingScreen: (Long) -> Unit,
) {
    if (tracks.isEmpty()) InfoText(stringResource = R.string.empty_track_list)
    else {
        val trackContentDescription = stringResource(id = R.string.track_list)
        val mediaController = LocalController.current

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
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

private fun getRequiredPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE
}