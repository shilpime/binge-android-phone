package com.tatasky.binge.ui.features.subscription.view

import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentCurrentSubscriptionBinding
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription.ChannelsCountAdapter
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription.adapter.ProviderAdapter
import com.tatasky.binge.utils.*
import javax.inject.Inject

class CurrentSubscriptionFragment : CommonCurrentSubscriptionFragment<FragmentCurrentSubscriptionBinding>() {

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics

    override fun layoutId(): Int = R.layout.fragment_current_subscription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            excludeTarget(R.id.tvID, true)
            this.duration = 250
        }
        reenterTransition = backward

        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            excludeTarget(R.id.tvID, true)
            this.duration = 250
        }
        exitTransition = forward
    }

    override fun setObserver() {
        super.setObserver()
        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if (it) {
                    //refresh Account page for subscription detail
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

    override fun toBeCalledOnce() {
        subscriptionAnalytics.trackViewMySubscription()
        binding.lifecycleOwner = viewLifecycleOwner
        if (activity?.intent?.extras?.getBoolean("fromLogin") == true
            || activity?.intent?.extras?.getString("fromScreen") == SOURCE_NOTIFICATION
        ) {
            binding.llPageIndicator.show()
            viewModel.isFromLogin = true
        } else {
            binding.llPageIndicator.hide()
        }
        if (activity?.intent?.extras?.getBoolean("firstTimeLogin") == true) {
            viewModel.firstTimeLogin = true
        }

        binding.btnProceed.setOnClickListener {
            startHomeScreen(activity)
        }
        viewModel.fetchCurrentSubscription()
    }

    override fun setPackInformation() {
        binding.firstTimeLogin = viewModel.firstTimeLogin
        val currentPack = viewModel.getCurrentSubscription()
        if (currentPack == null || currentPack.doNotConsiderThePack) {
            noSubscriptionMode()
        } else {
            if (currentPack.isInactive) {
                subscriptionExpiredMode(currentPack)
            } else if (currentPack.isCancelled) {
                subscriptionCancelledMode(currentPack)
            } else {
                subscriptionActiveMode(currentPack)
            }
            currentPack.primePackDetails?.let {
                handlePrimeSubscription(currentPack)
            }
            binding.tvMsg.text = currentPack.verbiage?.footerVerbiage?.message
            binding.btnCancelSubscription.setOnClickListener { beginCancellationProcess() }
        }
        binding.tvSubLandingTitle.show()
        viewModel.firstTimeLogin = false
    }

    private fun subscriptionCancelledMode(currentPack: PartnerPacks) {
        if(currentPack.isCombo) {
            subscriptionCancelledModeNew(currentPack)
            return
        }
        binding.gpActiveSubscription.show()
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        binding.selectedPackCard.tvTag.hide()
        binding.btnCancelSubscription.hide()
        if (currentPack.isInactive) {
            val color = ContextCompat.getColor(requireContext(), R.color.darkError)
            binding.selectedPackCard.tvRenewalCycle.setTextColor(color)
        }
        binding.selectedPackCard.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCard.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        if (true == currentPack.cancelBtn) {
            binding.selectedPackCard.btnModifySubscription.show()
        } else {
            binding.selectedPackCard.btnModifySubscription.hide()
        }
        binding.selectedPackCard.btnModifySubscription.setOnClickListener {
            beginRevokeProcess()
        }

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

    private fun subscriptionActiveMode(currentPack: PartnerPacks) {
        if(currentPack.isCombo) {
            subscriptionActiveModeNew(currentPack)
            return
        }
        binding.gpActiveSubscription.show()
        binding.selectedPackCard.tvTag.hide()
        if (true == currentPack.cancelBtn) {
            binding.btnCancelSubscription.show()
        } else {
            binding.btnCancelSubscription.hide()
        }
        if (true == currentPack.modifyBtn) {
            binding.selectedPackCard.btnModifySubscription.show()
        } else {
            binding.selectedPackCard.btnModifySubscription.hide()
        }
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        binding.selectedPackCard.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCard.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCard.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank())
                findNavController().navigateSafe(CurrentSubscriptionFragmentDirections.actionCurrentSubscriptionFragmentToSubscriptionFragment())
            else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }
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

    private fun subscriptionExpiredMode(currentPack: PartnerPacks) {
        if(currentPack.isCombo) {
            subscriptionExpiredModeNew(currentPack)
            return
        }
        binding.gpActiveSubscription.show()
        binding.selectedPackCard.tvTag.hide()
        viewModel.selectedPack = currentPack
        binding.selectedPack = currentPack
        if(currentPack.isCancelled){
            binding.selectedPackCard.tvTag.text = CANCELLED
            binding.selectedPackCard.tvTag.show()
        }
        if (true == currentPack.modifyBtn) {
            binding.selectedPackCard.btnModifySubscription.show()
        } else {
            binding.selectedPackCard.btnModifySubscription.hide()
        }
        if (true == currentPack.cancelBtn && !currentPack.isCancelled) {
            binding.btnCancelSubscription.show()
        } else {
            binding.btnCancelSubscription.hide()
        }
        binding.gpNoSubscription.hide()
        val color = ContextCompat.getColor(requireContext(), R.color.darkError)
        binding.selectedPackCard.tvRenewalCycle.setTextColor(color)
        binding.selectedPackCard.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCard.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCard.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank())
                findNavController().navigateSafe(CurrentSubscriptionFragmentDirections.actionCurrentSubscriptionFragmentToSubscriptionFragment())
            else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }
        if (viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCard.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCard.tvEligibility.show()
        } else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true)) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCard.tvEligibility.show()
        } else {
            binding.selectedPackCard.tvEligibility.hide()
        }
    }

    private fun noSubscriptionMode() {
        viewModel.firstTimeLogin = false
        binding.firstTimeLogin = viewModel.firstTimeLogin
        binding.gpNoSubscription.show()
        binding.gpActiveSubscription.hide()
        binding.gpActiveSubscriptionNew.hide()
        binding.selectedPackCard.tvTag.hide()
        binding.selectedPackCardNew.tvTag.hide()
        binding.tvMsg.hide()
        binding.btnCancelSubscription.hide()
        binding.btnSelectSubscription.setOnClickListener {
            if(shouldStartCancellationTrigger(true))
                viewModel.fetchBaIdList(sharedPrefs.getOriginalSubscriberId())
            else
                findNavController().navigateSafe(CurrentSubscriptionFragmentDirections.actionCurrentSubscriptionFragmentToSubscriptionFragment())
        }
        binding.primeVisible = false
    }

