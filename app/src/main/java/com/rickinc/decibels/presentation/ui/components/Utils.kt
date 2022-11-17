package com.rickinc.decibels.presentation.ui.components

import com.rickinc.decibels.presentation.ui.components.accomponistpermision.PermissionState

fun PermissionState.isPermanentlyDenied(): Boolean {
    return !shouldShowRationale && !hasPermission
}