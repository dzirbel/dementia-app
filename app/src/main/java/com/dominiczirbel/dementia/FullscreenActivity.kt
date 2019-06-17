package com.dominiczirbel.dementia

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity.*

class FullscreenActivity : AppCompatActivity() {

    private var devicePolicyManager: DevicePolicyManager? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private val shakeListener = ShakeListener(this::onShake)
    private var isShakeListenerRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        try {
            devicePolicyManager?.setLockTaskPackages(componentName, arrayOf(packageName))
        } catch (_: SecurityException) {
            Log.w("MyTag", "Unable to set lock task package")
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { accelerometer ->
            sensorManager
                ?.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
                ?.let { success ->
                    isShakeListenerRegistered = success
                }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(shakeListener)
        isShakeListenerRegistered = false
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            setFullscreen()
        }
    }

    private fun onShake(shakeCount: Int) {
        if (shakeCount == 2) {
            ExitFragmentDialog().show(supportFragmentManager, "exit")
        }
    }

    private fun setFullscreen() {
        frameLayout.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        if (isShakeListenerRegistered) {
            startLockTask()
        }
    }
}
