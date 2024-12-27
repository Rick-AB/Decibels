package com.rickinc.decibels.domain.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.GsonBuilder
import com.rickinc.decibels.domain.util.UriTypeAdapter

@Entity
data class Track(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val title: String,
    val trackLength: Int,
    val artist: String,
    val albumId: Long,
    val contentUri: Uri?,
    val thumbnail: Bitmap? = null,
    val mimeType: String?,
    val hasThumbnail: Boolean,
    val thumbnailUri: Uri? = null
) {
    companion object {
        fun getUniqueTrackList(): List<Track> {
            val track1 =
                Track(0, "Pride is the devil", 40000, "J.Cole", 4L, Uri.EMPTY, null, null, false)
            val track2 = Track(1, "Clouds", 40000, "NF", 5L, Uri.EMPTY, null, null, false)
            val track3 = Track(2, "Trust", 40000, "NF", 6L, Uri.EMPTY, null, null, false)
            return listOf(track1, track2, track3)
        }

        fun getSingleTrack(position: Int = 0): Track {
            val tracks = getUniqueTrackList()
            return if (position > tracks.lastIndex) tracks[tracks.lastIndex] else tracks[position]
        }
    }

    override fun toString(): String {
        val gson =
            GsonBuilder().registerTypeHierarchyAdapter(Uri::class.java, UriTypeAdapter()).create()
        return Uri.encode(gson.toJson(this))
    }
}
