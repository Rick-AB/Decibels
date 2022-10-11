package com.rickinc.decibels.presentation.model

data class Track(
    val trackId: Long,
    val trackName: String,
    val trackLength: Int
) {
    companion object {
        fun getUniqueTrackList(): List<Track> {
            val track1 = Track(0, "Pride is the devil", 4000)
            val track2 = Track(1, "Clouds", 4000)
            val track3 = Track(2, "Trust", 4000)
            return listOf(track1, track2, track3)
        }
    }
}
