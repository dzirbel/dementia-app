package com.dominiczirbel.dementia

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * A [SensorEventListener] which should be registered to an [Sensor.TYPE_ACCELEROMETER] and invokes the given [callback]
 * when the accelerometer reports acceleration that appears to be a shake.
 *
 * [callback] is invoked under the following conditions:
 * - the [SensorEvent] reports acceleration sufficiently larger than normal; see [G_FORCE_THRESHOLD]
 * - events are debounced so that temporally close are only reported once; see [DEBOUNCE_MS]
 * - the [callback] is given a single argument corresponding to the number of times the device has been shaken within a
 *   single timeframe, this counter starts at 1 and increases by exactly one on each successive call unless sufficient
 *   time has passed; see [RESET_COUNT_MS]
 */
class ShakeListener(private val callback: (Int) -> Unit) : SensorEventListener {

    private var lastShake: Long? = null
    private var shakeCount: Int = 1

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no-op
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = event.values[0] / SensorManager.GRAVITY_EARTH
            val y = event.values[1] / SensorManager.GRAVITY_EARTH
            val z = event.values[2] / SensorManager.GRAVITY_EARTH

            val g = sqrt(x * x + y * y + z * z)

            if (g > G_FORCE_THRESHOLD) {
                val now = System.currentTimeMillis()
                val sinceLastShake = lastShake?.let { now - it }
                if (sinceLastShake == null || sinceLastShake > DEBOUNCE_MS) {
                    lastShake = now

                    if (sinceLastShake == null || sinceLastShake > RESET_COUNT_MS) {
                        shakeCount = 1
                    } else {
                        shakeCount++
                    }

                    callback.invoke(shakeCount)
                }
            }
        }
    }

    companion object {

        /**
         * Minimum accelerometer magnitude to register as a shake event, as a multiplier of Earth's gravity.
         *
         * That is, the acceleration vector reported by the accelerometer is normalized by [SensorManager.GRAVITY_EARTH]
         * and the magnitude of the normalized vector must exceed this value to count as the device being shaken.
         */
        private const val G_FORCE_THRESHOLD = 2.5

        /**
         * The minimum time in milliseconds between events exceeding the [G_FORCE_THRESHOLD] that count as shake events,
         * e.g. increment the [shakeCount] and invoke the [callback].
         */
        private const val DEBOUNCE_MS = 200

        /**
         * The minimum time in milliseconds between events exceeding the [G_FORCE_THRESHOLD] that count as new shake
         * events, e.g. reset the [shakeCount].
         */
        private const val RESET_COUNT_MS = 1_000
    }
}
