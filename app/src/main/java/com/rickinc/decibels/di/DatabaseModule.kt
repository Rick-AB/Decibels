package com.rickinc.decibels.di

import androidx.room.Room
import com.rickinc.decibels.data.datasource.local.database.DecibelsDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), DecibelsDatabase::class.java, "decibels.db")
            .fallbackToDestructiveMigration()
            .build()
    }
}