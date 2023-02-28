package com.rickinc.decibels.presentation.nowplaying.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rickinc.decibels.presentation.nowplaying.*
import timber.log.Timber

@Composable
fun NowPlayingBottomSheetContent(
    nowPlayingUiState: NowPlayingState.TrackLoaded,
    nowPlayingViewModel: NowPlayingViewModel,
    animatedBackgroundColor: Color
) {
    val bottomSheetUiState =
        nowPlayingViewModel.bottomSheetUiState.collectAsStateWithLifecycle().value

    LaunchedEffect(key1 = nowPlayingUiState.track) {
        nowPlayingViewModel.onEvent(NowPlayingEvent.OnTrackChanged(nowPlayingUiState.track))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = bottomSheetUiState,
            transitionSpec = {
                fadeIn(tween(500)) with fadeOut(tween(500))
            },
            modifier = Modifier.align(Alignment.Center)
        ) {
            when (it) {
                NowPlayingBottomSheetState.Loading -> {
                    CircularProgressIndicator(
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(50.dp)
                    )
                }
                is NowPlayingBottomSheetState.LyricsLoaded -> {
                    NowPlayingBottomSheetLoaded(
                        uiState = it,
                        contentColor = animatedBackgroundColor
                    )
                }
                is NowPlayingBottomSheetState.ErrorLoadingLyrics -> {
                }
            }
        }
    }
}

@Composable
fun NowPlayingBottomSheetLoaded(
    uiState: NowPlayingBottomSheetState.LyricsLoaded,
    contentColor: Color
) {
    LaunchedEffect(key1 = Unit) {
        Timber.d("SUCCCCCCCESSSS")

    }
    Column(modifier = Modifier.fillMaxSize()) {
        val lyricsViewState = rememberLyricsViewState(lrcContent = uiState.lyrics)
        LyricsView(
            state = lyricsViewState,
            modifier = Modifier.weight(weight = 1f, fill = false),
            contentColor = contentColor,
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 150.dp,
            ),
        )
    }
}