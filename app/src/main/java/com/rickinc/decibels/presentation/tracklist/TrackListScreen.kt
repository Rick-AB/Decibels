package com.rickinc.decibels.presentation.tracklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.model.Track
import com.rickinc.decibels.presentation.ui.components.CustomTopAppBar
import com.rickinc.decibels.presentation.ui.theme.Typography

@Composable
fun SongsListScreen(navBackStackEntry: NavBackStackEntry) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = { SongsListTopAppBar() }) {
        Box(modifier = Modifier.padding(it)) {
            val viewModel: TrackListViewModel = hiltViewModel(navBackStackEntry)
            val screenState = viewModel.uiState.collectAsState().value

            when (screenState) {
                is TrackListState.DataLoaded -> TrackList(screenState.tracks)
                else -> Text(text = "")
            }

        }
    }
}

@Composable
fun SongsListTopAppBar() {
    CustomTopAppBar(title = stringResource(id = R.string.myMusic))
}

@Composable
fun TrackList(tracks: List<Track>) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tracks) {
                TrackItem(track = it)
            }
        }
    }
}

@Composable
fun TrackItem(track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column {
            Text(text = track.trackName, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = track.trackLength.toString(), style = Typography.bodyMedium)

        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_more_vert_24),
                contentDescription = "",
                tint = Color.Gray
            )
        }
    }
}