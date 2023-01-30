package com.rickinc.decibels.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.rickinc.decibels.di.AppModule
import dagger.hilt.EntryPoints

private lateinit var sharedPreferencesEntryPoint: AppModule.SharedPreferencesEntryPoint

@Composable
fun requireSharedPreferencesEntryPoint(): AppModule.SharedPreferencesEntryPoint {
    if (!::sharedPreferencesEntryPoint.isInitialized) {
        sharedPreferencesEntryPoint = EntryPoints.get(
            LocalContext.current.applicationContext,
            AppModule.SharedPreferencesEntryPoint::class.java
        )
    }
    return sharedPreferencesEntryPoint
}
