package com.github.drumber.input2esp.kp2a

import android.content.Intent
import keepass2android.pluginsdk.Kp2aControl
import keepass2android.pluginsdk.Strings

class CredentialsData {

    lateinit var entryId: String
        private set
    var fieldId: String? = null
        private set
    lateinit var entries: HashMap<String, String>
        private set

    fun parseData(intent: Intent) {
        if(intent.hasExtra(Strings.EXTRA_ENTRY_ID)) {
            entryId = intent.getStringExtra(Strings.EXTRA_ENTRY_ID).orEmpty()
            fieldId = intent.getStringExtra(Strings.EXTRA_FIELD_ID)?.substringAfter(Strings.PREFIX_STRING)
            entries = Kp2aControl.getEntryFieldsFromIntent(intent)
        }
    }

    companion object {
        fun fromIntent(intent: Intent): CredentialsData = CredentialsData().apply { parseData(intent) }
    }

}