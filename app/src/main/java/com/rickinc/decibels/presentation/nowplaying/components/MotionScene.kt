package com.rickinc.decibels.presentation.nowplaying.components

import android.annotation.SuppressLint
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
import com.rickinc.decibels.presentation.nowplaying.*
import com.rickinc.decibels.presentation.ui.theme.Typography

@SuppressLint("Range")
fun motionScene(): MotionScene {
    var startSet: ConstraintSetRef
    var endSet: ConstraintSetRef
    val ms = MotionScene {
        val topAppBar = createRefFor(NowPlayingLayout.TOP_APP_BAR)
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

        val startTitleFontSize = Typography.titleMedium.fontSize
        val endTitleFontSize = Typography.titleSmall.fontSize
        val startArtistFontSize = Typography.bodyMedium.fontSize
        val endArtistFontSize = Typography.bodySmall.fontSize

        startSet = constraintSet("start") {
            val guideLine = createGuidelineFromTop(0.55f)

            constrain(topAppBar) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                width = Dimension.fillToConstraints
                visibility = Visibility.Visible
            }

            constrain(albumArt) {
                start.linkTo(parent.start, 35.dp)
                end.linkTo(parent.end, 35.dp)
                top.linkTo(topAppBar.bottom)
                bottom.linkTo(guideLine)
                width = Dimension.fillToConstraints
                height = Dimension.ratio("1:1")
            }

            constrain(trackTitle) {
                start.linkTo(albumArt.start)
                end.linkTo(albumArt.end)
                top.linkTo(guideLine, 16.dp)
                customInt(textAlign, 0)
                customInt(maxLines, 2)
                customFontSize(fontSize, startTitleFontSize)
            }

            constrain(artist) {
                start.linkTo(trackTitle.start)
                end.linkTo(trackTitle.end)
                top.linkTo(trackTitle.bottom, 8.dp)
                customFontSize(fontSize, startArtistFontSize)
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
                // attribute to show play_pause button background color:: 0 == true
                customInt(showBackground, 0)
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
                start.linkTo(albumArt.start)
                end.linkTo(albumArt.end)
                top.linkTo(artist.bottom, 16.dp)
                width = Dimension.fillToConstraints
            }

//            createHorizontalChain(
//                shuffleButton,
//                previousButton,
//                playPauseButton,
//                nextButton,
//                repeatButton,
//                chainStyle = ChainStyle.Packed
//            )
        }

        endSet = constraintSet("end") {
            constrain(topAppBar) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                alpha = 0F
                visibility = Visibility.Gone
            }

            constrain(albumArt) {
                start.linkTo(parent.start, 16.dp)
                top.linkTo(parent.top, 16.dp)
                width = Dimension.value(50.dp)
                height = Dimension.value(50.dp)
            }

            constrain(trackTitle) {
                start.linkTo(albumArt.end, 16.dp)
                end.linkTo(playPauseButton.start, 16.dp)
                top.linkTo(albumArt.top)
                bottom.linkTo(albumArt.bottom)
                width = Dimension.fillToConstraints
                customInt(textAlign, 1)
                customInt(maxLines, 1)
                customFontSize(fontSize, endTitleFontSize)
            }

            constrain(artist) {
                start.linkTo(trackTitle.start)
                end.linkTo(trackTitle.end)
                top.linkTo(trackTitle.bottom)
//                bottom.linkTo(albumArt.bottom)
                width = Dimension.fillToConstraints
                customFontSize(fontSize, endArtistFontSize)
            }

            constrain(currentDuration) {
                start.linkTo(seekBar.start)
                top.linkTo(seekBar.bottom)
                alpha = 0F
            }

            constrain(trackDuration) {
                end.linkTo(seekBar.end)
                top.linkTo(seekBar.bottom)
                alpha = 0F
            }

            constrain(previousButton) {
                start.linkTo(shuffleButton.end)
                end.linkTo(playPauseButton.start)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
                alpha = 0F
            }

            constrain(nextButton) {
                end.linkTo(parent.end, 16.dp)
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
                customInt(showBackground, 1)
            }

            constrain(shuffleButton) {
                start.linkTo(seekBar.start)
                end.linkTo(previousButton.start)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
                alpha = 0F
            }

            constrain(repeatButton) {
                start.linkTo(nextButton.end)
                end.linkTo(seekBar.end)
                top.linkTo(playPauseButton.top)
                bottom.linkTo(playPauseButton.bottom)
                alpha = 0F
            }

            constrain(seekBar) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(artist.bottom, 16.dp)
                alpha = 0F
            }
        }

        transition(startSet, endSet, "default") {
            keyAttributes(
                topAppBar,
                currentDuration,
                trackDuration,
                seekBar,
                previousButton,
                shuffleButton,
                repeatButton
            ) {
                frame(15) {
                    alpha = 0F
                }
                frame(50) {
                    alpha = 0F
                }
                frame(75) {
                    alpha = 0F
                }
                frame(100) {
                    alpha = 0F
                }
            }

            keyAttributes(
                trackTitle,
                artist
            ) {
                frame(15) {
                    alpha = 0F
                    customInt(textAlign, 0)
                    customInt(maxLines, 2)
                }
                frame(30) {
                    alpha = 0F
                    customInt(textAlign, 1)
                    customInt(maxLines, 1)
                }
                frame(50) {
                    alpha = 0F
                    customInt(textAlign, 1)
                    customInt(maxLines, 1)
                }
                frame(99) {
                    alpha = 0.5F
                    customInt(textAlign, 1)
                    customInt(maxLines, 1)
                }
            }

            keyAttributes(
                playPauseButton,
                nextButton
            ) {
                frame(15) {
                    alpha = 0F
                    customInt(showBackground, 0)
                }
                frame(30) {
                    alpha = 0F
                    customInt(showBackground, 1)
                }
                frame(50) {
                    alpha = 0F
                    customInt(showBackground, 1)
                }
                frame(99) {
                    alpha = 0.5F
                    customInt(showBackground, 1)
                }
            }

            keyPositions(
                topAppBar,
                currentDuration,
                trackDuration,
                seekBar,
                previousButton,
                shuffleButton,
                repeatButton
            ) {
                type = RelativePosition.Path

                frame(25) {
                    percentX = 0F
                    percentY = 0F
                    percentHeight = 0F
                    percentWidth = 0F
                }
                frame(50) {
                    percentX = 0F
                    percentY = 0F
                    percentHeight = 0F
                    percentWidth = 0F
                }
                frame(75) {
                    percentX = 0F
                    percentY = 0F
                    percentHeight = 0F
                    percentWidth = 0F
                }
                frame(100) {
                    percentX = 0F
                    percentY = 0F
                    percentHeight = 0F
                    percentWidth = 0F
                }
            }

            keyPositions(
                trackTitle,
                artist,
                nextButton,
                playPauseButton
            ) {
                type = RelativePosition.Path

                frame(25) {
                    percentX = 0F
                    percentY = 0F
                    percentHeight = 0F
                    percentWidth = 0F
                }
                frame(50) {
                    type = RelativePosition.Delta
                    percentX = 1F
                    percentY = 1F
                    percentHeight = 1F
                    percentWidth = 1F
                }
                frame(75) {
                    type = RelativePosition.Delta
                    percentX = 1F
                    percentY = 1F
                    percentHeight = 1F
                    percentWidth = 1F
                }
                frame(100) {
                    type = RelativePosition.Delta
                    percentX = 1F
                    percentY = 1F
                    percentHeight = 1F
                    percentWidth = 1F
                }
            }

        }
    }
    return ms
}