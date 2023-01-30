package com.rickinc.decibels.domain.serializer

import android.graphics.Bitmap
import androidx.datastore.core.Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

object BitmapSerializer : Serializer<Bitmap?> {
    private var buffer: ByteBuffer? = null
    private var byteArray: ByteArray? = null

    override val defaultValue: Bitmap?
        get() = null

    override suspend fun readFrom(input: InputStream): Bitmap? {
        val rowBytes = input.read()
        val height = input.read()
        val width = input.read()
        val bitmapSize = input.read()

        if (byteArray == null || bitmapSize > byteArray!!.size)
            byteArray = ByteArray(bitmapSize)

        var offset = 0
        while (input.available() > 0) {
            offset += input.read(byteArray, offset, input.available())
        }

        if (buffer == null || bitmapSize > buffer!!.capacity())
            buffer = ByteBuffer.allocate(bitmapSize)

        buffer!!.position(0)
        buffer!!.put(byteArray!!)
        buffer!!.position(0)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    override suspend fun writeTo(t: Bitmap?, output: OutputStream) {
        if (t == null) return

        output.write(t.rowBytes)
        output.write(t.height)
        output.write(t.width)

        val bitmapSize = t.rowBytes * t.height
        if (buffer == null || bitmapSize > buffer!!.capacity())
            buffer = ByteBuffer.allocate(bitmapSize)

        output.write(buffer!!.capacity())

        buffer!!.position(0)

        t.copyPixelsToBuffer(buffer)
        if (byteArray == null || bitmapSize > byteArray!!.size)
            byteArray = ByteArray(bitmapSize)

        buffer!!.position(0)
        buffer!!.get(byteArray!!)

        output.write(byteArray, 0, byteArray!!.size)
    }
}

object VSeiralizer : KSerializer<Bitmap?> {
    private var buffer: ByteBuffer? = null
    private var byteArray: ByteArray? = null

    override fun deserialize(decoder: Decoder): Bitmap? {
        val rowBytes = decoder.decodeInt()
        val height = decoder.decodeInt()
        val width = decoder.decodeInt()
        val bitmapSize = decoder.decodeInt()

        if (byteArray == null || bitmapSize > byteArray!!.size)
            byteArray = ByteArray(bitmapSize)

        var offset = 0
//        while (decoder.) {
//            offset += input.read(byteArray, offset, input.available())
//        }

        if (buffer == null || bitmapSize > buffer!!.capacity())
            buffer = ByteBuffer.allocate(bitmapSize)

        buffer!!.position(0)
        buffer!!.put(byteArray!!)
        buffer!!.position(0)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun serialize(encoder: Encoder, value: Bitmap?) {
        val outputStream = ByteArrayOutputStream()
        if (value == null) return

        encoder.encodeInt(value.rowBytes)
        encoder.encodeInt(value.height)
        encoder.encodeInt(value.width)

        val bitmapSize = value.rowBytes * value.height
        if (buffer == null || bitmapSize > buffer!!.capacity())
            buffer = ByteBuffer.allocate(bitmapSize)

        encoder.encodeInt(buffer!!.capacity())

        buffer!!.position(0)

        value.copyPixelsToBuffer(buffer)
        if (byteArray == null || bitmapSize > byteArray!!.size)
            byteArray = ByteArray(bitmapSize)

        buffer!!.position(0)
        buffer!!.get(byteArray!!)

        encoder.encodeString(byteArray!!.decodeToString())
    }
}