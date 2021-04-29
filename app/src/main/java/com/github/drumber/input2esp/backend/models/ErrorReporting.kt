package com.github.drumber.input2esp.backend.models

import androidx.lifecycle.LiveData
import com.github.drumber.input2esp.backend.utils.event.EventWrapper

interface ErrorReporting {

    fun getLiveErrorReport(): LiveData<EventWrapper<Throwable>>

}