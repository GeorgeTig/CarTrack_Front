package com.example.cartrack.features.add_vehicle.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.features.add_vehicle.AddVehicleStep

@Composable
fun StepIndicator(
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
                    HorizontalDivider(
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
        Row(modifier = Modifier.fillMaxWidth()) {
            for (index in 0 until totalSteps) {
                val isActive = index <= currentStepOrdinal

                Text(
                    text = if (index < stepNames.size) stepNames[index] else "",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (index == currentStepOrdinal) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
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
private fun StepItem(stepNumber: Int, isCompleted: Boolean, isCurrent: Boolean) {
    val backgroundColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isCurrent || isCompleted -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted && !isCurrent) {
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
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

private fun getShortStepName(step: AddVehicleStep): String {
    return when (step) {
        AddVehicleStep.VIN -> "VIN"
        AddVehicleStep.SERIES_YEAR -> "Series"
        AddVehicleStep.ENGINE_DETAILS -> "Engine"
        AddVehicleStep.BODY_DETAILS -> "Body"
        AddVehicleStep.VEHICLE_INFO -> "Info"
        AddVehicleStep.CONFIRM -> "Confirm"
    }
}