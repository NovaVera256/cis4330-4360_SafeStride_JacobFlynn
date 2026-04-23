package edu.temple.safestride.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.temple.safestride.data.tracking.LocationTracker
import edu.temple.safestride.data.tracking.MotionMonitor
import edu.temple.safestride.data.tracking.TrackingCoordinator
import edu.temple.safestride.model.MotionSample
import edu.temple.safestride.model.SafeStrideUiState
import edu.temple.safestride.model.SafetyAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri

class SafeStrideViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(SafeStrideUiState())
    val uiState: StateFlow<SafeStrideUiState> = _uiState.asStateFlow()

    private val motionMonitor = MotionMonitor(appContext) { sample ->
        coordinator.onMotionSample(sample)
    }

    private val locationTracker = LocationTracker(appContext) { location ->
        coordinator.onLocation(location)
    }

    private val coordinator = TrackingCoordinator(
        onRoutePoint = { point, speed ->
            val current = _uiState.value
            _uiState.value = current.copy(
                routePoints = current.routePoints + point,
                currentLocation = point,
                speedMps = speed,
                statusMessage = if (speed > 0.3f) "Movement detected" else "Standing or slow movement"
            )
        },

        onMotionUpdate = { sample: MotionSample ->
            _uiState.value = _uiState.value.copy(
                accelMagnitude = sample.accelMagnitude,
                gyroMagnitude = sample.gyroMagnitude
            )
        },

        onSafetyAlert = { alert: SafetyAlert ->
            if (_uiState.value.safetyAlert == null) {
                _uiState.value = _uiState.value.copy(safetyAlert = alert)
            }
        }
    )

    init {
        _uiState.value = _uiState.value.copy(
            accelerometerAvailable = motionMonitor.hasAccelerometer(),
            gyroscopeAvailable = motionMonitor.hasGyroscope(),
            errorMessage = when {
                !motionMonitor.hasAccelerometer() && !motionMonitor.hasGyroscope() ->
                    "Accelerometer and gyroscope are not available on this device."
                !motionMonitor.hasAccelerometer() ->
                    "Accelerometer is not available on this device."
                !motionMonitor.hasGyroscope() ->
                    "Gyroscope is not available on this device."
                else -> null
            }
        )
    }

    fun updatePermissionState(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasLocationPermission = granted)
    }

    fun startTracking() {
        val state = _uiState.value

        if (!state.hasLocationPermission) {
            _uiState.value = state.copy(
                errorMessage = "Location permission is required before tracking can start."
            )

            return
        }

        if (!motionMonitor.hasRequiredSensors()) {
            _uiState.value = state.copy(
                errorMessage = "Required motion sensors are not available on this device."
            )

            return
        }

        coordinator.reset()

        _uiState.value = state.copy(
            isTracking = true,
            routePoints = emptyList(),
            currentLocation = null,
            speedMps = 0f,
            accelMagnitude = 0f,
            gyroMagnitude = 0f,
            safetyAlert = null,
            errorMessage = null,
            statusMessage = "Tracking active"
        )

        try {
            motionMonitor.start()
            locationTracker.start()
        } catch (e: SecurityException) {
            _uiState.value = _uiState.value.copy(
                isTracking = false,
                errorMessage = "Location permission was missing when updates were requested."
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isTracking = false,
                errorMessage = e.message ?: "An unexpected tracking error occurred."
            )
        }
    }

    fun stopTracking() {
        motionMonitor.stop()
        locationTracker.stop()
        _uiState.value = _uiState.value.copy(
            isTracking = false,
            statusMessage = "Tracking stopped"
        )
    }

    fun dismissSafetyAlert() {
        _uiState.value = _uiState.value.copy(safetyAlert = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun shareCurrentLocation(context: Context) {
        val location = _uiState.value.currentLocation ?: return
        val mapsUrl = "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "SafeStride location check-in:\n$mapsUrl"
            )
        }

        context.startActivity(Intent.createChooser(sendIntent, "Share location"))
    }

    fun openEmergencyDialer(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:911")
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No phone dialer app is available on this device.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }
}