package com.example.cartrack.feature.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess1 = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginSuccess2 = {
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
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                navigateBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                mainNavController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.ADD_VEHICLE) {
            AddVehicleScreen(
                onNavigateBack = { navController.popBackStack() },
                onVehicleAddedSuccessfully = {
                    Log.d("AppNavHost", "Vehicle Added Successfully callback invoked.")
                    Toast.makeText(context, "Vehicle Added!", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ADD_VEHICLE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(navController = navController) // Pass navController
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