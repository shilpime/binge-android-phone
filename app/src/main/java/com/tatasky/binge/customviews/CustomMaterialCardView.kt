package com.tatasky.binge.customviews

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import com.tatasky.binge.R

class CustomMaterialCardView : MaterialCardView {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init()
    }

    fun init(){
        setBackgroundResource(R.drawable.bg_bottom_gradient)

    }
}