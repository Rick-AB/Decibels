package com.rickinc.decibels.data.datasource.network.dto

import com.squareup.moshi.Json

data class LyricsResponseDto(
    @Json(name = "lrc_content")
    val lrcContent: String,
    val message: String
)
