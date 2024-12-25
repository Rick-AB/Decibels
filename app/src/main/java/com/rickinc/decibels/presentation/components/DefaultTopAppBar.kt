package com.rickinc.decibels.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rickinc.decibels.presentation.theme.Typography

@Composable
fun DefaultTopAppBar(
    title: String?,
    modifier: Modifier = Modifier,
    mainIcon: @Composable (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    Surface(
        tonalElevation = AppBarDefaults.TopAppBarElevation,
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(50.dp)
            ) {
                if (navigationIcon != null) navigationIcon()

                if (mainIcon != null) {
                    mainIcon()
                } else {
                    Spacer(modifier = Modifier.width(45.dp))
                }

                Row(
                    Modifier
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    content = { title?.let { TopBarTitleView(it) } }
                )

                if (actions != null)
                    Row(
                        Modifier,
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions()
                    }
            }
        }
    }
}

@Composable
fun TopBarTitleView(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 5.dp),
        style = Typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}