package br.com.joaovitorqueiroz.weatherapp.util.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPrefs(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE)

    var isDark: Boolean
        get() = sharedPreferences.getBoolean(IS_DARK_PREFERENCE, false)
        set(value) {
            sharedPreferences.edit().putBoolean(IS_DARK_PREFERENCE, value).apply()
        }

    companion object {
        const val USER_PREFERENCES_NAME = "user preferences"
        const val IS_DARK_PREFERENCE = "is_dark_theme"
    }
}
