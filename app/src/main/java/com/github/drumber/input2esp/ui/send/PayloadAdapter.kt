package com.github.drumber.input2esp.ui.send

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.models.Payload
import com.github.drumber.input2esp.backend.utils.Callback
import com.github.drumber.input2esp.ui.components.touchhelper.ItemTouchHelperAdapter

class PayloadAdapter(private val payloadList: MutableList<Payload>, private val dragStartListener: OnStartDragListener? = null, val clickCallback: Callback<Payload>)
    : RecyclerView.Adapter<PayloadViewHolder>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayloadViewHolder {
        return PayloadViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_payload_command, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: PayloadViewHolder, position: Int) {
        holder.bindTo(getItem(position), clickCallback)
        holder.dragHandleView.setOnTouchListener { view, motionEvent ->
            if(motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                dragStartListener?.onStartDrag(holder)
            }
            false
        }
    }

    fun getItem(position: Int) = payloadList[position]

    override fun getItemCount(): Int {
        return payloadList.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val prev = payloadList.removeAt(fromPosition)
        val newPosition = if(toPosition > fromPosition) toPosition - 1 else toPosition
        payloadList.add(newPosition, prev)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        payloadList.removeAt(position)
        notifyItemRemoved(position)
    }

}

class PayloadItemDiffCallback: DiffUtil.ItemCallback<Payload>() {
    override fun areItemsTheSame(oldItem: Payload, newItem: Payload): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Payload, newItem: Payload): Boolean = oldItem == newItem
}