package com.rickinc.decibels.presentation.features.nowplaying.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.exception.ErrorHolder
import com.rickinc.decibels.presentation.components.LottieLoading
import com.rickinc.decibels.presentation.features.nowplaying.LyricsViewState
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingBottomSheetState
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingEvent
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingState
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingViewModel
import com.rickinc.decibels.presentation.features.nowplaying.rememberLyricsViewState
import com.rickinc.decibels.presentation.util.formatTrackDuration
import com.rickinc.decibels.presentation.util.isDark

@Composable
fun NowPlayingBottomSheetContent(
    nowPlayingUiState: NowPlayingState.TrackLoaded,
    nowPlayingViewModel: NowPlayingViewModel,
    animatedBackgroundColor: Color,
    isCollapsed: Boolean,
    sheetOffsetY: () -> Float
) {
    val context = LocalContext.current
    val bottomSheetUiState =
        nowPlayingViewModel.bottomSheetUiState.collectAsStateWithLifecycle().value

    val actionGetTrackLyrics = {
        nowPlayingViewModel.onEvent(
            NowPlayingEvent.OnGetLyrics(
                context,
                nowPlayingUiState.track
            )
        )
    }

    LaunchedEffect(key1 = nowPlayingUiState.track.trackId) {
        actionGetTrackLyrics()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if (isCollapsed) BottomSheetIndicator()

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = bottomSheetUiState,
                transitionSpec = {
                    fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                },
                modifier = Modifier.align(Alignment.Center),
                label = "lyrics"
            ) {
                when (it) {
                    NowPlayingBottomSheetState.Loading -> {
                        LottieLoading(modifier = Modifier)
                    }

                    is NowPlayingBottomSheetState.LyricsLoaded -> {
                        NowPlayingBottomSheetLoaded(
                            uiState = it,
                            nowPlayingUiState = nowPlayingUiState,
                            contentColor = animatedBackgroundColor,
                            sheetOffsetY = sheetOffsetY
                        )
                    }

                    is NowPlayingBottomSheetState.ErrorLoadingLyrics -> {
                        LyricsErrorView(
                            it.error,
                            animatedBackgroundColor,
                            actionGetTrackLyrics
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LyricsErrorView(
    error: ErrorHolder,
    animatedBackgroundColor: Color,
    actionRetry: () -> Unit
) {
    val isDark = animatedBackgroundColor.toArgb().isDark()
    val textColor = if (isDark) Color.White else Color.Gray
    val buttonBackground = if (isDark) Color.White else MaterialTheme.colorScheme.onPrimary

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = error.message,
            style = MaterialTheme.typography.titleMedium.copy(
                color = textColor,
                textAlign = TextAlign.Center
            )
        )

        if (error is ErrorHolder.NetworkConnection) {

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = actionRetry,
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.Gray,
                    containerColor = buttonBackground
                )
            ) {
                Text(
                    text = stringResource(id = R.string.retry),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun ErrorViewPrev() {
    LyricsErrorView(
        error = ErrorHolder.NetworkConnection("No internet connection"),
        animatedBackgroundColor = Color.Black
    ) {

    }
}

@Composable
fun BottomSheetIndicator() {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(50),
            )
            .size(width = 36.dp, height = 4.dp),
    )
}

@Composable
fun NowPlayingBottomSheetLoaded(
    uiState: NowPlayingBottomSheetState.LyricsLoaded,
    nowPlayingUiState: NowPlayingState.TrackLoaded,
    contentColor: Color,
    sheetOffsetY: () -> Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = sheetOffsetY() }
    ) {
        val lyricsViewState = rememberLyricsViewState(lrcContent = uiState.lyrics)

        LaunchedEffect(key1 = nowPlayingUiState.isPlaying) {
            if (nowPlayingUiState.isPlaying) lyricsViewState.play(nowPlayingUiState.progress)
        }

        LyricsView(
            state = lyricsViewState,
            modifier = Modifier.weight(weight = 1f),
            contentColor = contentColor,
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 150.dp,
            ),
            fadingEdges = FadingEdges(top = 16.dp, bottom = 150.dp),
        )

//        PlaybackControls(state = lyricsViewState, nowPlayingUiState = nowPlayingUiState)
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
                        indication = ripple(bounded = false),
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