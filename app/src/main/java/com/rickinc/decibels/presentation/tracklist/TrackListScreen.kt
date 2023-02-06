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
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
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
    actionDeleteTrack: (Context, Track) -> Unit,
    onTrackItemClick: (Track) -> Unit
) {
    if (tracks.isEmpty()) InfoText(stringResource = R.string.empty_track_list)
    else {
        val trackContentDescription = stringResource(id = R.string.track_list)
        val mediaController = LocalController.current
        val context = LocalContext.current
        val launcher = getDeleteLauncher()
        var showDeleteDialog by remember { mutableStateOf(false) }
        var trackToDelete: Track? by remember { mutableStateOf(null) }

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

                    TrackItem(
                        context = context,
                        track = track,
                        trackAsMediaItem = trackAsMediaItem,
                        mediaController = mediaController,
                        modifier = Modifier.animateItemPlacement(),
                        onClick = actionTrackClick
                    ) {
                        checkVersionAndDelete(
                            context = context,
                            track = track,
                            launcher = launcher,
                            actionShowDeleteDialog = {
                                showDeleteDialog = true; trackToDelete = track
                            }
                        )
                    }
                }
            }

            if (nowPlayingState is NowPlayingState.TrackLoaded) {
                NowPlayingPreview(nowPlayingState, Modifier.align(Alignment.BottomCenter)) {
                    onTrackItemClick(nowPlayingState.track)
                }
            }

            if (showDeleteDialog && trackToDelete != null) {
                val dismissDialog = { showDeleteDialog = false; trackToDelete = null }
                DeleteDialog(
                    message = stringResource(
                        id = R.string.delete_track,
                        trackToDelete!!.trackTitle
                    ),
                    dismissDialog = dismissDialog
                ) {
                    dismissDialog()
                    actionDeleteTrack(context, trackToDelete!!)
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 16.dp, end = 16.dp),
    ) {
        Column(modifier = Modifier.weight(0.7f)) {
            Text(
                text = track.trackTitle,
                style = Typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist,
                style = Typography.bodySmall,
                maxLines = 1
            )
        }

        val displayTime = formatTrackDuration(track.trackLength.toLong())
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(0.3f)
                .padding(start = 16.dp)
        ) {
            Text(text = displayTime, style = Typography.bodyMedium)

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
                    modifier = Modifier.align(Alignment.TopEnd),
                    actionPlayNext = { playNext(context, mediaController, trackAsMediaItem) },
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
fun TrackItemMenu(
    expanded: Boolean,
    dismissMenu: () -> Unit,
    modifier: Modifier = Modifier,
    actionPlayNext: () -> Unit,
    onDeleteClick: () -> Unit
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
            onClick = onDeleteClick,
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(0.7f)
            ) {
                val thumbnail = nowPlayingState.track.thumbnail
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = stringResource(id = R.string.album_art),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))
                Crossfade(
                    targetState = nowPlayingState.track,
                    animationSpec = tween(500)
                ) { track ->
                    Column {
                        Text(
                            text = track.trackTitle,
                            style = Typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = track.artist,
                            style = Typography.bodySmall,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .weight(0.3f)
                    .padding(start = 8.dp)
            ) {
                val iconId =
                    if (nowPlayingState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                Crossfade(
                    targetState = iconId,
                    animationSpec = tween(500)
                ) { res ->
                    NowPlayingPreviewControlButton(
                        iconRes = res,
                        contentDescriptionRes = R.string.play_pause_button
                    ) {
                        if (nowPlayingState.isPlaying) controller?.pause()
                        else controller?.play()
                    }
                }

                NowPlayingPreviewControlButton(
                    iconRes = R.drawable.ic_next,
                    contentDescriptionRes = R.string.next_track_button
                ) {
                    controller?.seekToNextMediaItem()
                }
            }
        }

        val progress =
            nowPlayingState.progress.toFloat().div(nowPlayingState.track.trackLength)
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
fun NowPlayingPreviewControlButton(
    @DrawableRes iconRes: Int,
    @StringRes contentDescriptionRes: Int,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = contentDescriptionRes),
            modifier = Modifier
                .padding(8.dp)
                .size(24.dp)
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

@Preview(showBackground = true, device = Devices.PIXEL_4, showSystemUi = true)
@Composable
fun DeleteDialogPrev() {
    DeleteDialog(message = stringResource(id = R.string.delete_track), {}) {

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
private fun getDeleteLauncher(): ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            context.showLongToast(R.string.delete_failed_prompt)
        }
    }
}
