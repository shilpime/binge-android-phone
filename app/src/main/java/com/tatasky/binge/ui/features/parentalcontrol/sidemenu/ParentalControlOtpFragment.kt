package com.tatasky.binge.ui.features.parentalcontrol.sidemenu
//
//import android.content.IntentFilter
//import android.os.Bundle
//import android.os.CountDownTimer
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import androidx.databinding.DataBindingUtil
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.ViewModelStoreOwner
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import com.google.android.gms.auth.api.phone.SmsRetriever
//import com.tatasky.binge.R
//import com.tatasky.binge.data.receiver.SMSBroadcastReceiver
//import com.tatasky.binge.databinding.FragmentParentalControlOtpBinding
//import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
//import com.tatasky.binge.interfaces.OTPReceiveListener
//import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
//import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
//import com.tatasky.binge.ui.base.frameworks.extensions.disable
//import com.tatasky.binge.ui.base.frameworks.extensions.enable
//import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.GuestLoginViewModel
//import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
//import com.tatasky.binge.utils.d
//import com.tatasky.binge.utils.e
//import com.tatasky.binge.utils.navigateSafe
//import com.tatasky.binge.utils.showCustomToast
//import java.util.*
//import javax.inject.Inject
//
//class ParentalControlOtpFragment :
//    BaseFragment<FragmentParentalControlOtpBinding, ParentalControlViewModel>(),
//    OTPReceiveListener {
//    @Inject
//    lateinit var mViewModelFactory: ViewModelProvider.Factory
//
//    private val parentalControlOtpFragmentArgs by navArgs<ParentalControlOtpFragmentArgs>()
//
//    private lateinit var guestLoginViewModel: GuestLoginViewModel
//
//    private var smsReceiver: SMSBroadcastReceiver? = null
//
//    override fun getViewModelClass(): Class<ParentalControlViewModel> =
//        ParentalControlViewModel::class.java
//
//    override fun layoutId(): Int = R.layout.fragment_parental_control_otp
//
//    override fun getViewModelOwner(): ViewModelStoreOwner =
//        requireParentFragment().requireParentFragment()
//
//    override fun setObserver() {
//        guestLoginViewModel.progressListener.observe(viewLifecycleOwner) {
//            viewModel.setProgressing(it)
//        }
//
//        guestLoginViewModel.generateOtpResponse.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let {
//                setOtp("")
//                guestLoginViewModel.timer?.start()
//            }
//        }
//
//        guestLoginViewModel.generateOtpResponseError.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let { errorModel ->
//                onError(errorModel)
//            }
//        }
//
////        guestLoginViewModel.validateOtpResponse.observe(viewLifecycleOwner) {
////            it.getContentIfNotHandled()?.let {
////                viewModel.saveParentalPin(
////                    parentalPinValue = guestLoginViewModel.parentalPinValue,
////                    otp = guestLoginViewModel.otp,
////                    isLogin = true
////                )
////            }
////        }
//
////        guestLoginViewModel.validateOtpResponseError.observe(viewLifecycleOwner) {
////            it.getContentIfNotHandled()?.let { errorModel ->
////                when (errorModel.code) {
////                    40008, 60001 -> {
////                        setError(true, errorModel.message)
////                        setOtp("")
////                    }
////                    else -> {
////                        onError(errorModel)
////                    }
////                }
////            }
////        }
//
//        viewModel.saveParentalPinResponse.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let {
//                when {
//                    parentalControlOtpFragmentArgs.isParentalPinChangeRequested -> {
//                        //upon successfully saving parental pin take user to the settings screen
//
//                        val view = DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
//                            LayoutInflater.from(context),
//                            R.layout.layout_toast_success_failure,
//                            null,
//                            false
//                        )
//                        view.textLoginSuccessfulToast.text =
//                            getString(R.string.toast_msg_pin_changed_successful)
//                        view.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
//                        showCustomToast(context, view?.root, Gravity.FILL_HORIZONTAL)
//
//                        sharedPrefs.setParentalPinExists(true)
//                        findNavController().navigateSafe(
//                            ParentalControlOtpFragmentDirections.actionParentalControlOtpFragmentToParentalControlSettingsFragmentForPinChange()
//                        )
//                    }
//                    parentalControlOtpFragmentArgs.isParentalPinResetRequested -> {
//                        sharedPrefs.setParentalControlEnabled(false)
//                        findNavController().navigateSafe(
//                            ParentalControlOtpFragmentDirections.actionParentalControlOtpFragmentToParentalControlSettingsFragmentForPinChange()
//                        )
//                    }
////                    else -> {
////                        sharedPrefs.setParentalPinExists(true)
////                        //upon successfully saving parental pin take user to the settings screen
////                        findNavController().navigateSafe(
////                            ParentalControlOtpFragmentDirections.actionParentalControlOtpFragmentToParentalControlRatingFragment()
////                        )
////                    }
//                }
//            }
//        }
//
//        viewModel.saveParentalPinError.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let { errorModel ->
//                onError(errorModel)
//            }
//        }
//    }
//
//    override fun toBeCalledOnce() {
//        guestLoginViewModel =
//            ViewModelProvider(
//                requireParentFragment().requireParentFragment(),
//                mViewModelFactory
//            )[GuestLoginViewModel::class.java]
//
//        when {
//            parentalControlOtpFragmentArgs.isParentalPinChangeRequested -> {
//                binding.tvHeaderTitleFragmentParentalControlOtp.text =
//                    getString(R.string.title_parental_pin_change)
//            }
//            parentalControlOtpFragmentArgs.isParentalPinResetRequested -> {
//                binding.tvHeaderTitleFragmentParentalControlOtp.text =
//                    getString(R.string.title_parental_pin_reset)
//            }
//        }
//
//        guestLoginViewModel.rmn = sharedPrefs.getClearRMN()
//
//        setTimer()
//        guestLoginViewModel.timer?.start()
//
////        guestLoginViewModel.generateOtpResponse.value?.peekContent()?.data?.mobileNumber?.let { mobNo ->
//        String.format(
//            Locale.US,
//            getString(R.string.text_subtitle_guest_login_verify_otp),
//            guestLoginViewModel.rmn
//        ).let { subtitle ->
//            binding.tvSubtitleFragmentParentalControlOtp.text = subtitle
//        }
////        }
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.apply {
//            tvResendOtpFragmentParentalControlOtp.setOnClickListener {
//                setError(false)
//                setOtp("")
//                guestLoginViewModel.generateOtp()
//                it.disable()
//            }
//
//            clEtOtpContainerFragmentParentalControlOtp.apply {
//                etOtpDig1.addTextChangedListener(OtpTextWatcher(etOtpDig1))
//                etOtpDig2.addTextChangedListener(OtpTextWatcher(etOtpDig2))
//                etOtpDig3.addTextChangedListener(OtpTextWatcher(etOtpDig3))
//                etOtpDig4.addTextChangedListener(OtpTextWatcher(etOtpDig4))
//                etOtpDig5.addTextChangedListener(OtpTextWatcher(etOtpDig5))
//                etOtpDig6.addTextChangedListener(OtpTextWatcher(etOtpDig6))
//
//                etOtpDig1.setOnClickListener { setError(false) }
//                etOtpDig2.setOnClickListener { setError(false) }
//                etOtpDig3.setOnClickListener { setError(false) }
//                etOtpDig4.setOnClickListener { setError(false) }
//                etOtpDig5.setOnClickListener { setError(false) }
//                etOtpDig6.setOnClickListener { setError(false) }
//            }
//        }
//
//        startSMSListener()
//    }
//
//
//    override fun onOTPReceived(otp: String) {
//        setOtp(otp)
//    }
//
//    override fun onOTPTimeOut() {
//        d(this.javaClass.simpleName, "OTP Timeout")
//    }
//
//    private fun startSMSListener() {
//        try {
//            smsReceiver = SMSBroadcastReceiver()
//            smsReceiver?.initOTPListener(this)
//            d("TAG", "inside startSMSListener")
//
//            val intentFilter = IntentFilter()
//            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
//            activity?.registerReceiver(smsReceiver!!, intentFilter)
//
//            val client = SmsRetriever.getClient(requireActivity())
//
//            val task = client.startSmsRetriever()
//            task.addOnSuccessListener {
//                // API successfully started
//                // Show something like: Waiting for the OTP
//                d("TAG", "SMS Retriever API Started ")
//            }
//
//            task.addOnFailureListener {
//                // Fail to start API
//                e("TAG", it.localizedMessage)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun validateOtp() {
//        val otp = getOtp()
////        if (otp.length == 6) {
//        guestLoginViewModel.otp = otp
////        guestLoginViewModel.validateOtp()
//        if (parentalControlOtpFragmentArgs.isParentalPinResetRequested
//            || parentalControlOtpFragmentArgs.isParentalPinChangeRequested
//        ) {
////            guestLoginViewModel.validateOtp()
//            viewModel.saveParentalPin(
//                parentalPinValue = guestLoginViewModel.parentalPinValue,
//                otp = guestLoginViewModel.otp,
//                isLogin = true
//            )
//        } else {
//            findNavController().navigateSafe(
//                ParentalControlOtpFragmentDirections.actionParentalControlOtpFragmentToParentalControlRatingFragment()
//            )
//        }
////        } else {
////            setError(true, "Incorrect OTP")
////        }
//    }
//
//    private fun setError(enabled: Boolean, errorMsg: String? = null) {
//        binding.apply {
//            tvErrorFragmentParentalControlOtp.text = errorMsg ?: ""
//            tvErrorFragmentParentalControlOtp.visibility = if (enabled) View.VISIBLE else View.GONE
//            tvCodeExpiryFragmentParentalControlOtp.visibility =
//                if (!enabled) View.VISIBLE else View.GONE
//            clEtOtpContainerFragmentParentalControlOtp.apply {
//                etOtpDig1.background.level = if (enabled) 1 else 0
//                etOtpDig2.background.level = if (enabled) 1 else 0
//                etOtpDig3.background.level = if (enabled) 1 else 0
//                etOtpDig4.background.level = if (enabled) 1 else 0
//                etOtpDig5.background.level = if (enabled) 1 else 0
//                etOtpDig6.background.level = if (enabled) 1 else 0
//            }
//        }
//    }
//
//    private fun getOtp(): String {
//        val stringBuilder = StringBuilder()
//        binding.clEtOtpContainerFragmentParentalControlOtp.apply {
//            stringBuilder.append(etOtpDig1.text)
//            stringBuilder.append(etOtpDig2.text)
//            stringBuilder.append(etOtpDig3.text)
//            stringBuilder.append(etOtpDig4.text)
//            stringBuilder.append(etOtpDig5.text)
//            stringBuilder.append(etOtpDig6.text)
//        }
//        return stringBuilder.toString()
//    }
//
//    private fun setOtp(otp: String) {
//        if (otp.length == 6) {
//            binding.clEtOtpContainerFragmentParentalControlOtp.apply {
//                etOtpDig1.text = Editable.Factory.getInstance().newEditable(otp[0].toString())
//                etOtpDig2.text = Editable.Factory.getInstance().newEditable(otp[1].toString())
//                etOtpDig3.text = Editable.Factory.getInstance().newEditable(otp[2].toString())
//                etOtpDig4.text = Editable.Factory.getInstance().newEditable(otp[3].toString())
//                etOtpDig5.text = Editable.Factory.getInstance().newEditable(otp[4].toString())
//                etOtpDig6.text = Editable.Factory.getInstance().newEditable(otp[5].toString())
//            }
//        } else if (otp.isEmpty()) {
//            binding.clEtOtpContainerFragmentParentalControlOtp.apply {
//                etOtpDig1.text.clear()
//                etOtpDig2.text.clear()
//                etOtpDig3.text.clear()
//                etOtpDig4.text.clear()
//                etOtpDig5.text.clear()
//                etOtpDig6.text.clear()
//                etOtpDig1.requestFocus()
//            }
//        }
//    }
//
//    inner class OtpTextWatcher constructor(private val view: View) : TextWatcher {
//        override fun afterTextChanged(editable: Editable) {
//            val text = editable.toString()
//            binding.clEtOtpContainerFragmentParentalControlOtp.apply {
//                when (view.id) {
//                    etOtpDig1.id -> {
//                        if (text.length == 1)
//                            etOtpDig2.requestFocus()
//                    }
//                    etOtpDig2.id -> {
//                        if (text.length == 1)
//                            etOtpDig3.requestFocus()
//                        else if (text.isEmpty())
//                            etOtpDig1.requestFocus()
//                    }
//                    etOtpDig3.id -> {
//                        if (text.length == 1)
//                            etOtpDig4.requestFocus()
//                        else if (text.isEmpty())
//                            etOtpDig2.requestFocus()
//                    }
//                    etOtpDig4.id -> {
//                        if (text.length == 1)
//                            etOtpDig5.requestFocus()
//                        else if (text.isEmpty())
//                            etOtpDig3.requestFocus()
//                    }
//                    etOtpDig5.id -> {
//                        if (text.length == 1)
//                            etOtpDig6.requestFocus()
//                        else if (text.isEmpty())
//                            etOtpDig4.requestFocus()
//                    }
//                    etOtpDig6.id -> {
//                        if (text.isEmpty())
//                            etOtpDig5.requestFocus()
//                        else {
//                            binding.root.closeKeyboard()
//                        }
//                    }
//                }
//            }
//        }
//
//        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
//        }
//
//        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
//            if (getOtp().length == 6) validateOtp()
//        }
//    }
//
//    private fun setTimer() {
//        guestLoginViewModel.timer =
//            object : CountDownTimer(guestLoginViewModel.OTP_RESEND_DURATION.toLong(), 1000) {
//                override fun onTick(millisUntilFinished: Long) {
//                    var secs = (millisUntilFinished / 1000)
//
//                    if (secs == 0L) {
//                        secs = 1L
//                    }
//                    guestLoginViewModel.resendDurationLeft = secs
//                    context?.let {
//                        binding.tvCodeExpiryFragmentParentalControlOtp.text =
//                            String.format(
//                                Locale.US,
//                                it.getString(R.string.otp_resend_expire_msg),
//                                secs.toString() + ""
//                            )
//                    }
//
//                }
//
//                override fun onFinish() {
//                    binding.tvCodeExpiryFragmentParentalControlOtp.text = ""
//                    binding.tvResendOtpFragmentParentalControlOtp.enable()
//                }
//            }
//
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        guestLoginViewModel.timer?.cancel()
//        if (smsReceiver != null) {
//            activity?.unregisterReceiver(smsReceiver!!)
//        }
//    }
//}