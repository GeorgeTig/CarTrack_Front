package com.example.cartrack.features.auth.helpers

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
import com.example.cartrack.features.auth.PasswordStrength

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val (color, text) = when (strength) {
        PasswordStrength.NONE -> Color.Transparent to ""
        PasswordStrength.WEAK -> Color.Red.copy(alpha = 0.8f) to "Weak"
        PasswordStrength.MEDIUM -> Color(0xFFFFA500).copy(alpha = 0.8f) to "Medium" // Orange
        PasswordStrength.STRONG -> Color(0xFF4CAF50).copy(alpha = 0.8f) to "Strong" // Green
    }

    if (strength != PasswordStrength.NONE) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}