package com.tatasky.binge.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by Srikant Karnani on 30/1/20.
 */
class CustomRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var mRequestedLayout = false

    @SuppressLint("WrongCall")
    override fun requestLayout() {
        super.requestLayout()
        if (!mRequestedLayout) {
            mRequestedLayout = true
            this.post {
                mRequestedLayout = false
                layout(left, top, right, bottom)
                onLayout(false, left, top, right, bottom)
            }
        }
    }
}