package com.tatasky.binge.ui.base.frameworks.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tatasky.binge.R
import com.tatasky.binge.customviews.CustomTextInputLayout
import kotlin.math.hypot


/**
 * Visibility modifiers and check functions
 */

fun View.isVisibile(): Boolean = this.visibility == View.VISIBLE

val TextView.maxLength: Int?
    get() = filters.filterIsInstance<InputFilter.LengthFilter>().firstOrNull()?.max

/**
 * Sets text and content description using same string
 */
fun TextView.setTextWithContentDescription(value: String?) {
    text = value
    contentDescription = value
}

/**
 * Button enabling/disabling modifiers
 */

fun View.disable() {
    isEnabled = false
    alpha = 0.3f
}

fun View.enable() {
    isEnabled = true
    alpha = 1.0f
}

fun View.disableWithoutAlpha() {
    isEnabled = false
    alpha = 1.0f
}

/**
 * Sets color to status bar
 */
fun Window.addStatusBarColor(@ColorRes color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        this.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        this.statusBarColor = ContextCompat.getColor(this.context, color)
    }
}

fun View.showKeyboard() {
    if (this.requestFocus()) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.closeKeyboard() {
    val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    keyboard.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.hide() {
    if (this.visibility != View.GONE)
        this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    if (this.visibility != View.VISIBLE)
        this.visibility = View.VISIBLE
}

fun View.hideKeyboardOnOutsideTouch() {
    this.setOnTouchListener { _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> closeKeyboard()
        }
        false
    }
}

fun View.hideSlideUp() {
    val animate = TranslateAnimation(
        0f, // fromXDelta
        0f, // toXDelta
        0f, // fromYDelta
        -height.toFloat()
    )                // toYDelta
    animate.duration = 500
    animate.fillAfter = true
    animate.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {

        }

        override fun onAnimationStart(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            visibility = View.GONE
        }
    })
    startAnimation(animate)
}

fun View.showSlideDown() {
    visibility = View.VISIBLE
    val animate = TranslateAnimation(
        0f, // fromXDelta
        0f, // toXDelta
        -height.toFloat(), // fromYDelta
        0f // toYDelta
    )
    animate.duration = 500
    animate.fillAfter = true
    startAnimation(animate)
}

fun View.hideSlideDown() {
    if (this.visibility == View.VISIBLE) {
        val animate = TranslateAnimation(
            0f, // fromXDelta
            0f, // toXDelta
            0f, // fromYDelta
            height.toFloat()
        ) // toYDelta
        animate.duration = 250
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                visibility = View.GONE
            }
        })
        post { startAnimation(animate) }
    }
}

fun View.showSlideUp() {
    if (visibility == View.GONE) {
        alpha = 0f
        visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0f, // fromXDelta
            0f, // toXDelta
            height.toFloat(), // fromYDelta
            0f
        ) // toYDelta
        animate.duration = 300
        post {
            animate().alpha(1f).setDuration(300).setListener(null).start()
            startAnimation(animate)
        }
    }
}

fun View.hideSlideLeft() {
    clearAnimation()
    if (this.visibility == View.VISIBLE) {
        val animate = TranslateAnimation(
            0f, // fromXDelta
            -width.toFloat(), // toXDelta
            0f, // fromYDelta
            0f
        ) // toYDelta
        animate.duration = 300
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                visibility = View.GONE
            }
        })
        animate.interpolator = AccelerateDecelerateInterpolator()
        startAnimation(animate)
    }
}

fun View.hideSlideRight() {
    clearAnimation()
    if (this.visibility == View.VISIBLE) {
        val animate = TranslateAnimation(
            0f, // fromXDelta
            width.toFloat(), // toXDelta
            0f, // fromYDelta
            0f
        ) // toYDelta
        animate.duration = 300
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                visibility = View.GONE
            }
        })
        animate.interpolator = AccelerateDecelerateInterpolator()
        startAnimation(animate)
    }
}
fun View.showSlideLeft() {
    clearAnimation()
    if (visibility == View.GONE) {
        alpha = 0f
        visibility = View.VISIBLE
        val animate = TranslateAnimation(
            width.toFloat(), // fromXDelta
            0f, // toXDelta
            0f, // fromYDelta
            0f
        ) // toYDelta
        animate.duration = 300
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
                visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                visibility = View.VISIBLE
            }
        })
        animate.interpolator = AccelerateDecelerateInterpolator()
        animate().alpha(1f).setDuration(300).setListener(null).start()
        startAnimation(animate)
    }
}


