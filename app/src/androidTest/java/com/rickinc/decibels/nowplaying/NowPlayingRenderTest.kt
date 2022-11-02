package com.rickinc.decibels.nowplaying

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.nowplaying.NowPlayingUiState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NowPlayingRenderTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val testRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private val track = Track.getSingleTrack()
    private val uiState = NowPlayingUiState(track)

    @Test
    fun displayNowPlayingTopAppBar() {
        launchNowPlayingScreen(testRule, uiState) {
            // no operation
        } verify {
            nowPlayingAppBarIsDisplayed()
        }
    }

    @Test
    fun displayNowPlayingAlbumArt() {
        launchNowPlayingScreen(testRule, uiState) {
            // no operation
        } verify {
            nowPlayingAlbumArtIsDisplayed()
        }
    }

    @Test
    fun displayTrackInfo(){
        launchNowPlayingScreen(testRule, uiState) {
            // no operation
        } verify {
            trackInfoIsDisplayed(track)
        }
    }

    @Test
    fun displayNowPlayingControls(){
        launchNowPlayingScreen(testRule, uiState) {
            // no operation
        } verify {
            nowPlayingControlsAreDisplayed()
        }
    }
}