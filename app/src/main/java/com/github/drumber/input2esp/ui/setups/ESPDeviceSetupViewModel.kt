package com.github.drumber.input2esp.ui.setups

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.github.drumber.input2esp.Input2EspApplication
import com.github.drumber.input2esp.backend.DeviceManager
import com.github.drumber.input2esp.backend.models.ESPDeviceModel

class ESPDeviceSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager: DeviceManager by lazy { (application as Input2EspApplication).deviceManager }
    private var deviceModel: ESPDeviceModel? = null
    private var isDeviceModelSet = false

    fun setDeviceModel(deviceModelId: Int?) {
        if(isDeviceModelSet) return
        val device = deviceModelId?.let { deviceManager.getDevice(deviceModelId) }
        if(device is ESPDeviceModel) {
            this.deviceModel = device
        } else {
            this.deviceModel = ESPDeviceModel(deviceManager.generateUniqueId(), "", "", ESPDeviceModel.DEFAULT_PORT)
        }
        isDeviceModelSet = true

        deviceModel?.let {
            name = it.name
            hostname = it.hostname
            port = it.port
            protocol = it.provider
        }
    }

    var name: String? = null
    var hostname: String? = null
    var port: Int? = null
    var protocol: ESPDeviceModel.Provider? = null

    fun saveDeviceModel() {
        deviceModel?.let {
            it.name = name?.trim() ?: "ESP Device"
            it.hostname = hostname?.trim() ?: ""
            it.port = port ?: ESPDeviceModel.DEFAULT_PORT
            it.provider = protocol ?: ESPDeviceModel.Provider.TCP
            deviceManager.addDevice(it, true)
        }
    }

    /**
     * Delete the device in the device manager.
     * Note: The fragment should not be used after calling this,
     * because the deviceModel field will be set to null!
     */
    fun deleteDeviceModel() {
        deviceModel?.let { deviceManager.removeDevice(it) }
        deviceModel = null
    }

    fun isDeviceEdited(): Boolean {
        return !(deviceModel?.name.equals(name) &&
                deviceModel?.hostname.equals(hostname) &&
                deviceModel?.port == port &&
                deviceModel?.provider == protocol)
    }

    override fun onCleared() {
        super.onCleared()
        deviceModel = null
    }
}