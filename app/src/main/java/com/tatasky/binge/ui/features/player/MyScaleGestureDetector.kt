package com.tatasky.binge.ui.features.player

import android.view.ScaleGestureDetector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

class MyScaleGestureDetector(
    private val scaleFactorLambda: ((Float) -> Unit)? = null,
) : ScaleGestureDetector.SimpleOnScaleGestureListener() {

    override fun onScale(
        detector: ScaleGestureDetector
    ): Boolean {
        scaleFactorLambda?.invoke(detector.scaleFactor)
        return super.onScale(detector)
    }

    override fun onScaleBegin(
        detector: ScaleGestureDetector
    ) : Boolean{
        return super.onScaleBegin(detector)
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        super.onScaleEnd(detector)
    }

    companion object {
        const val optimumPinchZoomScaleFactor = 1F
    }
}