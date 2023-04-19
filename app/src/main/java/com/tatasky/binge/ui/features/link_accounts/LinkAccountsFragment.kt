package com.tatasky.binge.ui.features.link_accounts

import android.os.Bundle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentLinkAccountsBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.clearError
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.utils.navigateSafe

class LinkAccountsFragment : BaseFragment<FragmentLinkAccountsBinding, LinkAccountViewModel>() {
    override fun getViewModelClass(): Class<LinkAccountViewModel> {
        return LinkAccountViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_link_accounts
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun setObserver() {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.etNumber.et.setText("")
    }
    override fun toBeCalledOnce() {

        binding.btnOtp.setOnClickListener {

            if(validate()){
            findNavController().navigateSafe(
                LinkAccountsFragmentDirections.actionLinkAccountsToLinkAccountsOTP(
                    binding.etNumber.et.text.toString()
                )
            )
        }
        }
    }

    private fun validate() :Boolean{
        binding.root.closeKeyboard()
        clearErrors()
        return when {
            binding.etNumber.et.text.toString().length == 10 -> {
                true
            }
            binding.etNumber.et.text.toString().isEmpty() -> {
                binding.etNumber.til.error = getString(R.string.empty_mobile_validation)
                false
            }
            else -> {
                binding.etNumber.til.error = getString(R.string.validate_rmn)
                false
            }
        }
    }

    private fun clearErrors() {
        binding.etNumber.til.clearError()
    }
}