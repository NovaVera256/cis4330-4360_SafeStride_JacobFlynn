package edu.temple.safestride.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.temple.safestride.model.SafeStrideUiState
import edu.temple.safestride.util.format

@Composable
fun StatusCard(uiState: SafeStrideUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Live Status", fontWeight = FontWeight.SemiBold)

            HorizontalDivider()

            MetricRow("Status", uiState.statusMessage)
            MetricRow("Route Points", uiState.routePoints.size.toString())
            MetricRow("Speed", "${uiState.speedMps.format(2)} m/s")
            MetricRow("Accelerometer", uiState.accelMagnitude.format(2))
            MetricRow("Gyroscope", uiState.gyroMagnitude.format(2))
            MetricRow("Accelerometer Available", if (uiState.accelerometerAvailable) "Yes" else "No")
            MetricRow("Gyroscope Available", if (uiState.gyroscopeAvailable) "Yes" else "No")

            uiState.currentLocation?.let {
                HorizontalDivider()
                Text("Current Coordinates", fontWeight = FontWeight.Medium)
                Text("${it.latitude.format(6)}, ${it.longitude.format(6)}")
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value)
    }
}