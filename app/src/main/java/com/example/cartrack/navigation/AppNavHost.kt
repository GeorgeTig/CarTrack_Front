package com.example.cartrack.navigation

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
import com.example.cartrack.features.add_maintenance.AddMaintenanceScreen
import com.example.cartrack.features.add_vehicle.AddVehicleScreen
import com.example.cartrack.features.auth.AuthViewModel
import com.example.cartrack.features.auth.LoginScreen
import com.example.cartrack.features.auth.RegisterScreen
import com.example.cartrack.features.car_history.CarHistoryScreen
import com.example.cartrack.features.change_password.ChangePasswordScreen
import com.example.cartrack.features.edit_reminder.EditReminderScreen
import com.example.cartrack.features.notifications.NotificationsScreen
import com.example.cartrack.features.profile.EditProfileScreen
import com.example.cartrack.features.reminder_detail.ReminderDetailScreen
import com.example.cartrack.features.settings.SettingsScreen
import com.example.cartrack.main.MainScreen

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
        if (isSessionCheckComplete) {
            val startDestination = Routes.SPLASH_LOADING
            val currentRoute = navController.currentBackStackEntry?.destination?.route

            if (isLoggedIn) {
                if (currentRoute == startDestination || currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER) {
                    navController.navigate(Routes.MAIN) { popUpTo(startDestination) { inclusive = true } }
                }
            } else {
                if (currentRoute != Routes.LOGIN && currentRoute != Routes.REGISTER) {
                    navController.navigate(Routes.LOGIN) { popUpTo(startDestination) { inclusive = true } }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_LOADING,
        modifier = modifier
    ) {
        composable(Routes.SPLASH_LOADING) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccessNavigateToMain = {
                    navController.navigate(Routes.MAIN) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onLoginSuccessNavigateToAddVehicle = {
                    navController.navigate(Routes.addVehicleRoute(true)) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                navigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                navigateBackToLogin = { navController.popBackStack() }
            )
        }

        // --- Rute Protejate ---

        composable(Routes.MAIN) { MainScreen(appNavController = navController, authViewModel = authViewModel) }
        composable(Routes.SETTINGS) { SettingsScreen(navController = navController) }
        composable(Routes.EDIT_PROFILE) { EditProfileScreen(navController = navController) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(navController = navController) }
        composable(Routes.ADD_MAINTENANCE) { AddMaintenanceScreen(navController = navController) }
        composable(Routes.CHANGE_PASSWORD) { ChangePasswordScreen(navController = navController) }

        composable(
            route = Routes.ADD_VEHICLE_ROUTE_DEF,
            arguments = listOf(navArgument(Routes.ADD_VEHICLE_ARG) { type = NavType.BoolType; defaultValue = false })
        ) {
            val fromLogin = it.arguments?.getBoolean(Routes.ADD_VEHICLE_ARG) ?: false
            AddVehicleScreen(
                navController = navController,
                fromLoginNoVehicles = fromLogin,
                onVehicleAddedSuccessfully = {
                    Toast.makeText(context, "Vehicle Added!", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.MAIN) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.REMINDER_DETAIL_ROUTE_DEF,
            arguments = listOf(navArgument(Routes.REMINDER_ARG_ID) { type = NavType.IntType })
        ) {
            ReminderDetailScreen(navController = navController)
        }

        composable(
            route = Routes.CAR_HISTORY_ROUTE_DEF,
            arguments = listOf(navArgument(Routes.CAR_HISTORY_ARG_ID) { type = NavType.IntType })
        ) {
            CarHistoryScreen(navController = navController)
        }

        composable(
            route = Routes.EDIT_REMINDER_ROUTE_DEF,
            arguments = listOf(navArgument(Routes.REMINDER_ARG_ID) { type = NavType.IntType })
        ) {
            EditReminderScreen(navController = navController)
        }
    }
}