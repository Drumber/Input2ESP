package com.github.drumber.input2esp.backend.placeholders.handlers

import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.placeholders.KeyCode
import com.github.drumber.input2esp.backend.placeholders.PlaceholderHandler
import com.github.drumber.input2esp.backend.placeholders.PlaceholderManager
import com.github.drumber.input2esp.backend.placeholders.Processor
import com.github.drumber.input2esp.backend.utils.CommonUtils

/**
 * Provides placeholders for common keyboard modifier keys.
 *
 * See https://www.arduino.cc/reference/en/language/functions/usb/keyboard/keyboardmodifiers/ for a
 * list of modifier keys that are supported by the arduino.
 */
class ModifierHandler: PlaceholderHandler() {

    override fun processPlaceholder(placeholder: String, range: IntRange, text: String, processor: Processor) {
        KeyCode.Modifier.values().firstOrNull { it.name.equals(placeholder, true) }?.let {
            processor.append(it.code)
        }
    }

    fun register(manager: PlaceholderManager) {
        KeyCode.Modifier.values().forEach {
            manager.registerPlaceholder(it.name, this)
        }
    }

    override fun getCategory(): String? {
        return CommonUtils.getStringResource(R.string.placeholder_category_keys)
    }

    override fun getPriority(): Int = PRIORITY_LOW
}