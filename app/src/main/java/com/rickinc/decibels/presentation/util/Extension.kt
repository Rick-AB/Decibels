package com.rickinc.decibels.presentation.util

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import com.rickinc.decibels.data.datasource.network.LyricsScraper.Companion.clearRegex
import com.rickinc.decibels.data.datasource.network.LyricsScraper.Companion.clearSpecialCharactersAndURL
import com.rickinc.decibels.data.datasource.network.LyricsScraper.Companion.clearTrackExtras
import com.rickinc.decibels.domain.model.Track

fun Modifier.modifyIf(condition: Boolean, modify: Modifier.() -> Modifier): Modifier {
    return if (condition) modify() else this
}

fun Modifier.clickable(shape: Shape, enabled: Boolean = true, onClick: () -> Unit) =
    clip(shape).clickable(enabled = enabled, onClick = onClick)

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