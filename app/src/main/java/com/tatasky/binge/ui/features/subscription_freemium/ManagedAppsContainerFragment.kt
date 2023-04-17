package com.tatasky.binge.ui.features.subscription_freemium

import android.content.Context
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentManagedAppContainerBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import dagger.android.support.AndroidSupportInjection

class ManagedAppsContainerFragment : BaseFragment<FragmentManagedAppContainerBinding,FreemiumSubscriptionViewModel>() {

    override fun layoutId(): Int = R.layout.fragment_managed_app_container


    override fun getViewModelOwner(): ViewModelStoreOwner = requireActivity()


    override fun setObserver() {

    }

    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> {
      return  FreemiumSubscriptionViewModel::class.java
    }

    override fun toBeCalledOnce() {
        val localNavHost = childFragmentManager.findFragmentById(R.id.container_starter_subscription) as NavHostFragment
        val sourceArg = NavArgument.Builder().setDefaultValue("").build()
        val navController: NavController = localNavHost.navController
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_freemium_subscription_managed_apps)
        navGraph.addArgument("source", sourceArg)
        navController.graph = navGraph
//        if()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}