package com.tatasky.binge.ui.features.subscription.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.analytics.DOWNGRADE
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.analytics.UPGRADE
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentSubscriptionBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription.model.Cancellation
import com.tatasky.binge.ui.features.subscription.viewmodel.SubscriptionViewModel
import com.tatasky.binge.utils.*
import javax.inject.Inject

class SubscriptionFragment : CancellationBaseFragment<FragmentSubscriptionBinding, SubscriptionViewModel>() {
    private var firestickAvailableAtPinCode: Boolean = false

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun getViewModelClass(): Class<SubscriptionViewModel> =
        SubscriptionViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_subscription

    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.subscription)

    override fun setObserver() {
        super.setObserver()
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!findNavController().navigateUp())
                    startHomeScreen(activity)
            }
        })
        viewModel.getCancellationResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                viewModel.fetchCurrentSubscriptionAfterCancellation()
            }
        })
        viewModel.getCurrentSubscriptionResponseAfterCancellation().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                navigateToSummaryScreen()
            }
        })
        viewModel.rechargeResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.data?.let {
                if (it.rechargeUrl.isNullOrBlank()) {
                    onError(ErrorModel(message = "Recharge Cannot be processed please try after some time"))
                    return@Observer
                }
                try {
                    launchRechargeActivity(this, Uri.parse(it.rechargeUrl))
                } catch (e: Exception) {
                    onError(ErrorModel(message = "Recharge Cannot be processed please try after some time"))
                }
            }
        })
        viewModel.getWalletBalance().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { walletBalance ->
                when (walletBalance.code) {
                    CODE_SUCCESS -> {
                        viewModel.startRecharge(walletBalance.data?.recommendedRechargeAmount!!)
                    }
                    else -> {
                        onError(ErrorModel(walletBalance.code, walletBalance.message))
                    }
                }
            }
        })
        viewModel.getPacksResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val currentPack = viewModel.getCurrentSubscription()
                val dthStatus = viewModel.getDthStatus()
                if (dthStatus.equals(AccountStatusEnum.DEACTIVATED.status, true)) {
                    showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.account_inactive), getString(R.string.recharge), getString(R.string.skip), getString(R.string.message_inactive_dth_account)),
                        object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                viewModel.startRecharge()
                            }

                            override fun onSecondaryButtonClick() {
                                hideDialog()
                                if (!viewModel.isFromLogin) {
                                    activity?.finish()
                                } else {
                                    startHomeScreen(activity)
                                }
                            }

                            override fun onCloseButtonClick() {
                                hideDialog()
                            }
                        })
                } else if (dthStatus.equals(AccountStatusEnum.TEMP_SUSPENSION.status, true)) {
                    showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.account_suspended), getString(R.string.ok), null, getString(R.string.message_temp_suspension_dth_account)),
                        object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                hideDialog()
                                if (!viewModel.isFromLogin) {
                                    activity?.finish()
                                } else {
                                    startHomeScreen(activity)
                                }
                            }

                            override fun onSecondaryButtonClick() {
                                hideDialog()
                            }

                            override fun onCloseButtonClick() {
                                hideDialog()

                            }
                        })
                } else if ((dthStatus.equals(AccountStatusEnum.ACTIVE.status, true) && it.data?.accountSubStatus.equals(AccountStatusEnum.PARTIALLY_DUNNED.status, true))) {
                    showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.alert), getString(R.string.recharge), getString(R.string.skip), getString(R.string.message_partial_dunned_dth_account)),
                        object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                hideDialog()
                                viewModel.startRecharge()
                            }

                            override fun onSecondaryButtonClick() {
                                hideDialog()
                                if (!viewModel.isFromLogin) {
                                    activity?.finish()
                                } else {
                                    startHomeScreen(activity)
                                }
                            }

                            override fun onCloseButtonClick() {
                                hideDialog()
                            }
                        })
                } else {
                    firestickAvailableAtPinCode = it.data?.firestickAvilableAtPinCode ?: false
                    viewModel.sufficientBalance = it.data?.sufficientBalance ?: true
                    binding.tvSubLandingTitle.text = it.data?.title
                    binding.groupPackListing.show()

                    it.data?.subTitle?.takeIf { it.isNotBlank() }?.let {
                        binding.tvSubLandingMessage.text = it
                        binding.tvSubLandingMessage.show()
                    } ?: run {
                        binding.tvSubLandingMessage.hide()
                    }

                    it.data?.verbiage?.footerMessage?.takeIf { it.isNotBlank() }?.let {
                        binding.tvSubscriptionFooterMsg.text = it
                        binding.tvSubscriptionFooterMsg.show()
                    } ?: binding.tvSubscriptionFooterMsg.hide()

                    if (!viewModel.isFromLogin) {
                        binding.llPageIndicator.hide()
                    } else {
                        binding.llPageIndicator.show()
                    }
                }
            }
        })
        viewModel.getDisableProceed().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding.btnProceed.isEnabled = !it
            }
        })
        viewModel.getKnowMoreClickedDetail().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                showKnowMoreDialog(it)
            }
        })
    }

    override fun toBeCalledOnce() {
        if(shouldStartCancellationTrigger(true)){
            viewModel.fetchBaIdList(sharedPrefs.getOriginalSubscriberId())
        }
        val source = activity?.intent?.extras?.getString("fromScreen")?: SOURCE_HOME
        subscriptionAnalytics.trackViewPackListingScreen()
        subscriptionAnalytics.trackInitiatePackSelection(source, activity?.intent?.extras?.getBoolean("isFromNudge")?: false)
        if (activity?.intent?.extras?.getBoolean("fromLogin") == true || activity?.intent?.extras?.getString("fromScreen") == SOURCE_NOTIFICATION) {
            viewModel.isFromLogin = true
        }
        if (!activity?.intent?.extras?.getString("selectedAppId").isNullOrBlank()) {
            viewModel.setToSubscribePartner(activity?.intent?.extras?.getString("selectedAppId")!!)
        }
        viewModel.fetchPackList()
        binding.btnProceed.setOnClickListener {
            val currentPack = viewModel.getCurrentSubscription()
            if ((currentPack == null || currentPack.doNotConsiderThePack) && !viewModel.sufficientBalance) {
                showDialog(DialogModel(
                    false,
                    R.drawable.ic_subscription_error,
                    getString(R.string.low_balance),
                    getString(R.string.recharge),
                    getString(R.string.skip),
                    viewModel.getPacksResponse().value?.peekContent()?.data?.partiallyDunnedMessage?:getString(R.string.low_wallet_balance_msg)
                ),
                    object : CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            viewModel.getPacksResponse().value?.peekContent()?.data?.lowBalanceAmount?.toIntOrNull()?.let {
                                viewModel.startRecharge(it.toString())
                            }?: viewModel.startRecharge()
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                            if (!viewModel.isFromLogin) {
                                activity?.finish()
                            } else {
                                startHomeScreen(activity)
                            }
                        }

                        override fun onCloseButtonClick() {
                            hideDialog()
                        }
                    })
            } else {
                viewModel.clearAddAndDrop()
                currentPack?.let {
                    viewModel.dropPack(it.packId!!)
                    viewModel.addNewPack(viewModel.selectedPack?.packId!!)
                    if (!it.isCancelled && it.expiredWithinSixtyDays && ((it.subscriptionDetailInfo?.bingeAccountStatus.equals(
                            SubscriptionPackStatusEnum.DEACTIVE.status,
                            true
                        ) && it.isDummyUser == false
                    ) || (it.subscriptionDetailInfo?.bingeAccountStatus.equals(
                            SubscriptionPackStatusEnum.WRITTEN_OFF.status,
                            true
                        ) && it.isDummyUser == true && it.isFDRRaised == true))){
                        viewModel.requestSubscriptionCancellation(
                            Cancellation(
                                bingeCancel = true,
                                primeCancel = false
                            )
                        )
                    } else {
                        navigateToSummaryScreen()
                    }
                } ?: navigateToSummaryScreen()
                subscriptionAnalytics.trackAddPackContinue(viewModel.selectedPack?.packName ?: "" ,viewModel.selectedPack?.packType?:"",viewModel.selectedPack?.packPrice?:"")
//                subscriptionAnalytics.trackPackSelection(viewModel.selectedPack?.packName ?: "",viewModel.selectedPack?.packType?:"",viewModel.selectedPack?.packPrice?:"")
            }
        }
        binding.btnSkip.setOnClickListener {
            if (!viewModel.isFromLogin) {
                activity?.finish()
            } else {
                startHomeScreen(activity)
            }
            subscriptionAnalytics.trackAddPackSkip()
        }
    }
    private fun navigateToSummaryScreen() {
        if (!viewModel.isFsTaken() && firestickAvailableAtPinCode && viewModel.selectedPack?.eligibleFirestick == false && viewModel.getCurrentSubscription()?.takeIf { !it.doNotConsiderThePack } == null) {
            // Only to be shown to first time binge mobile user i.e before
            val title = getText(R.string.firestick_promo_text)
            val primaryBtn = getText(R.string.upgrade_to_premium)
            val ftvUpsellDialog = FireStickUpsellDialog.newInstance(
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        if(viewModel.selectFireStickPack()) {
                            findNavController().navigateSafe(
                                SubscriptionFragmentDirections.actionSubscriptionFragmentToSubscriptionSummaryFragment()
                            )
                        }
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                        hideDialog()
                        findNavController().navigateSafe(
                            SubscriptionFragmentDirections.actionSubscriptionFragmentToSubscriptionSummaryFragment()
                        )
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }

                }, title, primaryBtn
            )
            subscriptionAnalytics.trackUpSellView(viewModel.selectedPack?.packPrice?:"")
            ftvUpsellDialog.show(parentFragmentManager, DIALOG_TAG)
        } else if (viewModel.isFsTaken() && viewModel.selectedPack?.eligibleFirestick == false && viewModel.getCurrentSubscription()?.takeIf { !it.doNotConsiderThePack } != null) {
            showFireStickRecoveryDialog()
        } else {
            if (viewModel.getCurrentSubscription() != null) {
                val currPrice = viewModel.getCurrentSubscription()?.packPrice?.toFloatOrNull()?:0f
                val selectedPrice = viewModel.selectedPack?.packPrice?.toFloatOrNull()?:0f
                val modType = if(selectedPrice-currPrice == 0f){ "Renewal"} else if(selectedPrice-currPrice>0f){
                    UPGRADE} else {DOWNGRADE}
                subscriptionAnalytics.trackModifyPackInitiate(activity?.intent?.extras?.getString("fromScreen") ?: SOURCE_HOME, viewModel.selectedPack?.packName ?: "", viewModel.selectedPack?.packPrice ?: "", modType)
            }
            findNavController().navigateSafe(SubscriptionFragmentDirections.actionSubscriptionFragmentToSubscriptionSummaryFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RechargeActivity.RECHARGE_REQUEST_CODE) {
            if (1 == data?.getIntExtra("rechargeStatus", 0))
                showDialog(DialogModel(false, R.drawable.ic_success_tick, getString(R.string.payment_success), getString(R.string.proceed), null, getString(R.string.subscription_payment_success_msg)), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                        binding.groupPackListing.hide()
                        viewModel.fetchPackList()
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            else {
                showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.payment_failure), getString(R.string.ok), null, null), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                        activity?.let {
                            findNavController().navigateUpOrOpenHome(it as BaseActivity<*>)
                        }
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            }
        }
    }

    override fun onError(errorModel: ErrorModel) {
        if (errorModel.statusCode == RESPONSE_CODE_SUCCESS || errorModel.statusCode == CODE_SUCCESS) {
            showDialog(
                DialogModel(false, null, errorModel.message, "Ok", null),
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        if (!findNavController().navigateUp())
                            startHomeScreen(activity)
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }
                })
        } else {
            showDialog(
                DialogModel(false,
                    R.drawable.ic_subscription_error,
                    COMMON_ERROR_TITLE,
                    "Ok", null,
                    com.tatasky.binge.utils.COMMON_ERROR_MSG, errorModel.statusCode),
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        if (!findNavController().navigateUp())
                            startHomeScreen(activity)
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }
                })
        }
    }

    private fun showFireStickRecoveryDialog() {
        showDialog(
            DialogModel(
                false,
                R.drawable.ic_subscription_error,
                getString(R.string.firestick_recovery_title),
                getString(R.string.proceed),
                getString(R.string.cancel),
                getString(R.string.firestick_recovery_message),
                null
            ), object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    findNavController().navigateSafe(
                        SubscriptionFragmentDirections.actionSubscriptionFragmentToSubscriptionSummaryFragment()
                    )
                }

                override fun onSecondaryButtonClick() {
                    hideDialog()
                    navController().navigateUp()
                }

                override fun onCloseButtonClick() {
                }
            }
        )
    }

    private fun showKnowMoreDialog(knowMore: PartnerPacks.KnowMore) {
        KnowMoreDialog.newInstance(knowMore).show(parentFragmentManager, DIALOG_TAG)
    }
}