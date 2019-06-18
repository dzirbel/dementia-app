package com.dominiczirbel.dementia

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity.*

class FullscreenActivity : AppCompatActivity() {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private val shakeListener = ShakeListener(this::onShake)
    private var isShakeListenerRegistered = false

    private lateinit var backgroundAnimator: BackgroundAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        (getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager)?.runCatching {
            setLockTaskPackages(componentName, arrayOf(packageName))
        }

        backgroundAnimator = BackgroundAnimator(
            view = frameLayout,
            colorsRes = R.array.backgroundColors,
            tintRes = R.color.grey_900,
            tintRatio = 0.65f
        )
        backgroundAnimator.start()
    }

    override fun onResume() {
        super.onResume()
        isShakeListenerRegistered = accelerometer?.let { accelerometer ->
            sensorManager?.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        } == true
        backgroundAnimator.resume()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(shakeListener)
        isShakeListenerRegistered = false
        backgroundAnimator.pause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            setFullscreen()
        }
    }

    private fun onShake(shakeCount: Int) {
        if (shakeCount == 2) {
            ExitFragmentDialog().show(supportFragmentManager, ExitFragmentDialog.TAG)
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
