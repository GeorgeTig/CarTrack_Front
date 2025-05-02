package com.example.cartrack.feature.addvehicle.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleStep

@Composable
internal fun StepIndicator(
    currentStep: AddVehicleStep,
    modifier: Modifier = Modifier
) {
    val steps = AddVehicleStep.values()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Distribute space
        ) {
            steps.forEachIndexed { index, step ->
                val stepNumber = index + 1
                val isCompleted = step.ordinal < currentStep.ordinal
                val isCurrent = step == currentStep

                StepItem(
                    stepNumber = stepNumber,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent
                )

                // Add divider line between steps, but not after the last one
                if (index < steps.size - 1) {
                    Divider(
                        modifier = Modifier
                            .weight(1f) // Take available space
                            .height(2.dp)
                            .padding(horizontal = 4.dp), // Space around line
                        color = if (isCompleted || isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp)) // Space between numbers and labels
        // Row for labels (optional, adjust alignment if needed)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround // Try to align labels below numbers
        ) {
            steps.forEach { step ->
                Text(
                    text = getShortStepName(step), // Use short names
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp, // Smaller font for labels
                    modifier = Modifier.weight(1f) // Give equal weight
                        .padding(horizontal = 2.dp), // Prevent overlap
                    color = if (step == currentStep) MaterialTheme.colorScheme.primary else Color.Unspecified
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
    val completedColor = MaterialTheme.colorScheme.primary // Or a different green/color
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant
    val onActiveColor = MaterialTheme.colorScheme.onPrimary
    val onInactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    val backgroundColor = when {
        isCompleted -> completedColor
        isCurrent -> activeColor
        else -> inactiveColor
    }
    val contentColor = when {
        isCompleted && !isCurrent -> onActiveColor // Checkmark color on completed background
        isCurrent -> onActiveColor
        else -> onInactiveColor
    }
    val fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = Modifier
            .size(28.dp) // Size of the circle
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted && !isCurrent) {
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
                fontSize = 14.sp
            )
        }
    }
}

// Helper for short step names for the indicator labels
private fun getShortStepName(step: AddVehicleStep): String {
    return when (step) {
        AddVehicleStep.VIN -> "VIN"
        AddVehicleStep.SERIES -> "Series"
        AddVehicleStep.ENGINE -> "Engine"
        AddVehicleStep.BODY -> "Body"
        AddVehicleStep.MILEAGE -> "Mileage"
        AddVehicleStep.CONFIRM -> "Confirm"
    }
}