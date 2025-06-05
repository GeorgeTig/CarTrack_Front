package com.example.cartrack.feature.profile.presentation.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.profile.data.model.UserResponseDto

@Composable
fun UserInfoSection(user: UserResponseDto) {
    Card( // Folosim un Card pentru a grupa informațiile utilizatorului
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(72.dp) // Mărime ajustată
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleLarge, // Puțin mai mare
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                UserInfoDetailRow(icon = Icons.Filled.Email, text = user.email)
                Spacer(modifier = Modifier.height(4.dp))
                val phoneNumberDisplay = if (user.phoneNumber == "0" || user.phoneNumber.isBlank()) "N/A" else user.phoneNumber
                UserInfoDetailRow(icon = Icons.Filled.Phone, text = phoneNumberDisplay)
            }
        }
    }
}