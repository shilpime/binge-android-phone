package com.tatasky.binge.customviews

import android.animation.LayoutTransition
import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tatasky.binge.R


fun makeTextViewResizable(tv: TextView, maxLine: Int, expandText: String, viewMore: Boolean) {
    val vto = tv.viewTreeObserver
    vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val obs = tv.viewTreeObserver
            obs.removeGlobalOnLayoutListener(this)

            if (tv.lineCount <= maxLine) {
                /*
                * Will use if anything else required
                * */
            } else if (maxLine > 0 && tv.lineCount >= maxLine) {
                val lineEndIndex = tv.layout.getLineEnd(maxLine - 1)
                val text = (tv.tag as String).subSequence(0, (lineEndIndex - 2* expandText.length)-16)
                    .toString() + " " + expandText
                tv.setText(
                    addClickablePartTextViewResizable(
                        text, tv, expandText,
                        viewMore
                    ), TextView.BufferType.NORMAL
                )
            } else {
                val lineEndIndex = tv.layout.getLineEnd(tv.layout.lineCount - 1)
                val text = (tv.tag as String).subSequence(0, lineEndIndex).toString() + " " + expandText
                tv.setText(
                    addClickablePartTextViewResizable(
                        text, tv, expandText,
                        viewMore
                    ), TextView.BufferType.NORMAL
                )
            }
            tv.maxLines = 100
        }
    })
}

fun validateEmail(text:String):Boolean{
    return !TextUtils.isEmpty(text) && Patterns.EMAIL_ADDRESS.matcher(text).matches()
}
private fun addClickablePartTextViewResizable(
    strSpanned: String, tv: TextView,
    spanableText: String, viewMore: Boolean
): SpannableStringBuilder? {
    val str = strSpanned.toString()
    val ssb = SpannableStringBuilder(strSpanned)
    if (str.contains(spanableText)) {
        ssb.setSpan(object : MySpannable(false, tv.context) {
            override fun onClick(widget: View) {
                /*tv.invalidate()
                tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                if (viewMore) {
                    (tv.parent as ViewGroup).layoutTransition = LayoutTransition().apply {
                        enableTransitionType(LayoutTransition.CHANGING)
                        disableTransitionType(LayoutTransition.APPEARING)
                        disableTransitionType(LayoutTransition.CHANGE_APPEARING)
                        disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
                        disableTransitionType(LayoutTransition.DISAPPEARING)
                        setDuration(500)
                    }
                    makeTextViewResizable(tv, -1, " - Less", false)
                } else {
                    (tv.parent as ViewGroup).layoutTransition = null
                    makeTextViewResizable(tv, 2, " + More", true)
                }*/
            }
        }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length, 0)
    }
    return ssb
}

open class MySpannable(isUnderline: Boolean, context : Context) : ClickableSpan() {
    private var isUnderline = false
    private var context : Context? = null
    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = isUnderline
        ds.color = ContextCompat.getColor(context!!, R.color.more_color)//Color.parseColor("#1b76d3")
    }

    override fun onClick(widget: View) {}

    /**
     * Constructor
     */
    init {
        this.isUnderline = isUnderline
        this.context = context
    }
}

class MovementMethod : LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            if (off >= widget.text.length) {
                // Return true so click won't be triggered in the leftover empty space
                return true
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}
