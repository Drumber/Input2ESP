package com.github.drumber.input2esp.backend.placeholders

object KeyCode {

    fun fromChar(character: Char) = character.toInt()

    fun fromString(text: String)= text.map { fromChar(it) }

    fun buildString(keycodes: List<Int>) = keycodes.map { it.toChar() }.joinToString("")

    val ESCAPE = 177
    val ESC = 177
    val GUI = 131
    val WINDOWS = 131
    val COMMAND = 131
    val MENU = 229
    val APP = 229
    val END = 213
    val SPACE = 32
    val BACKSPACE = 178
    val TAB = 179
    val PRINTSCREEN = 206
    val ENTER = 176
    val RETURN = 176
    val UPARROW = 218
    val DOWNARROW = 217
    val LEFTARROW = 216
    val RIGHTARROW = 215
    val UP = 218
    val DOWN = 217
    val LEFT = 216
    val RIGHT = 215
    val CAPSLOCK = 193
    val INSERT = 209
    val DELETE = 212
    val DEL = 212
    val F1 = 194
    val F2 = 195
    val F3 = 196
    val F4 = 197
    val F5 = 198
    val F6 = 199
    val F7 = 200
    val F8 = 201
    val F9 = 202
    val F10 = 203
    val F11 = 204
    val F12 = 205
    val PAGEUP = 211
    val PAGEDOWN = 214

    /**
     * Key modifiers for non-printable characters.
     *
     * https://www.arduino.cc/reference/en/language/functions/usb/keyboard/keyboardmodifiers
     */
    enum class Modifier(val code: Int) {
        ALT(130),
        SHIFT(129),
        CTRL(128),
        CONTROL(128),
        ESCAPE(177),
        ESC(177),
        GUI(131),
        WINDOWS(131),
        COMMAND(131),
        MENU(229),
        APP(229),
        END(213),
        SPACE(32),
        BACKSPACE(178),
        TAB(179),
        PRINTSCREEN(206),
        ENTER(176),
        RETURN(176),
        UPARROW(218),
        DOWNARROW(217),
        LEFTARROW(216),
        RIGHTARROW(215),
        UP(218),
        DOWN(217),
        LEFT(216),
        RIGHT(215),
        CAPSLOCK(193),
        INSERT(209),
        DELETE(212),
        DEL(212),
        F1(194),
        F2(195),
        F3(196),
        F4(197),
        F5(198),
        F6(199),
        F7(200),
        F8(201),
        F9(202),
        F10(203),
        F11(204),
        F12(205),
        PAGEUP(211),
        PAGEDOWN(214)
    }
}