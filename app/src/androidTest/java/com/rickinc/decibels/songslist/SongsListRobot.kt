package com.rickinc.decibels.songslist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.rickinc.decibels.presentation.MainActivity
import com.rickinc.decibels.R

fun launchSongsListScreen(
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
    fun songListScreenIsVisible() {
        val myMusicText = rule.activity.getString(R.string.myMusic)
        rule.onNodeWithText(myMusicText)
            .assertIsDisplayed()
    }
}