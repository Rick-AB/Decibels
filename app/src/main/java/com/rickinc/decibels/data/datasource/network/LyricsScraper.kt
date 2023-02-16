package com.rickinc.decibels.data.datasource.network


import com.rickinc.decibels.domain.exception.LyricsNotFoundException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.util.*

data class SongInfo(val title: String, val artist: String?, val lyrics: String, val source: String?)

class LyricsScraper {
    companion object {
        private val clearRegex =
            "(([\\[(])(?!.*?(remix|edit|remake)).*?([])])|/|-| x |,|\"|video oficial|official lyric video| ft.?|\\|+|yhlqmdlg|x100pre|[\uD83D\uDC00-\uD83D\uDDFF]|\u274C)".toRegex()
        private val clearRegex2 = " {2,}".toRegex()
    }

    suspend fun getLyrics(song: String): SongInfo {
        val formattedSong =
            song.lowercase(Locale.ROOT).replace(clearRegex, "").replace(clearRegex2, " ").trim()
                .replace(" ", "+")

        Timber.d("LyricsScraper Title:: $formattedSong")
        val url = "https://www.google.com/search?q=${formattedSong}+lyrics&ie=UTF-8&tob=true"
        val doc: Document = Jsoup.connect(url).get()
        val lyrics =
            doc.select("div[class=hwc] div[class=BNeawe tAd8D AP7Wnd]").firstOrNull()?.text()

        val title = doc.select("span[class=BNeawe tAd8D AP7Wnd]").firstOrNull()?.text()
        val artist = doc.select("span[class=BNeawe s3v9rd AP7Wnd]").getOrNull(1)?.text()
        val source = doc.select("span[class=uEec3 AP7Wnd]").firstOrNull()?.text()

//        Timber.d("LyricsScraper Lyric:: $lyrics")
        if (lyrics == null) {
            throw LyricsNotFoundException("Lyrics not found!")
        }
        return SongInfo(title = title ?: "", artist = artist, lyrics = lyrics, source = source)
    }
}