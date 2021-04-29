package com.github.drumber.input2esp.backend.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileStorage {
    private val TAG = "FileStorage"

    fun storeData(context: Context, fileName: String, jsonData: String) {
        val file = File(context.filesDir, fileName)
        Log.d(TAG, "Storing in file ${file.absolutePath}")
        file.parentFile?.mkdirs()
        FileOutputStream(file).bufferedWriter().use {
            it.write(jsonData)
        }
    }

    fun loadData(context: Context, fileName: String): String? {
        val file = File(context.filesDir, fileName)
        Log.d(TAG, "Loading from file ${file.absolutePath}")
        if(!file.isFile) return null

        return FileInputStream(file).bufferedReader().use {
            it.readText()
        }
    }

}