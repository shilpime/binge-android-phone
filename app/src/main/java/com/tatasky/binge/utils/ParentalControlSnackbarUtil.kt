package com.tatasky.binge.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.tatasky.binge.customviews.CustomSnackbaParentalControl
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity

class ParentalControlSnackbarUtil {
    private var snackbar: CustomSnackbaParentalControl? = null

    fun showParentalControlSnackbar(
        context: Context?,
        msz: String,
        imgResource: Int? = null,
        btnText: String,
        lambdaAction: (() -> Unit)? = null,
        lambdaCancel: (() -> Unit)? = null,
    ) {
        context?.let {
            (it as BaseActivity<*>).window.decorView.findViewById<View>(android.R.id.content)
                ?.let { rootView ->
                    snackbar = CustomSnackbaParentalControl.make(rootView as ViewGroup,
                        msz,
                        imgResource,
                        btnText,
                        lambdaAction,
                        lambdaCancel)
                        .apply {
                            duration = Snackbar.LENGTH_INDEFINITE
                        }

                    snackbar?.show()
                }
        }
    }

    fun hideParentalControlSnackbar() {
        snackbar?.dismiss()
    }
}