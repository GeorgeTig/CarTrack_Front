package com.example.cartrack.feature.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleScreen // Keep this
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.auth.presentation.LoginScreen
import com.example.cartrack.feature.auth.presentation.RegisterScreen// Import MainScreen from correct package
import com.example.cartrack.main.presentation.MainScreen
// Removed VehicleSelectionScreen import
// Removed VinDecodedResponseDto imports unless needed elsewhere
import kotlinx.coroutines.delay
// Removed kotlinx.serialization and URLEncoder imports unless CONFIRM_VEHICLE route is actually used

// Define Routes in a shared location or keep here
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main" // Route for the screen holding the bottom nav bar
    const val ADD_VEHICLE = "add_vehicle" // Route for the dedicated Add Vehicle flow
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel() // Keep AuthViewModel
) {
    val isLoggedInState by authViewModel.isLoggedIn.collectAsStateWithLifecycle( initialValue = null)
    val lastVehicleIdState by authViewModel.hasVehicles.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current

        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = modifier
        ) {

            // --- Authentication Routes ---
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess1 = {
                        // Navigate to MAIN screen after login
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true } // Remove Login from backstack
                            launchSingleTop = true
                        }
                    },
                    onLoginSuccess2 = {
                        //Navigate to AddVehicle screen after login
                        navController.navigate(Routes.ADD_VEHICLE) {
                            popUpTo(Routes.LOGIN) { inclusive = true } // Remove Login from backstack
                            launchSingleTop = true
                        }
                    },
                    navigateToRegister = {
                        navController.navigate(Routes.REGISTER) { launchSingleTop = true }
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        // Go back to Login screen after registration
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true } // Clear register screen too
                            launchSingleTop = true
                        }
                    },
                    navigateBackToLogin = { navController.popBackStack() }
                )
            }

            // --- Main Application Route (Houses Bottom Navigation) ---
            composable(Routes.MAIN) {
                MainScreen(
                    mainNavController = navController, // Pass the main NavController
                    authViewModel = authViewModel // Pass ViewModel for logout action
                )
            }

            // --- Add Vehicle Screen (Navigated FROM MainScreen) ---
            composable(Routes.ADD_VEHICLE) { // <-- ONLY ONE DEFINITION NEEDED
                AddVehicleScreen(
                    // viewModel is hiltViewModel() internally
                    onNavigateBack = { navController.popBackStack() }, // Simple back navigation
                    onVehicleAddedSuccessfully = {
                        // This callback is invoked when the ViewModel signals success
                        Log.d("AppNavHost", "Vehicle Added Successfully callback invoked, navigating back.")
                        Toast.makeText(context, "Vehicle Added!", Toast.LENGTH_SHORT).show()

                        // Pop AddVehicleScreen off the stack. This returns the user to
                        // the screen they were on before navigating to AddVehicleScreen,
                        // which should be MainScreen in this flow.
                        navController.popBackStack()
                    }
                )
            }

            // --- REMOVED Old VehicleSelectionScreen Composable ---
            // composable(Routes.VEHICLE_SELECTION) { ... }

            // --- Optional: Vehicle Detail Route ---
            // composable(route = Routes.VEHICLE_DETAIL, ...) { ... }

        }
    }



/**
 * Helper composable to display an error message temporarily and then navigate back.
 * Used for handling critical navigation argument errors.
 */
@Composable
private fun ErrorDisplayAndNavigateBack(navController: NavController, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }

    LaunchedEffect(Unit) {
        delay(3500) // Show message for 3.5 seconds
        navController.popBackStack() // Navigate back
    }
}