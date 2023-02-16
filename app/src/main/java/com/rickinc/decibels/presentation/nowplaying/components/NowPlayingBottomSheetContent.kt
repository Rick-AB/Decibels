package com.rickinc.decibels.presentation.nowplaying.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rickinc.decibels.presentation.nowplaying.rememberLyricsViewState

@Composable
fun NowPlayingBottomSheetContent() {
    val state = rememberLyricsViewState(lrcContent = "")
    Column(modifier = Modifier.fillMaxSize()) {
        LyricsView(
            state = state,
            modifier = Modifier.weight(weight = 1f, fill = false),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 150.dp,
            ),
        )
    }
}