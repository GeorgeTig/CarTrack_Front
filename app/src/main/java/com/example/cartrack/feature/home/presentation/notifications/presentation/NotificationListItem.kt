package com.example.cartrack.feature.home.presentation.notifications.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ErrorOutline // Pentru erori/atenționări
import androidx.compose.material.icons.filled.WarningAmber // Iconiță mai specifică pentru warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto
import com.example.cartrack.ui.theme.CarTrackTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Item individual pentru o notificare în listă
@Composable
fun NotificationListItem(
    notification: NotificationResponseDto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // Colțuri rotunjite
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) // Fundal ușor transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Umbră subtilă
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top // Aliniere sus pentru iconiță și text
        ) {
            // Icon based on notification type (placeholder logic)
            val notificationIcon = when {
                // Aici poți adăuga logică pentru a alege iconița pe baza conținutului notificării
                // de ex. notification.message.contains("due", ignoreCase = true) -> Icons.Filled.WarningAmber
                // notification.message.contains("error", ignoreCase = true) -> Icons.Filled.ErrorOutline
                else -> Icons.Filled.Campaign // Iconiță default
            }
            val iconTint = when (notificationIcon) {
                Icons.Filled.WarningAmber -> MaterialTheme.colorScheme.tertiary // O culoare distinctă pentru avertismente
                Icons.Filled.ErrorOutline -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }

            Icon(
                imageVector = notificationIcon,
                contentDescription = "Notification type",
                tint = iconTint,
                modifier = Modifier
                    .size(36.dp) // Mărime iconiță
                    .padding(top = 2.dp) // Ușoară ajustare verticală
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface // Culoare text principal
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Data formatată (aici poți adăuga formatare "X minutes ago", "Yesterday at HH:mm" etc.)
                // Momentan afișăm data brută ca placeholder
                Text(
                    text = formatDisplayDate(notification.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Culoare text secundar
                )
            }

            // Indicator subtil de "necitit"
            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp) // Aliniere cu prima linie de text
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
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
        dateString // Returnează string-ul original dacă parsarea eșuează
    }
}


@Preview(showBackground = true, name = "NotificationListItem Read Preview")
@Composable
fun NotificationListItemReadPreview() {
    CarTrackTheme {
        NotificationListItem(
            notification = NotificationResponseDto(
                id = 1,
                message = "Your vehicle maintenance is due soon. Check details for more information.",
                date = "2023-10-27T10:30:00Z",
                isRead = true,
                userId = 1,
                vehicleId = 101,
                reminderId = 201
            ),
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true, name = "NotificationListItem Unread Preview")
@Composable
fun NotificationListItemUnreadPreview() {
    CarTrackTheme {
        NotificationListItem(
            notification = NotificationResponseDto(
                id = 2,
                message = "Oil change for Toyota Camry is overdue by 500 km.",
                date = "2024-03-10T14:15:00Z", // O dată mai recentă pentru a testa "Today"
                isRead = false,
                userId = 1,
                vehicleId = 102,
                reminderId = 202
            ),
            modifier = Modifier.padding(8.dp)
        )
    }
}