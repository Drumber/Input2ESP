package com.github.drumber.input2esp.backend.protocol

import android.util.Log
import com.github.drumber.input2esp.backend.models.CommandType
import com.github.drumber.input2esp.backend.models.DeviceModel
import com.github.drumber.input2esp.backend.models.Payload
import com.github.drumber.input2esp.backend.placeholders.PlaceholderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class PayloadSender(private val device: DeviceModel, private val placeholderManager: PlaceholderManager) {
    private val TAG = "PayloadSender"

    @Throws(EmptyPayloadException::class)
    suspend fun sendPayload(payload: Payload) {
        if(payload.payload.isBlank()) throw EmptyPayloadException()

        val delay = payload.delay
        if(delay > 0) {
            Log.d(TAG, "[Delay] Waiting $delay ms...")
            delay(delay.toLong())
        }
        val keycodes = placeholderManager.processText(payload.payload)
        //Log.d(TAG, "Sending Payload [${payload.type}]: ${KeyCode.buildString(keycodes)}") // only for debugging! Could show user login data in logcat
        Log.d(TAG, "Sending Payload [${payload.type}]")
        withContext(Dispatchers.IO) {
            when(payload.type) {
                CommandType.Press -> device.sendKeycode(keycodes)
                CommandType.Print -> device.print(keycodes)
                CommandType.PrintLine -> device.printLine(keycodes)
            }
        }
    }

    suspend fun sendPayloadList(payloadList: MutableList<Payload>) {
        payloadList.forEach {
            try {
                sendPayload(it)
            } catch (e: EmptyPayloadException) {
                Log.w(TAG, "Skipping empty payload command...")
            }
        }
    }

    public class EmptyPayloadException: Exception()

}