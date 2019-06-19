package com.dominiczirbel.dementia

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.DialogFragment
import java.util.concurrent.TimeUnit

class ExitFragmentDialog(private val onExit: () -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.exitDialog_title)
            .setMessage(getDialogMessage(AUTO_DISMISS_SECONDS))
            .setPositiveButton(R.string.exitDialog_positive) { _, _ ->
                onExit()
            }
            .setNegativeButton(R.string.exitDialog_negative) { _, _ ->
                // no-op
            }
            .create()

        val timer = object : CountDownTimer(
            TimeUnit.SECONDS.toMillis(AUTO_DISMISS_SECONDS.toLong()),
            TimeUnit.SECONDS.toMillis(1)
        ) {
            override fun onTick(millisUntilFinished: Long) {
                if (dialog.isShowing) {
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()
                    dialog.setMessage(getDialogMessage(seconds))
                } else {
                    cancel()
                }
            }

            override fun onFinish() {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        }

        timer.start()

        return dialog
    }

    private fun getDialogMessage(secondsRemaining: Int): String {
        return resources.getQuantityString(R.plurals.exitDialog_message, secondsRemaining, secondsRemaining)
    }

    companion object {

        const val TAG = "exit"

        const val AUTO_DISMISS_SECONDS = 10
    }
}
