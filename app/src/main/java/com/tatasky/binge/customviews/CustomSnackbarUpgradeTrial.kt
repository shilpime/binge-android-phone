package com.tatasky.binge.customviews

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.tatasky.binge.R

class CustomSnackbarUpgradeTrial(
    parent: ViewGroup,
    content: CustomSnackbarUpgradeView
) : BaseTransientBottomBar<CustomSnackbarUpgradeTrial>(parent, content, content) {

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
    }


    companion object {

        fun make(
            viewGroup: ViewGroup,
            title: String,
            subtitle: String
        ): CustomSnackbarUpgradeTrial {
            val customView = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.layout_custom_upgrade_toast,
                viewGroup,
                false
            ) as CustomSnackbarUpgradeView


            customView.findViewById<TextView>(R.id.upgrade_toast_title).text = title
            customView.findViewById<TextView>(R.id.upgrade_toast_desc).text = subtitle


            return CustomSnackbarUpgradeTrial(viewGroup, customView)

        }
    }
}