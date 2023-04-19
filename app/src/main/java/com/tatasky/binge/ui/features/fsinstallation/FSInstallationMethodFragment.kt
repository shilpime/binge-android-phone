package com.tatasky.binge.ui.features.fsinstallation


import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentFsInstallationBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.utils.navigateSafe
import com.tatasky.binge.utils.startHomeScreen


class FSInstallationMethodFragment : BaseFragment<FragmentFsInstallationBinding,FSInstallationViewModel>(){
    override fun getViewModelClass(): Class<FSInstallationViewModel> =
        FSInstallationViewModel::class.java


    override fun layoutId(): Int = R.layout.fragment_fs_installation

    override fun getViewModelOwner(): ViewModelStoreOwner =
        navController().getViewModelStoreOwner(R.id.nav_fs_journey)

    override fun setObserver() {
        viewModel.getCampaignResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {campaignResponse ->
                if(campaignResponse.data == null || (
                            campaignResponse.data?.diyInstallation == false
                                    && campaignResponse.data?.installationRequired == false)
                ){
                    binding.diy.root.visibility = View.VISIBLE
                    binding.requireAssistance.root.visibility = View.VISIBLE
                }
                else {
                    if (campaignResponse.data?.diyInstallation == true) {
                        binding.diy.root.visibility = View.VISIBLE
                    }
                    if (campaignResponse.data?.installationRequired == true) {
                        binding.requireAssistance.root.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                activity?.onBackPressed()
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeRadioBtns()
    }
    private fun initializeRadioBtns(){
        binding.requireAssistance.radiobtnFs.isChecked = viewModel.installationReq
        binding.diy.radiobtnFs.isChecked = !viewModel.installationReq
    }

    override fun toBeCalledOnce() {
        viewModel.fetchCampaignResponse()


        binding.diy.radiobtnFs.setOnCheckedChangeListener { compoundButton, b ->
            binding.requireAssistance.radiobtnFs.isChecked = !b
        }
        binding.requireAssistance.radiobtnFs.setOnCheckedChangeListener { compoundButton, b ->
            binding.diy.radiobtnFs.isChecked = !b
        }
        binding.diy.root.setOnClickListener {
            binding.requireAssistance.radiobtnFs.isChecked = false
            binding.diy.radiobtnFs.isChecked = true
        }
        binding.requireAssistance.root.setOnClickListener {
            binding.diy.radiobtnFs.isChecked = false
            binding.requireAssistance.radiobtnFs.isChecked = true
        }

        binding.btnFsProceed.setOnClickListener {
            if(binding.requireAssistance.radiobtnFs.isChecked) {
                viewModel.installationReq = true
                findNavController().navigateSafe(
                    FSInstallationMethodFragmentDirections.fsInstallationFragmentToFsAddressFragment()
                )
            } else {
                viewModel.installationReq = false
                findNavController().navigateSafe(
                    FSInstallationMethodFragmentDirections.fsInstallationFragmentToFsAddressFragment()
                )
            }
        }
        binding.btnNotNow.setOnClickListener {
            startHomeScreen(activity)
        }
    }
}