package com.github.drumber.input2esp.backend.network

data class NetworkEvent(val clientState: NetworkClientState, val data: Any? = null)