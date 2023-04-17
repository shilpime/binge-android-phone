/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tatasky.binge.utils

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.home.sub.SubFragment

/**
 * Manages the various graphs needed for a [
 *
 * ].
 *
 * This sample is a workaround until the Navigation Component supports multiple back stacks.
 */
fun BottomNavigationView.setupWithNavController(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    intent: Intent,
    reselectedFunction: () -> Unit,
    homeSelectedFunction: () -> Unit,
    sharedPrefs: PrefsRepo,
    interruptSelectedBottomTab: (source: String, selectedTabId: Int) -> Unit
): Pair<LiveData<NavController>, LiveData<Int>> {

    var backPressed = false
    // Map of tags
    val graphIdToTagMap = LinkedHashMap<Int, String>()
    // Result. Mutable live data with the selected controlled
    val selectedNavController = MutableLiveData<NavController>()
    val selectedMenuItemId = MutableLiveData<Int>()

    var firstFragmentGraphId = 0

    // First create a NavHostFragment for each NavGraph ID
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId,
            intent
        )

        // Obtain its id
        val graphId = navHostFragment.navController.graph.id

        if (index == 0) {
            firstFragmentGraphId = graphId
        }

        // Save to the map
        graphIdToTagMap[graphId] = fragmentTag

        // Attach or detach nav host fragment depending on whether it's the selected item.
        if (this.selectedItemId == graphId) {
            // Update livedata with the selected graph
            selectedNavController.value = navHostFragment.navController
            attachNavHostFragment(fragmentManager, navHostFragment, index == 0)
        } else {
            detachNavHostFragment(fragmentManager, navHostFragment)
        }
        selectedMenuItemId.value = this.selectedItemId
    }

    // Now connect selecting an item with swapping Fragments
    var selectedItemTag = graphIdToTagMap[this.selectedItemId]
    val firstFragmentTag = graphIdToTagMap[firstFragmentGraphId]
    var isOnFirstFragment = selectedItemTag == firstFragmentTag

    // When a navigation item is selected
    setOnNavigationItemSelectedListener { item ->
//        if (sharedPrefs.isParentalControlEnabled() && item.itemId != R.id.kids) {
//            //if kids mode is enabled and user navigates to menu item other than kids then interrupt the interaction
//            interruptSelectedBottomTab.invoke(
//                InterruptedBottomTabConstants.SOURCE_PARENTAL_PIN,
//                item.itemId
//            )
//            return@setOnNavigationItemSelectedListener false
//        }

       /* if (sharedPrefs.getInterruptCategoryTabStatus() && item.itemId == R.id.others) {
            interruptSelectedBottomTab.invoke(
                InterruptedBottomTabConstants.SOURCE_CATEGORIES,
                item.itemId
            )
            return@setOnNavigationItemSelectedListener false
        }*/
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val newlySelectedItemTag = graphIdToTagMap[item.itemId]
            if (selectedItemTag != newlySelectedItemTag) {
                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment
                // Pop everything above the first fragment (the "fixed start destination")
                if (item.itemId == R.id.home && !backPressed && selectedFragment.navController.let { it.currentDestination?.id != it.graph.startDestination }) {
                    (parent as ViewGroup).findViewById<View?>(containerId)?.alpha = 0f
                    selectedFragment.childFragmentManager.fragments.forEach {
                        if (it is BaseFragment<*, *>)
                            it.preventSetObserver()
                    }
                } else {
                    (parent as ViewGroup).findViewById<View?>(containerId)?.alpha = 1f
                }
                fragmentManager.popBackStack(
                    firstFragmentTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                ).apply {
                    if (item.itemId == R.id.home && !backPressed && selectedFragment.navController.let { it.currentDestination?.id != it.graph.startDestination }) {
                        try {
                            Handler(Looper.getMainLooper()).post {
                                selectedFragment.childFragmentManager.fragments.forEach {
                                    it.exitTransition = null
                                    it.enterTransition = null
                                    it.reenterTransition = null
                                    it.returnTransition = null
                                    it.sharedElementReturnTransition = null
                                }
                                selectedFragment.navController.popBackStack(selectedFragment.navController.graph.startDestination, false)
                                (parent as ViewGroup).findViewById<View?>(containerId)?.animate()?.alpha(1f)?.setDuration(350)?.setListener(null)?.start()
                            }
                        } catch (e: Exception) {
                        }

                    }
                    if (item.itemId == R.id.home && !backPressed) {
                        try {
                            Handler(Looper.getMainLooper()).post {
                                homeSelectedFunction.invoke()
                            }
                        } catch (e: Exception) {
                        }
                    }
                    backPressed = false
                }

                // Exclude the first fragment tag because it's always in the back stack.
                if (firstFragmentTag != newlySelectedItemTag) {
                    // Commit a transaction that cleans the back stack and adds the first fragment
                    // to it, creating the fixed started destination.
                    fragmentManager.beginTransaction()
//                        .setCustomAnimations(
//                            android.R.anim.fade_in,
//                            android.R.anim.fade_out,
//                            R.anim.nav_default_pop_enter_anim,
//                            R.anim.nav_default_pop_exit_anim
//                        )
                        .attach(selectedFragment)
                        .setPrimaryNavigationFragment(selectedFragment)
                        .apply {
                            // Detach all other Fragments
                            graphIdToTagMap.forEach {
                                if (it.value != newlySelectedItemTag) {
                                    detach(fragmentManager.findFragmentByTag(firstFragmentTag)!!)
                                }
                            }
                        }
                        .addToBackStack(firstFragmentTag)
                        .setReorderingAllowed(true)
                        .commit()
                }
                selectedItemTag = newlySelectedItemTag
                isOnFirstFragment = selectedItemTag == firstFragmentTag

                if (selectedNavController.value?.graph?.id != R.id.home) {
                    fragmentManager.fragments.forEach {
                        it.childFragmentManager.fragments.forEach {
                            it.sharedElementReturnTransition = null
                            if(it !is SubFragment)
                                it.onDestroyView()
                        }
                    }

                    selectedNavController.value?.popBackStack(
                        selectedNavController.value!!.graph.startDestination,
                        false
                    )
                }

                selectedNavController.value = selectedFragment.navController
                selectedMenuItemId.value = item.itemId
                true
            } else {
                false
            }
        }
    }

    // Optional: on item reselected, pop back stack to the destination of the graph
    setupItemReselected(graphIdToTagMap, fragmentManager, reselectedFunction)

    // Handle deep link
    setupDeepLinks(navGraphIds, fragmentManager, containerId, intent)

    // Finally, ensure that we update our BottomNavigationView when the back stack changes
    fragmentManager.addOnBackStackChangedListener {
        firstFragmentTag?.let {
            if (!isOnFirstFragment && !fragmentManager.isOnBackStack(it)) {
                backPressed = true
                this.selectedItemId = firstFragmentGraphId
            }
        }

        // Reset the graph if the currentDestination is not valid (happens when the back
        // stack is popped after using the back button).
        selectedNavController.value?.let { controller ->
            if (controller.currentDestination == null) {
                controller.navigate(controller.graph.id)
            }
        }
    }
    return Pair(selectedNavController, selectedMenuItemId)
}

