package com.rickinc.decibels.presentation.ui.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.material3.SheetValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
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

fun Modifier.modifyIf(condition: Boolean, modify: Modifier.() -> Modifier): Modifier {
    return if (condition) modify() else this
}

fun Modifier.clickable(shape: Shape, enabled: Boolean = true, onClick: () -> Unit) =
    clip(shape).clickable(enabled = enabled, onClick = onClick)

val androidx.compose.material3.BottomSheetScaffoldState.currentFraction: Float
    get() {
        val fraction = bottomSheetState.requireOffset()
        val targetValue = bottomSheetState.targetValue
        val currentValue = bottomSheetState.currentValue

        return when {
            currentValue == SheetValue.Hidden && targetValue == SheetValue.Hidden -> 0f
            currentValue == SheetValue.Expanded && targetValue == SheetValue.Expanded -> 1f
            currentValue == SheetValue.Hidden && targetValue == SheetValue.Expanded -> fraction
            else -> 1f - fraction
        }
    }
//val BottomSheetScaffoldState.currentFraction: Float
//    get() {
//        val fraction = bottomSheetState.progress
//        val targetValue = bottomSheetState.targetValue
//        val currentValue = bottomSheetState.currentValue
//
//        return when {
//            currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Collapsed -> 0f
//            currentValue == BottomSheetValue.Expanded && targetValue == BottomSheetValue.Expanded -> 1f
//            currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Expanded -> fraction
//            else -> 1f - fraction
//        }
//    }