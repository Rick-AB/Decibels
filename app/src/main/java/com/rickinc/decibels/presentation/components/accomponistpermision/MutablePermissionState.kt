package com.rickinc.decibels.presentation.components.accomponistpermision

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Creates a [MutablePermissionState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permission the permission to control and observe.
 */
@Composable
internal fun rememberMutablePermissionState(
    permission: String
): MutablePermissionState {
    val context = LocalContext.current
    val permissionState = remember(permission) {
        MutablePermissionState(permission, context, context.findActivity())
    }

    // Refresh the permission status when the lifecycle is resumed
    PermissionLifecycleCheckerEffect(permissionState)

    // Remember RequestPermission launcher and assign it to permissionState
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionState.hasPermission = it
        permissionState.permissionRequested = true
    }
    DisposableEffect(permissionState, launcher) {
        permissionState.launcher = launcher
        onDispose {
            permissionState.launcher = null
        }
    }

    return permissionState
}

/**
 * A mutable state object that can be used to control and observe permission status changes.
 *
 * In most cases, this will be created via [rememberMutablePermissionState].
 *
 * @param permission the permission to control and observe.
 * @param context to check the status of the [permission].
 * @param activity to check if the user should be presented with a rationale for [permission].
 */
@Stable
internal class MutablePermissionState(
    override val permission: String,
    private val context: Context,
    private val activity: Activity
) : PermissionState {

    private var _hasPermission by mutableStateOf(context.isPermissionGranted(permission))
    override var hasPermission: Boolean
        internal set(value) {
            _hasPermission = value
            refreshShouldShowRationale()
        }
        get() = _hasPermission

    override var shouldShowRationale: Boolean by mutableStateOf(
        activity.shouldShowRationale(permission)
    )
        private set

    override var permissionRequested: Boolean by mutableStateOf(false)

    override fun launchPermissionRequest() {
        launcher?.launch(
            permission
        ) ?: throw IllegalStateException("ActivityResultLauncher cannot be null")
    }

    internal var launcher: ActivityResultLauncher<String>? = null

    internal fun refreshHasPermission() {
        hasPermission = context.isPermissionGranted(permission)
    }

    private fun refreshShouldShowRationale() {
        shouldShowRationale = activity.shouldShowRationale(permission)
    }
}