fun View.showSlideRight() {
    clearAnimation()
    if (visibility == View.GONE) {
        alpha = 0f
        visibility = View.VISIBLE
        val animate = TranslateAnimation(
            -width.toFloat(), // fromXDelta
            0f, // toXDelta
            0f, // fromYDelta
            0f
        ) // toYDelta
        animate.duration = 300
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
                visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                visibility = View.VISIBLE
            }
        })
        animate.interpolator = AccelerateDecelerateInterpolator()
        animate().alpha(1f).setDuration(300).setListener(null).start()
        startAnimation(animate)
    }
}

fun View.showCircularReveal() {
    if (visibility == View.GONE) {
        visibility = View.VISIBLE
        val animate = ViewAnimationUtils.createCircularReveal(
            this, this.width / 2, this.height / 2, 0f,
            hypot(this.width.toDouble(), this.height.toDouble()).toFloat()
        )
        animate.duration = 250
        animate.start()
//        startAnimation(animate)
    }
}

fun CustomTextInputLayout.clearError(){
    this.error = null
}

fun View.animatePackItemScaling() {
    val scaleInXAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 1.1f)
    scaleInXAnimator.duration=250
    val scaleOutXAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.1f, 1.0f)
    scaleOutXAnimator.duration=250
    val animationSet = AnimatorSet()
    animationSet.playSequentially(
        scaleInXAnimator,
        scaleOutXAnimator
    )
    animationSet.duration = 500
    animationSet.start()
}
fun ImageView.startProgressAvd(enabled: Boolean) {
    if (enabled) {
        show()
//        val animated = AnimatedVectorDrawableCompat.create(context, R.drawable.progress_avd)
//        animated?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
//            override fun onAnimationEnd(drawable: Drawable?) {
//                this@startProgressAvd.post { animated.start() }
//            }
//        })
//        this.setImageDrawable(animated)
//        animated?.start()

        val animatedVector = AnimatedVectorDrawableCompat.create(context, R.drawable.progress_avd)
        this.setImageDrawable(animatedVector)
        val mainHandler = Handler(Looper.getMainLooper())
        animatedVector?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable) {
                mainHandler.post(Runnable { animatedVector.start() })
            }
        })
        animatedVector?.start()
    } else {
        hide()
    }
}

fun ImageView.startAvd(enabled: Boolean) {
    if (enabled) {
        show()
        if(drawable is AnimatedVectorDrawable)
            (drawable as AnimatedVectorDrawable).start()
    } else {
        hide()
    }
}

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun BottomNavigationView.changeNavTypeface(typeface: Typeface) {
    val view: View = this
    checker(view, typeface)
}

private fun checker(view: View, typeface: Typeface) {
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            checker(child, typeface)
        }
    } else if (view is TextView && view.text.toString().equals("kids", true)) {
        view.typeface = typeface
    }
}

inline fun <T: View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

fun View.isVisibleOnScreen() : Boolean {
    try {
        if (!isShown) {
            return false
        }
        val actualPosition = Rect()
        val isGlobalVisible = getGlobalVisibleRect(actualPosition)
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        val screen = Rect(0, 0, screenWidth, screenHeight)
        return isGlobalVisible && Rect.intersects(actualPosition, screen)
    }catch (e:Exception){
        return false
    }
}

fun View.visiblePercentage() : Float {
    return try {
        val visibleRect = Rect()
        this.getGlobalVisibleRect(visibleRect)
        val visiblePercentage =
            ((visibleRect.height() * visibleRect.width()) / (height * width).toFloat() * 100)
        String.format("%.2f", visiblePercentage).toFloat()
    }
    catch (e:Exception){
        0f
    }
}

fun View.isFullyVisibleOnScreen() = (visiblePercentage() == 100.00f)
