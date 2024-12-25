package com.rickinc.decibels.di

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.PowerManager
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.rickinc.decibels.data.datasource.local.database.DecibelsDatabase
import com.rickinc.decibels.data.datasource.local.device.DeviceDataSource
import com.rickinc.decibels.data.datasource.network.LyricsApiService
import com.rickinc.decibels.data.datasource.network.LyricsScraper
import com.rickinc.decibels.domain.util.RingtoneUtil
import com.rickinc.decibels.domain.util.TrackConverter
import com.rickinc.decibels.presentation.util.hasPermission
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providePlayer(
        @ApplicationContext context: Context
    ): Player {
        val hasWakeLockPermission = context.hasPermission(Manifest.permission.WAKE_LOCK)
        return ExoPlayer.Builder(context)
            .apply {
                if (hasWakeLockPermission) setWakeMode(PowerManager.PARTIAL_WAKE_LOCK)
            }
            .build()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context): DecibelsDatabase {
        return Room.databaseBuilder(appContext, DecibelsDatabase::class.java, "decibels.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideDeviceDataSource(@ApplicationContext appContext: Context): DeviceDataSource {
        return DeviceDataSource(appContext)
    }

    @Provides
    @Singleton
    fun providesOkhttp(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideLyricsApiService(httpClient: OkHttpClient): LyricsApiService {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.130:8000")//.baseUrl("http://10.0.2.2:8000")
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .addLast(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .client(httpClient)
            .build()
            .create(LyricsApiService::class.java)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SharedPreferencesEntryPoint {
        val sharedPreferences: SharedPreferences
    }
}

val appModule = module {
    single<Player> {
        val context = androidContext()
        val hasWakeLockPermission = context.hasPermission(Manifest.permission.WAKE_LOCK)
        ExoPlayer.Builder(context)
            .apply {
                if (hasWakeLockPermission) setWakeMode(PowerManager.PARTIAL_WAKE_LOCK)
            }
            .build()
    }

    singleOf(::TrackConverter)
    singleOf(::RingtoneUtil)
    includes(databaseModule, dataModule, networkModule, viewModelModule)
}