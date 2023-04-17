package com.tatasky.binge.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.ContentViewCallback
import com.tatasky.binge.R

class CustomSnackbarParentalControlView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defaultStyle: Int = 0,
) : ConstraintLayout(context, attributeSet, defaultStyle), ContentViewCallback {

    init {
        View.inflate(context, R.layout.snackbar_custom_parental_control, this)
    }

    override fun animateContentIn(delay: Int, duration: Int) {
    }

    override fun animateContentOut(delay: Int, duration: Int) {
    }

}