package com.example.cartrack.feature.home.presentation.helpers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto

@Composable
fun VehicleStoryItem(
    vehicle: VehicleResponseDto,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lățimea aproximativă pentru a încăpea 3-4 item-uri.
    // (Lățime ecran - padding-uri - spații între item-uri) / număr item-uri
    // Ex: (360dp - 32dp padding - 2*10dp spațiu) / 3 = ~102dp
    // Ex: (360dp - 32dp padding - 3*10dp spațiu) / 4 = ~74dp
    // Alegem o valoare între, ex: 85dp - 90dp. Ajustează după testare.
    val itemWidth = 88.dp
    val imageSize = 72.dp // Mărime mai mare pentru cerc

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(itemWidth) // Lățime fixă pentru item
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp) // Padding vertical în interiorul item-ului
    ) {
        Box(
            modifier = Modifier
                .size(imageSize) // Mărime imagine actualizată
                .clip(CircleShape)
                .background(
                    // Fundal mai subtil pentru cele neselectate
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) // Elevation mic
                )
                .border(
                    BorderStroke(
                        width = if (isSelected) 2.5.dp else 1.dp, // Bordură mai groasă la selectat
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) // Bordură subtilă la neselectat
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = Icons.Filled.DirectionsCar, // TODO: Imagine reală
                contentDescription = vehicle.series,
                modifier = Modifier.size(imageSize * 0.6f), // Iconița să fie proporțională cu cercul
                colorFilter = ColorFilter.tint(
                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp)) // Spațiu mai mic
        Text(
            text = vehicle.series, // Doar seria/numele aici
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), // Puțin mai bold
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center, // Text centrat
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = vehicle.year.toString(), // Anul pe rândul următor
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center, // Text centrat
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}