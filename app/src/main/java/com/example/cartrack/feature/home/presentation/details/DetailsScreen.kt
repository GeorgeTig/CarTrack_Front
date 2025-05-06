package com.example.cartrack.feature.home.presentation.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.core.ui.cards.* // Import your detail cards

@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Main container for the details tab content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Make the whole tab scrollable
            .padding(16.dp), // Padding for the content within the tab
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Only show buttons and details if a vehicle is actually selected
        if (uiState.selectedVehicleId != null) {
            // Inner Column for "Show Details" label and buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Show Car Details",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp) // Space below label
                )
                // Row for the action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center
                ) {
                    DetailActionButton("Engine") { viewModel.showDetailSection(VisibleDetailSection.ENGINE) }
                    DetailActionButton("Model") { viewModel.showDetailSection(VisibleDetailSection.MODEL) }
                    DetailActionButton("Body") { viewModel.showDetailSection(VisibleDetailSection.BODY) }
                }
            } // End inner Column

            Spacer(modifier = Modifier.height(24.dp)) // Space below buttons

            // --- Conditionally Displayed Detail Card Area ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                if (uiState.isLoadingDetails) {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 24.dp))
                } else {

                        when (uiState.visibleDetail) {
                            VisibleDetailSection.ENGINE -> VehicleEngineCard(engineInfo = uiState.engineDetails)
                            VisibleDetailSection.BODY -> VehicleBodyCard(bodyInfo = uiState.bodyDetails)
                           VisibleDetailSection.NONE -> Spacer(Modifier.height(0.dp))
                            VisibleDetailSection.MODEL -> VehicleModelCard(modelInfo = uiState.modelDetails)

                        }
                    }

            }

            // Display specific error for details loading
            if (!uiState.isLoadingDetails && uiState.error != null && uiState.visibleDetail != VisibleDetailSection.NONE) {
                Text(
                    "Error loading ${uiState.visibleDetail.name.lowercase()} details: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

        } else {
            // What to show in the Details tab if no vehicle is selected in the top dropdown
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Please select a vehicle from the dropdown above.")
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Bottom padding within scrollable area
    } // End Main Details Column
}

/**
 * Reusable Detail Action Button (can be moved to a shared location)
 */
@Composable
private fun DetailActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text, fontSize = 12.sp)
    }
}