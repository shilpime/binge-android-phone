package com.tatasky.binge.ui.features.subscription_freemium.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tatasky.binge.R
import com.tatasky.binge.analytics.MYPLAN_CHANGE
import com.tatasky.binge.analytics.MYPLAN_TENURE
import com.tatasky.binge.databinding.FragmentMyplanOtherOptionsBottomSheetBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.ManagedAppViewModel
import com.tatasky.binge.utils.PaymentUtility.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers
import com.tatasky.binge.utils.SERVER_DATE_TIME_FORMAT
import com.tatasky.binge.utils.getCurrentDateInFormat
import com.tatasky.binge.utils.getDifferenceBetweenTwoDates
import com.tatasky.binge.utils.navigateSafe
import dagger.android.support.AndroidSupportInjection

import javax.inject.Inject

class MyPlanOtherOptionsBottomSheet: BottomSheetDialogFragment() {

    private lateinit var managedAppViewModel: ManagedAppViewModel

    private var currentJourneyRef : String =""

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sharedPrefs: PrefsRepo
    lateinit var mBinding: FragmentMyplanOtherOptionsBottomSheetBinding
    private lateinit var mSubscriptionViewModel: FreemiumSubscriptionViewModel
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        managedAppViewModel =
            ViewModelProvider(
                requireActivity(),
                mViewModelFactory
            )[ManagedAppViewModel::class.java]
        managedAppViewModel.checkForManagedAppEligibility { eligible ->
        }
        setObservers()
        setListeners()
    }

    private fun setListeners() {
        mBinding.cardChangePlan.setOnClickListener {
            this.dismiss()
            mSubscriptionViewModel.subscriptionAnalytics.trackMyPlanRenewChangePlan(
                sharedPrefs.getSubscribedPack()?.productName ?: "",
                if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                else getDifferenceBetweenTwoDates(
                    sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                    getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                    SERVER_DATE_TIME_FORMAT
                ),
                mSubscriptionViewModel.getCurrentSubscription()
                    ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            if (sharedPrefs.isManagedAppEnabled()) {
                currentJourneyRef = MYPLAN_CHANGE
                managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef, "")
            } else {
                findNavController().navigateSafe(MyPlanOtherOptionsBottomSheetDirections.actionMyPlanOtherOptionsBottomSheetToFreemiumSubscriptionFragment())
            }
        }
        mBinding.cardChangeTenure.setOnClickListener {
            mSubscriptionViewModel.subscriptionAnalytics.trackMyPlanRenewChangeTenure(
                sharedPrefs.getSubscribedPack()?.productName ?: "",
                if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                else getDifferenceBetweenTwoDates(
                    sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                    getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                    SERVER_DATE_TIME_FORMAT
                ),
                mSubscriptionViewModel.getCurrentSubscription()
                    ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            if (sharedPrefs.getSubscribedPack() != null) {
                this.dismiss()
                if (sharedPrefs.isManagedAppEnabled()) {
                    currentJourneyRef = MYPLAN_TENURE
                    managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef, "")

                } else {
                    findNavController().navigateSafe(
                        MyPlanOtherOptionsBottomSheetDirections.actionMyPlanOtherOptionsBottomSheetToManagedAppFragment(
                            MYPLAN_TENURE
                        )
                    )
                }

            }
        }
    }

    private fun setObservers() {
        managedAppViewModel.getManagedAppResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { managedAppResponse ->
                managedAppResponse.data?.href?.let {


                    findNavController().navigateSafe(
                        MyPlanOtherOptionsBottomSheetDirections.actionMyPlanOtherOptionsBottomSheetToManagedAppFragment(
                            journeySource =  currentJourneyRef,
                            accessToken = managedAppResponse.data?.accessToken?:"",
                            pageUrl = it
                        )
                    )
                }
            }
        })

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            FragmentMyplanOtherOptionsBottomSheetBinding.inflate(inflater, container, false)
        mBinding.lifecycleOwner = viewLifecycleOwner

        mSubscriptionViewModel = ViewModelProvider(
            requireActivity(),
            mViewModelFactory
        )[FreemiumSubscriptionViewModel::class.java]
        val currentPack = sharedPrefs.getSubscribedPack()
        if (currentPack?.planCTADetails?.changePlanOption == true) {
            mBinding.cardChangePlan.show()
        } else {
            mBinding.cardChangePlan.hide()
        }
        if (currentPack?.planCTADetails?.changeTenureOption == true) {
            mBinding.cardChangeTenure.show()
        } else {
            mBinding.cardChangeTenure.hide()
        }
        mBinding.selectedPack = currentPack
        return mBinding.root
    }

}