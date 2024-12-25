package com.rickinc.decibels.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.SheetValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape


fun Modifier.modifyIf(condition: Boolean, modify: Modifier.() -> Modifier): Modifier {
    return if (condition) modify() else this
}

fun Modifier.clickable(shape: Shape, enabled: Boolean = true, onClick: () -> Unit) =
    clip(shape).clickable(enabled = enabled, onClick = onClick)

val BottomSheetScaffoldState.currentFraction: Float
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