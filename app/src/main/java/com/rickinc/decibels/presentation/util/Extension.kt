package com.rickinc.decibels.presentation.util

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape

fun Modifier.modifyIf(condition: Boolean, modify: Modifier.() -> Modifier): Modifier {
    return if (condition) modify() else this
}

fun Modifier.clickable(shape: Shape, enabled: Boolean = true, onClick: () -> Unit) =
    clip(shape).clickable(enabled = enabled, onClick = onClick)