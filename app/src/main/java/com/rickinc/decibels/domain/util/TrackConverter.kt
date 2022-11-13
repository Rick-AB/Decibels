package com.rickinc.decibels.domain.util

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.rickinc.decibels.domain.model.Track
import javax.inject.Inject

class TrackConverter @Inject constructor() {

    companion object {
        const val CONTENT_URI_KEY = "content_uri"
        const val TRACK_LENGTH_KEY = "track_length"
        const val ALBUM_ID_KEY = "album_id"
        const val THUMBNAIL_KEY = "thumbnail"
        const val MP3 = "mp3"
    }

    fun toMediaItem(track: Track): MediaItem {
        val extra = Bundle()
        extra.putString(CONTENT_URI_KEY, track.contentUri.toString())
        extra.putInt(TRACK_LENGTH_KEY, track.trackLength)
        extra.putLong(ALBUM_ID_KEY, track.albumId)
        extra.putString(MP3, track.mimeType)
        extra.putParcelable(THUMBNAIL_KEY, track.thumbnail)

        val mediaMetadata =
            MediaMetadata.Builder().setDisplayTitle(track.trackTitle)
                .setTitle(track.trackTitle)
                .setArtist(track.artist)
                .setExtras(extra)
                .build()

        val trackId = track.trackId.toString()
        return MediaItem.Builder()
            .setMediaMetadata(mediaMetadata)
            .setMediaId(trackId)
            .build()
    }

    fun toMediaItems(tracks: List<Track>): List<MediaItem> {
        val mediaItems = tracks.map {
            toMediaItem(it)
        }

        return mediaItems
    }

    @Suppress("DEPRECATION")
    fun toTrack(currentMediaItem: MediaItem): Track {
        val metaData = currentMediaItem.mediaMetadata
        val bundle = metaData.extras

        val trackId = currentMediaItem.mediaId.toLong()
        val trackTitle = metaData.displayTitle.toString()
        val trackLength = bundle?.getInt(TRACK_LENGTH_KEY)!!
        val artist = metaData.artist.toString()
        val albumId = bundle.getLong(ALBUM_ID_KEY)
        val contentUri = Uri.parse(bundle.getString(CONTENT_URI_KEY))
        val thumbnail = if (Build.VERSION.SDK_INT >= 33) {
            bundle.getParcelable(THUMBNAIL_KEY, Bitmap::class.java)
        } else {
            bundle.getParcelable(THUMBNAIL_KEY)
        }
        val mimeType = bundle.getString(MP3)

        return Track(
            trackId,
            trackTitle,
            trackLength,
            artist,
            albumId,
            contentUri,
            thumbnail,
            mimeType
        )
    }
}










