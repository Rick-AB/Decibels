package com.rickinc.decibels.presentation.nowplaying.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.nowplaying.*
import com.rickinc.decibels.presentation.util.formatTrackDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun NowPlayingBottomSheetContent(
    nowPlayingUiState: NowPlayingState.TrackLoaded,
    nowPlayingViewModel: NowPlayingViewModel,
    animatedBackgroundColor: Color
) {
    val context = LocalContext.current
    val bottomSheetUiState =
        nowPlayingViewModel.bottomSheetUiState.collectAsStateWithLifecycle().value

    LaunchedEffect(key1 = nowPlayingUiState.track) {
        Timber.d("LAUNCHED EFFECT")
        nowPlayingViewModel.onEvent(
            NowPlayingEvent.OnTrackChanged(
                context,
                nowPlayingUiState.track
            )
        )
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
                        nowPlayingUiState = nowPlayingUiState,
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
    nowPlayingUiState: NowPlayingState.TrackLoaded,
    contentColor: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val lyricsViewState = rememberLyricsViewState(lrcContent = uiState.lyrics)

        LaunchedEffect(key1 = nowPlayingUiState.isPlaying) {
            if (nowPlayingUiState.isPlaying) lyricsViewState.play(nowPlayingUiState.progress)
        }

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
            fadingEdges = FadingEdges(top = 16.dp, bottom = 150.dp),
        )

        PlaybackControls(state = lyricsViewState, nowPlayingUiState = nowPlayingUiState)
    }
}

@Composable
fun PlaybackControls(
    state: LyricsViewState,
    nowPlayingUiState: NowPlayingState.TrackLoaded,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        val duration = state.lyrics?.optimalDurationMillis
        if (duration != null && duration > 0L) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val currentDuration = formatTrackDuration(nowPlayingUiState.progress)
                Text(
                    text = currentDuration,
                    color = contentColor,
                    fontSize = 14.sp,
                )

                val trackDuration =
                    formatTrackDuration(nowPlayingUiState.track.trackLength.toLong())
                Text(
                    text = trackDuration,
                    color = contentColor,
                    fontSize = 14.sp,
                )
            }

            Slider(
                value = nowPlayingUiState.progress.toFloat(),
                onValueChange = { state.seekTo((duration * it).toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.8f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.5f),
                ),
                valueRange = 0f..nowPlayingUiState.track.trackLength.toFloat()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            val playIcon = if (nowPlayingUiState.isPlaying) {
                R.drawable.ic_pause
            } else {
                R.drawable.ic_play
            }
            Icon(
                painter = painterResource(playIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false),
                        onClick = {
                            if (state.isPlaying) {
                                state.pause()
                            } else {
                                state.play()
                            }
                        },
                    ),
                tint = contentColor,
            )
        }
    }
}