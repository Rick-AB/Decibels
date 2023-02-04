package com.rickinc.decibels.data.repository

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.rickinc.decibels.data.local.database.DecibelsDatabase
import com.rickinc.decibels.domain.model.NowPlaying
import com.rickinc.decibels.domain.model.Result
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.util.TrackConverter.Companion.MP3
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.io.IOException


class AudioRepositoryImpl(
    private val context: Context,
    decibelsDatabase: DecibelsDatabase
) : AudioRepository {
    private val dao = decibelsDatabase.dao

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun getAudioFiles(): Result<List<Track>> {
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

                    if (mimeType == MP3) {
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
            Result.Success(tracksWithThumbnail)
        }
    }

    override suspend fun updateNowPlaying(nowPlaying: NowPlaying) {
        dao.updateNowPlaying(nowPlaying)
    }

    override fun getNowPlayingFlow(): Flow<NowPlaying?> = dao.getNowPlaying()

    override fun deleteTrack(context: Context, track: Track) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) deleteTrackAfterVersionQ(context, track)
        else deleteTrackBeforeVersionQ(context, track)
    }

    private fun deleteTrackBeforeVersionQ(context: Context, track: Track) {
        if (track.contentUri == null) return

        context.contentResolver.delete(
            track.contentUri,
            "${MediaStore.Audio.Media._ID} = ?",
            arrayOf(track.trackId.toString())
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun deleteTrackAfterVersionQ(context: Context, track: Track) {
        val pendingIntent =
            MediaStore.createDeleteRequest(context.contentResolver, listOf(track.contentUri))
        val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
        if (context is ComponentActivity) {
            val launcher =
                context.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {

                    }
                }
        }
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