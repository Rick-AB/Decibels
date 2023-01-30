package com.rickinc.decibels.domain.model

import android.net.Uri
import com.rickinc.decibels.domain.serializer.UriAsStringSerializer
import kotlinx.serialization.Serializable

@Serializable
data class NowPlaying(
    val trackId: Long,
    val trackTitle: String,
    val trackLength: Int,
    val artist: String,
    val albumId: Long,

    @Serializable(with = UriAsStringSerializer::class)
    val contentUri: Uri?,
    val mimeType: String?
)
