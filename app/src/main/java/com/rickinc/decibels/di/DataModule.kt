package com.rickinc.decibels.di

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.rickinc.decibels.data.datasource.local.device.DeviceDataSource
import com.rickinc.decibels.data.datasource.network.LyricsScraper
import com.rickinc.decibels.data.repository.AudioRepositoryImpl
import com.rickinc.decibels.domain.repository.AudioRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// provides repositories and dataSources
val dataModule = module {
    single { DeviceDataSource(androidContext()) }
    single<AudioRepository> { AudioRepositoryImpl(get(), get(), get()) }
    single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
    singleOf(::LyricsScraper)
}

