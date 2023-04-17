package com.ttn.ttnplayer.listeners

import android.content.Context
import android.content.res.Configuration
import android.view.OrientationEventListener

class OrientationManager private constructor(private val context: Context) :
        OrientationEventListener(context) {
    private var previousAngle = 0
    private var orientation = 0
    private var orientationChangeListener: OrientationChangeListener? = null

    override fun onOrientationChanged(orientation: Int) {
        if (orientation == -1) return
        if (this.orientation == 0) {
            this.orientation = context.resources.configuration.orientation
            if (orientationChangeListener != null) {
                orientationChangeListener!!.onOrientationChanged(this.orientation)
            }
        }
        if (this.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                (previousAngle > 10 && orientation <= 10 ||
                        previousAngle in 271..349 && orientation >= 350)
        ) {
            if (orientationChangeListener != null) {
                orientationChangeListener!!.onOrientationChanged(Configuration.ORIENTATION_PORTRAIT)
            }
            this.orientation = Configuration.ORIENTATION_PORTRAIT
        }
        if (this.orientation == Configuration.ORIENTATION_PORTRAIT &&
                (previousAngle < 90 && orientation >= 90 && orientation < 270 ||
                        previousAngle > 280 && orientation <= 280 && orientation > 180)
        ) {
            if (orientationChangeListener != null) {
                orientationChangeListener!!.onOrientationChanged(Configuration.ORIENTATION_LANDSCAPE)
            }
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        }
        previousAngle = orientation
    }

    fun setOrientationChangedListener(l: OrientationChangeListener?) {
        orientationChangeListener = l
    }

    interface OrientationChangeListener {
        fun onOrientationChanged(newOrientation: Int)
    }

    companion object {
        private var instance: OrientationManager? = null
        fun getInstance(context: Context): OrientationManager? {
            if (instance == null) {
                instance = OrientationManager(context)
            }
            return instance
        }
    }

}