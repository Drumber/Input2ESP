package com.github.drumber.input2esp.ui.components

import androidx.core.widget.NestedScrollView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ExtendedFloatingActionButtonScrollListener(private val floatingActionButton: ExtendedFloatingActionButton, var offset: Int = 0): NestedScrollView.OnScrollChangeListener {

    override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        if(!floatingActionButton.isExtended && scrollY <= offset) {
            floatingActionButton.extend()
        } else if(floatingActionButton.isExtended && scrollY > offset) {
            floatingActionButton.shrink()
        }
    }

}