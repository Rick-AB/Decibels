package com.rickinc.decibels.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
    val primary = MaterialTheme.colorScheme.primary
    val systemController = rememberSystemUiController()
    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.FullScreen.TrackListScreen.route(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Screen.FullScreen.TrackListScreen.route()) {
            TrackListScreen { track ->
                navController.navigate(
                    Screen.FullScreen.NowPlayingScreen.buildRoute(
                        listOf(track.trackId.toString())
                    )
                )
            }
        }

        composable(
            Screen.FullScreen.NowPlayingScreen.route(),
            arguments = Screen.FullScreen.NowPlayingScreen.getArguments()
        ) {
            NowPlayingScreen {
                systemController.setStatusBarColor(primary)
                navController.popBackStack()
            }
        }
    }
}
