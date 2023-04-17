package com.tatasky.binge.customviews

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import com.tatasky.binge.utils.dpToPx

class MyView : View {
    private var mPaint: Paint? = null
    private  var mPaint1 : Paint? = null
    private var strokePaintColor: Paint? = null
    private var mMinRadius = 0f
    private var mMaxRadius = 0f
    private var mCurrentRadius = 0f
    private val TAG = MyView::class.java.name
    private val mAnimatorSet = AnimatorSet()


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.color = Color.argb(255, 219, 219, 219)
        mPaint1 = Paint()
        mPaint1?.setAntiAlias(true)
        mPaint1?.setStyle(Paint.Style.FILL)
        mPaint1?.setColor(Color.argb(0, 255, 255, 255))
        strokePaintColor = Paint()
        strokePaintColor!!.style = Paint.Style.STROKE
        strokePaintColor!!.strokeWidth = 1f
        strokePaintColor!!.color = Color.argb(100, 128, 128, 128)
        mMinRadius = (dpToPx(context, 88) / 2).toFloat()
        mCurrentRadius = mMinRadius
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mMaxRadius = Math.min(w, h) / 2.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = canvas.width
        val height = canvas.height
        if (mCurrentRadius > mMinRadius) { //
            canvas.drawCircle(width / 2.toFloat(), height / 2.toFloat(), mCurrentRadius + 100f, strokePaintColor!!)
        }
    }

    @SuppressLint("WrongConstant")
    fun animateRadius(radius: Float) {
        var radius = radius
        if (radius <= mCurrentRadius) {
            return
        }
        if (radius > mMaxRadius) {
            radius = mMaxRadius
        } else if (radius < mMinRadius) {
            radius = mMinRadius
        }
        if (radius == mCurrentRadius) {
            return
        }
        if (mAnimatorSet.isRunning) {
            mAnimatorSet.cancel()
        }
        val objectAnimator = ObjectAnimator.ofFloat(this, "CurrentRadius", 200f, 250f)
        objectAnimator.repeatMode = Animation.INFINITE
        objectAnimator.repeatCount = Animation.INFINITE
        objectAnimator.addUpdateListener { }
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                visibility = VISIBLE
                strokePaintColor!!.color = Color.argb(100, 255, 0, 0)
            }

            override fun onAnimationEnd(animation: Animator) {
                onAnimationEndTask()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {
                onAnimationEndTask()
            }
        })
        objectAnimator.duration = 1500
        mAnimatorSet.playSequentially(objectAnimator)
        mAnimatorSet.start()
    }

    private fun onAnimationEndTask() {
        visibility = GONE
        strokePaintColor!!.color = Color.argb(20, 128, 128, 128)
    }

    fun getCurrentRadius(): Float {
        return mCurrentRadius
    }

    fun setCurrentRadius(currentRadius: Float) {
        mCurrentRadius = currentRadius
        invalidate()
    }


}