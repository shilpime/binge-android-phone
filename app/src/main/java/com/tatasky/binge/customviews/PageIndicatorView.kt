package com.tatasky.binge.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.extensions.toPx
import kotlin.math.max

class PageIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val DEFAULT_SELECTED = 0
    private val DEFAULT_TOTAL = 4
    private var mSelectedPage = DEFAULT_SELECTED
    private var mTotalPages = DEFAULT_TOTAL

    private val selectedPaint: Paint = Paint()
    private val unSelectedPaint: Paint = Paint()
    private var margin: Float = 4.toPx().toFloat()

    private var circleRadius = 4.toPx().toFloat()
    val rects = arrayListOf<RectF>()

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.PageIndicatorView)
            mSelectedPage =
                a.getInt(R.styleable.PageIndicatorView_selected_page, DEFAULT_SELECTED)
            mTotalPages = a.getInt(R.styleable.PageIndicatorView_total_pages, DEFAULT_TOTAL)
            if (mSelectedPage > mTotalPages) {
                throw Exception()
            }
            a.recycle()
        }
        selectedPaint.isAntiAlias = true
        selectedPaint.color = ContextCompat.getColor(context, R.color.darkPrimary)
        selectedPaint.style = Paint.Style.FILL
        unSelectedPaint.isAntiAlias = true
        unSelectedPaint.color = ContextCompat.getColor(context, R.color.darkHighlight)
        unSelectedPaint.style = Paint.Style.FILL

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        calculateRects()
        for (i in 1..mTotalPages) {
            if (i != mSelectedPage)
                canvas?.drawCircle(
                    rects[i - 1].centerX(),
                    rects[i - 1].centerY(),
                    circleRadius,
                    unSelectedPaint
                )
            else
                canvas?.drawRoundRect(
                    rects[i - 1],
                    circleRadius,
                    circleRadius,
                    selectedPaint
                )
        }
    }

    private fun calculateRects() {
        rects.clear()
        for (i in 1..mTotalPages) {
            if (i < mSelectedPage) {
                rects.add(
                    RectF(
                        (i - 1) * (2 * circleRadius + margin),
                        0f,
                        (i - 1) * (2 * circleRadius + margin) + 2 * circleRadius,
                        8.toPx().toFloat()
                    )
                )
            } else if (i == mSelectedPage) {
                rects.add(
                    RectF(
                        (i - 1) * (2 * circleRadius + margin),
                        0f,
                        (i - 1) * (2 * circleRadius + margin) + 32.toPx().toFloat(),
                        8.toPx().toFloat()
                    )
                )
            } else {
                rects.add(
                    RectF(
                        (i - 2) * (2 * circleRadius + margin) + 36.toPx().toFloat(),
                        0f,
                        (i - 2) * (2 * circleRadius + margin) + 44.toPx().toFloat(),
                        8.toPx().toFloat()
                    )
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth =
            max(
                (suggestedMinimumWidth + paddingLeft + paddingRight),
                (32.toPx() + (mTotalPages - 1) * 2 * circleRadius + margin * (mTotalPages - 1)).toInt()
            )
        val desiredHeight =
            max((suggestedMinimumHeight + paddingTop + paddingBottom), 8.toPx())
        setMeasuredDimension(
            desiredWidth,
            desiredHeight
        )
    }

    fun setTotalPages(totalPages:Int){
        mTotalPages = totalPages
    }

    fun setSelected(selected: Int) {
        if (selected < 1 || selected > mTotalPages) {
            throw NumberFormatException()
        } else {
            mSelectedPage = selected
            invalidate()
        }
    }
}