package com.tatasky.binge.utils

import android.content.Context
import android.util.TypedValue
import androidx.core.text.isDigitsOnly

object TextUtils {
    fun String.isNotEmptyAndIsDigitAndIsGreaterThanZero() =
        this.isNotEmpty() && this.isDigitsOnly() && this.toInt() > 0

    fun spToPx(sp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }
}