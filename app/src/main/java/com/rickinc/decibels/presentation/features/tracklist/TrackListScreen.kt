package com.rickinc.decibels.presentation.features.tracklist

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.components.DefaultTopAppBar
import com.rickinc.decibels.presentation.components.accomponistpermision.findActivity
import com.rickinc.decibels.presentation.components.accomponistpermision.isPermissionPermanentlyDenied
import com.rickinc.decibels.presentation.components.accomponistpermision.setShouldShowRationaleStatus
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingState
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingViewModel
import com.rickinc.decibels.presentation.features.tracklist.components.InfoText
import com.rickinc.decibels.presentation.features.tracklist.components.NowPlayingPreview
import com.rickinc.decibels.presentation.features.tracklist.components.PermissionRequiredBody
import com.rickinc.decibels.presentation.features.tracklist.components.TrackItem
import com.rickinc.decibels.presentation.theme.LocalController
import com.rickinc.decibels.presentation.util.hasPermission
import com.rickinc.decibels.presentation.util.openAppSettings
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackListScreen(goToNowPlayingScreen: (Track) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TrackListTopAppBar() }
    ) { padding ->
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
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

            val showRationale =
                context.findActivity()
                    .shouldShowRequestPermissionRationale(permissionString)
            if (showRationale || isPermanentlyDenied()) shouldShowRationale = true
        }

        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    when {
                        isPermanentlyDenied() -> shouldShowRationale = true
                        else -> permissionLauncher.launch(permissionString)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        when {
            hasStoragePermission -> TrackListBody(padding, goToNowPlayingScreen)
            shouldShowRationale -> PermissionRequiredBody(isPermanentlyDenied = isPermanentlyDenied()) {
                shouldShowRationale = false

                if (isPermanentlyDenied()) context.openAppSettings()
                else permissionLauncher.launch(permissionString)
            }
        }
    }
}

@Composable
fun TrackListBody(
    innerPadding: PaddingValues,
    onTrackItemClick: (Track) -> Unit
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
            is TrackListState.DataLoaded -> TrackList(
                tracks = trackListScreenState.tracks,
                tracksAsMediaItems = trackListScreenState.tracksAsMediaItems,
                nowPlayingState = nowPlayingState,
                actionNavigateToNowPlaying = onTrackItemClick,
            )

            is TrackListState.Error -> InfoText(R.string.error_loading_audio_files)
            else -> {}
        }

    }
}

@Composable
fun TrackListTopAppBar() {
    DefaultTopAppBar(title = stringResource(id = R.string.app_name))
}

@Composable
fun TrackList(
    tracks: List<Track>,
    tracksAsMediaItems: List<MediaItem>,
    nowPlayingState: NowPlayingState?,
    actionNavigateToNowPlaying: (Track) -> Unit,
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
                    key = { _, track -> track.trackId }
                ) { index, track ->
                    val trackAsMediaItem = tracksAsMediaItems[index]

                    val actionTrackClick = {
                        playTrack(mediaController, trackAsMediaItem)
                        addPlaylist(mediaController, index, tracksAsMediaItems)
                        actionNavigateToNowPlaying(track)
                    }

                    TrackItem(
                        track = track,
                        trackAsMediaItem = trackAsMediaItem,
                        mediaController = mediaController,
                        modifier = Modifier.animateItem(),
                        onClick = actionTrackClick
                    )
                }
            }

            if (nowPlayingState is NowPlayingState.TrackLoaded) {
                NowPlayingPreview(nowPlayingState, Modifier.align(Alignment.BottomCenter)) {
                    actionNavigateToNowPlaying(nowPlayingState.track)
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

private fun addPlaylist(
    mediaController: MediaController?,
    currentTrackIndex: Int,
    tracksAsMediaItems: List<MediaItem>,
) {
    val tracksBeforeCurrent = tracksAsMediaItems.subList(0, currentTrackIndex)
    mediaController?.addMediaItems(0, tracksBeforeCurrent)

    val tracksAfterCurrent =
        tracksAsMediaItems.subList(currentTrackIndex + 1, tracksAsMediaItems.lastIndex)
    mediaController?.addMediaItems(tracksAfterCurrent)
}

private fun getRequiredPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE
}