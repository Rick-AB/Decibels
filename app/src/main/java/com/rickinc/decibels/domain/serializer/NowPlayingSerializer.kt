package com.rickinc.decibels.domain.serializer

import androidx.datastore.core.Serializer
import com.rickinc.decibels.domain.model.NowPlaying
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object NowPlayingSerializer : Serializer<NowPlaying?> {

    override val defaultValue: NowPlaying?
        get() = null

    override suspend fun readFrom(input: InputStream): NowPlaying? {
        return try {
            Json.decodeFromString(
                deserializer = NowPlaying.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: NowPlaying?, output: OutputStream) {
        if (t == null) return
        output.write(
            Json.encodeToString(
                serializer = NowPlaying.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}