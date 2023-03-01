package com.rickinc.decibels.data.repository

import android.content.Context
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.rickinc.decibels.data.datasource.local.database.DecibelsDatabase
import com.rickinc.decibels.data.datasource.local.device.DeviceDataSource
import com.rickinc.decibels.data.datasource.network.LyricsApiService
import com.rickinc.decibels.domain.model.NowPlaying
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.util.UploadStreamRequestBody
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.HttpException


class AudioRepositoryImpl(
    private val deviceDataSource: DeviceDataSource,
    private val lyricsApiService: LyricsApiService,
    decibelsDatabase: DecibelsDatabase
) : AudioRepository {
    private val dao = decibelsDatabase.dao

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun getAudioFiles(): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            Result.Success(deviceDataSource.getDeviceAudioFiles())
        }
    }

    override suspend fun updateNowPlaying(nowPlaying: NowPlaying) {
        dao.updateNowPlaying(nowPlaying)
    }

    override fun getNowPlayingFlow(): Flow<NowPlaying?> = dao.getNowPlaying()

    override fun deleteTrack(context: Context, track: Track) {
        deviceDataSource.deleteAudioFileFromDevice(context, track)
    }

    override suspend fun getLyricsForTrack(context: Context, track: Track): Result<String> {
        return try {
            var mimeType = ""

            val stream = track.contentUri?.let {
                mimeType = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(context.contentResolver.getType(it)).toString()
                context.contentResolver.openInputStream(it)
            } ?: return Result.Error("Unknown")

            val request = UploadStreamRequestBody("audio/*", stream)
            val filePart = MultipartBody.Part.createFormData(
                "file",
                "${track.trackTitle}.$mimeType",
                request
            )
            val response = lyricsApiService.getLyricsForSong(filePart)
            Result.Success(response.lrcContent)
        } catch (e: HttpException) {
            Result.Error("Could not find lyrics for the song ${track.trackTitle}")
        }
    }
}