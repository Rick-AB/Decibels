package com.rickinc.decibels.presentation.tracklist.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.ui.theme.Typography

@Composable
fun PermissionRequiredBody(
    isPermanentlyDenied: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val messageRes =
            if (isPermanentlyDenied) R.string.storage_permission_permanently_denied_message
            else R.string.storage_permission_denied_message
        Text(
            text = stringResource(id = messageRes),
            style = Typography.bodyLarge,
            modifier = Modifier.align(Alignment.Center)
        )

        Button(
            onClick = onClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val buttonTextRes = if (isPermanentlyDenied) R.string.go_to_settings
            else R.string.grant_permission
            Text(text = stringResource(id = buttonTextRes), style = Typography.bodyMedium)
        }
    }
}