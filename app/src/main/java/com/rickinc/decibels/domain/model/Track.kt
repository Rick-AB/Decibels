package com.rickinc.decibels.domain.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rickinc.decibels.domain.serializer.BitmapSerializer
import com.rickinc.decibels.domain.serializer.UriAsStringSerializer
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Track(
    @PrimaryKey(autoGenerate = false)
    val trackId: Long,
    val trackTitle: String,
    val trackLength: Int,
    val artist: String,
    val albumId: Long,
    @Serializable(with = UriAsStringSerializer::class)
    val contentUri: Uri?,
    @Serializable(with = BitmapSerializer::class)
    val thumbnail: Bitmap? = null,
    val mimeType: String?
) {
    companion object {
        fun getUniqueTrackList(): List<Track> {
            val track1 = Track(0, "Pride is the devil", 40000, "J.Cole", 4L, Uri.EMPTY, null, null)
            val track2 = Track(1, "Clouds", 40000, "NF", 5L, Uri.EMPTY, null, null)
            val track3 = Track(2, "Trust", 40000, "NF", 6L, Uri.EMPTY, null, null)
            return listOf(track1, track2, track3)
        }

        fun getSingleTrack(position: Int = 0): Track {
            val tracks = getUniqueTrackList()
            return if (position > tracks.lastIndex) tracks[tracks.lastIndex] else tracks[position]
        }
    }
}
