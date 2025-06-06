package com.example.cartrack.feature.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.cartrack.feature.addmaintenance.presentation.AddMaintenanceScreen
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleScreen
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.auth.presentation.LoginScreen
import com.example.cartrack.feature.auth.presentation.RegisterScreen
import com.example.cartrack.feature.carhistory.presentation.CarHistoryScreen
import com.example.cartrack.feature.editreminder.presentation.EditReminderScreen
import com.example.cartrack.feature.home.presentation.notifications.presentation.NotificationsScreen
import com.example.cartrack.feature.reminders.presentation.ReminderDetailScreen
import com.example.cartrack.main.presentation.MainScreen


object Routes {
    const val SPLASH_LOADING = "splash_loading"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val ADD_VEHICLE_FLOW_BASE_ROUTE = "add_vehicle_flow"
    const val ADD_VEHICLE_ARG_FROM_LOGIN = "fromLoginNoVehicles"
    const val ADD_VEHICLE_ROUTE_WITH_ARG_DEF =
        "$ADD_VEHICLE_FLOW_BASE_ROUTE?$ADD_VEHICLE_ARG_FROM_LOGIN={${ADD_VEHICLE_ARG_FROM_LOGIN}}"
    const val NOTIFICATIONS = "notifications"
    const val ADD_MAINTENANCE = "add_maintenance"
    const val CAR_HISTORY_BASE_ROUTE = "car_history"
    const val CAR_HISTORY_ARG_VEHICLE_ID = "vehicleId"
    const val REMINDER_DETAIL_BASE_ROUTE = "reminder_detail"
    const val REMINDER_DETAIL_ARG_ID = "reminderId"
    const val EDIT_REMINDER_BASE_ROUTE = "edit_reminder"
    const val EDIT_REMINDER_ARG_ID = "reminderId"
    const val EDIT_REMINDER_ROUTE_WITH_ARG_DEF = "$EDIT_REMINDER_BASE_ROUTE/{$EDIT_REMINDER_ARG_ID}"

    fun editReminderRoute(reminderId: Int): String {
        return "$EDIT_REMINDER_BASE_ROUTE/$reminderId"
    }
    const val REMINDER_DETAIL_ROUTE_WITH_ARG_DEF =
        "$REMINDER_DETAIL_BASE_ROUTE/{$REMINDER_DETAIL_ARG_ID}"

    fun reminderDetailRoute(reminderId: Int): String {
        return "$REMINDER_DETAIL_BASE_ROUTE/$reminderId"
    }

    const val CAR_HISTORY_ROUTE_WITH_ARG_DEF =
        "$CAR_HISTORY_BASE_ROUTE/{$CAR_HISTORY_ARG_VEHICLE_ID}"

    fun carHistoryRoute(vehicleId: Int): String {
        return "$CAR_HISTORY_BASE_ROUTE/$vehicleId"
    }

    fun addVehicleRoute(fromLoginNoVehicles: Boolean = false): String {
        return "$ADD_VEHICLE_FLOW_BASE_ROUTE?$ADD_VEHICLE_ARG_FROM_LOGIN=$fromLoginNoVehicles"
    }
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

