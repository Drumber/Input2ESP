package com.github.drumber.input2esp.backend

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.data.Repository
import com.github.drumber.input2esp.backend.models.DeviceModel
import com.github.drumber.input2esp.backend.utils.CommonUtils
import com.github.drumber.input2esp.backend.utils.CommonUtils.notifyObserver

class DeviceManager(context: Context) {
    private var context: Context? = context

    private val devices = MutableLiveData<MutableList<DeviceModel>>(ArrayList())

    fun getDevices(): LiveData<MutableList<DeviceModel>> = devices

    init {
        loadDevices()
    }

    private fun loadDevices() {
        checkClosed()
        val deviceList = Repository.instance.fetchDevices(context!!)
        devices.value?.addAll(deviceList)
        devices.notifyObserver()
    }

    private fun storeDevices() {
        checkClosed()
        val deviceList = devices.value
        deviceList?.let { Repository.instance.storeDevices(context!!, it) }
    }

    fun addDevice(device: DeviceModel, overwriteExisting: Boolean = false) {
        if(!checkUnique(device)) {
            if(!overwriteExisting) {
                throw IllegalDeviceException(CommonUtils.getStringResource(R.string.error_device_exists))
            } else {
                // remove the existing device
                removeDeviceIntern(device.id)
            }
        }

        devices.value?.add(device)
        onDataChange()
    }

    /** Remove device without triggering data change */
    private fun removeDeviceIntern(id: Int) {
        val device = devices.value?.firstOrNull { it.id == id }
        if(device != null) {
            devices.value?.remove(device)
        }
    }

    fun removeDevice(device: DeviceModel) {
        if(devices.value?.remove(device) == true)
            onDataChange()
    }

    fun removeDevice(id: Int) {
        val device = getDevice(id)
        if(device != null) {
            if(devices.value?.remove(device) == true)
                onDataChange()
        }
    }

    fun getDevice(id: Int): DeviceModel? {
        return devices.value?.firstOrNull { it.id == id }
    }

    fun clearDevices() {
        devices.value?.clear()
        onDataChange()
    }

    private fun onDataChange() {
        notifyObservers()
        storeDevices()
    }

    fun notifyObservers() {
        devices.notifyObserver()
    }

    private fun checkClosed() {
        if(context == null)
            throw IllegalStateException("This manager was closed and cannot be re-used: context is null.")
    }

    fun close() {
        context = null
    }

    /**
     * Returns false if the id of the device model is already in use.
     */
    private fun checkUnique(device: DeviceModel): Boolean = checkUnique(device.id)

    /**
     * Returns false if the id is already in use.
     */
    private fun checkUnique(id: Int): Boolean {
        devices.value?.forEach {
            if(it.id == id) return false
        }
        return true
    }

    @Synchronized
    fun generateUniqueId(): Int {
        var id: Int
        do {
            id = (System.currentTimeMillis() / 100L).toInt()
        } while(!checkUnique(id))
        return id
    }

}

class IllegalDeviceException(message: String): Exception(message)