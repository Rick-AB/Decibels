package com.rickinc.decibels.data.datasource.local.database.typeconverter

import android.net.Uri
import androidx.room.TypeConverter

class UriTypeConverter {

    @TypeConverter
    fun uriFromJson(value: String?): Uri {
        return Uri.parse(value)
    }

    @TypeConverter
    fun uriToJson(uri: Uri?): String {
        return uri.toString()
    }
}