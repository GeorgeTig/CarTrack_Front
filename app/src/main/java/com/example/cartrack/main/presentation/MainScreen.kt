package com.example.cartrack.main.presentation

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.home.presentation.HomeScreen
import com.example.cartrack.feature.maintenance.presentation.MaintenanceScreen // Import correct path
import com.example.cartrack.feature.profile.presentation.ProfileScreen
import com.example.cartrack.feature.navigation.BottomNavScreen
import com.example.cartrack.feature.navigation.Routes
import com.example.cartrack.feature.navigation.bottomNavItems

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Can remove if using padding
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    // NavController for the overall application graph (Login -> Main, Main -> AddVehicle etc.)
    mainNavController: NavHostController,
    // AuthViewModel might be needed for logout action triggered from Profile tab
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // NavController specific to the destinations managed by the bottom bar
    val bottomNavController = rememberNavController()
    val context = LocalContext.current // For Toasts

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    // Check if the current route hierarchy includes the screen's route
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            if (screen.route == BottomNavScreen.Add.route) {
                                // Use the mainNavController to navigate to the separate AddVehicle feature
                                mainNavController.navigate(Routes.ADD_VEHICLE)
                            } else {
                                // Navigate within the bottomNavController's graph for other tabs
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true // Save state of screens in bottom nav
                                    }
                                    launchSingleTop = true // Avoid multiple instances of same screen
                                    restoreState = true // Restore state when navigating back
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // This NavHost displays the actual content for each bottom navigation tab
        NavHost(
            navController = bottomNavController, // Controlled by bottom bar clicks (except Add)
            startDestination = BottomNavScreen.Home.route, // Default tab
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        ) {
            composable(BottomNavScreen.Home.route) {
                HomeScreen(

                )
            }
            composable(BottomNavScreen.Maintenance.route) {
                MaintenanceScreen(/* pass dependencies if needed */)
            }
            composable(BottomNavScreen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        authViewModel.logout()
                        // Use mainNavController to go back to Login, clearing the Main screen stack
                        mainNavController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            // No composable entry for BottomNavScreen.Add here as it navigates elsewhere.
        }
    }
}