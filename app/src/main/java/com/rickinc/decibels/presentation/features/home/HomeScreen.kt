package com.rickinc.decibels.presentation.features.home

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleStartEffect
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.util.accomponistpermision.findActivity
import com.rickinc.decibels.presentation.util.accomponistpermision.isPermissionPermanentlyDenied
import com.rickinc.decibels.presentation.util.accomponistpermision.setShouldShowRationaleStatus
import com.rickinc.decibels.presentation.features.home.tracklist.TrackListScreen
import com.rickinc.decibels.presentation.features.home.tracklist.TrackListState
import com.rickinc.decibels.presentation.theme.light_onBackground2
import com.rickinc.decibels.presentation.theme.light_title
import com.rickinc.decibels.presentation.util.hasPermission
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

enum class HomeTab {
    Favorites, Playlists, Tracks, Albums, Artists, Folders
}

@Serializable
data object HomeRoute

@Composable
fun HomeScreen(
    trackListState: TrackListState,
    onTrackItemClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { HomeTopAppBar() },
        modifier = modifier,
    ) {
        val context = LocalContext.current
        val permissionString = getRequiredPermission()
        var hasStoragePermission by remember { mutableStateOf(context.hasPermission(permissionString)) }
        var shouldShowRationale by remember { mutableStateOf(false) }
        val preferences = koinInject<SharedPreferences>()
        val isPermanentlyDenied: () -> Boolean =
            { isPermissionPermanentlyDenied(context, preferences, permissionString) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) hasStoragePermission = true
            else setShouldShowRationaleStatus(preferences, permissionString)

            val showRationale = context.findActivity()
                .shouldShowRequestPermissionRationale(permissionString)
            if (showRationale || isPermanentlyDenied()) shouldShowRationale = true
        }

        LifecycleStartEffect(Unit) {
            when {
                isPermanentlyDenied() -> shouldShowRationale = true
                else -> permissionLauncher.launch(permissionString)
            }

            onStopOrDispose { }
        }

        //        when {
//            hasStoragePermission -> TrackListContent(padding, goToNowPlayingScreen)
//            shouldShowRationale -> PermissionRequiredBody(
//                isPermanentlyDenied = isPermanentlyDenied(),
//                onClick = {
//                    shouldShowRationale = false
//
//                    if (isPermanentlyDenied()) context.openAppSettings()
//                    else permissionLauncher.launch(permissionString)
//                }
//            )
//        }

        Column(modifier = Modifier.padding(it)) {
            val coroutineScope = rememberCoroutineScope()
            val items = remember { HomeTab.entries.toTypedArray().toList() }
            val pagerState = rememberPagerState(initialPage = items.indexOf(HomeTab.Tracks)) {
                items.size
            }

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                divider = {},
                modifier = Modifier.fillMaxWidth(),
                indicator = { _ -> },
                containerColor = MaterialTheme.colorScheme.background
            ) {
                items.forEachIndexed { index, homeTab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        text = { Text(text = homeTab.name) },
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        selectedContentColor = light_onBackground2,
                        unselectedContentColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Surface(shape = MaterialTheme.shapes.medium) {
                HorizontalPager(state = pagerState) { index ->
                    val tab = items[index]
                    when (tab) {
                        HomeTab.Favorites -> {}
                        HomeTab.Playlists -> {}
                        HomeTab.Tracks -> {
                            TrackListScreen(
                                trackListState = trackListState,
                                onTrackClick = onTrackItemClick
                            )
                        }

                        HomeTab.Albums -> {}
                        HomeTab.Artists -> {}
                        HomeTab.Folders -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTopAppBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = light_title
        )
    )
}

private fun getRequiredPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE
}
