package com.example.cartrack.main.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.navigation.BottomNavScreen
import com.example.cartrack.feature.navigation.bottomNavItems
import com.example.cartrack.main.presentation.bottomsheet.BottomNavGraph
import com.example.cartrack.main.presentation.bottomsheet.BottomSheetAction
import com.example.cartrack.main.presentation.bottomsheet.MainActionsBottomSheetContent
import com.example.cartrack.main.presentation.bottomsheet.mainBottomSheetActions
import kotlinx.coroutines.launch // Import pentru launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    mainNavController: NavHostController, // Acesta este controller-ul global
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val bottomBarNavController = rememberNavController()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                // Pentru BottomNavScreen.Add, poți folosi o iconiță diferită dacă vrei
                                imageVector = if (screen == BottomNavScreen.Add) Icons.Filled.Menu /* Sau Icons.Filled.AddCircle */ else screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = isSelected, // Butonul "Add" nu va fi niciodată "selected" în bottom nav graph
                        onClick = {
                            if (screen == BottomNavScreen.Add) {
                                Log.d("MainScreen", "Add button in BottomBar clicked. Showing bottom sheet.") // LOG ADAUGAT
                                scope.launch {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = true
                                    }
                                }
                            } else {
                                bottomBarNavController.navigate(screen.route) {
                                    popUpTo(bottomBarNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            BottomNavGraph(
                navController = bottomBarNavController,
                appGlobalNavController = mainNavController,
                authViewModel = authViewModel
            )
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    Log.d("MainScreen", "ModalBottomSheet onDismissRequest.") // LOG ADAUGAT
                    showBottomSheet = false
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ) {
                MainActionsBottomSheetContent(
                    actions = mainBottomSheetActions,
                    onActionClick = { action ->
                        // Închide bottom sheet-ul întâi
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                        // Apoi gestionează acțiunea
                        Log.d("MainScreen", "BottomSheet action clicked: ${action.title}") // LOG ADAUGAT
                        when (action) {
                            is BottomSheetAction.AddVehicle -> {
                                action.route?.let { route ->
                                    Log.d("MainScreen", "Navigating to AddVehicle: $route") // LOG ADAUGAT
                                    mainNavController.navigate(route)
                                } ?: Log.e("MainScreen", "AddVehicle action has no route!")
                            }
                            is BottomSheetAction.SyncMileage -> {
                                Log.d("MainScreen", "Sync Mileage Action Triggered")
                                // TODO: Implementează logica
                            }
                            is BottomSheetAction.AddMaintenance -> { // CAZUL PENTRU ADD MAINTENANCE
                                action.route?.let { route ->
                                    Log.d("MainScreen", "Navigating to AddMaintenance: $route") // LOG ADAUGAT
                                    mainNavController.navigate(route)
                                } ?: Log.e("MainScreen", "AddMaintenance action has no route!")
                            }
                        }
                    }
                )
            }
        }
    }
}
