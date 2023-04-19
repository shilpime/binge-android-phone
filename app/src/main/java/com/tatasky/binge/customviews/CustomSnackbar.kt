package com.tatasky.binge.customviews

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.extensions.startAvd

class CustomSnackbar(
    parent: ViewGroup,
    content: CustomSnackbarView
) : BaseTransientBottomBar<CustomSnackbar>(parent, content, content) {

    init {
        getView().setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                android.R.color.transparent
            )
        )
        val lp = getView().layoutParams

        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        getView().layoutParams = lp
        getView().setOnTouchListener() { view, motionEvent -> false }
    }

    companion object {

        fun make(
            viewGroup: ViewGroup,
            msz: String,
            imgResource: Int?,
            /*Margin and other properties are only available for Constraint, Linear etc layout*/
            layoutParam: ConstraintLayout.LayoutParams? = null,
            isTablet: Boolean = false,
        ): CustomSnackbar {
            val customView = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.layout_custom_toast,
                viewGroup,
                false
            ) as CustomSnackbarView
            //Setup custom view properties for layout
            layoutParam?.let {
                customView.layoutParams = it //Adjusting custom parent view layout param
                if (isTablet)
                    customView.findViewById<ImageView>(R.id.tablet_toast_bg_iv).layoutParams =
                        it
            }
            customView.findViewById<ImageView>(R.id.toast_iv).adjustViewBounds
            imgResource?.let {
                customView.findViewById<ImageView>(R.id.toast_iv).setImageResource(imgResource)
                customView.findViewById<ImageView>(R.id.toast_iv).startAvd(true)
            }
            customView.findViewById<TextView>(R.id.toast_tv).text = msz
            customView.findViewById<TextView>(R.id.toast_tv).typeface = Typeface.createFromAsset(
                viewGroup.context.assets,
                viewGroup.context.getString(R.string.medium_font)
            )

            return CustomSnackbar(viewGroup, customView)
        }
    }

}
