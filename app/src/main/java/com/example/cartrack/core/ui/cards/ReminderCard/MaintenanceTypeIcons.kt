package com.example.cartrack.core.ui.cards.ReminderCard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import necessary icons
import androidx.compose.ui.graphics.vector.ImageVector

enum class MaintenanceTypeIcon(val icon: ImageVector) {
    OIL(Icons.Filled.OilBarrel),
    FLUIDS(Icons.Filled.Opacity),
    FILTERS(Icons.Filled.FilterAlt),
    BRAKES(Icons.Filled.DiscFull),
    ELECTRICAL(Icons.Filled.ElectricalServices),
    SUSPENSION(Icons.Filled.Compress),
    TRANSMISSION(Icons.Filled.Settings),
    STEERING(Icons.Filled.DirectionsCar),
    CONDITIONING(Icons.Filled.AcUnit),
    OTHER(Icons.Filled.Build);

    companion object {
        // --- Function uses typeId ---
        fun fromTypeId(typeId: Int?): MaintenanceTypeIcon {
            return when (typeId) {
                1 -> OIL
                2 -> FLUIDS
                3 -> FILTERS
                4 -> BRAKES
                5 -> ELECTRICAL
                6 -> SUSPENSION
                7 -> TRANSMISSION
                8 -> STEERING
                9 -> CONDITIONING
                else -> OTHER
            }
        }
    }
}