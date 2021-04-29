package com.github.drumber.input2esp.backend.models

import androidx.lifecycle.LiveData
import com.github.drumber.input2esp.backend.network.NetworkClientState

abstract class DeviceModel(val id: Int, var name: String) {

    abstract fun getTypeString(): String

    abstract suspend fun connect()

    abstract fun disconnect()

    suspend fun toggleConnect() {
        when(getConnectionState().value) {
            NetworkClientState.CONNECTED,
                NetworkClientState.CONNECTING,
                NetworkClientState.MESSAGE_RECEIVED -> disconnect()
            else -> connect()
        }
    }

    abstract fun getConnectionState(): LiveData<NetworkClientState>

    abstract fun sendKeycode(payload: List<Int>)

    abstract fun printLine(payload: List<Int>)

    abstract fun print(payload: List<Int>)

}