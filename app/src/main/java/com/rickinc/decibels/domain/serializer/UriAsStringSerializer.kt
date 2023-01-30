package com.rickinc.decibels.domain.serializer

import android.net.Uri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UriAsStringSerializer : KSerializer<Uri?> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Uri? {
        return Uri.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Uri?) {
        encoder.encodeString(value.toString())
    }
}