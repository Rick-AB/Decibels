package com.rickinc.decibels.presentation.nowplaying

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
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
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.ui.components.DefaultTopAppBar
import com.rickinc.decibels.presentation.ui.theme.LocalController
import com.rickinc.decibels.presentation.ui.theme.Typography
import com.rickinc.decibels.presentation.util.formatTrackDuration
import timber.log.Timber


@Composable
fun NowPlayingScreen(goBack: () -> Unit) {
    val context = LocalContext.current
    val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel(context as ComponentActivity)

    when (val uiState = nowPlayingViewModel.uiState.collectAsStateWithLifecycle().value) {
        is NowPlayingState.TrackLoaded -> NowPlayingScreen(uiState = uiState)
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
) {
    val defaultBackgroundColor = MaterialTheme.colorScheme.background
    val defaultColor = MaterialTheme.colorScheme.primary
    val trackThumbnail = uiState.currentTrack.thumbnail
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = LocalController.current
    val context = LocalContext.current
    val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel(context as ComponentActivity)
    val handler = Handler(Looper.getMainLooper())


    var backgroundColor by remember { mutableStateOf(defaultBackgroundColor) }
    val animatedBackgroundColor by animateColorAsState(targetValue = backgroundColor)

    var color by remember { mutableStateOf(defaultColor) }
    val animatedColor by animateColorAsState(targetValue = color)

    LaunchedEffect(key1 = trackThumbnail) {
        trackThumbnail?.let {
            Palette.from(it).generate { palette ->
                val rgb = palette?.dominantSwatch?.rgb!!
                val r = android.graphics.Color.red(rgb).times(0.5).toInt()
                val g = android.graphics.Color.green(rgb).times(0.5).toInt()
                val b = android.graphics.Color.blue(rgb).times(0.5).toInt()
                backgroundColor = Color(r, g, b)
                color = Color(rgb)
            }
        }
    }

//    DisposableEffect(key1 = lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_RESUME) {
//                handler.postDelayed(object : Runnable {
//                    override fun run() {
//                        val currentPos = (controller?.currentPosition ?: 0)
//                        val duration = controller?.duration ?: 0
//                        val progress = (currentPos.times(100)).div(duration)
//                        nowPlayingViewModel.onEvent(NowPlayingEvent.OnProgressChanged(currentPos))
//
//                        handler.removeCallbacks(this)
//                        // Schedule an update if necessary.
//                        val playbackState = controller?.playbackState ?: Player.STATE_IDLE
//                        if (playbackState != Player.STATE_ENDED) {
//                            var delayMs: Long
//                            if (controller?.playWhenReady!! && playbackState == Player.STATE_READY) {
//                                delayMs = 1000 - progress % 1000
//                                if (delayMs < 200) {
//                                    delayMs += 1000
//                                }
//                            } else {
//                                delayMs = 1000
//                            }
//                            handler.postDelayed(this, delayMs)
//                        }
//                    }
//                }, 1000)
//            }
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
//    }

    Scaffold(
        topBar = { NowPlayingTopAppBar({}, {}) },
        modifier = Modifier.fillMaxSize(),
        containerColor = animatedBackgroundColor
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 35.dp)
        ) {
            val (albumArtComposable, trackNameComposable, artistComposable,
                currentDurationComposable, trackDurationComposable, seekBar, previousButton,
                nextButton, playPauseButton, shuffleButton, repeatButton) = createRefs()

            val guideLine = createGuidelineFromTop(0.55f)

            if (trackThumbnail != null) {
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

            val trackName = uiState.currentTrack.trackTitle
            Text(
                text = trackName,
                style = Typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(trackNameComposable) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(guideLine, 16.dp)
                }
            )

            val artist = uiState.currentTrack.artist
            Text(
                text = artist,
                style = Typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(artistComposable) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(trackNameComposable.bottom, 8.dp)
                }
            )

            val sliderContentDesc = stringResource(id = R.string.seek_bar)
            Slider(
                value = uiState.progress.toFloat(),
                onValueChange = { controller?.seekTo(it.toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onBackground,
                    activeTrackColor = MaterialTheme.colorScheme.onBackground
                ),
                valueRange = 0f..uiState.currentTrack.trackLength.toFloat(),
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
                text = currentDuration,
                style = Typography.bodyMedium,
                modifier = Modifier.constrainAs(currentDurationComposable) {
                    start.linkTo(seekBar.start)
                    top.linkTo(seekBar.bottom)
                }
            )

            val trackDuration = formatTrackDuration(uiState.currentTrack.trackLength.toLong())
            Text(
                text = trackDuration,
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

            val controlSize = 40.dp
            NowPlayingControlButton(
                iconRes = R.drawable.ic_previous,
                contentDesc = R.string.previous_track_button,
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

            NowPlayingControlButton(
                iconRes = if (uiState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                contentDesc = R.string.play_pause_button,
                size = 44.dp,
                backgroundColor = animatedColor,
                modifier = Modifier.constrainAs(playPauseButton) {
                    start.linkTo(previousButton.end)
                    end.linkTo(nextButton.start)
                    top.linkTo(seekBar.bottom, 16.dp)
                },
            ) {
                if (uiState.isPlaying) controller?.pause()
                else controller?.play()
            }

            NowPlayingControlButton(
                iconRes = R.drawable.ic_next,
                contentDesc = R.string.next_track_button,
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
    size: Dp = 24.dp,
    backgroundColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.background(backgroundColor, CircleShape)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = contentDesc),
            modifier = Modifier
                .padding(8.dp)
                .size(size)
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
    onOverFlowClick: () -> Unit
) {
    val nowPlayingContentDesc = stringResource(id = R.string.top_app_bar)
    DefaultTopAppBar(title = "",
        mainIcon = { BackArrow(onBackClick) },
        actions = { OverFlowIcon(onOverFlowClick) },
        modifier = Modifier.semantics { contentDescription = nowPlayingContentDesc })
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