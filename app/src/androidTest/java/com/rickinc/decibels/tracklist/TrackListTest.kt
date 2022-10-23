package com.rickinc.decibels.tracklist

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rickinc.decibels.data.repository.TestAudioRepository
import com.rickinc.decibels.di.RepositoryModule
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.presentation.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Singleton

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class TrackListTest {

    @Module
    @InstallIn(SingletonComponent::class)
    class TestModule {

        @Provides
        @Singleton
        fun provideTestAudioRepository(): AudioRepository {
            return TestAudioRepository()
        }
    }

    @get:Rule(order = 0)
    var hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val trackListTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltTestRule.inject()
    }

    @Test
    fun displayTrackListScreen() {
        launchTrackListScreen(trackListTestRule) {
            // no operation
        } verify {
            trackListScreenIsDisplayed()
        }
    }

    @Test
    fun displayTrackListWhenDataLoaded() {
        launchTrackListScreen(trackListTestRule) {
            // no operation
        } verify {
            trackListIsDisplayed()
        }
    }

    @Test
    fun whenDataIsLoaded_listItemsDisplayTrackInfo() {
        val tracks = Track.getUniqueTrackList()
        launchTrackListScreen(trackListTestRule) {
            // no operation
        } verify {
            trackInfoIsDisplayed(tracks)
        }
    }

    @Test
    fun whenTrackListIsLoaded_tracksAreClickable() {
        launchTrackListScreen(trackListTestRule) {
            // no operation
        } verify {
            trackListItemsAreClickable()
        }
    }

    @Test
    fun whenSingleTrackIsClicked_nowPlayingScreenIsDisplayed() {
        launchTrackListScreen(trackListTestRule) {
            selectFirstTrack()
        } verify {
            nowPlayingScreenIsDisplayed()
        }
    }
}

