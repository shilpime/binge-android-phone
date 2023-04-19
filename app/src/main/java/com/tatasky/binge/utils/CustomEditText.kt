package com.tatasky.binge.utils

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class CustomEditText :  AppCompatEditText {

    private var mOnImeBack:EditTextImeBackListener?=null



    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {

    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.getKeyCode() == KeyEvent.KEYCODE_BACK &&
            event.getAction() == KeyEvent.ACTION_UP) {
            mOnImeBack?.onImeBack(this.getText().toString())
        }
        return super.dispatchKeyEvent( event)
    }


    fun setOnEditTextImeBackListener( listener: EditTextImeBackListener) {
        mOnImeBack = listener;
    }

}

interface EditTextImeBackListener {
     abstract fun onImeBack( text:String)
}