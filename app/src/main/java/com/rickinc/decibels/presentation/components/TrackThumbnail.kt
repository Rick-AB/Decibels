package com.rickinc.decibels.presentation.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rickinc.decibels.presentation.theme.light_onSecondary

@Composable
fun TrackThumbnail(
    thumbnailUri: Uri?,
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(50.dp, 50.dp),
    shape: Shape = MaterialTheme.shapes.medium
) {
    if (thumbnailUri != null) {
        AsyncImage(
            model = thumbnailUri,
            contentDescription = "album art",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(shape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(light_onSecondary)
                .clip(shape)
        )
    }
}