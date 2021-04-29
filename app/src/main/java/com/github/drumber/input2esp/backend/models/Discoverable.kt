package com.github.drumber.input2esp.backend.models

import com.github.drumber.input2esp.backend.network.DiscoverState
import com.github.drumber.input2esp.backend.utils.Callback

interface Discoverable {

    fun getDiscoverState(): DiscoverState

    suspend fun doDiscovering(callback: Callback<DiscoverState>? = null)

}