package com.rickinc.decibels.domain.util

import android.content.ContentValues
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.rickinc.decibels.R
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class RingtoneUtil {

    fun setAsRingtone(context: Context, file: File): Boolean {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.TITLE, file.name)
        values.put(
            MediaStore.MediaColumns.MIME_TYPE,
            getMIMEType(file.absolutePath)
        )
        values.put(MediaStore.MediaColumns.SIZE, file.length())
        values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name)
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)

        val contentResolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val newUri = contentResolver
                .insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            try {
                contentResolver.openOutputStream(newUri).use { os ->
                    val size = file.length().toInt()
                    val bytes = ByteArray(size)
                    try {
                        val buf = BufferedInputStream(FileInputStream(file))
                        buf.read(bytes, 0, bytes.size)
                        buf.close()
                        os?.write(bytes)
                        os?.close()
                        os?.flush()
                    } catch (_: IOException) {
                    }
                }
            } catch (ignored: Exception) {
            }
            RingtoneManager.setActualDefaultRingtoneUri(
                context.applicationContext,
                RingtoneManager.TYPE_RINGTONE,
                newUri
            )

            return true
        } else {
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)

            val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath) ?: return false

            contentResolver.delete(
                uri,
                MediaStore.MediaColumns.DATA + "=\"" + file.absolutePath + "\"",
                null
            )


            val newUri = contentResolver.insert(uri, values)
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                newUri
            )

            contentResolver.insert(
                MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)!!,
                values
            )

            return true
        }
    }

    private fun getMIMEType(url: String?): String? {
        var mType: String? = null
        val mExtension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (mExtension != null) {
            mType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mExtension)
        }
        return mType
    }
}