private fun BottomNavigationView.setupDeepLinks(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    intent: Intent
) {
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId,
            intent
        )
        // Handle Intent
        if (navHostFragment.navController.handleDeepLink(intent)
            && selectedItemId != navHostFragment.navController.graph.id
        ) {
            this.selectedItemId = navHostFragment.navController.graph.id
        }
    }
}

private fun BottomNavigationView.setupItemReselected(
    graphIdToTagMap: LinkedHashMap<Int, String>,
    fragmentManager: FragmentManager,
    reselectedFunction: () -> Unit
) {
    setOnNavigationItemReselectedListener { item ->
        val newlySelectedItemTag = graphIdToTagMap[item.itemId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        fragmentManager.fragments.forEach {
            it.childFragmentManager.fragments.forEach {
                it.sharedElementReturnTransition = null
            }
        }
        val popBackStack = navController.popBackStack(
            navController.graph.startDestination, false
        )
        try {
//            if (!popBackStack) {
            reselectedFunction.invoke()
//            }
        } catch (e: Exception) {

        }
    }
}

private fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryNavFragment: Boolean
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()

}

private fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int,
    intent: Intent
): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }
    var contentItem: ContentItem? = null
    if (navGraphId == R.navigation.nav_home) {
        if (intent.extras != null) {
            val extraData = intent.getStringExtra(KEY_SCREEN_DATA)
            try {
                val fromJson =
                    Gson().fromJson<MoEngageGenericModel>(
                        extraData,
                        MoEngageGenericModel::class.java
                    )
                when (fromJson.screenName) {
                    "detail_screen", KEY_NOTIFICATION_DETAIL -> contentItem = Gson().fromJson(Gson().toJson(fromJson.any), ContentItem::class.java)
                }
            } catch (e: Exception) {
                e("NavExtension", " Intent Parsing Error")
            }
        }
    }
    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(navGraphId, Bundle().apply {
        putParcelable("contentItem", contentItem)
    })
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

private fun getFragmentTag(index: Int) = "bottomNavigation#$index"

