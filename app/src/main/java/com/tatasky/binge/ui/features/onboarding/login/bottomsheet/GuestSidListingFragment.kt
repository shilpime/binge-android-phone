package com.tatasky.binge.ui.features.onboarding.login.bottomsheet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_LOGIN
import com.tatasky.binge.data.networking.models.requests.NewBingeUserRequest
import com.tatasky.binge.data.networking.models.response.SubscriberIdListResponse
import com.tatasky.binge.databinding.FragmentGuestLoginSidListingBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity.Companion.KEY_IS_DEVICE_REVIEW_ON_MAX_LIMIT_REACHED
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity.Companion.KEY_TEMP_BAID
import com.tatasky.binge.ui.features.device_management.DeviceListManagementAnalytics
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.temp.GuestLoginBottomSheetResult
import com.tatasky.binge.utils.AccountStatusEnum
import com.tatasky.binge.utils.DTH_WO_BINGE_USER
import com.tatasky.binge.utils.navigateSafe
import com.tatasky.binge.utils.showToast
import javax.inject.Inject

class GuestSidListingFragment :
    BaseFragment<FragmentGuestLoginSidListingBinding, GuestLoginViewModel>() {

    private var isCreate: Boolean = false

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    @Inject
    lateinit var deviceListManagementAnalytics: DeviceListManagementAnalytics

    private var selectedSubscriber: SubscriberIdListResponse.SubscriberDetail? = null
    override fun getViewModelClass(): Class<GuestLoginViewModel> =
        GuestLoginViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_guest_login_sid_listing

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
                            mobileNumber = viewModel.rmn,
                            subscriberId = selectedSubscriber?.sid,
                            baId = selectedSubscriber?.listOfBaIds!![0].baId,
                            bingeSubscriberId = selectedSubscriber?.listOfBaIds!![0].bingeSubscriberId,
                            eulaChecked = true,
                            isCreate = isCreate,
                            dthStatus = selectedSubscriber?.dthStatus,
                            isPastBingeUser = selectedSubscriber?.isPastBingeUser,
                            dsn = selectedSubscriber?.listOfBaIds!![0].deviceSerialNumber,
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
        viewModel.getMaxDeviceLimitReachedResponse().observe(viewLifecycleOwner, Observer {
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
                            deviceListManagementAnalytics.trackDeviceLimitPopupReviewClick(
                                SOURCE_LOGIN
                            )
                            hideDialog()
                            activity?.let { context ->
                                startActivityForResult(Intent(context, DeviceListManagementActivity::class.java).apply {
                                    putExtra(KEY_TEMP_BAID, selectedSubscriber?.listOfBaIds!![0].baId)
                                    putExtra(KEY_IS_DEVICE_REVIEW_ON_MAX_LIMIT_REACHED, true)
                                }, DeviceListManagementActivity.DEVICE_LIMIT_REQUEST_CODE)
                            }
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                            deviceListManagementAnalytics.trackDeviceLimitPopupSkip(
                                SOURCE_LOGIN
                            )
                            viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.FAILURE))
                        }

                        override fun onCloseButtonClick() {
                            hideDialog()
                            viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.FAILURE))
                        }
                    })
            }
        })
        viewModel.getExistingBingeUserLoginResponse().observe(viewLifecycleOwner) { obj ->
            obj.getContentIfNotHandled()?.let {
//                viewModel.saveLoggedInDetails(it)

                if (viewModel.isParentalPinSetupRequested && it.bingeUserData?.parentalPinExist == true) {
                    //if user is not logged in and parental pin setup requested and parental pin already exists then take user to pin success screen
                    findNavController().navigateSafe(GuestLoginOtpFragmentDirections.actionGlobalParentalPinSuccessFragment())
                } else if (viewModel.isParentalPinSetupRequested && it.bingeUserData?.parentalPinExist != true) {
                    //if user is not logged in and parental pin setup requested and parental pin does not exists then take user to pin setup screen
                    findNavController().navigateSafe(GuestLoginOtpFragmentDirections.actionGlobalParentalPinSetupFragment())
                } else {
                    //if parental pin setup not requested then just save login details and dismiss dialog
                    viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
                }
            }
        }

        viewModel.getSelectedSID().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { seletedSID ->
                selectedSubscriber = seletedSID
                loginAnalytics.trackMixPanelLoginSubscriptionIdSelect(seletedSID.sid)
                viewModel.nosBaids = seletedSID.listOfBaIds.size
                viewModel.setBAidAdapter(seletedSID.listOfBaIds)

                if (seletedSID.listOfBaIds.isNullOrEmpty() && !seletedSID.loginErrorMsg.isNullOrBlank()) {
                    if (seletedSID.accountStatus.equals(AccountStatusEnum.PENDING.status, true)) {
                        showDialog(
                            DialogModel(
                                false,
                                R.drawable.ic_subscription_error,
                                getString(R.string.error_message),
                                getString(R.string.ok),
                                null,
                                seletedSID.loginErrorMsg
                            ),
                            object : CommonDialogEventListener {
                                override fun onPrimaryButtonClick() {
                                    hideDialog()
                                }

                                override fun onSecondaryButtonClick() {
                                    hideDialog()
                                }

                                override fun onCloseButtonClick() {
                                    hideDialog()
                                }
                            })
                    } else {
                        showDialog(DialogModel(
                            cancelable = true,
                            imageId = null,
                            title = seletedSID.loginErrorMsg,
                            primaryButtonText = getString(R.string.subscribe_now),
                            secondaryButtonText = null,
                            text = null,
                            statusCode = null
                        ), object :
                            CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                findNavController().navigateUp()
                                hideDialog()
                                try {
                                    val browserIntent =
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(getString(R.string.subscribe_now_url))
                                        )
                                    startActivity(browserIntent)
                                } catch (e: Exception) {
                                    showToast(context, "No Browser found", null)
                                }
                            }

                            override fun onSecondaryButtonClick() {
                            }

                            override fun onCloseButtonClick() {
                                try {
                                    hideDialog()
                                    findNavController().popBackStack()
                                } catch (e: Exception) {
                                }
                            }
                        })
                    }
                    binding.btnNext.isEnabled = false
                } else {
                    binding.btnNext.isEnabled = true
                }
            }
        }
    }

    override fun toBeCalledOnce() {
        binding.viewModel = viewModel

        if (viewModel.isParentalPinSetupRequested) {
            binding.header.tvHeaderTitle.text = getString(R.string.header_title_parental_pin_setup)
        }

        binding.btnNext.setOnClickListener {
            if (selectedSubscriber == null) {
                showToast(context, "Select one Tatasky Subscriber Id")
            } else {
                isCreate = false
                if(DTH_WO_BINGE_USER.equals(selectedSubscriber?.dthStatus, true))
                    isCreate = true
                if (selectedSubscriber?.listOfBaIds.isNullOrEmpty()) {
                    //create new user
                    viewModel.referenceId = selectedSubscriber?.referenceId
                    viewModel.createNewBingeMobileUser(
                        NewBingeUserRequest(
                            login = viewModel.loginAuth,
                            mobileNumber = viewModel.rmn,
                            subscriberId = selectedSubscriber?.sid,
                            eulaChecked = true,
                            isCreate = true,
                            dthStatus = selectedSubscriber?.dthStatus,
                            isPastBingeUser = selectedSubscriber?.isPastBingeUser,
                            packageId = viewModel.packageId,
                            referenceId = viewModel.referenceId,
                            cartId = viewModel.cartId
                        )
                    )
                } else if (selectedSubscriber?.listOfBaIds?.size ?: 0 == 1) {
                    viewModel.referenceId = selectedSubscriber?.listOfBaIds!![0].referenceId
                    // User has only one BAID
                    viewModel.createNewBingeMobileUser(
                        NewBingeUserRequest(
                            login = viewModel.loginAuth,
                            mobileNumber = viewModel.rmn,
                            subscriberId = selectedSubscriber?.sid,
                            baId = selectedSubscriber?.listOfBaIds!![0].baId,
                            bingeSubscriberId = selectedSubscriber?.listOfBaIds!![0].bingeSubscriberId,
                            eulaChecked = true,
                            isCreate = isCreate,
                            dthStatus = selectedSubscriber?.dthStatus,
                            isPastBingeUser = selectedSubscriber?.isPastBingeUser,
                            dsn = selectedSubscriber?.listOfBaIds!![0].deviceSerialNumber,
                            packageId = viewModel.packageId,
                            referenceId = viewModel.referenceId,
                            cartId = viewModel.cartId
                        )
                    )
                }
                else {
                    /* SID has multiple binge plans */
                    moveToBaidListingScreen()
                }
            }
        }
    }

    private fun moveToBaidListingScreen() {
        selectedSubscriber?.let {
            viewModel.selectedSubscriberDetails = it // Store subscriber details for further usage
        }
        // Show plan/BAID selector screen
        findNavController().navigateSafe(
            GuestSidListingFragmentDirections.actionGuestLoginSidListingFragmentToGuestBAIDListingFragment()
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*overriding loader set on activity via BaseFragment*/
        showProgress = Runnable { }
    }
}
