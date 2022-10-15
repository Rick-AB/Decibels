package com.rickinc.decibels.presentation.nowplaying

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.ui.components.DefaultTopAppBar

@Composable
fun NowPlayingScreen() {
    Scaffold(topBar = { NowPlayingTopAppBar() }) {

    }
}

@Composable
fun NowPlayingTopAppBar() {
    val nowPlayingContentDesc = stringResource(id = R.string.now_playing_toolbar_content_desc)
    DefaultTopAppBar(title = "", modifier = Modifier.semantics {
        contentDescription = nowPlayingContentDesc
    })
}