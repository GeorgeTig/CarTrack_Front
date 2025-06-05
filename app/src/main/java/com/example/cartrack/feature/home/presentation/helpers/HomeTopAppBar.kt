package com.example.cartrack.feature.home.presentation.helpers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    hasNewNotifications: Boolean,
    onNotificationsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Your Garage",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold // Poți folosi și fontWeight direct în stil dacă e definit
            )
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (hasNewNotifications) {
                            // Pentru a schimba culoarea Badge-ului în Material 3,
                            // folosește parametrul 'containerColor' DACA Badge are un parametru 'colors'
                            // sau se bazează pe MaterialTheme.colorScheme.error.
                            // Simplu Badge() va folosi culoarea de accent/eroare a temei.
                            Badge(
                                // containerColor = MaterialTheme.colorScheme.error // Opțional, dacă vrei să forțezi
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant // Culoarea iconiței
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background, // Sau surface
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface // Dacă ai avea un navigationIcon
        )
    )
}