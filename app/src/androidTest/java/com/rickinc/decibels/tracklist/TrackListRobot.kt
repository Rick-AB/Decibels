package com.rickinc.decibels.tracklist

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.rickinc.decibels.presentation.MainActivity
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track

fun launchTrackListScreen(
    rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
    block: SongsListRobot.() -> Unit
): SongsListRobot {
    return SongsListRobot(rule).apply(block)
}

class SongsListRobot(
    private val rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    infix fun verify(
        block: SongsListVerification.() -> Unit
    ): SongsListVerification {
        return SongsListVerification(rule).apply(block)
    }

    fun selectFirstTrack() {
        val trackListContentDesc = rule.activity.getString(R.string.track_list)
        rule.onNodeWithContentDescription(trackListContentDesc)
            .onChildAt(0)
            .performClick()
    }
}


class SongsListVerification(
    private val rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    fun trackListScreenIsDisplayed() {
        val myMusicText = rule.activity.getString(R.string.myMusic)
        rule.onNodeWithText(myMusicText)
            .assertIsDisplayed()
    }

    fun trackListIsDisplayed() {
        val trackListContentDesc = rule.activity.getString(R.string.track_list)
        rule.onNodeWithContentDescription(trackListContentDesc)
            .assertIsDisplayed()
    }

    fun trackListChildrenAreDisplayed() {
        val trackListContentDesc = rule.activity.getString(R.string.track_list)
        rule.onNodeWithContentDescription(trackListContentDesc)
            .onChildren()
            .assertCountEquals(3)
    }

    fun trackListItemsAreClickable() {
        val trackListContentDesc = rule.activity.getString(R.string.track_list)
        rule.onNodeWithContentDescription(trackListContentDesc)
            .onChildren()
            .assertAll(hasClickAction())
    }

    fun errorScreenIsDisplayed() {
        val errorText = rule.activity.getString(R.string.error_loading_audio_files)
        rule.onNodeWithText(errorText)
            .assertExists()
            .assertIsDisplayed()
    }

    fun emptyScreenIsDisplayed() {
        val emptyText = rule.activity.getString(R.string.empty_track_list)
        rule.onNodeWithText(emptyText)
            .assertExists()
            .assertIsDisplayed()
    }

    fun nowPlayingScreenIsDisplayed() {
        val nowPlayingToolbarContentDesc =
            rule.activity.getString(R.string.now_playing_toolbar_content_desc)

        rule.onNodeWithContentDescription(nowPlayingToolbarContentDesc)
            .assertExists()
            .assertIsDisplayed()
    }

    fun trackInfoIsDisplayed(track: Track) {
        rule.onNodeWithText(track.trackName)
            .assertExists()
            .assertIsDisplayed()

        rule.onNodeWithText(track.artist)
            .assertExists()
            .assertIsDisplayed()
    }
}