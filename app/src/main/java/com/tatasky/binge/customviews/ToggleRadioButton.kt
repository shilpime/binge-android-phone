package com.tatasky.binge.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatRadioButton


class ToggleRadioButton : AppCompatRadioButton {

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }


    override fun toggle() {
        if (isChecked) {
            if (parent is RadioGroup) {
                (parent as RadioGroup).clearCheck()
            }
        } else {
            isChecked = true
        }
    }
}