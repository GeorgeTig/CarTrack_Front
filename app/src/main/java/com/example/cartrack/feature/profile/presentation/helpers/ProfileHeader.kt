package com.example.cartrack.feature.profile.presentation.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.profile.data.model.UserResponseDto

/**
 * Un Composable care afișează antetul principal al ecranului de profil.
 * Include imaginea de profil, numele utilizatorului și statisticile ("achievements").
 *
 * @param user Obiectul [UserResponseDto] care conține numele utilizatorului.
 * @param garageCount Numărul total de vehicule pentru statistică.
 * @param maintenanceLogsCount Numărul total de log-uri de mentenanță pentru statistică.
 * @param modifier Modifier-ul de aplicat acestui layout.
 */
@Composable
fun ProfileHeader(
    user: UserResponseDto,
    garageCount: Int,
    maintenanceLogsCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imaginea de Profil
        Image(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Numele utilizatorului
        Text(
            text = user.username,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Statistici ("Achievements")
        Spacer(modifier = Modifier.height(20.dp))
        ProfileStats(
            garageCount = garageCount,
            maintenanceLogsCount = maintenanceLogsCount
        )
    }
}

