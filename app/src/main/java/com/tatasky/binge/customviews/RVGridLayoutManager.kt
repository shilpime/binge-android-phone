package com.tatasky.binge.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Srikant Karnani on 2/12/19.
 */
class RVGridLayoutManager : GridLayoutManager {

    constructor(context: Context) : super(context, DefaultSpanCount) {
    }

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) :
            super(context, DefaultSpanCount, orientation, reverseLayout) {
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {

    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }

    }

    companion object {
        val DefaultSpanCount: Int = 2
    }
}