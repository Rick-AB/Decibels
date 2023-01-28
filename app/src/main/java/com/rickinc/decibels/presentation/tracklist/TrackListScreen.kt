package com.rickinc.decibels.presentation.tracklist

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.rickinc.decibels.presentation.ui.components.*
import com.rickinc.decibels.presentation.ui.components.accomponistpermision.findActivity
import com.rickinc.decibels.presentation.ui.theme.LightBlack
import com.rickinc.decibels.presentation.ui.theme.LocalController
import com.rickinc.decibels.presentation.ui.theme.Typography
import com.rickinc.decibels.presentation.util.formatTrackDuration
import com.rickinc.decibels.presentation.util.hasPermission
import com.rickinc.decibels.presentation.util.openAppSettings
import com.rickinc.decibels.presentation.util.showLongToast

@Composable
fun TrackListScreen(onTrackItemClick: (Track) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TrackListTopAppBar() }
    ) { padding ->
        var hasStoragePermission by remember { mutableStateOf(false) }
        var shouldShowRationale by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val preferences = requireSharedPreferencesEntryPoint().sharedPreferences
        val permissionString = getRequiredPermission()
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
                        context.hasPermission(permissionString) -> hasStoragePermission = true
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
        val screenState = viewModel.uiState.collectAsStateWithLifecycle().value
        val nowPlayingState = nowPlayingViewModel.uiState.collectAsStateWithLifecycle().value

        LaunchedEffect(key1 = Unit) {
            viewModel.getAudioFiles()
        }

        when (screenState) {
            is TrackListState.DataLoaded -> TrackList(
                tracks = screenState.tracks,
                tracksAsMediaItems = screenState.tracksAsMediaItems,
                nowPlayingState = nowPlayingState,
                onTrackItemClick = onTrackItemClick
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
    onTrackItemClick: (Track) -> Unit
) {
    if (tracks.isEmpty()) InfoText(stringResource = R.string.empty_track_list)
    else {
        val trackContentDescription = stringResource(id = R.string.track_list)
        val mediaController = LocalController.current
        val context = LocalContext.current

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier
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
                    TrackItem(
                        context = context,
                        track = track,
                        trackAsMediaItem = trackAsMediaItem,
                        mediaController = mediaController
                    ) {
                        playTrack(mediaController, trackAsMediaItem)
                        addPlaylist(mediaController, index, tracksAsMediaItems)
                        onTrackItemClick(track)
                    }
                }
            }

            if (nowPlayingState is NowPlayingState.TrackLoaded) {
                NowPlayingPreview(nowPlayingState, Modifier.align(Alignment.BottomCenter)) {
                    onTrackItemClick(nowPlayingState.currentTrack)
                }
            }
        }
    }
}

@Composable
fun TrackItem(
    context: Context,
    track: Track,
    trackAsMediaItem: MediaItem,
    mediaController: MediaController?,
    onClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

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
        Box {
            IconButton(onClick = { isMenuExpanded = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_more_vert_24),
                    contentDescription = "",
                    tint = Color.Gray
                )
            }

            TrackItemMenu(
                expanded = isMenuExpanded,
                dismissMenu = { isMenuExpanded = false },
                modifier = Modifier.align(Alignment.TopEnd)
            ) { playNext(context, mediaController, trackAsMediaItem) }
        }
    }
}

@Composable
fun TrackItemMenu(
    expanded: Boolean,
    dismissMenu: () -> Unit,
    modifier: Modifier = Modifier,
    actionPlayNext: () -> Unit
) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = dismissMenu,
        modifier = modifier.width(200.dp),
    ) {
        TrackItemMenuItem(
            menuTextRes = R.string.play,
            onDismiss = dismissMenu,
            onClick = {},
        )

        TrackItemMenuItem(
            menuTextRes = R.string.add_to_playlist,
            onDismiss = dismissMenu,
            onClick = {},
        )

        TrackItemMenuItem(
            menuTextRes = R.string.edit_track_info,
            onDismiss = dismissMenu,
            onClick = {},
        )

        TrackItemMenuItem(
            menuTextRes = R.string.share,
            onDismiss = dismissMenu,
            onClick = {},
        )

        TrackItemMenuItem(
            menuTextRes = R.string.delete,
            onDismiss = dismissMenu,
            onClick = {},
        )

        TrackItemMenuItem(
            menuTextRes = R.string.play_next,
            onDismiss = dismissMenu,
            onClick = actionPlayNext,
        )

        TrackItemMenuItem(
            menuTextRes = R.string.set_as_ringtone,
            onDismiss = dismissMenu,
            onClick = {},
        )
    }
}

@Composable
fun TrackItemMenuItem(
    @StringRes menuTextRes: Int,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(id = menuTextRes),
                style = Typography.titleMedium
            )
        },
        onClick = { onClick(); onDismiss() },
        contentPadding = PaddingValues(16.dp)
    )
}

@Composable
fun NowPlayingPreview(
    nowPlayingState: NowPlayingState.TrackLoaded,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val controller = LocalController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LightBlack)
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val thumbnail = nowPlayingState.currentTrack.thumbnail
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = stringResource(id = R.string.album_art),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_audio_file_24),
                    contentDescription = stringResource(id = R.string.album_art),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nowPlayingState.currentTrack.trackTitle,
                    style = Typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = nowPlayingState.currentTrack.artist,
                    style = Typography.bodySmall,
                )
            }

            val iconId = if (nowPlayingState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            Spacer(modifier = Modifier.width(24.dp))
            IconButton(
                onClick = {
                    if (nowPlayingState.isPlaying) controller?.pause()
                    else controller?.play()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = stringResource(id = R.string.play_pause_button),
                )
            }


            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = { controller?.seekToNextMediaItem() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_next),
                    contentDescription = stringResource(id = R.string.play_pause_button),
                )
            }

        }

        val progress =
            nowPlayingState.progress.toFloat().div(nowPlayingState.currentTrack.trackLength)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            color = Color.White,
            trackColor = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
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
fun PermissionRequiredBody(
    isPermanentlyDenied: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val messageRes =
            if (isPermanentlyDenied) R.string.storage_permission_permanently_denied_message
            else R.string.storage_permission_denied_message
        Text(
            text = stringResource(id = messageRes),
            style = Typography.bodyLarge,
            modifier = Modifier.align(Alignment.Center)
        )

        Button(
            onClick = onClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val buttonTextRes = if (isPermanentlyDenied) R.string.go_to_settings
            else R.string.grant_permission
            Text(text = stringResource(id = buttonTextRes), style = Typography.bodyMedium)
        }
    }
}

@Preview
@Composable
fun PermissionRequiredPrev() {
    PermissionRequiredBody(isPermanentlyDenied = true) {

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

private fun playNext(
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