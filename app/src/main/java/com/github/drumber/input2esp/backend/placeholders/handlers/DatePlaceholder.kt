package com.github.drumber.input2esp.backend.placeholders.handlers

import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.placeholders.PlaceholderHandler
import com.github.drumber.input2esp.backend.placeholders.Processor
import com.github.drumber.input2esp.backend.utils.CommonUtils
import java.text.DateFormat
import java.util.*

class DatePlaceholder: PlaceholderHandler() {

    val DATE = "date"
    val TIME = "time"

    override fun processPlaceholder(placeholder: String, range: IntRange, text: String, processor: Processor) {
        when(placeholder) {
            DATE -> DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(Date()).let { processor.append(it) }
            TIME -> DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(Date()).let { processor.append(it) }
        }
    }

    override fun getCategory(): String {
        return CommonUtils.getStringResource(R.string.placeholder_category_date)
    }

}