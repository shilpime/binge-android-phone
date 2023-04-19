package com.tatasky.binge.customviews

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import com.tatasky.binge.R

class CustomSpinner : AppCompatSpinner {
    // private static final String TAG = "CustomSpinner";
    private var mListener: OnSpinnerEventsListener? = null
    private var mOpenInitiated = false

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, mode: Int) : super(context, attrs, defStyleAttr, mode) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, mode: Int) : super(context, mode) {}
    constructor(context: Context) : super(context) {}

    interface OnSpinnerEventsListener {
        fun onSpinnerOpened(spin: Spinner?)
        fun onSpinnerClosed(spin: Spinner?)
    }

    override fun performClick(): Boolean {
        // register that the Spinner was opened so we have a status
        // indicator for the activity(which may lose focus for some other
        // reasons)
        mOpenInitiated = true
        background = ContextCompat.getDrawable(context, R.drawable.spinner_background_contracted)
        ((background as LayerDrawable).getDrawable(1) as AnimatedVectorDrawable).start()
        if (mListener != null) {
            mListener!!.onSpinnerOpened(this)
        }
        return super.performClick()
    }

    fun setSpinnerEventsListener(onSpinnerEventsListener: OnSpinnerEventsListener?) {
        mListener = onSpinnerEventsListener
    }

    /**
     * Propagate the closed Spinner event to the listener from outside.
     */
    fun performClosedEvent() {
        background = ContextCompat.getDrawable(context, R.drawable.spinner_background_expanded)
        ((background as LayerDrawable).getDrawable(1) as AnimatedVectorDrawable).start()
        mOpenInitiated = false
        if (mListener != null) {
            mListener!!.onSpinnerClosed(this)
        }
    }

    /**
     * A boolean flag indicating that the Spinner triggered an open event.
     *
     * @return true for opened Spinner
     */
    fun hasBeenOpened(): Boolean {
        return mOpenInitiated
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasBeenOpened() && hasWindowFocus) {
            performClosedEvent()
        }
    }
}