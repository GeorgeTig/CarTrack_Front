package com.example.cartrack.feature.profile.presentation.helpers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.cartrack.ui.theme.CarTrackTheme

/**
 * Un Composable care afișează un rând de statistici cheie, cum ar fi numărul de vehicule
 * și numărul de log-uri de mentenanță.
 *
 * @param garageCount Numărul de vehicule de afișat.
 * @param maintenanceLogsCount Numărul de log-uri de mentenanță de afișat.
 * @param modifier Modifier-ul de aplicat acestui layout.
 */
@Composable
fun ProfileStats(
    garageCount: Int,
    maintenanceLogsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly // Distribuie spațiul egal între item-uri
    ) {
        StatItem(count = garageCount.toString(), label = "Your Garage")
        StatItem(count = maintenanceLogsCount.toString(), label = "Maintenance Logs")
        // Poți adăuga ușor un al treilea item aici dacă va fi nevoie
        // StatItem(count = "123", label = "Days Tracked")
    }
}

/**
 * Un item individual de statistică, format dintr-un număr mare și o etichetă mică.
 * Acest Composable este privat, deoarece este folosit doar în interiorul `ProfileStats`.
 *
 * @param count Numărul de afișat (ca String).
 * @param label Eticheta de sub număr.
 */
@Composable
private fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Numărul, afișat cu un font mare și bold
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        // Eticheta, afișată cu un font mai mic și o culoare subtilă
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Preview pentru a testa componenta în izolare
@Preview(showBackground = true, name = "Profile Stats Preview")
@Composable
fun ProfileStatsPreview() {
    CarTrackTheme {
        ProfileStats(garageCount = 3, maintenanceLogsCount = 12)
    }
}
