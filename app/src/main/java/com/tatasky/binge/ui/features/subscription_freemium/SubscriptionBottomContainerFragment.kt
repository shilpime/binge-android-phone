package com.tatasky.binge.ui.features.subscription_freemium

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentSubscriptionBottomContainerBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseBottomSheetDialogFragment
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.KEY_FROM_SCREEN
import dagger.android.support.AndroidSupportInjection


class SubscriptionBottomContainerFragment :
    BaseBottomSheetDialogFragment<FragmentSubscriptionBottomContainerBinding, FreemiumSubscriptionViewModel>() {
    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> =
        FreemiumSubscriptionViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_subscription_bottom_container


    override fun getViewModelOwner(): ViewModelStoreOwner = requireActivity()


    fun newInstance(source: String?): SubscriptionBottomContainerFragment {
        val subscriptionBottomContainerFragment = SubscriptionBottomContainerFragment()
        val bundle = Bundle(1)
        bundle.putString(KEY_FROM_SCREEN, source)
        subscriptionBottomContainerFragment.setArguments(bundle)
        return subscriptionBottomContainerFragment
    }

    override fun setObserver() {

    }

    override fun toBeCalledOnce() {

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        val localNavHost = childFragmentManager.findFragmentById(R.id.container_starter_subscription) as NavHostFragment
        val sourceArg = NavArgument.Builder().setDefaultValue(arguments?.getString(KEY_FROM_SCREEN)).build()
        val navController: NavController = localNavHost.navController
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_freemium_subscription_bottomsheet)
        navGraph.addArgument("source", sourceArg)
        navController.graph = navGraph


    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun getTheme(): Int {
        return R.style.GuestLoginBottomSheetDialogTheme
    }


}