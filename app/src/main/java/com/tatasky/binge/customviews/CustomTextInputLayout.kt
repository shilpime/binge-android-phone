package com.tatasky.binge.customviews

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.google.android.material.textfield.TextInputLayout
import com.tatasky.binge.R


/**
 * Created by Srikant Karnani on 26/11/19.
 */
class CustomTextInputLayout : TextInputLayout {

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    override fun setError(errorText: CharSequence?) {
        if (errorText != null) {
            val typeFaceDefault =
                Typeface.createFromAsset(resources.assets, context.getString(R.string.default_font))
            val ssbuilder = SpannableStringBuilder(errorText)
            ssbuilder.setSpan(
                CustomTypefaceSpan("", typeFaceDefault),
                0,
                ssbuilder.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            super.setError(
                ssbuilder
            )
        } else {
            super.setError(errorText)
        }
        (findViewById<TextView?>(com.google.android.material.R.id.textinput_error)?.parent?.parent as View?)?.let {
            ViewCompat.setPaddingRelative(
                it,
                0,
                0,
                0,
                0
            )
        }
    }
}