package com.tatasky.binge.ui.features.subscription.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tatasky.binge.R
import com.tatasky.binge.customviews.CustomDialog
import com.tatasky.binge.databinding.LayoutCommonDialogBinding
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.utils.logoutApplication

class LogoutDialog(private val logoutLambda: (() -> Unit)? = null) : CustomDialog() {
    lateinit var binding: LayoutCommonDialogBinding

    override fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = LayoutCommonDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        isCancelable = false
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.dialogModel = DialogModel(
            cancelable = false,
            imageId = R.drawable.ic_subscription_error,
            title = getString(R.string.device_removed),
            primaryButtonText = getString(R.string.okay),
            text = getString(R.string.force_logout_message),
            secondaryButtonText = null
            )
        binding.btnDialogPrimary.setOnClickListener {
            dialog?.dismiss()
            logoutLambda?.invoke()
            logoutApplication(requireContext())
        }
        binding.btnDialogSecondary.setOnClickListener {
            dialog?.dismiss()
            logoutLambda?.invoke()
            logoutApplication(requireContext())
        }
    }

    companion object {
        fun newInstance(logoutLambda: (() -> Unit)? = null): LogoutDialog = LogoutDialog(logoutLambda)
    }
}