package com.dominiczirbel.dementia

import android.app.ActivityManager
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
import androidx.core.content.getSystemService
import kotlinx.android.synthetic.main.fullscreen_activity.*

class FullscreenActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val activityManager by lazy { getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager }
    private val sensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as? SensorManager }

    private var accelerometer: Sensor? = null

    private val shakeListener = ShakeListener(this::onShake)
    private var isShakeListenerRegistered = false

    private lateinit var backgroundAnimator: BackgroundAnimator

    private var showingMainMenu = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fullscreen_activity)

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
            toggleMainMenu(false)
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

    override fun onBackPressed() {
        if (!showingMainMenu && !isInLockTaskMode()) {
            toggleMainMenu(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "animateBackground") {
            backgroundAnimator.toggleAnimation(sharedPreferences.getBoolean("animateBackground", true))
        }
    }

    private fun onShake(shakeCount: Int) {
        if (shakeCount == 2) {
            ExitFragmentDialog { toggleMainMenu(true) }.show(supportFragmentManager, ExitFragmentDialog.TAG)
        }
    }

    private fun toggleMainMenu(showMainMenu: Boolean) {
        if (showingMainMenu != showMainMenu) {
            showingMainMenu = showMainMenu

            if (showMainMenu) {
                mainMenu.visibility = View.VISIBLE
                frameLayout.systemUiVisibility = NON_FULLSCREEN_FLAGS

                stopLockTask()
            } else {
                mainMenu.visibility = View.GONE
                frameLayout.systemUiVisibility = FULLSCREEN_FLAGS

                if (isShakeListenerRegistered) {
                    startLockTask()
                }
            }
        }
    }

    private fun isInLockTaskMode(): Boolean {
        return activityManager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
    }

    companion object {

        const val NON_FULLSCREEN_FLAGS = View.SYSTEM_UI_FLAG_VISIBLE

        const val FULLSCREEN_FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}
