package com.tatasky.binge.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import com.google.android.material.textfield.TextInputEditText
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.extensions.maxLength


/**
 * Created by Srikant Karnani on 21/11/19.
 */
class FixedPrefixTextInputEditText : TextInputEditText {
    internal var mOriginalLeftPadding = -1f
    var colorOfText: Int = Color.WHITE

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorOnBackground, typedValue, true)
        colorOfText = typedValue.data
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        calculatePrefix()
    }

    private fun calculatePrefix() {
        if (mOriginalLeftPadding == -1f) {
            val prefix = tag as String
            val widths = FloatArray(prefix.length)
            paint.getTextWidths(prefix, widths)
            var textWidth = 0f
            for (w in widths) {
                textWidth += w
            }
            mOriginalLeftPadding = compoundPaddingLeft.toFloat()
            setPadding((textWidth + mOriginalLeftPadding).toInt(), paddingRight, paddingTop, paddingBottom)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val prefix = tag as String
        val mPaint = paint
        mPaint.color = colorOfText
        canvas.drawText(prefix, mOriginalLeftPadding, getLineBounds(0, null).toFloat(), mPaint)
    }

    /*override fun setText(text: CharSequence?, type: BufferType?) {
        try{
            var mText = text
            mText = mText?.trim()
            mText = mText?.filter { !it.isWhitespace() }
            mText = mText?.filter { it.isDigit() }
            if (mText != null) {
                if(mText.length > 10 ){
                    mText = mText.takeLast(10)
                }
            }
            if((text?.length?:0)>(this.maxLength?:Int.MAX_VALUE)){
                mText = text?.substring(0 until (maxLength?:Int.MAX_VALUE))
            }
            super.setText(mText, type)
            post { setSelection(mText?.length?:0) }
        }
        catch (e : Exception){
            e.printStackTrace()
        }
    }*/
}