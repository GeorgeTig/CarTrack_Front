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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleStep

@Composable
internal fun StepIndicator(
    currentStep: AddVehicleStep,
    modifier: Modifier = Modifier
) {
    val steps = AddVehicleStep.entries.toTypedArray()
    val currentStepOrdinal = currentStep.ordinal

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                val isCompleted = index < currentStepOrdinal
                val isCurrent = index == currentStepOrdinal
                val isActive = index <= currentStepOrdinal

                // Step Circle/Icon
                StepItem(
                    stepNumber = index + 1,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent
                )

                // Divider Line (Active color up to current step)
                if (index < steps.size - 1) {
                    Divider(
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
        // Row for labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            steps.forEachIndexed { index, step ->
                val isCurrent = index == currentStepOrdinal
                val isActive = index <= currentStepOrdinal

                Text(
                    text = getShortStepName(step),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 1.dp),
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
    // Use theme colors semantically
    val activeColor = MaterialTheme.colorScheme.primary
    val completedColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    val onActiveColor = MaterialTheme.colorScheme.onPrimary
    val onInactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    val backgroundColor = when {
        isCurrent -> activeColor
        isCompleted -> completedColor
        else -> inactiveColor
    }
    val contentColor = when {
        isCurrent -> onActiveColor
        isCompleted -> onActiveColor
        else -> onInactiveColor
    }
    val fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Step $stepNumber Completed",
                tint = contentColor,
                modifier = Modifier.size(18.dp)
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