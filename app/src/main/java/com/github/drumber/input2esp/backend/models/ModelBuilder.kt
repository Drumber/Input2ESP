package com.github.drumber.input2esp.backend.models

import org.json.JSONObject

interface ModelBuilder<T: DeviceModel> {

    fun fromJson(jsonData: JSONObject): T

    fun toJson(model: DeviceModel): JSONObject

}