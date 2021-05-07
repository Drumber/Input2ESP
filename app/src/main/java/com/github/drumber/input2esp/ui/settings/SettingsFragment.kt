package com.github.drumber.input2esp.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
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
        findPreference<EditTextPreference>(getString(R.string.prefkey_default_command_delay))?.apply {
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_NUMBER
            }
            // show current delay in summary
            setSummaryProvider {
                "${(it as EditTextPreference).text} ms"
            }
        }

        val appVersion = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        findPreference<Preference>(getString(R.string.prefkey_app_version_info))?.summary = appVersion.versionName

        findPreference<Preference>(getString(R.string.prefkey_app_repo_info))?.setOnPreferenceClickListener {
            // open repository url in browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_repo_url)))
            startActivity(intent)
            true
        }
    }
}