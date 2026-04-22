package edu.temple.safestride.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import edu.temple.safestride.model.SafeStrideUiState
import androidx.compose.ui.unit.dp

@Composable
fun SafetyDialogs(
    uiState: SafeStrideUiState,
    onDismissAlert: () -> Unit,
    onClearError: () -> Unit,
    onShareLocation: () -> Unit,
    onStopTracking: () -> Unit
) {
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("SafeStride") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onClearError) {
                    Text("OK")
                }
            }
        )
    }

    uiState.safetyAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = onDismissAlert,
            title = { Text(alert.title) },
            text = { Text(alert.message) },
            confirmButton = {
                Button(
                    onClick = {
                        onShareLocation()
                        onDismissAlert()
                    }
                ) {
                    Text("Share Location")
                }
            },

            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismissAlert) {
                        Text("I'm Okay")
                    }
                    TextButton(onClick = onStopTracking) {
                        Text("Stop Tracking")
                    }
                }
            }
        )
    }
}