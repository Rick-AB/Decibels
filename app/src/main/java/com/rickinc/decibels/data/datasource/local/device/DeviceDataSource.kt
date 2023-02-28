package com.rickinc.decibels.data.datasource.local.device

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.util.TrackConverter
import kotlinx.coroutines.*
import java.io.IOException

class DeviceDataSource(
    private val context: Context,
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun getDeviceAudioFiles(): List<Track> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<Track>()
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
                        list.add(
                            Track(
                                trackId = id,
                                trackTitle = title,
                                trackLength = duration,
                                artist = artist,
                                albumId = albumId,
                                contentUri = contentUri,
                                mimeType = mimeType,
                                hasThumbnail = false
                            )
                        )
                    }
                }
            }
            val tracksWithThumbnail = getTracksWithThumbnail(list)
            tracksWithThumbnail
        }
    }

    fun deleteAudioFileFromDevice(context: Context, track: Track) {
        if (track.contentUri == null) return

        context.contentResolver.delete(
            track.contentUri,
            "${MediaStore.Audio.Media._ID} = ?",
            arrayOf(track.trackId.toString())
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun getTracksWithThumbnail(tracks: List<Track>): List<Track> {
        val result: List<Track>
        coroutineScope {
            result = tracks.map {
                async {
                    val thumbnailResult = getThumbnailAfterQ(it.contentUri!!)
                    val thumbnail = thumbnailResult.first
                    val hasOriginalBitmap = thumbnailResult.second
                    it.copy(thumbnail = thumbnail, hasThumbnail = hasOriginalBitmap)
                }
            }.awaitAll()
        }

        return result
    }

    private fun getThumbnailBeforeQ(path: String): Bitmap? {
        return BitmapFactory.decodeFile(path)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getThumbnailAfterQ(contentUri: Uri): Pair<Bitmap, Boolean> {
        return try {
            val bitmap = context.contentResolver.loadThumbnail(contentUri, Size(300, 300), null)
            Pair(bitmap, true)
        } catch (e: IOException) {
            val drawable = ContextCompat.getDrawable(
                context,
                com.rickinc.decibels.R.drawable.ic_baseline_audio_file_24,
            )

            val bitmap = drawable!!.toBitmap(
                width = 300,
                height = 300,
                Bitmap.Config.ARGB_8888
            )
            Pair(bitmap, false)
        }
    }
}