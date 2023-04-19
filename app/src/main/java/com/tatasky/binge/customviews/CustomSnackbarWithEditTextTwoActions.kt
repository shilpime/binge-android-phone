package com.tatasky.binge.customviews

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.tatasky.binge.R

@SuppressLint("ClickableViewAccessibility")
class CustomSnackbarWithEditTextTwoActions(
    parent: ViewGroup,
    content: CustomSnackbarWithEditTextTwoActionsView,
) : BaseTransientBottomBar<CustomSnackbarWithEditTextTwoActions>(parent, content, content) {

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
        getView().setOnTouchListener { _, _ -> false }
    }

    companion object {
        fun make(
            viewGroup: ViewGroup,
            mszTitle: String,
            mszDesc: String,
            imgResourceSmall: Int? = null,
            imgResourceLarge: Int? = null,
            imgResourceCancel: Int? = null,
            btnText: String,
            maxProgress: Int,
            currProgress: Int,
            lambdaAction: ((email: String) -> Unit)? = null,
            lambdaCancel: (() -> Unit)? = null,
        ): CustomSnackbarWithEditTextTwoActions {
            val customView = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.layout_custom_snackbar_with_et_two_actions,
                viewGroup,
                false
            ) as CustomSnackbarWithEditTextTwoActionsView

            val imageView =
                customView.findViewById<ImageView>(R.id.iv_large_snackbar_custom_with_two_actions)
             val tvTitle =
                customView.findViewById<TextView>(R.id.tv_title_snackbar_custom_with_two_actions)
            val etEmail = customView.findViewById<EditText>(R.id.et_desc_snackbar_custom_with_two_actions)

            etEmail.visibility = View.VISIBLE
            tvTitle.textSize = 14f

           imageView
                .apply {
                    adjustViewBounds
                    imgResourceSmall?.let { setImageResource(it) }
                }



            customView.findViewById<TextView>(R.id.btn_action_snackbar_custom_with_two_actions)
                .apply {
                    text = btnText
                    setOnClickListener {
                        if(validateEmail(etEmail.text.toString()))
                            lambdaAction?.invoke(etEmail.text.toString())
                        else
                            etEmail.error = viewGroup.context.resources.getString(R.string.error_email)
                    }

                }

            customView.findViewById<ImageView>(R.id.iv_close_snackbar_custom_with_two_actions)
                .apply {
                    adjustViewBounds
                    imgResourceCancel?.let { setImageResource(it) }
                    setOnClickListener { lambdaCancel?.invoke() }
                }
            tvTitle.apply {
                text = mszTitle
                typeface =
                    Typeface.createFromAsset(
                        viewGroup.context.assets,
                        viewGroup.context.getString(
                            R.string.medium_font
                        )
                    )
            }
            return CustomSnackbarWithEditTextTwoActions(viewGroup, customView)
        }
    }
}