package com.tatasky.binge.ui.features.subscription.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.tatasky.binge.R
import com.tatasky.binge.databinding.LayoutFirstickDialogBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import dagger.android.support.DaggerDialogFragment

class FirestickDialog() : DaggerDialogFragment() {

    private lateinit var binding: LayoutFirstickDialogBinding
    private var listener : CommonDialogEventListener? = null

    private constructor(listener : CommonDialogEventListener) : this() {
        this.listener = listener
    }

    override fun show(manager: FragmentManager, tag: String?) {
        manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LayoutFirstickDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        isCancelable = false
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.btnDialogPrimary.setOnClickListener {
            try {
                listener?.onPrimaryButtonClick()
            } catch (e:Exception){}
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            try {
                listener?.onSecondaryButtonClick()
            } catch (e:Exception){}
            dismiss()
        }
    }

    companion object {
        fun newInstance(listener : CommonDialogEventListener): FirestickDialog =
            FirestickDialog(listener)
    }
}