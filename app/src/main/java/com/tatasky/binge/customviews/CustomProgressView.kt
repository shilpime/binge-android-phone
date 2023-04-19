package com.tatasky.binge.customviews

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.tatasky.binge.R
import java.util.*

class CustomProgressView : View {

    private val INDETERMINANT_MIN_SWEEP = 15f

    private var paint: Paint? = null
    private var size = 0
    private var bounds: RectF? = null
    internal var sweepAngle: Float = 0.toFloat()
    internal lateinit var gradientMatrix: Matrix
    internal lateinit var radialGradient: Shader

    private val gradientAngle = 360

    private var isIndeterminate: Boolean = false
    private var autostartAnimation: Boolean = false
    private var currentProgress: Float =
        0.toFloat()
    private var maxProgress: Float = 0.toFloat()
    private var indeterminateSweep: Float = 0.toFloat()
    private var indeterminateRotateOffset: Float = 0.toFloat()
    private var thickness: Int =
        0
    private var color: Int = 0
    private var animDuration: Int = 0
    private var animSwoopDuration: Int = 0
    private var animSyncDuration: Int = 0
    private var animSteps: Int = 0

    private var startAngle: Float = 0.toFloat()
    private var actualProgress: Float = 0.toFloat()
    private var startAngleRotate: ValueAnimator? = null
    private var progressAnimator: ValueAnimator? = null
    private var indeterminateAnimator: AnimatorSet? = null
    private var initialStartAngle: Float = 0.toFloat()

    private var listeners: MutableList<CircularProgressViewListener>? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    protected fun init(attrs: AttributeSet?, defStyle: Int) {
        listeners = ArrayList<CircularProgressViewListener>()

        initAttributes(attrs, defStyle)

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        updatePaint()

        bounds = RectF()
    }

    private fun initAttributes(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CircularProgressView, defStyle, 0
        )

        val resources = resources

        // Initialize attributes from styleable attributes
        currentProgress = a.getFloat(
            R.styleable.CircularProgressView_cpv_progress,
            resources.getInteger(R.integer.cpv_default_progress).toFloat()
        )
        maxProgress = a.getFloat(
            R.styleable.CircularProgressView_cpv_maxProgress,
            resources.getInteger(R.integer.cpv_default_max_progress).toFloat()
        )
        thickness = a.getDimensionPixelSize(
            R.styleable.CircularProgressView_cpv_thickness,
            resources.getDimensionPixelSize(R.dimen.cpv_default_thickness)
        )
        isIndeterminate = a.getBoolean(
            R.styleable.CircularProgressView_cpv_indeterminate,
            resources.getBoolean(R.bool.cpv_default_is_indeterminate)
        )
        autostartAnimation = a.getBoolean(
            R.styleable.CircularProgressView_cpv_animAutostart,
            resources.getBoolean(R.bool.cpv_default_anim_autostart)
        )
        initialStartAngle = a.getFloat(
            R.styleable.CircularProgressView_cpv_startAngle,
            resources.getInteger(R.integer.cpv_default_start_angle).toFloat()
        )
        startAngle = initialStartAngle

        val accentColor =
            context.resources.getIdentifier("colorAccent", "attr", context.packageName)

        // If color explicitly provided
        if (a.hasValue(R.styleable.CircularProgressView_cpv_color)) {
            color =
                a.getColor(
                    R.styleable.CircularProgressView_cpv_color,
                    resources.getColor(R.color.cpv_default_color)
                )
        } else if (accentColor != 0) {
            val t = TypedValue()
            context.theme.resolveAttribute(accentColor, t, true)
            color = t.data
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val t = context.obtainStyledAttributes(intArrayOf(android.R.attr.colorAccent))
            color = t.getColor(0, resources.getColor(R.color.cpv_default_color))
        } else {
            //Use default color
            color = resources.getColor(R.color.cpv_default_color)
        }// If using native accentColor (SDK >21)
        // If using support library v7 accentColor

