package com.rickinc.decibels.presentation.components

import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rickinc.decibels.R

@Composable
fun LottieLoading(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.music_loading))
    LottieAnimation(
        composition = composition,
        modifier = modifier.requiredHeight(100.dp),
        iterations = Int.MAX_VALUE
    )
}