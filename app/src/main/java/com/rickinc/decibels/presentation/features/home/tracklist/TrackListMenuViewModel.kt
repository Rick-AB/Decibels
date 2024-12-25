package com.rickinc.decibels.presentation.features.home.tracklist

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.rickinc.decibels.BuildConfig
import com.rickinc.decibels.R
import com.rickinc.decibels.domain.model.Track
import com.rickinc.decibels.domain.repository.AudioRepository
import com.rickinc.decibels.domain.util.RingtoneUtil
import com.rickinc.decibels.presentation.util.getRealPathFromURI
import com.rickinc.decibels.presentation.util.showShortToast
import java.io.File

class TrackListMenuViewModel(
    private val ringtoneUtil: RingtoneUtil,
    private val audioRepository: AudioRepository
) : ViewModel() {

    fun deleteTrack(context: Context, trackId: Long, contentUri: Uri?): IntentSenderRequest? {
        if (contentUri == null) return null

        try {
            audioRepository.deleteTrack(context, trackId, contentUri)
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

    fun shareFile(context: Context, contentUri: Uri?) {
        val trackPath = contentUri?.let {
            getRealPathFromURI(context, it)
        } ?: return
        val file = File(trackPath)

        // Use the FileProvider to get a content URI
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider",
            file
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

    fun setAsRingtone(context: Context, contentUri: Uri?) {
        val trackPath = contentUri?.let {
            getRealPathFromURI(context, it)
        } ?: return
        val file = File(trackPath)
        val success = ringtoneUtil.setAsRingtone(context, file)
        if (success) context.showShortToast(R.string.ringtone_set)
        else context.showShortToast(R.string.error_setting_ringtone)
    }
}