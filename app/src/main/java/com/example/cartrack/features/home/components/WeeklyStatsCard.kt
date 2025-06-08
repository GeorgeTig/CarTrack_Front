package com.example.cartrack.features.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.vehicle.DailyUsageDto
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.DecimalFormat

@Composable
fun WeeklyStatsCard(dailyUsage: List<DailyUsageDto>, modifier: Modifier = Modifier) {
    val chartEntryModelProducer = remember(dailyUsage) {
        ChartEntryModelProducer(dailyUsage.mapIndexed { index, usage -> entryOf(index.toFloat(), usage.distance) })
    }

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        dailyUsage.getOrNull(value.toInt())?.dayLabel ?: ""
    }

    val totalDistance = remember(dailyUsage) { dailyUsage.sumOf { it.distance } }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Weekly Usage Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (dailyUsage.any { it.distance > 0 }) {
                Chart(
                    chart = columnChart(),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(title = "Miles"),
                    bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter, guideline = null),
                    modifier = Modifier.height(200.dp)
                )
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    Text("No usage data recorded for the last 7 days.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Sumar text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total this week:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val formatter = remember { DecimalFormat("#,###.#") }
                Text("${formatter.format(totalDistance)} mi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}