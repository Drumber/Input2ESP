package com.github.drumber.input2esp.backend.data

import android.content.Context
import android.util.Log
import com.github.drumber.input2esp.backend.models.DeviceModel
import com.github.drumber.input2esp.backend.Files
import com.github.drumber.input2esp.backend.models.ModelFactory
import org.json.JSONArray
import org.json.JSONObject

class Repository {
    private val TAG = "Repository"

    companion object {
        val instance: Repository by lazy { Repository() }
    }

    val modelFactory: ModelFactory by lazy { ModelFactory() }

    /*
        {
            "devices": [
                {
                    "classpath": "com.github.com.input2eso.backend.models.ESPDeviceModel"
                    "data": {
                        <model specific data here>
                    }
                },
                {
                ...
                }
            ]
        }
     */
    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun fetchDevices(context: Context): List<DeviceModel> {
        Log.d(TAG, "Loading devices from ${Files.DEVICE_STORAGE}...")
        val rawData = FileStorage.loadData(context, Files.DEVICE_STORAGE)

        val modelList = ArrayList<DeviceModel>()
        if(rawData == null) {
            Log.w(TAG, "No data available! Returning empty list.")
            return modelList
        }

        val jsonRoot = JSONObject(rawData)
        val jsonArray = jsonRoot.getJSONArray("devices")

        // parse device list from JSON array
        for (i in 0 until jsonArray.length()) {
            val deviceObject = jsonArray.getJSONObject(i)

            val classpath = deviceObject.getString("classpath")
            val clazz = Class.forName(classpath)

            if(DeviceModel::class.java.isAssignableFrom(clazz)) {
                val data = deviceObject.getJSONObject("data")
                // create instance of the model and add it to the list
                val model = modelFactory.buildModel(clazz as Class<out DeviceModel>, data)
                model?.let { modelList.add(it) }
            } else {
                Log.e(TAG, "The model class $clazz must be child of DeviceModel.")
            }
        }
        Log.d(TAG, "Loaded ${modelList.size} devices.")
        return modelList
    }

    @Synchronized
    fun storeDevices(context: Context, modelList: List<DeviceModel>) {
        Log.d(TAG, "Storing ${modelList.size} devices in ${Files.DEVICE_STORAGE}...")
        val jsonRoot = JSONObject()
        val jsonArray = JSONArray()

        for (model in modelList) {
            val data = modelFactory.serializeModel(model)
            if(data != null) {
                val jsonObj = JSONObject()
                jsonObj.apply {
                    put("classpath", model::class.java.canonicalName)
                    put("data", data)
                }
                // add to array
                jsonArray.put(jsonObj)
            } else {
                Log.w(TAG, "Could not serialize object of type ${model::class.java.canonicalName}: $model")
            }
        }

        jsonRoot.put("devices", jsonArray)
        FileStorage.storeData(context, Files.DEVICE_STORAGE, jsonRoot.toString())
        Log.d(TAG, "Done storing!")
    }

}