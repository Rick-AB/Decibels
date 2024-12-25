package com.rickinc.decibels.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingRoute
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingScreen
import com.rickinc.decibels.presentation.features.home.tracklist.TrackListRoute
import com.rickinc.decibels.presentation.features.home.tracklist.TrackListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = TrackListRoute) {
        composable<TrackListRoute> {
            TrackListScreen { track ->
                navController.navigate(NowPlayingRoute)
            }
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

