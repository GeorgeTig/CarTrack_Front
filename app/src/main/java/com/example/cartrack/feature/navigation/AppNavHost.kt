package com.example.cartrack.feature.navigation

import android.widget.Toast // Import Toast for placeholders
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.auth.presentation.LoginScreen
import com.example.cartrack.feature.auth.presentation.RegisterScreen
import com.example.cartrack.feature.vehicle.presentation.VehicleSelectionScreen // Import new screen

// Define route constants
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VEHICLE_SELECTION = "vehicle_selection" // New route
    // Add routes for Vehicle Details, Add Vehicle etc. later
    // const val VEHICLE_DETAILS = "vehicle_details/{vehicleId}"
    // const val ADD_VEHICLE = "add_vehicle"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedInState by authViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current // Get context for Toasts

    val startDestination = remember(isLoggedInState) {
        when (isLoggedInState) {
            true -> Routes.VEHICLE_SELECTION // <<< Go to Vehicle Selection if logged in
            false -> Routes.LOGIN
            null -> null
        }
    }

    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        // Navigate to vehicle selection, clear login from back stack
                        navController.navigate(Routes.VEHICLE_SELECTION) { // <<< Navigate to Vehicle Selection
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    navigateToRegister = {
                        navController.navigate(Routes.REGISTER) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    navigateBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // *** New Composable for Vehicle Selection Screen ***
            composable(Routes.VEHICLE_SELECTION) {
                VehicleSelectionScreen(
                    // ViewModel will be provided by Hilt automatically
                    onVehicleSelected = { vehicleId ->
                        // TODO: Implement navigation to Vehicle Details screen
                        println("Vehicle selected: $vehicleId")
                        Toast.makeText(context, "Selected vehicle ID: $vehicleId", Toast.LENGTH_SHORT).show()
                        // navController.navigate("vehicle_details/$vehicleId")
                    },
                    onAddVehicleClicked = {
                        // TODO: Implement navigation to Add Vehicle screen
                        println("Add vehicle clicked")
                        Toast.makeText(context, "Add vehicle clicked (Not implemented)", Toast.LENGTH_SHORT).show()
                        // navController.navigate(Routes.ADD_VEHICLE)
                    },
                    onLogout = { // Pass logout lambda
                        authViewModel.logout()
                        // Navigate back to login, clear the vehicle selection stack
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.VEHICLE_SELECTION) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // --- Old MainAppPlaceholderScreen (REMOVE or keep for reference) ---
            // composable(Routes.MAIN_APP) { ... }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
