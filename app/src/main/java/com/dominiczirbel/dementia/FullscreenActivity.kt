package com.dominiczirbel.dementia

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fullscreen_activity.*

class FullscreenActivity : AppCompatActivity() {

    private val activityManager by lazy { getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager }
    private val sensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val shakeListener = ShakeListener(this::onShake)

    private lateinit var backgroundAnimator: BackgroundAnimator

    private var showingMainMenu = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fullscreen_activity)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        (getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager)?.runCatching {
            setLockTaskPackages(componentName, arrayOf(packageName))
        }

        backgroundAnimator = BackgroundAnimator(
            view = frameLayout,
            colorsRes = R.array.backgroundColors,
            tintRes = R.color.grey_900,
            tintRatio = 0.65f
        )

        startButton.setOnClickListener {
            toggleMainMenu(false)
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.let { shakeListener.register(it) }
        backgroundAnimator.resume()

        applySystemUiVisibility()
        applySettings()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.let { shakeListener.unregister(it) }
        backgroundAnimator.pause()
    }

    override fun onBackPressed() {
        if (!showingMainMenu && !isInLockTaskMode()) {
            toggleMainMenu(true)
        } else {
            super.onBackPressed()
        }
    }

    private fun onShake(shakeCount: Int) {
        if (shakeCount == 2 && isInLockTaskMode()) {
            ExitFragmentDialog { toggleMainMenu(true) }.show(supportFragmentManager, ExitFragmentDialog.TAG)
        }
    }

    private fun toggleMainMenu(showMainMenu: Boolean) {
        if (showingMainMenu != showMainMenu) {
            showingMainMenu = showMainMenu

            mainMenu.visibility = if (showMainMenu) View.VISIBLE else View.GONE
            applySystemUiVisibility()
            applyLockTaskState()
        }
    }

    private fun applySettings() {
        backgroundAnimator.toggleAnimation(sharedPreferences.getBoolean(R.string.pref_animateBackground_key, resources))
        applyLockTaskState()
    }

    private fun applyLockTaskState() {
        if (!showingMainMenu &&
            sharedPreferences.getBoolean(R.string.pref_lockTask_key, resources) &&
            shakeListener.isRegistered
        ) {
            startLockTask()
        } else {
            stopLockTask()
        }
    }

    private fun applySystemUiVisibility() {
        frameLayout.systemUiVisibility = if (showingMainMenu) NON_FULLSCREEN_FLAGS else FULLSCREEN_FLAGS
    }

    private fun isInLockTaskMode(): Boolean {
        return activityManager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
    }

    companion object {

        private const val NON_FULLSCREEN_FLAGS = View.SYSTEM_UI_FLAG_VISIBLE

        private const val FULLSCREEN_FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}
