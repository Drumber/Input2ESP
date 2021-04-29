package com.github.drumber.input2esp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.widget.ExpandableListView
import android.widget.ListView

/**
 * Custom non-scrollable ListView.
 * Adapted from https://stackoverflow.com/a/24629341/12821118
 */
class NonScrollableListView: ListView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attr: AttributeSet) : super(context, attr)

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(context, attr, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val customHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE.shr(2), MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, customHeightMeasureSpec)
        layoutParams.height = measuredHeight
    }

}

class NonScrollableExpandableListView: ExpandableListView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attr: AttributeSet) : super(context, attr)

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(context, attr, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val customHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE.shr(2), MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, customHeightMeasureSpec)
        layoutParams.height = measuredHeight
    }

}