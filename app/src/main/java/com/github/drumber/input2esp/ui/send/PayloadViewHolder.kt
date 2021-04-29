package com.github.drumber.input2esp.ui.send

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.models.Payload
import com.github.drumber.input2esp.backend.utils.Callback
import com.github.drumber.input2esp.backend.utils.CommonUtils
import com.github.drumber.input2esp.ui.components.touchhelper.ItemTouchHelperViewHolder
import com.google.android.material.card.MaterialCardView

class PayloadViewHolder(view: View): RecyclerView.ViewHolder(view), ItemTouchHelperViewHolder {

    private val payloadTextView: TextView = view.findViewById(R.id.payload_textView)
    private val typeTextView: TextView = view.findViewById(R.id.command_type_textView)

    val dragHandleView: ImageView = view.findViewById(R.id.drag_handle)

    /**
     * Bind this view holder to a payload model.
     */
    fun bindTo(payload: Payload, clickCallback: Callback<Payload>) {
        payloadTextView.text =  if(payload.payload.isNotBlank()) payload.payload
                                else itemView.context.getString(R.string.error_empty_payload)
        typeTextView.text = payload.type.name

        itemView.setOnClickListener {
            clickCallback.onCallback(payload)
        }
    }

    override fun onItemSelected() {
        if(itemView is MaterialCardView) {
            itemView.isDragged = true
        } else {
            itemView.setBackgroundColor(CommonUtils.getThemeValue(itemView.context, R.attr.colorBackgroundFloating))
        }
    }

    override fun onItemClear() {
        if(itemView is MaterialCardView) {
            itemView.isDragged = false
        } else {
            itemView.setBackgroundColor(CommonUtils.getThemeValue(itemView.context, R.attr.selectableItemBackground))
        }
    }

}

/**
 * Listener for item drag events.
 * from: https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-6a6f0c422efd
 */
fun interface OnStartDragListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}