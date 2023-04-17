package com.tatasky.binge.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.jvm.JvmOverloads
import com.tatasky.binge.customviews.ClickableRecyclerView.CenteredGridLayoutManager

open class ClickableRecyclerView : RecyclerView {
    private val manager: GridLayoutManager? = null
    private var columnWidth = -1
    private val detector: GestureDetectorCompat

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    )

    private inner class ClickListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            performClick()
            return true
        }
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        detector.onTouchEvent(e)
        return super.dispatchTouchEvent(e)
    }


    fun setCenterFitLayoutManager(spanCount: Int) {
        layoutManager = CenteredGridLayoutManager(context, spanCount)
    }

    inner class CenteredGridLayoutManager : GridLayoutManager {
        private val maxCount = 5

        constructor(
            context: Context?,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes) {
        }

        constructor(context: Context?, spanCount: Int) : super(context, spanCount) {}
        constructor(
            context: Context?,
            spanCount: Int,
            orientation: Int,
            reverseLayout: Boolean
        ) : super(context, spanCount, orientation, reverseLayout) {
        }

        override fun getPaddingLeft(): Int {
            columnWidth = measuredWidth / maxCount
            val totalItemWidth = columnWidth * Math.min(itemCount, maxCount)
            return if (totalItemWidth >= measuredWidth) {
                super.getPaddingLeft() // do nothing
            } else {
                Math.round((measuredWidth - totalItemWidth) / 2f)
            }
        }

        override fun getPaddingRight(): Int {
            return paddingLeft
        }
    }

    init {
        detector = GestureDetectorCompat(context, ClickListener())
    }
}