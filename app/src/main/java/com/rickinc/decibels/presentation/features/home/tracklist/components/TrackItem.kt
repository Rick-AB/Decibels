package com.rickinc.decibels.presentation.features.home.tracklist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.components.TrackThumbnail
import com.rickinc.decibels.presentation.features.home.tracklist.TrackItem

@Composable
fun TrackItem(
    track: TrackItem,
    mediaController: MediaController?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp),
    ) {
        TrackThumbnail(thumbnailUri = track.thumbnailUri)

        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(0.9f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

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
                track = track,
                mediaController = mediaController,
                actionTrackClick = onClick
            )
        }
    }
}