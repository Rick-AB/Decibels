package com.rickinc.decibels.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.rickinc.decibels.presentation.ui.theme.DecibelsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DecibelsTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainActivityLayout()
                }
            }
        }
    }
}

