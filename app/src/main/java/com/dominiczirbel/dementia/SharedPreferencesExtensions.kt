package com.dominiczirbel.dementia

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.StringRes

fun SharedPreferences.getBoolean(@StringRes keyRes: Int, resources: Resources, defaultValue: Boolean = false): Boolean {
    return getBoolean(resources.getString(keyRes), defaultValue)
}
