package com.rickinc.decibels.songslist

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rickinc.decibels.MainActivity
import org.junit.Rule
import org.junit.Test

class SongsListTest {

    @get:Rule
    val songsListTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun displaySongListScreen() {
        launchSongsListScreen(songsListTestRule) {

        } verify {
            songListScreenIsVisible()
        }
    }
}