package com.rickinc.decibels.presentation.features.home.tracklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.features.home.tracklist.components.InfoText
import com.rickinc.decibels.presentation.features.home.tracklist.components.TrackItem
import com.rickinc.decibels.presentation.util.LocalController

@Composable
fun TrackListScreen(
    trackListState: TrackListState,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (trackListState) {
            is TrackListState.Loading -> {}
            is TrackListState.Content -> TrackList(
                tracks = trackListState.tracks,
                onTrackClick = onTrackClick
            )

            is TrackListState.Error -> InfoText(R.string.error_loading_audio_files)
            is TrackListState.Empty -> InfoText(R.string.empty_track_list)
        }
    }
}

@Composable
private fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackContentDescription = stringResource(id = R.string.track_list)
    val mediaController = LocalController.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = trackContentDescription
            }
    ) {
        itemsIndexed(
            items = tracks,
            key = { _, track -> track.id }
        ) { _, track ->
            TrackItem(
                track = track,
                mediaController = mediaController,
                modifier = Modifier.animateItem(),
                onClick = { onTrackClick(track) }
            )
        }
    }
}
