package com.tatasky.binge.customviews

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Util
import com.tatasky.binge.R
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

class TimeBarCustom : View, TimeBar{

    /**
     * Default height for the time bar, in dp.
     */
    val DEFAULT_BAR_HEIGHT_DP = 4

    /**
     * Default height for the touch target, in dp.
     */
    val DEFAULT_TOUCH_TARGET_HEIGHT_DP = 26

    /**
     * Default width for ad markers, in dp.
     */
    val DEFAULT_AD_MARKER_WIDTH_DP = 4

    /**
     * Default diameter for the scrubber when enabled, in dp.
     */
    val DEFAULT_SCRUBBER_ENABLED_SIZE_DP = 12

    /**
     * Default diameter for the scrubber when disabled, in dp.
     */
    val DEFAULT_SCRUBBER_DISABLED_SIZE_DP = 0

    /**
     * Default diameter for the scrubber when dragged, in dp.
     */
    val DEFAULT_SCRUBBER_DRAGGED_SIZE_DP = 16

    /**
     * Default color for the played portion of the time bar.
     */
    val DEFAULT_PLAYED_COLOR = -0x1

    /**
     * Default color for the unplayed portion of the time bar.
     */
    val DEFAULT_UNPLAYED_COLOR = 0x33FFFFFF

    /**
     * Default color for the buffered portion of the time bar.
     */
    val DEFAULT_BUFFERED_COLOR = -0x33000001

    /**
     * Default color for the scrubber handle.
     */
    val DEFAULT_SCRUBBER_COLOR = -0x1

    /**
     * Default color for ad markers.
     */
    val DEFAULT_AD_MARKER_COLOR = -0x4d000100

    /**
     * Default color for played ad markers.
     */
    val DEFAULT_PLAYED_AD_MARKER_COLOR = 0x33FFFF00

    /**
     * The threshold in dps above the bar at which touch events trigger fine scrub mode.
     */
    private val FINE_SCRUB_Y_THRESHOLD_DP = -50

    /**
     * The ratio by which times are reduced in fine scrub mode.
     */
    private val FINE_SCRUB_RATIO = 3

    /**
     * The time after which the scrubbing listener is notified that scrubbing has stopped after
     * performing an incremental scrub using key input.
     */
    private val STOP_SCRUBBING_TIMEOUT_MS: Long = 1000

    private val DEFAULT_INCREMENT_COUNT = 20

    /**
     * The name of the Android SDK view that most closely resembles this custom view. Used as the
     * class name for accessibility.
     */
    private val ACCESSIBILITY_CLASS_NAME = "android.widget.SeekBar"

    private lateinit var seekBounds: Rect
    private lateinit var progressBar: Rect
    private lateinit var bufferedBar: Rect
    private lateinit var scrubberBar: Rect
    private lateinit var playedPaint: Paint
    private lateinit var bufferedPaint: Paint
    private lateinit var unplayedPaint: Paint
    private lateinit var adMarkerPaint: Paint
    private lateinit var playedAdMarkerPaint: Paint
    private lateinit var scrubberPaint: Paint
    private var scrubberDrawable: Drawable? = null
    private var barHeight = 0
    private var touchTargetHeight = 0
    private var adMarkerWidth = 0
    private var scrubberEnabledSize = 0
    private var scrubberDisabledSize = 0
    private var scrubberDraggedSize = 0
    private var scrubberPadding = 0
    private var fineScrubYThreshold = 0
    private var formatBuilder: StringBuilder? = null
    private var formatter: Formatter? = null
    private var stopScrubbingRunnable: Runnable? = null
    private var listeners: CopyOnWriteArraySet<TimeBar.OnScrubListener>? = null
    private var locationOnScreen: IntArray = IntArray(2)
    private var touchPosition: Point? = null
    private var density = 0f

    private var keyCountIncrement = 0
    private var keyTimeIncrement: Long = 0
    private var lastCoarseScrubXPosition = 0
    private var lastExclusionRectangle: Rect? = null

