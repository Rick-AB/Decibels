package com.rickinc.decibels.data.datasource.network


import com.rickinc.decibels.domain.exception.LyricsNotFoundException
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.presentation.util.cleanTrackInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.net.URI

data class SongInfo(val title: String, val artist: String?, val lyrics: String, val source: String?)

class LyricsScraper  {
    companion object {
        val clearSpecialCharactersAndURL = "[|\\[\\]{}<>@].*".toRegex()
        val clearTrackExtras =
            "(([\\[(])(?!.*?(remix|edit|remake)).*?([])])|/|-| x |&|,|\"|video official|official lyric video| feat.? |ft.?|\\|+|yhlqmdlg|x100pre|[\uD83D\uDC00-\uD83D\uDDFF]|\u274C)".toRegex()
        val clearRegex = " {2,}".toRegex()
    }

    suspend fun getLyrics(track: Track): String {
        return withContext(Dispatchers.IO) {
            val formattedQuery = track.cleanTrackInfo()
            val encodedQuery = URI(null, null, formattedQuery, null).rawPath
            val url = "https://www.google.com/search?q=${encodedQuery}+lyrics&ie=UTF-8&tob=true"
            val doc: Document = Jsoup.connect(url).get() //Z1hOCe
            Timber.d(
                "LyricsScraper Lyrics:: ${
                    doc.getElementsByAttribute("data-lyricid").firstOrNull()?.text()
                }"
            )
            val lyrics = doc.getElementsByAttribute("data-lyricid").firstOrNull()?.text()
            val title = doc.select("span[class=BNeawe tAd8D AP7Wnd]").firstOrNull()?.text()
            val artist = doc.select("span[class=BNeawe s3v9rd AP7Wnd]").getOrNull(1)?.text()
            val source = doc.select("span[class=uEec3 AP7Wnd]").firstOrNull()?.text()

            if (lyrics == null) {
                throw LyricsNotFoundException("Lyrics not found!")
            }
            lyrics //SongInfo(title = title ?: "", artist = artist, lyrics = lyrics, source = source)
        }
    }
}