        animDuration = a.getInteger(
            R.styleable.CircularProgressView_cpv_animDuration,
            resources.getInteger(R.integer.cpv_default_anim_duration)
        )
        animSwoopDuration = a.getInteger(
            R.styleable.CircularProgressView_cpv_animSwoopDuration,
            resources.getInteger(R.integer.cpv_default_anim_swoop_duration)
        )
        animSyncDuration = a.getInteger(
            R.styleable.CircularProgressView_cpv_animSyncDuration,
            resources.getInteger(R.integer.cpv_default_anim_sync_duration)
        )
        animSteps = a.getInteger(
            R.styleable.CircularProgressView_cpv_animSteps,
            resources.getInteger(R.integer.cpv_default_anim_steps)
        )
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val xPad = paddingLeft + paddingRight
        val yPad = paddingTop + paddingBottom
        val width = measuredWidth - xPad
        val height = measuredHeight - yPad
        size = if (width < height) width else height
        setMeasuredDimension(size + xPad, size + yPad)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        size = if (w < h) w else h
        updateBounds()
    }

    private fun updateBounds() {
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        bounds!!.set(
            (paddingLeft + thickness).toFloat(),
            (paddingTop + thickness).toFloat(),
            (size - paddingLeft - thickness).toFloat(),
            (size - paddingTop - thickness).toFloat()
        )
    }

    private fun updatePaint() {
        //        paint.setColor(color);

        //        int[] colors = {getResources().getColor(R.color.end_color), getResources().getColor(R.color.start_color)};

        radialGradient = RadialGradient(
            0f, 0f, 210f,
            resources.getIntArray(R.array.colors), null, Shader.TileMode.MIRROR
        )
        //        LinearGradient linearGradient = new LinearGradient()
        paint!!.shader = radialGradient

        gradientMatrix = Matrix()

        //        Matrix matrix = new Matrix();
        //        matrix.postRotate(90, 100, 100);
        //
        //        float[] positions = {0, 1};
        //
        //        Shader gradient = new SweepGradient(size/2, size/2, colors, positions);
        //        gradient.setLocalMatrix(matrix);
        //
        //        paint.setShader(gradient);

        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = thickness.toFloat()
        paint!!.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the arc
        sweepAngle =
            if (isInEditMode) currentProgress / maxProgress * 360 else actualProgress / maxProgress * 360

        gradientMatrix.postRotate(90f)
        gradientMatrix.postTranslate(1f, 0f)
        radialGradient.setLocalMatrix(gradientMatrix)
        paint!!.shader = radialGradient

        if (!isIndeterminate)
            canvas.drawArc(bounds!!, startAngle, sweepAngle, false, paint!!)
        else
            canvas.drawArc(
                bounds!!,
                startAngle + indeterminateRotateOffset,
                indeterminateSweep,
                false,
                paint!!
            )

        canvas.rotate(sweepAngle)

    }

    /**
     * Returns the mode of this view (determinate or indeterminate).
     * @return true if this view is in indeterminate mode.
     */
    fun isIndeterminate(): Boolean {
        return isIndeterminate
    }

    /**
     * Sets whether this CircularProgressView is indeterminate or not.
     * It will reset the animation if the mode has changed.
     * @param isIndeterminate True if indeterminate.
     */
    fun setIndeterminate(isIndeterminate: Boolean) {
        val old = this.isIndeterminate
        val reset = this.isIndeterminate != isIndeterminate
        this.isIndeterminate = isIndeterminate
        if (reset)
            resetAnimation()
        if (old != isIndeterminate) {
            for (listener in listeners!!) {
                listener.onModeChanged(isIndeterminate)
            }
        }
    }

    /**
     * Get the thickness of the progress bar arc.
     * @return the thickness of the progress bar arc
     */
    fun getThickness(): Int {
        return thickness
    }

    /**
     * Sets the thickness of the progress bar arc.
     * @param thickness the thickness of the progress bar arc
     */
    fun setThickness(thickness: Int) {
        this.thickness = thickness
        updatePaint()
        updateBounds()
        invalidate()
    }

    /**
     *
     * @return the color of the progress bar
     */
    fun getColor(): Int {
        return color
    }

    /**
     * Sets the color of the progress bar.
     * @param color the color of the progress bar
     */
    fun setColor(color: Int) {
        this.color = color
        updatePaint()
        invalidate()
    }

    /**
     * Gets the progress value considered to be 100% of the progress bar.
     * @return the maximum progress
     */
    fun getMaxProgress(): Float {
        return maxProgress
    }

    /**
     * Sets the progress value considered to be 100% of the progress bar.
     * @param maxProgress the maximum progress
     */
    fun setMaxProgress(maxProgress: Float) {
        this.maxProgress = maxProgress
        invalidate()
    }

    /**
     * @return current progress
     */
    fun getProgress(): Float {
        return currentProgress
    }

    /**
     * Sets the progress of the progress bar.
     *
     * @param currentProgress the new progress.
     */
    fun setProgress(currentProgress: Float) {
        this.currentProgress = currentProgress
        // Reset the determinate animation to approach the new currentProgress
        if (!isIndeterminate) {
            if (progressAnimator != null && progressAnimator!!.isRunning)
                progressAnimator!!.cancel()
            progressAnimator = ValueAnimator.ofFloat(actualProgress, currentProgress)
            progressAnimator!!.duration = animSyncDuration.toLong()
            progressAnimator!!.interpolator = LinearInterpolator()
            progressAnimator!!.addUpdateListener { animation ->
                actualProgress = animation.animatedValue as Float
                invalidate()
            }
            progressAnimator!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    for (listener in listeners!!) {
                        listener.onProgressUpdateEnd(currentProgress)
                    }
                }
            })

            progressAnimator!!.start()
        }
        invalidate()
        for (listener in listeners!!) {
            listener.onProgressUpdate(currentProgress)
        }
    }

    /**
     * Register a CircularProgressViewListener with this View
     * @param listener The listener to register
     */
    fun addListener(listener: CircularProgressViewListener?) {
        if (listener != null)
            listeners!!.add(listener)
    }

    /**
     * Unregister a CircularProgressViewListener with this View
     * @param listener The listener to unregister
     */
    fun removeListener(listener: CircularProgressViewListener) {
        listeners!!.remove(listener)
    }

    /**
     * Starts the progress bar animation.
     * (This is an alias of resetAnimation() so it does the same thing.)
     */
    fun startAnimation() {
        resetAnimation()
    }

    /**
     * Resets the animation.
     */
    fun resetAnimation() {
        // Cancel all the old animators
        if (startAngleRotate != null && startAngleRotate!!.isRunning)
            startAngleRotate!!.cancel()
        if (progressAnimator != null && progressAnimator!!.isRunning)
            progressAnimator!!.cancel()
        if (indeterminateAnimator != null && indeterminateAnimator!!.isRunning)
            indeterminateAnimator!!.cancel()

        // Determinate animation
        if (!isIndeterminate) {
            // The cool 360 swoop animation at the start of the animation
            startAngle = initialStartAngle
            startAngleRotate = ValueAnimator.ofFloat(startAngle, startAngle + 360)
            startAngleRotate!!.duration = animSwoopDuration.toLong()
            startAngleRotate!!.interpolator = DecelerateInterpolator(2f)
            startAngleRotate!!.addUpdateListener { animation ->
                startAngle = animation.animatedValue as Float
                invalidate()
            }
            startAngleRotate!!.start()

            // The linear animation shown when progress is updated
            actualProgress = 0f
            progressAnimator = ValueAnimator.ofFloat(actualProgress, currentProgress)
            progressAnimator!!.duration = animSyncDuration.toLong()
            progressAnimator!!.interpolator = LinearInterpolator()
            progressAnimator!!.addUpdateListener { animation ->
                actualProgress = animation.animatedValue as Float
                invalidate()
            }
            progressAnimator!!.start()
        } else {
            indeterminateSweep = INDETERMINANT_MIN_SWEEP
            // Build the whole AnimatorSet
            indeterminateAnimator = AnimatorSet()
            var prevSet: AnimatorSet? = null
            var nextSet: AnimatorSet
            for (k in 0 until animSteps) {
                nextSet = createIndeterminateAnimator(k.toFloat())
                val builder = indeterminateAnimator!!.play(nextSet)
                if (prevSet != null)
                    builder.after(prevSet)
                prevSet = nextSet
            }

            // Listen to end of animation so we can infinitely loop
            indeterminateAnimator!!.addListener(object : AnimatorListenerAdapter() {
                internal var wasCancelled = false
                override fun onAnimationCancel(animation: Animator) {
                    wasCancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!wasCancelled)
                        resetAnimation()
                }
            })
            indeterminateAnimator!!.start()
            for (listener in listeners!!) {
                listener.onAnimationReset()
            }
        }// Indeterminate animation


    }

    /**
     * Stops the animation
     */

    fun stopAnimation() {
        if (startAngleRotate != null) {
            startAngleRotate!!.cancel()
            startAngleRotate = null
        }
        if (progressAnimator != null) {
            progressAnimator!!.cancel()
            progressAnimator = null
        }
        if (indeterminateAnimator != null) {
            indeterminateAnimator!!.cancel()
            indeterminateAnimator = null
        }
    }

    // Creates the animators for one step of the animation
    private fun createIndeterminateAnimator(step: Float): AnimatorSet {
        val maxSweep = 360f * (animSteps - 1) / animSteps + INDETERMINANT_MIN_SWEEP
        val start = -90f + step * (maxSweep - INDETERMINANT_MIN_SWEEP)

        // Extending the front of the arc
        val frontEndExtend = ValueAnimator.ofFloat(INDETERMINANT_MIN_SWEEP, maxSweep)
        frontEndExtend.duration = (animDuration / animSteps / 2).toLong()
        frontEndExtend.interpolator = DecelerateInterpolator(1f)
        frontEndExtend.addUpdateListener { animation ->
            indeterminateSweep = animation.animatedValue as Float
            invalidate()
        }

        // Overall rotation
        val rotateAnimator1 =
            ValueAnimator.ofFloat(step * 720f / animSteps, (step + .5f) * 720f / animSteps)
        rotateAnimator1.duration = (animDuration / animSteps / 2).toLong()
        rotateAnimator1.interpolator = LinearInterpolator()
        rotateAnimator1.addUpdateListener { animation ->
            indeterminateRotateOffset = animation.animatedValue as Float
        }

        // Followed by...

        // Retracting the back end of the arc
        val backEndRetract =
            ValueAnimator.ofFloat(start, start + maxSweep - INDETERMINANT_MIN_SWEEP)
        backEndRetract.duration = (animDuration / animSteps / 2).toLong()
        backEndRetract.interpolator = DecelerateInterpolator(1f)
        backEndRetract.addUpdateListener { animation ->
            startAngle = animation.animatedValue as Float
            indeterminateSweep = maxSweep - startAngle + start
            invalidate()
        }

        // More overall rotation
        val rotateAnimator2 =
            ValueAnimator.ofFloat((step + .5f) * 720f / animSteps, (step + 1) * 720f / animSteps)
        rotateAnimator2.duration = (animDuration / animSteps / 2).toLong()
        rotateAnimator2.interpolator = LinearInterpolator()
        rotateAnimator2.addUpdateListener { animation ->
            indeterminateRotateOffset = animation.animatedValue as Float
        }

        val set = AnimatorSet()
        set.play(frontEndExtend).with(rotateAnimator1)
        set.play(backEndRetract).with(rotateAnimator2).after(rotateAnimator1)
        return set
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (autostartAnimation)
            startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    override fun setVisibility(visibility: Int) {
        val currentVisibility = getVisibility()
        super.setVisibility(visibility)
        if (visibility != currentVisibility) {
            if (visibility == View.VISIBLE) {
                resetAnimation()
            } else if (visibility == View.GONE || visibility == View.INVISIBLE) {
                stopAnimation()
            }
        }
    }
}