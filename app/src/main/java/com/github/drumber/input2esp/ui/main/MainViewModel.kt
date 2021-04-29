package com.github.drumber.input2esp.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.github.drumber.input2esp.Input2EspApplication
import com.github.drumber.input2esp.backend.DeviceManager
import com.github.drumber.input2esp.backend.models.Discoverable
import com.github.drumber.input2esp.backend.utils.ConversionUtil
import com.github.drumber.input2esp.ui.components.DeviceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager: DeviceManager by lazy { (application as Input2EspApplication).deviceManager }
    private val isDiscovering: MutableLiveData<Boolean> by lazy { MutableLiveData(false) }
    fun isDiscovering(): LiveData<Boolean> = isDiscovering

    fun getDeviceList(): LiveData<List<DeviceItem>> {
        return Transformations.switchMap(deviceManager.getDevices()) { modelList ->
            // map DeviceModel to DeviceItem and reverse the order (=> latest saved devices on top)
            MutableLiveData(modelList.map { x -> ConversionUtil.deviceModelToDeviceItem(x)}.asReversed())
        }
    }

    fun clearDeviceList() {
        deviceManager.clearDevices()
    }

    fun deleteDevice(deviceId: Int) {
        deviceManager.removeDevice(deviceId)
    }

    fun discoverDevices() {
        if(isDiscovering.value == true) return // already discovering

        Log.d("MainViewModel", "Started device discovering...")
        viewModelScope.launch(Dispatchers.IO) {
            isDiscovering.postValue(true)
            deviceManager.getDevices().value?.forEach {
                if(it is Discoverable) {
                    it.doDiscovering {
                        deviceManager.notifyObservers()
                    }
                }
            }
            isDiscovering.postValue(false)
            Log.d("MainViewModel", "Finished device discovering.")
        }
    }

}