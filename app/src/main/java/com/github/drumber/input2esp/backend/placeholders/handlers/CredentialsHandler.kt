package com.github.drumber.input2esp.backend.placeholders.handlers

import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.placeholders.PlaceholderHandler
import com.github.drumber.input2esp.backend.placeholders.PlaceholderManager
import com.github.drumber.input2esp.backend.placeholders.Processor
import com.github.drumber.input2esp.backend.utils.CommonUtils
import com.github.drumber.input2esp.kp2a.CredentialsData
import keepass2android.pluginsdk.KeepassDefs

class CredentialsHandler(private val credentials: CredentialsData): PlaceholderHandler() {

    companion object {
        val USER_NAME = "username"
        val URL = "url"
        val PASSWORD = "password"
    }

    override fun processPlaceholder(placeholder: String, range: IntRange, text: String, processor: Processor) {
        when(placeholder) {
            USER_NAME -> credentials.entries[KeepassDefs.UserNameField]?.let { processor.append(it) }
            URL -> credentials.entries[KeepassDefs.UrlField]?.let { processor.append(it) }
            PASSWORD -> credentials.entries[KeepassDefs.PasswordField]?.let { processor.append(it) }
        }
    }

    fun register(manager: PlaceholderManager) {
        manager.registerPlaceholder(USER_NAME, this)
        manager.registerPlaceholder(URL, this)
        manager.registerPlaceholder(PASSWORD, this)
    }

    override fun getCategory(): String? {
        return CommonUtils.getStringResource(R.string.placeholder_category_kp2a)
    }

    override fun getPriority(): Int = PRIORITY_HIGH
}