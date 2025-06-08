package com.example.cartrack.features.home.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cartrack.features.home.LocationData

@Composable
fun LocationWeatherRow(locationData: LocationData, lastSync: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = locationData.city,
                style = MaterialTheme.typography.bodySmall
            )
            // Imaginea pentru vreme se afișează doar dacă avem un URL
            if (locationData.iconUrl.isNotBlank()) {
                AsyncImage(
                    model = locationData.iconUrl,
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = locationData.temperature,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "Synced: $lastSync",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}