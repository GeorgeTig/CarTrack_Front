package com.example.cartrack.features.home.helpers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto

@Composable
fun VehicleSelectorRow(
    vehicles: List<VehicleResponseDto>,
    selectedVehicleId: Int?,
    onVehicleSelect: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(vehicles, key = { it.id }) { vehicle ->
            VehicleStoryItem(
                vehicle = vehicle,
                isSelected = vehicle.id == selectedVehicleId,
                onClick = { onVehicleSelect(vehicle.id) }
            )
        }
    }
}

@Composable
private fun VehicleStoryItem(vehicle: VehicleResponseDto, isSelected: Boolean, onClick: () -> Unit) {
    val displayName = "${vehicle.producer} ${vehicle.series}"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!vehicle.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = vehicle.imageUrl,
                    contentDescription = displayName,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = displayName,
                    modifier = Modifier.size(40.dp),
                    colorFilter = ColorFilter.tint(if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}