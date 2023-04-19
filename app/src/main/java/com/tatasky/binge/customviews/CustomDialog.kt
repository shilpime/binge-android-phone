package com.tatasky.binge.customviews

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.card.MaterialCardView
import com.tatasky.binge.R
import com.tatasky.binge.utils.isTablet
import dagger.android.support.DaggerDialogFragment

abstract class CustomDialog() : DaggerDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflateView = inflater.inflate(R.layout.layout_custom_dialog, container, false)
        inflateView.context?.let {
            if(isTablet(it)){
                inflateView.findViewById<ConstraintLayout>(R.id.root_dialog_view).removeAllViews()
                inflateView.findViewById<ConstraintLayout>(R.id.root_dialog_view).addView(getRootViewLayout(inflater, inflateView.findViewById<FrameLayout>(R.id.root_dialog_view)))

            }else{
                inflateView.findViewById<MaterialCardView>(R.id.root_dialog_view).removeAllViews()
                inflateView.findViewById<MaterialCardView>(R.id.root_dialog_view).addView(getRootViewLayout(inflater, inflateView.findViewById<FrameLayout>(R.id.root_dialog_view)))

            }
        }
        return inflateView
    }

    abstract fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View

    override fun show(manager: FragmentManager, tag: String?) {
        manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme)
        }
    }
}
