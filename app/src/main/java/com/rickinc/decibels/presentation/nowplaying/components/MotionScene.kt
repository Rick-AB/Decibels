package com.rickinc.decibels.presentation.nowplaying.components

import android.annotation.SuppressLint
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintSetRef
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.MotionScene
import androidx.constraintlayout.compose.Visibility
import com.rickinc.decibels.presentation.nowplaying.NowPlayingLayout

@SuppressLint("Range")
fun motionScene(): MotionScene {
    var startSet: ConstraintSetRef
    var endSet: ConstraintSetRef
    val ms = MotionScene {
        val topAppBar = createRefFor(NowPlayingLayout.TOP_APP_BAR)
        val backArrow = createRefFor(NowPlayingLayout.BACK_ARROW)
        val overFlowMenu = createRefFor(NowPlayingLayout.OVER_FLOW_MENU)
        val albumArt = createRefFor(NowPlayingLayout.IMAGE)
        val trackTitle = createRefFor(NowPlayingLayout.TITLE)
        val artist = createRefFor(NowPlayingLayout.ARTIST)
        val currentDuration = createRefFor(NowPlayingLayout.CURRENT_DURATION)
        val trackDuration = createRefFor(NowPlayingLayout.TRACK_DURATION)
        val seekBar = createRefFor(NowPlayingLayout.SEEK_BAR)
        val previousButton = createRefFor(NowPlayingLayout.PREVIOUS)
        val nextButton = createRefFor(NowPlayingLayout.NEXT)
        val playPauseButton = createRefFor(NowPlayingLayout.PLAY_PAUSE)
        val shuffleButton = createRefFor(NowPlayingLayout.SHUFFLE)
        val repeatButton = createRefFor(NowPlayingLayout.REPEAT)

        startSet = constraintSet("start") {
            val guideLine = createGuidelineFromTop(0.55f)

            constrain(topAppBar) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                width = Dimension.fillToConstraints
                alpha = 1.0f
                visibility = Visibility.Visible
            }

            constrain(backArrow) {
                start.linkTo(topAppBar.start)
            }

            constrain(albumArt) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(topAppBar.bottom)
                bottom.linkTo(guideLine)
                width = Dimension.fillToConstraints
                height = Dimension.ratio("1:1")
            }

            constrain(trackTitle) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(guideLine, 16.dp)
                customInt("textAlign", 0)
            }

            constrain(artist) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(trackTitle.bottom, 8.dp)
                customInt("textAlign", 0)
            }

            constrain(currentDuration) {
                start.linkTo(seekBar.start)
                top.linkTo(seekBar.bottom)
            }

            constrain(trackDuration) {
                end.linkTo(seekBar.end)
                top.linkTo(seekBar.bottom)
            }

            constrain(previousButton) {
                start.linkTo(shuffleButton.end)
                end.linkTo(playPauseButton.start)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
            }

            constrain(nextButton) {
                start.linkTo(playPauseButton.end)
                end.linkTo(repeatButton.start)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
            }

            constrain(playPauseButton) {
                start.linkTo(previousButton.end)
                end.linkTo(nextButton.start)
                top.linkTo(seekBar.bottom, 16.dp)
            }

            constrain(shuffleButton) {
                start.linkTo(seekBar.start)
                end.linkTo(previousButton.start)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
            }

            constrain(repeatButton) {
                start.linkTo(nextButton.end)
                end.linkTo(seekBar.end)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
            }

            constrain(seekBar) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(artist.bottom, 16.dp)
            }
        }

        endSet = constraintSet("end") {
            constrain(topAppBar) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                alpha = 0.0f
                visibility = Visibility.Gone
            }

            constrain(albumArt) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                width = Dimension.value(50.dp)
                height = Dimension.value(50.dp)
            }

            constrain(trackTitle) {
                linkTo(
                    start = albumArt.end,
                    end = playPauseButton.start,
                    top = albumArt.top,
                    bottom = albumArt.bottom,
                    startMargin = 16.dp,
                    verticalBias = 0.3f
                )
                width = Dimension.fillToConstraints
                customInt("textAlign", 1)
            }

            constrain(artist) {
                start.linkTo(trackTitle.start)
                end.linkTo(trackTitle.end)
                top.linkTo(trackTitle.bottom, 8.dp)
                width = Dimension.fillToConstraints
                customInt("textAlign", 1)
            }

            constrain(currentDuration) {
                start.linkTo(seekBar.start)
                top.linkTo(seekBar.bottom)
                alpha = 0.0F
            }

            constrain(trackDuration) {
                end.linkTo(seekBar.end)
                top.linkTo(seekBar.bottom)
                alpha = 0.0F
            }

            constrain(previousButton) {
                start.linkTo(shuffleButton.end)
                end.linkTo(playPauseButton.start)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
                alpha = 0.0F
            }

            constrain(nextButton) {
                end.linkTo(parent.end)
                top.linkTo(albumArt.top)
                bottom.linkTo(albumArt.bottom)
                width = Dimension.value(24.dp)
                height = Dimension.value(24.dp)
            }

            constrain(playPauseButton) {
                end.linkTo(nextButton.start, 16.dp)
                top.linkTo(nextButton.top)
                bottom.linkTo(nextButton.bottom)
                width = Dimension.value(24.dp)
                height = Dimension.value(24.dp)
            }

            constrain(shuffleButton) {
                start.linkTo(seekBar.start)
                end.linkTo(previousButton.start)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
                alpha = 0.0F
            }

            constrain(repeatButton) {
                start.linkTo(nextButton.end)
                end.linkTo(seekBar.end)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
                alpha = 0.0F
            }

            constrain(seekBar) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(artist.bottom, 16.dp)
                alpha = 0.0F
            }
        }

        transition(startSet, endSet, "default") {
            keyAttributes(
                currentDuration,
                trackDuration,
                seekBar,
                previousButton,
                shuffleButton,
                repeatButton
            ) {
                frame(25) {
                    alpha = 0.0F
                }
            }

            keyPositions(
                trackTitle,
                artist,
                currentDuration,
                trackDuration,
                playPauseButton,
                nextButton,
                seekBar,
                previousButton,
                shuffleButton,
                repeatButton
            ) {
                frame(10) {
                    percentX = 0.0F
                    percentY = 0.0F
                    percentHeight = 0.0F
                    percentWidth = 0.0F
                }
                frame(20) {
                    percentX = 0.0F
                    percentY = 0.0F
                    percentHeight = 0.0F
                    percentWidth = 0.0F
                }
                frame(30) {
                    percentX = 0.0F
                    percentY = 0.0F
                    percentHeight = 0.0F
                    percentWidth = 0.0F
                }
                frame(40) {
                    percentX = 0.0F
                    percentY = 0.0F
                    percentHeight = 0.0F
                    percentWidth = 0.0F
                }
                frame(50) {
                    percentX = 0.0F
                    percentY = 0.0F
                    percentHeight = 0.0F
                    percentWidth = 0.0F
                }
            }

        }
    }
    return ms
}