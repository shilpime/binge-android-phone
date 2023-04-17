package com.tatasky.binge.ui.features.subscription.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tatasky.binge.customviews.CustomDialog
import com.tatasky.binge.databinding.LayoutFirestickUpsellDialogBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener

class FireStickUpsellDialog() : CustomDialog() {

    private lateinit var binding: LayoutFirestickUpsellDialogBinding
    private var listener: CommonDialogEventListener? = null
    private var title: CharSequence? = null
    private var primaryBtn: CharSequence? = null

    private constructor(listener: CommonDialogEventListener, title: CharSequence, primaryBtn : CharSequence) : this() {
        this.listener = listener
        this.title = title
        this.primaryBtn = primaryBtn
    }

    override fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = LayoutFirestickUpsellDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.btnDialogPrimary.text = primaryBtn
        isCancelable = false
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.btnDialogPrimary.setOnClickListener {
            try {
                listener?.onPrimaryButtonClick()
            } catch (e: Exception) {
            }
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            try {
                listener?.onSecondaryButtonClick()
            } catch (e: Exception) {
            }
            dismiss()
        }
    }

    companion object {
        fun newInstance(listener: CommonDialogEventListener, title: CharSequence, primaryBtn : CharSequence): FireStickUpsellDialog =
            FireStickUpsellDialog(listener, title, primaryBtn)
    }
}