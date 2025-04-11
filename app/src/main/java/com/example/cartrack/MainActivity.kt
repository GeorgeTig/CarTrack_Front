package com.example.cartrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cartrack.feature.navigation.AppNavHost // Import NavHost
import com.example.cartrack.ui.theme.CarTrackTheme
import dagger.hilt.android.AndroidEntryPoint // Import

@AndroidEntryPoint // <<< MUST be here
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set the NavHost as the main content
                    AppNavHost()
                }
            }
        }
    }
}
