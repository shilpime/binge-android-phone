package com.tatasky.binge.ui.features.subscription.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.tatasky.binge.R
import com.tatasky.binge.customviews.CustomDialog
import com.tatasky.binge.databinding.LayoutCommonDialogBinding
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.utils.logoutApplication
import javax.inject.Inject

class DeviceStatusLogoutDialog() : CustomDialog() {
    lateinit var viewModel: CommonSampleViewModel
    lateinit var binding: LayoutCommonDialogBinding
    private var title : String?=""
    private var msg : String?=""
    private var isAllDeviceLogout :Boolean = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private constructor(title:String, msg:String,isAllDeviceLogout :Boolean = false) : this() {
        this.title = title
        this.msg = msg
        this.isAllDeviceLogout = isAllDeviceLogout
    }

    override fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = LayoutCommonDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        isCancelable = false
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(CommonSampleViewModel::class.java)
        binding.dialogModel = DialogModel(
            cancelable = false,
            imageId = R.drawable.ic_subscription_error,
            title = title,
            primaryButtonText = getString(R.string.ok),
            text = msg,
            secondaryButtonText = null
        )
        binding.btnDialogPrimary.setOnClickListener {
            if(isAllDeviceLogout){
                viewModel.removeAllDevices()
            }
            else
                viewModel.removeDeviceAndSignout()
        }
        viewModel.progressListener.observe(viewLifecycleOwner, Observer {
            if(it) {
                binding.progressBar.startProgressAvd(it)
                dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            } else {
                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
        binding.ivDialogClose.setOnClickListener {
            viewModel.removeDeviceAndSignout()
        }
        binding.btnDialogSecondary.setOnClickListener {
            viewModel.removeDeviceAndSignout()
        }
        viewModel.deviceForceLogout().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                context?.let {
                    logoutApplication(it)
                }
            }
        })
    }

    companion object {
        fun newInstance(title: String, msg: String, isAllDeviceLogout :Boolean = false): DeviceStatusLogoutDialog =
            DeviceStatusLogoutDialog(title, msg, isAllDeviceLogout)
    }
}