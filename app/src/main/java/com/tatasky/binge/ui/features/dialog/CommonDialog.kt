package com.tatasky.binge.ui.features.dialog

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tatasky.binge.customviews.CustomDialog
import com.tatasky.binge.databinding.LayoutCommonDialogBinding
import javax.inject.Inject


/**
 * Created by Srikant Karnani on 20/12/19.
 */
class CommonDialog : CustomDialog() {
    lateinit var viewModel: DialogViewModel
    lateinit var binding: LayoutCommonDialogBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = LayoutCommonDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        isCancelable = false
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(DialogViewModel::class.java)
        binding.dialogModel = viewModel.getDialogModel().also {
            if (it == null)
                dialog?.dismiss()
        }
        binding.btnDialogPrimary.setOnClickListener {
            try {
                viewModel.getEventHandler()?.onPrimaryButtonClick()
            } catch (e:Exception) {
                dismiss()
            }
        }
        binding.btnDialogSecondary.setOnClickListener {
            try {
                viewModel.getEventHandler()?.onSecondaryButtonClick()
            } catch (e:Exception){
                dismiss()
            }
        }
        binding.ivDialogClose.setOnClickListener {
            dialog?.dismiss()
            viewModel.getEventHandler()?.onCloseButtonClick()
        }
//        binding.btnDialogSecondary.paintFlags = binding.btnDialogSecondary.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    companion object {
        fun newInstance(): CommonDialog = CommonDialog()
    }
}
