package com.rickinc.decibels.domain.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri


data class Track(
    val trackId: Long,
    val trackTitle: String,
    val trackLength: Int,
    val artist: String,
    val albumId: Long,
    val contentUri: Uri?,
    val thumbnail: Bitmap?
) {
    companion object {
        fun getUniqueTrackList(): List<Track> {
            val track1 = Track(0, "Pride is the devil", 40000, "J.Cole", 4L, Uri.EMPTY, null)
            val track2 = Track(1, "Clouds", 40000, "NF", 5L, Uri.EMPTY, null)
            val track3 = Track(2, "Trust", 40000, "NF", 6L, Uri.EMPTY, null)
            return listOf(track1, track2, track3)
        }

        fun getSingleTrack(position: Int = 0): Track {
            val tracks = getUniqueTrackList()
            return if (position > tracks.lastIndex) tracks[tracks.lastIndex] else tracks[position]
        }

        private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                if (drawable.bitmap != null) {
                    return drawable.bitmap
                }
            }
            val bitmap: Bitmap =
                if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                    Bitmap.createBitmap(
                        1,
                        1,
                        Bitmap.Config.ARGB_8888
                    ) // Single color bitmap will be created of 1x1 pixel
                } else {
                    Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}
