package br.com.joaovitorqueiroz.weatherapp

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import br.com.joaovitorqueiroz.weatherapp.databinding.SettingsActivityBinding
import br.com.joaovitorqueiroz.weatherapp.util.extension.isNightModeSystemDefault

private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity :
    AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private val binding: SettingsActivityBinding by lazy {
        SettingsActivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.settingsToolbar)
        binding.settingsToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment!!
        ).apply {
            arguments = args
            caller.setFragmentResultListener(requestKey = "request key") { _, _ ->
            }
            caller.setFragmentResult("request key", bundleOf("request" to "dsad"))
            /*setTargetFragment(caller, 0)*/
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {

        private var isDarkTheme: Boolean? = null
        private var isDark: SwitchPreferenceCompat? = null
        private lateinit var preferenceManager: SharedPreferences

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)
            isDark = findPreference(IS_DARK_THEME_PREFERENCE_NAME)
            preferenceManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val isNightMode = requireContext().isNightModeSystemDefault()
            isDarkTheme = preferenceManager.getBoolean(IS_DARK_THEME_PREFERENCE_NAME, isNightMode)
            setDarkTheme(isDarkTheme!!)
            setListeners()
        }

        private fun setListeners() {
            isDark?.setOnPreferenceChangeListener { _, newValue ->
                isDarkTheme = newValue as Boolean
                isDarkTheme?.let { safeFlag ->
                    preferenceManager.edit {
                        putBoolean(IS_DARK_THEME_PREFERENCE_NAME, safeFlag)
                    }
                    setDarkTheme(safeFlag)
                    return@setOnPreferenceChangeListener true
                }
                false
            }
        }

        private fun setDarkTheme(
            flag: Boolean
        ) {
            if (flag) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
        }

        companion object {
            const val IS_DARK_THEME_PREFERENCE_NAME = "is_dark_theme"
        }
    }

    class MessagesFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.messages_preferences, rootKey)
        }
    }

    class SyncFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey)
        }
    }
}
