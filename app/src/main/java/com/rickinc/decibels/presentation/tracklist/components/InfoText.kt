package com.rickinc.decibels.presentation.tracklist.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rickinc.decibels.presentation.ui.theme.Typography

@Composable
fun InfoText(@StringRes stringResource: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(id = stringResource),
            style = Typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}