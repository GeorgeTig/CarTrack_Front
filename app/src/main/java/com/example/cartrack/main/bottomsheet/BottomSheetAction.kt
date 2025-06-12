package com.example.cartrack.main.bottomsheet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cartrack.navigation.Routes

sealed class BottomSheetAction(
    val title: String,
    val icon: ImageVector,
    val route: String? = null,
    val isStyledAsButton: Boolean = false
) {
    data object AddVehicle : BottomSheetAction(
        title = "Add New Vehicle",
        icon = Icons.Filled.AddCircleOutline,
        route = Routes.addVehicleRoute(fromLoginNoVehicles = false)
    )
    data object AddMaintenance : BottomSheetAction(
        title = "Add Maintenance Log",
        icon = Icons.Filled.Build,
        route = Routes.ADD_MAINTENANCE
    )
    // --- ACȚIUNE NOUĂ ---
    data object AddCustomReminder : BottomSheetAction(
        title = "Add Custom Reminder",
        icon = Icons.Filled.NotificationsActive,
        route = Routes.ADD_CUSTOM_REMINDER // Vom adăuga această rută
    )
    // --- SFÂRȘIT ACȚIUNE NOUĂ ---

    data object QuickSyncMileage : BottomSheetAction(
        title = "Quick Sync Mileage",
        icon = Icons.AutoMirrored.Filled.DirectionsRun,
        isStyledAsButton = true
    )
}

// Actualizăm lista de acțiuni
val mainBottomSheetActions = listOf(
    BottomSheetAction.AddVehicle,
    BottomSheetAction.AddMaintenance,
    BottomSheetAction.AddCustomReminder, // Am adăugat noua acțiune
    BottomSheetAction.QuickSyncMileage
)