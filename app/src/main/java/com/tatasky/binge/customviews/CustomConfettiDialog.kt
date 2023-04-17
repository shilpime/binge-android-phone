package com.tatasky.binge.customviews

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.card.MaterialCardView
import com.tatasky.binge.R
import dagger.android.support.DaggerDialogFragment

abstract class CustomConfettiDialog() : DaggerDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflateView = inflater.inflate(R.layout.layout_custom_confetti_dialog, container, false)
        inflateView.findViewById<MaterialCardView>(R.id.root_dialog_view).removeAllViews()
        inflateView.findViewById<MaterialCardView>(R.id.root_dialog_view).addView(
            getRootViewLayout(
                inflater, inflateView.findViewById<FrameLayout>(
                    R.id.root_dialog_view
                )
            )
        )
        return inflateView
    }

    abstract fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View

    override fun show(manager: FragmentManager, tag: String?) {
        manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogThemeTransparent)
        }
    }
}
