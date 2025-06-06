package com.example.cartrack.feature.addvehicle.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleStep

@Composable
internal fun StepIndicator(
    currentStepOrdinal: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val stepNames = AddVehicleStep.entries.map { getShortStepName(it) }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (index in 0 until totalSteps) {
                val isCompleted = index < currentStepOrdinal
                val isCurrent = index == currentStepOrdinal

                StepItem(
                    stepNumber = index + 1,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent
                )

                if (index < totalSteps - 1) {
                    HorizontalDivider( // Folosește HorizontalDivider din Material3
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 2.dp),
                        color = if (index < currentStepOrdinal) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            // Folosește Arrangement.SpaceAround pentru a distribui etichetele
            // Dar dacă numărul de pași variază, weight(1f) pe fiecare Text e mai flexibil
        ) {
            for (index in 0 until totalSteps) {
                val isCurrent = index == currentStepOrdinal
                val isActive = index <= currentStepOrdinal // Activat dacă e curent sau completat

                Text(
                    text = if (index < stepNames.size) stepNames[index] else "", // Numele scurt al pasului
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp, // Font mai mic pentru etichete
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 1.dp), // Fiecare etichetă ia spațiu egal
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun StepItem(
    stepNumber: Int,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val completedColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant // Un fundal mai deschis pentru inactiv
    val onActiveColor = MaterialTheme.colorScheme.onPrimary
    val onInactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    val backgroundColor = when {
        isCurrent -> activeColor
        isCompleted -> completedColor
        else -> inactiveColor
    }
    val contentColor = when {
        isCurrent -> onActiveColor
        isCompleted -> onActiveColor // Iconița Check va fi pe fundal primar
        else -> onInactiveColor
    }
    val fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = Modifier
            .size(28.dp) // Mărime puțin redusă pentru a se potrivi mai bine cu textul de sub
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted && !isCurrent) { // Arată bifa doar dacă e completat și NU e curent
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Step $stepNumber Completed",
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = stepNumber.toString(),
                color = contentColor,
                fontWeight = fontWeight,
                fontSize = 13.sp // Font puțin redus
            )
        }
    }
}

private fun getShortStepName(step: AddVehicleStep): String {
    return when (step) {
        AddVehicleStep.VIN -> "VIN"
        AddVehicleStep.SERIES_YEAR -> "Series" // Presupunând că "Series & Year" e prea lung
        AddVehicleStep.ENGINE_DETAILS -> "Engine"
        AddVehicleStep.BODY_DETAILS -> "Body"
        AddVehicleStep.VEHICLE_INFO -> "Info"
        AddVehicleStep.CONFIRM -> "Confirm"
    }
}