package com.rickinc.decibels.domain.parser

import com.rickinc.decibels.domain.model.Lyrics

interface LyricsParser {
    fun parse(input: String): Lyrics?
}
