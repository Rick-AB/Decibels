package com.rickinc.decibels.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.ui.theme.Typography

@Composable
fun RationaleDialog(
    @StringRes messageRes: Int,
    isPermanentlyDenied: Boolean,
    dismissDialog: () -> Unit,
    actionPositiveButtonClick: () -> Unit
) {
    Dialog(
        onDismissRequest = dismissDialog,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)) {
                Text(
                    text = stringResource(messageRes),
                    style = Typography.bodyMedium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    if (isPermanentlyDenied) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            style = Typography.bodyMedium,
                            modifier = Modifier
                                .clickable { dismissDialog() }
                                .padding(4.dp)
                        )
                    }

                    val textRes = if (isPermanentlyDenied) R.string.go_to_settings else R.string.ok
                    val onClick =
                        if (isPermanentlyDenied) actionPositiveButtonClick else dismissDialog
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = textRes),
                        style = Typography.bodyMedium,
                        modifier = Modifier
                            .clickable { onClick() }
                            .padding(4.dp)
                    )
                }
            }
        }

    }
}

@Preview
@Composable
fun RationalePrev() {
    RationaleDialog(
        messageRes = R.string.storage_permission_denied_message,
        isPermanentlyDenied = false,
        dismissDialog = { /*TODO*/ }) {

    }
}