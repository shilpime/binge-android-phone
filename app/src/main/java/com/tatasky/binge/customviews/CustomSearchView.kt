package com.tatasky.binge.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.tatasky.binge.R
import android.R.id
import android.text.InputFilter
import android.text.Spanned
import android.R.drawable
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.Drawable.ConstantState
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.text.Editable
import android.text.TextWatcher
import android.view.View


/**
 * Created by Srikant Karnani on 5/1/20.
 */
class CustomSearchView : SearchView {
    private var mIsSubmitted = false
    private val STATE_SUBMITTED = intArrayOf(R.attr.state_submitted)
    var mSearchSrcTextView: SearchView.SearchAutoComplete? = null
    var listener: OnQueryTextListener? = null

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    fun setSubmitted(isSubmitted: Boolean) {
        mIsSubmitted = isSubmitted
        findViewById<TextView>(R.id.search_src_text)?.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.black
            )
        )
        findViewById<ImageView>(R.id.search_close_btn).setColorFilter(
            ContextCompat.getColor(
                context,
                R.color.darkOnSurface
            )
        )
        refreshDrawableState()
    }


    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace+1)
        if (mIsSubmitted)
            mergeDrawableStates(drawableState, STATE_SUBMITTED)

        return drawableState
    }

    override fun setOnQueryTextListener(listener: SearchView.OnQueryTextListener?) {
        super.setOnQueryTextListener(listener)
        this.listener = listener
        mSearchSrcTextView = this.findViewById(androidx.appcompat.R.id.search_src_text)
        mSearchSrcTextView?.filters = arrayOf(InputFilter.LengthFilter(25))
        mSearchSrcTextView?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newValue = replaceInvalidCharacters(s.toString())
                if (newValue != s.toString()) {
                    mSearchSrcTextView?.setText(newValue)
                    mSearchSrcTextView?.text?.length?.let { mSearchSrcTextView?.setSelection(it) }
                }
            }
        })
        mSearchSrcTextView?.setOnEditorActionListener { textView, i, keyEvent ->
            listener?.onQueryTextSubmit(query.toString())
            true
        }
    }

    fun getSubmitted() = mIsSubmitted

    private fun replaceInvalidCharacters(value: String) : String {
        val regex = "[a-zA-Z 0-9-,!'#\$()+./:~;<=>?@*]+".toRegex()
        val matchResult = regex.findAll(value.toString())
        val x = matchResult.map { it.value.trimStart() }.toList()
        return x.joinToString("")
    }
}
