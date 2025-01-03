package com.rickinc.decibels.presentation.components.accomponistpermision

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permissions the permissions to control and observe.
 */
@Composable
internal fun rememberMutableMultiplePermissionsState(
    permissions: List<String>
): MultiplePermissionsState {
    // Create mutable permissions that can be requested individually
    val mutablePermissions = rememberMutablePermissionsState(permissions)
    // Refresh permissions when the lifecycle is resumed.
    PermissionsLifecycleCheckerEffect(mutablePermissions)

    val multiplePermissionsState = remember(permissions) {
        MutableMultiplePermissionsState(mutablePermissions)
    }

    // Remember RequestMultiplePermissions launcher and assign it to multiplePermissionsState
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        multiplePermissionsState.updatePermissionsStatus(permissionsResult)
        multiplePermissionsState.permissionRequested = true
    }
    DisposableEffect(multiplePermissionsState, launcher) {
        multiplePermissionsState.launcher = launcher
        onDispose {
            multiplePermissionsState.launcher = null
        }
    }

    return multiplePermissionsState
}

@Composable
private fun rememberMutablePermissionsState(
    permissions: List<String>
): List<MutablePermissionState> {
    // Create list of MutablePermissionState for each permission
    val context = LocalContext.current
    val activity = context.findActivity()
    val mutablePermissions: List<MutablePermissionState> = remember(permissions) {
        permissions.map { MutablePermissionState(it, context, activity) }
    }
    // Update each permission with its own launcher
    for (permissionState in mutablePermissions) {
        key(permissionState.permission) {
            // Remember launcher and assign it to the permissionState
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                permissionState.hasPermission = it
            }
            DisposableEffect(launcher) {
                permissionState.launcher = launcher
                onDispose {
                    permissionState.launcher = null
                }
            }
        }
    }

    return mutablePermissions
}

/**
 * A state object that can be hoisted to control and observe multiple permission status changes.
 *
 * In most cases, this will be created via [rememberMutableMultiplePermissionsState].
 *
 * @param mutablePermissions list of mutable permissions to control and observe.
 */
@Stable
internal class MutableMultiplePermissionsState(
    private val mutablePermissions: List<MutablePermissionState>
) : MultiplePermissionsState {

    override val permissions: List<PermissionState> = mutablePermissions

    override val revokedPermissions: List<PermissionState> by derivedStateOf {
        permissions.filter { !it.hasPermission }
    }

    override val allPermissionsGranted: Boolean by derivedStateOf {
        permissions.all { it.hasPermission } || // Up to date when the lifecycle is resumed
            revokedPermissions.isEmpty() // Up to date when the user launches the action
    }

    override val shouldShowRationale: Boolean by derivedStateOf {
        permissions.any { it.shouldShowRationale }
    }

    override var permissionRequested: Boolean by mutableStateOf(false)

    override fun launchMultiplePermissionRequest() {
        launcher?.launch(
            permissions.map { it.permission }.toTypedArray()
        ) ?: throw IllegalStateException("ActivityResultLauncher cannot be null")
    }

    internal var launcher: ActivityResultLauncher<Array<String>>? = null

    internal fun updatePermissionsStatus(permissionsStatus: Map<String, Boolean>) {
        // Update all permissions with the result
        for (permission in permissionsStatus.keys) {
            mutablePermissions.firstOrNull { it.permission == permission }?.apply {
                permissionsStatus[permission]?.let { granted ->
                    this.hasPermission = granted
                }
            }
        }
    }
}
