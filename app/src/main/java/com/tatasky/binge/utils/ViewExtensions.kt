package com.tatasky.binge.utils

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.tatasky.binge.helper.imageLoadWithPlaceHolder

fun ImageView.setImageWithPlaceHolder(url: String?, @DrawableRes placeHolder: Int) {
    imageLoadWithPlaceHolder(
        this,
        url ?: "",
        placeHolder
    )
}
