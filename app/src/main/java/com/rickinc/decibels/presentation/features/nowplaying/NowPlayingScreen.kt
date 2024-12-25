package com.rickinc.decibels.presentation.features.nowplaying

import android.annotation.SuppressLint
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.MotionLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.components.DefaultTopAppBar
import com.rickinc.decibels.presentation.components.clickable
import com.rickinc.decibels.presentation.components.currentFraction
import com.rickinc.decibels.presentation.features.nowplaying.components.NowPlayingBottomSheetContent
import com.rickinc.decibels.presentation.features.nowplaying.components.motionScene
import com.rickinc.decibels.presentation.theme.LightBlack
import com.rickinc.decibels.presentation.util.LocalController
import com.rickinc.decibels.presentation.theme.Typography
import com.rickinc.decibels.presentation.util.formatTrackDuration

@Composable
fun NowPlayingScreen(selectedTrack: Track, goBack: () -> Unit) {
    val context = LocalContext.current
    val nowPlayingViewModel: NowPlayingViewModel = hiltViewModel(context as ComponentActivity)

    when (val uiState = nowPlayingViewModel.uiState.collectAsStateWithLifecycle().value) {
        is NowPlayingState.TrackLoaded -> {
            NowPlayingScreen(
                selectedTrack = selectedTrack,
                uiState = uiState,
                nowPlayingViewModel = nowPlayingViewModel,
                goBack = goBack
            )
        }

        is NowPlayingState.ErrorLoadingTrack -> {
            Toast.makeText(context, uiState.error.message, Toast.LENGTH_LONG).show()
            goBack()
        }

        else -> {}
    }

    BackHandler {
        goBack()
    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NowPlayingScreen(
    selectedTrack: Track,
    uiState: NowPlayingState.TrackLoaded,
    nowPlayingViewModel: NowPlayingViewModel,
    goBack: () -> Unit
) {
    val (track, isPlaying, repeatMode, isShuffleActive, progress, _) = uiState
    var useUiState by remember { mutableStateOf(false) }

    // initialize trackState with the selected track passed to the screen bcuz track from uiState can hold stale data initially
    val trackState by remember(track.id) {
        if (selectedTrack.id == track.id && !useUiState) useUiState = true

        if (useUiState) mutableStateOf(track)
        else mutableStateOf(selectedTrack)
    }

    val primaryBackgroundColor = LightBlack
    val secondaryBackgroundColor = MaterialTheme.colorScheme.primary
    val hasThumbnail = trackState.hasThumbnail
    val trackThumbnail = trackState.thumbnail!!
    val controller = LocalController.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val window = (LocalContext.current as ComponentActivity).window
    val systemUiController = rememberSystemUiController()
    val scaffoldState = rememberBottomSheetScaffoldState()

    var backgroundColor by remember { mutableStateOf(primaryBackgroundColor) }
    val animatedBackgroundColor by animateColorAsState(targetValue = backgroundColor, label = "animate background color")

    var color by remember { mutableStateOf(secondaryBackgroundColor) }
    val animatedOnBackgroundColor by animateColorAsState(targetValue = color, label = "animate color")

    LaunchedEffect(key1 = trackThumbnail) {
        if (hasThumbnail) {
            Palette.from(trackThumbnail).generate { palette ->
                val multiplicationFactor = 0.22
                val rgb = palette?.dominantSwatch?.rgb!!
                val r = android.graphics.Color.red(rgb).times(multiplicationFactor).toInt()
                val g = android.graphics.Color.green(rgb).times(multiplicationFactor).toInt()
                val b = android.graphics.Color.blue(rgb).times(multiplicationFactor).toInt()
                backgroundColor = Color(r, g, b)
                color = Color(rgb)
            }
        } else {
            backgroundColor = primaryBackgroundColor
            color = secondaryBackgroundColor
        }
    }

    LaunchedEffect(key1 = backgroundColor, key2 = color) {
        systemUiController.setStatusBarColor(backgroundColor)
        systemUiController.setNavigationBarColor(color)
    }

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(),
        sheetPeekHeight = 48.dp,
        modifier = Modifier.fillMaxSize(),
        containerColor = animatedBackgroundColor,
        sheetContent = {
            NowPlayingBottomSheetContent(
                nowPlayingUiState = uiState,
                nowPlayingViewModel = nowPlayingViewModel,
                animatedBackgroundColor = animatedBackgroundColor,
                isCollapsed = scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden,
            ) { scaffoldState.currentFraction }
        },
        sheetShadowElevation = 8.dp,
        sheetContainerColor = animatedOnBackgroundColor,
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) { _ ->
        MotionLayout(
            motionScene = motionScene(),
            progress = scaffoldState.currentFraction,
            modifier = Modifier
                .fillMaxSize()
        ) {
            NowPlayingTopAppBar(
                onBackClick = goBack,
                modifier = Modifier.layoutId(NowPlayingLayout.TOP_APP_BAR)
            )

            Image(
                bitmap = trackThumbnail.asImageBitmap(),
                contentDescription = stringResource(id = R.string.album_art),
                modifier = Modifier.layoutId(NowPlayingLayout.IMAGE)
            )

            val titleProperties = customProperties(NowPlayingLayout.TITLE.name)
            val alignmentProperty = titleProperties.int(textAlign)
            val singleLineProperty = titleProperties.int(maxLines)
            val titleFontSize = titleProperties.fontSize(fontSize)
            AnimatedContent(
                targetState = trackState.title,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                modifier = Modifier.layoutId(NowPlayingLayout.TITLE),
                label = "animate track name"
            ) { trackName ->
                Text(
                    text = trackName,
                    style = Typography.titleMedium.copy(fontSize = titleFontSize),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = getTextAlignmentFromInt(alignmentProperty),
                    maxLines = singleLineProperty,
                    overflow = TextOverflow.Clip
                )
            }

            val artistFontSize = customProperties(NowPlayingLayout.ARTIST.name).fontSize(fontSize)
            AnimatedContent(
                targetState = trackState.artist,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                modifier = Modifier.layoutId(NowPlayingLayout.ARTIST),
                label = "animate artist"
            ) { artist ->
                Text(
                    text = artist,
                    style = Typography.bodyMedium.copy(fontSize = artistFontSize),
                    textAlign = getTextAlignmentFromInt(alignmentProperty),
                    maxLines = singleLineProperty,
                    overflow = TextOverflow.Clip
                )
            }

            val sliderContentDesc = stringResource(id = R.string.seek_bar)
            Slider(
                value = progress.toFloat(),
                onValueChange = { controller?.seekTo(it.toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onBackground,
                    activeTrackColor = MaterialTheme.colorScheme.onBackground
                ),
                valueRange = 0f..track.trackLength.toFloat(),
                modifier = Modifier
                    .layoutId(NowPlayingLayout.SEEK_BAR)
                    .semantics { contentDescription = sliderContentDesc }
            )

            val currentDuration = formatTrackDuration(progress)
            Text(
                text = currentDuration,
                style = Typography.bodyMedium,
                modifier = Modifier.layoutId(NowPlayingLayout.CURRENT_DURATION)
            )

            val trackDuration = formatTrackDuration(track.trackLength.toLong())
            Text(
                text = trackDuration,
                style = Typography.bodyMedium,
                modifier = Modifier.layoutId(NowPlayingLayout.TRACK_DURATION)
            )

            NowPlayingControlButton(
                iconRes = R.drawable.ic_shuffle,
                contentDesc = R.string.shuffle_button,
                tint = if (isShuffleActive) MaterialTheme.colorScheme.primary else Color.White,
                modifier = Modifier.layoutId(NowPlayingLayout.SHUFFLE)
            ) {
                controller?.shuffleModeEnabled = !controller?.shuffleModeEnabled!!
            }

            val controlSize = 36.dp
            NowPlayingControlButton(
                iconRes = R.drawable.ic_previous,
                contentDesc = R.string.previous_track_button,
                tint = Color.White,
                size = controlSize,
                modifier = Modifier.layoutId(NowPlayingLayout.PREVIOUS)
            ) {
                controller?.seekToPreviousMediaItem()
            }

            val buttonProperties = customProperties(NowPlayingLayout.PLAY_PAUSE.name)
            NowPlayingControlButton(
                iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                contentDesc = R.string.play_pause_button,
                size = 40.dp,
                backgroundColor = animatedOnBackgroundColor,
                showBackground = buttonProperties.int(showBackground) == 0,
                modifier = Modifier.layoutId(NowPlayingLayout.PLAY_PAUSE)
            ) {
                if (isPlaying) controller?.pause()
                else controller?.play()
            }


            NowPlayingControlButton(
                iconRes = R.drawable.ic_next,
                contentDesc = R.string.next_track_button,
                tint = Color.White,
                size = controlSize,
                modifier = Modifier.layoutId(NowPlayingLayout.NEXT)
            ) {
                controller?.seekToNextMediaItem()
            }

            val isRepeatOn = repeatMode != Player.REPEAT_MODE_OFF
            RepeatIconButton(
                iconRes = R.drawable.ic_repeat,
                contentDesc = R.string.repeat_button,
                tint = if (isRepeatOn) MaterialTheme.colorScheme.primary else Color.White,
                repeatMode = repeatMode,
                modifier = Modifier.layoutId(NowPlayingLayout.REPEAT)
            ) {
                controller?.let {
                    handleRepeatSelection(repeatMode, it)
                }
            }
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
    showBackground: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (showBackground) {
        Box(
            modifier = modifier
                .clickable(
                    shape = CircleShape,
                    onClick = onClick
                )
                .background(backgroundColor, CircleShape)
                .padding(8.dp)
        ) {
            AnimatedContent(
                targetState = iconRes,
                transitionSpec = {
                    slideInVertically(tween()) { fullHeight -> -fullHeight * 2 } togetherWith
                            slideOutVertically(tween()) { fullHeight -> fullHeight * 2 }
                },
                label = "animate control button",
            ) { res ->
                Icon(
                    painter = painterResource(id = res),
                    contentDescription = stringResource(id = contentDesc),
                    modifier = Modifier.size(size)
                )
            }
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(id = contentDesc),
                modifier = Modifier
                    .size(36.dp)
            )
        }
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
    modifier: Modifier = Modifier,
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
        modifier = modifier.semantics { contentDescription = nowPlayingContentDesc }
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
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
    repeatMode: Int,
    mediaController: MediaController
) {
    when (repeatMode) {
        Player.REPEAT_MODE_OFF -> mediaController.repeatMode = Player.REPEAT_MODE_ALL
        Player.REPEAT_MODE_ALL -> mediaController.repeatMode = Player.REPEAT_MODE_ONE
        Player.REPEAT_MODE_ONE -> mediaController.repeatMode = Player.REPEAT_MODE_OFF
    }
}

private fun getTextAlignmentFromInt(value: Int): TextAlign {
    return when (value) {
        0 -> TextAlign.Center
        else -> TextAlign.Start
    }
}