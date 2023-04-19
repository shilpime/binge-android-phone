package com.tatasky.binge.helper

import com.tatasky.binge.analytics.MYPLAN_REGIONAL
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.utils.CustomSnackbarWithTwoActionsType
import com.tatasky.binge.utils.CustomSnackbarWithTwoActionsUtil
import com.tatasky.binge.utils.NudgeUtils

object NudgeHelper {
    private val customSnackbarRegionalApp = CustomSnackbarWithTwoActionsUtil()

    fun BaseActivity<*>.showRegionalAppNudge(
        viewModel: CommonSampleViewModel,
        isManagedAppOpen: Boolean
    ) {
        (this as? LandingActivity)?.apply {
            if (!NudgeUtils.shouldShowRegionalAppNudge(viewModel, isManagedAppOpen)) return
            customSnackbarRegionalApp.hideCustomSnackbarWithTwoActions() //Hide any previous nudge
            viewModel.sharedPrefs.saveLoggedInAppLaunchCountForRegionalAppNudge(0) //Reset the count
            val regionalAppNudgeData = viewModel.sharedPrefs.getSubscribedPack()?.regionalAppNudge
            customSnackbarRegionalApp.showCustomSnackbarWithTwoActions(
                context = this,
                snackbarType = CustomSnackbarWithTwoActionsType.SnackbarTypeRegionalApps,
                mszTitle = regionalAppNudgeData?.regionalAppVerbiage ?: "",
                mszDesc = "",
                imgResourceSmall = null,
                imgResourceLarge = null,
                btnActionText = regionalAppNudgeData?.regionalAppCTA ?: "",
                maxProgress = 0,
                currProgress = 0,
                lambdaAction = {
                    customSnackbarRegionalApp.hideCustomSnackbarWithTwoActions()
                    showMiniDrawer(
                        "",
                        skipDrawer = true,
                        journeyRef = MYPLAN_REGIONAL
                    )
                },
                lambdaCancel = {
                    customSnackbarRegionalApp.hideCustomSnackbarWithTwoActions()
                }
            )
        }
    }
}