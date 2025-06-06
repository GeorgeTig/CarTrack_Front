package com.example.cartrack.feature.auth.presentation.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun PasswordRequirementsList(requirements: List<Pair<String, Boolean>>) {
    if (requirements.any { !it.first.contains("Minimum 8 characters") } || requirements.firstOrNull()?.second == false || requirements.any { it.second }) {
        Column(modifier = Modifier.padding(top = 4.dp)) {
            requirements.forEach { (requirement, met) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (met) Color.Green.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        requirement,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (met) Color.Green.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}