package com.example.cartrack.features.car_history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.ui.components.ShimmerPlaceholder
import com.valentinilk.shimmer.shimmer

@Composable
fun MaintenanceListShimmer(modifier: Modifier = Modifier) {
    // Am înlocuit LazyColumn cu un Column simplu.
    Column(
        modifier = modifier
            .fillMaxSize() // Asigură-te că ocupă spațiul
            .shimmer()
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Am înlocuit `items(5)` cu o buclă standard `repeat`.
        repeat(5) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
            )
        }
    }
}