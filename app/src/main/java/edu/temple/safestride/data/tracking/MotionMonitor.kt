package edu.temple.safestride.data.tracking

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import edu.temple.safestride.model.MotionSample
import kotlin.math.abs
import kotlin.math.sqrt

class MotionMonitor(
    context: Context,
    private val onMotionChanged: (MotionSample) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var latestAccel = 0f
    private var latestGyro = 0f

    fun hasAccelerometer(): Boolean = accelerometer != null
    fun hasGyroscope(): Boolean = gyroscope != null
    fun hasRequiredSensors(): Boolean = accelerometer != null && gyroscope != null

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                10_000,
                0
            )
        }

        gyroscope?.let {
            sensorManager.registerListener(
                this,
                it,
                10_000,
                0
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)
                latestAccel = abs(magnitude - SensorManager.GRAVITY_EARTH)
            }

            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                latestGyro = sqrt(x * x + y * y + z * z)
            }
        }

        onMotionChanged(
            MotionSample(
                accelMagnitude = latestAccel,
                gyroMagnitude = latestGyro
            )
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}