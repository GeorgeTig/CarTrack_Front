package com.example.cartrack.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cartrack.features.auth.AuthViewModel
import com.example.cartrack.main.bottomsheet.MainActionsBottomSheetContent
import com.example.cartrack.main.bottomsheet.mainBottomSheetActions
import com.example.cartrack.navigation.BottomNavGraph
import com.example.cartrack.navigation.BottomNavScreen
import com.example.cartrack.navigation.bottomNavItems
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    appNavController: NavHostController,
    authViewModel: AuthViewModel
) {
    val bottomBarNavController = rememberNavController()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, screen.title, Modifier.size(24.dp)) },
                        label = { Text(screen.title, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = isSelected && screen != BottomNavScreen.Add,
                        onClick = {
                            if (screen == BottomNavScreen.Add) {
                                showBottomSheet = true
                            } else {
                                bottomBarNavController.navigate(screen.route) {
                                    popUpTo(bottomBarNavController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            BottomNavGraph(
                bottomNavController = bottomBarNavController,
                appNavController = appNavController,
                authViewModel = authViewModel
            )
        }
        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
                MainActionsBottomSheetContent(
                    actions = mainBottomSheetActions,
                    onActionClick = { action ->
                        scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false }
                        action.route?.let { appNavController.navigate(it) }
                        // Aici poți adăuga logică și pentru acțiuni fără rută, ex: SyncMileage
                    }
                )
            }
        }
    }
}