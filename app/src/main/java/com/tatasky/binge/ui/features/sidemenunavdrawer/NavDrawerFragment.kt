package com.tatasky.binge.ui.features.sidemenunavdrawer

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.NO
import com.tatasky.binge.analytics.YES
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.data.networking.models.response.SubscriberProfileListModel
import com.tatasky.binge.databinding.FragmentNavDrawerBinding
import com.tatasky.binge.helper.circularImageLoad
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.more.SettingsViewModel
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.PaymentUtility.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers
import java.util.*

class NavDrawerFragment : BaseFragment<FragmentNavDrawerBinding, SettingsViewModel>() {
    override fun getViewModelClass(): Class<SettingsViewModel> {
        return SettingsViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_nav_drawer
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return requireActivity()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun setObserver() {
        viewModel.unreadNotificationCount().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let{unreadNotificationCount ->
                e("NavDrawerObserver","unreadNotificationCount:$unreadNotificationCount")
//                binding.menuItemNotification.count = unreadNotificationCount
                if(unreadNotificationCount > 0)
                    binding.menuItemNotification.ivSettingsItem.setImageResource(R.drawable.ic_notifications)
                else
                    binding.menuItemNotification.ivSettingsItem.setImageResource(R.drawable.ic_no_notification)
            }
        }
        viewModel .getProfileInfo().observe(viewLifecycleOwner) {
            setupHeaderData(it.peekContent())
        }
        viewModel.getUserLoogedIn().observe(viewLifecycleOwner) {
            when (it.getContentIfNotHandled()) {
                false -> {
                    binding.header.apply {
                        tvTitleProfile.visibility = View.GONE
                        tvTextLogin.visibility = View.GONE
                    }
                    //saveUserProfile(null)
                    lifecycleScope.launchWhenResumed {
                        viewModel.setFromSavedProfileInfo()
                    }
                }
                else -> {
                    setupHeaderData(null)
                }
            }
        }

        viewModel.getWalletBalance().observe(viewLifecycleOwner) {
            it.peekContent().let { walletBalance ->
                walletBalance?.data?.balanceQueryRespDTO?.let { rechargeResponse ->
                    ("₹ " + rechargeResponse.balance).also {
                        binding.menuItemWithRecharge.menuItemArrowText = it
                    }
                    binding.menuItemWithRecharge.tvArrowSettingsItem.setTextColor(resources.getColor(R.color.darkOnSecondary))
                    binding.menuItemWithRecharge.tvArrowSettingsItem.show()
                    binding.menuItemWithRecharge.btnDescText = walletBalance.data?.endDateVerbiage
                    if(walletBalance.data?.highlightDateVerbiage == true)
                        binding.menuItemWithRecharge.tvBtnDesc.setTextColor(resources.getColor(R.color.darkOnSecondary))
                }
            }
        }

        viewModel.rechargeResponse().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.data?.let {
                if (it.rechargeUrl.isNullOrBlank()) {
                    onError(ErrorModel(message = "Recharge cannot be processed now please try after some time"))
                }
                try {
                    launchRechargeActivity(this, Uri.parse(it.rechargeUrl))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        viewModel.getCurrentSubscription().observe(viewLifecycleOwner){
            it.getContentIfNotHandled().let{currentPlan->
                binding.menuItemWithRenew.menuItemArrowText =
                    if (currentPlan?.freemiumCombo == true) currentPlan?.comboPlanName?:"" else currentPlan?.productName?:""
                if (currentPlan == null || !sharedPrefs.getLoginStatus()) {
                    binding.menuItemGoVip.root.visibility = View.VISIBLE
                    binding.menuItemWithRenew.root.visibility = View.GONE
                } else if (currentPlan.freeTrialStatus == true && (currentPlan.subscriptionType.equals(
                        subscriptionTypeAtv
                    ) || currentPlan.subscriptionType.equals(subscriptionTypeFtv))
                ) {
                    binding.menuItemGoVip.root.visibility = View.GONE
                    binding.menuItemWithRenew.root.visibility = View.VISIBLE
                }
                else if (currentPlan.freeTrialStatus == true && currentPlan.isInactive) {
                    binding.menuItemGoVip.root.visibility = View.VISIBLE
                    binding.menuItemWithRenew.root.visibility = View.GONE
                }
                else {
                    binding.menuItemWithRenew.root.visibility = View.VISIBLE
                    binding.menuItemWithRenew.menuItemArrowText =
                        if (currentPlan.freemiumCombo) currentPlan.comboPlanName else currentPlan.productName


                    if(currentPlan.planCTADetails?.getPlanOption == true){
                        binding.menuItemWithRenew.menuActionLayoutVisible = true
                        binding.menuItemWithRenew.btnActionText = currentPlan.planCTADetails?.getPlanText
                        binding.menuItemWithRenew.btnDescText = currentPlan.planCTADetails?.renewPlanVerbiage
                        if (currentPlan.planCTADetails?.expiredVerbiage == true) {
                            context?.let { it ->
                                binding.menuItemWithRenew.tvBtnDesc.setTextColor(
                                    ContextCompat.getColor(
                                        it,
                                        R.color.darkError
                                    )
                                )
                            }
                        }
                    }
                    else if(currentPlan.planCTADetails?.renewPlanOption == true){
                        binding.menuItemWithRenew.menuActionLayoutVisible = true
                        binding.menuItemWithRenew.btnActionText = currentPlan.planCTADetails?.renewPlanText
                        binding.menuItemWithRenew.btnDescText = currentPlan.planCTADetails?.renewPlanVerbiage
                        if (currentPlan.planCTADetails?.expiredVerbiage == true) {
                            context?.let { it ->
                                binding.menuItemWithRenew.tvBtnDesc.setTextColor(
                                    ContextCompat.getColor(
                                        it,
                                        R.color.darkError
                                    )
                                )
                            }
                        }
                    } else{
                        binding.menuItemWithRenew.menuActionLayoutVisible = false
                    }
                    binding.menuItemGoVip.root.visibility = View.GONE
                }

                if(!sharedPrefs.getLoginStatus() ||
                    NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true)
                ){
                    binding.menuItemWithRecharge.root.visibility = View.GONE
                }
                else{
                    binding.menuItemWithRecharge.root.visibility = View.VISIBLE
                    binding.menuItemWithRecharge.ivSettingsArrow.visibility = View.GONE
                    binding.menuItemWithRecharge.btnAction.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        recharged = true
        viewModel.fetchBalance()
        if (requestCode == RechargeActivity.RECHARGE_REQUEST_CODE) {
            if (1 == data?.getIntExtra("rechargeStatus", 0))
                showDialog(DialogModel(false, R.drawable.ic_success_tick, getString(R.string.payment_success), getString(R.string.proceed), null, getString(R.string.subscription_payment_success_msg)), object :
                    CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            else {
                showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.payment_failure), getString(R.string.ok), null, null), object :
                    CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            }
        }
    }

    override fun toBeCalledOnce() {
        binding.apply {
            menuItemWithRecharge.ivSettingsArrow.visibility = View.GONE

            footer.tvVersionNumber.text = String.format(
                Locale.getDefault(),
                getString(R.string.version_no),
                BuildConfig.VERSION_NAME
            )

            menuItemHelpAndSupport.menuItemText =
                sharedPrefs.getConfigResponse()?.data?.config?.helpCenterInfo?.helpCenterHeading

            menuItemGoVip.ivSettingsArrow.setImageResource(R.drawable.ic_arrow_right_golden)
            val paint =  menuItemGoVip.tvSettingsItem.paint
            paintPremiumGradient(menuItemGoVip.tvSettingsItem,paint.measureText(getString(R.string.subscribe)))
            menuItemGoVip.tvSettingsItem.setTextColor(resources.getColor(R.color.color_golden_vip))

            header.tvTextLogin.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.LoginClicked))
                }
            })
            menuItemWithRecharge.btnAction.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
//                viewModel.startRecharge()
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.Recharge))
                }
            })
            menuItemWithRenew.btnAction.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    sharedPrefs.getSubscribedPack()?.let{
                        val currentPack = it
                        viewModel.subscriptionAnalytics.trackMyPlanRenewPlan(
                            currentPack.productName?:"",
                            if (!currentPack.isInactive) YES else NO,
                            if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                            else getDifferenceBetweenTwoDates(
                                sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                                getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                                SERVER_DATE_TIME_FORMAT
                            ),
                            currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                                ?: ""
                        )
                    }
                    viewModel.miscAnalytics.trackMixPanelMenuOption()

                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.Renew))
                }
            })

            menuItemWithRenew.root.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.MyPlanClicked))
                }
            })

            menuItemGoVip.root.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.GoVipClicked))
                }
            })


            menuItemBingeList.root.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.BingeListClicked))
                }
            })

            menuItemNotification.root.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.NotificationClicked))
                }
            })

            menuItemSettings.root.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.SettingsClicked))
                }
            })

            menuItemHelpAndSupport.root.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    viewModel.miscAnalytics.trackHelpCenterClick()
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.HelpAndSupportClicked))
                }
            })

            footer.tvTermsCondition.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.TnCClicked))
                }
            })

            footer.tvTvPrivacyPolicy.setOnClickListener(object : SingleClickListener() {
                override fun onClicked(v: View?) {
                    viewModel.miscAnalytics.trackMixPanelMenuOption()
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.PrivacyPolicyClicked))
                }
            })
        }
    }

    private fun setupHeaderData(hr: SubscriberProfileListModel?) {
        binding.apply {
            if(!sharedPrefs.getLoginStatus() ||
                NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true)
            ){
                menuItemWithRecharge.root.visibility = View.GONE
            }
            else{
                menuItemWithRecharge.root.visibility = View.VISIBLE
                menuItemWithRecharge.ivSettingsArrow.visibility = View.GONE
                menuItemWithRecharge.btnAction.visibility = View.VISIBLE
            }
        }
        binding.header.apply {
            if(!sharedPrefs.getLoginStatus()){
                tvTitleProfile.visibility = View.VISIBLE
                tvSubtitleProfile.visibility = View.GONE
                tvTextDeviceName.visibility = View.GONE
                tvTextLogin.visibility = View.VISIBLE

                tvTitleProfile.text = getString(R.string.text_un_logged_in_header)
            }
            else {
                if (hr?.userData != null) {
                    saveUserProfile(hr)
                }
                else{
                    //for non-dth user unsubscribed with details not keyed-in
                    viewModel.sharedPrefs.getClearRMN().also {
                        if (it.isEmpty()) {
                            tvTitleProfile.visibility = View.GONE
                        } else {
                            tvTitleProfile.text = getString(R.string.mobile_template, it)
                            tvTitleProfile.visibility = View.VISIBLE
                        }
                    }
                    tvSubtitleProfile.visibility = View.GONE
                    tvTextDeviceName.visibility = View.GONE
                    tvTextLogin.visibility = View.GONE
                }
            }
            ivProfile.setOnClickListener {
                if (hr?.userData != null) {
                    viewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.EditProfile))
                }
            }
        }
    }


    private fun saveUserProfile(selectedProfile : SubscriberProfileListModel?) {

        var selectedProfileSaved = viewModel.sharedPrefs.getSelectedProfile()
        if(selectedProfile != null) {
            if (selectedProfileSaved == null) {
                selectedProfileSaved = LoginResponse.BingeSubscription()
                selectedProfileSaved.baId = selectedProfile?.userData?.id
                selectedProfileSaved.aliasName = selectedProfile?.userData?.aliasName
                selectedProfileSaved.profileId = selectedProfile?.userData?.profileId
                selectedProfileSaved.rmn = selectedProfile?.userData?.rmn
                selectedProfileSaved.firstName = selectedProfile?.userData?.firstName
                selectedProfileSaved.lastName = selectedProfile?.userData?.lastName
                selectedProfileSaved.emailId = selectedProfile?.userData?.email
                selectedProfileSaved.aliasName = selectedProfile?.userData?.aliasName
                selectedProfileSaved.imageUrl = selectedProfile?.userData?.image
            }
            viewModel.sharedPrefs.setSelectedProfile(selectedProfileSaved)
        }
        updateUserProfile(selectedProfileSaved)
    }

    private fun updateUserProfile(hr: LoginResponse.BingeSubscription?) {
        val fName = hr?.firstName
        val lName = hr?.lastName
        if (fName.isNullOrEmpty()) {
            binding.header.tvTitleProfile.visibility = View.GONE
        } else {
            "$fName $lName".also { binding.header.tvTitleProfile.text = it }
            binding.header.tvTitleProfile.visibility = View.VISIBLE
        }
        sharedPrefs.getClearRMN().also {
            if (it.isEmpty()) {
                binding.header.tvSubtitleProfile.visibility = View.GONE
            } else {
                binding.header.tvSubtitleProfile.text = getString(R.string.mobile_template, it)
                binding.header.tvSubtitleProfile.visibility = View.VISIBLE
            }
        }
        if(NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true))
            binding.header.tvTextDeviceName.visibility = View.GONE
        else
            hr?.aliasName.also {
                if (it.isNullOrEmpty()) {
                    binding.header.tvTextDeviceName.visibility = View.GONE
                } else {
                    binding.header.tvTextDeviceName.text = it
                    binding.header.tvTextDeviceName.visibility = View.VISIBLE
                }
            }
        val config = viewModel.sharedPrefs.getConfigResponse()
        val w = dpToPx(requireContext(), 48)
//                    if(hr?.image?.isNotEmpty() == true) {
//                        hr?.image?.let {
        val imgUrl = getCloudinaryUrl(
            viewModel.sharedPrefs.getCloudenieryUrl(),
            w, w,
            config?.data?.config?.subscriberImage?.imageBaseUrl + hr?.imageUrl
        )
        circularImageLoad(binding.header.ivProfile, imgUrl)
//                        }
//                    }
        binding.header.tvTextLogin.visibility = View.GONE
    }
}