    // LaunchedEffect pentru a gestiona navigarea inițială și reacția la schimbările de login
    LaunchedEffect(isSessionCheckComplete, isLoggedIn, navController.currentBackStackEntry) {
        Log.d(
            "AppNavHost",
            "Effect check. SessionComplete: $isSessionCheckComplete, LoggedIn: $isLoggedIn, CurrentRoute: ${navController.currentBackStackEntry?.destination?.route}"
        )
        if (isSessionCheckComplete) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val startDestinationForPopUp =
                Routes.SPLASH_LOADING // Folosim direct numele rutei de start

            if (isLoggedIn) {
                // Utilizatorul este logat, decide dacă navighează la MAIN sau ADD_VEHICLE
                if (currentRoute == Routes.SPLASH_LOADING || currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER) {
                    val clientId = authViewModel.jwtDecoder.getClientIdFromToken()
                    if (clientId != null) {
                        authViewModel.authRepository.hasVehicles(clientId)
                            .onSuccess {
                                Log.i(
                                    "AppNavHost",
                                    "User logged in, has vehicles. Navigating to MAIN."
                                )
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(startDestinationForPopUp) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            .onFailure {
                                Log.w(
                                    "AppNavHost",
                                    "User logged in, no vehicles. Navigating to Add Vehicle (fromLogin=true)."
                                )
                                navController.navigate(Routes.addVehicleRoute(fromLoginNoVehicles = true)) {
                                    popUpTo(startDestinationForPopUp) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                    } else {
                        Log.e("AppNavHost", "User logged in but ClientID is null. Forcing LOGIN.")
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(startDestinationForPopUp) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    Log.d(
                        "AppNavHost",
                        "User logged in. Already on a relevant route: $currentRoute. No initial navigation needed."
                    )
                }
            } else { // isLoggedIn este false
                // Utilizatorul NU este logat
                if (currentRoute != Routes.LOGIN && currentRoute != Routes.REGISTER) {
                    Log.i(
                        "AppNavHost",
                        "User not logged in. Current route: $currentRoute. Navigating to LOGIN."
                    )
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(startDestinationForPopUp) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    Log.d(
                        "AppNavHost",
                        "User not logged in. Already on LOGIN or REGISTER. No navigation needed."
                    )
                }
            }
        } else {
            Log.d("AppNavHost", "Session check NOT YET complete. Waiting...")
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_LOADING, // Începe mereu cu ecranul de splash
        modifier = modifier
    ) {
        composable(Routes.SPLASH_LOADING) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccessNavigateToMain = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginSuccessNavigateToAddVehicle = {
                    navController.navigate(Routes.addVehicleRoute(fromLoginNoVehicles = true)) {
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
                    Toast.makeText(
                        context,
                        "Registration Successful! Please login.",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                navigateBackToLogin = { navController.popBackStack() }
            )
        }

        // Rute protejate - verifică isLoggedIn și isSessionCheckComplete
        val protectedRouteModifier: @Composable (NavBackStackEntry, @Composable () -> Unit) -> Unit =
            { _, content ->
                if (!isLoggedIn && isSessionCheckComplete) {
                    // Dacă nu e logat și verificarea s-a terminat, nu ar trebui să fie aici.
                    // LaunchedEffect pentru a naviga după ce compoziția e stabilă.
                    LaunchedEffect(Unit) {
                        Log.w(
                            "AppNavHost",
                            "Accessing protected route ${navController.currentDestination?.route} while not logged in. Redirecting to LOGIN."
                        )
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(
                                navController.graph.findStartDestination().route
                                    ?: Routes.SPLASH_LOADING
                            ) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else if (isLoggedIn) {
                    content() // Afișează conținutul rutei dacă e logat
                } else {
                    // Afișează un loader dacă sesiunea încă se verifică
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            }

        composable(Routes.MAIN) {
            protectedRouteModifier(it) {
                MainScreen(mainNavController = navController, authViewModel = authViewModel)
            }
        }

        composable(
            route = Routes.REMINDER_DETAIL_ROUTE_WITH_ARG_DEF,
            arguments = listOf(navArgument(Routes.REMINDER_DETAIL_ARG_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            protectedRouteModifier(backStackEntry) {
                val reminderId = backStackEntry.arguments?.getInt(Routes.REMINDER_DETAIL_ARG_ID)
                if (reminderId != null) {
                    ReminderDetailScreen(navController = navController, reminderId = reminderId)
                } else {
                    // Caz de eroare - navighează înapoi dacă ID-ul lipsește
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "Error: Reminder ID is missing.", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
            }
        }

        composable(
            route = Routes.EDIT_REMINDER_ROUTE_WITH_ARG_DEF,
            arguments = listOf(navArgument(Routes.EDIT_REMINDER_ARG_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            protectedRouteModifier(backStackEntry) {
                val reminderId = backStackEntry.arguments?.getInt(Routes.EDIT_REMINDER_ARG_ID)
                if (reminderId != null) {
                    EditReminderScreen(navController = navController, reminderId = reminderId)
                } else {
                    // Caz de eroare
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "Error: Edit failed, reminder ID missing.", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
            }
        }

        composable(
            route = Routes.ADD_VEHICLE_ROUTE_WITH_ARG_DEF,
            arguments = listOf(navArgument(Routes.ADD_VEHICLE_ARG_FROM_LOGIN) {
                type = NavType.BoolType; defaultValue = false
            })
        ) { backStackEntry ->
            protectedRouteModifier(backStackEntry) {
                val fromLoginNoVehicles =
                    backStackEntry.arguments?.getBoolean(Routes.ADD_VEHICLE_ARG_FROM_LOGIN) ?: false
                AddVehicleScreen(
                    navController = navController,
                    fromLoginNoVehicles = fromLoginNoVehicles,
                    onVehicleAddedSuccessfully = {
                        Toast.makeText(context, "Vehicle Added!", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.MAIN) {
                            val routeNameToPopUpTo = if (fromLoginNoVehicles) {
                                Routes.SPLASH_LOADING // Pop up la ruta de start a grafului
                            } else {
                                Routes.ADD_VEHICLE_FLOW_BASE_ROUTE // Doar fluxul curent
                            }
                            popUpTo(routeNameToPopUpTo) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Routes.NOTIFICATIONS) {
            protectedRouteModifier(it) {
                NotificationsScreen(navController = navController)
            }
        }

        composable(Routes.ADD_MAINTENANCE) {
            protectedRouteModifier(it) {
                AddMaintenanceScreen(navController = navController)
            }
        }

        composable(
            route = Routes.CAR_HISTORY_ROUTE_WITH_ARG_DEF,
            arguments = listOf(navArgument(Routes.CAR_HISTORY_ARG_VEHICLE_ID) {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            protectedRouteModifier(backStackEntry) {
                val vehicleId = backStackEntry.arguments?.getInt(Routes.CAR_HISTORY_ARG_VEHICLE_ID)
                CarHistoryScreen(navController = navController, vehicleId = vehicleId)
            }
        }
    }
}