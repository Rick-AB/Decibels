package com.rickinc.decibels.presentation.model

data class Track(
    val trackId: Int,
    val trackName: String,
    val trackLength: Long
) {
    companion object {
        fun createDummyTracks(): List<Track> {
            val track1 = Track(0, "Pride is the devil", 4000)
            val track2 = Track(1, "Clouds", 4000)
            val track3 = Track(2, "Trust", 4000)
            return listOf(track1, track2, track3)
        }
    }
}
