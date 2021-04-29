package com.github.drumber.input2esp.backend.utils

import com.github.drumber.input2esp.backend.models.DeviceModel
import com.github.drumber.input2esp.backend.utils.DeviceUtils.getConnectionStateForModel
import com.github.drumber.input2esp.backend.utils.DeviceUtils.getDescriptionForModel
import com.github.drumber.input2esp.backend.utils.DeviceUtils.getNameForModel
import com.github.drumber.input2esp.ui.components.DeviceItem

object ConversionUtil {

    fun deviceModelToDeviceItem(model: DeviceModel): DeviceItem {
        return DeviceItem(model.id,
            getNameForModel(model),
            model.getTypeString(),
            getDescriptionForModel(model),
            getConnectionStateForModel(model),
            getConnectionStateForModel(model)?.getString()
        )
    }



}