package com.rickinc.decibels.tracklist

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rickinc.decibels.presentation.MainActivity
import org.junit.Rule
import org.junit.Test

class TrackListTest {

    @get:Rule
    val songsListTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun displayTrackListScreen() {
        launchTrackListScreen(songsListTestRule) {

        } verify {
            trackListScreenIsVisible()
        }
    }
}