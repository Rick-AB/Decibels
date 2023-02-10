package com.rickinc.decibels.presentation.tracklist.components

import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.tracklist.TrackListMenuViewModel
import com.rickinc.decibels.presentation.ui.components.DeleteDialog
import com.rickinc.decibels.presentation.ui.theme.Typography
import com.rickinc.decibels.presentation.util.showLongToast

@Composable
fun TrackItemMenu(
    expanded: Boolean,
    dismissMenu: () -> Unit,
    track: Track,
    trackAsMediaItem: MediaItem,
    mediaController: MediaController?,
    actionTrackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: TrackListMenuViewModel = hiltViewModel()
    val launcher = getDeleteLauncher(track, viewModel::deleteTrack)

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        val dismissDialog = { showDeleteDialog = false }
        DeleteDialog(
            message = stringResource(
                id = R.string.delete_track,
                track.trackTitle
            ),
            dismissDialog = dismissDialog
        ) {
            dismissDialog()

            val intentSenderRequest = viewModel.deleteTrack(context, track)
            if (intentSenderRequest != null) launcher.launch(intentSenderRequest)
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = dismissMenu,
        modifier = modifier.width(200.dp),
    ) {
        TrackItemMenuItem(
            menuTextRes = R.string.play,
            onDismiss = dismissMenu,
            onClick = actionTrackClick,
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
            onClick = { viewModel.shareFile(context, track) },
        )

        TrackItemMenuItem(
            menuTextRes = R.string.delete,
            onDismiss = dismissMenu,
            onClick = {
                checkVersionAndDelete(
                    context = context,
                    track = track,
                    launcher = launcher,
                    actionShowDeleteDialog = { showDeleteDialog = true }
                )
            },
        )

        TrackItemMenuItem(
            menuTextRes = R.string.play_next,
            onDismiss = dismissMenu,
            onClick = { playNext(context, mediaController, trackAsMediaItem) },
        )

        TrackItemMenuItem(
            menuTextRes = R.string.set_as_ringtone,
            onDismiss = dismissMenu,
            onClick = { viewModel.setAsRingtone(context, track) },
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
        onClick = { onDismiss(); onClick() },
        contentPadding = PaddingValues(16.dp)
    )
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
    actionDeleteTrack: (Context, Track) -> IntentSenderRequest?
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

private fun playNext(
    context: Context,
    mediaController: MediaController?,
    mediaItem: MediaItem
) {
    val index = mediaController?.nextMediaItemIndex ?: 0
    mediaController?.addMediaItem(index, mediaItem)
    context.showLongToast(R.string.song_s_will_be_played_next)
}
