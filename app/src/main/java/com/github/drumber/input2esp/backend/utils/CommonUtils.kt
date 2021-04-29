package com.github.drumber.input2esp.backend.utils

import android.content.Context
import android.util.TypedValue
import androidx.lifecycle.MutableLiveData
import com.github.drumber.input2esp.Input2EspApplication
import org.json.JSONException
import org.json.JSONObject

object CommonUtils {

    fun getStringResource(id: Int): String {
        return Input2EspApplication.getInstance().get()?.getString(id) ?: "<<Error: Cannot access application instance.>>"
    }

    fun getThemeValue(context: Context, resId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.data
    }

    fun getThemeResourceId(context: Context, resId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.resourceId
    }

    /**
     * Extension function to notify a LiveData element about
     * a data change.
     * from https://stackoverflow.com/a/52075248/12821118
     */
    fun <T> MutableLiveData<T>.notifyObserver() {
        this.postValue(this.value)
    }

    fun JSONObject.getStringOr(name: String, default: String?): String? {
        return try {
            this.getString(name)
        } catch (e: JSONException) {
            default
        }
    }
    fun JSONObject.getIntOr(name: String, default: Int?): Int? {
        return try {
            this.getInt(name)
        } catch (e: JSONException) {
            default
        }
    }

    fun JSONObject.getBooleanOr(name: String, default: Boolean?): Boolean? {
        return try {
            this.getBoolean(name)
        } catch (e: JSONException) {
            default
        }
    }

    fun JSONObject.getDoubleOr(name: String, default: Double?): Double? {
        return try {
            this.getDouble(name)
        } catch (e: JSONException) {
            default
        }
    }


}