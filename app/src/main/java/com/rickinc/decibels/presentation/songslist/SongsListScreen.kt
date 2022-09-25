package com.rickinc.decibels.presentation.songslist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.ui.components.CustomTopAppBar

@Composable
fun SongsListScreen(navBackStackEntry: NavBackStackEntry) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = { SongsListTopAppBar() }) {
        Box(modifier = Modifier.padding(it)) {

        }
    }
}

@Composable
fun SongsListTopAppBar() {
    CustomTopAppBar(title = stringResource(id = R.string.myMusic))
}