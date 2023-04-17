package com.tatasky.binge.ui.features.parentalcontrol.sidemenu
//
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.View
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.ViewModelStoreOwner
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import com.tatasky.binge.R
//import com.tatasky.binge.databinding.FragmentParentalControlPinBinding
//import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
//import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
//import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.GuestLoginViewModel
//import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
//import com.tatasky.binge.utils.e
//import com.tatasky.binge.utils.navigateSafe
//import javax.inject.Inject
//
//class ParentalControlPinFragment :
//    BaseFragment<FragmentParentalControlPinBinding, ParentalControlViewModel>() {
//
//    @Inject
//    lateinit var mViewModelFactory: ViewModelProvider.Factory
//
//    private lateinit var guestLoginViewModel: GuestLoginViewModel
//    private val parentalControlPinFragmentArgs by navArgs<ParentalControlPinFragmentArgs>()
//
//    override fun getViewModelClass(): Class<ParentalControlViewModel> =
//        ParentalControlViewModel::class.java
//
//    override fun layoutId(): Int = R.layout.fragment_parental_control_pin
//
//    override fun getViewModelOwner(): ViewModelStoreOwner =
//        requireParentFragment().requireParentFragment()
//
//    override fun setObserver() {
//        guestLoginViewModel.progressListener.observe(viewLifecycleOwner) {
//            viewModel.setProgressing(it)
//        }
//
////        viewModel.validateParentalPinResponse.observe(viewLifecycleOwner) {
////            it.getContentIfNotHandled()?.let {
////                sharedPrefs.setParentalControlEnabled(false)
////                findNavController().navigateSafe(
////                    ParentalControlPinFragmentDirections.actionParentalControlPinFragmentToParentalControlSettingsFragment()
////                )
////            }
////        }
//
////        viewModel.validateParentalPinError.observe(viewLifecycleOwner) {
////            it.getContentIfNotHandled()?.let { errorModel ->
////                onError(errorModel)
////            }
////        }
//
//        guestLoginViewModel.generateOtpResponse.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let {
//                findNavController().navigateSafe(
//                    ParentalControlPinFragmentDirections.actionParentalControlPinFragmentToParentalControlOtpFragment(
//                        isParentalPinChangeRequested = parentalControlPinFragmentArgs.isParentalPinChangeRequested,
//                        isParentalPinResetRequested = parentalControlPinFragmentArgs.isParentalPinResetRequested
//                    )
//                )
//            }
//        }
//
//        guestLoginViewModel.generateOtpResponseError.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let { errorModel ->
//                onError(errorModel)
//            }
//        }
//    }
//
//    override fun toBeCalledOnce() {
//        when {
//            parentalControlPinFragmentArgs.isParentalPinResetRequested -> findNavController().currentDestination?.label =
//                getString(R.string.text_forgot_pin)
//            parentalControlPinFragmentArgs.isParentalPinChangeRequested -> findNavController().currentDestination?.label =
//                getString(R.string.text_change_pin)
//        }
//
//        guestLoginViewModel = ViewModelProvider(
//            requireParentFragment().requireParentFragment(),
//            mViewModelFactory
//        )[GuestLoginViewModel::class.java]
//
//        guestLoginViewModel.rmn = sharedPrefs.getClearRMN()
//
//        when {
////            parentalControlPinFragmentArgs.isParentalPinVerificationRequested -> {
////                binding.groupParentalControlPinVerification.visibility = View.VISIBLE
////                binding.btnCancelParentalControlPin.visibility = View.GONE
////                binding.tvHeaderTitleFragmentParentalControlPin.text =
////                    getString(R.string.title_parental_pin_control_verification)
////            }
//            parentalControlPinFragmentArgs.isParentalPinChangeRequested -> {
//                binding.tvHeaderTitleFragmentParentalControlPin.text =
//                    getString(R.string.title_parental_pin_change)
//            }
//
//            parentalControlPinFragmentArgs.isParentalPinResetRequested -> {
//                binding.tvHeaderTitleFragmentParentalControlPin.text =
//                    getString(R.string.title_parental_pin_reset)
//            }
//        }
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        e("ParentalPinSetup", "RMN : " + sharedPrefs.getRMN())
//        binding.apply {
//            btnProceedFragmentParentalControlPin.setOnClickListener {
//                if (getPin().length == 4) {
//                    guestLoginViewModel.parentalPinValue = getPin()
//                    when {
////                        parentalControlPinFragmentArgs.isParentalPinVerificationRequested -> {
////                            //if user has requested for pin verification then directly hit the pin verification api
////                            viewModel.validateParentalPin(
////                                parentalPinValue = guestLoginViewModel.parentalPinValue
////                            )
////                        }
//                        else -> {
//                            guestLoginViewModel.rmn = sharedPrefs.getClearRMN()
//                            guestLoginViewModel.generateOtp()
//                        }
//                    }
//                }
//            }
//
//            btnCancelParentalControlPin.setOnClickListener {
//                activity?.onBackPressed()
//            }
//
////            btnCancelParentalControlPinVerification.setOnClickListener {
////                activity?.onBackPressed()
////            }
//
////            btnForgotParentalControlPinVerification.setOnClickListener {
////                findNavController().navigateSafe(
////                    ParentalControlPinFragmentDirections.actionParentalControlPinFragmentSelf(
////                        isParentalPinVerificationRequested = false,
////                        isParentalPinChangeRequested = false,
////                        isParentalPinResetRequested = true
////                    )
////                )
////            }
//
//            clEtContainerParentalControlPin.apply {
//                etPinDig1.addTextChangedListener(PinTextWatcher(etPinDig1))
//                etPinDig2.addTextChangedListener(PinTextWatcher(etPinDig2))
//                etPinDig3.addTextChangedListener(PinTextWatcher(etPinDig3))
//                etPinDig4.addTextChangedListener(PinTextWatcher(etPinDig4))
//            }
//        }
//
//    }
//
//    inner class PinTextWatcher constructor(private val view: View) : TextWatcher {
//        override fun afterTextChanged(editable: Editable) {
//            val text = editable.toString()
//            binding.clEtContainerParentalControlPin.apply {
//                when (view.id) {
//                    etPinDig1.id -> {
//                        if (text.length == 1)
//                            etPinDig2.requestFocus()
//                    }
//                    etPinDig2.id -> {
//                        if (text.length == 1)
//                            etPinDig3.requestFocus()
//                        else if (text.isEmpty())
//                            etPinDig1.requestFocus()
//                    }
//                    etPinDig3.id -> {
//                        if (text.length == 1)
//                            etPinDig4.requestFocus()
//                        else if (text.isEmpty())
//                            etPinDig2.requestFocus()
//                    }
//                    etPinDig4.id -> {
//                        if (text.isEmpty())
//                            etPinDig3.requestFocus()
//                        else
//                            binding.root.closeKeyboard()
//                    }
//                }
//            }
//        }
//
//        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
//        }
//
//        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
//            binding.btnProceedFragmentParentalControlPin.isEnabled = getPin().length == 4
//        }
//    }
//
//
//    private fun getPin(): String {
//        val stringBuilder = StringBuilder()
//        binding.clEtContainerParentalControlPin.apply {
//            stringBuilder.append(etPinDig1.text)
//            stringBuilder.append(etPinDig2.text)
//            stringBuilder.append(etPinDig3.text)
//            stringBuilder.append(etPinDig4.text)
//        }
//        return stringBuilder.toString()
//    }
//}