package com.tatasky.binge.ui.features.sidemenunavdrawer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.analytics.NO
import com.tatasky.binge.analytics.PARA_WATCH_NOTIFICATION
import com.tatasky.binge.analytics.YES
import com.tatasky.binge.data.networking.models.response.SubscriberProfileListModel
import com.tatasky.binge.databinding.FragmentSettingsBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.setSingleOnClick
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.more.SettingsViewModel
import com.tatasky.binge.ui.features.myaccount.MyAccountViewModel
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.*
import com.tatasky.binge.utils.*
import javax.inject.Inject

class SettingsFragment : BaseFragment<FragmentSettingsBinding, SettingsViewModel>() {

    private var contentLangTitle: String? = null
    private var isResultHandled = false

    @Inject
    lateinit var loginAnalytics: LoginAnalytics
    @Inject
    lateinit var sideMenuDrawerAnalytics: SideMenuDrawerAnalytics
    val userData = SubscriberProfileListModel.Data()
    private  lateinit var myAccountViewModel :MyAccountViewModel
    private var refreshCallsCount = -1

    override fun getViewModelClass(): Class<SettingsViewModel> {
        return SettingsViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_settings
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun setObserver() {
        viewModel.getSignoutResponse().observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                context?.let { it1 ->
                    activity?.let { it2 -> viewModel.deleteAllCleverTapNotifications(it2) }
                    loginAnalytics.trackLogout()
                    logoutApplication(it1)
//                    moveToHome()
                }
            }
        })

        viewModel.getToggledSetting().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it) {
                    AUTO_PLAY_TRAILER_SETTINGS_KEY -> {
                        viewModel.sharedPrefs.setAutoPlayTrailerOn(!binding.viewAutoTrailer.switchView.isChecked)
                        binding.viewAutoTrailer.switchView.isChecked = !binding.viewAutoTrailer.switchView.isChecked
                        sideMenuDrawerAnalytics.trackAutoPlaySettingsChanged(if(binding.viewAutoTrailer.switchView.isChecked) YES else NO)
                    }
                    WATCH_NOTI_SETTINGS_KEY -> {
                        viewModel.sharedPrefs.setAllowWatchNotification(!binding.viewNotificationSettings.switchView.isChecked)
                        binding.viewNotificationSettings.switchView.isChecked = !binding.viewNotificationSettings.switchView.isChecked
                        sideMenuDrawerAnalytics.trackNotificationSettingsChanged(PARA_WATCH_NOTIFICATION, if(binding.viewNotificationSettings.switchView.isChecked) YES else NO)
                    }
                }
            }
        })

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<ParentalControlBottomSheetResult>(
                KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT
            )
            ?.observe(viewLifecycleOwner) { result ->
                if (!isResultHandled) {
                    isResultHandled = true
                    when (result.resultStatus) {
                        ParentalControlBottomSheetResultStatus.SUCCESS_DISMISS -> {
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_PIN_FORGOT -> {
                                    val toastView =
                                        DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                                            LayoutInflater.from(context),
                                            R.layout.layout_toast_success_failure,
                                            null,
                                            false
                                        )
                                    toastView.textLoginSuccessfulToast.text =
                                        getString(R.string.toast_msg_pin_changed_successful)
                                    toastView.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                                    showCustomToast(
                                        context,
                                        toastView?.root,
                                        Gravity.FILL_HORIZONTAL
                                    )
                                }
                            }
                        }
                        ParentalControlBottomSheetResultStatus.PIN_VERIFIED -> {
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_PIN_VERIFICATION -> {
                                    Handler().postDelayed({
                                        findNavController().navigateSafe(
                                            SettingsFragmentDirections.actionSettingsFragmentToSwitchAccountFragment()
                                        )
                                    }, 500)
                                }
                            }
                        }
                        ParentalControlBottomSheetResultStatus.DISMISS -> {
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_PIN_VERIFICATION -> {
                                    //show error message here
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.autoPlayOn = viewModel.sharedPrefs.getAutoPlayTrailerOn()
        binding.notificationOn = viewModel.sharedPrefs.getWatchNotificationAllowed()
    }

    override fun onResume() {
        super.onResume()
        handleObservers()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun toBeCalledOnce() {
        myAccountViewModel =
            ViewModelProvider(this, viewModelFactory)[MyAccountViewModel::class.java]
        myAccountViewModel.miscAnalytics.trackMixPanelSettingsVisit()
        val verbiage = sharedPrefs.getConfigResponse()?.data?.config?.getLanguageVerbiage(CATEGORY_LANGUAGE_SETTING)
        contentLangTitle = verbiage?.data?.header ?: getString(R.string.content_language)
        binding.contentLangTitle = contentLangTitle
        binding.numberOfBingeAccount = viewModel.sharedPrefs.getNumberOfBingeAccount()
        binding.viewNotificationSettings.switchView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(sharedPrefs.getLoginStatus()) {
                    binding.viewNotificationSettings.switchView.isClickable = false
                    viewModel.toggleSetting(WATCH_NOTI_SETTINGS_KEY)
                }
                else{
                    viewModel.sharedPrefs.setAllowWatchNotification(!binding.viewNotificationSettings.switchView.isChecked)
                }
                return false
            }
        })

        binding.viewAutoTrailer.switchView.setOnTouchListener { v, event ->
            if(sharedPrefs.getLoginStatus()) {
                binding.viewAutoTrailer.switchView.isClickable = false
                viewModel.toggleSetting(AUTO_PLAY_TRAILER_SETTINGS_KEY)
            }
            else{
                viewModel.sharedPrefs.setAutoPlayTrailerOn(!binding.viewAutoTrailer.switchView.isChecked)
            }
            false
        }

        setOnClickListener(binding.viewContentLanguage.root)
        setOnClickListener(binding.viewDeviceManagement.root)
        setOnClickListener(binding.viewEditProfile.root)
        setOnClickListener(binding.viewTransactionHistory.root)
