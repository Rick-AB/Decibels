package com.rickinc.decibels.presentation.features.tracklist.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

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