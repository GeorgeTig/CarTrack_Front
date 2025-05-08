package com.example.cartrack.feature.home.presentation.details

import androidx.compose.animation.AnimatedVisibility // Import for animations
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import com.example.cartrack.core.ui.cards.VehicleBodyCard
import com.example.cartrack.core.ui.cards.VehicleEngineCard
import com.example.cartrack.core.ui.cards.VehicleModelCard

@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Main container for the details tab content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.selectedVehicleId != null) {
            // Inner Column for "Show Details" label and buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Show Car Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Row for the action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Use the themed DetailActionButton
                    DetailActionButton("Engine") { viewModel.showDetailSection(VisibleDetailSection.ENGINE) }
                    DetailActionButton("Model") { viewModel.showDetailSection(VisibleDetailSection.MODEL) }
                    DetailActionButton("Body") { viewModel.showDetailSection(VisibleDetailSection.BODY) }
                 }
            }

            Spacer(modifier = Modifier.height(24.dp)) // Space below buttons

            // --- Conditionally Displayed Detail Card Area ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                if (uiState.isLoadingDetails) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 24.dp),
                        // Use theme color
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // Use AnimatedVisibility (Optional, but kept from previous examples)
                    this@Column.AnimatedVisibility(
                        visible = uiState.visibleDetail != VisibleDetailSection.NONE,
                        enter = slideInVertically { it / 2 } + fadeIn(),
                        exit = slideOutVertically { -it / 2 } + fadeOut()
                    ) {
                        // Use the themed cards and ensure all cases are handled
                        when (uiState.visibleDetail) {
                            VisibleDetailSection.ENGINE -> VehicleEngineCard(engineInfo = uiState.engineDetails)
                            VisibleDetailSection.MODEL -> VehicleModelCard(modelInfo = uiState.modelDetails)
                            VisibleDetailSection.BODY -> VehicleBodyCard(bodyInfo = uiState.bodyDetails)
                            VisibleDetailSection.NONE -> Spacer(Modifier.height(0.dp))
                        }
                    }
                }
            }

            // Display specific error for details loading
            if (!uiState.isLoadingDetails && uiState.error != null ) {
                Text(
                   text = "Error loading details: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

        } else {
            // What to show if no vehicle ID is available in the state (e.g., initial state before HomeVM loads)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally){
                    Text(
                        "Please select a vehicle",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Muted text
                    )
                    if (uiState.isLoadingInitialId) { // Assuming you add this state back to DetailsUiState
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Reusable Detail Action Button (Themed)
 */
@Composable
private fun DetailActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(text, fontSize = 12.sp)
    }
}