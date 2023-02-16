package com.rickinc.decibels.di

import com.rickinc.decibels.data.datasource.local.database.DecibelsDatabase
import com.rickinc.decibels.data.datasource.local.device.DeviceDataSource
import com.rickinc.decibels.data.repository.AudioRepositoryImpl
import com.rickinc.decibels.domain.repository.AudioRepository
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
        decibelsDatabase: DecibelsDatabase
    ): AudioRepository {
        return AudioRepositoryImpl(deviceDataSource, decibelsDatabase)
    }
}