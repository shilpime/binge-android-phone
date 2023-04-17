package com.tatasky.binge.ui.features.coachmark

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import com.tatasky.binge.R
import com.tatasky.binge.utils.dpToPx
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptOptions
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptUtils
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.CirclePromptBackground
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


class DimmedCirclePromptBackground(
    @IntRange(from = 0, to = 255)
    private val promptBackgroundAlpha: Int = 179,
    @IntRange(from = 0, to = 255)
    private val dimBackgroundAlpha: Int = 191,
    private val promptBackgroundBorderWidth: Int = 1,
    private val increasePromptBackgroundRadiusBy: Int = 0,
    private val decreasePromptBackgroundRadiusBy: Int = 0,
    private val context: Context
) : CirclePromptBackground() {

    private val dimBounds: RectF = RectF()
    private var dimPaint: Paint = Paint()

    /**
     * The current circle centre position.
     */
    private var mPosition: PointF = PointF()

    /**
     * The current radius for the circle.
     */
    private var mRadius = 0f

    /**
     * The position for circle centre at 1.0 scale.
     */
    private var mBasePosition: PointF = PointF()

    /**
     * The radius for the circle at 1.0 scale.
     */
    private var mBaseRadius = 0f

    /**
     * The paint to use to render the circle.
     */
    private var mPaint: Paint = Paint()

    /**
     * The alpha value to use at 1.0 scale.
     */
    @IntRange(from = 0, to = 255)
    private var mBaseColourAlpha = 0

    /**
     * The current path of the background (useful for clipping)
     */
    private var mPath: Path = Path()

    init {
        dimPaint.color = Color.BLACK
        mPaint.isAntiAlias = true
    }

    override fun setColour(@ColorInt colour: Int) {
        mPaint.color = colour
        mBaseColourAlpha = Color.alpha(colour)
        mPaint.alpha = promptBackgroundAlpha
    }

    override fun prepare(
        options: PromptOptions<*>, clipToBounds: Boolean,
        clipBounds: Rect
    ) {
        // Obtain values from the prompt options.
        val promptText = options.promptText
        val focalBounds = options.promptFocal.bounds
        val focalCentreX = focalBounds.centerX()
        val focalCentreY = focalBounds.centerY()
        val focalPadding = options.focalPadding
        val textBounds = promptText.bounds
        val textPadding = options.textPadding
        val clipBoundsInset88dp = RectF(clipBounds)
        // Default material design offset prompt when more than 88dp inset
        val inset88dp = 88f * options.resourceFinder.resources.displayMetrics.density
        clipBoundsInset88dp.inset(inset88dp, inset88dp)

        // Is the focal centre more than 88dp from the clip bounds edge
        if ((focalCentreX > clipBoundsInset88dp.left
                    && focalCentreX < clipBoundsInset88dp.right)
            || (focalCentreY > clipBoundsInset88dp.top
                    && focalCentreY < clipBoundsInset88dp.bottom)
        ) {
            // The circle position and radius is calculated based on three points placed around the
            // prompt: XY1, XY2 and XY3.
            // XY2: the text left side
            // XY3: the text right side
            // XY1: the furthest point on the focal target from the text centre x point

            // XY1
            val textWidth = textBounds.width()
            // Calculate the X distance from the text centre x to focal centre x
            val distanceX = focalCentreX - textBounds.left + textWidth / 2
            // Calculate how much percentage wise the focal centre x is from the text centre x to
            // the nearest text edge
            val percentageOffset = 100 / textWidth * distanceX
            // Angle is the above percentage of 90 degrees
            var angle = 90 * (percentageOffset / 100)
            // 0 degrees is right side middle
            // If text above target
            angle = if (textBounds.top < focalBounds.top) {
                180 - angle
            } else {
                180 + angle
            }
            val furthestPoint = options.promptFocal.calculateAngleEdgePoint(
                angle,
                focalPadding
            )
            val x1 = furthestPoint.x
            val y1 = furthestPoint.y

            // XY2
            val x2 = textBounds.left - textPadding
            // If text is above the target
            val y2: Float = if (textBounds.top < focalBounds.top) {
                textBounds.top
            } else {
                textBounds.bottom
            }

            // XY3
            var x3 = textBounds.right + textPadding

            // If the focal width is greater than the text width
            if (focalBounds.right > x3) {
                x3 = focalBounds.right + focalPadding
            }

            // Calculate the position and radius
            val offset = x2.toDouble().pow(2.0) + y2.toDouble().pow(2.0)
            val bc = (x1.toDouble().pow(2.0) + y1.toDouble().pow(2.0) - offset) / 2.0
            val cd = (offset - x3.toDouble().pow(2.0) - y2.toDouble().pow(2.0)) / 2.0
            val det = ((x1 - x2) * (y2 - y2) - (x2 - x3) * (y1 - y2)).toDouble()
            val idet = 1 / det
            mBasePosition[((bc * (y2 - y2) - cd * (y1 - y2)) * idet).toFloat()] =
                ((cd * (x1 - x2) - bc * (x2 - x3)) * idet).toFloat()
            mBaseRadius = sqrt(
                (x2 - mBasePosition.x).toDouble().pow(2.0)
                        + (y2 - mBasePosition.y).toDouble().pow(2.0)
            ).toFloat()
            /*point1.set(x1, y1);
            point2.set(x2, y2);
            point3.set(x3, y3);*/
        } else {
            mBasePosition[focalCentreX] = focalCentreY
            // Calculate the furthest distance from the center based on the text size.
            val length = max(
                abs(textBounds.right - focalCentreX),
                abs(textBounds.left - focalCentreX)
            ) + textPadding
            // Calculate the height based on the distance from the focal centre to the furthest text y position.
            val height = (focalBounds.height() / 2) + focalPadding + textBounds.height()
            // Calculate the radius based on the calculated width and height
            mBaseRadius =
                sqrt(length.toDouble().pow(2.0) + height.toDouble().pow(2.0))
                    .toFloat()
            /*point1.set(focalCentreX + (prompt.mHorizontalTextPositionLeft ? -length : length),
                            focalCentreY + (prompt.mVerticalTextPositionAbove ? - height : height));*/
        }
        mPosition.set((mBasePosition))

        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        // Set the bounds to display as dimmed to the screen bounds
        dimBounds.set(0f, 0f, metrics.widthPixels.toFloat(), metrics.heightPixels.toFloat() + 200 /*Increasing more for devices with bottom gesture*/)
    }

    override fun update(options: PromptOptions<*>, revealModifier: Float, alphaModifier: Float) {
        val focalBounds = options.promptFocal.bounds
        val focalCentreX = focalBounds.centerX()
        val focalCentreY = focalBounds.centerY()
        mRadius = mBaseRadius * revealModifier
        mPaint.alpha = promptBackgroundAlpha
        // Change the current centre position to be a position scaled from the focal to the base.
        mPosition[focalCentreX + ((mBasePosition.x - focalCentreX) * revealModifier)] =
            focalCentreY + ((mBasePosition.y - focalCentreY) * revealModifier)
        mPath.reset()
        mPath.addCircle(mPosition.x, mPosition.y, mRadius, Path.Direction.CW)

        // Allow for the dimmed background to fade in and out
        dimPaint.alpha = dimBackgroundAlpha
    }

    override fun draw(canvas: Canvas) {
        drawDimBackgroundOnCompleteScreen(canvas, dimBounds, dimPaint)
        drawPromptBackground(canvas)
        drawPromptBackGroundBorder(canvas)
    }

    private fun drawPromptBackGroundBorder(canvas: Canvas) {
        // Border with canvas
        val paint = Paint()
        paint.color = ContextCompat.getColor(context, R.color.stroke_color)
        paint.strokeWidth = dpToPx(context, promptBackgroundBorderWidth).toFloat()
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.isDither = true
        canvas.drawCircle(
            mPosition.x,
            mPosition.y,
            mRadius + increasePromptBackgroundRadiusBy - decreasePromptBackgroundRadiusBy,
            paint
        )
    }

    private fun drawPromptBackground(canvas: Canvas) {
        // Prompt background
        canvas.drawCircle(
            mPosition.x,
            mPosition.y,
            mRadius + increasePromptBackgroundRadiusBy - decreasePromptBackgroundRadiusBy,
            mPaint
        )
    }

    private fun drawDimBackgroundOnCompleteScreen(canvas: Canvas, dimBounds: RectF, dimPaint: Paint) {
        // Outside prompt background to Dim the background
        canvas.drawRect(dimBounds, dimPaint)
    }

    override fun contains(x: Float, y: Float): Boolean {
        return PromptUtils.isPointInCircle(x, y, mPosition, mRadius)
    }

    override fun getPath(): Path {
        return mPath
    }
}