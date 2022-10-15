package com.rickinc.decibels.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.tracklist.TrackListScreen
import com.rickinc.decibels.presentation.ui.components.DefaultTopAppBar

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

@Composable
fun NowPlayingScreen() {
    NowPlayingTopAppBar()
}

@Composable
fun NowPlayingTopAppBar() {
    val nowPlayingContentDesc = stringResource(id = R.string.now_playing_toolbar_content_desc)
    DefaultTopAppBar(title = "", modifier = Modifier.semantics {
        contentDescription = nowPlayingContentDesc
    })
}
