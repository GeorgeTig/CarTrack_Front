package com.example.cartrack.feature.auth.presentation.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.auth.presentation.PasswordStrength

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val color = when (strength) {
        PasswordStrength.NONE -> Color.Transparent
        PasswordStrength.WEAK -> Color.Red.copy(alpha = 0.7f)
        PasswordStrength.MEDIUM -> Color(0xFFFFA500).copy(alpha = 0.8f)
        PasswordStrength.STRONG -> Color.Green.copy(alpha = 0.7f)
    }
    val text = when (strength) {
        PasswordStrength.NONE -> ""
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
    }
    if (strength != PasswordStrength.NONE) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}