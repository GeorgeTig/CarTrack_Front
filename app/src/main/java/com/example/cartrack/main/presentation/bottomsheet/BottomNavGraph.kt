package com.example.cartrack.main.presentation.bottomsheet

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.home.presentation.HomeScreen
import com.example.cartrack.feature.maintenance.presentation.MaintenanceScreen
import com.example.cartrack.feature.navigation.BottomNavScreen
import com.example.cartrack.feature.navigation.Routes
import com.example.cartrack.feature.profile.presentation.ProfileScreen

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    appGlobalNavController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(navController = navController, startDestination = BottomNavScreen.Home.route) {
        composable(BottomNavScreen.Home.route) {
            HomeScreen(authViewModel = authViewModel, appNavController = appGlobalNavController)
        }
        composable(BottomNavScreen.Maintenance.route) {
            // Asigură-te că pasezi navController-ul corect dacă MaintenanceScreen are nevoie de navigare globală
            MaintenanceScreen(viewModel = hiltViewModel(/* eventual viewModelStoreOwner dacă e necesar pentru scope specific */))
        }
        composable(BottomNavScreen.Profile.route) {
            ProfileScreen(onLogout = {
                authViewModel.logout()
                appGlobalNavController.navigate(Routes.LOGIN) {
                    popUpTo(appGlobalNavController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            })
        }
    }
}