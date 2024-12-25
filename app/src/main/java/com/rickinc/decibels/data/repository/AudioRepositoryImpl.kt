package com.rickinc.decibels.data.repository

import android.content.Context
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.rickinc.decibels.R
import com.rickinc.decibels.data.datasource.local.database.DecibelsDatabase
import com.rickinc.decibels.data.datasource.local.device.DeviceDataSource
import com.rickinc.decibels.data.datasource.network.LyricsApiService
import com.rickinc.decibels.data.util.RequestHandler
import com.rickinc.decibels.domain.exception.ErrorHolder
import com.rickinc.decibels.domain.model.NowPlaying
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.util.UploadStreamRequestBody
import com.rickinc.decibels.presentation.util.ConnectionState
import com.rickinc.decibels.presentation.util.currentConnectivityState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
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
        withContext(Dispatchers.IO) {
            dao.updateNowPlaying(nowPlaying)
        }
    }

    override fun getNowPlayingFlow(): Flow<NowPlaying?> = dao.getNowPlaying().distinctUntilChanged()

    override fun deleteTrack(context: Context, track: Track) {
        deviceDataSource.deleteAudioFileFromDevice(context, track)
    }

    override suspend fun getLyricsForTrack(context: Context, track: Track): Result<String> {
        if (context.currentConnectivityState == ConnectionState.Unavailable)
            return Result.Error(ErrorHolder.NetworkConnection(context.getString(R.string.no_internet_connection)))

        var mimeType = ""

        val stream = track.contentUri?.let {
            mimeType = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(context.contentResolver.getType(it)).toString()
            context.contentResolver.openInputStream(it)
        } ?: return Result.Error(ErrorHolder.Local(context.getString(R.string.unknown_error)))

        val request = UploadStreamRequestBody("audio/*", stream)
        val filePart = MultipartBody.Part.createFormData(
            "file",
            "${track.trackTitle}.$mimeType",
            request
        )
        val response = RequestHandler.safeApiCall(Dispatchers.IO) {
            lyricsApiService.getLyricsForSong(filePart).lrcContent
        }

        return response
    }
}