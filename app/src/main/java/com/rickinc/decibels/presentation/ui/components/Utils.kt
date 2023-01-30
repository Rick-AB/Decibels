package com.rickinc.decibels.presentation.ui.components

import android.content.Context
import android.content.SharedPreferences
import com.rickinc.decibels.presentation.ui.components.accomponistpermision.findActivity

fun isPermissionPermanentlyDenied(
    context: Context,
    sharedPreferences: SharedPreferences,
    permission: String
): Boolean {
    val previousShouldShowStatus = getRationaleDisplayStatus(sharedPreferences, permission)
    val currentShouldShowStatus =
        context.findActivity().shouldShowRequestPermissionRationale(permission)
    return previousShouldShowStatus != currentShouldShowStatus
}

fun setShouldShowRationaleStatus(sharedPreferences: SharedPreferences, permission: String) {
    sharedPreferences.edit().putBoolean(permission, true).apply()
}

fun getRationaleDisplayStatus(
    sharedPreferences: SharedPreferences, permission: String
): Boolean {
    return sharedPreferences.getBoolean(permission, false)
}