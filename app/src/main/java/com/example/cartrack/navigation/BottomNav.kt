package com.example.cartrack.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cartrack.features.auth.AuthViewModel
import com.example.cartrack.features.home.HomeScreen
import com.example.cartrack.features.maintenance.MaintenanceScreen
import com.example.cartrack.features.profile.ProfileScreen

sealed class BottomNavScreen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : BottomNavScreen("bottom_home", "Home", Icons.Default.Home)
    data object Maintenance : BottomNavScreen("bottom_maintenance", "Maintenance", Icons.Default.Build)
    data object Add : BottomNavScreen("bottom_add", "Add", Icons.Default.AddCircle)
    data object Profile : BottomNavScreen("bottom_profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    BottomNavScreen.Home,
    BottomNavScreen.Maintenance,
    BottomNavScreen.Add,
    BottomNavScreen.Profile
)

@Composable
fun BottomNavGraph(
    bottomNavController: NavHostController,
    appNavController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(navController = bottomNavController, startDestination = BottomNavScreen.Home.route) {
        composable(BottomNavScreen.Home.route) {
            HomeScreen(
                appNavController = appNavController,
                authViewModel = authViewModel
            )
        }
        composable(BottomNavScreen.Maintenance.route) {
            MaintenanceScreen(appNavController = appNavController)
        }
        composable(BottomNavScreen.Profile.route) {
            ProfileScreen(appNavController = appNavController)
        }
    }
}