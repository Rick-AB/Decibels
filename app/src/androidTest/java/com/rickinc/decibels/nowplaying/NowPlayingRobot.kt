package com.rickinc.decibels.nowplaying

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.media3.session.MediaController
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.nowplaying.NowPlayingScreen
import com.rickinc.decibels.presentation.nowplaying.NowPlayingState
import com.rickinc.decibels.presentation.ui.theme.LocalController
import com.rickinc.decibels.presentation.util.formatTrackDuration

fun launchNowPlayingScreen(
    rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>,
    uiState: NowPlayingState.TrackLoaded,
    controller: MediaController,
    block: NowPlayingRobot.() -> Unit
): NowPlayingRobot {
    rule.setContent {
        CompositionLocalProvider(LocalController provides controller) {
            NowPlayingScreen(uiState)
        }

    }
    return NowPlayingRobot(rule).apply(block)
}

class NowPlayingRobot(
    private val rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>
) {
    infix fun verify(
        block: NowPlayingVerification.() -> Unit
    ): NowPlayingVerification {
        return NowPlayingVerification(rule).apply(block)
    }
}

class NowPlayingVerification(
    private val rule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>
) {
    fun nowPlayingAppBarIsDisplayed() {
        val toolbarDesc = rule.activity.getString(R.string.top_app_bar)
        rule.onNodeWithContentDescription(toolbarDesc)
            .assertIsDisplayed()

        val backArrowDesc = rule.activity.getString(R.string.back_arrow)
        rule.onNodeWithContentDescription(backArrowDesc)
            .assertIsDisplayed()

        val overFlowDesc = rule.activity.getString(R.string.over_flow)
        rule.onNodeWithContentDescription(overFlowDesc)
            .assertIsDisplayed()
    }

    fun nowPlayingAlbumArtIsDisplayed() {
        val albumArtDesc = rule.activity.getString(R.string.album_art)
        rule.onNodeWithContentDescription(albumArtDesc)
            .assertIsDisplayed()
    }

    fun trackInfoIsDisplayed(track: Track) {
        rule.onNodeWithText(track.trackTitle)
            .assertIsDisplayed()

        rule.onNodeWithText(track.artist)
            .assertIsDisplayed()

        rule.onNodeWithText(formatTrackDuration(track.trackLength.toLong()))
            .assertIsDisplayed()

        rule.onNodeWithText(formatTrackDuration(0))
            .assertIsDisplayed()
    }

    fun nowPlayingControlsAreDisplayed() {
        val seekBarDesc = rule.activity.getString(R.string.seek_bar)
        rule.onNodeWithContentDescription(seekBarDesc)
            .assertIsDisplayed()

        val previousBtnDesc = rule.activity.getString(R.string.previous_track_button)
        rule.onNodeWithContentDescription(previousBtnDesc)
            .assertIsDisplayed()

        val playPauseBtnDesc = rule.activity.getString(R.string.play_pause_button)
        rule.onNodeWithContentDescription(playPauseBtnDesc)
            .assertIsDisplayed()

        val nextBtnDesc = rule.activity.getString(R.string.next_track_button)
        rule.onNodeWithContentDescription(nextBtnDesc)
            .assertIsDisplayed()

        val shuffleDesc = rule.activity.getString(R.string.shuffle_button)
        rule.onNodeWithContentDescription(shuffleDesc)
            .assertIsDisplayed()

        val repeatDesc = rule.activity.getString(R.string.repeat_button)
        rule.onNodeWithContentDescription(repeatDesc)
            .assertIsDisplayed()
    }

}