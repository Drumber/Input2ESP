package com.github.drumber.input2esp.backend.data

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import com.chibatching.kotpref.KotprefModel
import com.github.drumber.input2esp.R

object Preferences: KotprefModel() {

    override val commitAllPropertiesByDefault: Boolean = true
    override val kotprefName: String = getString(R.string.pref_file_key)

    var themeMode by stringPref(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString(), getString(R.string.prefkey_theme))
    var autoDiscovery by booleanPref(true, getString(R.string.prefkey_auto_discovery))



    /**
     * Get string from resource file
     */
    private fun getString(@StringRes stringRes: Int) = context.getString(stringRes)

    private fun getStringArray(stringArrRes: Int) = context.resources.getStringArray(stringArrRes)

}