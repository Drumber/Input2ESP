package com.github.drumber.input2esp.backend.utils

import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.utils.CommonUtils.getStringResource
import java.net.ConnectException
import java.net.SocketTimeoutException

object ExceptionUtil {

    /**
     * Translate common network IO exceptions to a more user friendly string.
     */
    fun translateNetworkException(e: Throwable): String {
        return when(e) {
            is SocketTimeoutException -> getStringResource(R.string.error_network_connection_timeout)
            is IllegalArgumentException -> getStringResource(R.string.error_network_unsupported_address)
            is ConnectException -> getStringResource(R.string.error_network_cannot_connect)
            else -> getStringResource(R.string.error_network_regular_error)
        }
    }

}