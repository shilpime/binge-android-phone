package com.tatasky.binge.ui.features.onboarding.login.bottomsheet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.tatasky.binge.R
import com.tatasky.binge.analytics.MOBILE
import com.tatasky.binge.analytics.NEW
import com.tatasky.binge.analytics.PREVUSED
import com.tatasky.binge.analytics.SOURCE_LOGIN
import com.tatasky.binge.data.networking.models.requests.NewBingeUserRequest
import com.tatasky.binge.data.networking.models.response.GetOtpGuestLoginResponse
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.data.networking.models.response.SubscriberIdListResponse
import com.tatasky.binge.data.receiver.SMSBroadcastReceiver
import com.tatasky.binge.databinding.FragmentGuestLoginBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.interfaces.OTPReceiveListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity
import com.tatasky.binge.ui.features.device_management.DeviceListManagementAnalytics
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.onboarding.login.adapter.PartnerLogoRVAdapter
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.temp.GuestLoginBottomSheetResult
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import com.tatasky.binge.utils.*
import javax.inject.Inject
import kotlinx.coroutines.delay
import java.util.*

const val KEY_PREVIOUSLY_SELECTED_MOBILE = "KeyPreviouslySelectedMobile"

class GuestLoginFragment :
    BaseFragment<FragmentGuestLoginBinding, GuestLoginViewModel>(), OTPReceiveListener, View.OnKeyListener {

    private val guestLoginFragmentArgs by navArgs<GuestLoginFragmentArgs>()
    private var isFromAutoFillOtp = false
    private var isPastBingeUser: Boolean = false
    private var isCreate: Boolean = false
    private var dsn : String?= null
    private lateinit var userMobileNumber: String
    @Inject
    lateinit var loginAnalytics: LoginAnalytics


    @Inject
    lateinit var deviceListManagementAnalytics: DeviceListManagementAnalytics

    private var isEligibleForFreeTrial = true

    private var selectedBingeUser: LoginResponse.BingeSubscription? = null
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var parentalControlViewModel: ParentalControlViewModel
    private var smsReceiver: SMSBroadcastReceiver? = null

    override fun onKey(p0: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        //Handling for deleting OTP and regaining the focus after deletion
        handleOTPKey(binding.layoutLoginOTP.clEtOtpContainer, p0, keyCode, keyEvent)
        // Identifying with the last input, if user is entering the otp using keyboard
        if (p0 == binding.layoutLoginOTP.clEtOtpContainer.etOtpDig6 && keyEvent?.action == KeyEvent.ACTION_UP)
            isFromAutoFillOtp = false
        return false
    }


    override fun onOTPReceived(otp: String) {
        loginAnalytics.trackOTPReceived()
        setOtp(otp, true)
        smsReceiver?.let { it.unregisterSMSReceiver(context, it) }
    }

    override fun onOTPTimeOut() {
        d("OTP_Autofill", "OTP Timeout")
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
                d("OTP_Autofill", "SMS Retriever API Started ")
            }

            task.addOnFailureListener {
                // Fail to start API
                e("OTP_Autofill", it.localizedMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun validateOtp() {
        val otp = getOtp()
        viewModel.otp = otp
        loginAnalytics.trackOtpEnter(MOBILE)
        viewModel.validateOtp(isFromAutoFillOtp)
    }

    private fun setError(enabled: Boolean, errorMsg: String? = null, incorrectOtp: String? = null) {
        binding.apply {
            layoutLoginOTP.tvErrorGuestLoginVerifyOtp.text =
                incorrectOtp ?: getString(R.string.incorrect_otp)
            layoutLoginOTP.tvErrorGuestLoginVerifyOtp.visibility =
                if (enabled) View.VISIBLE else View.GONE
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
            /*Only for OTP copy paste Use case:
            Since for OTP copy paste 1st edittext is of 6 length, only return first digit else
            return empty char and trim retured OTP*/
            stringBuilder.append(etOtpDig1.text.getOrElse(0) { ' ' })
            stringBuilder.append(etOtpDig2.text)
            stringBuilder.append(etOtpDig3.text)
            stringBuilder.append(etOtpDig4.text)
            stringBuilder.append(etOtpDig5.text)
            stringBuilder.append(etOtpDig6.text)
        }
        return stringBuilder.toString().trim()
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
                            1 -> {
                                isFromAutoFillOtp = false
                                etOtpDig2.requestFocus()
                            }
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
                        else if (text.isEmpty()){
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

    private fun setTimer(resendOtpInVerbiage: String?= null) {
        viewModel.timer =
            object : CountDownTimer(viewModel.OTP_RESEND_DURATION.toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    var secs = (millisUntilFinished / 1000)

                    if (secs == 0L) {
                        secs = 1L
                    }
                    viewModel.resendDurationLeft = secs
                    context?.let {
                        binding.layoutLoginOTP.tvCodeExpiryGuestLoginVerifyOtp.apply {
                            text = resendOtpInVerbiage?.let {
                                String.format(
                                    Locale.US,
                                    it,
                                    secs.toString() + ""
                                )
                            }.run {
                                String.format(
                                    Locale.US,
                                    it.getString(R.string.text_code_expiry_guest_login_verify_otp),
                                    secs.toString() + ""
                                )
                            }
                        }
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
        try{
            viewModel.timer?.cancel()
            smsReceiver?.let { it.unregisterSMSReceiver(context, it) }
        }
        catch (e :Exception){}
    }

    override fun getViewModelClass(): Class<GuestLoginViewModel> =
        GuestLoginViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_guest_login

    override fun getViewModelOwner(): ViewModelStoreOwner =
        requireParentFragment().requireParentFragment()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DeviceListManagementActivity.DEVICE_LIMIT_REQUEST_CODE) {
            when (resultCode) { // If user's device removal is successful
                Activity.RESULT_OK ->
                    viewModel.createNewBingeMobileUser(
                        NewBingeUserRequest(
                            login = viewModel.loginAuth,
                            subscriberId = sharedPrefs.getTempSavedSid(),
                            bingeSubscriberId = selectedBingeUser?.bingeSubscriberId ,
                            mobileNumber = viewModel.rmn,
                            baId = selectedBingeUser?.baId,
                            isCreate = isCreate,
                            dthStatus = sharedPrefs.getTempDthStatus(),
                            isPastBingeUser = isPastBingeUser,
                            dsn = dsn,
                            packageId = viewModel.packageId,
                            referenceId = viewModel.referenceId,
                            cartId = viewModel.cartId
                        )
                    )
                Activity.RESULT_CANCELED ->
                    viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.FAILURE))
            }
        }
    }

    override fun setObserver() {
        viewModel.errorOkClicked.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                setOtp("")
            }
        }

        viewModel.getMaxDeviceLimitReachedResponse().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { maxDeviceLimitReachedResponse ->
                deviceListManagementAnalytics.trackDeviceLimitPopupShown(SOURCE_LOGIN)
                showDialog(
                    DialogModel(
                        false,
                        R.drawable.ic_device_center,
                        title = viewModel.getVerbiageFromConfig()?.device?.header,
                        viewModel.getVerbiageFromConfig()?.device?.review
                            ?: getString(R.string.review_devices),
                        secondaryButtonText = getString(R.string.text_non_underlined_Not_Now),
                        text = viewModel.getVerbiageFromConfig()?.device?.subHeader
                    ), object :
                        CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            deviceListManagementAnalytics.trackDeviceLimitPopupReviewClick(SOURCE_LOGIN)
                            hideDialog()
                            activity?.let { context ->
                                startActivityForResult(Intent(context, DeviceListManagementActivity::class.java).apply {
                                    putExtra(DeviceListManagementActivity.KEY_TEMP_BAID, selectedBingeUser?.baId)
                                    putExtra(DeviceListManagementActivity.KEY_IS_DEVICE_REVIEW_ON_MAX_LIMIT_REACHED, true)
                                }, DeviceListManagementActivity.DEVICE_LIMIT_REQUEST_CODE)
                            }
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                            deviceListManagementAnalytics.trackDeviceLimitPopupSkip(SOURCE_LOGIN)
                            viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.FAILURE))
                        }

                        override fun onCloseButtonClick() {
                            hideDialog()
                            viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.FAILURE))
                        }
                    })
            }
        }
        viewModel.generateOtpResponseError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                when (errorModel.code) {
                    20090 -> {
                        loginAnalytics.trackLoginRmnEnterInvalid(requireContext())
                        binding.etMobileGuestLogin.tilNumber.error = errorModel.message
                        binding.etMobileGuestLogin.et.background.level = 1
                    }
                    else -> onError(errorModel)
                }
            }
        }


        parentalControlViewModel.progressListener.observe(viewLifecycleOwner) {
            viewModel.setProgressing(it)
        }

        viewModel.getSubLookupResponse().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { subList ->
                var finalSidListing: List<SubscriberIdListResponse.SubscriberDetail>? = subList.subscribersList
                viewModel.sharedPrefs.addSubscriberIDListResponse(subList)
                e("getSubLookupResponse","inside finalSidLisitng size : ${finalSidListing?.size}")
                if (finalSidListing == null || finalSidListing.isEmpty()
                    || (finalSidListing.size == 1 && finalSidListing[0].listOfBaIds.isEmpty())
                ) {
                    e("GuestLoginOTP", "SID Listing inside createNew BingeMobileUser")
                    var sid = viewModel.rmn
                    var dthStatus = "Non DTH User"
                    var pastBingeUser = false
                    if(finalSidListing?.size?:0 > 0) {
                        sid = finalSidListing!![0].sid
                        dthStatus = finalSidListing[0].dthStatus?:"Non DTH User"
                        pastBingeUser = finalSidListing[0].isPastBingeUser ?: false
                        viewModel.referenceId = finalSidListing[0].referenceId
                    }
                    viewModel.createNewBingeMobileUser(
                        NewBingeUserRequest(
                            login = viewModel.loginAuth,
                            mobileNumber = viewModel.rmn,
                            subscriberId = sid,
                            eulaChecked = true,
                            isCreate = true,
                            isPastBingeUser = pastBingeUser,
                            dthStatus = dthStatus,
                            packageId = viewModel.packageId,
                            referenceId = viewModel.referenceId,
                            cartId = viewModel.cartId
                        )
                    )
                } else {
                    finalSidListing.let { it ->
                        e("GuestLoginOTP", "SID Listing inside it.size : ${it.size}")
                        if (it.size == 1) {
                            val subObj = it[0]
                            viewModel.nosBaids = it[0].listOfBaIds.size
                            when {
                                it[0].listOfBaIds.isEmpty() || (it[0].listOfBaIds.size == 1 && it[0].listOfBaIds[0].baId == null) -> {
                                    viewModel.referenceId = subObj.listOfBaIds[0].referenceId
                                    viewModel.loginDTO.sid = subObj.sid
                                    viewModel.loginDTO.email =
                                        it[0].email //it.bingeUserData?.emailId
                                    viewModel.loginDTO.name =
                                        it[0].name //it.bingeUserData?.aliasName
                                    viewModel.loginDTO.rmn = it[0].rmn ?: ""
                                    isCreate = true
                                    viewModel.createNewBingeMobileUser(
                                        NewBingeUserRequest(
                                            login = viewModel.loginAuth,
                                            subscriberId = subObj.sid,
                                            bingeSubscriberId = subObj.listOfBaIds[0].bingeSubscriberId ,
                                            mobileNumber = viewModel.rmn,
                                            baId = subObj.listOfBaIds[0].baId,
                                            isCreate = isCreate,
                                            dthStatus = subObj.dthStatus,
                                            isPastBingeUser = subObj.isPastBingeUser,
                                            packageId = viewModel.packageId,
                                            referenceId = viewModel.referenceId,
                                            cartId = viewModel.cartId
                                        )
                                    )
                                }
                                it[0].listOfBaIds.size == 1 -> {
                                    isCreate = false
                                    viewModel.referenceId = subObj.listOfBaIds[0].referenceId
                                    if(DTH_WO_BINGE_USER.equals(subObj.dthStatus, true)) {
                                        isCreate = true
                                        viewModel.referenceId = subObj.referenceId
                                    }
                                    dsn = subObj.listOfBaIds[0].deviceSerialNumber
                                    viewModel.createNewBingeMobileUser(
                                        NewBingeUserRequest(
                                            login = viewModel.loginAuth,
                                            subscriberId = subObj.sid,
                                            bingeSubscriberId = subObj.listOfBaIds[0].bingeSubscriberId ,
                                            mobileNumber = viewModel.rmn,
                                            baId = subObj.listOfBaIds[0].baId,
                                            isCreate = isCreate,
                                            dthStatus = subObj.dthStatus,
                                            isPastBingeUser = subObj.isPastBingeUser,
                                            dsn = dsn,
                                            packageId = viewModel.packageId,
                                            referenceId = viewModel.referenceId,
                                            cartId = viewModel.cartId
                                        )
                                    )
                                    this.isPastBingeUser = it[0].isPastBingeUser == true
                                    this.selectedBingeUser = it[0].listOfBaIds[0]
                                    //                                checkDTHStatus(it[0].dthStatus ?: "",it[0].accountSubStatus, selectedBingeUser?.subscriptionDetailInfo?.bingeAccountStatus?:"", selectedBingeUser?.subscriptionDetailInfo?.isPaid?:false, selectedBingeUser?.subscriptionDetailInfo?.migrated?:false, selectedBingeUser!!, it[0].sid)
                                }
                                else -> {
                                    //move to select BAID List Screen
                                    viewModel.selectedSubscriberDetails = it[0]
                                    viewModel.setBAidAdapter(it[0].listOfBaIds)
                                    findNavController().navigateSafe(
                                        GuestLoginFragmentDirections.actionGuestLoginSidListingFragmentToGuestBAIDListingFragment()
                                    )
                                }
                            }
                        } else {
                            e("GuestLoginOTP", "SID Listing inside it.size : ${it.size}")
                            viewModel.setSelectSidAdapter(it)
                            findNavController().navigateSafe(GuestLoginFragmentDirections.actionGuestLoginFragmentToGuestLoginSidListingFragment())
                        }
                    }
                }
            }
        }

        viewModel.generateOtpResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { generateOTPResponse ->
                loginAnalytics.trackGetOtp()
                // Generate OTP response has masked number, So getting from text field
                updateAndShowOtpUI(getOtpGuestLoginResponse = generateOTPResponse)
            }
        }

        viewModel.validateOtpResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                viewModel.untrackedMixpanelId = loginAnalytics.getMixPanelId()
                viewModel.untrackedMixpanelUnifiedId = loginAnalytics.getMixPanelUnifiedId()
                when {
                    viewModel.isLoggedIn && viewModel.isParentalPinSetupRequested -> {
                        //save parental pin if already logged in and pin setup process was initiated
                        parentalControlViewModel.saveParentalPin(
                            parentalPinValue = viewModel.parentalPinValue,
                            otp = viewModel.otp,
                            isLogin = true
                        )
                    }
                    viewModel.isParentalPinResetRequested -> {
                        //save parental pin if already logged in and pin reset process was initiated
                        parentalControlViewModel.saveParentalPin(
                            parentalPinValue = viewModel.parentalPinValue,
                            otp = viewModel.otp,
                            isLogin = true
                        )
                    }
                    else -> {
                        viewModel.loginDTO.rmn = viewModel.rmn
                        viewModel.saveLoginToken(it)
                        viewModel.fetchSubIdList()
                    }
                }
            }
        }

        viewModel.validateOtpResponseError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                when (errorModel.code) {
                    40008, 60001 -> {
                        setError(true, errorModel.message)
                        setOtp("")
                    }
                    60003 -> {
                        setError(true, errorModel.message, incorrectOtpVerbiage)
                        setOtp("")
                    }
                    else -> {
                        onError(errorModel)
                    }
                }
            }
        }

        parentalControlViewModel.saveParentalPinResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                when {
                    viewModel.isParentalPinSetupRequested -> {
                        //upon successfully saving parental pin take user to the success screen
                        sharedPrefs.setParentalPinExists(true)
                        findNavController().navigateSafe(GuestLoginFragmentDirections.actionGlobalParentalPinSuccessFragment())
                    }
                    viewModel.isParentalPinResetRequested -> {
                        sharedPrefs.setParentalControlEnabled(false)
                        viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
                    }
                }
            }
        }

        parentalControlViewModel.saveParentalPinError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                onError(errorModel)
            }
        }

        viewModel.getExistingBingeUserLoginResponse().observe(viewLifecycleOwner) { obj ->
            obj.getContentIfNotHandled()?.let {
//                viewModel.saveLoggedInDetails(it)
                /*Changes for ProbeSDK*/
                context?.let { playerEventRegisterForMitigationSession(sharedPrefs.getClearRMN(), it) }
                /*End*/
                if (viewModel.isParentalPinSetupRequested && selectedBingeUser?.parentalPinExist == true) {
                    //if user is not logged in and parental pin setup requested and parental pin already exists then take user to pin success screen
                    findNavController().navigateSafe(GuestLoginFragmentDirections.actionGlobalParentalPinSuccessFragment())
                } else if (viewModel.isParentalPinSetupRequested && selectedBingeUser?.parentalPinExist != true) {
                    //if user is not logged in and parental pin setup requested and parental pin does not exists then take user to pin setup screen
                    findNavController().navigateSafe(GuestLoginFragmentDirections.actionGlobalParentalPinSetupFragment())
                } else {
                    //if parental pin setup not requested then just save login details and dismiss dialog
                    viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
                }
            }
        }
    }

    private var incorrectOtpVerbiage: String? = null
    private fun updateAndShowOtpUI(getOtpGuestLoginResponse: GetOtpGuestLoginResponse? = null) {
        val dataFromResponse = getOtpGuestLoginResponse?.data
        startSMSListener()
        binding.header.imgBack.show()
        binding.rmnGroup.visibility = View.GONE
        binding.etMobileGuestLogin.et.hide()
        binding.layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.apply {
            disable()
            dataFromResponse?.let {
                text = it.resendOtpHeading
                incorrectOtpVerbiage = it.incorrectOtpVerbiage
            }
        }
        binding.layoutLoginOTP.root.show()
        // Setting Error to false for this https://jira.tothenew.com/browse/TSF-19626
        setError(false)
        binding.layoutLoginOTP.clEtOtpContainer.etOtpDig1.apply {
            requestFocus()
            showKeyboard()
        }

        setOtp("")
        setTimer(dataFromResponse?.resendOtpInVerbiage)
        viewModel.resendCount++
        val configCount = viewModel.sharedPrefs.getOtpResentCount() + 1
        if (viewModel.resendCount >= configCount) {
            binding.layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.disable()
            binding.layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.invisible()
        } else {
            viewModel.timer?.start()
            binding.layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.disable()
        }
        val enterOtpVerbiage = getString(
            R.string.please_enter_otp,
            dataFromResponse?.enterOTP?.splitAnyString("*")?.first(),
            userMobileNumber
        )
        binding.layoutLoginOTP.tvSubtitleGuestLoginVerifyOtp.text = enterOtpVerbiage
    }

    override fun toBeCalledOnce() {
        binding.viewModel = viewModel
        setBingeLogoOnGuestPage()
        isEligibleForFreeTrial = viewModel.showFreeTrailUI
        guestLoginFragmentArgs.selectedRmnDetails?.let {
            loginAnalytics.trackLoginPageVisit(
                PREVUSED,
                viewModel.loginSource
            )
            isEligibleForFreeTrial = it.freeTrialEligible
            it.mobileNumber?.let { mobNo ->
                userMobileNumber = mobNo
                viewModel.rmn = mobNo
                validateAndGenerateOtp()
            }
        } ?: run {
            loginAnalytics.trackLoginPageVisit(
                NEW,
                viewModel.loginSource
            )
        }
        if (isEligibleForFreeTrial)
            binding.etMobileGuestLogin.etHeader.visibility = View.GONE

        setupProviderRVAdapter()

        if (viewModel.isParentalPinVerificationRequested) {
            //if parental pin verification action is requested then directly take the user to parental pin screen for pin verification
            findNavController().navigateSafe(GuestLoginFragmentDirections.actionGlobalParentalPinSetupFragment())
        }

        if (viewModel.isParentalPinSetupRequested) {
            if (viewModel.isLoggedIn) {
                //if user is already logged in and parental pin setup requested then take user directly to pin setup screen
                findNavController().navigateSafe(GuestLoginFragmentDirections.actionGlobalParentalPinSetupFragment())
            } else {
                //if user is not logged in and parental pin setup requested then initiate login flow with pin setup verbiage
                binding.header.tvHeaderTitle.text =
                    getString(R.string.header_title_parental_pin_setup)
            }
        }

        binding.etMobileGuestLogin.et.imeOptions = EditorInfo.IME_ACTION_GO
        binding.etMobileGuestLogin.et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Hide the keyboard when user has entered
                // the max length of allowed characters
                if (s?.length == 10) {
                    binding.root.closeKeyboard()
                    binding.etMobileGuestLogin.et.setSelection(10)
                    binding.etMobileGuestLogin.et.setMaxLength(s.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (after == 10)
                    binding.etMobileGuestLogin.et.setMaxLength(after)
                else
                    binding.etMobileGuestLogin.et.setMaxLength(15) //User is pasting RMN with country code or more than 10 digits, Allow upto 15 digits
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length ?: 0 < 10) {
                    binding.etMobileGuestLogin.tilNumber.clearError()
                    binding.etMobileGuestLogin.et.background.level = 0
                }
                else if (count <= (binding.etMobileGuestLogin.et.maxLength?.minus(1) /*Minus 1, As + is not allowed in EditText*/ ?: 0))
                    binding.etMobileGuestLogin.et.text = Editable.Factory.getInstance().newEditable(s?.takeLast(10))
                binding.btnProceedGuestLogin.isEnabled = (s?.length ?: 0) >= 10
                if (count == 10) {
                    viewModel.rmn = s.toString()
                }
            }
        })

        setLicenseAgreement(sharedPrefs.getEulaTitle() ?: "", sharedPrefs.getEulaSubTitle() ?: "")

        parentalControlViewModel =
            ViewModelProvider(
                requireParentFragment().requireParentFragment(),
                mViewModelFactory
            )[ParentalControlViewModel::class.java]

        when {
            viewModel.isParentalPinSetupRequested && !viewModel.isLoggedIn -> {
                binding.header.tvHeaderTitle.text =
                    getString(R.string.header_title_parental_pin_setup)
            }
            viewModel.isParentalPinSetupRequested && viewModel.isLoggedIn -> {
                setBingeLogoOnGuestPage()
                binding.header.tvHeaderTitle.text = getString(R.string.title_parental_pin_setup)
                viewModel.rmn = sharedPrefs.getClearRMN()
            }
            viewModel.isParentalPinResetRequested -> {
                binding.header.tvHeaderTitle.text = getString(R.string.title_parental_pin_reset)
            }
        }
        binding.apply {
            if (rmnGroup.isVisibile())
                lifecycleScope.launchWhenResumed {
                    delay(400)
                    etMobileGuestLogin.et.showKeyboard()
                }
            layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.setOnClickListener {
                setError(false)
                setOtp("")
                loginAnalytics.trackOtpInvoked(
                    viewModel?.loginType ?: "",
                    viewModel?.loginAuth ?: "",
                    viewModel?.rmn ?: "",
                    viewModel?.loginSource ?: ""
                )
                loginAnalytics.trackOtpResend()
                userMobileNumber = viewModel?.rmn ?: ""
                viewModel?.generateOtp()
                binding.layoutLoginOTP.tvResendOtpGuestLoginVerifyOtp.disable()
            }
            layoutLoginOTP.clEtOtpContainer.apply {
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

                /*For OTP digit deletion on pressing del key*/
                etOtpDig1.setOnKeyListener(this@GuestLoginFragment)
                etOtpDig2.setOnKeyListener(this@GuestLoginFragment)
                etOtpDig3.setOnKeyListener(this@GuestLoginFragment)
                etOtpDig4.setOnKeyListener(this@GuestLoginFragment)
                etOtpDig5.setOnKeyListener(this@GuestLoginFragment)
                etOtpDig6.setOnKeyListener(this@GuestLoginFragment)
            }
        }
    }

    private fun setBingeLogoOnGuestPage() {
        binding.header.logo.setImageWithPlaceHolder(
            viewModel.getVerbiageFromConfig()?.loginScreen?.logo,
            R.drawable.medium_binge_logo
        )
    }

    private fun setLicenseAgreement(tncPart1: String?, tncPart2: String?) {
        val licenseAgreementPart2 = SpannableString(tncPart2)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                reenterTransition = null
                exitTransition = null
                loginAnalytics.trackLicAgreement()
                findNavController().navigateSafe(GuestLoginFragmentDirections.actionGuestLoginFragmentToLicenseAgreementActivity())
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = resources.getColor(R.color.darkButtonTertiary)
            }
        }
        licenseAgreementPart2.setSpan(
            clickableSpan,
            0,
            licenseAgreementPart2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.licenseAgreementTV.movementMethod = LinkMovementMethod()
        binding.licenseAgreementTV.text = SpannableStringBuilder().append(
            tncPart1,
            licenseAgreementPart2
        )
    }

    private fun setupProviderRVAdapter() {
        viewModel.setPartnerLogoAdapter(
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.filterProviders ?: emptyList()
        )
        val providerRecyclerView = binding.header.partnerRV
        val providerAdapter = viewModel.getPartnerLogoAdapter()
        // Disable recyclerview touch
        providerRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return true
            }
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        providerRecyclerView.setItemViewCacheSize(ITEM_CACHE) // For optimisation
        if (providerAdapter != null && providerAdapter.itemCount > 0)
            lifecycleScope.launchWhenResumed {
                delay(500L)
                enableAutoScroll(providerRecyclerView, providerAdapter)
            }
    }

    private suspend fun enableAutoScroll(
        providerRecyclerView: RecyclerView,
        providerAdapter: PartnerLogoRVAdapter?,
    ) {
        providerAdapter?.let {
            if (providerRecyclerView.canScrollHorizontally(DIRECTION_RIGHT))
                providerRecyclerView.smoothScrollBy(SCROLL_DX, 0)
            else {
                val firstPosition =
                    (providerRecyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                if (firstPosition != null && firstPosition != RecyclerView.NO_POSITION) {
                    val currentList = providerAdapter.currentList
                    val secondPart = currentList.subList(0, firstPosition)
                    val firstPart = currentList.subList(firstPosition, currentList.size)
                    providerAdapter.submitList(firstPart + secondPart)
                }
            }
            delay(DELAY_BETWEEN_SCROLL_MS)
            enableAutoScroll(providerRecyclerView, providerAdapter)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*overriding loader set on activity via BaseFragment*/
        showProgress = Runnable { }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProceedGuestLogin.setOnClickListener {
            loginAnalytics.trackLoginRmnEnter()
            validateAndGenerateOtp()
        }

        binding.btnCancelGuestLogin.setOnClickListener {
            binding.root.closeKeyboard()
            loginAnalytics.trackLoginPageNotNow()
            viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.FAILURE))
        }
        binding.header.imgBack.setOnClickListener{
            lifecycleScope.launchWhenResumed {
                binding.root.closeKeyboard()
                if (binding.layoutLoginOTP.root.isVisibile() &&
                    guestLoginFragmentArgs.selectedRmnDetails == null
                ) {
                    // User came here from new RMN entering
                    viewModel.timer?.cancel()
                    smsReceiver?.let { it.unregisterSMSReceiver(context, it) }
                    delay(200)
                    binding.header.imgBack.hide()
                    binding.layoutLoginOTP.root.hide()
                    binding.rmnGroup.apply {
                        show()
                        binding.etMobileGuestLogin.et.show()
                        lifecycleScope.launchWhenResumed {
                            delay(300)
                            binding.etMobileGuestLogin.et.showKeyboard()
                        }
                    }
                } else
                    findNavController().navigateUp()
            }
        }
    }

    private fun validateAndGenerateOtp() {
        if (viewModel.rmn.length == 10) {
            userMobileNumber = viewModel.rmn
            loginAnalytics.trackOtpInvoked(
                viewModel.loginType,
                viewModel.loginAuth,
                viewModel.rmn,
                viewModel.loginSource
            )
            viewModel.generateOtp()
        }
    }

    companion object {
        private const val DELAY_BETWEEN_SCROLL_MS = 25L
        private const val SCROLL_DX = 8
        private const val DIRECTION_RIGHT = 1
        private const val ITEM_CACHE = 8
    }
}
