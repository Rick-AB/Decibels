package com.rickinc.decibels.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.rickinc.decibels.presentation.features.home.HomeRoute
import com.rickinc.decibels.presentation.features.home.HomeScreen
import com.rickinc.decibels.presentation.features.home.tracklist.TrackListViewModel
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingRoute
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            val trackListViewModel = koinViewModel<TrackListViewModel>()
            val trackListState = trackListViewModel.state.collectAsStateWithLifecycle().value

            LifecycleStartEffect(Unit) {
                trackListViewModel.getAudioFiles()
                onStopOrDispose { }
            }

            HomeScreen(
                trackListState = trackListState,
                modifier = Modifier.fillMaxSize()
            )
        }

        composable<NowPlayingRoute> { navBackStackEntry ->
            val systemController = rememberSystemUiController()
            val primary = MaterialTheme.colorScheme.primary
//            NowPlayingScreen(track) {
//                systemController.setStatusBarColor(primary)
//                navController.popBackStack()
//            }
        }
    }
}

