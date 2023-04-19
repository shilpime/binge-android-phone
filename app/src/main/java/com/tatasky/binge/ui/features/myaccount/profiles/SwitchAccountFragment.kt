package com.tatasky.binge.ui.features.myaccount.profiles

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.databinding.FragmentSwitchAccountBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.myaccount.MyAccountViewModel
import com.tatasky.binge.ui.features.updateprofile.ProfileAnalytics
import com.tatasky.binge.utils.LOGIN_MAX_DEVICE_ERROR_CODE
import com.tatasky.binge.utils.NON_DTH_USER
import com.tatasky.binge.utils.isTablet
import com.tatasky.binge.utils.setSelectedAccountDetail
import com.tatasky.binge.utils.showCustomLoginToast
import com.tatasky.binge.utils.startHomeScreen
import javax.inject.Inject

class SwitchAccountFragment : BaseFragment<FragmentSwitchAccountBinding, MyAccountViewModel>() {
    @Inject
    lateinit var profileAnalytics: ProfileAnalytics
    private var selectedProfile: LoginResponse.BingeSubscription? = null
    private var handler: Handler? = null
    override fun getViewModelClass(): Class<MyAccountViewModel> {
        return MyAccountViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_switch_account
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    private var runnable: Runnable = Runnable { startHomeScreen(activity) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DeviceListManagementActivity.DEVICE_LIMIT_REQUEST_CODE) {
            when (resultCode) { // If user's device removal is successful
                Activity.RESULT_OK ->
                    switchAccount()
                Activity.RESULT_CANCELED -> {/*Do something*/}
            }
        }
    }

    override fun toBeCalledOnce() {
        if (context?.let { isTablet(it) } == true)
        {
            binding.toolbarLayout.visibility = View.INVISIBLE
            binding.textView?.visibility =View.VISIBLE
        }
        viewModel.fetchBaIdList(sharedPrefs.getOriginalSubscriberId(), false)
        binding.viewModel = viewModel
        binding.btnNext.setOnClickListener {
            switchAccount()
        }
    }

    //Switch profile
    private fun switchAccount() {
        viewModel.switchBAID(
            viewModel.sharedPrefs.getBaId(),
            if(selectedProfile!!.baId.isNullOrBlank())
                selectedProfile!!.deviceSerialNumber
            else
                null,
            selectedProfile?.baId?:""
        )
    }

    override fun setObserver() {
        viewModel.getMaxDeviceLimitReachedResponse().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { maxDeviceLimitReachedResponse ->
                showDialog(
                    DialogModel(
                        false,
                        R.drawable.ic_device_center,
                        title = sharedPrefs.getConfigResponse()?.data?.config?.device?.header,
                        primaryButtonText =
                        sharedPrefs.getConfigResponse()?.data?.config?.device?.review
                            ?: getString(R.string.review_devices),
                        secondaryButtonText = getString(R.string.text_non_underlined_Not_Now),
                        text = sharedPrefs.getConfigResponse()?.data?.config?.device?.subHeader
                    ), object :
                        CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            hideDialog()
                            activity?.let { context ->
                                startActivityForResult(Intent(context, DeviceListManagementActivity::class.java).apply {
                                    putExtra(DeviceListManagementActivity.KEY_TEMP_BAID, selectedProfile?.baId /*Target BAID of which to list devices*/)
                                    putExtra(DeviceListManagementActivity.KEY_IS_DEVICE_REVIEW_ON_MAX_LIMIT_REACHED, true)
                                }, DeviceListManagementActivity.DEVICE_LIMIT_REQUEST_CODE)
                            }
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                            /*viewModel.guestLoginResult.postValue(
                                SingleEvent(
                                    GuestLoginBottomSheetResult.FAILURE)
                            )*/
                        }

                        override fun onCloseButtonClick() {
                            hideDialog()
                            /*viewModel.guestLoginResult.postValue(
                                SingleEvent(
                                    GuestLoginBottomSheetResult.FAILURE)
                            )*/
                        }
                    })
            }
        }
        /*viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!viewModel.onlyMessage)
                    findNavController().navigateUp()
            }
        })*/

        viewModel.getNumberOfBingeAccounts().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if(activity?.intent?.getBooleanExtra(Intent.EXTRA_USER, false) == true && it.data?.listOfBaIds?.size?:0 > 0)
                    viewModel.selectDefaultBingeAccount()
            }
        })

        viewModel.getSelectedBAID().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                selectedProfile = it
                if (sharedPrefs.getBaId().equals(selectedProfile?.baId, true)) {
                    binding.btnNext.hide()
                } else {
                    binding.btnNext.show()
                }
            }
        })

        viewModel.getSwitchAccountResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding.btnNext.hide()
                if(selectedProfile?.baId.isNullOrBlank()){
                    selectedProfile?.baId = it.data?.baId
                    selectedProfile?.profileId = it.data?.profileId
                }

                profileAnalytics.trackSwitchProfile(selectedProfile?.baId?:"")
                showCustomLoginToast(
                    requireContext(),
                    getString(R.string.switch_account_success_msg, selectedProfile?.aliasName ?: ""),
                    isSuccess = true,
                    isBottomNavVisible = false
                )
                it.data?.mixpanelId?.let {it1 ->
                    profileAnalytics.setUserIdentity(it1)
                    viewModel.sharedPrefs.saveMixPanelId(it1)
                }
                setSelectedAccountDetail(selectedProfile!!, viewModel.sharedPrefs)
                viewModel.fetchProfileInfo()
                if(!NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true)) {
                    viewModel.fetchBalance()
                }
                viewModel.fetchFreemiumCurrentSubscription()
                /*handler = Handler(Looper.getMainLooper())
                if(sharedPrefs.getDeviceCancellationFlag()) { // TODO: Ask if this is required
                    handler?.postDelayed(
                        {
                            startHomeScreen(activity, isCancelledUser = true)
                        }, 500
                    )
                }
                else
                    handler?.postDelayed(runnable, 500)*/
            }
        })
    }

    override fun onError(errorModel: ErrorModel) {
        if (errorModel.code == LOGIN_MAX_DEVICE_ERROR_CODE && sharedPrefs.getDeviceCancellationFlag()) {
            showDialog(
                DialogModel(
                    false,
                    null,
                    errorModel.message,
                    getString(R.string.ok),
                    null,
                    null,
                    null,
                    null
                ),
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        viewModel.removeDeviceAndSignout()
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }

                }
            )
        } else
            super.onError(errorModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (handler != null) {
            handler?.removeCallbacks(runnable)
        }
    }
}
