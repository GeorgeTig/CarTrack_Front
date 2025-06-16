package com.example.cartrack.features.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.core.data.model.notification.NotificationResponseDto
import kotlinx.datetime.*

@Composable
fun NotificationListItem(
    notification: NotificationResponseDto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = "Vehicle",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    notification.vehicleName?.let {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("$it (${notification.vehicleYear ?: ""})")
                        }
                        append(" ")
                    }
                    append(notification.message)
                },
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp,
                fontWeight = if (!notification.isRead) FontWeight.Medium else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formatDisplayDate(notification.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!notification.isRead) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private fun formatDisplayDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        if (dateTime.date == today) {
            "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
        } else {
            "${dateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${dateTime.dayOfMonth}"
        }
    } catch (e: Exception) {
        dateString.take(10)
    }
}