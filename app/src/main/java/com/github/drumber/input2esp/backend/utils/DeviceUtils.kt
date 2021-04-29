package com.github.drumber.input2esp.backend.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.models.DeviceModel
import com.github.drumber.input2esp.backend.models.Discoverable
import com.github.drumber.input2esp.backend.models.ESPDeviceModel
import com.github.drumber.input2esp.backend.network.DiscoverState
import com.github.drumber.input2esp.backend.network.NetworkClientState

object DeviceUtils {

    fun getColorForDiscoverState(context: Context, state: DiscoverState?): Int {
        return when(state) {
            DiscoverState.ONLINE -> ContextCompat.getColor(context, R.color.state_online)
            DiscoverState.OFFLINE -> ContextCompat.getColor(context, R.color.state_offline)
            else -> ContextCompat.getColor(context, R.color.state_offline)
        }
    }

    fun getColorForClientState(context: Context, state: NetworkClientState): Int {
        return when(state) {
            NetworkClientState.CONNECTED,
            NetworkClientState.MESSAGE_RECEIVED -> ContextCompat.getColor(context, R.color.state_connected)
            NetworkClientState.CONNECTION_LOST,
            NetworkClientState.CONNECTING -> ContextCompat.getColor(context, R.color.state_neutral)
            NetworkClientState.DISCONNECTED,
            NetworkClientState.FAILED -> ContextCompat.getColor(context, R.color.state_failed)
            else -> ContextCompat.getColor(context, R.color.state_offline)
        }
    }

    fun getNameForModel(model: DeviceModel): String {
        return if(model.name.isBlank()) {
            CommonUtils.getStringResource(R.string.error_unnamed_device)
        } else {
            model.name
        }
    }

    fun getDescriptionForModel(model: DeviceModel): String = when(model) {
        is ESPDeviceModel -> model.let {
            "${it.hostname}:${it.port}"
        }
        else -> ""
    }

    fun getConnectionStateForModel(model: DeviceModel): DiscoverState? {
        return if(model is Discoverable
                && model.getConnectionState().value != NetworkClientState.CONNECTED
                && model.getConnectionState().value != NetworkClientState.CONNECTION_LOST
        ) {
            model.getDiscoverState()
        } else {
            when(model.getConnectionState().value) {
                NetworkClientState.CONNECTED,
                NetworkClientState.MESSAGE_RECEIVED -> DiscoverState.ONLINE
                NetworkClientState.DISCONNECTED,
                NetworkClientState.CONNECTING,
                NetworkClientState.CONNECTION_LOST -> DiscoverState.OFFLINE
                NetworkClientState.FAILED -> DiscoverState.OFFLINE
                else -> null
            }
        }
    }

}