package com.example.cartrack.feature.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// Auth imports
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.auth.presentation.LoginScreen
import com.example.cartrack.feature.auth.presentation.RegisterScreen
// Vehicle Selection import
import com.example.cartrack.feature.vehicle.presentation.VehicleSelectionScreen
// Add Vehicle import
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleScreen

// Define route constants for consistency
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VEHICLE_SELECTION = "vehicle_selection"
    const val ADD_VEHICLE = "add_vehicle"
    // Add other routes later (e.g., vehicle details)
    // const val VEHICLE_DETAILS = "vehicle_details/{vehicleId}"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    // AuthViewModel scoped to the NavGraph to manage auth state across screens
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedInState by authViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current // For placeholder Toasts

    // Determine start destination based on login state
    val startDestination = remember(isLoggedInState) {
        when (isLoggedInState) {
            true -> Routes.VEHICLE_SELECTION // Start at vehicle selection if logged in
            false -> Routes.LOGIN            // Start at login if not logged in
            null -> null                     // Waiting for state determination
        }
    }

    // Only build the NavHost once the start destination is known
    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            // --- Authentication Screens ---
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel, // Re-use the NavGraph-scoped ViewModel
                    onLoginSuccess = {
                        navController.navigate(Routes.VEHICLE_SELECTION) {
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
                    viewModel = authViewModel, // Re-use the NavGraph-scoped ViewModel
                    onRegisterSuccess = {
                        // Go back to login after registration success
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    navigateBackToLogin = {
                        navController.popBackStack() // Simple back navigation
                    }
                )
            }

            // --- Vehicle Screens ---
            composable(Routes.VEHICLE_SELECTION) {
                // This composable receives the navigation actions as lambdas
                VehicleSelectionScreen(
                    // ViewModel for this screen will be provided by Hilt
                    onVehicleSelected = { vehicleId ->
                        // TODO: Implement navigation to Vehicle Details screen
                        Toast.makeText(context, "Navigate to details for vehicle ID: $vehicleId (TBD)", Toast.LENGTH_SHORT).show()
                        // navController.navigate(Routes.VEHICLE_DETAILS.replace("{vehicleId}", vehicleId.toString()))
                    },
                    // >>> Implementation of the action for the '+' button <<<
                    onAddVehicleClicked = {
                        // When called, navigate to the Add Vehicle route
                        navController.navigate(Routes.ADD_VEHICLE)
                    },
                    // >>> End Implementation <<<
                    onLogout = { // Logout action
                        authViewModel.logout()
                        // Navigate back to login, clear the back stack up to this point
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.VEHICLE_SELECTION) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.ADD_VEHICLE) {
                // Add Vehicle screen composable
                AddVehicleScreen(
                    // ViewModel for this screen provided by Hilt
                    onNavigateBack = { navController.popBackStack() }, // Simple back action
                    // TODO: Define what happens after successful decode/selection/save
                    onNavigateToSave = { /* uniqueVehicleData -> ... */
                        Toast.makeText(context, "Save Action TBD", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // Example: Go back after "save" placeholder
                    },
                    onNavigateToSelection = { /* ambiguousResults -> ... */
                        Toast.makeText(context, "Selection UI Navigation TBD", Toast.LENGTH_SHORT).show()
                        // Maybe stay here, or navigate to another screen with results
                    }
                )
            }

            // TODO: Add composable entry for Vehicle Details screen later
            // composable(Routes.VEHICLE_DETAILS) { backStackEntry ->
            //    val vehicleId = backStackEntry.arguments?.getString("vehicleId")?.toIntOrNull()
            //    if (vehicleId != null) {
            //        VehicleDetailsScreen(vehicleId = vehicleId, onNavigateBack = { navController.popBackStack() })
            //    } else {
            //        // Handle error: Invalid ID
            //    }
            //}

        }
    } else {
        // Show loading indicator while waiting for initial login state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}