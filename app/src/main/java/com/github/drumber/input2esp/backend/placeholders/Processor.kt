package com.github.drumber.input2esp.backend.placeholders

class Processor {

    val keycodes = mutableListOf<Int>()

    fun append(text: String) {
        val codes = KeyCode.fromString(text)
        keycodes.addAll(codes)
    }

    fun append(char: Char) = keycodes.add(KeyCode.fromChar(char))

    fun append(char: Int) = keycodes.add(char)

}