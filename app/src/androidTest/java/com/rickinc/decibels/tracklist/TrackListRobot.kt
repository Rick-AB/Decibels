package com.rickinc.decibels.tracklist

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.rickinc.decibels.presentation.MainActivity
import com.rickinc.decibels.R

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
}


class SongsListVerification(
    private val rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    fun trackListScreenIsVisible() {
        val myMusicText = rule.activity.getString(R.string.myMusic)
        rule.onNodeWithText(myMusicText)
            .assertIsDisplayed()
    }

    fun trackListIsVisible() {
        val trackListContentDesc = rule.activity.getString(R.string.track_list)
        rule.onNodeWithContentDescription(trackListContentDesc)
            .onChildren()
            .assertCountEquals(3)
    }
}