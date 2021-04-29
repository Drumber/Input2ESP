package com.github.drumber.input2esp.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.ImageViewCompat
import com.github.drumber.input2esp.backend.network.DiscoverState
import com.github.drumber.input2esp.backend.utils.DeviceUtils
import com.github.drumber.input2esp.databinding.ItemManageDeviceBinding

data class DeviceItem(val id: Int,
                      var name: String,
                      var type: String,
                      var description: String,
                      var state: DiscoverState?,
                      var stateDescription: String?
)

fun interface DeviceSettingClickedListener {
    fun onDeviceSettingClicked(deviceId: Int)
}

class DeviceListAdapter(
    context: Context,
    resource: Int,
    objects: List<DeviceItem>
): ArrayAdapter<DeviceItem>(context, resource, objects) {

    inner class ViewHolder(val binding: ItemManageDeviceBinding)

    private var settingClickedListener: DeviceSettingClickedListener? = null
    fun setDeviceSettingClickedListener(listener: DeviceSettingClickedListener) {
        settingClickedListener = listener
    }
    fun removeDeviceSettingClickedListener() {
        settingClickedListener = null
    }

    private fun notifyDeviceSettingClicked(deviceId: Int) {
        settingClickedListener?.onDeviceSettingClicked(deviceId)
    }

    override fun areAllItemsEnabled(): Boolean = false

    override fun isEnabled(position: Int): Boolean {
        return true
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder?
        if(convertView == null) {
            val binding: ItemManageDeviceBinding = ItemManageDeviceBinding.inflate(LayoutInflater.from(context), parent, false)
            viewHolder = ViewHolder(binding)
            binding.root.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        // get the device information for the requested position
        val device: DeviceItem? = getItem(position)?.let {
            val id = it.id
            val name = it.name
            val type = it.type
            val description = it.description
            val state = it.state
            val stateDescription = it.stateDescription
            return@let DeviceItem(id, name, type, description, state, stateDescription)
        }

        if(device != null) {
            val description = "${device.type} ${device.description}"
            viewHolder.binding.apply {
                deviceNameTextView.text = device.name
                deviceDescriptionTextView.text = description
                if(device.state != null) {
                    ImageViewCompat.setImageTintList(deviceStateImageView, ColorStateList.valueOf(DeviceUtils.getColorForDiscoverState(context, device.state)))
                } else {
                    deviceStateImageView.visibility = View.GONE
                }
                if(device.stateDescription != null) {
                    deviceStateTextView.text = device.stateDescription
                } else {
                    deviceStateTextView.visibility = View.GONE
                }

                deviceSettingButton.setOnClickListener { notifyDeviceSettingClicked(device.id) }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    deviceSettingButton.focusable = View.NOT_FOCUSABLE
                }
            }
        }

        return viewHolder.binding.root
    }

}