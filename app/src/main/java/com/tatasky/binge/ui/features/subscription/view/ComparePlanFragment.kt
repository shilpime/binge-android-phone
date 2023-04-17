package com.tatasky.binge.ui.features.subscription.view

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.EligiblePackResponse
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentComparePlansBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription.adapter.ComparePlanAdapter
import com.tatasky.binge.ui.features.subscription_freemium.FreemiumSubscriptionActivity
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.navigateSafe

class ComparePlanFragment : BaseFragment<FragmentComparePlansBinding, FreemiumSubscriptionViewModel>() {
    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> =
        FreemiumSubscriptionViewModel::class.java


    override fun layoutId(): Int = R.layout.fragment_compare_plans


    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.freemium_subscription)


    override fun setObserver() {
        viewModel.getEligiblePacksResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let{
                binding.btnProceed.show()
                binding.groupComparePackListing.show()
                setAdapter(it)
            }
        })
    }

    private fun setAdapter(it: EligiblePackResponse) {
        val packList = mutableListOf<PartnerPacks>()
        it.data?.forEach {it1 ->
            it1.componentList.forEach {  it2 ->
                if(it2.componentName.equals(it1.productId,true)){
                    packList.add(it1)
                }
            }
        }
        it.data?.let { data ->
            val mappingListChild = data[0]
            val mappingList = mutableListOf<PartnerPacks>()

            data.forEach {
                mappingList.add(mappingListChild)
            }
            val comparePlanAdapter = ComparePlanAdapter(subscriptionAnalytics = viewModel.subscriptionAnalytics,mainList = data)
            (binding.comparePlanRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            binding.comparePlanRecycler.adapter = comparePlanAdapter
            binding.comparePlanHeaderRecycler.adapter = ComparePlanAdapter(true,subscriptionAnalytics = viewModel.subscriptionAnalytics,mainList = data.subList(0,1))
            setTnc(it)
        }
    }

    private fun setTnc(packList: EligiblePackResponse) {
        val x = packList.data?.get(packList.data!!.size - 1)
        if(x != null) {
            binding.tvComparePlanTnc.text =
                (x.sunnextFooterMessage ?: "") + "\n" + "\n" + (x.comparePlanFooterMessage ?: "")
        }
    }

    override fun toBeCalledOnce() {
        viewModel.subscriptionAnalytics.trackComparePlanViews()
        viewModel.setMockResponse(readRawFile())
        viewModel.fetchEligiblePackList()
        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.btnProceed.setOnClickListener {
            (binding.comparePlanRecycler.adapter as ComparePlanAdapter).getSelected()?.let { it1 ->
                ComparePlanFragmentDirections.actionComparePlanFragmentToTenureBottomSheetDialog(
                    it1
                )
            }?.let { it2 -> findNavController().navigateSafe(it2) }
        }
        imageLoad(binding.compareBackground,
            sharedPrefs.getConfigResponse()?.data?.config?.freemiumBackgroundPoster?.androidSubscriptionBackgroundPoster?.otherPackPoster
                ?: ""
        )
    }

    private fun readRawFile(): EligiblePackResponse {
        val objectArrayString: String =
            requireContext().resources.openRawResource(R.raw.eligible_pack).bufferedReader()
                .use { it.readText() }
        return Gson().fromJson(objectArrayString, EligiblePackResponse::class.java)
    }
}