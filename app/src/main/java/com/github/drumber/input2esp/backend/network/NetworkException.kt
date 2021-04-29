package com.github.drumber.input2esp.backend.network

import androidx.annotation.StringRes
import com.github.drumber.input2esp.backend.utils.CommonUtils

class NetworkException: Exception {

    override val message: String

    constructor(@StringRes stringResource: Int) {
        message = CommonUtils.getStringResource(stringResource)
    }

    constructor(message: String) {
        this.message = message
    }

}