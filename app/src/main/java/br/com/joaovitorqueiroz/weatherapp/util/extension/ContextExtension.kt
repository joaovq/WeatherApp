package br.com.joaovitorqueiroz.weatherapp.util.extension

import android.content.Context
import android.content.res.Configuration
import android.os.Build

fun Context.isNightModeSystemDefault() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.resources?.configuration?.isNightModeActive ?: false
    } else {
        when (this.resources?.configuration?.uiMode) {
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> true
        }
    }