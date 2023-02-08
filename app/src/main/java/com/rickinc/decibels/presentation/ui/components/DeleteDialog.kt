package com.rickinc.decibels.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rickinc.decibels.R
import com.rickinc.decibels.presentation.ui.theme.secondaryVariant

@Composable
fun DeleteDialog(
    message: String,
    dismissDialog: () -> Unit,
    actionPositiveButtonClick: () -> Unit
) {
    Dialog(
        onDismissRequest = dismissDialog,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            backgroundColor = secondaryVariant,
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)) {
                Text(
                    text = stringResource(R.string.confirm_delete),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { dismissDialog() }
                            .padding(4.dp)
                    )


                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.delete),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { actionPositiveButtonClick() }
                            .padding(4.dp)
                    )
                }
            }
        }

    }
}