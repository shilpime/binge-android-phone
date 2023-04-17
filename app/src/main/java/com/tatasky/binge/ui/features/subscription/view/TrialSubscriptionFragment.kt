package com.tatasky.binge.ui.features.subscription.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.databinding.FragmentTrialSubscripitonBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.invisible
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription.adapter.ProviderAdapter
import com.tatasky.binge.ui.features.subscription.viewmodel.SubscriptionViewModel
import com.tatasky.binge.utils.*
import java.util.*
import javax.inject.Inject

class TrialSubscriptionFragment : BaseFragment<FragmentTrialSubscripitonBinding, SubscriptionViewModel>(){

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun getViewModelClass(): Class<SubscriptionViewModel> =
        SubscriptionViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_trial_subscripiton

    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.subscription)


    private fun handleAfterPackPurchaseNavigation() {
        if (!activity?.intent?.extras?.getString("selectedAppId").isNullOrBlank()) {
            activity?.setResult(Activity.RESULT_OK)
            activity?.finish()
        } else {
            startHomeScreen(activity)
        }
    }

    override fun setObserver() {
        viewModel.getPurchasedPackResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                when (response.code) {
                    CODE_SUCCESS -> {
                        viewModel.saveSubscription(response.data)
                        /*subscriptionAnalytics.trackSubscribeSuccess(
                            viewModel.selectedPack?.packType ?: "FREE",
                            activity?.intent?.extras?.getString("fromScreen")?: SOURCE_HOME,
                            viewModel.selectedPack?.packName ?: "",
                            viewModel.selectedPack?.packPrice ?: "",
							activity?.intent?.extras?.getBoolean("isFromNudge")?: false,
                            false,
                            "",
                            "",
                            "",
                            ""
						)*/
                        subscriptionAnalytics.trackStartTrialEvent()
                        when {
                            response.data?.isPaid == false -> {
                                handleAfterPackPurchaseNavigation()
                            }
                        }
                    }
                    RESPONSE_CODE_LOW_BALANCE_ERROR -> {
//                        subscriptionAnalytics.trackSubscribeFailure(
//                            response.message ?: "",
//                            response.data?.packName ?: viewModel.selectedPack?.packName ?: "",
//                            response.data?.packType ?: viewModel.selectedPack?.packType ?: "FREE","",
//                        ""
//                        )
                        showDialog(DialogModel(
                            false,
                            R.drawable.ic_subscription_error,
                            getString(R.string.low_balance),
                            getString(R.string.recharge),
                            getString(R.string.skip),
                            response.message
                        ),
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
                    }
                    else -> {
                        var title = com.tatasky.binge.analytics.COMMON_ERROR_MSG
//                        subscriptionAnalytics.trackSubscribeFailure(
//                            response.message ?: title,
//                            response.data?.packName ?: viewModel.selectedPack?.packName ?: "",
//                            response.data?.packType ?: viewModel.selectedPack?.packType ?: "FREE","",""
//                        )
                        if (response.code == RESPONSE_CODE_DOWNGRADE_ERROR || response.code == RESPONSE_CODE_DOWNGRADE_ERROR2) {
                            title = getString(R.string.cannot_downgrade)
                        } else if(response.code == RESPONSE_CODE_DOWNGRADE_ERROR3){
                            title = getString(R.string.already_downgraded)
                        }
                        showDialog(
                            DialogModel(
                                false,
                                R.drawable.ic_subscription_error,
                                title,
                                getString(R.string.ok),
                                null,
                                response.message
                            ), object : CommonDialogEventListener {
                                override fun onPrimaryButtonClick() {
                                    hideDialog()
                                    if (findNavController().graph.startDestination == R.id.currentSubscriptionFragment)
                                        findNavController().popBackStack(R.id.currentSubscriptionFragment, false)
                                    else if (findNavController().graph.startDestination == R.id.rechargeSubscriptionFragment)
                                        findNavController().popBackStack(R.id.rechargeSubscriptionFragment, false)
                                    else
                                        if (requireActivity().isTaskRoot) {
                                            startHomeScreen(activity)
                                        } else {
                                            activity?.finish()
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
        })
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!findNavController().navigateUp())
                    startHomeScreen(activity)
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
                //From BE we should get only 1 pack
                binding.trialRecycler.recyclerPacks.adapter = ProviderAdapter(it.data!!.packsList[0].appList)
                binding.root.show()
                viewModel.selectedPack = it.data?.packsList?.getOrNull(0)
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
                    viewModel.sufficientBalance = it.data?.sufficientBalance ?: true
                }
                binding.trialExpireMsg.text = it.data?.expiryMessage
                binding.trialPackMsg.text = it.data?.verbiage?.footerMessage
                binding.tvSubLandingTitle.text = it.data?.verbiage?.title?:"Congratulations!"
                binding.tvSubLandingMessage.text = it.data?.verbiage?.desc?:"Start you 7-Days Free Trial"
            }
        })
    }

    override fun toBeCalledOnce() {
        if (activity?.intent?.extras?.getBoolean("fromLogin") == true
            || activity?.intent?.extras?.getString("fromScreen") == SOURCE_NOTIFICATION
        ) {
            viewModel.isFromLogin = true
        }
        val source = activity?.intent?.extras?.getString("fromScreen")?: SOURCE_HOME
        subscriptionAnalytics.trackViewFreeTrialScreen()
        subscriptionAnalytics.trackInitiatePackSelection(source, activity?.intent?.extras?.getBoolean("isFromNudge")?: false)
        if (!activity?.intent?.extras?.getString("selectedAppId").isNullOrBlank()) {
            viewModel.setToSubscribePartner(activity?.intent?.extras?.getString("selectedAppId")!!)
        }
        viewModel.fetchPackList(activity?.intent?.extras?.getString("selectedAppId"))
        binding.trialBtnProceed.setOnClickListener {
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
                skipNavigateToSummary()
                subscriptionAnalytics.trackAddPackContinue(viewModel.selectedPack?.packName ?: "" ,viewModel.selectedPack?.packType?:"",viewModel.selectedPack?.packPrice?:"")
//                subscriptionAnalytics.trackPackSelection(viewModel.selectedPack?.packName ?: "",viewModel.selectedPack?.packType?:"",viewModel.selectedPack?.packPrice?:"")
            }
        }
        binding.trialBtnSkip.setOnClickListener {
            if (!viewModel.isFromLogin) {
                activity?.finish()
            } else {
                startHomeScreen(activity)
            }
            subscriptionAnalytics.trackFreeTrialLater()
        }
    }

    private fun skipNavigateToSummary(){
        if (viewModel.getCurrentSubscription() == null) {
            viewModel.requestSubscriptionCreation()
        } else {
            onError(ErrorModel())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RechargeActivity.RECHARGE_REQUEST_CODE) {
            if (1 == data?.getIntExtra("rechargeStatus", 0))
                showDialog(DialogModel(false, R.drawable.ic_success_tick, getString(R.string.payment_success), getString(R.string.proceed), null, getString(R.string.subscription_payment_success_msg)), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
//                        binding.groupPackListing.hide()
                        viewModel.fetchPackList(activity?.intent?.extras?.getString("selectedAppId"))
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
}