package com.rickinc.decibels.presentation.tracklist

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.ui.components.DefaultTopAppBar
import com.rickinc.decibels.presentation.ui.components.accomponistpermision.rememberPermissionState
import com.rickinc.decibels.presentation.ui.components.isPermanentlyDenied
import com.rickinc.decibels.presentation.ui.theme.LocalController
import com.rickinc.decibels.presentation.ui.theme.Typography
import com.rickinc.decibels.presentation.util.formatTrackDuration
import timber.log.Timber

@Composable
fun TrackListScreen(onTrackItemClick: (Track) -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = { TrackListTopAppBar() }) { padding ->
        val storagePermission =
            rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    storagePermission.launchPermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        when {
            storagePermission.hasPermission -> TrackListBody(
                padding,
                onTrackItemClick
            )
            storagePermission.shouldShowRationale -> PermissionRequiredBody()
            storagePermission.isPermanentlyDenied() && storagePermission.permissionRequested -> PermissionRequiredBody()
        }
    }
}

@Composable
fun TrackListBody(
    innerPadding: PaddingValues,
    onTrackItemClick: (Track) -> Unit
) {
    Box(modifier = Modifier.padding(innerPadding)) {
        val viewModel: TrackListViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
        val screenState = viewModel.uiState.collectAsState().value

        LaunchedEffect(key1 = Unit) {
            viewModel.getAudioFiles()
        }

        when (screenState) {
            is TrackListState.DataLoaded -> TrackList(
                tracks = screenState.tracks,
                getMediaItems = viewModel::getMediaItems,
                getMediaItem = viewModel::getMediaItem,
                onTrackItemClick = onTrackItemClick
            )
            is TrackListState.Error -> InfoText(R.string.error_loading_audio_files)
            else -> {}
        }

    }
}

@Composable
fun TrackListTopAppBar() {
    DefaultTopAppBar(title = stringResource(id = R.string.myMusic))
}

@Composable
fun TrackList(
    tracks: List<Track>,
    getMediaItems: (List<Track>) -> List<MediaItem>,
    getMediaItem: (Track) -> MediaItem,
    onTrackItemClick: (Track) -> Unit
) {
    if (tracks.isEmpty()) InfoText(stringResource = R.string.empty_track_list)
    else {
        val trackContentDescription = stringResource(id = R.string.track_list)
        val mediaController = LocalController.current
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = trackContentDescription
            }
        ) {
            itemsIndexed(tracks, key = { _, track -> track.trackId }) { index, track ->
                TrackItem(track = track) {
                    playTrack(mediaController, getMediaItem(track))
                    addPlaylist(mediaController, index, tracks, getMediaItems)
                    onTrackItemClick(track)
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
    tracks: List<Track>,
    getMediaItems: (List<Track>) -> List<MediaItem>
) {
    val subListBeforeCurrent = tracks.subList(0, currentTrackIndex)
    mediaController?.addMediaItems(0, getMediaItems(subListBeforeCurrent))

    val subListAfterCurrent = tracks.subList(currentTrackIndex + 1, tracks.lastIndex)
    mediaController?.addMediaItems(getMediaItems(subListAfterCurrent))
}

@Composable
fun TrackItem(track: Track, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.trackTitle, style = Typography.titleMedium, maxLines = 1)

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = track.artist, style = Typography.bodySmall, maxLines = 1)
        }

        val displayTime = formatTrackDuration(track.trackLength.toLong())
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = displayTime, style = Typography.bodyMedium)

        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_more_vert_24),
                contentDescription = "",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun InfoText(@StringRes stringResource: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(id = stringResource),
            style = Typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PermissionRequiredBody() {
    Column {

    }
}