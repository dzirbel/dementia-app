package com.dominiczirbel.dementia

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

// TODO add dialog timeout
class ExitFragmentDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
            .setTitle(R.string.exitDialog_title)
            .setMessage(R.string.exitDialog_message)
            .setPositiveButton(R.string.exitDialog_positive) { _, _ ->
                activity?.apply {
                    stopLockTask()
                    finish()
                }
            }
            .setNegativeButton(R.string.exitDialog_negative) { _, _ ->
                // no-op
            }
            .create()
    }
}
