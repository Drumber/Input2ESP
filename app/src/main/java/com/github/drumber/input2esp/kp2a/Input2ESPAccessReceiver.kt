package com.github.drumber.input2esp.kp2a

import keepass2android.pluginsdk.Strings
import java.util.ArrayList

class Input2ESPAccessReceiver: keepass2android.pluginsdk.PluginAccessBroadcastReceiver() {

    override fun getScopes(): ArrayList<String> {
        return arrayListOf(Strings.SCOPE_DATABASE_ACTIONS, Strings.SCOPE_CURRENT_ENTRY)
    }

}