package com.tatasky.binge.ui.features.parentalcontrol.bottomsheet

import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.tatasky.binge.R
import com.tatasky.binge.data.receiver.SMSBroadcastReceiver
import com.tatasky.binge.databinding.FragmentParentalControlDialogOtpBinding
import com.tatasky.binge.interfaces.OTPReceiveListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.base.frameworks.extensions.disable
import com.tatasky.binge.ui.base.frameworks.extensions.enable
import com.tatasky.binge.ui.base.frameworks.extensions.invisible
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.GuestLoginViewModel
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import com.tatasky.binge.utils.*
import java.util.*
import javax.inject.Inject

class ParentalControlDialogOtpFragment :
    BaseFragment<FragmentParentalControlDialogOtpBinding, ParentalControlViewModel>(),
    OTPReceiveListener, View.OnKeyListener {

    private var isFromAutoFillOtp = false
    private var resendCount: Int = 0

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory


    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    private lateinit var guestLoginViewModel: GuestLoginViewModel

    private var smsReceiver: SMSBroadcastReceiver? = null

    override fun getViewModelClass(): Class<ParentalControlViewModel> =
        ParentalControlViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_parental_control_dialog_otp

    override fun getViewModelOwner(): ViewModelStoreOwner =
        requireParentFragment().requireParentFragment()


    override fun onKey(p0: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        //Handling for deleting OTP and regaining the focus after deletion
        handleOTPKey(binding.layoutLoginOTP.clEtOtpContainer, p0, keyCode, keyEvent)
        // Identifying with the last input, if user is entering the otp using keyboard
        if (p0 == binding.layoutLoginOTP.clEtOtpContainer.etOtpDig6 && keyEvent?.action == KeyEvent.ACTION_UP)
            isFromAutoFillOtp = false
        return false
    }

    override fun setObserver() {
        guestLoginViewModel.progressListener.observe(viewLifecycleOwner) {
            viewModel.setProgressing(it)
        }

        guestLoginViewModel.generateOtpResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                setOtp("")
                resendCount++
                val configCount = viewModel.sharedPrefs.getOtpResentCount()
                if (resendCount >= configCount) {
                    binding.layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.disable()
                    binding.layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.invisible()
                } else {
                    guestLoginViewModel.timer?.start()
                    binding.layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.disable()
                }
            }
        }

        guestLoginViewModel.generateOtpResponseError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                onError(errorModel)
            }
        }

        guestLoginViewModel.validateOtpResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                viewModel.authTokenValue = it.data?.userAuthenticateToken ?: ""
                findNavController().navigateSafe(
                    ParentalControlDialogOtpFragmentDirections.actionParentalControlDialogOtpFragmentToParentalControlVerificationFragment(
                        isPinVerificationRequested = viewModel.actionBeforeOpeningBottomSheet == ACTION_PIN_VERIFICATION,
                        isPinResetRequested = viewModel.actionBeforeOpeningBottomSheet == ACTION_PIN_FORGOT,
                        isPinSetupRequested = viewModel.actionBeforeOpeningBottomSheet == ACTION_PIN_CREATE,
                        isPinChangeRequested = viewModel.actionBeforeOpeningBottomSheet == ACTION_PIN_CHANGE
                    )
                )
            }
        }

        guestLoginViewModel.validateOtpResponseError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                when (errorModel.code) {
                    40008, 60001, 60003 -> {
                        setError(true, errorModel.message)
                        setOtp("")
                    }
                    else -> {
                        onError(errorModel)
                    }
                }
            }
        }
    }

    override fun toBeCalledOnce() {
        guestLoginViewModel =
            ViewModelProvider(
                this,
                mViewModelFactory
            )[GuestLoginViewModel::class.java]

        binding.header.tvHeaderTitle.text = getString(R.string.enter_otp)
        context?.let { con ->
            binding.header.logo.setImageDrawable(getDrawable(con, R.drawable.ic_lock))
        }
        String.format(
            Locale.US,
            getString(R.string.text_subtitle_guest_login_verify_otp),
            sharedPrefs.getClearRMN().maskPhoneNumber()
        ).let { subtitle ->
            binding.layoutLoginOTP.tvSubtitleGuestLoginVerifyOtp.text = subtitle
        }
        guestLoginViewModel.rmn = sharedPrefs.getClearRMN()
        setTimer()

        when (viewModel.actionBeforeOpeningBottomSheet) {
            ACTION_PIN_VERIFICATION, ACTION_RATING_CHANGE -> {
                findNavController().navigateSafe(
                    ParentalControlDialogOtpFragmentDirections.actionParentalControlDialogOtpFragmentToParentalControlVerificationFragment(
                        isPinChangeRequested = false,
                        isPinSetupRequested = false,
                        isPinResetRequested = false,
                        isPinVerificationRequested = true
                    )
                )
            }
            else -> {
                loginAnalytics.trackParentalPinOtpInitiate(
                    viewModel.getSource(viewModel.actionBeforeOpeningBottomSheet)
                )
                guestLoginViewModel.generateOtp()
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
        binding.layoutLoginOTP.apply {
            tvResendOtpGuestLoginVerifyOtp.setOnClickListener {
                setError(false)
                setOtp("")
                loginAnalytics.trackParentalPinOtpInitiate(
                    viewModel.getSource(viewModel.actionBeforeOpeningBottomSheet)
                )
                guestLoginViewModel.generateOtp()
                it.disable()
            }

            clEtOtpContainer.apply {
                etOtpDig1.addTextChangedListener(OtpTextWatcher(etOtpDig1))
                etOtpDig2.addTextChangedListener(OtpTextWatcher(etOtpDig2))
                etOtpDig3.addTextChangedListener(OtpTextWatcher(etOtpDig3))
                etOtpDig4.addTextChangedListener(OtpTextWatcher(etOtpDig4))
                etOtpDig5.addTextChangedListener(OtpTextWatcher(etOtpDig5))
                etOtpDig6.addTextChangedListener(OtpTextWatcher(etOtpDig6))

                etOtpDig1.setOnClickListener { setError(false) }
                etOtpDig2.setOnClickListener { setError(false) }
                etOtpDig3.setOnClickListener { setError(false) }
                etOtpDig4.setOnClickListener { setError(false) }
                etOtpDig5.setOnClickListener { setError(false) }
                etOtpDig6.setOnClickListener { setError(false) }

                /*For digit deletion on pressing del key*/
                etOtpDig1.setOnKeyListener(this@ParentalControlDialogOtpFragment)
                etOtpDig2.setOnKeyListener(this@ParentalControlDialogOtpFragment)
                etOtpDig3.setOnKeyListener(this@ParentalControlDialogOtpFragment)
                etOtpDig4.setOnKeyListener(this@ParentalControlDialogOtpFragment)
                etOtpDig5.setOnKeyListener(this@ParentalControlDialogOtpFragment)
                etOtpDig6.setOnKeyListener(this@ParentalControlDialogOtpFragment)
            }
        }
        startSMSListener()
    }

    override fun onOTPReceived(otp: String) {
        setOtp(otp, true)
    }

    override fun onOTPTimeOut() {
        d(this.javaClass.simpleName, "OTP Timeout")
    }

    private fun startSMSListener() {
        try {
            if (smsReceiver?.isRegistered == true) return
            d("OTP_Autofill", "inside startSMSListener")
            smsReceiver = null
            smsReceiver = SMSBroadcastReceiver()
            smsReceiver?.initOTPListener(this)
            smsReceiver?.let { it.registerSMSReceiver(context, it) }
            val client = SmsRetriever.getClient(requireActivity())

            val task = client.startSmsRetriever()
            task.addOnSuccessListener {
                // API successfully started
                // Show something like: Waiting for the OTP
                d("TAG", "SMS Retriever API Started ")
            }

            task.addOnFailureListener {
                // Fail to start API
                e("TAG", it.localizedMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun validateOtp() {
        loginAnalytics.trackParentalPinOtpProceed(viewModel.getSource(viewModel.actionBeforeOpeningBottomSheet))
        guestLoginViewModel.otp = getOtp()
        guestLoginViewModel.validateOtp(isFromAutoFillOtp)
    }

    private fun setError(enabled: Boolean, errorMsg: String? = null) {
        binding.apply {
            layoutLoginOTP.tvErrorGuestLoginVerifyOtp.text = errorMsg ?: ""
            layoutLoginOTP.tvErrorGuestLoginVerifyOtp.visibility = if (enabled) View.VISIBLE else View.GONE
            layoutLoginOTP.tvCodeExpiryGuestLoginVerifyOtp.visibility =
                if (!enabled) View.VISIBLE else View.GONE
            layoutLoginOTP.clEtOtpContainer.apply {
                etOtpDig1.background.level = if (enabled) 1 else 0
                etOtpDig2.background.level = if (enabled) 1 else 0
                etOtpDig3.background.level = if (enabled) 1 else 0
                etOtpDig4.background.level = if (enabled) 1 else 0
                etOtpDig5.background.level = if (enabled) 1 else 0
                etOtpDig6.background.level = if (enabled) 1 else 0
            }
        }
    }

    private fun getOtp(): String {
        val stringBuilder = StringBuilder()
        binding.layoutLoginOTP.clEtOtpContainer.apply {
            stringBuilder.append(etOtpDig1.text)
            stringBuilder.append(etOtpDig2.text)
            stringBuilder.append(etOtpDig3.text)
            stringBuilder.append(etOtpDig4.text)
            stringBuilder.append(etOtpDig5.text)
            stringBuilder.append(etOtpDig6.text)
        }
        return stringBuilder.toString()
    }

    private fun setOtp(otp: String, isFromAutoFillOtp: Boolean = false) {
        if (otp.length == 6) {
            this.isFromAutoFillOtp = isFromAutoFillOtp
            binding.layoutLoginOTP.clEtOtpContainer.apply {
                etOtpDig1.text = Editable.Factory.getInstance().newEditable(otp[0].toString())
                etOtpDig2.text = Editable.Factory.getInstance().newEditable(otp[1].toString())
                etOtpDig3.text = Editable.Factory.getInstance().newEditable(otp[2].toString())
                etOtpDig4.text = Editable.Factory.getInstance().newEditable(otp[3].toString())
                etOtpDig5.text = Editable.Factory.getInstance().newEditable(otp[4].toString())
                etOtpDig6.text = Editable.Factory.getInstance().newEditable(otp[5].toString())
            }
        } else if (otp.isEmpty()) {
            binding.layoutLoginOTP.clEtOtpContainer.apply {
                etOtpDig1.text.clear()
                etOtpDig2.text.clear()
                etOtpDig3.text.clear()
                etOtpDig4.text.clear()
                etOtpDig5.text.clear()
                etOtpDig6.text.clear()
                etOtpDig1.requestFocus()
            }
        }
    }

    inner class OtpTextWatcher constructor(private val view: View) : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            binding.layoutLoginOTP.clEtOtpContainer.apply {
                when (view.id) {
                    etOtpDig1.id -> {
                        when (text.length) {
                            1 -> etOtpDig2.requestFocus()
                            /*Enable OTP paste, Removed max length from Edit text for first field*/
                            6 -> setOtp(text) //If copied text is 6 digit set it
                            else -> setOtp("") //If copied text is other than 1 or 6 digit clear OTP field
                        }
                    }
                    etOtpDig2.id -> {
                        if (text.length == 1)
                            etOtpDig3.requestFocus()
                        else if (text.isEmpty())
                            etOtpDig1.requestFocus()
                    }
                    etOtpDig3.id -> {
                        if (text.length == 1)
                            etOtpDig4.requestFocus()
                        else if (text.isEmpty())
                            etOtpDig2.requestFocus()
                    }
                    etOtpDig4.id -> {
                        if (text.length == 1)
                            etOtpDig5.requestFocus()
                        else if (text.isEmpty())
                            etOtpDig3.requestFocus()
                    }
                    etOtpDig5.id -> {
                        if (text.length == 1)
                            etOtpDig6.requestFocus()
                        else if (text.isEmpty())
                            etOtpDig4.requestFocus()
                    }
                    etOtpDig6.id -> {
                        if (text.length == 1)
                            etOtpDig5.requestFocus()
                        else if(text.isEmpty()){
                            etOtpDig5.clearFocus()
                            binding.root.closeKeyboard()
                        }
                    }
                }
            }
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
        }

        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
            if (getOtp().length == 6) validateOtp()
        }
    }

    private fun setTimer() {
        guestLoginViewModel.timer =
            object : CountDownTimer(guestLoginViewModel.OTP_RESEND_DURATION.toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    var secs = (millisUntilFinished / 1000)

                    if (secs == 0L) {
                        secs = 1L
                    }
                    guestLoginViewModel.resendDurationLeft = secs
                    context?.let {
                        binding.layoutLoginOTP.tvCodeExpiryGuestLoginVerifyOtp.text =
                            String.format(
                                Locale.US,
                                it.getString(R.string.otp_resend_expire_msg),
                                secs.toString() + ""
                            )
                    }

                }

                override fun onFinish() {
                    binding.layoutLoginOTP.tvCodeExpiryGuestLoginVerifyOtp.text = ""
                    binding.layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.enable()
                }
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        guestLoginViewModel.timer?.cancel()
        smsReceiver?.let { it.unregisterSMSReceiver(context, it) }
    }
}