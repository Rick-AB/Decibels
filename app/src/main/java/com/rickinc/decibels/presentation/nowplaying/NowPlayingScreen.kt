package com.rickinc.decibels.presentation.nowplaying

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.ui.components.DefaultTopAppBar
import com.rickinc.decibels.presentation.ui.theme.LightBlack
import com.rickinc.decibels.presentation.ui.theme.LocalController
import com.rickinc.decibels.presentation.ui.theme.Typography
import com.rickinc.decibels.presentation.util.clickable
import com.rickinc.decibels.presentation.util.formatTrackDuration


@Composable
fun NowPlayingScreen(goBack: () -> Unit) {
    val context = LocalContext.current
    val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel(context as ComponentActivity)

    when (val uiState = nowPlayingViewModel.uiState.collectAsStateWithLifecycle().value) {
        is NowPlayingState.TrackLoaded -> NowPlayingScreen(uiState = uiState, goBack = goBack)
        is NowPlayingState.ErrorLoadingTrack -> {
            Toast.makeText(context, uiState.error.errorMessage, Toast.LENGTH_LONG).show()
            goBack()
        }
        else -> {}
    }

}

@Composable
fun NowPlayingScreen(
    uiState: NowPlayingState.TrackLoaded,
    goBack: () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val primaryBackgroundColor = LightBlack
    val secondaryBackgroundColor = MaterialTheme.colorScheme.primary
    val track = uiState.track
    val hasThumbnail = track.hasThumbnail
    val trackThumbnail = track.thumbnail!!
    val controller = LocalController.current

    var backgroundColor by remember { mutableStateOf(primaryBackgroundColor) }
    val animatedBackgroundColor by animateColorAsState(targetValue = backgroundColor)

    var color by remember { mutableStateOf(secondaryBackgroundColor) }
    val animatedColor by animateColorAsState(targetValue = color)

    LaunchedEffect(key1 = trackThumbnail) {
        if (hasThumbnail) {
            Palette.from(trackThumbnail).generate { palette ->
                val rgb = palette?.dominantSwatch?.rgb!!
                val r = android.graphics.Color.red(rgb).times(0.22).toInt()
                val g = android.graphics.Color.green(rgb).times(0.22).toInt()
                val b = android.graphics.Color.blue(rgb).times(0.22).toInt()
                backgroundColor = Color(r, g, b)
                color = Color(rgb)
            }
        } else {
            backgroundColor = primaryBackgroundColor
            color = secondaryBackgroundColor
        }

        systemUiController.setSystemBarsColor(backgroundColor)
    }

    val showTrackInfo = remember(track) {
//        track.trackId.toString() == controller?.currentMediaItem?.mediaId
        true
    }

    Scaffold(
        topBar = { NowPlayingTopAppBar(goBack) },
        modifier = Modifier.fillMaxSize(),
        containerColor = animatedBackgroundColor
    ) { _ ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 35.dp)
        ) {
            val (albumArtComposable, trackNameComposable, artistComposable,
                currentDurationComposable, trackDurationComposable, seekBar, previousButton,
                nextButton, playPauseButton, shuffleButton, repeatButton) = createRefs()

            val guideLine = createGuidelineFromTop(0.55f)

            if (showTrackInfo) {
                Image(
                    bitmap = trackThumbnail.asImageBitmap(),
                    contentDescription = stringResource(id = R.string.album_art),
                    modifier = Modifier.constrainAs(albumArtComposable) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(guideLine)
                        width = Dimension.fillToConstraints
                        height = Dimension.ratio("1:1")
                    }
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_audio_file_24),
                    contentDescription = stringResource(id = R.string.album_art),
                    modifier = Modifier
                        .constrainAs(albumArtComposable) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(guideLine)
                            width = Dimension.fillToConstraints
                            height = Dimension.ratio("1:1")
                        }
                )
            }

            Crossfade(
                targetState = track.trackTitle,
                modifier = Modifier.constrainAs(trackNameComposable) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(guideLine, 16.dp)
                }
            )
            { trackName ->
                Text(
                    text = trackName,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }

            Crossfade(
                targetState = track.artist,
                modifier = Modifier.constrainAs(artistComposable) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(trackNameComposable.bottom, 8.dp)
                }
            )
            { artist ->
                Text(
                    text = artist,
                    style = Typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            val sliderContentDesc = stringResource(id = R.string.seek_bar)
            Slider(
                value = uiState.progress.toFloat(),
                onValueChange = { controller?.seekTo(it.toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onBackground,
                    activeTrackColor = MaterialTheme.colorScheme.onBackground
                ),
                valueRange = 0f..uiState.track.trackLength.toFloat(),
                modifier = Modifier
                    .constrainAs(seekBar) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(artistComposable.bottom, 16.dp)
                    }
                    .semantics { contentDescription = sliderContentDesc }
            )

            val currentDuration = formatTrackDuration(uiState.progress)
            Text(
                text = if (showTrackInfo) currentDuration else "--",
                style = Typography.bodyMedium,
                modifier = Modifier.constrainAs(currentDurationComposable) {
                    start.linkTo(seekBar.start)
                    top.linkTo(seekBar.bottom)
                }
            )

            val trackDuration = formatTrackDuration(uiState.track.trackLength.toLong())
            Text(
                text = if (showTrackInfo) trackDuration else "--",
                style = Typography.bodyMedium,
                modifier = Modifier.constrainAs(trackDurationComposable) {
                    end.linkTo(seekBar.end)
                    top.linkTo(seekBar.bottom)
                }
            )

            NowPlayingControlButton(
                iconRes = R.drawable.ic_shuffle,
                contentDesc = R.string.shuffle_button,
                tint = if (uiState.isShuffleActive) MaterialTheme.colorScheme.primary else Color.White,
                modifier = Modifier.constrainAs(shuffleButton) {
                    start.linkTo(seekBar.start)
                    end.linkTo(previousButton.start)
                    top.linkTo(playPauseButton.top)
                    bottom.linkTo(playPauseButton.bottom)
                }
            ) {
                controller?.shuffleModeEnabled = !controller?.shuffleModeEnabled!!
            }

            val controlSize = 36.dp
            NowPlayingControlButton(
                iconRes = R.drawable.ic_previous,
                contentDesc = R.string.previous_track_button,
                tint = Color.White,
                size = controlSize,
                modifier = Modifier.constrainAs(previousButton) {
                    start.linkTo(shuffleButton.end)
                    end.linkTo(playPauseButton.start)
                    top.linkTo(playPauseButton.top)
                    bottom.linkTo(playPauseButton.bottom)
                }
            ) {
                controller?.seekToPreviousMediaItem()
            }

            Crossfade(
                targetState = uiState.isPlaying,
                modifier = Modifier.constrainAs(playPauseButton) {
                    start.linkTo(previousButton.end)
                    end.linkTo(nextButton.start)
                    top.linkTo(seekBar.bottom, 16.dp)
                }
            ) { isPlaying ->
                NowPlayingControlButton(
                    iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                    contentDesc = R.string.play_pause_button,
                    size = 40.dp,
                    backgroundColor = animatedColor
                ) {
                    if (uiState.isPlaying) controller?.pause()
                    else controller?.play()
                }
            }

            NowPlayingControlButton(
                iconRes = R.drawable.ic_next,
                contentDesc = R.string.next_track_button,
                tint = Color.White,
                size = controlSize,
                modifier = Modifier.constrainAs(nextButton) {
                    start.linkTo(playPauseButton.end)
                    end.linkTo(repeatButton.start)
                    top.linkTo(playPauseButton.top)
                    bottom.linkTo(playPauseButton.bottom)
                }
            ) {
                controller?.seekToNextMediaItem()
            }

            val isRepeatOn = uiState.repeatMode != Player.REPEAT_MODE_OFF
            RepeatIconButton(
                iconRes = R.drawable.ic_repeat,
                contentDesc = R.string.repeat_button,
                tint = if (isRepeatOn) MaterialTheme.colorScheme.primary else Color.White,
                repeatMode = uiState.repeatMode,
                modifier = Modifier.constrainAs(repeatButton) {
                    start.linkTo(nextButton.end)
                    end.linkTo(seekBar.end)
                    top.linkTo(playPauseButton.top)
                    bottom.linkTo(playPauseButton.bottom)
                }
            ) {
                controller?.let {
                    handleRepeatSelection(uiState, it)
                }
            }


            createHorizontalChain(
                shuffleButton,
                previousButton,
                playPauseButton,
                nextButton,
                repeatButton,
                chainStyle = ChainStyle.SpreadInside
            )
        }

    }
}

