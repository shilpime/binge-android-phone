package com.tatasky.binge.ui.features.onboarding.login.bottomsheet

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_LOGIN
import com.tatasky.binge.data.networking.models.requests.NewBingeUserRequest
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.databinding.FragmentGuestLoginBaidListingBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity
import com.tatasky.binge.ui.features.device_management.DeviceListManagementAnalytics
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.temp.GuestLoginBottomSheetResult
import com.tatasky.binge.utils.navigateSafe
import com.tatasky.binge.utils.showToast
import javax.inject.Inject

class GuestBAIDListingFragment :
    BaseFragment<FragmentGuestLoginBaidListingBinding, GuestLoginViewModel>() {

    private var selectedBingeUser: LoginResponse.BingeSubscription? = null

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var deviceListManagementAnalytics: DeviceListManagementAnalytics

    override fun getViewModelClass(): Class<GuestLoginViewModel> =
        GuestLoginViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_guest_login_baid_listing

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
                            bingeSubscriberId = selectedBingeUser?.bingeSubscriberId ,
                            mobileNumber = viewModel.rmn,
                            baId = selectedBingeUser?.baId,
                            subscriberId = viewModel.selectedSubscriberDetails?.sid,
                            eulaChecked = true,
                            isCreate = false,
                            dthStatus = viewModel.selectedSubscriberDetails?.dthStatus,
                            isPastBingeUser = viewModel.selectedSubscriberDetails?.isPastBingeUser,
                            dsn = selectedBingeUser?.deviceSerialNumber,
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
                        maxDeviceLimitReachedResponse.message,
                        getString(R.string.review_devices),
                        getString(R.string.text_non_underlined_Not_Now),
                        maxDeviceLimitReachedResponse.title
                    ), object :
                        CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            deviceListManagementAnalytics.trackDeviceLimitPopupReviewClick(
                                SOURCE_LOGIN
                            )
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
        })
        viewModel.getSelectedBAID().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { bingeUser ->
                binding.btnNext.isEnabled = true
                selectedBingeUser = bingeUser
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
    }

    override fun toBeCalledOnce() {
        binding.vm = viewModel
        binding.btnNext.setOnClickListener {
            if (selectedBingeUser == null) {
                showToast(context, "Select one Binge Mobile Account")
            } else {
//                if (!selectedBingeUser?.firstTimeLogin!!) {
                // User is doing login again
                /** Same method name is used to create or login existing user.
                isCreate flag is used to check if it is a new user or existing user */
                viewModel.referenceId = selectedBingeUser?.referenceId
                viewModel.createNewBingeMobileUser(
                    NewBingeUserRequest(
                        login = viewModel.loginAuth,
                        bingeSubscriberId = selectedBingeUser?.bingeSubscriberId ,
                        mobileNumber = viewModel.rmn,
                        baId = selectedBingeUser?.baId,
                        subscriberId = viewModel.selectedSubscriberDetails?.sid,
                        eulaChecked = true,
                        isCreate = false,
                        dthStatus = viewModel.selectedSubscriberDetails?.dthStatus,
                        isPastBingeUser = viewModel.selectedSubscriberDetails?.isPastBingeUser,
                        dsn = selectedBingeUser?.deviceSerialNumber,
                        packageId = viewModel.packageId,
                        referenceId = viewModel.referenceId,
                        cartId = viewModel.cartId
                    )
                )
            }
        }
    }
}