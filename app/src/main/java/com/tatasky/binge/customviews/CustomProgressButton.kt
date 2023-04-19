package com.tatasky.binge.customviews

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.tatasky.binge.R

class CustomProgressButton: MaterialButton {
    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    fun showLoading(){
        setOnClickListener(null)
        icon = ContextCompat.getDrawable(context, R.drawable.button_loading_drawable)
        text = ""
        if(icon is AnimatedVectorDrawable){
                (icon!! as AnimatedVectorDrawable).start()
        }
    }

    /*override fun setText(text: CharSequence?, type: BufferType?) {
        if(!text.isNullOrBlank())
            icon = null
        super.setText(text, type)
    }*/
}