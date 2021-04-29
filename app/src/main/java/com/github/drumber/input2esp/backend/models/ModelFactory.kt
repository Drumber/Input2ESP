package com.github.drumber.input2esp.backend.models

import org.json.JSONObject

class ModelFactory {

    private val builders = HashMap<Class<out DeviceModel>, ModelBuilder<out DeviceModel>>()

    init {
        registerDefaultBuilder()
    }

    fun registerBuilder(clazz: Class<out DeviceModel>, builder: ModelBuilder<out DeviceModel>) {
        builders[clazz] = builder
    }

    fun unregisterBuilder(clazz: Class<out DeviceModel>) {
        builders.remove(clazz)
    }

    fun buildModel(clazz: Class<out DeviceModel>, data: JSONObject): DeviceModel? {
        return builders[clazz]?.fromJson(data)
    }

    fun serializeModel(model: DeviceModel): JSONObject? {
        val builder = builders[model::class.java]
        return builder?.toJson(model)
    }

    private fun registerDefaultBuilder() {
        registerBuilder(ESPDeviceModel::class.java, ESPModelBuilder())
    }

}