package com.github.drumber.input2esp.ui.send

import android.app.Application
import androidx.lifecycle.*
import com.github.drumber.input2esp.Input2EspApplication
import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.DeviceManager
import com.github.drumber.input2esp.backend.data.Preferences
import com.github.drumber.input2esp.backend.models.CommandType
import com.github.drumber.input2esp.backend.models.DeviceModel
import com.github.drumber.input2esp.backend.models.ErrorReporting
import com.github.drumber.input2esp.backend.models.Payload
import com.github.drumber.input2esp.backend.placeholders.KeyCode
import com.github.drumber.input2esp.backend.placeholders.PlaceholderManager
import com.github.drumber.input2esp.backend.placeholders.handlers.CredentialsHandler
import com.github.drumber.input2esp.backend.protocol.PayloadSender
import com.github.drumber.input2esp.backend.utils.CommonUtils
import com.github.drumber.input2esp.backend.utils.ExceptionUtil
import com.github.drumber.input2esp.backend.utils.event.EventWrapper
import com.github.drumber.input2esp.kp2a.CredentialsData
import keepass2android.pluginsdk.KeepassDefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager: DeviceManager by lazy { (application as Input2EspApplication).deviceManager }
    val placeholderManager: PlaceholderManager by lazy { PlaceholderManager() }

    private val payloadList: MutableLiveData<MutableList<Payload>> by lazy { MutableLiveData(mutableListOf()) }
    fun getPayloadList(): LiveData<MutableList<Payload>> = payloadList

    private val isSending: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    fun isSending(): LiveData<Boolean> = isSending

    var deviceModel: DeviceModel? = null
        private set

    init {
        // add default payload
        addPayloadCommand()
    }

    fun addPayloadCommand(payload: Payload = Payload(CommandType.PrintLine, "")) {
        payloadList.value?.add(payload)
        notifyPayloadChange()
    }

    fun removePayloadCommand(payload: Payload) {
        if(payloadList.value?.remove(payload) == true) {
            notifyPayloadChange()
        }
    }

    fun notifyPayloadChange() {
        payloadList.postValue(payloadList.value)
    }

    fun setDeviceModel(deviceId: Int) {
        if(deviceModel?.id == deviceId) return
        deviceModel = deviceManager.getDevice(deviceId)
        if(deviceModel is ErrorReporting) {
            liveErrorMessage.addSource((deviceModel as ErrorReporting).getLiveErrorReport()) { throwable ->
                throwable.getContentIfNotHandled()?.let {
                    liveErrorMessage.postValue(EventWrapper(ExceptionUtil.translateNetworkException(it)))
                }
            }
        }
    }

    fun onConnectButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deviceModel?.toggleConnect()
            } catch (e: Exception) {
                liveErrorMessage.postValue(EventWrapper("Error: ${e.message}"))
            }
        }
    }

    fun onPayloadSend() {
        if(isSending.value == true) return
        val payloadList = payloadList.value
        if(payloadList != null && payloadList.size > 0) {
            deviceModel?.let {
                isSending.postValue(true)
                val payloadSender = PayloadSender(it, placeholderManager)
                viewModelScope.launch(Dispatchers.IO) {
                    payloadSender.sendPayloadList(payloadList)
                }.invokeOnCompletion { isSending.postValue(false) }
            }
        } else {
            liveErrorMessage.postValue(EventWrapper(CommonUtils.getStringResource(R.string.error_no_commands)))
        }
    }

    private val liveErrorMessage: MediatorLiveData<EventWrapper<String>> by lazy { MediatorLiveData() }
    fun getLiveErrorMessage(): LiveData<EventWrapper<String>> = liveErrorMessage

    private var credentialsHandler: CredentialsHandler? = null

    /**
     * Called when keepass2android credentials are available.
     */
    fun onCredentialsAvailable(credentials: CredentialsData) {
        if(credentialsHandler == null) {
            credentialsHandler = CredentialsHandler(credentials).also {
                it.register(placeholderManager)
            }

            // check if there is only the default payload
            payloadList.value?.let {
                if(it.size == 1 && it[0].payload.isBlank()) {
                    removePayloadCommand(it[0]) // remove default payload command

                    // add payload based on selected field
                    when(credentials.fieldId) {
                        KeepassDefs.UserNameField -> addCredentialPayload(CredentialsHandler.USER_NAME)
                        KeepassDefs.UrlField -> addCredentialPayload(CredentialsHandler.URL)
                        KeepassDefs.PasswordField -> addCredentialPayload(CredentialsHandler.PASSWORD)
                        else -> addAllCredentialPayloads()
                    }
                }
            }
        }
    }

    private fun addCredentialPayload(placeholder: String) {
        val cmdType = if(Preferences.enterAfterCredentials) CommandType.PrintLine else CommandType.Print
        addPayloadCommand(Payload(cmdType, placeholder.makePlaceholder()))
    }

    private fun addAllCredentialPayloads() {
        val cmdType = if(Preferences.enterAfterCredentials) CommandType.PrintLine else CommandType.Print
        addPayloadCommand(Payload(CommandType.Print, CredentialsHandler.USER_NAME.makePlaceholder()))   // 1. user name
        addPayloadCommand(Payload(CommandType.Press, KeyCode.Modifier.TAB.name.makePlaceholder()))      // 2. TAB
        addPayloadCommand(Payload(cmdType, CredentialsHandler.PASSWORD.makePlaceholder()))              // 3. password (+ Enter)
    }

    /**
     * Add closures around the specified placeholder.
     */
    private fun String.makePlaceholder(): String {
        return "${placeholderManager.closure.head}$this${placeholderManager.closure.tail}"
    }

    override fun onCleared() {
        super.onCleared()
        credentialsHandler = null
    }

}