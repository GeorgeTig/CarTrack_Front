package com.example.cartrack.main.presentation.bottomsheet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cartrack.feature.navigation.Routes // Asigură-te că acest import e corect

sealed class BottomSheetAction(
    val title: String,
    val icon: ImageVector,
    val route: String? = null,
    val isStyledAsButton: Boolean = false // NOU: Flag pentru stilizare specială ca buton plin
) {
    data object AddVehicle : BottomSheetAction(
        title = "Add New Vehicle",
        icon = Icons.Filled.AddCircleOutline,
        route = Routes.ADD_VEHICLE
        // isStyledAsButton = false (default)
    )
    data object AddMaintenance : BottomSheetAction(
        title = "Add Maintenance Log",
        icon = Icons.Filled.Build,
        route = Routes.ADD_MAINTENANCE // <-- VERIFICĂ ACEASTĂ LINIE
    )
    data object SyncMileage : BottomSheetAction(
        title = "Sync Vehicle Mileage",
        icon = Icons.AutoMirrored.Filled.DirectionsRun,
        isStyledAsButton = true // Marcat pentru stilizare ca buton plin
    )
    // Poți adăuga alte acțiuni normale aici, înainte de SyncMileage dacă vrei
}

// Reordonează lista
val mainBottomSheetActions = listOf(
    BottomSheetAction.AddVehicle,
    BottomSheetAction.AddMaintenance,
    BottomSheetAction.SyncMileage // Acum este ultima
)