package com.tatasky.binge.customviews

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.tatasky.binge.R

class CustomSnackbaParentalControl(
    parent: ViewGroup,
    content: CustomSnackbarParentalControlView,
) : BaseTransientBottomBar<CustomSnackbaParentalControl>(parent, content, content) {

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
            imgResource: Int? = null,
            btnText: String,
            lambdaAction: (() -> Unit)? = null,
            lambdaCancel: (() -> Unit)? = null,
        ): CustomSnackbaParentalControl {
            val customView = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.layout_custom_snackbar_parental_control,
                viewGroup,
                false
            ) as CustomSnackbarParentalControlView
            customView.findViewById<ImageView>(R.id.iv_close_snackbar_parental_control)
                .apply {
                    adjustViewBounds
                    imgResource?.let { setImageResource(imgResource) }
                    setOnClickListener { lambdaCancel?.invoke() }
                }
            customView.findViewById<TextView>(R.id.tv_msg_snackbar_parental_control)
                .apply {
                    text = msz
                    typeface =
                        Typeface.createFromAsset(viewGroup.context.assets,
                            viewGroup.context.getString(
                                R.string.medium_font))
                }

            customView.findViewById<MaterialButton>(R.id.btn_action_snackbar_parental_control)
                .apply {
                    text = btnText
                    setOnClickListener { lambdaAction?.invoke() }
                }

            return CustomSnackbaParentalControl(viewGroup, customView)
        }
    }
}