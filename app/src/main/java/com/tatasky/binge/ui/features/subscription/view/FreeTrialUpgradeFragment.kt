package com.tatasky.binge.ui.features.subscription.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.tatasky.binge.R
import com.tatasky.binge.analytics.COMMON_ERROR_MSG
import com.tatasky.binge.analytics.SOURCE_ACCOUNT
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.service.SubscriptionChangeNotifierService
import com.tatasky.binge.databinding.FragmentTrialUpgradeBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription.adapter.ProviderAdapter
import com.tatasky.binge.ui.features.subscription.viewmodel.SubscriptionViewModel
import com.tatasky.binge.utils.*
import javax.inject.Inject

class FreeTrialUpgradeFragment : BaseFragment<FragmentTrialUpgradeBinding, SubscriptionViewModel>() {
    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics
    override fun getViewModelClass(): Class<SubscriptionViewModel> = SubscriptionViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_trial_upgrade

    override fun getViewModelOwner(): ViewModelStoreOwner = findNavController().getViewModelStoreOwner(R.id.subscription)

    override fun setObserver() {
        viewModel.getUpgradeTrialResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val dthStatus = viewModel.getDthStatus()
                val currentPack = viewModel.getCurrentSubscription()
                if (dthStatus.equals(AccountStatusEnum.DEACTIVATED.status, true)) {
                    showDialog(DialogModel(
                        false,
                        R.drawable.ic_subscription_error,
                        getString(R.string.account_inactive),
                        getString(R.string.recharge),
                        getString(R.string.skip),
                        getString(R.string.message_inactive_dth_account)
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
                } else if (dthStatus.equals(AccountStatusEnum.TEMP_SUSPENSION.status, true)) {
                    showDialog(DialogModel(
                        false,
                        R.drawable.ic_subscription_error,
                        getString(R.string.account_suspended),
                        getString(R.string.ok),
                        null,
                        getString(R.string.message_temp_suspension_dth_account)
                    ),
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
                } else if ((dthStatus.equals(
                        AccountStatusEnum.ACTIVE.status,
                        true
                    ) && currentPack?.accountSubStatus.equals(
                        AccountStatusEnum.PARTIALLY_DUNNED.status,
                        true
                    ))
                ) {
                    showDialog(DialogModel(
                        false,
                        R.drawable.ic_subscription_error,
                        getString(R.string.alert),
                        getString(R.string.recharge),
                        getString(R.string.skip),
                        getString(R.string.message_partial_dunned_dth_account)
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
                } else {
                    viewModel.clearAddAndDrop()
                    binding.upgradeResponse = it.data
                    binding.additionalProvidersRecycler.layoutManager =
                        GridLayoutManager(context, minOf(it.data?.upgradedProviders?.size ?: 0, 3))
                    binding.additionalProvidersRecycler.adapter =
                        ProviderAdapter(it.data?.upgradedProviders ?: emptyList())
                    binding.baseProvidersRecycler.adapter =
                        ProviderAdapter(it.data?.basePackProviders ?: emptyList())
                    binding.btnUpgrade.setOnClickListener {
                        viewModel.requestSubscriptionModification()
                    }
                    viewModel.getCurrentSubscription()?.let { currentPack ->
                        viewModel.dropPack(currentPack.packId!!)
                        viewModel.addNewPack(it.data?.upgradePackId!!)
                    }
                    subscriptionAnalytics.trackInitiatePackSelection(activity?.intent?.extras?.getString("fromScreen") ?: SOURCE_ACCOUNT, activity?.intent?.extras?.getBoolean("isFromNudge") ?: false)
//                    subscriptionAnalytics.trackPackSelection(it.data?.packName ?: "","FREE",it.data?.packPrice?:"")
                    subscriptionAnalytics.trackModifyPackInitiate(activity?.intent?.extras?.getString("fromScreen") ?: SOURCE_ACCOUNT, it.data?.packName ?: "", it.data?.packPrice ?: "", "Upgrade")
                }
            }
        })

        viewModel.getPurchasedPackResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                when (response.code) {
                    CODE_SUCCESS -> {
                        viewModel.saveSubscription(response.data)
                        viewModel.selectedPack = response.data
                        /*subscriptionAnalytics.trackSubscribeSuccess(
                            viewModel.selectedPack?.packType ?: "Free",
                            activity?.intent?.extras?.getString("fromScreen") ?: SOURCE_HOME,
                            viewModel.selectedPack?.packName ?: "",
                            viewModel.selectedPack?.packPrice ?: "",
                            activity?.intent?.extras?.getBoolean("isFromNudge") ?: false,
                            response.data?.isFDRRaised == true,
                            if (response.data?.isFDRRaised == true) viewModel.getCurrentSubscription()?.nudges?.paidPackSelectionNudge?.let { (it.totalFreeTrialDuration - it.availableDays) + 1 }
                                ?.toString() ?: "" else "",
                            "Upgrade",
                            "",
                            ""
                        )*/
                        /*subscriptionAnalytics.trackModifyPackSuccess(
                            activity?.intent?.extras?.getString("fromScreen") ?: SOURCE_HOME,
                            viewModel.selectedPack?.packName ?: "",
                            viewModel.selectedPack?.packPrice ?: "",
                                "Upgrade"
                        )*/
                        activity?.let {
                            it.startService(Intent(it, SubscriptionChangeNotifierService::class.java).apply {
                                putExtra("title", getString(R.string.plan_upgraded))
                                putExtra("message", response.data?.modificationMessage?:"Your free trial subscription has been upgraded successfully.")
                            })
                            if (!activity?.intent?.extras?.getString("selectedAppId").isNullOrBlank()) {
                                it.setResult(Activity.RESULT_OK)
                                it.finish()
                            } else {
                                startHomeScreen(activity)
                            }
                        }
                    }
                    else -> {
                        var title = COMMON_ERROR_MSG
//                        subscriptionAnalytics.trackSubscribeFailure(
//                            response.message ?: title,
//                            response.data?.packName ?: "Premium",
//                            response.data?.packType ?: "Free",
//                            "",
//                            ""
//                        )
                        if (response.code == RESPONSE_CODE_DOWNGRADE_ERROR || response.code == RESPONSE_CODE_DOWNGRADE_ERROR2) {
                            title = getString(R.string.cannot_downgrade)
                        } else if (response.code == RESPONSE_CODE_DOWNGRADE_ERROR3) {
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
                                        findNavController().popBackStack(
                                            R.id.currentSubscriptionFragment,
                                            false
                                        )
                                    else if (findNavController().graph.startDestination == R.id.rechargeSubscriptionFragment)
                                        findNavController().popBackStack(
                                            R.id.rechargeSubscriptionFragment,
                                            false
                                        )
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
    }

    override fun toBeCalledOnce() {
        if (activity?.intent?.extras?.getBoolean("fromLogin") == true
            || activity?.intent?.extras?.getString("fromScreen") == SOURCE_NOTIFICATION
        ) {
            viewModel.isFromLogin = true
        }
        viewModel.fetchTrialUpgradeDetails()
        binding.btnNotNow.setOnClickListener {
            subscriptionAnalytics.trackAddPackSkip()
            if (!viewModel.isFromLogin) {
                activity?.finish()
            } else {
                startHomeScreen(activity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RechargeActivity.RECHARGE_REQUEST_CODE) {
            if (1 == data?.getIntExtra("rechargeStatus", 0))
                showDialog(DialogModel(false, R.drawable.ic_success_tick, getString(R.string.payment_success), getString(R.string.proceed), null, getString(R.string.subscription_payment_success_msg)), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                        viewModel.fetchTrialUpgradeDetails()
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

}