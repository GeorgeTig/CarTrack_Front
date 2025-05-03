package com.example.cartrack.feature.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications // Added
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// Renamed for clarity, represents the destinations within the Main screen's bottom bar
sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavScreen("bottom_home", "Home", Icons.Default.Home)
    object Maintenance : BottomNavScreen("bottom_maintenance", "Maintenance", Icons.Default.Build)

    // ADD is conceptually different - it triggers navigation OUTSIDE the bottom nav host
    object Add : BottomNavScreen("bottom_add", "Add", Icons.Default.AddCircle)

    // You mentioned 5 buttons, adding Notifications as an example
   object Profile : BottomNavScreen("bottom_profile", "Profile", Icons.Default.Person)
}

// List of items to iterate over for the bottom bar (excluding Add conceptually, handled separately)
val bottomNavItems = listOf(
    BottomNavScreen.Home,
    BottomNavScreen.Maintenance,
    BottomNavScreen.Add, // Keep it here for visual order, but handle click differently
    BottomNavScreen.Profile
)