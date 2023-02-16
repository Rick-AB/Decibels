package com.rickinc.decibels.data.datasource.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rickinc.decibels.data.datasource.local.database.typeconverter.BitmapTypeConverter
import com.rickinc.decibels.data.datasource.local.database.typeconverter.UriTypeConverter
import com.rickinc.decibels.domain.model.NowPlaying
import com.rickinc.decibels.domain.model.Track

@TypeConverters(value = [UriTypeConverter::class, BitmapTypeConverter::class])
@Database(entities = [NowPlaying::class, Track::class], version = 1, exportSchema = false)
abstract class DecibelsDatabase : RoomDatabase() {
    abstract val dao: DecibelsDao
}