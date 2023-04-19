package com.tatasky.binge.utils

import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.ui.features.home.HomeAnalytics

class CustomScrollListener(val callback: () -> Unit) : RecyclerView.OnScrollListener() {
    var currentScrollState = 0
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                if (currentScrollState == 1) {
                    callback()
                }
            }
            RecyclerView.SCROLL_STATE_DRAGGING -> println("Scrolling now")
            RecyclerView.SCROLL_STATE_SETTLING -> println("Scroll Settling")
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dx > 0) {
            currentScrollState = 1
        } else if (dx < 0) {
            currentScrollState = -1
        } else {
            currentScrollState = 0
        }
    }
}