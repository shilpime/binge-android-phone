package com.tatasky.binge.ui.features.parentalcontrol.bottomsheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentParentalPinSetupBinding
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.GuestLoginViewModel
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.temp.GuestLoginBottomSheetResult
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import com.tatasky.binge.utils.navigateSafe
import javax.inject.Inject

class ParentalPinSetupFragment :
    BaseFragment<FragmentParentalPinSetupBinding, GuestLoginViewModel>(), View.OnKeyListener {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    private lateinit var parentalControlViewModel: ParentalControlViewModel

    override fun getViewModelClass(): Class<GuestLoginViewModel> =
        GuestLoginViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_parental_pin_setup

    override fun getViewModelOwner(): ViewModelStoreOwner =
        requireParentFragment().requireParentFragment()

    override fun onKey(p0: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        //Handling for deleting OTP and regaining the focus after deletion
        if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent?.action == KeyEvent.ACTION_UP)
            binding.clEtContainerParentalPinSetup.apply {
                when(p0) {
                    etPinDig1 -> clearFieldAndFocus(etPinDig1)
                    etPinDig2 -> {
                        if (etPinDig2.text.isNotEmpty())
                            clearFieldAndFocus(etPinDig2)
                        else
                            clearFieldAndFocus(etPinDig1)
                    }
                    etPinDig3 -> {
                        if (etPinDig3.text.isNotEmpty())
                            clearFieldAndFocus(etPinDig3)
                        else
                            clearFieldAndFocus(etPinDig2)
                    }
                    etPinDig4 -> {
                        if (etPinDig4.text.isNotEmpty())
                            clearFieldAndFocus(etPinDig4)
                        else
                            clearFieldAndFocus(etPinDig3)
                    }
                }
            }
        return false
    }

    //Handling for deleting digit fields and regaining the focus after deletion
    private fun clearFieldAndFocus(etOtpDig: EditText) {
        etOtpDig.text.clear()
        etOtpDig.requestFocus()
    }

    override fun setObserver() {
        parentalControlViewModel.progressListener.observe(viewLifecycleOwner) {
            viewModel.setProgressing(it)
        }

        parentalControlViewModel.saveParentalPinResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                //upon successfully saving parental pin, take user to the success screen
                sharedPrefs.setParentalPinExists(true)
                findNavController().navigateSafe(ParentalPinSetupFragmentDirections.actionGlobalParentalPinSuccessFragment())
            }
        }

        parentalControlViewModel.saveParentalPinError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                onError(errorModel)
            }
        }

        parentalControlViewModel.validateParentalPinResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                sharedPrefs.setParentalControlEnabled(false)
                viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
            }
        }

        parentalControlViewModel.validateParentalPinError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                onError(errorModel)
            }
        }

        viewModel.generateOtpResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateSafe(ParentalPinSetupFragmentDirections.actionGlobalGuestLoginVerifyOtpFragment())
            }
        }

        viewModel.generateOtpResponseError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                onError(errorModel)
            }
        }
    }

    override fun toBeCalledOnce() {
        parentalControlViewModel = ViewModelProvider(
            requireParentFragment().requireParentFragment(),
            mViewModelFactory
        )[ParentalControlViewModel::class.java]

        when {
            viewModel.isParentalPinSetupRequested && viewModel.isLoggedIn -> {
                viewModel.rmn = sharedPrefs.getClearRMN()
            }
            viewModel.isParentalPinVerificationRequested -> {
                binding.apply {
                    groupParentalPinVerification.visibility = View.VISIBLE
                    btnCancelParentalPinSetup.visibility = View.GONE
                    titleParentalPinSetup.text = getString(R.string.title_parental_pin_verification)
                }
            }
            viewModel.isParentalPinResetRequested -> {
                viewModel.rmn = sharedPrefs.getClearRMN()
                binding.apply {
                    groupParentalPinVerification.visibility = View.GONE
                    btnCancelParentalPinSetup.visibility = View.VISIBLE
                    titleParentalPinSetup.text = getString(R.string.title_parental_pin_reset)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*overriding loader set on activity via BaseFragment*/
        showProgress = Runnable { }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            binding.btnProceedParentalPinSetup.isEnabled = false
            btnProceedParentalPinSetup.setOnClickListener {
                if (getPin().length == 4) {
                    viewModel.parentalPinValue = getPin()
                    when {
                        viewModel.isParentalPinSetupRequested && viewModel.isLoggedIn -> {
                            loginAnalytics.trackParentalPinOtpInitiate("")
                            viewModel.generateOtp()
                        }
                        viewModel.isParentalPinSetupRequested -> {
                            //if user is not logged in then use the otp used in last screen and save the parental pin
                            parentalControlViewModel.saveParentalPin(
                                parentalPinValue = viewModel.parentalPinValue,
                                otp = viewModel.otp,
                                isLogin = false
                            )
                        }
                        viewModel.isParentalPinVerificationRequested -> {
                            //if user has requested for pin verification then directly hit the pin verification api
                            parentalControlViewModel.validateParentalPin(
                                parentalPinValue = viewModel.parentalPinValue
                            )
                        }
                        viewModel.isParentalPinResetRequested -> {
                            loginAnalytics.trackParentalPinOtpInitiate("")
                            viewModel.generateOtp()
                        }
                    }
                }
            }

            btnCancelParentalPinSetup.setOnClickListener {
                when {
                    viewModel.isParentalPinSetupRequested -> viewModel.guestLoginResult.postValue(
                        SingleEvent(GuestLoginBottomSheetResult.SUCCESS)
                    )
                    else -> viewModel.guestLoginResult.postValue(
                        SingleEvent(
                            GuestLoginBottomSheetResult.PARENTAL_CONTROL_NOT_DISABLED
                        )
                    )
                }
            }

            btnCancelParentalPinVerification.setOnClickListener {
                viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.PARENTAL_CONTROL_NOT_DISABLED))
            }

            btnForgotParentalPinVerification.setOnClickListener {
                viewModel.isParentalPinVerificationRequested = false
                viewModel.isParentalPinResetRequested = true
                findNavController().navigateSafe(ParentalPinSetupFragmentDirections.actionParentalPinSetupFragmentSelf())
            }

            clEtContainerParentalPinSetup.apply {
                etPinDig1.addTextChangedListener(PinTextWatcher(etPinDig1))
                etPinDig2.addTextChangedListener(PinTextWatcher(etPinDig2))
                etPinDig3.addTextChangedListener(PinTextWatcher(etPinDig3))
                etPinDig4.addTextChangedListener(PinTextWatcher(etPinDig4))

                /*For digit deletion on pressing del key*/
                etPinDig1.setOnKeyListener(this@ParentalPinSetupFragment)
                etPinDig2.setOnKeyListener(this@ParentalPinSetupFragment)
                etPinDig3.setOnKeyListener(this@ParentalPinSetupFragment)
                etPinDig4.setOnKeyListener(this@ParentalPinSetupFragment)
            }
        }
    }

    inner class PinTextWatcher constructor(private val view: View) : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            binding.clEtContainerParentalPinSetup.apply {
                when (view.id) {
                    etPinDig1.id -> {
                        if (text.length == 1)
                            etPinDig2.requestFocus()
                    }
                    etPinDig2.id -> {
                        if (text.length == 1)
                            etPinDig3.requestFocus()
                        else if (text.isEmpty())
                            etPinDig1.requestFocus()
                    }
                    etPinDig3.id -> {
                        if (text.length == 1)
                            etPinDig4.requestFocus()
                        else if (text.isEmpty())
                            etPinDig2.requestFocus()
                    }
                    etPinDig4.id -> {
                        if (text.isEmpty())
                            etPinDig3.requestFocus()
                        else
                            binding.root.closeKeyboard()
                    }
                }
            }
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
        }

        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
            binding.btnProceedParentalPinSetup.isEnabled = getPin().length == 4
        }
    }

    private fun getPin(): String {
        val stringBuilder = StringBuilder()
        binding.clEtContainerParentalPinSetup.apply {
            stringBuilder.append(etPinDig1.text)
            stringBuilder.append(etPinDig2.text)
            stringBuilder.append(etPinDig3.text)
            stringBuilder.append(etPinDig4.text)
        }
        return stringBuilder.toString()
    }
}