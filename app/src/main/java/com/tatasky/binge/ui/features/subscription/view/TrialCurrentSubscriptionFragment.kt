package com.tatasky.binge.ui.features.subscription.view

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentTrialCurrentSubscriptionBinding
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription.adapter.ProviderAdapter
import com.tatasky.binge.utils.*

class TrialCurrentSubscriptionFragment : CommonCurrentSubscriptionFragment<FragmentTrialCurrentSubscriptionBinding>() {

    override fun layoutId(): Int = R.layout.fragment_trial_current_subscription

    override fun toBeCalledOnce() {
        if (activity?.intent?.extras?.getBoolean("fromLogin") == true
            || activity?.intent?.extras?.getString("fromScreen") == SOURCE_NOTIFICATION
        ) {
            viewModel.isFromLogin = true
        }
        binding.firstTimeLogin = viewModel.isFromLogin
        if(viewModel.isFromLogin){
            binding.btnProceed.show()
        }
        viewModel.fetchCurrentSubscription()
        binding.btnSelectSubscription.setOnClickListener {
            findNavController().navigateSafe(TrialCurrentSubscriptionFragmentDirections.actionTrialSubscribedFragmentToSubscriptionFragment())
        }
        binding.btnProceed.setOnClickListener {
            startHomeScreen(activity)
        }
    }

    private fun subscriptionActiveMode(currentPack: PartnerPacks) {
        binding.selectedPackCard.tvTag.hide()
        binding.selectedPack = currentPack
        binding.selectedPackCard.tvRenewalCycle.text = currentPack.verbiage?.subsTitle
        binding.selectedPackCard.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        if(viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)){
            binding.selectedPackCard.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCard.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCard.tvEligibility.show()
        } else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true) || (currentPack.eligibleFirestick && currentPack.fsTaken)){
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCard.tvEligibility.show()
        } else {
            binding.selectedPackCard.tvEligibility.hide()
        }
    }

    override fun setPackInformation() {
        binding.nsvTrial.show()
        binding.firstTimeLogin = viewModel.firstTimeLogin
        val currentPack = viewModel.getCurrentSubscription()
        if (currentPack != null) {
            subscriptionActiveMode(currentPack)
        } else {
            e("Error", "Pack not found")
            startHomeScreen(activity)
            return
        }
        currentPack.primePackDetails?.let {
            handlePrimeSubscription(currentPack)
        }
        if(viewModel.isFromLogin || isHideSelectPackButton(currentPack) || currentPack.isDummyUser == null || currentPack.isDummyUser == false ){
            binding.btnSelectSubscription.hide()
        } else {
            binding.btnSelectSubscription.show()
        }

        binding.selectedPackCard.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank()) {
                if(currentPack.mobileUpgradable) {
                    findNavController().navigateSafe(TrialCurrentSubscriptionFragmentDirections.actionTrialSubscribedFragmentToUpgradeFragment())
                } else {
                    findNavController().navigateSafe(TrialCurrentSubscriptionFragmentDirections.actionTrialSubscribedFragmentToSubscriptionFragment())
                }
            } else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }

        binding.selectedPackCard.btnRevokeSubscription.setOnClickListener {
            beginRevokeProcess()
        }
        binding.freeTrialExpiryMsg.text = currentPack.verbiage?.expiryMessage
        binding.tvFooter.text = currentPack.verbiage?.footerMessage
        binding.tvSubLandingTitle.text = currentPack.verbiage?.headerMessage
        binding.btnSelectSubscription.text = currentPack.verbiage?.button
        binding.btnCancelSubscription.setOnClickListener { beginCancellationProcess() }
        if (true == currentPack.cancelBtn && !currentPack.isCancelled) {
            binding.btnCancelSubscription.show()
        } else {
            binding.btnCancelSubscription.hide()
        }
        viewModel.firstTimeLogin = false
    }

    private fun handlePrimeSubscription(currentPack: PartnerPacks) {
        binding.amazonPacks.lifecycleOwner = viewLifecycleOwner
        if (true == currentPack.primePackDetails?.isActive || true == currentPack.primePackDetails?.isSuspended) {
            binding.primeVisible = true
            val primePack = currentPack.primePackDetails!!
            if (primePack.imageUrl.isNullOrBlank())
                updateProviderSeeAll(binding.amazonPacks.packIcon, PROVIDER_PRIME, sharedPrefs.getProviderLogo(), sharedPrefs.getCloudenieryUrl())
            else
                transparentImageLoad(binding.amazonPacks.packIcon, primePack.imageUrl?:"")
            var color = ContextCompat.getColor(requireContext(), R.color.pink_50)
            binding.amazonPacks.tvExpiryMsg.setTextColor(color)
            binding.amazonPacks.tvExpiryMsg.text = primePack.expiryDateToDisplay
            binding.amazonPacks.tvPrice.text = String.format(
                getString(R.string.rupees_cyclic),
                "${primePack.getFormattedPrice()}",
                primePack.renewalCycle
            )
            binding.amazonPacks.tvPackDisclaimer.show()
        } else {
            binding.primeVisible = false
        }
    }


    override fun setObserver() {
        super.setObserver()
        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if (it && !viewModel.holdCurrentSubscriptionResponse) {
                    //refresh Account page for subscription detail
                    if (viewModel.getCurrentSubscription()?.isPaid == true || false == viewModel.getCurrentSubscription()?.subscriptionDetailInfo?.bingeAccountStatus?.equals(SubscriptionPackStatusEnum.ACTIVE.status, true)){
                        activity?.let {
                            startActivity(
                                    getSubscriptionActivityIntent(it, fromLogin = false, selectedAppId = null, fromScreen = SOURCE_HOME).apply {
                                        flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                            it.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
                        }
                        return@let
                    }
                    setPackInformation()
                    context?.let {
                        if(true == viewModel.getCurrentSubscription()?.atvCancelled){
                            localBroadcastHelper.sendBroadcast(it, localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH)
                        }
                    }
                }
            }
        })
    }
}