package com.tatasky.binge.customviews

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import com.tatasky.binge.R
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.isNetworkConnected
import com.tatasky.binge.utils.showToast
import java.util.*

class VoiceView : View, AnimatorUpdateListener {
    val TAG: String? = "RippleView"
    private var mRipples: MutableList<Ripple> = ArrayList()
    private var innerGreyCircle: Paint? = null
    private var innerBlueCircle: Paint? = null
    private var innerWhiteCircle: Paint? = null
    private val greyCircleAnimatorSet = AnimatorSet()
    private var mNormalBitmap: Bitmap? = null
    private var mRecordingBitmap: Bitmap? = null
    var mIsRecording = false
    private val STATE_NORMAL = 0
    private val STATE_RECORDING = 1
    var mState = STATE_NORMAL
    private var listener: Listener? = null
    private var increaseRadius = true
    private var radius: Int = 0
    private var MIC_BUTTON_RADIUS: Int = 0
    private var HIGHEST_SHADOW_RADIUS: Int = 0
    private var LOWEST_SHADOW_RADIUS: Int = 0

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes : Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private class Ripple internal constructor(
        startRadiusFraction: Float,
        stopRadiusFraction: Float,
        startAlpha: Float,
        stopAlpha: Float,
        color: Int,
        delay: Long,
        duration: Long,
        strokeWidth: Float,
        updateListener: AnimatorUpdateListener?
    ) {
        var mAnimatorSet: AnimatorSet
        var mRadiusAnimator: ValueAnimator
        var mAlphaAnimator: ValueAnimator
        var ripplePaint: Paint
        fun draw(
            canvas: Canvas,
            centerX: Int,
            centerY: Int,
            radiusMultiplicator: Float
        ) {
            ripplePaint.alpha = (255 * mAlphaAnimator.animatedValue as Float).toInt()
            canvas.drawCircle(
                centerX.toFloat(),
                centerY.toFloat(),
                mRadiusAnimator.animatedValue as Float * radiusMultiplicator,
                ripplePaint
            )
        }

        fun startAnimation() {
            mAnimatorSet.start()
        }

        fun stopAnimation() {
            mAnimatorSet.cancel()
        }

        init {
            mRadiusAnimator = ValueAnimator.ofFloat(startRadiusFraction, stopRadiusFraction)
            mRadiusAnimator.duration = duration
            mRadiusAnimator.repeatCount = ValueAnimator.INFINITE
            mRadiusAnimator.addUpdateListener(updateListener)
            mRadiusAnimator.interpolator = DecelerateInterpolator()
            mAlphaAnimator = ValueAnimator.ofFloat(startAlpha, stopAlpha)
            mAlphaAnimator.duration = duration
            mAlphaAnimator.repeatCount = ValueAnimator.INFINITE
            mAlphaAnimator.addUpdateListener(updateListener)
            mAlphaAnimator.interpolator = DecelerateInterpolator()
            mAnimatorSet = AnimatorSet()
            mAnimatorSet.playTogether(mRadiusAnimator, mAlphaAnimator)
            mAnimatorSet.startDelay = delay
            ripplePaint = Paint()
            ripplePaint.style = Paint.Style.STROKE
            ripplePaint.color = color
            ripplePaint.alpha = (255 * startAlpha).toInt()
            ripplePaint.isAntiAlias = true
            ripplePaint.strokeWidth = strokeWidth
        }
    }

    private fun init(context: Context) {

        radius = dpToPx(context, 70)
        MIC_BUTTON_RADIUS = dpToPx(context, 40)
        HIGHEST_SHADOW_RADIUS = dpToPx(context, 60)
        LOWEST_SHADOW_RADIUS = dpToPx(context, 40)

        mNormalBitmap = getBitmap(R.drawable.ic_mic_white_voice)
        mRecordingBitmap = getBitmap(R.drawable.ic_mic_colour)
        innerGreyCircle = Paint()
        innerGreyCircle!!.isAntiAlias = true
        innerGreyCircle!!.color = Color.argb(100, 204, 216, 216)
        innerBlueCircle = Paint()
        innerBlueCircle!!.isAntiAlias = true
        innerBlueCircle!!.color = Color.argb(255, 225, 0, 146)
        innerWhiteCircle = Paint()
        innerWhiteCircle!!.isAntiAlias = true
        innerWhiteCircle!!.color = Color.argb(255, 255, 255, 255)
        mRipples = ArrayList()
        mRipples.add(Ripple(0.0f, 1.0f, 1.0f, 0.0f, Color.rgb(204, 216, 216), 500, 3000, 4f, this))
        mRipples.add(Ripple(0.0f, 1.0f, 1.0f, 0.0f, Color.WHITE, 500, 500, 4f, this))
    }


    fun startAnimation() {
        visibility = VISIBLE
        for (ripple in mRipples) {
            ripple.startAnimation()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val centerX = width / 2
        val centerY = height / 2
        when (mState) {
            STATE_NORMAL -> {
                canvas.drawCircle(
                    centerX.toFloat(), centerY.toFloat(), MIC_BUTTON_RADIUS.toFloat(),
                    innerBlueCircle!!
                )
                canvas.drawBitmap(
                    mNormalBitmap!!,
                    centerX - mNormalBitmap!!.width / 2.toFloat(),
                    centerY - mNormalBitmap!!.height / 2.toFloat(),
                    innerBlueCircle
                )
            }
            STATE_RECORDING -> {
                for (ripple in mRipples) {
                    ripple.draw(canvas, centerX, centerY, centerX.toFloat())
                }
                canvas.drawCircle(
                    centerX.toFloat(), centerY.toFloat(), getAnimationRadius().toFloat(),
                    innerGreyCircle!!
                )
                canvas.drawCircle(
                    centerX.toFloat(), centerY.toFloat(), MIC_BUTTON_RADIUS.toFloat(),
                    innerWhiteCircle!!
                )
                canvas.drawBitmap(
                    mRecordingBitmap!!,
                    centerX - mRecordingBitmap!!.width / 2.toFloat(),
                    centerY - mRecordingBitmap!!.height / 2.toFloat(),
                    innerWhiteCircle
                )
            }
        }
    }

    private fun getAnimationRadius(): Int {
        radius = if (increaseRadius) radius + range() else radius - 1
        if (radius > HIGHEST_SHADOW_RADIUS) increaseRadius = false
        if (radius < LOWEST_SHADOW_RADIUS) increaseRadius = true
        return radius
    }

    private fun range(): Int {
        val r = Random()
        return r.nextInt(3 - 1 + 1) + 1
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!mIsRecording) {
            if (!isNetworkConnected(context))
                showToast(context, context.getString(R.string.network_error_message))
            else {
                listener!!.onStartListen()
                invalidate()
//                MixPanelHelper.getInstance().eventVoiceSearchReactivate()
//                MoEngageHelper.getInstance().eventVoiceSearchReactivate()
            }
        }
        return super.onTouchEvent(event)
    }

    fun startRecording() {
        startAnimation()
        mState = STATE_RECORDING
        mIsRecording = true
    }

    fun stopRecording() {
        mState = STATE_NORMAL
        mIsRecording = false
    }

    private fun getBitmap(drawableRes: Int): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, drawableRes)
        val canvas = Canvas()
        val bitmap =
            Bitmap.createBitmap(drawable?.intrinsicWidth?:0, drawable?.intrinsicHeight?:0, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable?.draw(canvas)
        return bitmap
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun onStartListen()
    }

}