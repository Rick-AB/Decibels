package com.rickinc.decibels.presentation.tracklist

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.model.Track
import com.rickinc.decibels.presentation.ui.components.CustomTopAppBar
import com.rickinc.decibels.presentation.ui.components.accomponistpermision.rememberPermissionState
import com.rickinc.decibels.presentation.ui.components.isPermanentlyDenied
import com.rickinc.decibels.presentation.ui.theme.Typography
import com.rickinc.decibels.presentation.util.formatLongToDisplayString

@Composable
fun TrackListScreen(navBackStackEntry: NavBackStackEntry) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = { TrackListTopAppBar() }) {
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
            storagePermission.hasPermission -> TrackListBody(it, navBackStackEntry)
            storagePermission.shouldShowRationale -> PermissionRequiredBody()
            storagePermission.isPermanentlyDenied() && storagePermission.permissionRequested -> PermissionRequiredBody()
        }
    }
}

@Composable
fun TrackListBody(innerPadding: PaddingValues, navBackStackEntry: NavBackStackEntry) {
    Box(modifier = Modifier.padding(innerPadding)) {
        val viewModel: TrackListViewModel = hiltViewModel(navBackStackEntry)
        val screenState = viewModel.uiState.collectAsState().value

        LaunchedEffect(key1 = Unit) {
            viewModel.getAudioFiles()
        }

        when (screenState) {
            is TrackListState.DataLoaded -> TrackList(screenState.tracks)
            else -> Text(text = "")
        }

    }
}

@Composable
fun TrackListTopAppBar() {
    CustomTopAppBar(title = stringResource(id = R.string.myMusic))
}

@Composable
fun TrackList(tracks: List<Track>) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tracks, key = { it.trackId }) {
                TrackItem(track = it)
            }
        }
    }
}

@Composable
fun TrackItem(track: Track) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.trackName, style = Typography.titleMedium, maxLines = 1)
        }

        val displayTime = formatLongToDisplayString(track.trackLength.toLong())
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
fun PermissionRequiredBody() {
    Column {

    }
}