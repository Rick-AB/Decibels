package com.rickinc.decibels.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.rickinc.decibels.presentation.nowplaying.NowPlayingScreen
import com.rickinc.decibels.presentation.tracklist.TrackListScreen

@Composable
fun MainActivityLayout() {
    val navController = rememberAnimatedNavController()

    Scaffold {
        Column(modifier = Modifier.padding(it)) {
            ScreenContent(navController)
        }
    }
}

@Composable
fun ScreenContent(navController: NavHostController) {

    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.FullScreen.TrackListScreen.route(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Screen.FullScreen.TrackListScreen.route()) {
            TrackListScreen(navBackStackEntry = it) {
                navController.navigate(Screen.FullScreen.NowPlayingScreen.route())
            }
        }

        composable(Screen.FullScreen.NowPlayingScreen.route()) {
            NowPlayingScreen()
        }
    }
}
