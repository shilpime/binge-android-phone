package com.tatasky.binge.ui.features.home.bottomsheet.welcome


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tatasky.binge.customviews.CustomDialog
import com.tatasky.binge.databinding.FragmentWelcomeDetailsBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.subscription_freemium.adapter.FreemiumProviderAdapter
import com.tatasky.binge.utils.getDisplayMatics
import javax.inject.Inject


class WelcomeMessageDialog : CustomDialog() {
    private lateinit var binding: FragmentWelcomeDetailsBinding

    @Inject
    lateinit var sharedPrefs: PrefsRepo
    override fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentWelcomeDetailsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        isCancelable = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            dismiss()
        }
        setUiElements()
    }

    fun setUiElements() {
        var cardWidth = 0
        sharedPrefs.getSubscribedPack()?.let {
            if (it?.getSelectedComponentAppList?.size ?: 0 != 0) {
                cardWidth = getDisplayMatics().widthPixels
            }

            binding.selectedPack = it
            it.fibreDetails?.let {fiberDetail->
                binding.fiberTvMore.text = fiberDetail.fiberMoreAppsVerbiage
                binding.cyopTvValid.text = fiberDetail.fiberPackValidity
                fiberDetail.logo?.let { it1 -> transparentImageLoad(binding.ivFiberLogo, it1) }

            }

            it.getSelectedComponentAppList.forEach {
                if(it.fibrePosition.isNullOrEmpty())
                    it.fibrePosition="100"
            }

            val sortedList=it.getSelectedComponentAppList.sortedWith(compareBy {

                it.fibrePosition?.toInt()
            }).toMutableList()

            binding.rvFiberApps.adapter =
                FreemiumProviderAdapter(
                    sortedList.take(10), cardWidth,fiberDialog=true)

        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnStartWatching.setOnClickListener {
            (activity as? LandingActivity)?.let {
                it.fiberDialogShowing=false
                if(it.showFsAfterFiber){
                    it.showFirestickDialogOnUI()
                }
            }
            dismiss()
        }
    }
}