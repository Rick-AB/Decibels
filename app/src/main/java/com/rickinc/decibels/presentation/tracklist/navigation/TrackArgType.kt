package com.rickinc.decibels.presentation.tracklist.navigation

import com.google.gson.Gson
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.navigation.JsonNavType

class TrackArgType : JsonNavType<Track>() {
    override fun fromJsonParse(value: String): Track {
        return Gson().fromJson(value, Track::class.java)
    }

    override fun Track.getJsonParse(): String {
        return Gson().toJson(this)
    }
}