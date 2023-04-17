package com.tatasky.binge.ui.features.link_accounts

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentLinkAccountsBinding
import com.tatasky.binge.databinding.FragmentLinkAccountsOtpBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.utils.getFormattedMobile
import com.tatasky.binge.utils.navigateSafe
import java.util.*

class LinkAccountsOTPFragment :
    BaseFragment<FragmentLinkAccountsOtpBinding, LinkAccountViewModel>() {

    var mobileNumber: String = ""
    private val args by navArgs<LinkAccountsOTPFragmentArgs>()

    override fun getViewModelClass(): Class<LinkAccountViewModel> {
        return LinkAccountViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_link_accounts_otp
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
       // mobileNumber= viewModel.getMobileNumber()
        binding.headerText =
            String.format(
                Locale.US,
                getString(R.string.otp_sent),
                getFormattedMobile(mobileNumber)
            )
    }

    override fun setObserver() {
    }

    override fun toBeCalledOnce() {

        mobileNumber = args.rmn

        binding.btnVerifyOtp.setOnClickListener {
            if (validateOtp()) {
                val otp = binding.etOtp.et.text.toString()
                findNavController().navigateSafe(
                    LinkAccountsOTPFragmentDirections.actionLinkAccountsOTPToLinkAccountsSuccessful(
                        mobileNumber
                    )
                )

            }
            it.closeKeyboard()
        }
        binding.tvResendOtp.setOnClickListener {
        }
    }

    private fun validateOtp(): Boolean {
        val otp = binding.etOtp.et.text.toString()
        if (otp.length < 6) {
            binding.etOtp.til.error = getString(R.string.error_4_valid_otp)
            return false
        }
        return true
    }

}