package com.rickinc.decibels.di

import com.rickinc.decibels.data.repository.TestAudioRepository
import com.rickinc.decibels.domain.repository.AudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

//@Module
//@TestInstallIn(
//    components = [SingletonComponent::class],
//    replaces = [RepositoryModule::class]
//)
//class TestAudioRepositoryModule {
//
//    @Provides
//    @Singleton
//    fun provideTestRepository(): AudioRepository {
//        return TestAudioRepository()
//    }
//}