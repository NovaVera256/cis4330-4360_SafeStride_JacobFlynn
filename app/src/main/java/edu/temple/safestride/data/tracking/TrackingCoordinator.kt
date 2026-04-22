package edu.temple.safestride.data.tracking

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import edu.temple.safestride.model.MotionSample
import edu.temple.safestride.model.SafetyAlert
import edu.temple.safestride.model.SafetyAlertType
import kotlin.math.max

class TrackingCoordinator(
    private val onRoutePoint: (LatLng, Float) -> Unit,
    private val onMotionUpdate: (MotionSample) -> Unit,
    private val onSafetyAlert: (SafetyAlert) -> Unit
) {

    private var previousLocation: Location? = null
    private var latestMotion = MotionSample()
    private var lastPromptTime = 0L

    private val promptCooldownMs = 20_000L
    private val highSpeedThresholdMps = 8.5f
    private val strongImpactThreshold = 18f
    private val rotationThreshold = 6.5f

    fun onMotionSample(sample: MotionSample) {
        latestMotion = sample
        onMotionUpdate(sample)

        if (
            sample.accelMagnitude >= strongImpactThreshold &&
            sample.gyroMagnitude >= 3.5f
            ) {

            maybeTrigger(
                SafetyAlert(
                    type = SafetyAlertType.FALL_OR_IMPACT,
                    title = "Possible fall or impact detected",
                    message = "A strong impact and sudden motion were detected. Do you want to share your location or confirm that you're safe?"
                )
            )
        }

        if (
            sample.gyroMagnitude >= rotationThreshold &&
            sample.accelMagnitude >= 8f
            ) {
            maybeTrigger(
                SafetyAlert(
                    type = SafetyAlertType.EXTREME_ROTATION,
                    title = "Abrupt rotational movement detected",
                    message = "SafeStride noticed an unusual movement pattern. Are you okay?"
                )
            )
        }
    }

    fun onLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val speed = when {
            location.hasSpeed() -> location.speed
            previousLocation != null -> {
                val distanceMeters = previousLocation!!.distanceTo(location)
                val timeSeconds = (location.time - previousLocation!!.time) / 1000f
                if (timeSeconds > 0f) distanceMeters / timeSeconds else 0f
            }
            else -> 0f
        }

        previousLocation = location
        onRoutePoint(latLng, max(speed, 0f))

        if (speed >= highSpeedThresholdMps) {
            maybeTrigger(
                SafetyAlert(
                    type = SafetyAlertType.HIGH_SPEED,
                    title = "Unusually high speed detected",
                    message = "SafeStride noticed movement faster than the normal walking range. Are you okay?"
                )
            )
        }
    }

    fun reset() {
        previousLocation = null
        latestMotion = MotionSample()
    }

    private fun maybeTrigger(alert: SafetyAlert) {
        val now = System.currentTimeMillis()
        if (now - lastPromptTime < promptCooldownMs) return
        lastPromptTime = now
        onSafetyAlert(alert)
    }
}