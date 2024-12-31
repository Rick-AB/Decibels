package com.rickinc.decibels.data.datasource.local.device

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.util.TrackConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceDataSource(private val context: Context) {
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun getDeviceAudioFiles(): List<Track> {
        return withContext(Dispatchers.IO) {
            val tracks = mutableListOf<Track>()
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
            )

            val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            val query = context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )
            val albumArtStorage = Uri.parse("content://media/external/audio/albumart")
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val duration = cursor.getInt(durationColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val mimeType = MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(context.contentResolver.getType(contentUri))
                    if (mimeType == TrackConverter.MP3) {
                        tracks.add(
                            Track(
                                id = id,
                                title = title,
                                trackLength = duration,
                                artist = artist,
                                albumId = albumId,
                                contentUri = contentUri,
                                mimeType = mimeType,
                                hasThumbnail = false,
                                thumbnailUri = ContentUris.withAppendedId(albumArtStorage, albumId)
                            )
                        )
                    }
                }
            }
            tracks
        }
    }

    fun deleteAudioFileFromDevice(context: Context, trackId: Long, contentUri: Uri) {
        context.contentResolver.delete(
            contentUri,
            "${MediaStore.Audio.Media._ID} = ?",
            arrayOf(trackId.toString())
        )
    }
}
