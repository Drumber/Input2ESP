package com.github.drumber.input2esp.backend.network

import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.utils.CommonUtils

/**
 * Device discover states
 */
enum class DiscoverState {

    ONLINE {
        override fun getString() = CommonUtils.getStringResource(R.string.state_online)
    },

    OFFLINE {
        override fun getString() = CommonUtils.getStringResource(R.string.state_offline)
    };

    abstract fun getString(): String
}