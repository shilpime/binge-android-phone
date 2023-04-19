package com.tatasky.binge.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.tatasky.binge.customviews.CustomSnackbarWithTwoActions
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity

sealed class CustomSnackbarWithTwoActionsType {
    object SnackbarTypeNormalSizeImage : CustomSnackbarWithTwoActionsType()
    object SnackbarTypeLargeSizeImage : CustomSnackbarWithTwoActionsType()
    object SnackbarTypeSubsRenewal : CustomSnackbarWithTwoActionsType()
    object SnackbarTypeSubsExpired : CustomSnackbarWithTwoActionsType()
    object SnackbarTypeGameNudge : CustomSnackbarWithTwoActionsType()
    object SnackbarTypeRegionalApps : CustomSnackbarWithTwoActionsType()
}

/**
 * Use this to display snackbars for Renewal Nudges
 */
class CustomSnackbarWithTwoActionsUtil {
    var isShown: Boolean = false
        private set

    var snackBarType: CustomSnackbarWithTwoActionsType? = null
        private set

    private var snackbar: CustomSnackbarWithTwoActions? = null

    fun showCustomSnackbarWithTwoActions(
        context: Context?,
        snackbarType: CustomSnackbarWithTwoActionsType,
        mszTitle: String,
        mszDesc: String,
        imgResourceSmall: Int? = null,
        imgResourceLarge: Int? = null,
        imgResourceCancel: Int? = null,
        btnActionText: String,
        maxProgress: Int,
        currProgress: Int,
        lambdaAction: (() -> Unit)? = null,
        lambdaCancel: (() -> Unit)? = null,
    ) {
        context?.let {
            (it as BaseActivity<*>).window.decorView.findViewById<View>(android.R.id.content)
                ?.let { rootView ->
                    when (snackbarType) {
                        CustomSnackbarWithTwoActionsType.SnackbarTypeSubsExpired,
                        CustomSnackbarWithTwoActionsType.SnackbarTypeSubsRenewal ->
                            snackbar = CustomSnackbarWithTwoActions.makeNewStyleNudge(
                                rootView as ViewGroup,
                                snackbarType,
                                mszTitle,
                                mszDesc,
                                imgResourceSmall,
                                imgResourceCancel,
                                btnActionText,
                                maxProgress,
                                currProgress,
                                lambdaAction,
                                lambdaCancel,
                            )
                        else -> snackbar = CustomSnackbarWithTwoActions.make(
                            rootView as ViewGroup,
                            snackbarType,
                            mszTitle,
                            mszDesc,
                            imgResourceSmall,
                            imgResourceLarge,
                            imgResourceCancel,
                            btnActionText,
                            maxProgress,
                            currProgress,
                            lambdaAction,
                            lambdaCancel,
                            it as BaseActivity<*>
                        )
                    }
                    snackbar?.apply {
                        duration = Snackbar.LENGTH_INDEFINITE
                    }
                    isShown = true
                    snackBarType = snackbarType
                    snackbar?.show()
                }
        }
    }

    fun hideCustomSnackbarWithTwoActions() {
        isShown = false
        snackbar?.dismiss()
    }
}