@Composable
fun NowPlayingControlButton(
    @DrawableRes iconRes: Int,
    @StringRes contentDesc: Int,
    tint: Color = Color.White,
    size: Dp = 24.dp,
    modifier: Modifier,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = contentDesc),
            modifier = Modifier.size(size),
            tint = tint
        )
    }
}

@Composable
fun NowPlayingControlButton(
    @DrawableRes iconRes: Int,
    @StringRes contentDesc: Int,
    size: Dp,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(
                shape = CircleShape,
                onClick = onClick
            )
            .background(backgroundColor, CircleShape)
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = contentDesc),
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun RepeatIconButton(
    @DrawableRes iconRes: Int,
    @StringRes contentDesc: Int,
    size: Dp = 24.dp,
    tint: Color,
    repeatMode: Int,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(id = contentDesc),
                modifier = Modifier
                    .padding(8.dp)
                    .size(size),
                tint = tint
            )
        }

        if (repeatMode == Player.REPEAT_MODE_ONE)
            Text(text = "1", style = Typography.labelSmall, color = tint)
    }

}

@Composable
private fun NowPlayingTopAppBar(
    onBackClick: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val nowPlayingContentDesc = stringResource(id = R.string.top_app_bar)
    DefaultTopAppBar(
        title = "",
        mainIcon = { BackArrow(onBackClick) },
        actions = {
            Box {
                OverFlowIcon { isMenuExpanded = true }

                NowPlayingOverFlowMenu(
                    menuExpanded = isMenuExpanded,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) { isMenuExpanded = false }
            }
        },
        modifier = Modifier.semantics { contentDescription = nowPlayingContentDesc }
    )
}

@Composable
private fun NowPlayingOverFlowMenu(
    menuExpanded: Boolean,
    modifier: Modifier,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = onDismiss,
        modifier = modifier.background(Color.White, RoundedCornerShape(12.dp))
    ) {
        Text(text = "Do stuff", style = Typography.bodyMedium)
        Text(text = "Do another thing", style = Typography.bodyMedium)
        Text(text = "Do something extraordinary", style = Typography.bodyMedium)
    }
}

@Composable
private fun BackArrow(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = stringResource(id = R.string.back_arrow)
        )
    }
}

@Composable
private fun OverFlowIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(id = R.string.over_flow)
        )
    }
}

private fun handleRepeatSelection(
    uiState: NowPlayingState.TrackLoaded,
    mediaController: MediaController
) {
    when (uiState.repeatMode) {
        Player.REPEAT_MODE_OFF -> mediaController.repeatMode = Player.REPEAT_MODE_ALL
        Player.REPEAT_MODE_ALL -> mediaController.repeatMode = Player.REPEAT_MODE_ONE
        Player.REPEAT_MODE_ONE -> mediaController.repeatMode = Player.REPEAT_MODE_OFF
    }
}