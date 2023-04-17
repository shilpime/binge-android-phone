package com.tatasky.binge.ui.features.subscription.view

import android.net.Uri
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_ACCOUNT
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.PurchasePackResponse
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseFragment
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.ui.features.subscription.model.Cancellation
import com.tatasky.binge.ui.features.subscription.viewmodel.SubscriptionViewModel
import com.tatasky.binge.utils.AccountStatusEnum
import com.tatasky.binge.utils.DIALOG_TAG
import com.tatasky.binge.utils.startHomeScreen
import java.util.*

abstract class CommonCurrentSubscriptionFragment<VB : ViewDataBinding> : CancellationBaseFragment<VB, SubscriptionViewModel>() {
    override fun getViewModelClass(): Class<SubscriptionViewModel> =
        SubscriptionViewModel::class.java

    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.subscription)

    abstract fun setPackInformation()

    override fun setObserver() {
        super.setObserver()
        // DONOT refactor this to lambda
        viewModel.getCurrentSubscriptionResponse().observe(viewLifecycleOwner, object :
            Observer<SingleEvent<PurchasePackResponse>> {
            override fun onChanged(t: SingleEvent<PurchasePackResponse>?) {
                t?.getContentIfNotHandled()
            }
        })

        viewModel.cancelRequest().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { c ->
                if (c.bingeCancel && c.primeCancel) {
                    showBingeAndPrimeCancellationDialog(viewModel.getCurrentSubscription()?.primeAutoSelect == true)
                } else if (c.primeCancel) {
                    showPrimeCancellationDialog()
                } else if (c.bingeCancel) {
                    showOnlyBingeCancellationDialog()
                }
            }
        })
        viewModel.getCancellationResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                viewModel.fetchCurrentSubscription()
                showDialog(
                    DialogModel(false, R.drawable.ic_success_tick, it.data?.displayMessage?.title?:"Cancellation Request Taken" , getString(
                        R.string.ok), null, it.data?.displayMessage?.message), object :
                        CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        viewModel.releaseCurrentSubscriptionResponse()
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }
                })
            }
        })
        viewModel.getCancellationRevokeResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                viewModel.subscriptionAnalytics.trackSubscriptionRevoke(activity?.intent?.extras?.getString("fromScreen")?: SOURCE_ACCOUNT)
                showDialog(
                    DialogModel(false, R.drawable.ic_success_tick, it.data?.displayMessage?.title?:getString(
                        R.string.revoke_success_title), getString(R.string.proceed), null,it.data?.displayMessage?.message), object :
                        CommonDialogEventListener {
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
            }
        })
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (false == activity?.isTaskRoot) {
                    activity?.finish()
                } else {
                    startHomeScreen(activity)
                }
            }
        })
    }


    protected fun beginCancellationProcess() {
        if(!viewModel.getCurrentSubscription()?.cancelBtnMessage.isNullOrBlank()){
            showDialog(DialogModel(false, R.drawable.ic_subscription_error, viewModel.getCurrentSubscription()?.cancelBtnMessage, getString(R.string.ok), null), object :
                CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    hideDialog()
                }

                override fun onSecondaryButtonClick() {
                    hideDialog()
                }

                override fun onCloseButtonClick() {
                }
            })
        } else if (true == viewModel.getCurrentSubscription()?.primePackDetails?.let { (it.isActive || it.isSuspended) && it.platform.equals("mobile" , true) && !it.primeCancellationRaised}) {
            showCancellationDialog()
        } else {
            showOnlyBingeCancellationDialog()
        }
    }

    private fun showBingeAndPrimeCancellationDialog(forced : Boolean) {
        showDialog(DialogModel(false, R.drawable.ic_remove_subscription, viewModel.getCurrentSubscription()?.verbiage?.cancelSubs?.confirmationDialogBox?.title ?: getString(R.string.cancellation_title), "Yes", getString(R.string.cancel), if(forced) viewModel.getCurrentSubscription()?.verbiage?.cancelSubs?.confirmationDialogBox?.message else viewModel.getCurrentSubscription()?.verbiage?.cancelSubs?.confirmationDialogBox?.subTitle), object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                viewModel.requestSubscriptionCancellation(Cancellation(true, primeCancel = true))
                hideDialog()
            }

            override fun onSecondaryButtonClick() {
                hideDialog()
                viewModel.subscriptionAnalytics.trackSubscriptionCancelSkip()
            }

            override fun onCloseButtonClick() {
                hideDialog()
            }
        })
    }

    private fun showOnlyBingeCancellationDialog() {
        showDialog(DialogModel(false, R.drawable.ic_remove_subscription, viewModel.getCurrentSubscription()?.verbiage?.cancelSubs?.confirmationDialogBox?.title ?: getString(R.string.cancellation_title), "Yes", getString(R.string.cancel), text = viewModel.getCurrentSubscription()?.verbiage?.cancelSubs?.confirmationDialogBox?.subTitle), object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                viewModel.requestSubscriptionCancellation(Cancellation(bingeCancel = true, primeCancel = false))
                hideDialog()
            }

            override fun onSecondaryButtonClick() {
                hideDialog()
                viewModel.subscriptionAnalytics.trackSubscriptionCancelSkip()
            }

            override fun onCloseButtonClick() {
                hideDialog()
            }
        })
    }

    private fun showPrimeCancellationDialog() {
        showDialog(DialogModel(false, R.drawable.ic_remove_subscription, viewModel.getCurrentSubscription()?.verbiage?.cancelSubs?.confirmationDialogBox?.primeTitle?:viewModel.getCurrentSubscription()?.verbiage?.cancelSubs?.confirmationDialogBox?.title ?: getString(R.string.cancellation_title), "Yes", getString(R.string.cancel)), object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                viewModel.requestSubscriptionCancellation(Cancellation(bingeCancel = false, primeCancel = true))
                hideDialog()
            }

            override fun onSecondaryButtonClick() {
                hideDialog()
                viewModel.subscriptionAnalytics.trackSubscriptionCancelSkip()
            }

            override fun onCloseButtonClick() {
                hideDialog()
            }
        })
    }

    private fun showCancellationDialog() {
        val cancellationSelectionDialog = CancellationSelectionDialog.newInstance(viewModel)
        cancellationSelectionDialog.show(parentFragmentManager, DIALOG_TAG)
    }

    protected fun handleModifyBlockedDialog(accountSubStatus : String?, msg : String){
        val dthStatus = viewModel.getDthStatus()
        when (dthStatus.toLowerCase(Locale.getDefault())) {
            AccountStatusEnum.DEACTIVATED.status.toLowerCase(Locale.getDefault()) -> {
                showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.alert), getString(R.string.recharge), getString(R.string.skip), msg), object :
                    CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        viewModel.startRecharge()
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            }
            AccountStatusEnum.TEMP_SUSPENSION.status.toLowerCase(Locale.getDefault()) -> {
                showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.alert), getString(R.string.ok), null, msg), object :
                    CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            }
            AccountStatusEnum.ACTIVE.status.toLowerCase(Locale.getDefault()) -> {
                if (accountSubStatus.equals(
                        AccountStatusEnum.PARTIALLY_DUNNED.status.toLowerCase(
                            Locale.getDefault()), true)) {
                    showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.alert), getString(R.string.recharge), getString(R.string.skip), msg), object :
                        CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            viewModel.startRecharge()
                            hideDialog()
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                        }

                        override fun onCloseButtonClick() {
                        }
                    })
                } else {
                    onError(ErrorModel(message = msg))
                }
            }
            else -> {
                onError(ErrorModel())
            }
        }
    }

    protected fun beginRevokeProcess() {
        showDialog(DialogModel(false, R.drawable.ic_my_subscription, viewModel.getCurrentSubscription()?.verbiage?.revokeSubs?.title?:getString(R.string.resume_subscription_title), "Yes", getString(R.string.cancel)), object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                viewModel.requestSubscriptionRevokeCancellation()
                hideDialog()
            }

            override fun onSecondaryButtonClick() {
                hideDialog()
                viewModel.subscriptionAnalytics.trackSubscriptionRevokeSkip()
            }

            override fun onCloseButtonClick() {
                hideDialog()
            }
        })
    }
}