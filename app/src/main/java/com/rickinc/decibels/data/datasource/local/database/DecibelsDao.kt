package com.rickinc.decibels.data.datasource.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rickinc.decibels.domain.model.NowPlaying
import kotlinx.coroutines.flow.Flow

@Dao
interface DecibelsDao {

    @Query("SELECT * FROM NowPlaying")
    fun getNowPlaying(): Flow<NowPlaying>

    @Insert(NowPlaying::class, onConflict = OnConflictStrategy.REPLACE)
    fun updateNowPlaying(nowPlaying: NowPlaying)
}