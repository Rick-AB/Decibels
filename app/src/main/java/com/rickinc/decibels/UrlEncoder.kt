package com.rickinc.decibels

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class UrlEncoder {
    fun encode(url: String): String {
        return URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
    }
    fun decode(url: String): String {
        return URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
    }
}
