package com.rickinc.decibels.data.datasource.network

import com.rickinc.decibels.data.datasource.network.dto.LyricsResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface LyricsApiService {

    @Multipart
    @POST("/api/v1/lyrics")
    suspend fun getLyricsForSong(@Part body: MultipartBody.Part): LyricsResponseDto

}