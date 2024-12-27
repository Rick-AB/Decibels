package com.rickinc.decibels.di

import com.rickinc.decibels.data.datasource.local.database.DecibelsDatabase
import com.rickinc.decibels.data.datasource.local.device.DeviceDataSource
import com.rickinc.decibels.data.datasource.network.LyricsApiService
import com.rickinc.decibels.data.repository.TrackRepositoryImpl
import com.rickinc.decibels.domain.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideAudioRepository(
        deviceDataSource: DeviceDataSource,
        lyricsApiService: LyricsApiService,
        decibelsDatabase: DecibelsDatabase
    ): TrackRepository {
        return TrackRepositoryImpl(deviceDataSource, lyricsApiService, decibelsDatabase)
    }
}