// To avoid "java.lang.IllegalArgumentException: navigation destination is unknown to this NavController", se more https://stackoverflow.com/q/51060762/6352712
fun NavController.navigateSafe(
    @IdRes destinationId: Int,
    navDirection: NavDirections,
    callBeforeNavigate: () -> Unit
) {
    if (NetworkUtil.checkInternetBeforeNavigate()) {
        try {
            if (currentDestination?.id == destinationId) {
                callBeforeNavigate()
                navigate(navDirection)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun NavController.navigateSafe(@IdRes destinationId: Int, bundle: Bundle? = null) {
    if (NetworkUtil.checkInternetBeforeNavigate()) {
        try {
            if (currentDestination?.getAction(destinationId) != null) {
                navigate(destinationId, bundle)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun NavController.navigateSafe(@IdRes destinationId: Int, navDirection: NavDirections) {
    if (NetworkUtil.checkInternetBeforeNavigate()) {
        try {
            if (currentDestination?.id == destinationId) {
                navigate(navDirection)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun NavController.navigateSafe(navDirection: NavDirections) {
    if (NetworkUtil.checkInternetBeforeNavigate()) {
        try {
            if (navDirection.actionId == R.id.action_to_detail || navDirection.actionId == R.id.action_to_detail1) {
                if (PROVIDER_HUNGAMA.equals(navDirection.arguments.getParcelable<ContentItem>("contentItem")?.provider, true)) {
                    changeNodeDestination(R.id.nav_details, R.id.hungamaDetailsFragment).navigate(navDirection)
                }
                else if (PROVIDER_EROSNOW.equals(navDirection.arguments.getParcelable<ContentItem>("contentItem")?.provider, true)) {
                    changeNodeDestination(R.id.nav_details, R.id.erosnowDetailsFragment).navigate(navDirection)
                }
                else if(PROVIDER_PRIME.equals(navDirection.arguments.getParcelable<ContentItem>("contentItem")?.provider, true)){
                    changeNodeDestination(R.id.nav_details, R.id.prime_activity).navigate(navDirection)
                }
                else {
                    changeNodeDestination(R.id.nav_details, R.id.ttnDetailsFragment).navigate(navDirection)
                }
            } else {
                navigate(navDirection)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun NavController.changeNodeDestination(nodeId: Int, destinationId: Int): NavController {
    var _graph = graph.findNode(nodeId) as NavGraph?
    if(_graph==null){
        graph.forEach{ navDestination ->
            if(navDestination is NavGraph) {
                _graph = navDestination.findNode(nodeId) as NavGraph?
            }
        }
    }
    _graph?.startDestination = destinationId
    return this
}

fun NavController.navigateSafe(navDirection: NavDirections, navigatorExtras: Navigator.Extras) {
    if (NetworkUtil.checkInternetBeforeNavigate()) {
        try {
            if (navDirection.actionId == R.id.action_to_detail || navDirection.actionId == R.id.action_to_detail1) {
                if (PROVIDER_HUNGAMA.equals(navDirection.arguments.getParcelable<ContentItem>("contentItem")?.provider, true)) {
                    changeNodeDestination(R.id.nav_details, R.id.hungamaDetailsFragment).navigate(navDirection, navigatorExtras)
                }
                else if (PROVIDER_EROSNOW.equals(navDirection.arguments.getParcelable<ContentItem>("contentItem")?.provider, true)) {
                    changeNodeDestination(R.id.nav_details, R.id.erosnowDetailsFragment).navigate(navDirection, navigatorExtras)
                }
                else if(PROVIDER_PRIME.equals(navDirection.arguments.getParcelable<ContentItem>("contentItem")?.provider, true)){
                    changeNodeDestination(R.id.nav_details, R.id.prime_activity).navigate(navDirection, navigatorExtras)
                }
                else {
                    changeNodeDestination(R.id.nav_details, R.id.ttnDetailsFragment).navigate(navDirection, navigatorExtras)
                }
            } else {
                navigate(navDirection, navigatorExtras)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun NavController.navigateSafe(navDirection: NavDirections, navOptions: NavOptions) {
    if (NetworkUtil.checkInternetBeforeNavigate()) {
        try {
            if (navDirection.actionId == R.id.action_to_detail || navDirection.actionId == R.id.action_to_detail1) {
                if (PROVIDER_HUNGAMA.equals(navDirection.arguments.getParcelable<ContentItem>("contentItem")?.provider, true)) {
                    changeNodeDestination(R.id.nav_details, R.id.hungamaDetailsFragment).navigate(navDirection, navOptions)
                }
                else if (PROVIDER_EROSNOW.equals(navDirection.arguments.getParcelable<ContentItem>("contentItem")?.provider, true)) {
                    changeNodeDestination(R.id.nav_details, R.id.erosnowDetailsFragment).navigate(navDirection, navOptions)
                }
                else if(PROVIDER_PRIME.equals(navDirection.arguments.getParcelable<ContentItem>("contentItem")?.provider, true)){
                    changeNodeDestination(R.id.nav_details, R.id.prime_activity).navigate(navDirection, navOptions)
                }
                else {
                    changeNodeDestination(R.id.nav_details, R.id.ttnDetailsFragment).navigate(navDirection, navOptions)
                }
            } else {
                navigate(navDirection, navOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun NavController.navigateUpOrFinish(activity: AppCompatActivity): Boolean {
    return if (navigateUp()) {
        true
    } else {
        activity.finish()
        activity.overridePendingTransition(R.anim.slide_right_out, R.anim.slide_left_in)
        true
    }
}

fun NavController.navigateUpOrOpenHome(activity: AppCompatActivity): Boolean {
    return if (navigateUp()) {
        true
    } else {
        if (activity.isTaskRoot) {
            startHomeScreen(activity)
        } else {
            activity.finish()
            activity.overridePendingTransition(R.anim.slide_right_out, R.anim.slide_left_in)
        }
        true
    }
}