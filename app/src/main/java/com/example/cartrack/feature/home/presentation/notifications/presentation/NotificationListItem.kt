package com.example.cartrack.feature.home.presentation.notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign // Iconiță generică pentru anunț/notificare
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline // Pentru erori/atenționări
import androidx.compose.material.icons.filled.WarningAmber // Iconiță mai specifică pentru warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto
import com.example.cartrack.ui.theme.CarTrackTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun NotificationListItem(
    notification: NotificationResponseDto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Folosim un Row ca element de bază, fără Card, pentru un look de listă-feed
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top // Aliniem totul sus
    ) {
        // --- 1. Imaginea Vehiculului (Avatar) ---
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = "Vehicle",
            modifier = Modifier
                .size(48.dp) // Mărime potrivită pentru un avatar
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        // --- 2. Conținutul Text (Nume Vehicul + Mesaj + Data) ---
        Column(modifier = Modifier.weight(1f)) {
            // Folosim AnnotatedString pentru a combina numele vehiculului și mesajul
            Text(
                text = buildAnnotatedString {
                    if (notification.vehicleName != null && notification.vehicleYear != null) {
                        // Numele vehiculului este bold
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("${notification.vehicleName} (${notification.vehicleYear})")
                        }
                        append(" ") // Spațiu între nume și mesaj
                    }
                    // Mesajul este normal
                    append(notification.message)
                },
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp,
                // Dacă nu e citit, tot textul este un pic mai proeminent
                fontWeight = if (!notification.isRead) FontWeight.Medium else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Data notificării
            Text(
                text = formatDisplayDate(notification.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- 3. Indicatorul de necitit (opțional) ---
        if (!notification.isRead) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .padding(top = 6.dp) // Aliniere aproximativă cu prima linie de text
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}


// Helper pentru a formata data pentru afișare (simplificat)
// Poți extinde această funcție pentru formatare mai complexă ("X ago", etc.)
fun formatDisplayDate(dateString: String): String {
    return try {
        val instant = kotlinx.datetime.Instant.parse(dateString)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        // Format simplu: "HH:mm" pentru azi, "MMM dd" pentru alte zile
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        if (dateTime.date == today) {
            "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
        } else {
            "${dateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${dateTime.dayOfMonth}"
        }
    } catch (e: Exception) {
        dateString
    }
}


