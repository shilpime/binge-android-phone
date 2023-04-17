package com.tatasky.binge.ui.features.fsinstallation

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFadeThrough
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentConfirmAddressBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.utils.CODE_SUCCESS
import com.tatasky.binge.utils.navigateSafe
import com.tatasky.binge.utils.startHomeScreen

class FSConfirmAddressFragment : BaseFragment<FragmentConfirmAddressBinding,FSInstallationViewModel>(){

    override fun getViewModelClass(): Class<FSInstallationViewModel> =
        FSInstallationViewModel::class.java


    override fun layoutId(): Int = R.layout.fragment_confirm_address

    override fun getViewModelOwner(): ViewModelStoreOwner =
        navController().getViewModelStoreOwner(R.id.nav_fs_journey)

    override fun setObserver() {
        viewModel.getAddress().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { address->
                binding.name.text = address.data?.name
                binding.address.text = address.data?.address
                binding.btnFsProceed.setOnClickListener {
                    findNavController().navigateSafe(
                        FSConfirmAddressFragmentDirections.fsInstallationFragmentToFsScheduleFragment(
                            address
                        )
                    )
                }
                TransitionManager.beginDelayedTransition(binding.clContainer,MaterialFadeThrough())
                binding.clContainer.show()
            }
        })
        viewModel.isWorkOrderProcessed().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it1 ->
                if (it1.code == CODE_SUCCESS) {
                    viewModel.sharedPrefs.saveFirestickTaken(true)
                    showDialog(
                        DialogModel(
                            false,
                            R.drawable.ic_success_tick,
                            getString(R.string.thank_you),
                            getString(R.string.start_watching_now),
                            null,
                            getString(R.string.fs_delivery_msg)
                        ),object :
                            CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                hideDialog()
                                startHomeScreen(activity)
                            }

                            override fun onSecondaryButtonClick() {
                            }

                            override fun onCloseButtonClick() {
                            }
                        })
                }
            }
        })
    }

    override fun toBeCalledOnce() {
        viewModel.fetchAddress()
        binding.btnNotNow.setOnClickListener {
            startHomeScreen(activity)
        }
    }
}