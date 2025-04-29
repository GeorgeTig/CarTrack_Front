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
import androidx.navigation.compose.*
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.auth.presentation.LoginScreen
import com.example.cartrack.feature.auth.presentation.RegisterScreen
import com.example.cartrack.feature.vehicle.presentation.VehicleSelectionScreen
import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto
import com.example.cartrack.feature.addvehicle.presentation.AddVehicle.AddVehicleScreen
import com.example.cartrack.feature.addvehicle.presentation.ConfirmVehicle.ConfirmVehicleScreen
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import kotlinx.coroutines.delay

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VEHICLE_SELECTION = "vehicle_selection"
    const val ADD_VEHICLE = "add_vehicle"

    const val ARG_RESULTS_JSON = "resultsJson"
    const val CONFIRM_VEHICLE = "confirm_vehicle/{$ARG_RESULTS_JSON}"

    // Helper function to create the route with encoded JSON (encode the results of VIN decoding)
    fun confirmVehicleRoute(results: List<VinDecodedResponseDto>): String {
        val jsonString = Json.encodeToString(results)
        val encodedJson = URLEncoder.encode(jsonString, "UTF-8")
        return "confirm_vehicle/$encodedJson"
    }
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // Observe login state to determine initial screen and handle auth changes
    val isLoggedInState by authViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current

    // Determine the start page destination (login screen or vehicle selection if logged in)
    val startDestination = remember(isLoggedInState) {
        when (isLoggedInState) {
            true -> Routes.VEHICLE_SELECTION
            false -> Routes.LOGIN
            null -> null
        }
    }

    // Only build the NavHost once the start destination is known
    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {

            // Login screen
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

            // Registration screen
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

            // Vehicle Selection screen
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

            // Add Vehicle screen
            composable(Routes.ADD_VEHICLE) {
                AddVehicleScreen(
                    onNavigateBack = { navController.popBackStack() },
                    // When VIN decoding is successful (result list is not empty)
                    onVinDecoded = { results ->
                        navController.navigate(Routes.confirmVehicleRoute(results))
                    }
                )
            }

            // Confirm Vehicle screen
            composable(
                route = Routes.CONFIRM_VEHICLE,
                arguments = listOf(navArgument(Routes.ARG_RESULTS_JSON) {
                    type = NavType.StringType
                    nullable = false
                })
            ) { backStackEntry ->
                val argumentExists = backStackEntry.arguments?.containsKey(Routes.ARG_RESULTS_JSON) == true

                if (argumentExists) {
                    ConfirmVehicleScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onVehicleAddedSuccessfully = {
                            Log.d("AppNavHost", "Vehicle Added Successfully callback invoked, navigating back to list.")
                            Toast.makeText(context, "Vehicle Added!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Routes.VEHICLE_SELECTION) {
                                popUpTo(Routes.VEHICLE_SELECTION) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    Log.e("AppNavHost", "ConfirmVehicleScreen route called without required resultsJson argument.")
                    ErrorDisplayAndNavigateBack(navController, "Error: Missing required vehicle details.")
                }
            }

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

/**
 * Helper composable to display an error message temporarily and then navigate back.
 * Used for handling critical navigation argument errors.
 */
@Composable
private fun ErrorDisplayAndNavigateBack(navController: NavController, message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
    }

    LaunchedEffect(Unit) {
        delay(3500)
        navController.popBackStack()
    }
}