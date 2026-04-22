package edu.temple.safestride.model

import com.google.android.gms.maps.model.LatLng

data class SafeStrideUiState(
    val isTracking: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val routePoints: List<LatLng> = emptyList(),
    val currentLocation: LatLng? = null,
    val speedMps: Float = 0f,
    val accelMagnitude: Float = 0f,
    val gyroMagnitude: Float = 0f,
    val statusMessage: String = "Ready to track",
    val errorMessage: String? = null,
    val safetyAlert: SafetyAlert? = null,
    val accelerometerAvailable: Boolean = true,
    val gyroscopeAvailable: Boolean = true
)