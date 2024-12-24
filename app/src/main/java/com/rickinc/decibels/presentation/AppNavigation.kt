package com.rickinc.decibels.presentation

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.GsonBuilder
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.util.UriTypeAdapter
import com.rickinc.decibels.presentation.navigation.Screen
import com.rickinc.decibels.presentation.nowplaying.NowPlayingRoute
import com.rickinc.decibels.presentation.nowplaying.NowPlayingScreen
import com.rickinc.decibels.presentation.tracklist.TrackListRoute
import com.rickinc.decibels.presentation.tracklist.TrackListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = TrackListRoute) {
        composable<TrackListRoute> {
            TrackListScreen { track ->
                navController.navigate(
                    Screen.FullScreen.NowPlayingScreen.buildRoute(
                        listOf(track.toString())
                    )
                )
            }
        }

        composable<NowPlayingRoute> { navBackStackEntry ->
            val gson = GsonBuilder().registerTypeHierarchyAdapter(Uri::class.java, UriTypeAdapter())
                .create()

            val track = navBackStackEntry.arguments?.getString(Screen.TRACK)
                ?.let { gson.fromJson(it, Track::class.java) }!!

            val systemController = rememberSystemUiController()
            val primary = MaterialTheme.colorScheme.primary
            NowPlayingScreen(track) {
                systemController.setStatusBarColor(primary)
                navController.popBackStack()
            }
        }
    }
}

