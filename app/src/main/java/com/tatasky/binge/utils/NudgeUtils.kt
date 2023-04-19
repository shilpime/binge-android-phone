package com.tatasky.binge.utils

import com.tatasky.binge.ui.features.common.CommonSampleViewModel

object NudgeUtils {
    fun shouldShowRegionalAppNudge(
        viewModel: CommonSampleViewModel,
        isManagedAppOpen: Boolean
    ): Boolean {
        if (!viewModel.isLoggedIn()) return false
        if (!viewModel.sharedPrefs.isManagedAppEnabled()) return false

        val isRegionalAppNudgeEnabled =
            viewModel.sharedPrefs.getSubscribedPack()?.regionalAppNudge?.enableRegionalAppNudge == true
        if (!isManagedAppOpen && isRegionalAppNudgeEnabled) {
            val regionalAppsNudgeFrequency =
                viewModel.sharedPrefs.getConfigResponse()?.data?.config?.regionalAppsNudgeFrequency
            val lastLoggedInAppLaunchCount =
                viewModel.sharedPrefs.getLoggedInAppLaunchCountForRegionalAppNudge()
            return regionalAppsNudgeFrequency == lastLoggedInAppLaunchCount
        }
        return false
    }
}