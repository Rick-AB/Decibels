package com.rickinc.decibels.presentation.nowplaying

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Slider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.ui.components.DefaultTopAppBar
import com.rickinc.decibels.presentation.ui.theme.Typography
import com.rickinc.decibels.presentation.util.formatTrackDuration

@Composable
fun NowPlayingScreen() {
    NowPlayingScreen(uiState = NowPlayingUiState(Track.getSingleTrack()))
}

@Composable
fun NowPlayingScreen(
    uiState: NowPlayingUiState
) {
    Scaffold(topBar = { NowPlayingTopAppBar() }) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 35.dp)
        ) {
            val (albumArtComposable, trackNameComposable, artistComposable,
                currentDurationComposable, trackDurationComposable, seekBar, previousButton,
                nextButton, playPauseButton, shuffleButton, repeatButton) = createRefs()

            val guideLine = createGuidelineFromTop(0.55f)

            Image(
                painter = painterResource(id = R.drawable.ic_baseline_more_vert_24),
                contentDescription = stringResource(id = R.string.album_art),
                modifier = Modifier.constrainAs(albumArtComposable) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(guideLine)
                    width = Dimension.fillToConstraints
                    height = Dimension.ratio("1:1")
                }
            )

            val trackName = uiState.currentTrack.trackName
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
                value = 0f,
                onValueChange = {},
                modifier = Modifier
                    .constrainAs(seekBar) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(artistComposable.bottom, 16.dp)
                    }
                    .semantics { contentDescription = sliderContentDesc }
            )

            Text(
                text = "00:00",
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
                modifier = Modifier.constrainAs(shuffleButton) {
                    start.linkTo(seekBar.start)
                    end.linkTo(previousButton.start)
                    top.linkTo(playPauseButton.top)
                    bottom.linkTo(playPauseButton.bottom)
                }
            )

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
            )

            NowPlayingControlButton(
                iconRes = R.drawable.ic_play,
                contentDesc = R.string.play_pause_button,
                size = 44.dp,
                backgroundColor = Color.Magenta,
                modifier = Modifier.constrainAs(playPauseButton) {
                    start.linkTo(previousButton.end)
                    end.linkTo(nextButton.start)
                    top.linkTo(seekBar.bottom, 16.dp)
                }
            )

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
            )

            NowPlayingControlButton(
                iconRes = R.drawable.ic_repeat,
                contentDesc = R.string.repeat_button,
                modifier = Modifier.constrainAs(repeatButton) {
                    start.linkTo(nextButton.end)
                    end.linkTo(seekBar.end)
                    top.linkTo(playPauseButton.top)
                    bottom.linkTo(playPauseButton.bottom)
                }
            )


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
    size: Dp = 24.dp,
    modifier: Modifier
) {
    IconButton(onClick = { /*TODO*/ }, modifier = modifier) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = contentDesc),
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun NowPlayingControlButton(
    @DrawableRes iconRes: Int,
    @StringRes contentDesc: Int,
    size: Dp = 24.dp,
    backgroundColor: Color,
    modifier: Modifier
) {
    IconButton(
        onClick = { /*TODO*/ },
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
private fun NowPlayingTopAppBar() {
    val nowPlayingContentDesc = stringResource(id = R.string.top_app_bar)
    DefaultTopAppBar(title = "",
        mainIcon = { BackArrow() },
        actions = { OverFlowIcon() },
        modifier = Modifier.semantics { contentDescription = nowPlayingContentDesc })
}

@Composable
private fun BackArrow() {
    IconButton(onClick = { /*TODO*/ }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = stringResource(id = R.string.back_arrow)
        )
    }
}

@Composable
private fun OverFlowIcon() {
    IconButton(onClick = { /*TODO*/ }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(id = R.string.over_flow)
        )
    }
}