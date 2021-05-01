package com.github.drumber.input2esp.backend.models

import com.github.drumber.input2esp.backend.data.Preferences

data class Payload(var type: CommandType, var payload: String, var delay: Int = Preferences.defaultCommandDelay.toInt())

enum class CommandType {
    PrintLine,
    Print,
    Press
}
