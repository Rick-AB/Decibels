package com.rickinc.decibels.data.datasource.network.dto

import com.squareup.moshi.Json

data class HttpErrorDetail(
    @Json(name = "error_code")
    val errorCode: Int,
    @Json(name = "error_message")
    val errorMessage: String
)
