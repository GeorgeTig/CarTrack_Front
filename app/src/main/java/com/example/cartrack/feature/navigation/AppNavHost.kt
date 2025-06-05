package com.example.cartrack.feature.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator // Import pentru Splash
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Asigură-te că folosești acest import
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.cartrack.feature.addmaintenance.presentation.AddMaintenanceScreen
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleScreen
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.auth.presentation.LoginScreen
import com.example.cartrack.feature.auth.presentation.RegisterScreen
import com.example.cartrack.feature.carhistory.presentation.CarHistoryScreen
import com.example.cartrack.feature.home.presentation.notifications.presentation.NotificationsScreen
import com.example.cartrack.main.presentation.MainScreen
import kotlinx.coroutines.delay


object Routes {
    const val SPLASH_LOADING = "splash_loading"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val ADD_VEHICLE = "add_vehicle"
    const val NOTIFICATIONS = "notifications"
    const val ADD_MAINTENANCE = "add_maintenance"
    const val CAR_HISTORY = "car_history/{vehicleId}"

    fun carHistory(vehicleId: Int) = "car_history/$vehicleId"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isSessionCheckComplete by authViewModel.isSessionCheckComplete.collectAsStateWithLifecycle()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(isSessionCheckComplete, isLoggedIn) {
        Log.d("AppNavHost", "LaunchedEffect triggered. isSessionCheckComplete: $isSessionCheckComplete, isLoggedIn: $isLoggedIn")
        if (isSessionCheckComplete) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            Log.d("AppNavHost", "Session check complete. Current route: $currentRoute")

            if (isLoggedIn) {
                Log.d("AppNavHost", "User IS logged in.")
                if (currentRoute == Routes.SPLASH_LOADING || currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER) {
                    val clientId = authViewModel.jwtDecoder.getClientIdFromToken()
                    Log.d("AppNavHost", "ClientID for hasVehicles check (after silent refresh): $clientId")
                    if (clientId != null) {
                        authViewModel.authRepository.hasVehicles(clientId)
                            .onSuccess {
                                Log.i("AppNavHost", "Silent login: hasVehicles check SUCCESSFUL. Navigating to MAIN.")
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            .onFailure { exception ->
                                Log.w("AppNavHost", "Silent login: hasVehicles check FAILED (${exception.message}). Navigating to ADD_VEHICLE.")
                                navController.navigate(Routes.ADD_VEHICLE) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                    } else {
                        Log.e("AppNavHost", "ClientID is NULL after successful login/refresh. Navigating to LOGIN (fallback).")
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    Log.d("AppNavHost", "Already on a main app route ($currentRoute) after silent login. No navigation needed from LaunchedEffect.")
                }
            } else { // isLoggedIn este false după verificarea sesiunii
                Log.d("AppNavHost", "User IS NOT logged in after session check.")
                if (currentRoute == Routes.SPLASH_LOADING || currentRoute != Routes.LOGIN && currentRoute != Routes.REGISTER) {
                    Log.d("AppNavHost", "Not on LOGIN/REGISTER (or was on SPLASH). Navigating to LOGIN.")
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    Log.d("AppNavHost", "Already on LOGIN/REGISTER ($currentRoute). No navigation needed.")
                }
            }
        } else {
            Log.d("AppNavHost", "Session check NOT YET complete. Waiting...")
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_LOADING,
        modifier = modifier
    ) {


        composable(Routes.SPLASH_LOADING) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Log.d("AppNavHost", "Displaying SPLASH_LOADING screen.")
            }
        }

        composable(Routes.LOGIN) {
            Log.d("AppNavHost", "Displaying LOGIN screen.")
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess1 = {
                    Log.i("AppNavHost", "LoginScreen -> onLoginSuccess1 (has vehicles). Navigating to MAIN.")
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginSuccess2 = {
                    Log.i("AppNavHost", "LoginScreen -> onLoginSuccess2 (no vehicles). Navigating to ADD_VEHICLE.")
                    navController.navigate(Routes.ADD_VEHICLE) {
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
            Log.d("AppNavHost", "Displaying REGISTER screen.")
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    Toast.makeText(context, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                navigateBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.MAIN) {
            Log.d("AppNavHost", "Attempting to display MAIN screen. isLoggedIn: $isLoggedIn, isSessionCheckComplete: $isSessionCheckComplete")
            if (!isLoggedIn && isSessionCheckComplete) {
                LaunchedEffect(Unit) {
                    Log.w("AppNavHost", "In MAIN composable but not logged in. Redirecting to LOGIN.")
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else if (isLoggedIn) {
                Log.d("AppNavHost", "Displaying MAIN screen.")
                MainScreen(
                    mainNavController = navController,
                    authViewModel = authViewModel
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Log.d("AppNavHost", "In MAIN composable, session check not complete, showing loader.")
                }
            }
        }

        composable(Routes.ADD_VEHICLE) {
            Log.d("AppNavHost", "Attempting to display ADD_VEHICLE screen. isLoggedIn: $isLoggedIn, isSessionCheckComplete: $isSessionCheckComplete")
            if (!isLoggedIn && isSessionCheckComplete) {
                LaunchedEffect(Unit) {
                    Log.w("AppNavHost", "In ADD_VEHICLE composable but not logged in. Redirecting to LOGIN.")
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADD_VEHICLE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else if (isLoggedIn) {
                Log.d("AppNavHost", "Displaying ADD_VEHICLE screen.")
                AddVehicleScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onVehicleAddedSuccessfully = {
                        Log.i("AppNavHost", "AddVehicleScreen -> onVehicleAddedSuccessfully. Navigating to MAIN.")
                        Toast.makeText(context, "Vehicle Added!", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.ADD_VEHICLE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Log.d("AppNavHost", "In ADD_VEHICLE composable, session check not complete, showing loader.")
                }
            }
        }

        composable(Routes.NOTIFICATIONS) {
            Log.d("AppNavHost", "Attempting to display NOTIFICATIONS screen. isLoggedIn: $isLoggedIn, isSessionCheckComplete: $isSessionCheckComplete")
            if (!isLoggedIn && isSessionCheckComplete) {
                LaunchedEffect(Unit) {
                    Log.w("AppNavHost", "In NOTIFICATIONS composable but not logged in. Redirecting to LOGIN.")
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.NOTIFICATIONS) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else if (isLoggedIn) {
                Log.d("AppNavHost", "Displaying NOTIFICATIONS screen.")
                NotificationsScreen(navController = navController)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Log.d("AppNavHost", "In NOTIFICATIONS composable, session check not complete, showing loader.")
                }
            }
        }

        composable(
            route = Routes.CAR_HISTORY, // "car_history/{vehicleId}"
            arguments = listOf(navArgument("vehicleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getInt("vehicleId")
            Log.d("AppNavHost", "Displaying CAR_HISTORY screen for vehicleId: $vehicleId")
            // Poți adăuga protecție de login aici dacă e necesar
            if (isLoggedIn) { // Presupunând că isLoggedIn e disponibil din AuthViewModel
                CarHistoryScreen(navController = navController, vehicleId = vehicleId)
            } else {
                // Redirecționează la login dacă nu e logat
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.CAR_HISTORY) { inclusive = true } // Sau la graficul principal
                        launchSingleTop = true
                    }
                }
            }
        }


        // NOUA RUTĂ PENTRU ADD_MAINTENANCE
        composable(Routes.ADD_MAINTENANCE) {
            Log.d("AppNavHost", "Attempting to display ADD_MAINTENANCE screen. isLoggedIn: $isLoggedIn, isSessionCheckComplete: $isSessionCheckComplete")
            if (!isLoggedIn && isSessionCheckComplete) {
                LaunchedEffect(Unit) {
                    Log.w("AppNavHost", "In ADD_MAINTENANCE composable but not logged in. Redirecting to LOGIN.")
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADD_MAINTENANCE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else if (isLoggedIn) {
                Log.d("AppNavHost", "Displaying ADD_MAINTENANCE screen.")
                AddMaintenanceScreen(navController = navController) // Pasează navController-ul global
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Log.d("AppNavHost", "In ADD_MAINTENANCE composable, session check not complete, showing loader.")
                }
            }
        }
    }
}

// ErrorDisplayAndNavigateBack rămâne neschimbat
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
        delay(3500)
        navController.popBackStack()
    }
}