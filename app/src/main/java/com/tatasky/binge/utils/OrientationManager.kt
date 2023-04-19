package com.tatasky.binge.utils

import android.content.Context
import android.view.OrientationEventListener
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class OrientationManager : OrientationEventListener {
    enum class ScreenOrientation {
        REVERSED_LANDSCAPE, LANDSCAPE, PORTRAIT, REVERSED_PORTRAIT
    }

    var disposable: Disposable? = null

    private var mContext : Context?=null
    private var screenOrientation: ScreenOrientation? = null
    private var listener: OrientationListener? = null

    constructor(
        context: Context?,
        rate: Int,
        listener: OrientationListener?
    ) : super(context, rate) {
        mContext = context
        setListener(listener)
    }

    constructor(context: Context?, rate: Int) : super(context, rate) {}
    constructor(context: Context?) : super(context) {}

    override fun onOrientationChanged(orientation: Int) {
        if (android.provider.Settings.System.getInt(mContext?.contentResolver, android.provider.Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            if (orientation == -1) {
                return
            }
            val isPortrait = orientation > 300 || orientation < 60 || orientation in 120..240

//        if ((requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && isPortrait) ||
//            (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && !isPortrait)){
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
//        }
            val newOrientation: ScreenOrientation = if (isPortrait) {
                ScreenOrientation.PORTRAIT
            } else {
                ScreenOrientation.LANDSCAPE
            }
//        val newOrientation: ScreenOrientation = when (orientation) {
//            in 60..140 -> {
//                ScreenOrientation.REVERSED_LANDSCAPE
//            }
//            in 140..220 -> {
//                ScreenOrientation.REVERSED_PORTRAIT
//            }
//            in 220..300 -> {
//                ScreenOrientation.LANDSCAPE
//            }
//            else -> {
//                ScreenOrientation.PORTRAIT
//            }
//        }
            if (newOrientation != screenOrientation) {
                screenOrientation = newOrientation
                if (listener != null) {
                    disposable?.dispose()
                    if(mContext?.let { isTablet(it) } == true){
                        disposable = Completable.timer(400, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                            listener?.onOrientationChange(screenOrientation)
                        }
                    }else{
                        disposable = Completable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                            listener?.onOrientationChange(screenOrientation)
                        }
                    }

                }
            }
        }
    }

    private fun setListener(listener: OrientationListener?) {
        this.listener = listener
    }

    interface OrientationListener {
        fun onOrientationChange(screenOrientation: ScreenOrientation?)
    }
}
