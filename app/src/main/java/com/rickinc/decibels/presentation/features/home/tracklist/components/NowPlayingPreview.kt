package com.rickinc.decibels.presentation.features.home.tracklist.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.features.nowplaying.NowPlayingState
import com.rickinc.decibels.presentation.util.LocalController
import com.rickinc.decibels.presentation.theme.Typography
import com.rickinc.decibels.presentation.theme.secondaryVariant

@Composable
fun NowPlayingPreview(
    nowPlayingState: NowPlayingState.TrackLoaded,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val controller = LocalController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(secondaryVariant)
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(0.7f)
            ) {
                val thumbnail = nowPlayingState.track.thumbnail
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = stringResource(id = R.string.album_art),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))
                Crossfade(
                    targetState = nowPlayingState.track,
                    animationSpec = tween(500)
                ) { track ->
                    Column {
                        Text(
                            text = track.title,
                            style = Typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = track.artist,
                            style = Typography.bodySmall,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .weight(0.3f)
                    .padding(start = 8.dp)
            ) {
                val iconId =
                    if (nowPlayingState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                Crossfade(
                    targetState = iconId,
                    animationSpec = tween(500)
                ) { res ->
                    NowPlayingPreviewControlButton(
                        iconRes = res,
                        contentDescriptionRes = R.string.play_pause_button
                    ) {
                        if (nowPlayingState.isPlaying) controller?.pause()
                        else controller?.play()
                    }
                }

                NowPlayingPreviewControlButton(
                    iconRes = R.drawable.ic_next,
                    contentDescriptionRes = R.string.next_track_button
                ) {
                    controller?.seekToNextMediaItem()
                }
            }
        }

        val progress =
            nowPlayingState.progress.toFloat().div(nowPlayingState.track.trackLength)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            color = Color.White,
            trackColor = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}