//package com.tatasky.binge.ui.features.onboarding.login.bottomsheet
//
//import android.content.IntentFilter
//import android.os.Bundle
//import android.os.CountDownTimer
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.View
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.ViewModelStoreOwner
//import androidx.navigation.fragment.findNavController
//import com.google.android.gms.auth.api.phone.SmsRetriever
//import com.tatasky.binge.R
//import com.tatasky.binge.data.networking.models.requests.NewBingeUserRequest
//import com.tatasky.binge.data.networking.models.response.LoginResponse
//import com.tatasky.binge.data.networking.models.response.SubscriberIdListResponse
//import com.tatasky.binge.data.receiver.SMSBroadcastReceiver
//import com.tatasky.binge.databinding.FragmentGuestLoginVerifyOtpBinding
//import com.tatasky.binge.interfaces.OTPReceiveListener
//import com.tatasky.binge.ui.base.frameworks.SingleEvent
//import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
//import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
//import com.tatasky.binge.ui.base.frameworks.extensions.disable
//import com.tatasky.binge.ui.base.frameworks.extensions.enable
//import com.tatasky.binge.ui.features.myaccount.MyAccountViewModel
//import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.temp.GuestLoginBottomSheetResult
//import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
//import com.tatasky.binge.utils.*
//import java.util.*
//import javax.inject.Inject
//
//class GuestLoginOtpFragment :
//    BaseFragment<FragmentGuestLoginVerifyOtpBinding, GuestLoginViewModel>(), OTPReceiveListener {
//    private var selectedBingeUser: LoginResponse.BingeSubscription? = null
//
//    @Inject
//    lateinit var mViewModelFactory: ViewModelProvider.Factory
//
//    private lateinit var parentalControlViewModel: ParentalControlViewModel
//    private lateinit var navDrawerViewModel: MyAccountViewModel
//
//    private var smsReceiver: SMSBroadcastReceiver? = null
//
//    override fun getViewModelClass(): Class<GuestLoginViewModel> =
//        GuestLoginViewModel::class.java
//
//    override fun layoutId(): Int = R.layout.fragment_guest_login_verify_otp
//
//    override fun getViewModelOwner(): ViewModelStoreOwner =
//        requireParentFragment().requireParentFragment()
//
//    override fun setObserver() {
//        parentalControlViewModel.progressListener.observe(viewLifecycleOwner) {
//            viewModel.setProgressing(it)
//        }
//
//        viewModel.getSubLookupResponse().observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let { subList ->
//                viewModel.dthStatus = subList.data?.dthStatus ?: ""
//                var finalSidLisitng: List<SubscriberIdListResponse.SubscriberDetail>? = null
//                if(DTH_WO_BINGE_USER.equals(viewModel.dthStatus, true)){
//                    e("getSubLookupResponse","inside DTH_WO_BINGE_USER")
//                    e("getSubLookupResponse","subList.data?.nonDthSubscribersList?.isEmpty() : ${subList.data?.nonDthSubscribersList?.isEmpty()}")
//                    if(subList.data?.nonDthSubscribersList?.isEmpty() == true)
//                        finalSidLisitng = subList.data?.subscribersList
//                    else
//                        finalSidLisitng = subList.data?.nonDthSubscribersList
//                    e("getSubLookupResponse","inside finalSidLisitng size : ${finalSidLisitng?.size}")
//                }
//                else if (subList.data?.subscribersList?.isNotEmpty() == true) finalSidLisitng = subList.data?.subscribersList
//                else finalSidLisitng = subList.data?.nonDthSubscribersList
//                if(DTH_WO_BINGE_USER.equals(viewModel.dthStatus, true) && subList.data?.nonDthSubscribersList?.isEmpty() == true)
//                    viewModel.isCreateNonDth = true
//                if (finalSidLisitng == null || finalSidLisitng?.size == 0
//                    || finalSidLisitng!![0].listOfBaIds.isEmpty()
//                ) {
//                    e("GuestLoginOTP", "SID Listing inside createNew BingeMobileUser")
//                    viewModel.createNewBingeMobileUser(
//                        NewBingeUserRequest(
//                            login = viewModel.loginAuth,
//                            mobileNumber = viewModel.rmn,
//                            eulaChecked = true,
//                            isCreate = true
//                        )
//                    )
//                } else {
//                    finalSidLisitng?.let { it ->
//                        e("GuestLoginOTP", "SID Listing inside it.size : ${it.size}")
//                        if (it.size == 1) {
//                            val subObj = it[0]
//                            viewModel.createNewBingeMobileUser(
//                                NewBingeUserRequest(
//                                    login = viewModel.loginAuth,
//                                    subscriberId = subObj.sid,
//                                    bingeSubscriberId = subObj.sid,
//                                    mobileNumber = viewModel.rmn,
//                                    baId = subObj.listOfBaIds[0].baId,
//                                    isCreate = false
//                                )
//                            )
//                            viewModel.nosBaids = it[0].listOfBaIds.size
//                            when {
//                                it[0].listOfBaIds.isEmpty() || (it[0].listOfBaIds.size == 1 && it[0].listOfBaIds[0].baId == null) -> {
//                                    viewModel.loginDTO.sid = it[0].sid
//                                    viewModel.loginDTO.email =
//                                        it[0].email //it.bingeUserData?.emailId
//                                    viewModel.loginDTO.name =
//                                        it[0].name //it.bingeUserData?.aliasName
//                                    viewModel.loginDTO.rmn = it[0].rmn ?: ""
//
//                                }
//                                it[0].listOfBaIds.size == 1 -> {
//                                    this.selectedBingeUser = it[0].listOfBaIds[0]
////                                checkDTHStatus(it[0].dthStatus ?: "",it[0].accountSubStatus, selectedBingeUser?.subscriptionDetailInfo?.bingeAccountStatus?:"", selectedBingeUser?.subscriptionDetailInfo?.isPaid?:false, selectedBingeUser?.subscriptionDetailInfo?.migrated?:false, selectedBingeUser!!, it[0].sid)
//                                }
//                                else -> {
//                                    viewModel.setBAidAdapter(it[0].listOfBaIds)
//                                    //move to select BAID List Screen
//                                }
//                            }
//                        } else {
//                            e("GuestLoginOTP", "SID Listing inside it.size : ${it.size}")
//                            viewModel.setSelectSidAdapter(it)
//                            findNavController().navigateSafe(GuestLoginOtpFragmentDirections.actionGuestOtpToSidListing())
//                        }
//                    }
//                }
//            }
//        }
//
//        viewModel.generateOtpResponse.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let {
//                setOtp("")
//                viewModel.timer?.start()
//            }
//        }
//
//        viewModel.generateOtpResponseError.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let { errorModel ->
//                onError(errorModel)
//            }
//        }
//
//        viewModel.validateOtpResponse.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let {
//                when {
//                    viewModel.isLoggedIn && viewModel.isParentalPinSetupRequested -> {
//                        //save parental pin if already logged in and pin setup process was initiated
//                        parentalControlViewModel.saveParentalPin(
//                            parentalPinValue = viewModel.parentalPinValue,
//                            otp = viewModel.otp,
//                            isLogin = true
//                        )
//                    }
//                    viewModel.isParentalPinResetRequested -> {
//                        //save parental pin if already logged in and pin reset process was initiated
//                        parentalControlViewModel.saveParentalPin(
//                            parentalPinValue = viewModel.parentalPinValue,
//                            otp = viewModel.otp,
//                            isLogin = true
//                        )
//                    }
//                    else -> {
//                        viewModel.loginDTO.rmn = viewModel.rmn
//                        viewModel.saveLoginToken(it)
//                        viewModel.fetchSubIdList()
//                    }
//                }
//            }
//        }
//
//        viewModel.validateOtpResponseError.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let { errorModel ->
//                when (errorModel.code) {
//                    40008, 60001 -> {
//                        setError(true, errorModel.message)
//                        setOtp("")
//                    }
//                    else -> {
//                        onError(errorModel)
//                    }
//                }
//            }
//        }
//
//        parentalControlViewModel.saveParentalPinResponse.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let {
//                when {
//                    viewModel.isParentalPinSetupRequested -> {
//                        //upon successfully saving parental pin take user to the success screen
//                        sharedPrefs.setParentalPinExists(true)
//                        findNavController().navigateSafe(GuestLoginFragmentDirections.actionGlobalParentalPinSuccessFragment())
//                    }
//                    viewModel.isParentalPinResetRequested -> {
//                        sharedPrefs.setParentalControlEnabled(false)
//                        viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
//                    }
//                }
//            }
//        }
//
//        parentalControlViewModel.saveParentalPinError.observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let { errorModel ->
//                onError(errorModel)
//            }
//        }
//
//        viewModel.getExistingBingeUserLoginResponse().observe(viewLifecycleOwner) { obj ->
//            obj.getContentIfNotHandled()?.let {
//                viewModel.saveLoggedInDetails(it)
//                sharedPrefs.setParentalPinExists(selectedBingeUser?.parentalPinExist == true)
//                sharedPrefs.setParentalRating(selectedBingeUser?.parentalPinRating ?: "")
//
//                if (viewModel.isParentalPinSetupRequested && selectedBingeUser?.parentalPinExist == true) {
//                    //if user is not logged in and parental pin setup requested and parental pin already exists then take user to pin success screen
//                    findNavController().navigateSafe(GuestLoginOtpFragmentDirections.actionGlobalParentalPinSuccessFragment())
//                } else if (viewModel.isParentalPinSetupRequested && selectedBingeUser?.parentalPinExist != true) {
//                    //if user is not logged in and parental pin setup requested and parental pin does not exists then take user to pin setup screen
//                    findNavController().navigateSafe(GuestLoginOtpFragmentDirections.actionGlobalParentalPinSetupFragment())
//                } else {
//                    //if parental pin setup not requested then just save login details and dismiss dialog
//                    navDrawerViewModel.fetchProfileInfo()
////                    viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
//                }
//            }
//        }
//
//        navDrawerViewModel.getProfileInfo().observe(this) {
//            it.getContentIfNotHandled().let {
//                when (sharedPrefs.getDthStatusFreemium()) {
//                    DTH_WO_BINGE_USER, DTH_W_BINGE_USER -> {
//                        navDrawerViewModel.fetchBalance()
//                    }
//                    else -> {
//                        viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
//                    }
//                }
//            }
//        }
//
//        navDrawerViewModel.getWalletBalance().observe(this) {
//            it.getContentIfNotHandled().let {
//                viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
//            }
//        }
//    }
//
//    override fun toBeCalledOnce() {
//        parentalControlViewModel =
//            ViewModelProvider(
//                requireParentFragment().requireParentFragment(),
//                mViewModelFactory
//            )[ParentalControlViewModel::class.java]
//
//        navDrawerViewModel =
//            ViewModelProvider(
//                requireActivity(),
//                mViewModelFactory
//            )[MyAccountViewModel::class.java]
//
//        when {
//            viewModel.isParentalPinSetupRequested && !viewModel.isLoggedIn -> {
//                binding.header.tvHeaderTitle.text =
//                    getString(R.string.header_title_parental_pin_setup)
//            }
//            viewModel.isParentalPinSetupRequested && viewModel.isLoggedIn -> {
//                binding.header.logo.setImageResource(R.drawable.ic_lock)
//                binding.header.tvHeaderTitle.text = getString(R.string.title_parental_pin_setup)
//                viewModel.rmn = sharedPrefs.getRMN()
//            }
//            viewModel.isParentalPinResetRequested -> {
//                binding.header.tvHeaderTitle.text = getString(R.string.title_parental_pin_reset)
//            }
//        }
//
//        setTimer()
//        viewModel.timer?.start()
//
//        viewModel.generateOtpResponse.value?.peekContent()?.data?.mobileNumber?.let { mobNo ->
//            String.format(
//                Locale.US,
//                getString(R.string.text_subtitle_guest_login_verify_otp),
//                mobNo
//            ).let { subtitle ->
//                binding.tvSubtitleGuestLoginVerifyOtp.text = subtitle
//            }
//        }
//
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        /*overriding loader set on activity via BaseFragment*/
//        showProgress = Runnable { }
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.apply {
//            tvResendOtpGuestLoginVerifyOtp.setOnClickListener {
//                setError(false)
//                setOtp("")
//                viewModel.generateOtp()
//                it.disable()
//            }
//
//            clEtOtpContainer.apply {
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
//        startSMSListener()
//    }
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
//        viewModel.otp = otp
//        viewModel.validateOtp()
////        } else {
////            setError(true, "Incorrect OTP")
////        }
//    }
//
//    private fun setError(enabled: Boolean, errorMsg: String? = null) {
//        binding.apply {
//            tvErrorGuestLoginVerifyOtp.text = errorMsg ?: ""
//            tvErrorGuestLoginVerifyOtp.visibility = if (enabled) View.VISIBLE else View.GONE
//            tvCodeExpiryGuestLoginVerifyOtp.visibility =
//                if (!enabled) View.VISIBLE else View.GONE
//            clEtOtpContainer.apply {
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
//        binding.clEtOtpContainer.apply {
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
//            binding.clEtOtpContainer.apply {
//                etOtpDig1.text = Editable.Factory.getInstance().newEditable(otp[0].toString())
//                etOtpDig2.text = Editable.Factory.getInstance().newEditable(otp[1].toString())
//                etOtpDig3.text = Editable.Factory.getInstance().newEditable(otp[2].toString())
//                etOtpDig4.text = Editable.Factory.getInstance().newEditable(otp[3].toString())
//                etOtpDig5.text = Editable.Factory.getInstance().newEditable(otp[4].toString())
//                etOtpDig6.text = Editable.Factory.getInstance().newEditable(otp[5].toString())
//            }
//        } else if (otp.isEmpty()) {
//            binding.clEtOtpContainer.apply {
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
//            binding.clEtOtpContainer.apply {
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
//        viewModel.timer =
//            object : CountDownTimer(viewModel.OTP_RESEND_DURATION.toLong(), 1000) {
//                override fun onTick(millisUntilFinished: Long) {
//                    var secs = (millisUntilFinished / 1000)
//
//                    if (secs == 0L) {
//                        secs = 1L
//                    }
//                    viewModel.resendDurationLeft = secs
//                    context?.let {
//                        binding.tvCodeExpiryGuestLoginVerifyOtp.text =
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
//                    binding.tvCodeExpiryGuestLoginVerifyOtp.text = ""
//                    binding.tvResendOtpGuestLoginVerifyOtp.enable()
//                }
//            }
//
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        viewModel.timer?.cancel()
//        if (smsReceiver != null) {
//            activity?.unregisterReceiver(smsReceiver!!)
//        }
//    }
//}