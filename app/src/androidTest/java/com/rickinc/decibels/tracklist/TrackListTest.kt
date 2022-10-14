package com.rickinc.decibels.tracklist

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rickinc.decibels.data.repository.TestAudioRepository
import com.rickinc.decibels.di.RepositoryModule
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

        } verify {
            trackListScreenIsVisible()
        }
    }

    @Test
    fun displayTrackListWhenDataLoaded() {
        launchTrackListScreen(trackListTestRule) {

        } verify {
            trackListIsVisible()
        }
    }

    @Test
    fun displayTrackListChildrenWhenDataIsLoaded() {
        launchTrackListScreen(trackListTestRule) {

        } verify {
            trackListChildrenIsVisible()
        }
    }

    @Test
    fun whenTrackListIsLoaded_TracksAreClickable() {
        launchTrackListScreen(trackListTestRule) {

        } verify {
            trackListItemsAreClickable()
        }
    }
}

