package com.github.drumber.input2esp.backend.models

data class Payload(var type: CommandType, var payload: String)

enum class CommandType {
    PrintLine,
    Print,
    Press
}
