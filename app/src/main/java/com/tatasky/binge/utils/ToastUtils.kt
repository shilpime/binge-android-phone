package com.tatasky.binge.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.tatasky.binge.R
import com.tatasky.binge.customviews.CustomSnackbar
import com.tatasky.binge.customviews.CustomSnackbarUpgradeTrial
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity

fun showToast(
    context: Context?,
    msz: String,
    imgResource: Int? = null,
    layoutParam: ConstraintLayout.LayoutParams? = null/*Margin and other properties are only available for Constraint, Linear etc layout*/
) {
    context?.let {
        (it as BaseActivity<*>).window.decorView.findViewById<View>(android.R.id.content)
            ?.let { rootView ->
                CustomSnackbar.make(rootView as ViewGroup, msz, imgResource, layoutParam).show()
            }
    }
}
fun showToastOverDialog(dialog: Dialog, msz: String, imgResource: Int? = null) {
    dialog.window?.decorView?.let { rootView ->
        CustomSnackbar.make(rootView as ViewGroup, msz, imgResource).show()
    }
}

fun showCustomToast(context: Context?, layout: View?, gravity: Int = Gravity.NO_GRAVITY, isBottomNavVisible: Boolean = true): Toast {
    val toast = Toast(context)
    toast.duration = Toast.LENGTH_SHORT
    if (gravity == Gravity.FILL_HORIZONTAL) {
        val gravity = Gravity.BOTTOM or Gravity.FILL_HORIZONTAL
        if (!isBottomNavVisible) {
            context?.let {
                toast.setGravity(gravity, dpToPx(it, 20), dpToPx(it, 20))
            }
        }
        else {
            context?.let {
                toast.setGravity(gravity, dpToPx(it, 20), dpToPx(it, 64))
            }
        }
    } else if (gravity != Gravity.NO_GRAVITY) {
        toast.setGravity(gravity, 0, 0)
    }
    else  {
        context?.let {
            toast.setGravity(Gravity.BOTTOM, 0, dpToPx(it, 64))
        }
    }
    toast.view = layout
    toast.show()
    return toast
}

fun showCustomLoginToast(context: Context, message: String, isSuccess: Boolean, isBottomNavVisible: Boolean = true) {
    val view = DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
        LayoutInflater.from(context),
        R.layout.layout_toast_success_failure,
        null,
        false
    )
    view.textLoginSuccessfulToast.text = message
    if (isSuccess) {
        view.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
    } else {
        view.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_warning_login_failure)
    }
    showCustomToast(context, view?.root, Gravity.FILL_HORIZONTAL, isBottomNavVisible)
}

fun showCustomUpgradeToast(context: Context?, title: String, subtitle: String) {
    context?.let {
        (it as BaseActivity<*>).window.decorView.findViewById<View>(android.R.id.content)
            ?.let { rootView ->
                CustomSnackbarUpgradeTrial.make(rootView as ViewGroup, title, subtitle).show()
            }
    }
}