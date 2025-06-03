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
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleScreen
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.auth.presentation.LoginScreen
import com.example.cartrack.feature.auth.presentation.RegisterScreen
import com.example.cartrack.feature.home.presentation.notifications.presentation.NotificationsScreen
import com.example.cartrack.main.presentation.MainScreen
import kotlinx.coroutines.delay


object Routes {
    const val SPLASH_LOADING = "splash_loading" // Rută nouă pentru ecranul de încărcare
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val ADD_VEHICLE = "add_vehicle"
    const val NOTIFICATIONS = "notifications"
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
    // Nu mai este nevoie de hasVehicles aici dacă login-ul gestionează corect onLoginSuccess1/2

    LaunchedEffect(isSessionCheckComplete, isLoggedIn) {
        Log.d("AppNavHost", "LaunchedEffect triggered. isSessionCheckComplete: $isSessionCheckComplete, isLoggedIn: $isLoggedIn")
        if (isSessionCheckComplete) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            Log.d("AppNavHost", "Session check complete. Current route: $currentRoute")

            if (isLoggedIn) {
                Log.d("AppNavHost", "User IS logged in.")
                // După un silent refresh reușit, isLoggedIn este true.
                // Acum trebuie să decidem dacă mergem la MAIN sau ADD_VEHICLE.
                // Această logică este similară cu onLoginSuccess1/2 din LoginScreen.
                // Presupunem că AuthViewModel.hasVehicles se actualizează corect.

                // Verifică dacă nu suntem deja pe o rută post-login pentru a evita navigări multiple
                if (currentRoute == Routes.SPLASH_LOADING || currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER) {
                    val clientId = authViewModel.jwtDecoder.getClientIdFromToken()
                    Log.d("AppNavHost", "ClientID for hasVehicles check (after silent refresh): $clientId")
                    if (clientId != null) {
                        // Apelăm suspend function, deci trebuie un scope de corutină,
                        // LaunchedEffect este deja un scope de corutină.
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
                        // Acest caz nu ar trebui să se întâmple dacă isLoggedIn e true după un refresh reușit
                        Log.e("AppNavHost", "ClientID is NULL after successful silent refresh. Navigating to LOGIN (fallback).")
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
        startDestination = Routes.SPLASH_LOADING, // Începe cu ecranul de încărcare
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
                onLoginSuccess1 = { // Are vehicule
                    Log.i("AppNavHost", "LoginScreen -> onLoginSuccess1 (has vehicles). Navigating to MAIN.")
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginSuccess2 = { // Nu are vehicule
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
            // Protecție suplimentară pentru a evita afișarea MAIN dacă nu e logat
            if (!isLoggedIn && isSessionCheckComplete) {
                LaunchedEffect(Unit) { // Navighează o singură dată
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
                // Afișează un loader dacă verificarea sesiunii nu s-a terminat
                // Acest caz ar trebui acoperit de SPLASH_LOADING, dar ca măsură de siguranță
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
                        // Simplificat la popBackStack, navigarea la MAIN se face din onVehicleAddedSuccessfully
                        // sau dacă utilizatorul anulează și vrea să se întoarcă la ecranul anterior (care ar putea fi MAIN).
                        navController.popBackStack()
                    },
                    onVehicleAddedSuccessfully = {
                        Log.i("AppNavHost", "AddVehicleScreen -> onVehicleAddedSuccessfully. Navigating to MAIN.")
                        Toast.makeText(context, "Vehicle Added!", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.ADD_VEHICLE) { inclusive = true }
                            launchSingleTop = true // Asigură o singură instanță de MAIN
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
    }
}


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