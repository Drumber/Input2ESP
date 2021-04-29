package com.github.drumber.input2esp.backend.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.drumber.input2esp.backend.network.*
import com.github.drumber.input2esp.backend.protocol.ESPloitV2
import com.github.drumber.input2esp.backend.utils.Callback
import com.github.drumber.input2esp.backend.utils.CommonUtils.getIntOr
import com.github.drumber.input2esp.backend.utils.CommonUtils.getStringOr
import com.github.drumber.input2esp.backend.utils.event.EventWrapper
import org.json.JSONObject
import java.util.*

class ESPDeviceModel(id: Int, name: String, var hostname: String, var port: Int, mode: String = Provider.TCP.toString()): DeviceModel(id, name), Discoverable, Observer, ErrorReporting {
    private val TAG = "ESPDeviceModel"

    companion object {
        const val DEFAULT_PORT = 8080
    }

    enum class Provider {
        TCP,
        ESPloitV2
    }

    private val liveClientState: MutableLiveData<NetworkClientState> by lazy { MutableLiveData(NetworkClientState.DISCONNECTED) }
    private val liveErrorReport: MutableLiveData<EventWrapper<Throwable>> by lazy { MutableLiveData() }
    private var discoverState: DiscoverState = DiscoverState.OFFLINE
    private var client: NetworkClient? = null
    private var eslpoitV2: ESPloitV2? = null

    var provider: Provider = Provider.valueOf(mode)
    set(value) {
        when(value) {
            Provider.TCP -> {
                eslpoitV2 = null
            }
            Provider.ESPloitV2 -> {
                client?.disconnect()
                client = null
            }
        }
        field = value
    }

    private fun createClient() {
        if(provider == Provider.TCP) {
            if(client != null) {
                // update client hostname and port
                client?.hostname = hostname
                client?.port = port
                return
            }
            client = NetworkClient(hostname, port)
            client?.addObserver(this)
        } else {
            if(eslpoitV2 != null) {
                eslpoitV2?.ip = "$hostname:$port"
                return
            }
            eslpoitV2 = ESPloitV2("$hostname:$port")
        }
    }

    override suspend fun connect() {
        createClient()
        client?.connect()
    }

    override fun disconnect() {
        client?.disconnect()
    }

    /** NetworkClient observer update function */
    override fun update(o: Observable?, arg: Any?) {
        val event = arg as NetworkEvent
        when (event.clientState) {
            NetworkClientState.MESSAGE_RECEIVED -> println(event.data) // TODO: handle message events
            NetworkClientState.CONNECTION_LOST,
            NetworkClientState.FAILED -> (event.data as? Throwable)?.let { liveErrorReport.postValue(EventWrapper(it)) }
        }
        liveClientState.postValue(event.clientState)
    }

    override fun getConnectionState(): LiveData<NetworkClientState> = liveClientState

    override fun getLiveErrorReport(): LiveData<EventWrapper<Throwable>> = liveErrorReport

    override fun sendKeycode(payload: List<Int>) {
        createClient()
        if(provider == Provider.TCP) {
            // TODO: not implemented
        } else {
            eslpoitV2?.let {
                val command = it.generatePressCommand(payload)
                runPayload(command)
            }
        }
    }

    override fun printLine(payload: List<Int>) {
        createClient()
        if(provider == Provider.TCP) {
            // TODO: not implemented
        } else {
            eslpoitV2?.let {
                val command = it.generatePrintLineCommand(payload)
                runPayload(command)
            }
        }
    }

    override fun print(payload: List<Int>) {
        createClient()
        if(provider == Provider.TCP) {
            // TODO: not implemented
        } else {
            eslpoitV2?.let {
                val command = it.generatePrintCommand(payload)
                runPayload(command)
            }
        }
    }

    private fun runPayload(command: String) {
        try {
            eslpoitV2?.runLivePayload(command)
        } catch (e: Exception) {
            Log.e(TAG, "Error while running payload.", e)
            liveErrorReport.postValue(EventWrapper(e))
        }
    }

    override fun getTypeString(): String = "ESP"

    override fun getDiscoverState(): DiscoverState {
        return discoverState
    }

    override suspend fun doDiscovering(callback: Callback<DiscoverState>?) {
        val online = NetworkPing.isReachable(hostname, port)
        discoverState = if(online) DiscoverState.ONLINE else DiscoverState.OFFLINE
        callback?.onCallback(discoverState)
    }

}

class ESPModelBuilder: ModelBuilder<ESPDeviceModel> {
    override fun fromJson(jsonData: JSONObject): ESPDeviceModel {
        return ESPDeviceModel(
            jsonData.getInt("id"),
            jsonData.getString("name"),
            jsonData.getStringOr("hostname", "")!!,
            jsonData.getIntOr("port", ESPDeviceModel.DEFAULT_PORT)!!,
            jsonData.getStringOr("provider", ESPDeviceModel.Provider.TCP.name)!!
        )
    }

    override fun toJson(model: DeviceModel): JSONObject {
        model as ESPDeviceModel
        return JSONObject().apply {
            put("id", model.id)
            put("name", model.name)
            put("hostname", model.hostname)
            put("port", model.port)
            put("provider", model.provider.name)
        }
    }

}