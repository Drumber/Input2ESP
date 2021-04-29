package com.github.drumber.input2esp.ui.send.command

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.github.drumber.input2esp.R

class PlaceholderListAdapter(val context: Context, val groups: List<String>, val itemsGroups: Map<String, List<String>>): BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = groups.size

    override fun getChildrenCount(groupPosition: Int): Int = itemsGroups[getGroup(groupPosition)]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = groups[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return itemsGroups[getGroup(groupPosition)]?.get(childPosition) ?: ""
    }

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

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
}