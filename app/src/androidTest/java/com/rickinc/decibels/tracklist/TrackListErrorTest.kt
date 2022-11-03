package com.rickinc.decibels.tracklist

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rickinc.decibels.di.RepositoryModule
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.presentation.MainActivity
import com.rickinc.decibels.domain.model.Track
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
class TrackListErrorTest {

    @Module
    @InstallIn(SingletonComponent::class)
    class TestErrorModule {

        @Provides
        @Singleton
        fun provideTestAudioErrorRepository(): AudioRepository {
            return TestAudioErrorRepository()
        }
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val trackListTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun whenLoadingFails_DisplayErrorScreen() {
        launchTrackListScreen(trackListTestRule) {

        } verify {
            errorScreenIsDisplayed()
        }
    }

    class TestAudioErrorRepository : AudioRepository {
        override suspend fun getAudioFiles(): Result<List<Track>> {
            return Result.Error("Failed to load audio files")
        }

    }
}
