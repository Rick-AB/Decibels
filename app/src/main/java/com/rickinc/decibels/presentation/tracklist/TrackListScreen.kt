package com.rickinc.decibels.presentation.tracklist

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.nowplaying.NowPlayingState
import com.rickinc.decibels.presentation.nowplaying.NowPlayingViewModel
import com.rickinc.decibels.presentation.tracklist.components.InfoText
import com.rickinc.decibels.presentation.tracklist.components.NowPlayingPreview
import com.rickinc.decibels.presentation.tracklist.components.PermissionRequiredBody
import com.rickinc.decibels.presentation.tracklist.components.TrackItem
import com.rickinc.decibels.presentation.ui.components.*
import com.rickinc.decibels.presentation.ui.components.accomponistpermision.findActivity
import com.rickinc.decibels.presentation.ui.theme.LocalController
import com.rickinc.decibels.presentation.util.hasPermission
import com.rickinc.decibels.presentation.util.openAppSettings
import com.rickinc.decibels.presentation.util.showLongToast

@Composable
fun TrackListScreen(onTrackItemClick: (Track) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TrackListTopAppBar() }
    ) { padding ->
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val permissionString = getRequiredPermission()

        var hasStoragePermission by remember { mutableStateOf(context.hasPermission(permissionString)) }
        var shouldShowRationale by remember { mutableStateOf(false) }

        val preferences = requireSharedPreferencesEntryPoint().sharedPreferences
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
            hasStoragePermission -> TrackListBody(padding, onTrackItemClick)
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
        val context = LocalContext.current
        val viewModel: TrackListViewModel = hiltViewModel(context as ComponentActivity)
        val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel(context)
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
                actionDeleteTrack = viewModel::deleteTrack,
                actionShareTrack = viewModel::shareFile,
                onTrackItemClick = onTrackItemClick,
            )
            is TrackListState.Error -> InfoText(R.string.error_loading_audio_files)
            else -> {}
        }

    }
}

@Composable
fun TrackListTopAppBar() {
    DefaultTopAppBar(title = stringResource(id = R.string.appName))
}

@Composable
fun TrackList(
    tracks: List<Track>,
    tracksAsMediaItems: List<MediaItem>,
    nowPlayingState: NowPlayingState?,
    actionDeleteTrack: (Context, Track) -> Pair<Track, IntentSenderRequest>?,
    actionShareTrack: (Context, Track) -> Unit,
    onTrackItemClick: (Track) -> Unit
) {
    if (tracks.isEmpty()) InfoText(stringResource = R.string.empty_track_list)
    else {
        val trackContentDescription = stringResource(id = R.string.track_list)
        val mediaController = LocalController.current
        val context = LocalContext.current
        var trackToDelete: Track? by remember { mutableStateOf(null) }
        var trackAttemptedToDelete: Track? by remember { mutableStateOf(null) }
        val launcher = getDeleteLauncher(trackAttemptedToDelete, actionDeleteTrack)


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
                        onTrackItemClick(track)
                    }

                    val onDeleteClick = {
                        checkVersionAndDelete(
                            context = context,
                            track = track,
                            launcher = launcher,
                            actionShowDeleteDialog = { trackToDelete = track }
                        )
                    }

                    TrackItem(
                        context = context,
                        track = track,
                        trackAsMediaItem = trackAsMediaItem,
                        mediaController = mediaController,
                        modifier = Modifier.animateItemPlacement(),
                        onClick = actionTrackClick,
                        actionShareTrack = { actionShareTrack(context, track) },
                        onDeleteClick = onDeleteClick
                    )
                }
            }

            if (nowPlayingState is NowPlayingState.TrackLoaded) {
                NowPlayingPreview(nowPlayingState, Modifier.align(Alignment.BottomCenter)) {
                    onTrackItemClick(nowPlayingState.track)
                }
            }

            if (trackToDelete != null) {
                val dismissDialog = { trackToDelete = null }
                DeleteDialog(
                    message = stringResource(
                        id = R.string.delete_track,
                        trackToDelete!!.trackTitle
                    ),
                    dismissDialog = dismissDialog
                ) {
                    val resultPair = actionDeleteTrack(context, trackToDelete!!)
                    if (resultPair != null) {
                        trackAttemptedToDelete = resultPair.first
                        launcher.launch(resultPair.second)
                    }
                    dismissDialog()
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

fun playNext(
    context: Context,
    mediaController: MediaController?,
    mediaItem: MediaItem
) {
    val index = mediaController?.nextMediaItemIndex ?: 0
    mediaController?.addMediaItem(index, mediaItem)
    context.showLongToast(R.string.song_s_will_be_played_next)
}

private fun getRequiredPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE
}

private fun checkVersionAndDelete(
    context: Context,
    track: Track,
    actionShowDeleteDialog: () -> Unit,
    launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        delegateDeleteToActivity(context, track, launcher)
    else actionShowDeleteDialog()
}

@RequiresApi(Build.VERSION_CODES.R)
private fun delegateDeleteToActivity(
    context: Context,
    track: Track,
    launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
) {
    val pendingIntent =
        MediaStore.createDeleteRequest(context.contentResolver, listOf(track.contentUri))
    val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()

    launcher.launch(intentSenderRequest)
}

@Composable
private fun getDeleteLauncher(
    track: Track?,
    actionDeleteTrack: (Context, Track) -> Pair<Track, IntentSenderRequest>?
): ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            context.showLongToast(R.string.delete_failed_prompt)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            actionDeleteTrack(context, track!!)
        }
    }
}
