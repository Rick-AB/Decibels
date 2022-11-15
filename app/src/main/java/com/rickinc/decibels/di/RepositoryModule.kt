package com.rickinc.decibels.di

import android.content.Context
import com.rickinc.decibels.data.repository.AudioRepositoryImpl
import com.rickinc.decibels.domain.repository.AudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideAudioRepository(
        @ApplicationContext context: Context,
    ): AudioRepository {
        return AudioRepositoryImpl(context)
    }
}