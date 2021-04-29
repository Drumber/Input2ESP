package com.github.drumber.input2esp.backend.network

import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.utils.CommonUtils

/**
 * State of the network client.
 */
enum class NetworkClientState {
    CONNECTED {
        override fun getString() = CommonUtils.getStringResource(R.string.state_connected)
    },

    CONNECTING {
        override fun getString() = CommonUtils.getStringResource(R.string.state_connecting)
    },

    DISCONNECTED {
        override fun getString() = CommonUtils.getStringResource(R.string.state_disconnected)
    },

    CONNECTION_LOST {
        override fun getString() = CommonUtils.getStringResource(R.string.state_connectionlost)
    },

    FAILED {
        override fun getString() = CommonUtils.getStringResource(R.string.state_failed)
        fun getThrowwale() {}
    },

    MESSAGE_RECEIVED {
        override fun getString() = CommonUtils.getStringResource(R.string.state_message_received)
    };

    abstract fun getString(): String

}