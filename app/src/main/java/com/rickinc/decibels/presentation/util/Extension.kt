package com.rickinc.decibels.presentation.util

import android.util.Patterns
import com.rickinc.decibels.data.datasource.network.LyricsScraper.Companion.clearRegex
import com.rickinc.decibels.data.datasource.network.LyricsScraper.Companion.clearSpecialCharactersAndURL
import com.rickinc.decibels.data.datasource.network.LyricsScraper.Companion.clearTrackExtras
import com.rickinc.decibels.domain.model.Track

fun Track.cleanTrackInfo(): String {
    fun cleanInfo(info: String): String {
        return info.lowercase()
            .replace(clearSpecialCharactersAndURL, "")
            .replace(Patterns.WEB_URL.toRegex(), "")
            .replace(clearTrackExtras, "")
    }

    fun concatInfo(info: String): String {
        return info.replace(clearRegex, " ").trim().replace(" ", "+")
    }
    return concatInfo(cleanInfo(this.trackTitle) + " " + cleanInfo(this.artist))
}