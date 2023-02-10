package com.rickinc.decibels.presentation.tracklist

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.rickinc.decibels.BuildConfig
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.presentation.util.getRealPathFromURI
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrackListMenuViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {

    fun deleteTrack(context: Context, track: Track): IntentSenderRequest? {
        try {
            audioRepository.deleteTrack(context, track)
        } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val recoverableSecurityException =
                    securityException as? RecoverableSecurityException
                        ?: throw securityException

                val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                return IntentSenderRequest.Builder(intentSender).build()
            } else {
                throw securityException
            }
        }
        return null
    }

    fun shareFile(context: Context, track: Track) {
        val trackPath = track.contentUri?.let {
            getRealPathFromURI(context, it)
        } ?: return
        val requestFile = File(trackPath)

        // Use the FileProvider to get a content URI
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider",
            requestFile
        )

        Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            setDataAndType(fileUri, context.contentResolver.getType(fileUri))
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.also {
            context.startActivity(Intent.createChooser(it, "Share Audio File"))
        }
    }
}