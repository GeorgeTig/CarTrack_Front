package com.example.cartrack.features.home.helpers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(hasNewNotifications: Boolean, onNotificationsClick: () -> Unit) {
    TopAppBar(
        title = { Text("Your Garage", style = MaterialTheme.typography.titleLarge) },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(badge = { if (hasNewNotifications) Badge() }) {
                    Icon(Icons.Filled.Notifications, "Notifications")
                }
            }
        }
    )
}