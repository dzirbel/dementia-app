package com.dominiczirbel.dementia

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fullscreen_activity.*

class FullscreenActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private val shakeListener = ShakeListener(this::onShake)
    private var isShakeListenerRegistered = false

    private lateinit var backgroundAnimator: BackgroundAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fullscreen_activity)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        (getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager)?.runCatching {
            setLockTaskPackages(componentName, arrayOf(packageName))
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        backgroundAnimator = BackgroundAnimator(
            view = frameLayout,
            colorsRes = R.array.backgroundColors,
            tintRes = R.color.grey_900,
            tintRatio = 0.65f
        )

        // TODO dedup this code with the listener
        backgroundAnimator.toggleAnimation(sharedPreferences.getBoolean("animateBackground", true))

        startButton.setOnClickListener {
            setFullscreen()
            mainMenu.visibility = View.GONE
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "animateBackground") {
            backgroundAnimator.toggleAnimation(sharedPreferences.getBoolean("animateBackground", true))
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
