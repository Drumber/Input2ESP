package com.github.drumber.input2esp.backend.protocol

import android.util.Log
import com.github.drumber.input2esp.backend.placeholders.KeyCode
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class ESPloitV2(var ip: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    fun runLivePayload(command: String) {
        val body = FormBody.Builder()
                .add("livepayloadpresent", "1")
                .add("livepayload", command)
                .build()

        val request = Request.Builder()
                .url("http://$ip/runlivepayload")
                .post(body)
                .build()

        client.newCall(request).execute().use { response ->
            if(!response.isSuccessful) throw IOException("Unexpected code $response")

            Log.d("ESPloitV2", "Response: ${response.body?.string()}")
        }

    }

    fun generatePressCommand(keycodes: List<Int>): String {
        return "Press:" + keycodes.joinToString("+")
    }

    fun generatePrintCommand(text: String): String {
        return "Print:$text"
    }

    fun generatePrintCommand(keycodes: List<Int>): String {
        val text = KeyCode.buildString(keycodes)
        return generatePrintCommand(text)
    }

    fun generatePrintLineCommand(text: String): String {
        return "PrintLine:$text"
    }

    fun generatePrintLineCommand(keycodes: List<Int>): String {
        val text = KeyCode.buildString(keycodes)
        return generatePrintLineCommand(text)
    }

}