//        setOnClickListener(binding.viewPaymentSettings.root)
        setOnClickListener(binding.viewSwitchAccount.root)
        setOnClickListener(binding.viewParentalPin.root)
        handleBottomStaticMenuItemClicks()
    }

    private fun handleObservers() {
        myAccountViewModel.errorMessage.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let { error ->
                refreshCallsCount = -1
                onError(error)
            }
        }
        myAccountViewModel.getProfileInfo().observe(viewLifecycleOwner) {
            checkForAccountRefreshComplete()
        }
        myAccountViewModel.getWalletBalance().observe(viewLifecycleOwner) {
            checkForAccountRefreshComplete()
        }
        myAccountViewModel.getRefreshAccountStatus().observe(viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let {
            checkForAccountRefreshComplete()
//            }
        }

    }


    private fun checkForAccountRefreshComplete() {
        refreshCallsCount--
        e("checkForAccountRefreshComplete","refreshCallsCount : $refreshCallsCount")
        if (refreshCallsCount == 0) {
            val layoutParams = ConstraintLayout.LayoutParams(/*Custom view's parent is CL so using CL layout params*/
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply { /*set Margin/Padding here*/ }
            showToast(context, "Account refresh successful", layoutParam = layoutParams)
        }
    }

    private fun handleBottomStaticMenuItemClicks() {

        binding.tvLogOut.setOnClickListener {
            val dialogModel = DialogModel(
                false,
                R.drawable.ic_logout,
                getString(R.string.signout_header),
                getString(R.string.signout),
                getString(R.string.text_not_now),
                getString(R.string.logout_confirmation)
            )
            val listener = object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    hideDialog()
                    viewModel.removeDeviceAndSignout()
                }

                override fun onSecondaryButtonClick() {
                    hideDialog()
                }

                override fun onCloseButtonClick() {
                    hideDialog()
                }
            }
            showDialog(dialogModel, listener)
        }

        binding.tvAccountRefresh.setSingleOnClick(2000) {
            if (NetworkUtil.checkInternetBeforeNavigate()) {
                myAccountViewModel.fetchProfileInfo()
                refreshCallsCount = 1
                /*if (NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true)
                    || DTH_W_BINGE_NEW_USER.equals(sharedPrefs.getDthStatusFreemium(), true)
                ) {*/
                /*Refresh Account for each type user with check inside CommonService*/
                myAccountViewModel.refreshAccount()
                refreshCallsCount++
                /*}*/
                if (!NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true)) {
                    myAccountViewModel.fetchBalance()
                    refreshCallsCount++
                }
            }
        }
    }

    fun setOnClickListener(v: View) {
        v.setOnClickListener {
            myAccountViewModel.miscAnalytics.trackMixPanelSettingsMenuOption()
            when (v.id) {
                R.id.view_parental_pin -> {
                    loginAnalytics.trackParentalControlClick()
                    findNavController().navigateSafe(
                        SettingsFragmentDirections.actionGlobalParentalPinMenuFragment()
                    )
                }
                R.id.view_content_language -> {
                    findNavController().navigateSafe(
                        SettingsFragmentDirections.actionSettingsFragmentToContentLanguage(contentLangTitle)
                    )
                }
                R.id.view_device_management -> {
                    findNavController().navigateSafe(
                        SettingsFragmentDirections.actionSettingsFragmentToDeviceListFragment()
                    )
                }
                R.id.view_edit_profile -> {
                    val savedUserData = viewModel.sharedPrefs.getSelectedProfile()
                    userData.email = savedUserData?.emailId
                    userData.firstName = savedUserData?.firstName
                    userData.lastName = savedUserData?.lastName
                    userData.rmn = savedUserData?.rmn
                    userData.image = savedUserData?.imageUrl
                    findNavController().navigateSafe(
                        SettingsFragmentDirections.actionGlobalEditProfileFragment(userData)
                    )
                }
                R.id.view_transaction_history -> {
                    findNavController().navigateSafe(
                        SettingsFragmentDirections.actionSettingsFragmentToTransactionHistoryFragment(
                            viewModel.sharedPrefs.getSelectedProfile()?.aliasName ?: ""
                        )
                    )
                }
                R.id.view_switch_account -> {
                    if (sharedPrefs.isParentalPinExists()
                        && sharedPrefs.getParentalRating()?.ageRatingName.takeIf { rating ->
                            rating == null || rating == getString(R.string.no_restrictions)
                        } == null
                    ) {
                        isResultHandled = false
                        findNavController().navigateSafe(
                            SettingsFragmentDirections.actionGlobalParentalControlBottomDialogFragment(
                                actionBeforeOpeningBottomSheet = ACTION_PIN_VERIFICATION,
                                ageRatingValue = null,
                                fromNudge = false
                            )
                        )
                    } else {
                        findNavController().navigateSafe(
                            SettingsFragmentDirections.actionSettingsFragmentToSwitchAccountFragment()
                        )
                    }
                }
                else -> {
                    showToast(context, "feature under development")
                }
            }
        }
    }

    fun moveToHome(){
        startActivity(
            Intent(activity, LandingActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
    }
}