    private var scrubbing = false
    private var scrubPosition: Long = 0
    private var duration: Long = 0
    private var position: Long = 0
    private var bufferedPosition: Long = 0
    private var adGroupCount = 0
    private var adGroupTimesMs: LongArray? = null
    private var playedAdGroups: BooleanArray? = null

    constructor(context: Context?) : super(context, null){
        init(context, null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?):super(context, attrs, 0) {
        init(context, attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :this(
        context,
        attrs,
        defStyleAttr,
        attrs
    ){
        init(context, attrs, defStyleAttr)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        timebarAttrs: AttributeSet?
    ) :super(context, attrs, defStyleAttr) {
        init(context, timebarAttrs, defStyleAttr)
    }

    fun init(context:Context?, timebarAttrs:AttributeSet?, defStyleAttr: Int){
        seekBounds = Rect()
        progressBar = Rect()
        bufferedBar = Rect()
        scrubberBar = Rect()
        playedPaint = Paint()
        bufferedPaint = Paint()
        unplayedPaint = Paint()
        adMarkerPaint = Paint()
        playedAdMarkerPaint = Paint()
        scrubberPaint = Paint()
        scrubberPaint.setAntiAlias(true)
        listeners = CopyOnWriteArraySet()
        locationOnScreen = IntArray(2)
        touchPosition = Point()

        // Calculate the dimensions and paints for drawn elements.
        val res = context?.resources
        val displayMetrics = res?.displayMetrics
        density = displayMetrics?.density ?: 0f
        fineScrubYThreshold = dpToPx(density, FINE_SCRUB_Y_THRESHOLD_DP)
        val defaultBarHeight: Int =
            dpToPx(density, DEFAULT_BAR_HEIGHT_DP)
        var defaultTouchTargetHeight: Int =
            dpToPx(density, DEFAULT_TOUCH_TARGET_HEIGHT_DP)
        val defaultAdMarkerWidth: Int =
            dpToPx(density, DEFAULT_AD_MARKER_WIDTH_DP)
        val defaultScrubberEnabledSize: Int =
            dpToPx(density, DEFAULT_SCRUBBER_ENABLED_SIZE_DP)
        val defaultScrubberDisabledSize: Int =
            dpToPx(density, DEFAULT_SCRUBBER_DISABLED_SIZE_DP)
        val defaultScrubberDraggedSize: Int =
            dpToPx(density, DEFAULT_SCRUBBER_DRAGGED_SIZE_DP)
        if (timebarAttrs != null) {
            val a =
                context?.theme?.obtainStyledAttributes(
                    timebarAttrs,
                    R.styleable.DefaultTimeBar,
                    0,
                    0
                )
            if(a!= null) {
                try {
                    scrubberDrawable =
                        a.getDrawable(R.styleable.DefaultTimeBar_scrubber_drawable)
                    setDrawableLayoutDirection(scrubberDrawable)
                    defaultTouchTargetHeight =
                        (scrubberDrawable?.minimumHeight ?: Int.MAX_VALUE).coerceAtLeast(defaultTouchTargetHeight)
                    barHeight = a.getDimensionPixelSize(
                        R.styleable.DefaultTimeBar_bar_height,
                        defaultBarHeight
                    )
                    touchTargetHeight = a.getDimensionPixelSize(
                        R.styleable.DefaultTimeBar_touch_target_height,
                        defaultTouchTargetHeight
                    )
                    adMarkerWidth = a.getDimensionPixelSize(
                        R.styleable.DefaultTimeBar_ad_marker_width,
                        defaultAdMarkerWidth
                    )
                    scrubberEnabledSize = a.getDimensionPixelSize(
                        R.styleable.DefaultTimeBar_scrubber_enabled_size, defaultScrubberEnabledSize
                    )
                    scrubberDisabledSize = a.getDimensionPixelSize(
                        R.styleable.DefaultTimeBar_scrubber_disabled_size,
                        defaultScrubberDisabledSize
                    )
                    scrubberDraggedSize = a.getDimensionPixelSize(
                        R.styleable.DefaultTimeBar_scrubber_dragged_size, defaultScrubberDraggedSize
                    )
                    val playedColor = a.getInt(
                        R.styleable.DefaultTimeBar_played_color,
                        DEFAULT_PLAYED_COLOR
                    )
                    val scrubberColor = a.getInt(
                        R.styleable.DefaultTimeBar_scrubber_color,
                        DEFAULT_SCRUBBER_COLOR
                    )
                    val bufferedColor = a.getInt(
                        R.styleable.DefaultTimeBar_buffered_color,
                        DEFAULT_BUFFERED_COLOR
                    )
                    val unplayedColor = a.getInt(
                        R.styleable.DefaultTimeBar_unplayed_color,
                        DEFAULT_UNPLAYED_COLOR
                    )
                    val adMarkerColor = a.getInt(
                        R.styleable.DefaultTimeBar_ad_marker_color,
                        DEFAULT_AD_MARKER_COLOR
                    )
                    val playedAdMarkerColor = a.getInt(
                        R.styleable.DefaultTimeBar_played_ad_marker_color,
                        DEFAULT_PLAYED_AD_MARKER_COLOR
                    )
                    playedPaint.setColor(playedColor)
                    scrubberPaint.setColor(scrubberColor)
                    bufferedPaint.setColor(bufferedColor)
                    unplayedPaint.setColor(unplayedColor)
                    adMarkerPaint.setColor(adMarkerColor)
                    playedAdMarkerPaint.setColor(playedAdMarkerColor)
                } finally {
                    a.recycle()
                }
            }
        } else {
            barHeight = defaultBarHeight
            touchTargetHeight = defaultTouchTargetHeight
            adMarkerWidth = defaultAdMarkerWidth
            scrubberEnabledSize = defaultScrubberEnabledSize
            scrubberDisabledSize = defaultScrubberDisabledSize
            scrubberDraggedSize = defaultScrubberDraggedSize
            playedPaint.setColor(DEFAULT_PLAYED_COLOR)
            scrubberPaint.setColor(DEFAULT_SCRUBBER_COLOR)
            bufferedPaint.setColor(DEFAULT_BUFFERED_COLOR)
            unplayedPaint.setColor(DEFAULT_UNPLAYED_COLOR)
            adMarkerPaint.setColor(DEFAULT_AD_MARKER_COLOR)
            playedAdMarkerPaint.setColor(DEFAULT_PLAYED_AD_MARKER_COLOR)
            scrubberDrawable = null
        }
        formatBuilder = java.lang.StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        stopScrubbingRunnable = Runnable { stopScrubbing( /* canceled= */false) }
        scrubberPadding = if (scrubberDrawable != null) {
            (scrubberDrawable?.getMinimumWidth() ?: 0 + 1) / 2
        } else {
            ((Math.max(
                scrubberDisabledSize,
                Math.max(scrubberEnabledSize, scrubberDraggedSize)
            ) + 1)
                    / 2)
        }
        duration = C.TIME_UNSET
        keyTimeIncrement = C.TIME_UNSET
        keyCountIncrement = DEFAULT_INCREMENT_COUNT
        isFocusable = true
        if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }
    }

    /**
     * Sets the color for the portion of the time bar representing media before the playback position.
     *
     * @param playedColor The color for the portion of the time bar representing media before the
     * playback position.
     */
    fun setPlayedColor(@ColorInt playedColor: Int) {
        playedPaint!!.color = playedColor
        invalidate(seekBounds)
    }

    /**
     * Sets the color for the scrubber handle.
     *
     * @param scrubberColor The color for the scrubber handle.
     */
    fun setScrubberColor(@ColorInt scrubberColor: Int) {
        scrubberPaint.color = scrubberColor
        invalidate(seekBounds)
    }

    /**
     * Sets the color for the portion of the time bar after the current played position up to the
     * current buffered position.
     *
     * @param bufferedColor The color for the portion of the time bar after the current played
     * position up to the current buffered position.
     */
    fun setBufferedColor(@ColorInt bufferedColor: Int) {
        bufferedPaint!!.color = bufferedColor
        invalidate(seekBounds)
    }

    /**
     * Sets the color for the portion of the time bar after the current played position.
     *
     * @param unplayedColor The color for the portion of the time bar after the current played
     * position.
     */
    fun setUnplayedColor(@ColorInt unplayedColor: Int) {
        unplayedPaint!!.color = unplayedColor
        invalidate(seekBounds)
    }

    /**
     * Sets the color for unplayed ad markers.
     *
     * @param adMarkerColor The color for unplayed ad markers.
     */
    fun setAdMarkerColor(@ColorInt adMarkerColor: Int) {
        adMarkerPaint!!.color = adMarkerColor
        invalidate(seekBounds)
    }

    /**
     * Sets the color for played ad markers.
     *
     * @param playedAdMarkerColor The color for played ad markers.
     */
    fun setPlayedAdMarkerColor(@ColorInt playedAdMarkerColor: Int) {
        playedAdMarkerPaint!!.color = playedAdMarkerColor
        invalidate(seekBounds)
    }

    override fun setKeyTimeIncrement(time: Long) {
        Assertions.checkArgument(time > 0)
        keyCountIncrement = C.INDEX_UNSET
        keyTimeIncrement = time
    }

    override fun setKeyCountIncrement(count: Int) {
        Assertions.checkArgument(count > 0)
        keyCountIncrement = count
        keyTimeIncrement = C.TIME_UNSET
    }

    override fun setPosition(position: Long) {
        this.position = position
        contentDescription = getProgressText()
        update()
    }

    override fun setBufferedPosition(bufferedPosition: Long) {
        this.bufferedPosition = bufferedPosition
        update()
    }

    override fun setDuration(duration: Long) {
        this.duration = duration
        if (scrubbing && duration == C.TIME_UNSET) {
            stopScrubbing( /* canceled= */true)
        }
        update()
    }

    override fun getPreferredUpdateDelay(): Long {
        val timeBarWidthDp = pxToDp(density, progressBar!!.width())
        return if (timeBarWidthDp == 0 || duration == 0L || duration == C.TIME_UNSET) Long.MAX_VALUE else duration / timeBarWidthDp
    }

    override fun setAdGroupTimesMs(
        adGroupTimesMs: LongArray?, playedAdGroups: BooleanArray?,
        adGroupCount: Int
    ) {
        Assertions.checkArgument(
            adGroupCount == 0
                    || adGroupTimesMs != null && playedAdGroups != null
        )
        this.adGroupCount = adGroupCount
        this.adGroupTimesMs = adGroupTimesMs
        this.playedAdGroups = playedAdGroups
        update()
    }

    override fun addListener(listener: TimeBar.OnScrubListener) {
        listeners?.add(listener)
    }

    override fun removeListener(listener: TimeBar.OnScrubListener) {
        listeners?.remove(listener)
    }

    // View methods.

    // View methods.
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (scrubbing && !enabled) {
            stopScrubbing( /* canceled= */true)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        drawTimeBar(canvas)
        drawPlayhead(canvas)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || duration <= 0) {
            return false
        }
        val touchPosition = resolveRelativeTouchPosition(event)
        val x = touchPosition.x
        val y = touchPosition.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (isInSeekBar(x.toFloat(), y.toFloat())) {
                positionScrubber(x.toFloat())
                startScrubbing(getScrubberPosition())
                update()
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> if (scrubbing) {
                if (y < fineScrubYThreshold) {
                    val relativeX = x - lastCoarseScrubXPosition.toFloat()
                    positionScrubber(lastCoarseScrubXPosition + relativeX / FINE_SCRUB_RATIO)
                } else {
                    lastCoarseScrubXPosition = x
                    positionScrubber(x.toFloat())
                }
                updateScrubbing(getScrubberPosition())
                update()
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (scrubbing) {
                stopScrubbing( /* canceled= */event.action == MotionEvent.ACTION_CANCEL)
                return true
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isEnabled) {
            var positionIncrement = getPositionIncrement()
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    positionIncrement = -positionIncrement
                    if (scrubIncrementally(positionIncrement)) {
                        removeCallbacks(stopScrubbingRunnable)
                        postDelayed(stopScrubbingRunnable, STOP_SCRUBBING_TIMEOUT_MS)
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> if (scrubIncrementally(positionIncrement)) {
                    removeCallbacks(stopScrubbingRunnable)
                    postDelayed(stopScrubbingRunnable, STOP_SCRUBBING_TIMEOUT_MS)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> if (scrubbing) {
                    stopScrubbing( /* canceled= */false)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onFocusChanged(
        gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?
    ) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (scrubbing && !gainFocus) {
            stopScrubbing( /* canceled= */false)
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateDrawableState()
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        if (scrubberDrawable != null) {
            scrubberDrawable!!.jumpToCurrentState()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val height =
            if (heightMode == MeasureSpec.UNSPECIFIED) touchTargetHeight else if (heightMode == MeasureSpec.EXACTLY) heightSize else Math.min(
                touchTargetHeight,
                heightSize
            )
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
        updateDrawableState()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        val barY = (height - touchTargetHeight) / 2
        val seekLeft = paddingLeft
        val seekRight = width - paddingRight
        val progressY = barY + (touchTargetHeight - barHeight) / 2
        seekBounds!![seekLeft, barY, seekRight] = barY + touchTargetHeight
        progressBar!![seekBounds!!.left + scrubberPadding, progressY, seekBounds!!.right - scrubberPadding] =
            progressY + barHeight
        if (Util.SDK_INT >= 29) {
            setSystemGestureExclusionRectsV29(width, height)
        }
        update()
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        if (scrubberDrawable != null && setDrawableLayoutDirection(
                scrubberDrawable,
                layoutDirection
            )
        ) {
            invalidate()
        }
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SELECTED) {
            event.text.add(getProgressText())
        }
        event.className = ACCESSIBILITY_CLASS_NAME
    }

    @TargetApi(21)
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = ACCESSIBILITY_CLASS_NAME
        info.contentDescription = getProgressText()
        if (duration <= 0) {
            return
        }
        if (Util.SDK_INT >= 21) {
            info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD)
            info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD)
        } else {
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }
    }

    override fun performAccessibilityAction(action: Int, args: Bundle?): Boolean {
        if (super.performAccessibilityAction(action, args)) {
            return true
        }
        if (duration <= 0) {
            return false
        }
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            if (scrubIncrementally(-getPositionIncrement())) {
                stopScrubbing( /* canceled= */false)
            }
        } else if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
            if (scrubIncrementally(getPositionIncrement())) {
                stopScrubbing( /* canceled= */false)
            }
        } else {
            return false
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
        return true
    }

    // Internal methods.

    // Internal methods.
    private fun startScrubbing(scrubPosition: Long) {
        this.scrubPosition = scrubPosition
        scrubbing = true
        isPressed = true
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(true)
        for (listener: TimeBar.OnScrubListener in listeners!!) {
            listener.onScrubStart(this, scrubPosition)
        }
    }

    private fun updateScrubbing(scrubPosition: Long) {
        if (this.scrubPosition == scrubPosition) {
            return
        }
        this.scrubPosition = scrubPosition
        for (listener: TimeBar.OnScrubListener in listeners!!) {
            listener.onScrubMove(this, scrubPosition)
        }
    }

    private fun stopScrubbing(canceled: Boolean) {
        removeCallbacks(stopScrubbingRunnable)
        scrubbing = false
        isPressed = false
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(false)
        invalidate()
        for (listener: TimeBar.OnScrubListener in listeners!!) {
            listener.onScrubStop(this, scrubPosition, canceled)
        }
    }

    /**
     * Incrementally scrubs the position by `positionChange`.
     *
     * @param positionChange The change in the scrubber position, in milliseconds. May be negative.
     * @return Returns whether the scrubber position changed.
     */
    private fun scrubIncrementally(positionChange: Long): Boolean {
        if (duration <= 0) {
            return false
        }
        val previousPosition = if (scrubbing) scrubPosition else position
        val scrubPosition = Util.constrainValue(previousPosition + positionChange, 0, duration)
        if (scrubPosition == previousPosition) {
            return false
        }
        if (!scrubbing) {
            startScrubbing(scrubPosition)
        } else {
            updateScrubbing(scrubPosition)
        }
        update()
        return true
    }

    private fun update() {
        bufferedBar!!.set(progressBar)
        scrubberBar!!.set(progressBar)
        val newScrubberTime = if (scrubbing) scrubPosition else position
        if (duration > 0) {
            val bufferedPixelWidth = ((progressBar!!.width() * bufferedPosition) / duration).toInt()
            bufferedBar!!.right =
                Math.min(progressBar!!.left + bufferedPixelWidth, progressBar!!.right)
            val scrubberPixelPosition =
                ((progressBar!!.width() * newScrubberTime) / duration).toInt()
            scrubberBar!!.right =
                Math.min(progressBar!!.left + scrubberPixelPosition, progressBar!!.right)
        } else {
            bufferedBar!!.right = progressBar!!.left
            scrubberBar!!.right = progressBar!!.left
        }
        invalidate(seekBounds)
    }

    private fun positionScrubber(xPosition: Float) {
        scrubberBar!!.right =
            Util.constrainValue(xPosition.toInt(), progressBar!!.left, progressBar!!.right)
    }

    private fun resolveRelativeTouchPosition(motionEvent: MotionEvent): Point {
        getLocationOnScreen(locationOnScreen)
        touchPosition!![(motionEvent.rawX.toInt()) - locationOnScreen[0]] =
            (motionEvent.rawY.toInt()) - locationOnScreen[1]
        return (touchPosition)!!
    }

    private fun getScrubberPosition(): Long {
        return if (progressBar!!.width() <= 0 || duration == C.TIME_UNSET) {
            0
        } else (scrubberBar!!.width() * duration) / progressBar!!.width()
    }

    private fun isInSeekBar(x: Float, y: Float): Boolean {
        return seekBounds!!.contains(x.toInt(), y.toInt())
    }

    private fun drawTimeBar(canvas: Canvas) {
        val progressBarHeight = progressBar!!.height()
        val barTop = progressBar!!.centerY() - progressBarHeight / 2
        val barBottom = barTop + progressBarHeight
        if (duration <= 0) {
            canvas.drawRect(
                progressBar!!.left.toFloat(),
                barTop.toFloat(),
                progressBar!!.right.toFloat(),
                barBottom.toFloat(),
                (unplayedPaint)!!
            )
            return
        }
        var bufferedLeft = bufferedBar!!.left
        val bufferedRight = bufferedBar!!.right
        val progressLeft =
            Math.max(Math.max(progressBar!!.left, bufferedRight), scrubberBar!!.right)
        if (progressLeft < progressBar!!.right) {
            canvas.drawRect(
                progressLeft.toFloat(),
                barTop.toFloat(),
                progressBar!!.right.toFloat(),
                barBottom.toFloat(),
                (unplayedPaint)!!
            )
        }
        bufferedLeft = Math.max(bufferedLeft, scrubberBar!!.right)
        if (bufferedRight > bufferedLeft) {
            canvas.drawRect(
                bufferedLeft.toFloat(),
                barTop.toFloat(),
                bufferedRight.toFloat(),
                barBottom.toFloat(),
                (bufferedPaint)!!
            )
        }
        if (scrubberBar!!.width() > 0) {
            canvas.drawRect(
                scrubberBar!!.left.toFloat(),
                barTop.toFloat(),
                scrubberBar!!.right.toFloat(),
                barBottom.toFloat(),
                (playedPaint)!!
            )
        }
        if (adGroupCount == 0) {
            return
        }
        val adGroupTimesMs = Assertions.checkNotNull(adGroupTimesMs)
        val playedAdGroups = Assertions.checkNotNull(playedAdGroups)
        val adMarkerOffset = adMarkerWidth / 2
        for (i in 0 until adGroupCount) {
            val adGroupTimeMs = Util.constrainValue(adGroupTimesMs[i], 0, duration)
            val markerPositionOffset =
                (progressBar!!.width() * adGroupTimeMs / duration).toInt() - adMarkerOffset
            val markerLeft = progressBar!!.left + Math.min(
                progressBar!!.width() - adMarkerWidth,
                Math.max(0, markerPositionOffset)
            )
            val paint = if (playedAdGroups[i]) (playedAdMarkerPaint)!! else (adMarkerPaint)!!
            canvas.drawRect(
                markerLeft.toFloat(),
                barTop.toFloat(),
                markerLeft + adMarkerWidth.toFloat(),
                barBottom.toFloat(),
                paint
            )
        }
    }

    private fun drawPlayhead(canvas: Canvas) {
        if (duration <= 0) {
            return
        }
        var playheadX =
            Util.constrainValue(scrubberBar!!.right, scrubberBar!!.left, progressBar!!.right)
        val playheadY = scrubberBar!!.centerY()
        if (scrubberDrawable == null) {
            val scrubberSize =
                if ((scrubbing || isFocused)) scrubberDraggedSize else (if (isEnabled) scrubberEnabledSize else scrubberDisabledSize)
            val playheadRadius = scrubberSize / 2
            if (playheadX < progressBar!!.left + playheadRadius) {
                playheadX = progressBar!!.left + playheadRadius
            }
            if (playheadX > progressBar!!.right - playheadRadius) {
                playheadX = progressBar!!.right - playheadRadius
            }
            canvas.drawCircle(
                playheadX.toFloat(),
                playheadY.toFloat(),
                playheadRadius.toFloat(),
                scrubberPaint
            )
        } else {
            val scrubberDrawableWidth = scrubberDrawable?.intrinsicWidth?:0
            val scrubberDrawableHeight = scrubberDrawable?.intrinsicHeight?:0
            scrubberDrawable?.setBounds(
                playheadX - scrubberDrawableWidth / 2,
                playheadY - scrubberDrawableHeight / 2,
                playheadX + scrubberDrawableWidth / 2,
                playheadY + scrubberDrawableHeight / 2
            )
            scrubberDrawable?.draw(canvas)
        }
    }

    private fun updateDrawableState() {
        if (((scrubberDrawable != null) && scrubberDrawable!!.isStateful
                    && scrubberDrawable!!.setState(drawableState))
        ) {
            invalidate()
        }
    }

    @RequiresApi(29)
    private fun setSystemGestureExclusionRectsV29(width: Int, height: Int) {
        if (lastExclusionRectangle != null
            && lastExclusionRectangle!!.width() == width
            && lastExclusionRectangle!!.height() == height
        ) {
            // Allocating inside onLayout is considered a DrawAllocation lint error, so avoid if possible.
            return
        }
        lastExclusionRectangle = Rect( /* left= */0,  /* top= */0, width, height)
        systemGestureExclusionRects = listOf(lastExclusionRectangle)
    }

    private fun getProgressText(): String? {
        return Util.getStringForTime(
            (formatBuilder)!!,
            (formatter)!!, position
        )
    }

    private fun getPositionIncrement(): Long {
        return if (keyTimeIncrement == C.TIME_UNSET) (if (duration == C.TIME_UNSET) 0 else (duration / keyCountIncrement)) else keyTimeIncrement
    }

    private fun setDrawableLayoutDirection(drawable: Drawable?): Boolean {
        return Util.SDK_INT >= 23 && setDrawableLayoutDirection(
            drawable,
            layoutDirection
        )
    }

    private fun setDrawableLayoutDirection(drawable: Drawable?, layoutDirection: Int): Boolean {
        return Util.SDK_INT >= 23 && drawable?.setLayoutDirection(layoutDirection)?:false
    }

    private fun dpToPx(density: Float, dps: Int): Int {
        return (dps * density + 0.5f).toInt()
    }

    private fun pxToDp(density: Float, px: Int): Int {
        return (px / density).toInt()
    }
}