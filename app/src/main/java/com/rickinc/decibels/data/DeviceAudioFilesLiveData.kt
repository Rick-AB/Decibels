package com.rickinc.decibels.data

import android.content.Context
import android.provider.MediaStore
import com.rickinc.decibels.data.local.device.DeviceDataSource
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.util.ContentProviderLiveData

class DeviceAudioFilesLiveData(
    context: Context,
    private val deviceDataSource: DeviceDataSource
) : ContentProviderLiveData<List<Track>>(context, uri) {

    companion object {
        private val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    override fun getContentProviderValue(): List<Track> {
        return emptyList()
    }
}