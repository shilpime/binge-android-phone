package com.tatasky.binge.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.tatasky.binge.ui.base.frameworks.extensions.afterMeasured


/**
 * Created by Srikant Karnani on 22/11/21.
 */
open class ProviderGridRecyclerView : ClickableRecyclerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setLayoutManager(itemsCount: Int, spanCount: Int) {
        layoutManager = getGridManagerLastRowCenter(
            context,
            spanCount,
            itemsCount,
            RecyclerView.VERTICAL,
            false
        )
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        postDelayed(1000L) {
            super.setAdapter(adapter)
        }
    }

    private fun getGridManagerLastRowCenter(
        ctx: Context, spanCount: Int, itemsCount: Int,
        @RecyclerView.Orientation orientation: Int, reverseLayout: Boolean
    )
            : LayoutManager {

        // get number of items in last row
        val lastRowCount = itemsCount % spanCount

       /* // number of rows with all items
        val fullRows = itemsCount / spanCount
        // "span" counter for whole row (as minimum divider)
        val rowSpan = spanCount * lastRowCount
        // span value for item in the first rows
        val lastRowItemSpan = rowSpan / lastRowCount
        // span value for item in the last row
        val baseRowItemSpan = rowSpan / spanCount*/

        // return generated manager

        return object  : FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP){
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                post {
                    lp?.setMargins(0, 0, 0, 0)
                    lp?.width = measuredWidth / spanCount
                    lp?.height = measuredWidth / spanCount
                }
                return true
            }

            override fun getPaddingEnd(): Int {
                return 0
            }

            override fun getPaddingStart(): Int {
                return 0
            }

            override fun canScrollHorizontally(): Boolean {
                return false
            }

            override fun canScrollVertically(): Boolean {
                return false
            }

            override fun calculateItemDecorationsForChild(child: View, outRect: Rect) {
                super.calculateItemDecorationsForChild(child.apply {
                    left = 0
                    right = 0
                }, outRect)
            }
        }.apply {
            justifyContent = JustifyContent.CENTER
            maxLine = 3
        }
    }
}