/*New UI handling*/

    private fun subscriptionActiveModeNew(currentPack: PartnerPacks) {
        binding.gpActiveSubscription.hide()
        binding.gpActiveSubscriptionNew.show()
        binding.selectedPackCardNew.tvTag.hide()
        if (true == currentPack.cancelBtn) {
            binding.btnCancelSubscription.show()
        } else {
            binding.btnCancelSubscription.hide()
        }
        if (true == currentPack.modifyBtn) {
            binding.selectedPackCardNew.btnModifySubscription.show()
        } else {
            binding.selectedPackCardNew.btnModifySubscription.hide()
        }
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        binding.selectedPackCardNew.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCardNew.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCardNew.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank())
                findNavController().navigateSafe(CurrentSubscriptionFragmentDirections.actionCurrentSubscriptionFragmentToSubscriptionFragment())
            else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }
        if(viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)){
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCardNew.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true) || (currentPack.eligibleFirestick && currentPack.fsTaken)){
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCardNew.tvEligibility.show()
        } else {
            binding.selectedPackCardNew.tvEligibility.hide()
        }
        currentPack.comboPackChannelsInfo?.channelsBreakdownInfo?.let {
            binding.selectedPackCardNew.recyclerChannels.adapter = ChannelsCountAdapter(it)
        }
    }

    private fun subscriptionExpiredModeNew(currentPack: PartnerPacks) {
        binding.gpActiveSubscription.hide()
        binding.gpActiveSubscriptionNew.show()
        binding.selectedPackCardNew.tvTag.hide()
        viewModel.selectedPack = currentPack
        binding.selectedPack = currentPack
        if(currentPack.isCancelled){
            binding.selectedPackCardNew.tvTag.text = CANCELLED
            binding.selectedPackCardNew.tvTag.show()
        }
        if (true == currentPack.modifyBtn) {
            binding.selectedPackCardNew.btnModifySubscription.show()
        } else {
            binding.selectedPackCardNew.btnModifySubscription.hide()
        }
        if (true == currentPack.cancelBtn && !currentPack.isCancelled) {
            binding.btnCancelSubscription.show()
        } else {
            binding.btnCancelSubscription.hide()
        }
        binding.gpNoSubscription.hide()
        val color = ContextCompat.getColor(requireContext(), R.color.darkError)
        binding.selectedPackCardNew.tvRenewalCycle.setTextColor(color)
        binding.selectedPackCardNew.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCardNew.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCardNew.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank())
                findNavController().navigateSafe(CurrentSubscriptionFragmentDirections.actionCurrentSubscriptionFragmentToSubscriptionFragment())
            else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }
        if (viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCardNew.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true)) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCardNew.tvEligibility.show()
        } else {
            binding.selectedPackCardNew.tvEligibility.hide()
        }
        currentPack.comboPackChannelsInfo?.channelsBreakdownInfo?.let {
            binding.selectedPackCardNew.recyclerChannels.adapter = ChannelsCountAdapter(it)
        }
    }

    private fun subscriptionCancelledModeNew(currentPack: PartnerPacks) {
        binding.gpActiveSubscription.hide()
        binding.gpActiveSubscriptionNew.show()
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        binding.selectedPackCardNew.tvTag.hide()
        binding.btnCancelSubscription.hide()
        if (currentPack.isInactive) {
            val color = ContextCompat.getColor(requireContext(), R.color.darkError)
            binding.selectedPackCardNew.tvRenewalCycle.setTextColor(color)
        }
        binding.selectedPackCardNew.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCardNew.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        if (true == currentPack.cancelBtn) {
            binding.selectedPackCardNew.btnModifySubscription.show()
        } else {
            binding.selectedPackCardNew.btnModifySubscription.hide()
        }
        binding.selectedPackCardNew.btnModifySubscription.setOnClickListener {
            beginRevokeProcess()
        }

        if(viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)){
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCardNew.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true) || (currentPack.eligibleFirestick && currentPack.fsTaken)){
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCardNew.tvEligibility.show()
        } else {
            binding.selectedPackCardNew.tvEligibility.hide()
        }
        currentPack.comboPackChannelsInfo?.channelsBreakdownInfo?.let {
            binding.selectedPackCardNew.recyclerChannels.adapter = ChannelsCountAdapter(it)
        }
    }
}