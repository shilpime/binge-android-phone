package com.tatasky.binge.customviews

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show

class CustomSearchConstraintLayout : ConstraintLayout {
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

    private fun init() {
        layoutTransition = LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }
    }

    fun setState(state: Int) {
        when (state) {
            STATE_IDLE -> {
                findViewById<CustomSearchView>(R.id.et_search)?.setSubmitted(false)
                findViewById<ImageView>(R.id.iv_search)?.show()
                findViewById<ImageView>(R.id.iv_speak_now)?.show()
//                findViewById<ImageButton>(R.id.iv_back)?.hide()
                findViewById<ImageView>(R.id.iv_speak_now)?.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.darkHighlight)
                )
               setBackgroundIdle()
            }
            STATE_SEARCHING -> {
                findViewById<ImageView>(R.id.iv_search)?.hide()
//                findViewById<ImageButton>(R.id.iv_back)?.show()
                findViewById<CustomSearchView>(R.id.et_search)?.setSubmitted(false)
                findViewById<ImageView>(R.id.iv_speak_now)?.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.darkHighlight)
                )
               setBackgroundOther()
            }
            STATE_SEARCHED -> {
                findViewById<ImageView>(R.id.iv_search)?.hide()
//                findViewById<ImageButton>(R.id.iv_back)?.show()
                findViewById<ImageView>(R.id.iv_speak_now)?.hide()
                findViewById<CustomSearchView>(R.id.et_search)?.setSubmitted(true)
                findViewById<ImageView>(R.id.iv_speak_now)?.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.darkOnBackground)
                )
               setBackgroundOther()
            }
            STATE_NO_RESULT -> {
                findViewById<ImageView>(R.id.iv_search)?.hide()
//                findViewById<ImageButton>(R.id.iv_back)?.show()
                findViewById<ImageView>(R.id.iv_speak_now)?.hide()
                findViewById<CustomSearchView>(R.id.et_search)?.setSubmitted(false)
                findViewById<ImageView>(R.id.iv_speak_now)?.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.darkHighlight)
                )
                setBackgroundOther()
            }
        }
    }

    private fun setBackgroundIdle(){
        val set = ConstraintSet()
        set.clone(this)
//        set.clear(findViewById<View>(R.id.view_background).id, ConstraintSet.START)
//        set.connect(
//            R.id.view_background, ConstraintSet.START,
//            ConstraintSet.PARENT_ID, ConstraintSet.START
//        )
        set.applyTo(this)
    }
    private fun setBackgroundOther(){
        val set = ConstraintSet()
        set.clone(this)
//        set.clear(findViewById<View>(R.id.view_background).id, ConstraintSet.START)
//        set.connect(
//            R.id.view_background, ConstraintSet.START,
//            R.id.et_search, ConstraintSet.START
//        )
        set.applyTo(this)
    }

    companion object {
        val STATE_IDLE = 0
        val STATE_SEARCHING = 1
        val STATE_SEARCHED = 2
        val STATE_NO_RESULT = -1
    }
}