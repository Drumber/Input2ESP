package com.github.drumber.input2esp.ui.settings

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.github.drumber.input2esp.R

class SettingsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.pref_file_key)
        setPreferencesFromResource(R.xml.settings, rootKey)

        findPreference<ListPreference>(getString(R.string.prefkey_theme))?.setOnPreferenceChangeListener { preference, newValue ->
            (newValue as? String)?.toIntOrNull()?.let {
                AppCompatDelegate.setDefaultNightMode(it)
            }
            true
        }

        // accept only number values for 'default command delay' preference
        findPreference<EditTextPreference>(getString(R.string.prefkey_default_command_delay))?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }
}