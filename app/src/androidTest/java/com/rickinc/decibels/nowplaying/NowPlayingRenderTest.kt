package com.rickinc.decibels.nowplaying

import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.util.concurrent.ListenableFuture
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.service.DecibelPlaybackService
import com.rickinc.decibels.presentation.nowplaying.NowPlayingState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NowPlayingRenderTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val testRule = createAndroidComposeRule<ComponentActivity>()

    private val track = Track.getSingleTrack()
    private val uiState = NowPlayingState.TrackLoaded(
        track = track,
        isPlaying = false,
        repeatMode = 0,
        isShuffleActive = false,
        progress = 0
    )

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var sessionToken: SessionToken

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private lateinit var controller: MediaController

    @Before
    fun setup() {
        hiltRule.inject()
        sessionToken = SessionToken(
            context,
            ComponentName(context, DecibelPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controller = controllerFuture.get()
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