package com.github.drumber.input2esp.ui.send.command

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.github.drumber.input2esp.R

class PlaceholderListAdapter(val context: Context, val itemsGroups: MutableMap<String, MutableList<String>>): BaseExpandableListAdapter(), Filterable {

    private val filter: PlaceholderFilter by lazy { PlaceholderFilter() }
    private var originalValues: Map<String, List<String>>? = null

    override fun getGroupCount(): Int = itemsGroups.keys.size

    override fun getChildrenCount(groupPosition: Int): Int = itemsGroups[getGroup(groupPosition)]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = itemsGroups.keys.toMutableList()[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return itemsGroups[getGroup(groupPosition)]?.get(childPosition) ?: ""
    }

    override fun getGroupId(groupPosition: Int): Long = getGroup(groupPosition).hashCode().toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view: View = if(convertView != null) convertView else {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_placeholder_group, null)
        }

        val groupText = view.findViewById<TextView>(R.id.group_textView)
        groupText.text = getGroup(groupPosition) as String

        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view: View = if(convertView != null) convertView else {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_placeholder_child, null)
        }

        val itemText = view.findViewById<TextView>(R.id.placeholder_textView)
        itemText.text = getChild(groupPosition, childPosition) as String

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getFilter(): Filter {
        return filter
    }

    private inner class PlaceholderFilter: Filter() {
        override fun performFiltering(prefix: CharSequence?): FilterResults {
            val results = FilterResults()

            if(originalValues == null) {
                synchronized(this) {
                    originalValues = itemsGroups.toMap()
                }
            }

            if(prefix == null || prefix.isEmpty()) {
                originalValues?.let {
                    results.values = it
                    results.count = it.size
                }
            } else {
                val values = originalValues?.toMutableMap()
                if(values != null) {
                    val newValues = mutableMapOf<String, MutableList<String>>()

                    values.entries.forEach { (key, value) ->
                        // loop over all placeholders of current group
                        value.forEach { placeholder ->
                            // add placeholder to new list of current group if it contains the prefix
                            if(placeholder.contains(prefix, true)) {
                                val newPlaceholders = newValues[key] ?: mutableListOf()
                                newPlaceholders.add(placeholder)
                                newValues[key] = newPlaceholders
                            }
                        }
                    }

                    results.values = newValues
                    results.count = newValues.size
                }
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val newValues: Map<out String, MutableList<String>>? = results?.values as? Map<out String, MutableList<String>>
            if(newValues != null) {
                itemsGroups.clear()
                itemsGroups.putAll(newValues)
                if(results.count > 0) {
                    notifyDataSetChanged()

                } else {
                    notifyDataSetInvalidated()
                }
            }
        }

    }
}