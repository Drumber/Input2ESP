package com.github.drumber.input2esp.backend.utils

fun interface Callback<T> {

    fun onCallback(value: T?)

}