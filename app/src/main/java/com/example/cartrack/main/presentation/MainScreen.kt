package com.example.cartrack.main.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.home.presentation.HomeScreen
import com.example.cartrack.feature.maintenance.presentation.MaintenanceScreen
import com.example.cartrack.feature.profile.presentation.ProfileScreen
import com.example.cartrack.feature.navigation.BottomNavScreen
import com.example.cartrack.feature.navigation.Routes
import com.example.cartrack.feature.navigation.bottomNavItems

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainNavController: NavHostController, // This is the NavController from AppNavHost
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            if (screen.route == BottomNavScreen.Add.route) {
                                mainNavController.navigate(Routes.ADD_VEHICLE)
                            } else {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavScreen.Home.route) {
                HomeScreen(appNavController = mainNavController) // Pass mainNavController here
            }
            composable(BottomNavScreen.Maintenance.route) {
                MaintenanceScreen()
            }
            composable(BottomNavScreen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        authViewModel.logout()
                        mainNavController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}