package com.example.cartrack.feature.carhistory.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarHistoryScreen(
    navController: NavHostController,
    vehicleId: Int? // Primește ID-ul vehiculului
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle History for ID: $vehicleId") }, // Afișează ID-ul
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Car History Details for Vehicle ID: $vehicleId will be shown here.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}