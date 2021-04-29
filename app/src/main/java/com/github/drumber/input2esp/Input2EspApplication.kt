package com.github.drumber.input2esp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.livedata.asLiveData
import com.github.drumber.input2esp.backend.DeviceManager
import com.github.drumber.input2esp.backend.data.Preferences
import java.lang.ref.WeakReference

class Input2EspApplication: Application() {

    companion object {
        private lateinit var instance: WeakReference<Input2EspApplication>
        fun getInstance() = instance
    }

    val deviceManager: DeviceManager by lazy { DeviceManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = WeakReference(this)

        Kotpref.init(this)

        Preferences.asLiveData(Preferences::themeMode).observeForever {
            AppCompatDelegate.setDefaultNightMode(it.toInt())
        }
    }

}