package com.github.drumber.input2esp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.drumber.input2esp.kp2a.CredentialsData

class MainActivityViewModel: ViewModel() {

    private val credentialsData: MutableLiveData<CredentialsData> by lazy { MutableLiveData() }

    fun setCredentialsData(credentialsData: CredentialsData) {
        this.credentialsData.value = credentialsData
    }

    fun getCredentialsData(): LiveData<CredentialsData> = credentialsData

}