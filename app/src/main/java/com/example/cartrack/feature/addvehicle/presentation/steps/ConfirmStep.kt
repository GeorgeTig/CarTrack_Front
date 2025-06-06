package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import pentru diverse iconițe
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow
import com.example.cartrack.feature.addvehicle.presentation.getFinalConfirmedModelDetailsForDisplay

@Composable
internal fun ConfirmVehicleStep(uiState: AddVehicleUiState) {
    // Căutăm din nou engine-ul pe baza ID-ului confirmat
    val determinedEngine = uiState.allDecodedOptions
        .flatMap { it.vehicleModelInfo }
        .flatMap { it.engineInfo }
        .find { it.engineId == uiState.confirmedEngineId }

    // Căutăm din nou body-ul pe baza ID-ului confirmat
    val determinedBody = uiState.allDecodedOptions
        .flatMap { it.vehicleModelInfo }
        .flatMap { it.bodyInfo }
        .find { it.bodyId == uiState.confirmedBodyId }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ... restul codului este identic cu cel dinainte de refactorizarea agresivă ...
        Text("Confirm Vehicle Details", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Article, "General Info", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("General Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Divider(Modifier.padding(vertical = 8.dp))
                DetailRow("VIN:", uiState.vinInput.ifBlank { "N/A" })
                DetailRow("Vehicle:", uiState.getFinalConfirmedModelDetailsForDisplay().ifBlank { "N/A" })
            }
        }
        determinedEngine?.let { engine ->
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Settings, "Engine Info", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Engine Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Divider(Modifier.padding(vertical = 8.dp))
                    DetailRow("Type:", engine.engineType)
                    DetailRow("Size:", "${engine.size} L")
                    DetailRow("Horsepower:", "${engine.horsepower} hp")
                    DetailRow("Transmission:", engine.transmission)
                    DetailRow("Drive Type:", engine.driveType)
                }
            }
        }
        determinedBody?.let { body ->
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Weekend, "Body Info", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Body Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Divider(Modifier.padding(vertical = 8.dp))
                    DetailRow("Body Style:", body.bodyType)
                    DetailRow("Doors:", body.doorNumber.toString())
                    DetailRow("Seats:", body.seatNumber.toString())
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Speed, "Mileage Info", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Mileage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Divider(Modifier.padding(vertical = 8.dp))
                DetailRow("Current Mileage:", "${uiState.mileageInput} km")
            }
        }
    }
}