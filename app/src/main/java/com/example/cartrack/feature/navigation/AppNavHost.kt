package com.example.cartrack.feature.navigation

import android.util.Log // Import Log for error logging
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
import androidx.navigation.* // Import NavType, navArgument, etc.
import androidx.navigation.compose.*
// Auth imports
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.auth.presentation.LoginScreen
import com.example.cartrack.feature.auth.presentation.RegisterScreen
// Vehicle Selection import
import com.example.cartrack.feature.vehicle.presentation.VehicleSelectionScreen
// Add Vehicle imports

import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto // Import model for argument type
import com.example.cartrack.feature.addvehicle.presentation.AddVehicle.AddVehicleScreen
import com.example.cartrack.feature.addvehicle.presentation.ConfirmVehicleScreen
// Kotlinx Serialization imports
import kotlinx.serialization.encodeToString // For encoding list to string
import kotlinx.serialization.json.Json // For JSON serialization
// URL Encoding/Decoding
import java.net.URLDecoder // Kept for potential future use but not strictly needed here anymore
import java.net.URLEncoder // Needed for building the route
// Coroutines Delay
import kotlinx.coroutines.delay

// Define route constants for consistency and easier refactoring
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VEHICLE_SELECTION = "vehicle_selection"
    const val ADD_VEHICLE = "add_vehicle"

    // Define argument name (public)
    const val ARG_RESULTS_JSON = "resultsJson"

    // Route definition for confirmation screen with argument placeholder
    const val CONFIRM_VEHICLE = "confirm_vehicle/{$ARG_RESULTS_JSON}"

    // Helper function to build the route safely, encoding the JSON argument
    fun confirmVehicleRoute(results: List<VinDecodedResponseDto>): String {
        // Ensure the List itself is serializable
        val jsonString = Json.encodeToString(results)
        // URL-encode the JSON string for safe passage in the route path
        val encodedJson = URLEncoder.encode(jsonString, "UTF-8")
        return "confirm_vehicle/$encodedJson" // Construct the full route
    }
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    // AuthViewModel scoped to the NavGraph to manage auth state across screens
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // Observe login state to determine initial screen and handle auth changes
    val isLoggedInState by authViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current // Get context for potential Toasts

    // Determine start destination based on login state (only once initially)
    val startDestination = remember(isLoggedInState) {
        when (isLoggedInState) {
            true -> Routes.VEHICLE_SELECTION // Start at vehicle selection if logged in
            false -> Routes.LOGIN            // Start at login if not logged in
            null -> null                     // Waiting for state determination (show loading)
        }
    }

    // Only build the NavHost once the start destination is known
    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = startDestination, // Set the calculated start destination
            modifier = modifier
        ) {
            // --- Authentication Screens ---
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Routes.VEHICLE_SELECTION) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
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
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    navigateBackToLogin = { navController.popBackStack() }
                )
            }

            // --- Vehicle Listing/Selection Screen ---
            composable(Routes.VEHICLE_SELECTION) {
                VehicleSelectionScreen(
                    onVehicleSelected = { vehicleId ->
                        Toast.makeText(context, "Navigate to details for vehicle ID: $vehicleId (TBD)", Toast.LENGTH_SHORT).show()
                    },
                    onAddVehicleClicked = {
                        navController.navigate(Routes.ADD_VEHICLE) // Navigate to VIN entry screen
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.VEHICLE_SELECTION) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // --- Add Vehicle (VIN Entry) Screen ---
            composable(Routes.ADD_VEHICLE) {
                AddVehicleScreen(
                    onNavigateBack = { navController.popBackStack() },
                    // When VIN decoding is successful (result list is not empty)
                    onVinDecoded = { results ->
                        // Navigate to the confirmation/selection screen, passing the results
                        // Use the helper function to correctly build the route with encoded JSON
                        navController.navigate(Routes.confirmVehicleRoute(results))
                    }
                )
            }

            // --- Confirm/Select Vehicle Screen ---
            composable(
                route = Routes.CONFIRM_VEHICLE, // Use route constant with placeholder
                // Define the navigation argument expected by this route
                arguments = listOf(navArgument(Routes.ARG_RESULTS_JSON) {
                    type = NavType.StringType // The argument is passed as a string
                    nullable = false // Argument is required
                })
            ) { backStackEntry -> // ViewModel inside ConfirmVehicleScreen will handle the argument
                // We just need to ensure the composable is called.
                // Check if argument exists just as a safeguard against manual/incorrect navigation.
                val argumentExists = backStackEntry.arguments?.containsKey(Routes.ARG_RESULTS_JSON) ?: false

                if (argumentExists) {
                    // Call the ConfirmVehicleScreen composable.
                    // It will internally use hiltViewModel() to get its own ViewModel instance,
                    // which will then retrieve and process the argument from SavedStateHandle.
                    ConfirmVehicleScreen(
                        onNavigateBack = { navController.popBackStack() }, // Standard back navigation
                        // This lambda defines what happens *after* the user confirms and the
                        // ViewModel successfully saves the vehicle.
                        onVehicleAddedSuccessfully = {
                            Log.d("AppNavHost", "Vehicle Added Successfully callback invoked, navigating back to list.")
                            Toast.makeText(context, "Vehicle Added!", Toast.LENGTH_SHORT).show()
                            // Navigate back to vehicle list, clearing add/confirm screens from backstack
                            navController.navigate(Routes.VEHICLE_SELECTION) {
                                // Pop up to the list screen, removing everything added after it
                                popUpTo(Routes.VEHICLE_SELECTION) {
                                    inclusive = true // Remove the list screen itself to refresh it (optional)
                                    // If you want to keep the old list screen state, set inclusive = false
                                }
                                // Consider launchSingleTop = true if VECHICLE_SELECTION can be complex
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    // Should not happen if navigation is done via Routes.confirmVehicleRoute,
                    // but handle the case where the argument is missing.
                    Log.e("AppNavHost", "ConfirmVehicleScreen route called without required resultsJson argument.")
                    ErrorDisplayAndNavigateBack(navController, "Error: Missing required vehicle details.")
                }
            } // End composable(Routes.CONFIRM_VEHICLE)

        } // End NavHost
    } else { // Show loading indicator while waiting for initial login state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Helper composable to display an error message temporarily and then navigate back.
 * Used for handling critical navigation argument errors.
 */
@Composable
private fun ErrorDisplayAndNavigateBack(navController: NavController, message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
    }
    // Effect to navigate back after a delay
    LaunchedEffect(Unit) {
        delay(3500) // Wait 3.5 seconds
        navController.popBackStack() // Go back one step in navigation
    }
}