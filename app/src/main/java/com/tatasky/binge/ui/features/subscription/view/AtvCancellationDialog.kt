package com.tatasky.binge.ui.features.subscription.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.tatasky.binge.utils.setSelectedAccountDetail
import com.tatasky.binge.utils.showToast
import com.tatasky.binge.utils.startHomeScreen
import javax.inject.Inject


class AtvCancellationDialog(): CustomDialog() {
    lateinit var binding: LayoutCommonDialogBinding
    lateinit var viewModel: CommonSampleViewModel

    private var dialogContent : String?=""

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private constructor(dialogContent: String) : this() {
        this.dialogContent = dialogContent
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
            title = dialogContent,
            primaryButtonText = getString(R.string.ok),
            secondaryButtonText = null
        )
        viewModel.progressListener.observe(viewLifecycleOwner, Observer {
            binding.progressBar.startProgressAvd(it)
            if(it) {
                dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            } else {
                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

        binding.btnDialogPrimary.setOnClickListener {
            viewModel.switchAccountAtv(viewModel.sharedPrefs.getBaId())
        }

        viewModel.getSwitchAccountAtvResponse().observe(viewLifecycleOwner, Observer{
            it.getContentIfNotHandled()?.let {
                val selectedProfile = viewModel.sharedPrefs.getSelectedProfile()
                if(it.data?.baId.isNullOrBlank()){
                    showToast(context, getString(R.string.atv_cancel_api_error))
                    dismiss()
                } else {
                    selectedProfile?.baId = it.data?.baId
                    selectedProfile?.profileId = it.data?.profileId
                    setSelectedAccountDetail(selectedProfile!!, viewModel.sharedPrefs)
                    handler = Handler(Looper.getMainLooper())
                    handler?.postDelayed(runnable, 500)
                }
            }
        })
    }

    private var handler: Handler? = null
    private var runnable: Runnable = Runnable { startHomeScreen(activity) }

    companion object {
        fun newInstance(dialogContent:String): AtvCancellationDialog =
            AtvCancellationDialog(dialogContent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (handler != null) {
            handler?.removeCallbacks(runnable)
        }
    }

}