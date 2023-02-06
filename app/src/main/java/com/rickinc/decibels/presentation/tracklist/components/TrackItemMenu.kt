package com.rickinc.decibels.presentation.tracklist.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.ui.theme.Typography

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