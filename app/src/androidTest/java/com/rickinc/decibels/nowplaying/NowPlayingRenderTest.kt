package com.rickinc.decibels.nowplaying

import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.service.DecibelPlaybackService
import com.rickinc.decibels.presentation.nowplaying.NowPlayingState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NowPlayingRenderTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    companion object {
        @get:Rule(order = 1)
        val testRule = createAndroidComposeRule<ComponentActivity>()

        private val track = Track.getSingleTrack()
        private val uiState = NowPlayingState.TrackLoaded(
            currentTrack = track,
            isPlaying = false,
            repeatMode = 0,
            isShuffleActive = false,
            progress = 0
        )

        private val sessionToken = SessionToken(
            testRule.activity,
            ComponentName(testRule.activity, DecibelPlaybackService::class.java)
        )

        private val controllerFuture =
            MediaController.Builder(testRule.activity, sessionToken).buildAsync()

        private lateinit var controller: MediaController

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            controller = controllerFuture.get()
        }
    }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displayNowPlayingTopAppBar() {
        launchNowPlayingScreen(testRule, uiState, controller) {
            // no operation
        } verify {
            nowPlayingAppBarIsDisplayed()
        }
    }

    @Test
    fun displayNowPlayingAlbumArt() {
        launchNowPlayingScreen(testRule, uiState, controller) {
            // no operation
        } verify {
            nowPlayingAlbumArtIsDisplayed()
        }
    }

    @Test
    fun displayTrackInfo() {
        launchNowPlayingScreen(testRule, uiState, controller) {
            // no operation
        } verify {
            trackInfoIsDisplayed(track)
        }
    }

    @Test
    fun displayNowPlayingControls() {
        launchNowPlayingScreen(testRule, uiState, controller) {
            // no operation
        } verify {
            nowPlayingControlsAreDisplayed()
        }
    }
}