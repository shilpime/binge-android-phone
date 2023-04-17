package com.tatasky.binge.ui.features.subscription_freemium.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentAddonCancellationBottomSheetBinding
import com.tatasky.binge.databinding.FragmentAddonCancellationBottomSheetBindingImpl
import com.tatasky.binge.databinding.FragmentCancellationConfirmationBottomSheetBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.base.BaseBottomSheetDialogFragment
import com.tatasky.binge.ui.base.frameworks.extensions.disable
import com.tatasky.binge.ui.base.frameworks.extensions.enable
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.subscription.model.Cancellation
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.BindingAdapters.Companion.setHtmlText
import com.tatasky.binge.utils.PROVIDER_PRIME
import com.tatasky.binge.utils.expandBottomSheet
import com.tatasky.binge.utils.updateProviderSeeAll
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_addon_cancellation_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_update_user_details.*
import javax.inject.Inject

class AddOnCancellationBottomSheet : BaseBottomSheetDialogFragment<FragmentAddonCancellationBottomSheetBinding,FreemiumSubscriptionViewModel>() {

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }


    private fun setListeners(mCurrentPack : PartnerPacks) {
        binding.addOn1.root.setOnClickListener {
            binding.addOn1.rbAddOn.isChecked = !binding.addOn1.rbAddOn.isChecked
        }
        binding.addOn2.root.setOnClickListener {
            binding.addOn2.rbAddOn.isChecked = !binding.addOn2.rbAddOn.isChecked
        }
        binding.addOn2.rbAddOn.setOnCheckedChangeListener { _, isChecked ->
            binding.btnProceed.isEnabled =
                isChecked || binding.addOn1.rbAddOn.isChecked
        }
        binding.addOn1.rbAddOn.setOnCheckedChangeListener { _, isChecked ->
            if(mCurrentPack.primeAutoSelect == true) {
                binding.addOn2.rbAddOn.isChecked = isChecked
                binding.addOn2.root.isClickable = !isChecked
            }
            binding.btnProceed.isEnabled =
                isChecked || binding.addOn2.rbAddOn.isChecked
        }
        binding.btnLater.setOnClickListener {
            dismiss()
        }
        binding.btnProceed.setOnClickListener {
            viewModel.requestSubscriptionCancellation(Cancellation(binding.addOn1.rbAddOn.isChecked, binding.addOn2.rbAddOn.isChecked))
            dismiss()
        }
    }


    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> {
        return FreemiumSubscriptionViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_addon_cancellation_bottom_sheet
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return activity ?:findNavController().getViewModelStoreOwner(R.id.freemium_subscription)
    }

    override fun setObserver() {
    }

    override fun toBeCalledOnce() {
        expandBottomSheet(dialog)
        binding.pack = sharedPrefs.getSubscribedPack()

        val pack = sharedPrefs.getSubscribedPack()
        binding.addOn1.tvItemName.text = pack?.productName
        binding.addOn1.tvPrice.setHtmlText(pack?.amount)
        binding.addOn2.tvItemName.text = pack?.primePackDetails?.title
        binding.addOn2.tvPrice.text = String.format(
            getString(R.string.rupees_cyclic),
            "${pack?.primePackDetails?.getFormattedPrice()}",
            pack?.primePackDetails?.renewalCycle
        )


        val primePack = pack?.primePackDetails!!
        if (primePack.imageUrl.isNullOrBlank())
            updateProviderSeeAll(
                binding.addOn2.ivItemImage,
                PROVIDER_PRIME,
                sharedPrefs.getProviderLogo(),
                sharedPrefs.getCloudenieryUrl()
            )
        else
            transparentImageLoad(binding.addOn2.ivItemImage, primePack.imageUrl ?: "")


        setListeners(pack)
    }
}