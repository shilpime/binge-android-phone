package com.tatasky.binge.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.HomeDirections
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_DEEPLINK
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.analytics.util.getDeeplinkContentAnalyticsModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.data.networking.models.response.SubscriberProfileListModel
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.base.frameworks.extensions.takeIfNotBlankOrNull
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.sub.SubFragmentDirections
import com.tatasky.binge.utils.*

object DeeplinkHelper {

    private val TAG = DeeplinkHelper::class.java.simpleName

    /**
     * Using Navigation app links for internal redirection.
     * Exposed Custom URI and App links both.
     * Only Custom URI host and scheme will be converted to App links host.
     * @param customUriString
     */
    fun createApplinkFromCustomDeeplinkURI(
        customUriString: String?/*Pass Uri in String format*/
    ): Pair<Uri?, Bundle>? =
        customUriString?.takeIf { it.startsWith(CUSTOM_URI_SCHEME_WITH_HOST) }
            ?.replace(CUSTOM_URI_SCHEME_WITH_HOST, "$APPLINK_SCHEME$APPLINK_HOST/")?.let {
                Pair(
                    Uri.parse(it),
                    Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }

    interface DeeplinkHandler {
        fun handleCustomDeeplink(
            activity: LandingActivity,
            localIntent: Intent?,
            viewModel: CommonSampleViewModel,
            navController: LiveData<NavController>?,
            deeplinkActionLambda: ((String?) -> Unit)? = null
        )
    }

    class DeeplinkHandlerImpl : DeeplinkHandler {
        override fun handleCustomDeeplink(
            activity: LandingActivity,
            localIntent: Intent?,
            viewModel: CommonSampleViewModel,
            navController: LiveData<NavController>?,
            deeplinkActionLambda: ((String?) -> Unit)?
        ) {
            activity.apply {
                localIntent?.let {
                    intent = it
                }
                d(TAG, "handleCustomDeeplink: ${intent.data}")
                if (intent.data == null) return@apply
                when (intent.data?.path) {
                    "/$KEY_MY_SUBSCRIPTION" -> {
                        var journeySourceRefId: String? = null
                        val subscriptionAction = if (sharedPrefs.isManagedAppEnabled()) {
                            /**
                             * Accepting encoded Uri for Managed app use case
                             * After decoding replacing the # with _
                             * as # (fragment) separates the Uri/Url part so won't be able to fetch
                             * the proper query parameter
                             */
                            val decodedAcceptedUri =
                                Uri.decode(intent.dataString).replace("#", "_").toUri()
                            journeySourceRefId = decodedAcceptedUri.getQueryParameter(
                                KEY_JOURNEY_SOURCE_REFID
                            )
                            decodedAcceptedUri.getQueryParameter(ACTION)?.takeIf { it.isNotBlank() }?.replace("_", "#")
                        }
                        else
                            intent.data?.getQueryParameter(ACTION)?.takeIf { it.isNotBlank() }
                        if (!viewModel.isLoggedIn() && (subscriptionAction == null ||
                                    subscriptionAction == DeeplinkSubscriptionActions.ACTION_RENEW.action ||
                                    subscriptionAction == DeeplinkSubscriptionActions.ACTION_MANAGED_APP_REGIONAL_APP_SELECTION.action) &&
                            intent.extras?.getString(KEY_SCREEN_NAME) != KEY_NOTIFICATION_MANAGE_PACK
                        ) {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                            return
                        }
                        if (sharedPrefs.getEligibleForFreeTrial()) return //Eligible for Free trial, block deep linking
                        val currentPack = sharedPrefs.getSubscribedPack()
                        // Checking change plan eligibility for logged in user as guest user can have plan selection journey as well
                        val startPackListing =
                            (subscriptionAction == DeeplinkSubscriptionActions.ACTION_PACK_SELECTION.action ||
                                    subscriptionAction == DeeplinkSubscriptionActions.ACTION_MANAGED_APP_PACK_SELECTION.action ||
                                    subscriptionAction == DeeplinkSubscriptionActions.ACTION_MANAGED_APP_VIEW_PACK.action ||
                                    subscriptionAction == DeeplinkSubscriptionActions.ACTION_MANAGED_APP_REGIONAL_APP_SELECTION.action ||
                                    subscriptionAction == DeeplinkSubscriptionActions.ACTION_MANAGED_APP_PROVIDER_PACK.action &&
                                    currentPack?.planCTADetails?.changePlanOption == true)
                        /**
                         * If parameter value is null or unhandled,
                         * by default user redirected to My plan or as per the rule
                         * else to the screen for which action is configured
                         */
                        startActivity(
                            getSubscriptionActivityIntent(
                                this,
                                selectedAppId = null,
                                fromScreen = getSourceOrFromScreenName(),
                                packName =
                                if (intent.data?.getQueryParameter(ACTION) == DeeplinkSubscriptionActions.ACTION_PACK_SELECTION.action)
                                    intent.data?.getQueryParameter(KEY_PACK_NAME).takeIfNotBlankOrNull()
                                else
                                    null,
                                startPackListing = startPackListing,
                                journeySource =
                                if (sharedPrefs.isManagedAppEnabled())
                                    getSubscriptionDeeplinkActionAfterEligibilityCheck(
                                        currentPack,
                                        subscriptionAction,
                                        viewModel.isLoggedIn()
                                    )
                                else
                                    subscriptionAction,
                                journeySourceRefId = journeySourceRefId.takeIfNotBlankOrNull()
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                    }
                    "/$KEY_RECHARGE" -> {
                        if (viewModel.isLoggedIn())
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_CHECK_DTH_STATUS_AND_RECHARGE)
                        else if (intent.extras?.getString(KEY_SCREEN_NAME) != KEY_NOTIFICATION_MANAGE_PACK) {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_BINGE_LIST" -> {
                        if (viewModel.isLoggedIn()) {
                            navController?.value?.navigate(
                                R.id.action_global_watchlist,
                                intent?.extras
                            )
                        } else {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_CATEGORIES" -> showCategoryBottomSheet()
                    "/$KEY_SEARCH" -> navController?.value?.navigateSafe(
                        R.id.action_global_search,
                        intent?.extras
                    )
                    "/$KEY_PRIME" -> if (!viewModel.isLoggedIn()) {
                        deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                        viewModel.getPreviouslyUsedMobileNumbers()
                    } else {
                        handlePrimeInterstitialDeepLinkWithLoggedInUser(
                            viewModel.sharedPrefs,
                            intent,
                            this,
                            navController?.value,
                            viewModel.isDTHUser()
                        )
                    }
                    "/$KEY_PARENTAL_CONTROL" -> {
                        if (viewModel.isLoggedIn())
                            navController?.value?.navigateSafe(
                                R.id.action_global_parentalPinMenuFragment,
                                intent?.extras
                            )
                        else {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_CONTENT_LANGUAGE" -> {
                        if (viewModel.isLoggedIn()) {
                            val verbiage =
                                sharedPrefs.getConfigResponse()?.data?.config?.getLanguageVerbiage(
                                    CATEGORY_LANGUAGE_SETTING
                                )
                            val contentLangTitle =
                                verbiage?.data?.header ?: getString(R.string.content_language)
                            navController?.value?.navigate(
                                R.id.contentLanguage,
                                Bundle().apply {
                                    putString(KEY_TITLE, contentLangTitle)
                                    intent?.extras
                                }
                            ) /*Using Fragment/DestinationId directly so using inbuilt Nav function to navigate*/
                        }
                        else {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_TRANSACTION_HISTORY" -> {
                        if (viewModel.isLoggedIn())
                            navController?.value?.navigate(
                                R.id.transactionHistoryFragment,
                                intent?.extras
                            ) /*Using Fragment/DestinationId directly so using inbuilt Nav function to navigate*/
                        else {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_DEVICE_MANAGEMENT" -> {
                        if (viewModel.isLoggedIn())
                            navController?.value?.navigate(
                                R.id.deviceListFragment,
                                intent?.extras
                            ) /*Using Fragment/DestinationId directly so using inbuilt Nav function to navigate*/
                        else {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_EDIT_PROFILE" -> {
                        if (viewModel.isLoggedIn())
                            getEditProfileDataAndNavigate(viewModel, navController)
                        else {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_SETTING" -> {
                        if (viewModel.isLoggedIn())
                            navController?.value?.navigateSafe(
                                R.id.action_global_settingsFragment,
                                intent?.extras
                            ) /*Using Fragment/DestinationId directly so using inbuilt Nav function to navigate*/
                        else {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_LOGIN" -> {
                        if (!viewModel.isLoggedIn()) {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_NOTIFICATION" -> {
                        if (viewModel.isLoggedIn()) {
                            navController?.value?.navigateSafe(
                                R.id.action_global_notifications,
                                intent?.extras
                            )
                        }
                        else {
                            deeplinkActionLambda?.invoke(DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER)
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    "/$KEY_HELP_CENTER" -> {
                        navController?.value?.navigateSafe(R.id.action_global_help_and_faq)
                    }
                    else -> localIntent?.data?.let {
                        /**
                         * Currently supporting only Google and FB DDL
                         * As for them, getting the DDL data late on the [LandingActivity]
                         * So not able to utilise built in Nav component app link handling
                         * @see LandingActivity.setObservers
                         */
                        when(it.path) {
                            "/$KEY_MOVIES" -> setSelectedTab(R.id.movies)
                            "/$KEY_SHOWS" -> setSelectedTab(R.id.shows)
                            "/$KEY_SPORTS" -> setSelectedTab(R.id.sports)
                            "/$KEY_GAMES" -> setSelectedTab(R.id.gametab)
                            else -> navController?.value?.handleDeepLink(intent)
                        }
                    }
                }
            }
        }

        private fun getEditProfileDataAndNavigate(
            viewModel: CommonSampleViewModel,
            navController: LiveData<NavController>?
        ) {
            val savedUserData = viewModel.sharedPrefs.getSelectedProfile()
            val userData = SubscriberProfileListModel.Data()
            userData.email = savedUserData?.emailId
            userData.firstName = savedUserData?.firstName
            userData.lastName = savedUserData?.lastName
            userData.rmn = savedUserData?.rmn
            userData.image = savedUserData?.imageUrl
            navController?.value?.navigateSafe(
                HomeDirections.actionGlobalEditProfileFragment(
                    userData
                )
            )
        }

        private fun handlePrimeInterstitialDeepLinkWithLoggedInUser(
            sharedPrefs: PrefsRepo,
            intent: Intent?,
            context: Context?,
            navController: NavController?,
            isDTHUser: Boolean
        ) {
            val primePackDetails =
                sharedPrefs.getSubscribedPack()?.primePackDetails ?: sharedPrefs.getPrimePackDetails()
            if (intent?.data?.lastPathSegment.equals(
                    KEY_PRIME,
                    true
                ) && isDTHUser /*Only DTH user can take Prime from Balance*/) {
                primePackDetails?.let {
                    //User has already taken prime subscription from Tata Play app
                    context?.let { context ->
                        showToast(
                            context,
                            context.getString(R.string.prime_subscribed_from_tata_play)
                        )
                    }
                } ?: run {
                    //User don't have Prime pack subscription from Tata Play app, Navigate to Prime screen
                    navController?.navigateSafe(
                        SubFragmentDirections.actionToDetail(
                            ContentItem().apply {
                                provider = PROVIDER_PRIME
                                providerContentId = ""
                            },
                            contentAnalyticsModel = getDeeplinkContentAnalyticsModel()
                        ))
                }
            }
        }

        private fun getSubscriptionDeeplinkActionAfterEligibilityCheck(
            currentPack: PartnerPacks?,
            deeplinkAction: String?,
            userLoggedIn: Boolean
        ): String? {
            deeplinkAction?.let { subscriptionAction ->
                if ((currentPack == null || !userLoggedIn) &&
                    subscriptionAction != DeeplinkSubscriptionActions.ACTION_RENEW.action &&
                    subscriptionAction != DeeplinkSubscriptionActions.ACTION_PACK_SELECTION.action &&
                    subscriptionAction != DeeplinkSubscriptionActions.ACTION_MANAGED_APP_REGIONAL_APP_SELECTION.action
                )
                    return subscriptionAction // Allow only Managed app Guest/Unsubscribed use case
                return when (subscriptionAction) {
                    DeeplinkSubscriptionActions.ACTION_MANAGED_APP_REGIONAL_APP_SELECTION.action ->
                        if (currentPack?.regionalAppInfo?.enableRegionalAppCTA == true) subscriptionAction
                        else null
                    DeeplinkSubscriptionActions.ACTION_MANAGED_APP_PACK_SELECTION.action ->
                        if (currentPack?.planCTADetails?.changePlanOption == true) subscriptionAction
                        else null
                    DeeplinkSubscriptionActions.ACTION_MANAGED_APP_VIEW_PACK.action ->
                        if (currentPack?.planCTADetails?.changePlanOption == true) subscriptionAction
                        else null
                    DeeplinkSubscriptionActions.ACTION_MANAGED_APP_PROVIDER_PACK.action ->
                        if (currentPack?.planCTADetails?.changePlanOption == true) subscriptionAction
                        else null
                    DeeplinkSubscriptionActions.ACTION_RENEW.action ->
                        if (currentPack?.planCTADetails?.renewPlanOption == true) subscriptionAction
                        else null
                    DeeplinkSubscriptionActions.ACTION_PACK_SELECTION.action -> null // Blocking the use case
                    else -> subscriptionAction // Allow all other use case which don't require eligibility checks
                }
            }
            return null
        }
    }

    enum class DeeplinkSubscriptionActions(val action: String) {
        //Subscription use cases supported by Deep link/Notification
        ACTION_RENEW("renew"),
        ACTION_PACK_SELECTION("packSelection"),
        ACTION_MANAGED_APP_PACK_SELECTION("HOME#PACKCONTENT"),
        ACTION_MANAGED_APP_VIEW_PACK("PACKSELECTION#CYOP"),
        ACTION_MANAGED_APP_PROVIDER_PACK("PROVIDERPACSELECT#CONTENT"),
        ACTION_MANAGED_APP_REGIONAL_APP_SELECTION("REGAPPSELECTION#CONTENT")
    }

    const val DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER = "DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER"
    const val DEEPLINK_ACTION_CHECK_DTH_STATUS_AND_RECHARGE =
        "DEEPLINK_ACTION_CHECK_DTH_STATUS_AND_RECHARGE"
    private const val CUSTOM_URI_SCHEME_WITH_HOST = "tataplaybinge://*/"
    private const val APPLINK_HOST = BuildConfig.hostName
    private const val APPLINK_SCHEME = "https://"
    const val GOOGLE_ANALYTICS_DEFERRED_DEEPLINK_PREF = "google.analytics.deferred.deeplink.prefs"
    const val KEY_DEEPLINK = "deeplink"
    const val KEY_TIMESTAMP = "timestamp"
}