package com.tatasky.binge.ui.features.myaccount

import androidx.viewpager2.widget.ViewPager2

class ViewPager2PageChangeCallback(private val listener: (Int) -> Unit) :
        ViewPager2.OnPageChangeCallback() {

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        listener.invoke(position)
       /* when (position) {
            0 -> listener.invoke(position)
            6 -> listener.invoke(1)
        }*/
    }
}