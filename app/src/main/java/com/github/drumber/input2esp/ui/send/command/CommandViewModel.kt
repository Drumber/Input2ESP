package com.github.drumber.input2esp.ui.send.command

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.drumber.input2esp.backend.models.Payload
import com.github.drumber.input2esp.backend.placeholders.PlaceholderManager
import com.github.drumber.input2esp.ui.send.SendViewModel

class CommandViewModel: ViewModel() {

    lateinit var placeholderManager: PlaceholderManager
    private val payload: MutableLiveData<Payload> by lazy { MutableLiveData() }

    var sendViewModel: SendViewModel? = null

    var searchText: String? = null

    fun getPayload(): LiveData<Payload> = payload

    fun setPayload(payload: Payload) {
        this.payload.value = payload
    }

    fun deletePayload() {
        payload.value?.let { sendViewModel?.removePayloadCommand(it) }
    }

    fun getPlaceholderList(): List<String> {
        return placeholderManager.getPlaceholders().toList()
    }

    fun getPlaceholderGroups() = placeholderManager.getPlaceholderGroups()

    fun getPlaceholder(index: Int): String {
        return makePlaceholder(getPlaceholderList()[index])
    }

    fun makePlaceholder(placeholder: String): String {
        placeholderManager.apply {
            return "${closure.head}$placeholder${closure.tail}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        sendViewModel = null
    }

}