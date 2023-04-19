package com.tatasky.binge.ui.features.home

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.transition.AutoTransition
import androidx.transition.Fade
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.airbnb.lottie.LottieAnimationView
import com.clevertap.android.sdk.CleverTapAPI
import com.erosnow.partner.ENConfiguration
import com.erosnow.partner.ENSDK
import com.erosnow.partner.ErosNowEnvironment
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.HomeDirections
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.databinding.LayoutTsWalletBalanceBinding
import com.tatasky.binge.helper.DeeplinkHelper
import com.tatasky.binge.helper.DeeplinkHelper.DEEPLINK_ACTION_CHECK_DTH_STATUS_AND_RECHARGE
import com.tatasky.binge.helper.DeeplinkHelper.DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER
import com.tatasky.binge.helper.NudgeHelper.showRegionalAppNudge
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.interfaces.ConfettiDialogEventListener
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.MiscAnalytics
import com.tatasky.binge.ui.features.coachmark.CoachMark
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.dialog.ConfettiDialogModel
import com.tatasky.binge.ui.features.dialog.ConfettiDialogViewModel
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.fsinstallation.FSInstallationActivity
import com.tatasky.binge.ui.features.home.bottomsheet.categories.CategoriesBottomSheetDialog
import com.tatasky.binge.ui.features.home.bottomsheet.welcome.WelcomeMessageDialog
import com.tatasky.binge.ui.features.more.SettingsViewModel
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.*
import com.tatasky.binge.ui.features.search.SearchAnalytics
import com.tatasky.binge.ui.features.sidemenunavdrawer.NavDrawerActions
import com.tatasky.binge.ui.features.subscription.view.FirestickDialog
import com.tatasky.binge.ui.features.subscription_freemium.FreemiumSubscriptionActivity.Companion.KEY_HAS_LOW_BALANCE_FOR_THIS_TXN
import com.tatasky.binge.ui.features.subscription_freemium.SubscriptionBottomContainerFragment
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.ui.features.ttnplayer.TTNPlayerFragment
import com.tatasky.binge.ui.features.updateprofile.EditProfileViewModel
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.PaymentUtility.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers
import com.tatasky.binge.utils.PaymentUtility.getPgPaymentStatus
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_header_guest_login.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.ParticleSystem
import nl.dionsegijn.konfetti.listeners.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.models.Size
import java.util.*
import java.util.concurrent.TimeUnit


class LandingActivity : BaseActivity<CommonSampleViewModel>(),
    DeeplinkHelper.DeeplinkHandler by DeeplinkHelper.DeeplinkHandlerImpl() {

    @Inject
    lateinit var coachMark: CoachMark

    private var shouldTriggerInMixpanel: Boolean = true

    //pg: Payment Gateway
    private var pgPaymentStatus: String? = null
    private var pgResponseCode: String? = null

    @Inject
    lateinit var subscriptionViewModel: FreemiumSubscriptionViewModel
    private var currentPack: PartnerPacks? = null


//    //To keep current journey flags for managed apps
//    var currentJourneyRef:String =""
//    var currentJourneyRefKey:String =""
//    var cartId:String =""

    private var isUserSubscribed: Boolean = true
    private var latestBalanceResponse: WalletBalanceResponse? = null
    private var gameLottieVisible: Boolean = false
    var isScreenFullScreenMode: Boolean = false
    var isParentalPinChanged: Boolean = false


    @Inject
    lateinit var homeAnalytics: HomeAnalytics
    @Inject
    lateinit var miscAnalytics: MiscAnalytics
    var deviceType:String?=null


    private var isFreeTrialStartedUIShown: Boolean = false
    var isResultHandled: Boolean = false
    private var paymentInfoBundle: Bundle? = null
    private var dialogFlag = false

    private lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    lateinit var navDrawerViewModel: SettingsViewModel
    val editProfileViewModel: EditProfileViewModel by lazy {

        ViewModelProvider(this, viewModelFactory)[EditProfileViewModel::class.java]

    }

    lateinit var miniDrawerDisposable: Disposable
    var needToShowMiniDrawer = true

    //    val actionInterruptSelectedTabInKidsModeLiveData = MutableLiveData<SingleEvent<Int>>()
    val parentalControlSnackbarUtil = ParentalControlSnackbarUtil()
    val customSnackbarWithTwoActionsUtil = CustomSnackbarWithTwoActionsUtil()
    val customSnackbarGameNudge = CustomSnackbarWithTwoActionsUtil()
    val customSnackbarWithEditTextTwoActionsUtil = CustomSnackbarWithEditTextTwoActionsUtil()
    var currentMenuItemId: LiveData<Int>? = null
    private var currentNavController: LiveData<NavController>? = null
    private var categoryBottomSheetDialog: CategoriesBottomSheetDialog? = null
    var subscriptionBottomSheetDialog : SubscriptionBottomContainerFragment? = null
    var welcomeDialog : WelcomeMessageDialog? = null
    var bottomNavShowing = true
    var allowTouches = false
    var categoryPageName =""
    var categoryPageType =""
    private var paymentCallbackArrayInfo  : ArrayList<Long>? = null
    private var retryCount = 1
    private var mHandler = Handler(Looper.getMainLooper())
    var packList: EligiblePackResponse? = null
    var tabWasSelected = false
//    val packUpdateStatus = HashMap<Int,Boolean>()


    //todo: confirm from shilpi regarding allowing touch on home tab
    var destinationsOfAllowedTouches = setOf(/*R.id.action_sub_landing_home,
       */ R.id.searchFragment,
        R.id.action_account_landing,
        R.id.watchlistFragment
    )
    var destinationsWhereToolbarShown = setOf(
        R.id.action_sub_landing_home,
        R.id.action_sub_landing_movie,
        R.id.action_sub_landing_shows,
        R.id.action_sub_landing_sports,
        R.id.action_sub_landing_games,
        R.id.selectLanguageBottomSheetDialog,
        R.id.guestLoginBottomDialogFragment,
        R.id.subscriptionTypeSelectorFragment
    )

    @Inject
    lateinit var searchAnalytics: SearchAnalytics

    @Inject
    lateinit var localBroadcastHelper: LocalBroadcastHelper

    private var navDestinationChangedListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            tabWasSelected = true
            homeAnalytics.trackBottomTabItemSelected(
                when(destination.id) {
                    R.id.action_sub_landing_home -> SOURCE_HOME
                    R.id.action_sub_landing_movie -> SOURCE_MOVIES
                    R.id.action_sub_landing_shows -> SOURCE_TV_SHOWS
                    R.id.action_sub_landing_games -> SOURCE_GAMES
                    R.id.action_sub_landing_sports -> SOURCE_SPORTS
                    else -> {
                        tabWasSelected = false
                        null
                    }
                }
            )
            if (destination.id in destinationsWhereToolbarShown) {
                if ((controller.previousBackStackEntry?.destination?.id != R.id.action_managed_apps)
                    && (destination.id != R.id.guestLoginBottomDialogFragment)
                )
                    Handler(Looper.getMainLooper()).postDelayed(
                        { home_toolbar.show() }, 100
                    )
            } else
                Handler(Looper.getMainLooper()).postDelayed(
                    {home_toolbar.hide()}, 100)
            e("bottomListener","destination.id : ${destination.id}")
            if (destination.id in setOf(
                    R.id.voiceSearchDialog,
                    R.id.switchAccount,
//                    R.id.parentalPinMenuFragment,
//                    R.id.parentalControlPinFragment,
//                    R.id.parentalControlSetupFragment,
//                    R.id.parentalControlOtpFragment,
                    R.id.episodeSeeAllFragment,
                    R.id.parentalControlSettingsFragment,
                    R.id.parentalControlRatingFragment,
                    R.id.episodeVoiceSearch,
                    R.id.watchlistFragment,
                    R.id.notificationFragment,
                    R.id.settingsFragment,
                    R.id.termsConditionFragment,
                    R.id.privacyPolicyFragment,
                    R.id.deviceListFragment,
                    R.id.transactionHistoryFragment,
                    R.id.deviceListFragment,
                    R.id.contentLanguage,
                    R.id.contentLanguage,
                    R.id.transactionHistoryFragment,
                    R.id.switchAccountFragment,
                    R.id.editProfileFragment,
                    R.id.faq_fragment,
                    R.id.managedAppFragment,
                    R.id.licenseAgreementFragment
                )
            ) {
                e("bottomListener","inside hideBottomNav")
                hideBottomNav()
            } else {
                e("bottomListener","inside showBottomNav")
                showBottomNav()
            }
            if(destination.id == R.id.action_sub_landing_games){
                hideGameBottomAnim()
                gameLottieVisible = false
            }

            allowTouches = destination.id in destinationsOfAllowedTouches
        }

    internal fun getSourceOrFromScreenName() =
        intent?.extras?.get(KEY_FROM_SCREEN) as String? /*Intent extra is being used to get source from previous activity or from deeplink*/
            ?: viewModel.source.takeIf {
                !it.isNullOrBlank()
            }?.also { viewModel.source = null /*After consumption, make it null again*/ }
            ?: SOURCE_HOME

    fun showFreeTrialStartedUI() {
        if (!isFreeTrialStartedUIShown) {
            sharedPrefs.setIsEligibleForFreeTrial(false)
            val konfettiView = this.findViewById<KonfettiView>(R.id.viewKonfetti)
            if (konfettiView != null) {
                findViewById<MaterialCardView>(R.id.transparentKonfettiParent)?.show()
                konfettiView.build()
                    .addColors(
                        ContextCompat.getColor(this, R.color.color_golden_vip),
                        ContextCompat.getColor(this, R.color.color_golden_vip)
                    )
                    .setDirection(90.0)
                    .setSpeed(1f, 5f)
                    .setFadeOutEnabled(true)
                    .setTimeToLive(1000L)
                    .addShapes(
                        nl.dionsegijn.konfetti.models.Shape.Rectangle(0.5f),
                        nl.dionsegijn.konfetti.models.Shape.Circle
                    )
                    .addSizes(Size(8))
                    .setPosition(-50f, 2000f, -50f, -50f)
                    .streamFor(250, 1500L)

                konfettiView.onParticleSystemUpdateListener = object :
                    OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(
                        view: KonfettiView,
                        system: ParticleSystem,
                        activeSystems: Int
                    ) {
                        isFreeTrialStartedUIShown = true
                        findViewById<MaterialCardView>(R.id.transparentKonfettiParent)?.hide()
                    }

                    override fun onParticleSystemStarted(
                        view: KonfettiView,
                        system: ParticleSystem,
                        activeSystems: Int
                    ) { }
                }

            }
        }
    }

    private fun showPaymentSuccessful() {
        if(!NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(),true)) {
            navDrawerViewModel.fetchBalance()
        }
        customSnackbarWithTwoActionsUtil.takeIf {
            it.snackBarType == CustomSnackbarWithTwoActionsType.SnackbarTypeSubsExpired
                    || it.snackBarType == CustomSnackbarWithTwoActionsType.SnackbarTypeSubsRenewal
        }?.hideCustomSnackbarWithTwoActions()

        val addPackCalled = paymentInfoBundle?.getBoolean("addPackCalled")
        val header = paymentInfoBundle?.getString("paymentHeader")
        val message = paymentInfoBundle?.getString("paymentMessage")
        val footer = paymentInfoBundle?.getString("paymentFooter")
        if(!dialogFlag) {
            dialogFlag = true
            if (addPackCalled == true) {
                val dialogViewModel =
                    ViewModelProvider(this, viewModelFactory).get(ConfettiDialogViewModel::class.java)
                val dialogModel =
                    ConfettiDialogModel(
                        R.drawable.ic_tick_login_success,
                        header?:"",
                        getString(R.string.start_watching_now),
                        message?:"",
                        footer?:""
                    )
                val eventListener = object : ConfettiDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        //dismiss
                        hideConfettiDialog()
                        sharedPrefs.resetFSDialog()
                        showFirestickOfferDialogByFrequency(true)
                    }
                }
                dialogViewModel.setDialogModel(dialogModel)
                dialogViewModel.setEventHandler(eventListener)
                showConfettiDialog()
            } else {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        showDialog(
                            DialogModel(
                                cancelable = false,
                                imageId = R.drawable.ic_tick_login_success,
                                title = header ?: "",
                                primaryButtonText = getString(R.string.start_watching_now),
                                text = message ?:"",
                                subText = footer?:"",
                                secondaryButtonText = null
                            ),object: CommonDialogEventListener {
                                override fun onPrimaryButtonClick() {
                                    hideDialog()
                                    sharedPrefs.resetFSDialog()
                                    showFirestickOfferDialogByFrequency(true)
                                }

                                override fun onSecondaryButtonClick() {

                                }

                                override fun onCloseButtonClick() {

                                }

                            }
                        )
                    },300
                )
            }
        }
        localBroadcastHelper.sendBroadcast(
            this@LandingActivity,
            localBroadcastHelper.ACTION_SUBSCRIPTION_UPDATED_DO_REFRESH
        )
        trackEvents()
    }

    private fun trackEvents() {
        currentPack = sharedPrefs.getSubscribedPack()
        latestBalanceResponse =
            navDrawerViewModel.getWalletBalance().value?.peekContent()
                ?: sharedPrefs.getFetchedBalanceData()?.let {
                    Gson().fromJson(
                        sharedPrefs.getFetchedBalanceData(),
                        WalletBalanceResponse::class.java
                    )
                }
        val previousCurrentPack = sharedPrefs.getPreviousSubscribedPack()
        if (true == previousCurrentPack?.freeTrialStatus /*Key to identify if user is/was on Free trial if true, If false means paid pack activated*/)
            subscriptionAnalytics.trackPurchaseEvent()
        lifecycleScope.launchWhenResumed {
            if (paymentInfoBundle?.getBoolean(KEY_PAY_BY_DTH) == true) {
                subscriptionAnalytics.trackPaymentFlowExit(
                    SUCCESS,
                    TP_WALLET,
                    SUCCESS,
                    SUCCESS
                )
            }
            delay(EVENT_DELAY_MS) // Delay is added to maintain the event sequence based on Timestamp
            subscriptionAnalytics.trackPayment(
                if (paymentInfoBundle?.getBoolean(KEY_PAY_BY_DTH) == true) TSWALLET else PG,
                latestBalanceResponse?.data?.balanceQueryRespDTO?.balance ?: "",
                COMPLETED,
                ""/* Fixme, Todo*/,
                "",
                viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/
                    ?: currentPack?.paymentMethod ?: "",
                currentPack?.transactionID ?: paymentInfoBundle?.getString("orderId") ?: "",
                "",
                "",
                paymentInfoBundle?.getString(KEY_PACK_PRICE)
                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                paymentInfoBundle?.getString(KEY_PACK_NAME)
                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.productName
                    ?: "" /*Pack name*/,
                viewModel.sharedPrefs.getAddModifyResponse()?.data?.validityInDays?.let {
                    (it) + "D"
                } /*Due to FDO, Use validity from Add/Modify pack response*/
                    ?: currentPack?.packDurationInDaysWithDSuffix,
                viewModel.sharedPrefs.getAddModifyResponse()?.data?.productId,
                viewModel.getPaymentStatus().value?.peekContent()?.data?.promoCode /*Due to FDO, using API response*/
                    ?: currentPack?.promoCode,
                viewModel.sharedPrefs.getAddModifyResponse()?.data?.paymentPayload?.payload?.amount
                    ?: paymentInfoBundle?.getString(KEY_ACTUAL_PRORATED_AMOUNT_FROM_API) /*For Old stack users*/,
                viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/
                    ?: currentPack?.paymentMethod,
                paymentInfoBundle?.getString(KEY_SELECTED_TENURE_PACK_PRICE)
                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                paymentInfoBundle?.getString(KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX)
                    ?: currentPack?.packDurationInDaysWithDSuffix,
                pgPaymentStatus ?: SUCCESS,
                pgResponseCode ?: SUCCESS,
                shouldTriggerInMixpanel
            )
            delay(EVENT_DELAY_MS)
            if (paymentInfoBundle?.getString(KEY_MODIFICATION_TYPE) != null) {
                subscriptionAnalytics.trackModifyPackSuccess(
                    paymentInfoBundle?.getString(KEY_FROM_SCREEN) ?: SOURCE_DEEPLINK,
                    paymentInfoBundle?.getString(KEY_PACK_NAME)
                        ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.productName
                        ?: "" /*Pack name*/,
                    paymentInfoBundle?.getString(KEY_PACK_PRICE)
                        ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                    paymentInfoBundle?.getString(KEY_MODIFICATION_TYPE)
                        ?: "" /*Includes Renew Use case*/,
                    viewModel.sharedPrefs.getAddModifyResponse()?.data?.productId,
                    viewModel.sharedPrefs.getAddModifyResponse()?.data?.validityInDays?.let {
                        (it) + "D"
                    } /*Due to FDO, Use validity from Add/Modify pack response*/
                        ?: currentPack?.packDurationInDaysWithDSuffix,
                    viewModel.getPaymentStatus().value?.peekContent()?.data?.promoCode /*Due to FDO, using API response*/
                        ?: currentPack?.promoCode,
                    viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/
                        ?: currentPack?.paymentMethod,
                    viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount,
                    previousCurrentPack?.productId,
                    paymentInfoBundle?.getString(KEY_APPSFLYER_SOURCE) ?: SOURCE_HOME,
                    paymentInfoBundle?.getString(KEY_SELECTED_TENURE_PACK_PRICE)
                        ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                    paymentInfoBundle?.getString(KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX)
                        ?: currentPack?.packDurationInDaysWithDSuffix,
                    currentPack?.packType
                        ?: if (true == previousCurrentPack?.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID,
                    previousCurrentPack?.productName,
                    previousCurrentPack?.packType
                        ?: if (true == previousCurrentPack?.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID,
                    previousCurrentPack?.amountValue,
                    previousCurrentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType,
                    paymentInfoBundle?.getString(KEY_SELECTED_TENURE_TYPE),
                    paymentInfoBundle?.getBoolean(KEY_IS_FIRST_SUBSCRIPTION, false) == true,
                    paymentInfoBundle?.getString(KEY_PRODUCT_TYPE)
                )
            }
            delay(EVENT_DELAY_MS)
            subscriptionAnalytics.trackSubscribeSuccess(
                currentPack?.packType ?: PACK_TYPE_PAID,
                paymentInfoBundle?.getString(KEY_FROM_SCREEN) ?: SOURCE_DEEPLINK,
                paymentInfoBundle?.getString(KEY_PACK_NAME)
                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.productName
                    ?: "" /*Pack name*/,
                paymentInfoBundle?.getString(KEY_PACK_PRICE)
                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                intent?.extras?.getBoolean("isFromNudge") ?: false,
                currentPack?.fdoRequested == true,
                "",
                paymentInfoBundle?.getString(KEY_MODIFICATION_TYPE) ?: "" /*Includes Renew Use case*/,
                paymentMethod = if (paymentInfoBundle?.getBoolean(KEY_PAY_BY_DTH) == true) TSWALLET else PG,
                paymentType =
                when (
                    viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMode
                        ?: currentPack?.paymentMode
                ) {
                    OPEL_ONE_TIME -> ONETIME
                    OPEL_RECURRING -> RECURRING
                    else -> ""
                },
                viewModel.sharedPrefs.getAddModifyResponse()?.data?.productId,
                viewModel.getPaymentStatus().value?.peekContent()?.data?.promoCode /*Due to FDO, using API response*/
                    ?: currentPack?.promoCode,
                viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/
                    ?: currentPack?.paymentMethod,
                currentPack?.userIsOnFirstPaidPack ?: false,
                viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount,
                paymentInfoBundle?.getString(KEY_APPSFLYER_SOURCE) ?: SOURCE_HOME,
                paymentInfoBundle?.getString(KEY_SELECTED_TENURE_PACK_PRICE)
                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                paymentInfoBundle?.getString(KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX)
                    ?: currentPack?.packDurationInDaysWithDSuffix,
                previousCurrentPack?.productName ?: FREEMIUM,
                previousCurrentPack?.let { if (true == it.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID }
                    ?: PACK_TYPE_FREE,
                previousCurrentPack?.amountValue ?: FREEMIUM,
                previousCurrentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType ?: "",
                paymentInfoBundle?.getString(KEY_SELECTED_TENURE_TYPE),
                paymentInfoBundle?.getBoolean(KEY_IS_FIRST_SUBSCRIPTION, false) == true,
                shouldTriggerInMixpanel,
                paymentInfoBundle?.getString(KEY_PRODUCT_TYPE)
            )
        }
    }

    private fun setPaymentProgressing(enabled: Boolean) {
        if (enabled) {
            paymentProgressBar.startProgressAvd(true)
        } else {
            paymentProgressBar.startProgressAvd(false)
        }
    }

    override fun onPause() {
        super.onPause()
        dialogFlag = false
    }

    private fun stopMiniDrawerTimer(){
        needToShowMiniDrawer = false
        if (::miniDrawerDisposable.isInitialized)
            miniDrawerDisposable.dispose()
        subscriptionBottomSheetDialog?.dismiss()
    }

    override fun init(savedInstanceState: Bundle?) {

        paymentCallbackArrayInfo = sharedPrefs.getConfigResponse()?.data?.config?.paymentCallbackArrayInfo?.paymentStatusApiArray?: arrayListOf(10000L,30000L)
        paymentProgressBar.viewTreeObserver.addOnGlobalLayoutListener {
            when (paymentProgressBar.visibility) {
                View.VISIBLE -> {
                    this.window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                }
                View.GONE -> {
                    this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
                View.INVISIBLE -> {
                    this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        }

        deviceType = if(isTablet(this))
            DEVICE_TYPE_TABLET
        else DEVICE_TYPE
        deviceType?.let {
            sharedPrefs.setDeviceType(it)
        }

        bottomNav.itemIconTintList = null
        viewModel.sharedPrefs.setInterruptCategoryTabStatus(true)
        navDrawerViewModel =
            ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        navDrawerViewModel.setFromSavedProfileInfo()
        navDrawerViewModel.setFromSavedWalletBalance()
        toBeCalledOnce()
        //handle FS popup flow
        if (!intent.getBooleanExtra("checkPaymentStatus", false)) {
            handleFSFlow()
        }
        //handle Drawer Width Dynamically
        handleDrawerUI()
        handleNavDrawerActions()
        if (intent.getBooleanExtra("checkPaymentStatus", false)) {
            checkPaymentStatus()
        }
//
//        if (/*!viewModel.isLoggedIn()*/false) {
//            if (intent.data != null) {
//                if (viewModel.sharedPrefs.getShowMarketingScreen())
//                    logoutMarketingApplication(this)
//                else {
//                    logoutApplication(this)
//                }
//                return
//            }
//            (application as MyApp).clearAllData()
//            showLogoutDialog()
//            return
//        }

        initErosNow()

        d("LandingActivity", "isFirstTimeLandingOpening: ${sharedPrefs.isFirstTimeLandingOpen()}")
        if(sharedPrefs.isFirstTimeLandingOpen() /*&& conditionsToShowGameNudge()*/) {
            if(sharedPrefs.getConfigResponse()?.data?.app?.gameAnimPopup?.gameAndroid?.popupEnabled != false){
                conditionsToShowGameBottomNavAnimation()
            }
        }

        if (viewModel.isLoggedIn()) {
            viewModel.fetchCurrentSubscription(false)
            viewModel.deleteAllUnusedToken()
        } else
            showNudges()

        //Used for launching the subscription drawer4
        //Todo Uncomment before prodcution
        if(!sharedPrefs.isFirstTimeLanguagePopUpShown())
            fetchPackListAfterLaunch()
        d("App Launch Count", "${viewModel.sharedPrefs.getAppLaunchValue()}")
        viewModel.generateAID()

        localBroadcastHelper.registerBroadcast(
            this,
            mNotificationReceiver,
            localBroadcastHelper.ACTION_NOTIFICATION_RECEIVED
        )
        localBroadcastHelper.registerBroadcast(
            this,
            mNotificationReceiver,
            localBroadcastHelper.ACTION_LOGOUT
        )
        localBroadcastHelper.registerBroadcast(
            this,
            mNotificationReceiver,
            localBroadcastHelper.ACTION_DEVICE_STATUS_LOGOUT
        )
        localBroadcastHelper.registerBroadcast(
            this,
            mNotificationReceiver,
            localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH
        )
        localBroadcastHelper.registerBroadcast(
            this,
            mNotificationReceiver,
            localBroadcastHelper.ACTION_PAYMENT_UPDATED
        )
        localBroadcastHelper.registerBroadcast(
            this,
            mNotificationReceiver,
            localBroadcastHelper.ACTION_SUBSCRIPTION_UPDATED
        )

        if (savedInstanceState == null) {
            setupDrawerLayout()
            setupBottomNavigationBar()
        }

        bottomNav.fixBlinking(this)
        //ResourcesCompat.getFont(this, R.font.gochi_hand)?.let { bottomNav.changeNavTypeface(it) }
        bottomNav.hideToolTip()


        viewModel.refreshHome?.observe(this) {
            it.getContentIfNotHandled()?.let{it1->
                if(it1) {
                    //Reload the activity
                    finish()
                    overridePendingTransition( 0, 0)
                    startActivity(intent)
                    overridePendingTransition( 0, 0)
                }
            }
        }

        viewModel.getPaymentStatus().observe(this) {
            it.getContentIfNotHandled()?.let {paymentStatusResponse ->
                val orderId = paymentInfoBundle?.getString("orderId")?:""
                when (paymentStatusResponse.data?.paymentStatus) {
                    SUCCESS -> {
                        setPaymentProgressing(false)
                        viewModel.fetchCurrentSubscription(true)
//                        showPaymentSuccessful()
                    }
                    INPROGRESS -> {
                        setPaymentProgressing(true)
                        if (retryCount < paymentCallbackArrayInfo!!.size) {
                            mHandler.postDelayed(
                                {
                                    setPaymentProgressing(true)
                                    viewModel.fetchPaymentStatus(orderId)
                                },
                                paymentCallbackArrayInfo!![retryCount]
                            )
                            ++retryCount
                        } else {
                            shouldTriggerInMixpanel = false
                            setPaymentProgressing(false)
                            sharedPrefs.setPaymentPendingStatus(true)
                            showDialog(
                                DialogModel(
                                    imageId = R.drawable.ic_subscription_error,
                                    cancelable = false,
                                    title = sharedPrefs.getAddModifyResponse()?.data?.paymentErrorVerbiages?.transactionPendingVerbiage?:"",
                                    primaryButtonText = getString(R.string.close),
                                    secondaryButtonText = null
                                ), object : CommonDialogEventListener {
                                    override fun onPrimaryButtonClick() {
                                        hideDialog()
                                    }

                                    override fun onSecondaryButtonClick() {
                                    }

                                    override fun onCloseButtonClick() {
                                    }
                                }
                            )
                            lifecycleScope.launchWhenResumed {
                                if (paymentInfoBundle?.getBoolean(KEY_PAY_BY_DTH) == true) {
                                    subscriptionAnalytics.trackPaymentFlowExit(
                                        PENDING,
                                        TP_WALLET,
                                        PENDING,
                                        PENDING
                                    )
                                }
                                delay(EVENT_DELAY_MS)
                                subscriptionAnalytics.trackPayment(
                                    if (paymentInfoBundle?.getBoolean(KEY_PAY_BY_DTH) == true) TSWALLET else PG,
                                    latestBalanceResponse?.data?.balanceQueryRespDTO?.balance ?: "",
                                    PENDING,
                                    "",
                                    "",
                                    viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/
                                        ?: sharedPrefs.getSubscribedPack()?.paymentMethod ?: "",
                                    orderId,
                                    "",
                                    viewModel.sharedPrefs.getAddModifyResponse()?.data?.paymentErrorVerbiages?.transactionPendingVerbiage
                                        ?: "",
                                    paymentInfoBundle?.getString(KEY_PACK_PRICE)
                                        ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount
                                        ?: "",
                                    paymentInfoBundle?.getString(KEY_PACK_NAME)
                                        ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.productName
                                        ?: "" /*Pack name*/,
                                    viewModel.sharedPrefs.getAddModifyResponse()?.data?.validityInDays?.let {
                                        (it) + "D"
                                    } /*Due to FDO, Use validity from Add/Modify pack response*/
                                        ?: sharedPrefs.getSubscribedPack()?.packDurationInDaysWithDSuffix,
                                    viewModel.sharedPrefs.getAddModifyResponse()?.data?.productId,
                                    viewModel.getPaymentStatus().value?.peekContent()?.data?.promoCode /*Due to FDO, using API response*/
                                        ?: sharedPrefs.getSubscribedPack()?.promoCode,
                                    viewModel.sharedPrefs.getAddModifyResponse()?.data?.paymentPayload?.payload?.amount
                                        ?: paymentInfoBundle?.getString(
                                            KEY_ACTUAL_PRORATED_AMOUNT_FROM_API
                                        ) /*For Old stack users*/,
                                    viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/
                                        ?: sharedPrefs.getSubscribedPack()?.paymentMethod,
                                    paymentInfoBundle?.getString(KEY_SELECTED_TENURE_PACK_PRICE)
                                        ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount
                                        ?: "",
                                    paymentInfoBundle?.getString(
                                        KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX
                                    ) ?: "",
                                    pgPaymentStatus ?: PENDING,
                                    pgResponseCode ?: PENDING
                                )
                                delay(EVENT_DELAY_MS)
                                subscriptionAnalytics.trackSubscribeFailure(
                                    sharedPrefs.getAddModifyResponse()?.data?.paymentErrorVerbiages?.transactionPendingVerbiage
                                        ?: "",
                                    paymentInfoBundle?.getString(KEY_PACK_NAME)
                                        ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.productName
                                        ?: "", /*Pack name*/
                                    PACK_TYPE_PAID,
                                    paymentMethod = if (paymentInfoBundle?.getBoolean(KEY_PAY_BY_DTH) == true) TSWALLET else PG,
                                    paymentType =
                                    when (viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMode) {
                                        OPEL_ONE_TIME -> ONETIME
                                        OPEL_RECURRING -> RECURRING
                                        else -> ""
                                    },
                                    paymentInfoBundle?.getString(KEY_SELECTED_TENURE_TYPE),
                                    paymentInfoBundle?.getString(KEY_FROM_SCREEN)
                                        ?: SOURCE_DEEPLINK,
                                    currentPack?.productName ?: FREEMIUM,
                                    currentPack?.let { if (true == it.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID }
                                        ?: PACK_TYPE_FREE,
                                    paymentInfoBundle?.getString(KEY_PACK_PRICE)
                                        ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount
                                        ?: "",
                                    currentPack?.amountValue ?: FREEMIUM,
                                    currentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                                        ?: "",
                                    paymentInfoBundle?.getBoolean(
                                        KEY_IS_FIRST_SUBSCRIPTION,
                                        false
                                    ) == true,
                                    paymentInfoBundle?.getString(KEY_MODIFICATION_TYPE),
                                    paymentInfoBundle?.getString(KEY_SELECTED_TENURE_PACK_PRICE)
                                        ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount
                                        ?: "",
                                    paymentInfoBundle?.getString(
                                        KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX
                                    )
                                        ?: "",
                                    viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/
                                        ?: sharedPrefs.getSubscribedPack()?.paymentMethod,
                                    viewModel.getPaymentStatus().value?.peekContent()?.data?.promoCode /*Due to FDO, using API response*/
                                        ?: sharedPrefs.getSubscribedPack()?.promoCode,
                                    shouldTriggerInMixpanel,
                                    actualAmountPaid = viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount,
                                    productType = paymentInfoBundle?.getString(KEY_PRODUCT_TYPE)
                                )
                            }
                        }
                    } else -> {
                        lifecycleScope.launchWhenResumed {
                            if (paymentInfoBundle?.getBoolean(KEY_PAY_BY_DTH) == true) {
                                subscriptionAnalytics.trackPaymentFlowExit(
                                    FAILURE,
                                    TP_WALLET,
                                    FAILURE,
                                    FAILURE
                                )
                            }
                            delay(EVENT_DELAY_MS)
                            subscriptionAnalytics.trackPayment(
                                if (paymentInfoBundle?.getBoolean(KEY_PAY_BY_DTH) == true) TSWALLET else PG,
                                latestBalanceResponse?.data?.balanceQueryRespDTO?.balance ?: "",
                                FAILED,
                                "",
                                "",
                                viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/ ?: sharedPrefs.getSubscribedPack()?.paymentMethod ?: "",
                                orderId,
                                "",
                                viewModel.sharedPrefs.getAddModifyResponse()?.data?.paymentErrorVerbiages?.transactionPendingVerbiage ?: "",
                                paymentInfoBundle?.getString(KEY_PACK_PRICE)
                                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                                paymentInfoBundle?.getString(KEY_PACK_NAME)
                                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.productName
                                    ?: "" /*Pack name*/,
                                viewModel.sharedPrefs.getAddModifyResponse()?.data?.validityInDays?.let {
                                    (it) + "D"
                                } /*Due to FDO, Use validity from Add/Modify pack response*/
                                    ?: sharedPrefs.getSubscribedPack()?.packDurationInDaysWithDSuffix,
                                viewModel.sharedPrefs.getAddModifyResponse()?.data?.productId,
                                viewModel.getPaymentStatus().value?.peekContent()?.data?.promoCode /*Due to FDO, using API response*/  ?: sharedPrefs.getSubscribedPack()?.promoCode,
                                viewModel.sharedPrefs.getAddModifyResponse()?.data?.paymentPayload?.payload?.amount ?: paymentInfoBundle?.getString(KEY_ACTUAL_PRORATED_AMOUNT_FROM_API) /*For Old stack users*/,
                                viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/ ?: sharedPrefs.getSubscribedPack()?.paymentMethod,
                                paymentInfoBundle?.getString(KEY_SELECTED_TENURE_PACK_PRICE)
                                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                                paymentInfoBundle?.getString(KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX) ?: "",
                                pgPaymentStatus ?: FAILURE,
                                pgResponseCode ?: FAILURE
                            )
                            delay(EVENT_DELAY_MS)
                            subscriptionAnalytics.trackSubscribeFailure(
                                sharedPrefs.getAddModifyResponse()?.data?.paymentErrorVerbiages?.paymentFailureVerbiage?:"",
                                paymentInfoBundle?.getString(KEY_PACK_NAME)
                                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.productName
                                    ?: "" /*Pack name*/,
                                PACK_TYPE_PAID,
                                paymentMethod = if (paymentInfoBundle?.getBoolean(KEY_PAY_BY_DTH) == true) TSWALLET else PG,
                                paymentType =
                                when (viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMode) {
                                    OPEL_ONE_TIME -> ONETIME
                                    OPEL_RECURRING -> RECURRING
                                    else -> ""
                                },
                                paymentInfoBundle?.getString(KEY_SELECTED_TENURE_TYPE),
                                paymentInfoBundle?.getString(KEY_FROM_SCREEN) ?: SOURCE_DEEPLINK,
                                currentPack?.productName ?: FREEMIUM,
                                currentPack?.let { if (true == it.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID }
                                    ?: PACK_TYPE_FREE,
                                paymentInfoBundle?.getString(KEY_PACK_PRICE)
                                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                                currentPack?.amountValue ?: FREEMIUM,
                                currentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType ?: "",
                                paymentInfoBundle?.getBoolean(KEY_IS_FIRST_SUBSCRIPTION, false) == true,
                                paymentInfoBundle?.getString(KEY_MODIFICATION_TYPE),
                                paymentInfoBundle?.getString(KEY_SELECTED_TENURE_PACK_PRICE)
                                    ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount
                                    ?: "",
                                paymentInfoBundle?.getString(
                                    KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX
                                )
                                    ?: "",
                                viewModel.getPaymentStatus().value?.peekContent()?.data?.paymentMethod /*Due to FDO, using API response*/
                                    ?: sharedPrefs.getSubscribedPack()?.paymentMethod,
                                viewModel.getPaymentStatus().value?.peekContent()?.data?.promoCode /*Due to FDO, using API response*/
                                    ?: sharedPrefs.getSubscribedPack()?.promoCode,
                                actualAmountPaid = viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount,
                                productType = paymentInfoBundle?.getString(KEY_PRODUCT_TYPE)
                            )
                        }
                        setPaymentProgressing(false)
                    }
                }
            }
        }

        viewModel.updateInPack.observe(this){
            it.getContentIfNotHandled()?.let{
                if(it){
                    handleSubscribeButtonVisibility()
                    hideNudges()
                    showNudges()
                    sharedPrefs.setFirstTimeLandingOpen(false)
                    viewModel.checkForManagedAppEligibility {
                        if (it)
                            showRegionalAppNudge(viewModel, isManagedAppOpen())
                    }


                }
            }

        }


        viewModel.getSubscriptionResponse().observe(this){
            it.getContentIfNotHandled().let {
                showPaymentSuccessful()
            }
        }


        viewModel.getLiveNotificationCount().observe(this, Observer {
            it.getContentIfNotHandled()?.let { unreadNotificationCount ->
                if (unreadNotificationCount > 0) {
                    bottomNav.getOrCreateBadge(R.id.account).apply {
                        this.verticalOffset = 12
                        this.badgeGravity = BadgeDrawable.BOTTOM_START
                        this.backgroundColor =
                            ContextCompat.getColor(this@LandingActivity, R.color.darkError)
                        this.isVisible = true
                    }
                } else {
                    bottomNav.getOrCreateBadge(R.id.account).apply {
                        this.isVisible = false
                    }
                }
            }
        })
        viewModel.getLiveOrientation().observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                when (it) {
                    OrientationManager.ScreenOrientation.PORTRAIT,
                    OrientationManager.ScreenOrientation.REVERSED_PORTRAIT -> {
                        handleDrawerUI()
                        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val params = window.attributes
                            params.layoutInDisplayCutoutMode =
                                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                        }
                        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    }
                    OrientationManager.ScreenOrientation.LANDSCAPE,
                    OrientationManager.ScreenOrientation.REVERSED_LANDSCAPE -> {
                        handleDrawerUI()
                        if(isScreenFullScreenMode){
                            hideBottomNav()
                        }else{
                            showBottomNav()
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val params = window.attributes
                            params.layoutInDisplayCutoutMode =
                                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        }
                        window?.decorView?.systemUiVisibility = (
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                )
                        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    }
                }
            }
        })
        viewModel.forceDeviceStatusLogout.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                localBroadcastHelper.sendBroadcast(
                    this,
                    localBroadcastHelper.ACTION_DEVICE_STATUS_LOGOUT
                )
            }
        })

        viewModel.getCategoryList().observe(this) {
            it.getContentIfNotHandled()?.let { _ ->
                showCategoryBottomSheet()
            }
        }
        viewModel.setTotalUnreadCount()

        navDrawerViewModel.getToggledSetting().observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                when (it) {
                    WATCH_NOTI_SETTINGS_KEY -> {
                        val prevValue = sharedPrefs.getWatchNotificationAllowed()
                        viewModel.sharedPrefs.setAllowWatchNotification(!prevValue)
                        showToast(context = this, getString(R.string.msg_notification_on))
                    }
                }
            }
        })

        handleDeeplink()
        subscriptionAnalytics.updateProperty(NOTIFICATION_ENABLED,if (sharedPrefs.getWatchNotificationAllowed()) YES else NO)
    }

    private fun fetchPackListAfterLaunch() {
        //COMMENTED PREVIOUS DAY BASED LAUNCH DUE TO ATL REQUIREMENT

//        val lastTime = viewModel.sharedPrefs.getSubscriptionDrawerLastSeen()
//        val gapToShow = (Calendar.getInstance().timeInMillis - lastTime)/(1000*60*60) // change in hours
//        e("MiniDrawerLogic","gapToShow:$gapToShow, lastTime: ${lastTime/1000}")
//        if(!isUserSubscribed &&
//            sharedPrefs.getConfigResponse()?.data?.config?.subscriptionDrawer?.gapToOpenSubscriptionDrawer?:0 <= gapToShow &&
//            sharedPrefs.getConfigResponse()?.data?.config?.subscriptionDrawer?.openSubscriptionDrawer == true
//        ) {
//            e(
//                "Landjsfjds",
//                "sharedPrefs.isFirstTimeLandingOpen() : ${sharedPrefs.isFirstTimeLandingOpen()}"
//            )
//            if (intent.data == null && sharedPrefs.isFirstTimeLandingOpen()) {
//                viewModel.fetchEligiblePackList()
//            }
//        }

        fun fetchPackList() {
            e(
                "Landjsfjds",
                "sharedPrefs.isFirstTimeLandingOpen() : ${sharedPrefs.isFirstTimeLandingOpen()}"
            )
            if (intent.data == null && sharedPrefs.isFirstTimeLandingOpen()) {
                viewModel.fetchEligiblePackList()
            }
        }

        val isLoggedIn = sharedPrefs.getLoginStatus()
        val guestDrawerFrequency =
            (sharedPrefs.getConfigResponse()?.data?.config?.subscriptionDrawer?.guestDrawerFrequency
                ?: 1) +1
        val loggedInDrawerFrequency =
            (sharedPrefs.getConfigResponse()?.data?.config?.subscriptionDrawer?.loggedInDrawerFrequency
                ?: 1) + 1
        val openSubscriptionDrawer =
            sharedPrefs.getConfigResponse()?.data?.config?.subscriptionDrawer?.openSubscriptionDrawer

        val showGuestSubscritionDrawer = sharedPrefs.getConfigResponse()?.data?.config?.subscriptionDrawer?.firstLaunchGuestOpenSubscriptionDrawer
        val showLoginSubscritionDrawer = sharedPrefs.getConfigResponse()?.data?.config?.subscriptionDrawer?.firstLaunchLoginOpenSubscriptionDrawer
        val guestAppLaunchCount = sharedPrefs.getAppLaunchValueGuest()
        val loggedInAppLaunchCount = sharedPrefs.getAppLaunchValueLoggedIn()

        fun frequencyChecksForLoggedIn() {
            if (openSubscriptionDrawer == false) return

            if (showLoginSubscritionDrawer != null && showLoginSubscritionDrawer >1) {

                    if (loggedInAppLaunchCount == showLoginSubscritionDrawer || loggedInDrawerFrequency == 0) {
                        fetchPackList()
                    }
                    else {
                        if(loggedInAppLaunchCount !=0)
                        if (loggedInAppLaunchCount == showLoginSubscritionDrawer + loggedInDrawerFrequency) {
                            fetchPackList()
                            sharedPrefs.setAppLaunchValueLoggedIn(loggedInAppLaunchCount - loggedInDrawerFrequency)
                        }
                        else
                            return
                    }
            }
        }

        fun frequencyChecksForGuest() {
            if (openSubscriptionDrawer == false) return

            if (showGuestSubscritionDrawer != null && showGuestSubscritionDrawer >1) {
                    if (guestAppLaunchCount == showGuestSubscritionDrawer || guestDrawerFrequency == 0) {
                        fetchPackList()
                    }
                    else {
                        if (guestAppLaunchCount !=0)
                        if (guestAppLaunchCount  == showGuestSubscritionDrawer + guestDrawerFrequency ) {
                            fetchPackList()
                            sharedPrefs.setAppLaunchValueGuest(guestAppLaunchCount - guestDrawerFrequency)
                        }
                        else
                            return
                    }
            }
        }

        if(isUserSubscribed){
            return
        }

        if (isLoggedIn)
            frequencyChecksForLoggedIn()
        else
            frequencyChecksForGuest()

        if(sharedPrefs.isFirstTimeLandingOpen())
            sharedPrefs.setStartLaunchCount(true)
        sharedPrefs.setFirstTimeLandingOpen(false)
    }

    private fun showLogoutToast() {
        if (intent.extras?.getString(KEY_FROM_SCREEN).equals(SOURCE_LOGOUT, true)) {
            val logoutToastBinding =
                LayoutToastSuccessFailureBinding.inflate(layoutInflater, null, false).apply {
                    textLoginSuccessfulToast.text = getString(R.string.logout_success)
                    imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_logout_24dp)
                }
            showCustomToast(this, logoutToastBinding.root, Gravity.FILL_HORIZONTAL)
        }
    }

    private fun toBeCalledOnce() {
        ProvidersCache.setAllowedProvidersList(sharedPrefs.getAllowedProviderList())
        ProvidersCache.setAvailableProviders(sharedPrefs.getAllowedProviderInfo())
        showLogoutToast()
        CleverTapAPI.getDefaultInstance(this)?.initializeInbox()
        pubnubHelper.getLastStatus(null)
        setObservers()
        setListeners()
        handleSubscribeButtonVisibility()
        setupPaymentSDK(
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.paymentGatewayInfo?.paymentClientId
                ?: "",
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.paymentGatewayInfo?.paymentServiceId
                ?: "",
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.paymentGatewayInfo?.paymentBetaAssets
                ?: false,
            true
        )
        lifecycleScope.launchWhenResumed {
            delay(500)
            setupCoachMark(
                viewModel.sharedPrefs.getGenericAppLaunchCount(),
                viewModel.sharedPrefs.getCoachMarkLaunchFrequency(),
                viewModel.sharedPrefs.isHomeScreenSearchCoachMarkEnabled()
            )
        }
    }

    private fun setupCoachMark(
        genericAppLaunchCount: Int,
        coachMarkLaunchFrequency: Int,
        homeScreenSearchCoachMarkEnabled: Boolean
    ) {
        if (coachMarkLaunchFrequency < 1 ||
            coachMarkLaunchFrequency > genericAppLaunchCount ||
            !homeScreenSearchCoachMarkEnabled
        )
            return
        viewModel.sharedPrefs.enableHomeScreenSearchCoachMark(false)
        coachMark.apply {
            this@LandingActivity.buildCoachMark(
                coachMarkName = PARA_SEARCH,
                source = SOURCE_HOME,
                target = findViewById(R.id.search_icon),
                title = getString(R.string.action_search),
                description = getString(R.string.coach_mark_search_description),
                icon = R.drawable.ic_search,
                iconColor = R.color.darkPrimary
            )
        }

    }

    private fun handleSubscribeCtaVisibility(show: Boolean) {
        viewModel.setSubscribeBtnVisibilty(show)
        if (show) btn_subscribe.show() else btn_subscribe.hide()
    }

    fun handleSubscribeButtonVisibility() {
        if (!sharedPrefs.getLoginStatus()) {
            isUserSubscribed = false
//            btn_subscribe.show()
            handleSubscribeCtaVisibility(true)
        } else {
            val currentSubscription = sharedPrefs.getSubscribedPack()
            if (currentSubscription?.freeTrialStatus == true && (currentSubscription?.subscriptionType.equals(
                    subscriptionTypeAtv
                ) || currentSubscription?.subscriptionType.equals(subscriptionTypeFtv))
            ) {
//                btn_subscribe.hide()
                handleSubscribeCtaVisibility(false)
                isUserSubscribed = true
                return
            }
            if (currentSubscription?.isCombo == true) {
//                btn_subscribe.hide()
                handleSubscribeCtaVisibility(false)
                isUserSubscribed = true
                return
            }
            if (currentSubscription == null) {
//                btn_subscribe.show()
                handleSubscribeCtaVisibility(true)
                isUserSubscribed = false
                return
            }
            isUserSubscribed = true
//            btn_subscribe.hide()
            handleSubscribeCtaVisibility(false)

        }
    }


    fun checkAndOpenManagedApps(
        context: Context?,
        fromScreen: String = SOURCE_NOTIFICATION,
        startPackListing: Boolean = false,
        source: String = APP_LAUNCH,
        journeyRef: String = "",
        journeyRefKey: String = "",
        skipDrawer: Boolean = false
    ) {


        viewModel.checkForManagedAppEligibility { eligible ->
            if (!eligible)
                startActivity(
                    getSubscriptionActivityIntent(
                        context = context,
                        startPackListing = startPackListing,
                        fromScreen = fromScreen
                    )
                ) else
                showMiniDrawer(
                    source = source,
                    journeyRef = journeyRef,
                    journeyRefKey = journeyRefKey,
                    skipDrawer = skipDrawer
                )
        }

    }

    fun showMiniDrawer(
        source: String = APP_LAUNCH,
        journeyRef: String = "",
        journeyRefKey: String = "",
        skipDrawer: Boolean = false/*this variable flag is to skip drawer when drawer with only CYOP is enabled*/
    ) {
        val currentTime = Calendar.getInstance().timeInMillis
        viewModel.sharedPrefs.setSubscriptionDrawerLastSeen(currentTime)
        var tempSkipDrawer=skipDrawer
        if (sharedPrefs.getConfigResponse()?.data?.config?.enableTickTickJourney == true) {
            tempSkipDrawer=false
        }
        if (navDrawerViewModel.isRenew) {
            navDrawerViewModel.isRenew = false
            showSubscriptionBottomSheet(source, SCREEN_PLAN, skipDrawer = tempSkipDrawer)
        } else
            showSubscriptionBottomSheet(
                source,
                journeyRef,
                journeyRefKey,
                skipDrawer = tempSkipDrawer
            )
    }

    fun showMiniDrawerDefault(source: String = APP_LAUNCH) {
        sharedPrefs.getConfigResponse()?.data?.config?.let {
            if(sharedPrefs.isManagedAppEnabled()){
                if ((it.enableTickTickJourney && it.tickTickDrawerScreen?.openTickTickDrawer == true)
                    || (!it.enableTickTickJourney && it.tickTickFixedPlanDrawerScreen?.openTickTickDrawer == true)
                ) {
                    val currentTime = Calendar.getInstance().timeInMillis
                    viewModel.sharedPrefs.setSubscriptionDrawerLastSeen(currentTime)
                    showSubscriptionBottomSheet(source)
                }
            }else{
                val currentTime = Calendar.getInstance().timeInMillis
                viewModel.sharedPrefs.setSubscriptionDrawerLastSeen(currentTime)
                showSubscriptionBottomSheet(source, openNativeDrawer = true)
            }
        }
    }

    private fun setObservers() {
        lifecycleScope.launchWhenStarted {
            dataStorePrefs.getGoogleOrFacebookDeferredDeeplinkUriInString().collectLatest {
                //Collect the latest Deferred deeplink
                it?.let {
                    d("Deeplink", "Deferred deeplink value: $it")
                    DeeplinkHelper.createApplinkFromCustomDeeplinkURI(it)?.let { deeplinkBundle ->
                        val localIntent = Intent(Intent.ACTION_VIEW)
                        localIntent.data = deeplinkBundle.first
                        localIntent.putExtras(deeplinkBundle.second)
//                        delay(100L) //Let everything setup on this activity with little delay
                        handleDeeplink(localIntent)
                    }
                    dataStorePrefs.saveGoogleOrFacebookDeferredDeeplinkUriInString(null)
                }
            }
        }

        viewModel.errorMessage.observe(this, Observer {
            it.getContentIfNotHandled().let { t ->
                if(t?.code == CODE_LOGOUT_ALL)
                    showAllDeviceLogoutDialog()
            }
        })

        viewModel.getEligiblePacksResponse().observe(this, Observer {


            if(needToShowMiniDrawer) {
                needToShowMiniDrawer = false
                it?.getContentIfNotHandled()?.let { it1 ->
                    packList = it1
                    val timer =
                        sharedPrefs.getConfigResponse()?.data?.config?.subscriptionDrawer?.delayToOpenSubscriptionDrawer?.toLong()
                            ?: 0L
                    miniDrawerDisposable = Completable.timer(
                        timer,
                        TimeUnit.SECONDS,
                        AndroidSchedulers.mainThread()
                    )
                        .subscribe {
                            this.showMiniDrawerDefault(APP_LAUNCH)
                        }
                }
            }
        })

        /*navDrawerViewModel: It extends from CancellationBaseViewModel, To centralize the
        payload observe using CancellationBaseViewModel here and in Subscription and Payment Activity*/
        navDrawerViewModel.getJuspayInitiatePayload().observe(this, Observer {
            it.getContentIfNotHandled()?.let { juspayInitiatePayload ->
                initiateJusPay(
                    this,
                    juspayInitiatePayload,
                    viewModel.sharedPrefs.getDthStatusFreemium()
                )
            }
        })
    }

    var fiberDialogShowing=false
    fun showWelcomeDialog(){
        if (!fiberDialogShowing) {
            sharedPrefs.getSubscribedPack()?.let { currentPack ->
                Handler(Looper.getMainLooper()).postDelayed({
                    if (currentPack.showFibreMsg && sharedPrefs.isFirstTimeLanguagePopUpShown() == false && !sharedPrefs.getWelcomeDialogStatus()) {
                        currentPack.fibreDetails?.let {
                            welcomeDialog?.dismissAllowingStateLoss()
                            welcomeDialog = WelcomeMessageDialog()
                            welcomeDialog?.show(
                                supportFragmentManager,
                                "welcome dialog"
                            )
                            fiberDialogShowing = true
                            sharedPrefs.setWelcomeDialogStatus(true)
                        }

                    }
                }, 200)

            }
        }
    }

    private fun showSubscriptionBottomSheet(
        source: String,
        journeyRef: String = "",
        journeyRefKey: String = "",
        skipDrawer: Boolean = false,
        openNativeDrawer: Boolean = false
    ) {

        if (!sharedPrefs.isFirstTimeLanguagePopUpShown()) {
            try {
                if(openNativeDrawer){
                    subscriptionBottomSheetDialog?.dismissAllowingStateLoss()
                    subscriptionBottomSheetDialog =
                        SubscriptionBottomContainerFragment().newInstance(source)
                    subscriptionBottomSheetDialog?.show(
                        supportFragmentManager,
                        "subscription bottomsheet"
                    )

                }else{
                    Handler(Looper.getMainLooper()).postDelayed({
                        currentJourneyRef = journeyRef
                        currentJourneyRefKey = journeyRefKey
                        currentNavController?.value?.navigateSafe(
                            HomeDirections.actionManagedApps(
                                source=source,
                                journeySource=journeyRef,
                                journeySourceRefId= journeyRefKey,
                                skipDrawer = skipDrawer
                            )
                        )
                    }, 200)
                }

            } catch (e: Exception) {

            }
        }
    }

    private fun setListeners() {
        btn_subscribe.text = viewModel.getConfigFromPreference()?.hamburger?.subscribe ?: getString(
            R.string.subscribe
        )
        btn_subscribe.setOnClickListener(object : SingleClickListener() {
            override fun onClicked(v: View?) {
                onGoVipClicked(true)
            }
        })
    }

    fun handleDeeplink(localIntent: Intent? = null) {
        handleCustomDeeplink(this, localIntent, viewModel, currentNavController) { deeplinkAction ->
            when (deeplinkAction) {
                DEEPLINK_ACTION_STOP_MINI_DRAWER_TIMER -> stopMiniDrawerTimer()
                DEEPLINK_ACTION_CHECK_DTH_STATUS_AND_RECHARGE -> checkDTHStatusAndOpenRecharge()
                else -> Unit
            }
        }
    }

    private fun checkDTHStatusAndOpenRecharge() {
        if (viewModel.isDTHUser())
            navDrawerViewModel.startRecharge(getSourceOrFromScreenName())
    }

    private fun handleFSFlow() {
        if (intent?.getStringExtra(KEY_FROM_SCREEN) != SOURCE_NOTIFICATION && intent?.getStringExtra(
                KEY_FROM_SCREEN
            ) != SOURCE_NOTIFICATION_ERROR
            && !intent.getBooleanExtra("checkPaymentStatus", false)
        ) {
//            showFirestickDialogOnUI()
            showFirestickOfferDialogByFrequency()
        }
    }
    var recommendationDialog : FirestickDialog? = null

    fun showFirestickOfferDialogByFrequency(ignoreFrequency : Boolean = false) {
        if(!isManagedAppOpen())
            Handler(Looper.getMainLooper()).postDelayed(
            {

                val isPackExpired = viewModel.sharedPrefs.getSubscribedPack()?.isInactive == true
                val firestickDialogVisibilityType = viewModel.sharedPrefs.getFirestickDialogVisibilityType()
                val eligibleFirestick = viewModel.sharedPrefs.getSubscribedPack()?.eligibleFirestick
                val isFSRequestRaised = viewModel.sharedPrefs.getSubscribedPack()?.isFSRequestRaised ?: false
                val isAlreadytakenFirestick = viewModel.sharedPrefs.isFirestickTaken()
                e("FTVPopup","firestickDialogVisibilityType: $firestickDialogVisibilityType")
                if (recommendationDialog == null
                    && !isPackExpired
                    && !isFSRequestRaised
                    && !isAlreadytakenFirestick
                    && eligibleFirestick == true
                    && sharedPrefs.getSubscribedPack()?.downgradeRequested != true
                ) {
                    when (firestickDialogVisibilityType) {
                        KEY_DIALOG_VISIBILITY_TYPE_TIME -> {
                            val lastTime = viewModel.sharedPrefs.getFirestickDialogLastVisibleTime()
                            val timeFrequency = viewModel.sharedPrefs.getFirestickDialogTimeFrequency()
                            val currentTime = Calendar.getInstance().timeInMillis
                            if (lastTime == 0L || (lastTime > 0 && timeFrequency > 0 && (currentTime - lastTime) >= (timeFrequency * 3600000))) {
                                showFirestickDialogOnUI()
                                viewModel.sharedPrefs.setFirestickDialogLastVisibleTime(currentTime)
                            }
                        }
                        KEY_DIALOG_VISIBILITY_TYPE_EVENT -> {
                            val launchValue = viewModel.sharedPrefs.getAppLaunchValue()
                            val launchFrequency = viewModel.sharedPrefs.getFirestickDialogLaunchFrequency()
                            if (launchValue == 0 || launchValue % launchFrequency == 0) {
                                showFirestickDialogOnUI()
                            }
                        }
                        KEY_DIALOG_VISIBILITY_TYPE_TIME_EVENT -> {
                            val launchValue = viewModel.sharedPrefs.getAppLaunchValue()
                            val launchFrequency = viewModel.sharedPrefs.getFirestickDialogLaunchFrequency()
                            e("FTVPopup","launchValue: $launchValue, " +
                                    "launchFrequency $launchFrequency, " +
                                    "viewModel.sharedPrefs.getFirestickDialogFirstVisbileTime(): ${viewModel.sharedPrefs.getFirestickDialogFirstVisbileTime()}")
                            if (viewModel.sharedPrefs.getFirestickDialogFirstVisbileTime() > 0) {
                                if (launchValue == 0 || launchValue % launchFrequency == 0) {
                                    showFirestickDialogOnUI()
                                }
                            } else {
                                viewModel.sharedPrefs.setFirestickDialogFirstVisbileTime(Calendar.getInstance().timeInMillis)
                                showFirestickDialogOnUI()
                            }
                        }
                    }
                }

            },200)
    }

    var showFsAfterFiber=false
    fun showFirestickDialogOnUI() {
        if(fiberDialogShowing)
        {
            showFsAfterFiber=true
            return
        }else{
            showFsAfterFiber=false
        }
        subscriptionAnalytics.trackUpSellView(sharedPrefs.getSubscribedPack()?.amountValue ?: "")
        subscriptionAnalytics.trackUpSellConverted(
            sharedPrefs.getPreviousSubscribedPack()?.amountValue.toString(),
            sharedPrefs.getSubscribedPack()?.amountValue.toString()
        )
        try {
            recommendationDialog = FirestickDialog.newInstance(
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        recommendationDialog?.dismiss()
//                        recommendationDialog = null
                        startActivity(Intent(this@LandingActivity, FSInstallationActivity::class.java))
                        //findNavController().navigateSafe(HomeFragmentDirections.actionHomeFragmentToFirestickJourney())
                    }

                    override fun onSecondaryButtonClick() {
                        recommendationDialog?.dismiss()
//                        recommendationDialog = null
                    }

                    override fun onCloseButtonClick() {
                    }

                }
            )
            recommendationDialog?.show(supportFragmentManager, DIALOG_TAG)
        } catch (e : Exception){ }
    }

    private fun handleDrawerUI() {
        var width = getDisplayMatics().widthPixels - dpToPx(this, 60)
        if(isTablet(this)){
            width = if(this.resources.configuration.orientation== Configuration.ORIENTATION_PORTRAIT)
                (getDisplayMatics().widthPixels * 0.5).toInt()
            else (getDisplayMatics().widthPixels * 0.3).toInt()
        }

        val layoutParams = DrawerLayout.LayoutParams(width,
            MATCH_PARENT)
        layoutParams.gravity = GravityCompat.START
        navDrawer.layoutParams = layoutParams
    }



    override fun getViewModelClass(): Class<CommonSampleViewModel> =
        CommonSampleViewModel::class.java

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        intent.data = null
        setupDrawerLayout()
        setupBottomNavigationBar()
    }

    override fun allowedTouchWhenLoading(): Boolean {
        return allowTouches
    }

    override fun onDestroy() {
        super.onDestroy()
        hideNudges()
        localBroadcastHelper.unregisterBroadcast(this, mNotificationReceiver)
        terminateJuspayService(this)
    }

    fun getPageName():String?{
        val bottomNavigationView: BottomNavigationView? =
            findViewById<BottomNavigationView?>(R.id.bottomNav)
        val selectedItemId: Int? = bottomNavigationView?.selectedItemId
        val selectedItem: MenuItem? = selectedItemId?.let { bottomNavigationView.menu.findItem(it) }
        return selectedItem?.title.toString()
    }

    fun setSelectedTab(tabId: Int) {
        val bottomNavBar: BottomNavigationView? =
            findViewById<BottomNavigationView?>(R.id.bottomNav)
        bottomNavBar?.selectedItemId = tabId
    }

    fun triggerHomeScrollTop() {
        viewModel.fakeRefreshHome.postValue(SingleEvent(true))
    }

    private fun setupDrawerLayout() {
        drawerLayout = root_container_drawer
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        actionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.nav_open,
            R.string.nav_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupBottomNavigationBar() {
        val navGraphIds = listOf<Int>(
            R.navigation.nav_home,
            R.navigation.nav_movies,
            R.navigation.nav_shows,
            R.navigation.nav_sports,
            R.navigation.nav_game_tab
        )


        /*for (position in 0..4) {
            packUpdateStatus.put(bottomNav.menu.getItem(position).itemId, false)
        }*/

        val bottomNavBar: BottomNavigationView? = findViewById<BottomNavigationView?>(R.id.bottomNav)
        val pair = bottomNavBar?.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.fragment_container,
            intent = intent,
            reselectedFunction = { viewModel.setReselectedTab() },
            homeSelectedFunction = { viewModel.setHomeSelectedTab() },
            sharedPrefs = sharedPrefs,
            interruptSelectedBottomTab = {
                    source, selectedTabId ->
                when (source) {
                    InterruptedBottomTabConstants.SOURCE_CATEGORIES -> {
                        showCategoryBottomSheet()
                    }
                }
            }
        )
        currentNavController = pair?.first
        currentMenuItemId = pair?.second

        viewModel.getReselected().observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                if (it) {
                    triggerHomeScrollTop()
                }
            }
        })


        currentNavController?.observe(this, Observer {
            it?.let {
                val appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.action_sub_landing_home,
                        R.id.watchlistFragment,
                        R.id.detailsFragment,
                        R.id.searchFragment,
                        R.id.action_account_landing,
                        R.id.moreFragment,
                        R.id.voiceSearchDialog
                    )
                )
                try {
                    findViewById<Toolbar>(R.id.toolbar)
                        .setupWithNavController(it, appBarConfiguration)
                } catch (e: Exception) {
                }
                it.removeOnDestinationChangedListener(navDestinationChangedListener)
                it.addOnDestinationChangedListener(navDestinationChangedListener)
                it.currentBackStackEntry?.savedStateHandle
                    ?.getLiveData<ParentalControlBottomSheetResult>(
                        KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT
                    )
                    ?.observe(this) { result ->
                        if (!isResultHandled) {
                            isResultHandled = true
                            when (result.resultStatus) {
                                ParentalControlBottomSheetResultStatus.SUCCESS_DISMISS -> {
                                    when (result.actionBeforeOpeningBottomSheet) {
                                        ACTION_PIN_FORGOT -> {

                                            val toastView =
                                                DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                                                    LayoutInflater.from(this),
                                                    R.layout.layout_toast_success_failure,
                                                    null,
                                                    false
                                                )
                                            toastView.textLoginSuccessfulToast.text =
                                                getString(R.string.toast_msg_pin_changed_successful)
                                            toastView.imageTickLoginSuccessfulToast.setImageResource(
                                                R.drawable.ic_tick_login_success
                                            )
                                            showCustomToast(
                                                this,
                                                toastView?.root,
                                                Gravity.FILL_HORIZONTAL
                                            )
                                        }
                                    }
                                }
                                ParentalControlBottomSheetResultStatus.PIN_VERIFIED -> {
                                    when (result.actionBeforeOpeningBottomSheet) {
                                        ACTION_PIN_VERIFICATION -> {
                                            navDrawerViewModel.navDrawerAction.value?.peekContent()
                                                ?.let { navDrawerActions ->
                                                    when (navDrawerActions) {
                                                        is NavDrawerActions.Recharge -> {
                                                            navDrawerViewModel.startRecharge(getSourceOrFromScreenName())
                                                        }
                                                        is NavDrawerActions.Renew -> {
                                                            val currentPack =
                                                                sharedPrefs.getSubscribedPack()
                                                            if (currentPack?.planCTADetails?.getPlanOption == true) {


                                                                checkAndOpenManagedApps(
                                                                    context = this,
                                                                    startPackListing = true,
                                                                    fromScreen = getSourceOrFromScreenName(),
                                                                    source = HAMBURGER_SUBSCRIBE_CTA
                                                                )

                                                            } else {
                                                                if (currentPack?.migrated == true) {
                                                                    showDialog(
                                                                        DialogModel(
                                                                            false,
                                                                            null,
                                                                            currentPack.migratedVerbiage,
                                                                            getString(R.string.ok),
                                                                            null
                                                                        ),
                                                                        object :
                                                                            CommonDialogEventListener {
                                                                            override fun onPrimaryButtonClick() {
                                                                                viewModel.setErrorOkClicked()
                                                                                hideDialog()
                                                                            }

                                                                            override fun onCloseButtonClick() {
                                                                                hideDialog()
                                                                            }

                                                                            override fun onSecondaryButtonClick() {
                                                                            }
                                                                        })
                                                                } else {
                                                                    if (currentPack != null) {
                                                                        currentPack.tenure?.find {
                                                                            it.currentTenure == true
                                                                        }?.let {
                                                                            startActivity(
                                                                                getPaymentActivityIntent(
                                                                                    context = this,
                                                                                    packId = currentPack.productId,
                                                                                    selectedTenureId = it.tenureId,
                                                                                    selectedTenureAmount = it.offeredPriceValue,
                                                                                    isMigrated = currentPack.migrated,
                                                                                    migratedVerbiage = currentPack.migratedVerbiage,
                                                                                    proratedAmount = null,
                                                                                    fromScreen = getSourceOrFromScreenName(),
                                                                                    sharedPrefs = sharedPrefs
                                                                                ).apply {
                                                                                    putExtra(
                                                                                        KEY_IS_RENEW,
                                                                                        true
                                                                                    )
                                                                                }
                                                                            )
                                                                        } ?: run {


                                                                            startActivity(
                                                                                getPaymentActivityIntent(
                                                                                    context = this,
                                                                                    packId = currentPack.productId,
                                                                                    selectedTenureId = currentPack.productId,//if no current tenure found then send the current pack product id
                                                                                    selectedTenureAmount = currentPack.amountValue,
                                                                                    isMigrated = currentPack.migrated,
                                                                                    migratedVerbiage = currentPack.migratedVerbiage,
                                                                                    proratedAmount = null,
                                                                                    fromScreen = getSourceOrFromScreenName(),
                                                                                    sharedPrefs = sharedPrefs
                                                                                ).apply { putExtra(
                                                                                    KEY_IS_RENEW, true) }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        is NavDrawerActions.GoVipClicked -> {

                                                            stopMiniDrawerTimer()

                                                            checkAndOpenManagedApps(
                                                                context = this,
                                                                startPackListing = true,
                                                                fromScreen = getSourceOrFromScreenName(),
                                                                source = HOME_SUBSCRIBE_CTA,
                                                                skipDrawer = true,
                                                                journeyRef = DRAWER_CYOP
                                                            )



                                                        }
                                                        is NavDrawerActions.MyPlanClicked -> {
                                                            stopMiniDrawerTimer()
                                                            startActivity(
                                                                getSubscriptionActivityIntent(
                                                                    this,
                                                                    startPackListing = false,
                                                                    fromScreen = getSourceOrFromScreenName()
                                                                )
                                                            )
                                                        }
                                                        else -> {
                                                            stopMiniDrawerTimer()

                                                            checkAndOpenManagedApps(
                                                                context = this,
                                                                startPackListing = true,
                                                                fromScreen = getSourceOrFromScreenName(),
                                                                source = HOME_SUBSCRIBE_CTA,
                                                                skipDrawer = true,
                                                                journeyRef = DRAWER_CYOP
                                                            )

                                                        }
                                                    }
                                                } ?: run{
                                                stopMiniDrawerTimer()
                                                checkAndOpenManagedApps(
                                                    context = this,
                                                    startPackListing = true,
                                                    fromScreen = getSourceOrFromScreenName(),
                                                    source = HOME_SUBSCRIBE_CTA,
                                                    skipDrawer = true,
                                                    journeyRef = DRAWER_CYOP
                                                )
                                            }
                                        }
                                    }
                                }
                                ParentalControlBottomSheetResultStatus.DISMISS -> {
                                    when (result.actionBeforeOpeningBottomSheet) {
                                        ACTION_PIN_VERIFICATION -> {
                                            //show error message here
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        })

        val iconSearch: ImageView = findViewById(R.id.search_icon)
        iconSearch.setOnClickListener {
            viewModel.sharedPrefs.enableHomeScreenSearchCoachMark(false)
            parentalControlSnackbarUtil.hideParentalControlSnackbar()
            findNavController(R.id.fragment_container).navigate(R.id.action_global_search)
        }
        val iconHamberger: ImageView = findViewById(R.id.hamberger_menu)
        iconHamberger.setOnClickListener {
            if (progressBar.isVisibile())
                return@setOnClickListener
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            if (sharedPrefs.getLoginStatus()) {
                navDrawerViewModel.updateLoggedOutState(false)
                if(sharedPrefs.getFetchedProfileData() != null) {
                    navDrawerViewModel.setFromSavedProfileInfo()
                }
                else{
                    navDrawerViewModel.fetchProfileInfo()
                }
                if(sharedPrefs.getFetchedBalanceData() != null) {
                    navDrawerViewModel.setFromSavedWalletBalance()
                }
                else{
                    navDrawerViewModel.fetchBalance()
                }
            } else {
                navDrawerViewModel.updateLoggedOutState(true)
            }
//           Adding unread notification messages only for Clevertap in order to show on Badge count
           navDrawerViewModel.getUnReadCleverTapNotificationCount(this)
//            navDrawerViewModel.getUnReadCleverTapNotificationCount(context = this)
            navDrawerViewModel.getSubscription()
            miscAnalytics.trackMixPanelMenuClick()
            drawerLayout.open()
        }

        viewModel?.deviceForceLogout()?.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                logoutApplication(this)
                if(it)  localBroadcastHelper.sendBroadcast(this, localBroadcastHelper.ACTION_LOGOUT)
            }
        })
        /*viewModel.getSelectedCategory().observe(this) {
            it.peekContent()?.let { pair ->
                if (currentMenuItemId?.value != R.id.others) {
                    viewModel.sharedPrefs.setInterruptCategoryTabStatus(false)
                    setSelectedTab(R.id.others)
                }
                categoryPageName = pair.first
                categoryPageType = pair.second
                categoryBottomSheetDialog?.dismiss()
            }
        }*/
    }

    fun showCategoryBottomSheet() {
        viewModel.getCategoryList().value?.peekContent()?.let {
            homeAnalytics.trackBottomTabItemSelected(SOURCE_CATEGORY)
            categoryBottomSheetDialog?.dismiss()
            categoryBottomSheetDialog = CategoriesBottomSheetDialog(it)
            categoryBottomSheetDialog?.show(supportFragmentManager, "category bottomsheet")
            return
        }
        viewModel.fetchCategories()
    }

    /**
     * ID of the XML Content view to be set for this activity
     */
    override fun getContentViewId(): Int = R.layout.activity_home

    /**
     * Root layout of this activity
     */
    override fun getRootLayoutContainer(): View = root_container

    override fun onSupportNavigateUp(): Boolean {
        return (currentNavController?.value?.navigateUp() ?: false) || super.onSupportNavigateUp()
    }


    fun showGameBottomAnim(){
        if(gameLottieVisible && bottomNavShowing)
            lv_game_tab.show()
    }
    fun hideGameBottomAnim(){
        lv_game_tab.invisible()
    }

    fun showBottomNav() {
        e("bottomListener","inside showBottomNav $bottomNavShowing")
        if(isManagedAppOpen() || isScreenFullScreenMode) return
        root_container.post {
            if (!bottomNavShowing) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        TransitionManager.beginDelayedTransition(
                            root_container,
                            TransitionInflater.from(this)
                                .inflateTransition(R.transition.default_transition)
                        )
                        val set = ConstraintSet()
                        set.clone(root_container)
                        set.clear(bottomNav.id, ConstraintSet.TOP)
                        set.connect(
                            bottomNav.id, ConstraintSet.BOTTOM,
                            ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM
                        )
                        set.applyTo(root_container)
                        bottomNavShowing = true
                        bottomNav.show()
                        showGameBottomAnim()
                    }, 100
                )
            }
        }
    }

    fun hideBottomNav() {
        e("bottomListener","inside hideBottomNav $bottomNavShowing")
        root_container.post {
            if (bottomNavShowing) {
                hideGameBottomAnim()
                TransitionManager.beginDelayedTransition(
                    root_container,
                    TransitionInflater.from(this).inflateTransition(R.transition.default_transition)
                )
                val set = ConstraintSet()
                set.clone(root_container)
                set.clear(bottomNav.id, ConstraintSet.BOTTOM)
                set.connect(
                    bottomNav.id, ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM
                )
                set.applyTo(root_container)
                bottomNavShowing = false
                bottomNav.hide()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)
        fragment?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        e("LandingActivity", "insdie onNewIntent")
        setIntent(intent)
        if (intent.getBooleanExtra("checkPaymentStatus", false)) {
            checkPaymentStatus()
        }
        else if (!intent.getBooleanExtra("silentLogin", false)) {
            handleFSFlow()
        }
        recommendationDialog = null
        setupDrawerLayout()
        setupBottomNavigationBar()
        viewModel.setTotalUnreadCount()
    }


    private fun checkPaymentStatus() {
        shouldTriggerInMixpanel = true
        sharedPrefs.getLastPgSdkProcessStatus()?.let {
            pgResponseCode = it.uppercase()
            pgPaymentStatus = pgResponseCode.getPgPaymentStatus()
            sharedPrefs.saveLastPgSdkProcessStatus(null)
        }
        paymentInfoBundle = intent.getBundleExtra("bundle")
        if (paymentInfoBundle?.getBoolean("upFrontMoneyCollected") == true) {
//            showPaymentSuccessful()
            setPaymentProgressing(false)
            viewModel.fetchCurrentSubscription(true)
        } else {
            setPaymentProgressing(true)
            val orderId = paymentInfoBundle?.getString("orderId")
            mHandler.postDelayed(
                {
                    viewModel.fetchPaymentStatus(orderId ?: "")
                }, paymentCallbackArrayInfo!![0]
            )
        }
    }

    private fun BottomNavigationView.setUpLottieAnimation(lottieView: LottieAnimationView, position: Int) {
        if (intent?.extras?.getString(KEY_FROM_SCREEN).equals(SOURCE_DEEPLINK)) return
        Handler(Looper.getMainLooper()).postDelayed({
            val item = this.menu.getItem(position)
            lv_game_tab.viewTreeObserver.addOnGlobalLayoutListener {
                when (lv_game_tab.visibility) {
                    View.VISIBLE -> {
                        this.findViewById<View>(bottomNav.menu.getItem(position).itemId).visibility = View.INVISIBLE
                    }
                    View.GONE -> {
                        this.findViewById<View>(bottomNav.menu.getItem(position).itemId).visibility = View.VISIBLE
                    }
                    View.INVISIBLE -> {
                        this.findViewById<View>(bottomNav.menu.getItem(position).itemId).visibility = View.VISIBLE
                    }
                }
            }
            this.post(Runnable {
                this.findViewById<View>(item.itemId).visibility = View.INVISIBLE
                lottieView.setOnClickListener {
                    if (viewModel.sharedPrefs.isGameHapticFeedbackEnabled()) {
                        viewModel.sharedPrefs.enableGameHapticFeedback(false)
                        vibratePhone(this.context, 100L)
                    }
                    setSelectedTab(R.id.gametab)
                    hideGameBottomAnim()
                    gameLottieVisible = false
                }
                gameLottieVisible = true
                calculateGameAnimCoordinates(context, position, lottieView, this)
            })
        },200)
    }

    fun BottomNavigationView.fixBlinking(context: Context) {
        try {
            val menuView = getChildAt(0) as BottomNavigationMenuView
            val iconView: View = menuView.getChildAt(4)
                .findViewById(com.google.android.material.R.id.icon) as View

            val layoutParams: ViewGroup.LayoutParams = iconView.getLayoutParams();
            // set your height here
            layoutParams.height = dpToPx(context, 24)
            layoutParams.width = dpToPx(context, 32)
            // set your width here
            iconView.layoutParams = layoutParams
            with(menuView::class.java.getDeclaredField("set")) {
                isAccessible = true
                val transitionSet = (get(menuView) as AutoTransition).apply {
                    for (i in transitionCount downTo 0) {
                        val transition = getTransitionAt(i) as? Fade ?: continue
                        removeTransition(transition)
                    }
                }
                set(menuView, transitionSet)
            }
        } catch (e: Exception) {
        }
    }

    fun BottomNavigationView.hideToolTip() {
        bottomNav.menu.forEach {
            val view = bottomNav.findViewById<View>(it.itemId)
            view.setOnLongClickListener {
                true
            }
            view.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                    v.isPressed = false  // To preserve ripple effect
                    if (it.itemId.equals(R.id.gametab) && viewModel.sharedPrefs.isGameHapticFeedbackEnabled()) {
                        viewModel.sharedPrefs.enableGameHapticFeedback(false)
                        vibratePhone(context, 100L)
                    }
                }
                if (event.action == MotionEvent.ACTION_DOWN) {
                    v.isPressed = true
                }
                true
            }

        }
    }

    override fun onRestart() {
        super.onRestart()
//        pubnubHelper.getLastStatus(null)
    }



    override fun onResume() {
        super.onResume()
        setupBottomNavigationBar()
        try {
            viewModel.isCCTOpen = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    val mNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (localBroadcastHelper.ACTION_NOTIFICATION_RECEIVED == intent.action) {
                viewModel.setTotalUnreadCount()
            }
            if (localBroadcastHelper.ACTION_LOGOUT == intent.action || localBroadcastHelper.ACTION_DEVICE_STATUS_LOGOUT == intent.action || localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH == intent.action) {
                try {
                    if (viewModel.isCCTOpen) {
                        val myIntent = Intent(context, LandingActivity::class.java)
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(myIntent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (localBroadcastHelper.ACTION_PAYMENT_UPDATED == intent.action && this@LandingActivity.intent.getBooleanExtra(
                    "checkPaymentStatus",
                    false
                )
            ) {
                this@LandingActivity.intent.putExtra("checkPaymentStatus",false) // to avoid multiple payment success dialog TSF-7544
                //show payment success dialog
                viewModel.disposePaymentStatus()
                setPaymentProgressing(false)
//                showPaymentSuccessful()
                setPaymentProgressing(false)
                viewModel.fetchCurrentSubscription(true)
            }
            if(localBroadcastHelper.ACTION_SUBSCRIPTION_UPDATED == intent.action){
                navDrawerViewModel.getSubscription()
            }
        }
    }

    private fun initErosNow() {
        /*Initialization of ErosNow*/
        val configuration = ENConfiguration(
            erosNowEnvironment = if (BuildConfig.FLAVOR == "uat")
                ErosNowEnvironment.STAGING
            else
                ErosNowEnvironment.PRODUCTION,
            partnerCode = BuildConfig.EROSNOW_PARTNER_CODE,
            apiClientId = BuildConfig.EROSNOW_API_CLIENTID,
            country = "IN",
            deviceId = DeviceInfoUtils.getDeviceId(this)
        )
        ENSDK.setup(this, configuration)
    }

    override fun onUserLeaveHint() {
        val navHostFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            childFragments.forEach { fragment ->
                if (fragment is TTNPlayerFragment)
                    fragment.onUserLeaveHint()
            }
        }
        super.onUserLeaveHint()
    }

    private fun handleNavDrawerActions() {
        navDrawerViewModel.navDrawerAction.observe(this) {
            it.getContentIfNotHandled()?.let { navDrawerAction ->
                drawerLayout.close()
                when (navDrawerAction) {
                    is NavDrawerActions.LoginClicked -> {
                        stopMiniDrawerTimer()
                        viewModel.getPreviouslyUsedMobileNumbers()
                    }
                    is NavDrawerActions.EditProfile -> {
                        if (viewModel.isLoggedIn()) {
                            val userData = SubscriberProfileListModel.Data()
                            viewModel.sharedPrefs.getSelectedProfile().let { savedUserData ->
                                userData.email = savedUserData?.emailId
                                userData.firstName = savedUserData?.firstName
                                userData.lastName = savedUserData?.lastName
                                userData.rmn = savedUserData?.rmn
                                userData.image = savedUserData?.imageUrl
                            }
                            findNavController(R.id.fragment_container).navigate(
                                R.id.action_global_editProfileFragment,
                                Bundle().apply { putParcelable("profileModel", userData) })
                        } else {
                            stopMiniDrawerTimer()
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    is NavDrawerActions.Recharge -> {
//                        startActivity(Intent(this, FreemiumSubscriptionActivity::class.java))
                        if (sharedPrefs.isParentalPinExists()
                            && sharedPrefs.getParentalRating()?.ageRatingName.takeIf { rating ->
                                rating == null || rating == getString(R.string.no_restrictions)
                            } == null
                        ) {
                            isResultHandled = false
                            findNavController(R.id.fragment_container).navigateSafe(
                                R.id.action_global_parentalControlBottomDialogFragment,
                                Bundle().apply {
                                    putString(
                                        "actionBeforeOpeningBottomSheet",
                                        ACTION_PIN_VERIFICATION
                                    )
                                    putString("ageRatingValue", null)
                                    putString("source", EVENT_RECHARGE)
                                }
                            )
                        } else {
                            navDrawerViewModel.startRecharge(getSourceOrFromScreenName())
                        }
                    }
                    is NavDrawerActions.Renew -> {
                        if (sharedPrefs.isParentalPinExists()
                            && sharedPrefs.getParentalRating()?.ageRatingName.takeIf { rating ->
                                rating == null || rating == getString(R.string.no_restrictions)
                            } == null
                        ) {
                            isResultHandled = false
                            findNavController(R.id.fragment_container).navigateSafe(
                                R.id.action_global_parentalControlBottomDialogFragment,
                                Bundle().apply {
                                    putString(
                                        "actionBeforeOpeningBottomSheet",
                                        ACTION_PIN_VERIFICATION
                                    )
                                    putString("ageRatingValue", null)
                                    putBoolean("fromNudge",true)
                                    putString("source", EVENT_MY_PLAN)
                                }
                            )
                        } else {
                            val currentPack = sharedPrefs.getSubscribedPack()
                            if(currentPack?.planCTADetails?.getPlanOption == true){
                                stopMiniDrawerTimer()

                                checkAndOpenManagedApps(
                                    context = this,
                                    startPackListing = true,
                                    fromScreen = getSourceOrFromScreenName(),
                                    source = HAMBURGER_SUBSCRIBE_CTA
                                )

                            } else{
                                if(currentPack?.migrated == true){
                                    showDialog(
                                        DialogModel(false, null, currentPack.migratedVerbiage, getString(R.string.ok), null),
                                        object : CommonDialogEventListener {
                                            override fun onPrimaryButtonClick() {
                                                viewModel.setErrorOkClicked()
                                                hideDialog()
                                            }

                                            override fun onCloseButtonClick() {
                                                hideDialog()
                                            }

                                            override fun onSecondaryButtonClick() {
                                            }
                                        })
                                }else{
                                    if (currentPack != null) {
                                        currentPack.tenure?.find {
                                            it.currentTenure == true
                                        }?.let {

                                            startActivity(
                                                getPaymentActivityIntent(
                                                    context = this,
                                                    packId = currentPack.productId,
                                                    selectedTenureId = it.tenureId,
                                                    selectedTenureAmount = it.offeredPriceValue,
                                                    isMigrated = currentPack.migrated,
                                                    migratedVerbiage = currentPack.migratedVerbiage,
                                                    proratedAmount = null,
                                                    fromScreen = getSourceOrFromScreenName(),
                                                    sharedPrefs = sharedPrefs
                                                ).apply {
                                                    putExtra(
                                                        KEY_IS_RENEW,
                                                        true
                                                    )
                                                }
                                            )
                                        }?:run{

                                            startActivity(
                                                getPaymentActivityIntent(
                                                    context = this,
                                                    packId = currentPack.productId,
                                                    selectedTenureId = currentPack.productId, //if no current tenure found then send the current pack product id
                                                    selectedTenureAmount = currentPack.amountValue,
                                                    isMigrated = currentPack.migrated,
                                                    migratedVerbiage = currentPack.migratedVerbiage,
                                                    proratedAmount = null,
                                                    fromScreen = getSourceOrFromScreenName(),
                                                    sharedPrefs = sharedPrefs
                                                ).apply {
                                                    putExtra(
                                                        KEY_IS_RENEW,
                                                        true
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is NavDrawerActions.GoVipClicked -> {
                        onGoVipClicked()
                    }
                    is NavDrawerActions.MyPlanClicked ->{
                        onMyPlanClicked()
                    }
                    is NavDrawerActions.BingeListClicked -> {
                        if (viewModel.isLoggedIn()) {
                            findNavController(R.id.fragment_container).navigate(R.id.action_global_watchlist)
                        } else {
                            stopMiniDrawerTimer()
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    is NavDrawerActions.NotificationClicked -> {
                        if (viewModel.isLoggedIn()) {
                            findNavController(R.id.fragment_container).navigate(R.id.action_global_notifications)
                        } else {
                            stopMiniDrawerTimer()
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    is NavDrawerActions.SettingsClicked -> {
                        if (viewModel.isLoggedIn()) {
                            findNavController(R.id.fragment_container).navigate(R.id.action_global_settingsFragment)
                        } else {
                            stopMiniDrawerTimer()
                            viewModel.getPreviouslyUsedMobileNumbers()
                        }
                    }
                    is NavDrawerActions.HelpAndSupportClicked -> {
                        stopMiniDrawerTimer()
                        findNavController(R.id.fragment_container).navigate(R.id.action_global_help_and_faq)
                    }
                    is NavDrawerActions.TnCClicked -> {
                        stopMiniDrawerTimer()
                        findNavController(R.id.fragment_container).navigate(R.id.action_global_termsConditionFragment)
                    }
                    is NavDrawerActions.PrivacyPolicyClicked -> {
                        stopMiniDrawerTimer()
                        findNavController(R.id.fragment_container).navigate(R.id.action_global_privacyPolicyFragment)
                    }
                }
            }
        }
    }

    private fun onGoVipClicked(fromHeaderClick: Boolean=false){
//        if (viewModel.isLoggedIn()) {
        if (sharedPrefs.getLoginStatus() && sharedPrefs.isParentalPinExists()
            && sharedPrefs.getParentalRating()?.ageRatingName.takeIf { rating ->
                rating.isNullOrEmpty() || rating == getString(R.string.no_restrictions)
            } == null
        ) {
            isResultHandled = false
            findNavController(R.id.fragment_container).navigateSafe(
                R.id.action_global_parentalControlBottomDialogFragment,
                Bundle().apply {
                    putString(
                        "actionBeforeOpeningBottomSheet",
                        ACTION_PIN_VERIFICATION
                    )
                    putString("ageRatingValue", null)
                    putBoolean("fromNudge", true)
                    putString("source", EVENT_MY_PLAN)
                }
            )
        } else {
            stopMiniDrawerTimer()

            if (sharedPrefs.getConfigResponse()?.data?.config?.enableTickTickJourney == false) {
                if (fromHeaderClick) {

                    checkAndOpenManagedApps(
                        context = this,
                        startPackListing = true,
                        fromScreen = getSourceOrFromScreenName(),
                        source = HOME_SUBSCRIBE_CTA,
                        skipDrawer = true,
                        journeyRef = DRAWER_CYOP
                    )

                } else {

                    checkAndOpenManagedApps(
                        context = this,
                        startPackListing = true,
                        fromScreen = getSourceOrFromScreenName(),
                        source =  HAMBURGER_SUBSCRIBE_CTA,
                        skipDrawer = true,
                        journeyRef = DRAWER_CYOP
                    )

                }

            } else {


                if (fromHeaderClick) {

                    checkAndOpenManagedApps(
                        context = this,
                        startPackListing = true,
                        fromScreen = getSourceOrFromScreenName(),
                        source =  HOME_SUBSCRIBE_CTA
                    )

                } else {
                    checkAndOpenManagedApps(
                        context = this,
                        startPackListing = true,
                        fromScreen = getSourceOrFromScreenName(),
                        source =  HAMBURGER_SUBSCRIBE_CTA
                    )

                }

            }


        }
    }



    private fun onMyPlanClicked(){
        if (viewModel.isLoggedIn()) {
            if (sharedPrefs.isParentalPinExists()
                && sharedPrefs.getParentalRating()?.ageRatingName.takeIf { rating ->
                    rating.isNullOrEmpty() || rating == getString(R.string.no_restrictions)
                } == null
            ) {
                isResultHandled = false
                findNavController(R.id.fragment_container).navigateSafe(
                    R.id.action_global_parentalControlBottomDialogFragment,
                    Bundle().apply {
                        putString(
                            "actionBeforeOpeningBottomSheet",
                            ACTION_PIN_VERIFICATION
                        )
                        putString("ageRatingValue", null)
                        putString("source", EVENT_MY_PLAN)
                    }
                )
            } else {
                stopMiniDrawerTimer()
                startActivity(
                    getSubscriptionActivityIntent(
                        context = this,
                        isFromNudge = true,
                        startPackListing = false,
                        fromScreen = getSourceOrFromScreenName()
                    )
                )
            }
        } else {
            stopMiniDrawerTimer()
            viewModel.getPreviouslyUsedMobileNumbers()
        }
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    fun hideNudges() {
        customSnackbarWithTwoActionsUtil.hideCustomSnackbarWithTwoActions()
    }
    fun hideGameNudge(){
        customSnackbarGameNudge.hideCustomSnackbarWithTwoActions()

    }

    fun showGameNudge(){
        sharedPrefs.setGameNudgeShown(true)
        customSnackbarGameNudge.showCustomSnackbarWithTwoActions(
            context = this,
            snackbarType = CustomSnackbarWithTwoActionsType.SnackbarTypeGameNudge,
            mszTitle = sharedPrefs.getConfigResponse()?.data?.app?.gameNudgePopUp?.gameAndroid?.nudgeHeader?:"",
            mszDesc = sharedPrefs.getConfigResponse()?.data?.app?.gameNudgePopUp?.gameAndroid?.nudgeVerbiage?:"",
            imgResourceSmall = null,
            imgResourceLarge = null,
            imgResourceCancel = R.drawable.ic_cross,
            btnActionText = "btnText",
            maxProgress = 0,
            currProgress = 0,
            lambdaAction = {
                homeAnalytics.trackGameNotificationNudgeClick()
                customSnackbarGameNudge.hideCustomSnackbarWithTwoActions()
                setSelectedTab(R.id.gametab)
                viewModel.fakeRefreshHome.postValue(SingleEvent(true))
            },
            lambdaCancel = {
                customSnackbarGameNudge.hideCustomSnackbarWithTwoActions()
            }
        )
    }



    fun showNudges() {
        /**
         * nextNudgeGapMillis -> {0 => immediately show}, {-1 => never show}, {>0 => handle with last nudge shown time}
         * */
        fun hasLastNudgeShownTimePassed(
            lastNudgeTimeMillis: Long,
            nextNudgeGapMillis: Long
        ): Boolean {
            e("ShowNudges","cuurent ${Calendar.getInstance().timeInMillis } hasLastNudgeShownTimePassed lastNudgeTimeMillis:$lastNudgeTimeMillis, nextNudgeGapMillis:$nextNudgeGapMillis")
            return nextNudgeGapMillis >= 0
                    && Calendar.getInstance().timeInMillis - lastNudgeTimeMillis >= nextNudgeGapMillis
        }

        /**
         * for every new install/login, use launchFrequency for first nudge timings and use timeFrequency for repeated nudges
         * */
        fun conditionsToShowUpdateEmailNudge(nudgeData :ConfigResponse.UpsellNudge?): Boolean {
            val timeGap =
                if (sharedPrefs.getNudgeTimeUpdateEmail() != 0L) nudgeData?.launchFrequency
                else nudgeData?.timeFrequency

            val lastTime =
                if (sharedPrefs.getNudgeTimeUpdateEmail() == 0L) sharedPrefs.getLoginTimeStamp()
                else sharedPrefs.getNudgeTimeUpdateEmail()

            val conditions = sharedPrefs.getLoginStatus()
                    && sharedPrefs.getSelectedProfile()?.emailId.isNullOrEmpty()
            e("ShowNudges","conditions : $conditions conditionsToShowUpdateEmailNudge timeGap:$timeGap, lastTime:$lastTime")
            return conditions && hasLastNudgeShownTimePassed(
                lastTime,
                changeDaysToMillis(timeGap)
            )
        }

        fun conditionsToShowNotificationNudge(nudgeData: ConfigResponse.UpsellNudge?): Boolean {
            val timeGap =
                if (sharedPrefs.getNudgeTimeShowNotification() != 0L) nudgeData?.launchFrequency
                else nudgeData?.timeFrequency

            val lastTime =
                if (sharedPrefs.getNudgeTimeShowNotification() == 0L) sharedPrefs.getLoginTimeStamp()
                else sharedPrefs.getNudgeTimeShowNotification()

            val conditions = sharedPrefs.getLoginStatus()
                    && !sharedPrefs.getWatchNotificationAllowed()

            return conditions && hasLastNudgeShownTimePassed(
                lastTime,
                changeDaysToMillis(timeGap)
            )
        }

        fun conditionsToShowRenewPlanBeforeExpiryNudge(nudgeData: ConfigResponse.UpsellNudge?): Boolean {
            val timeGap =
                if (sharedPrefs.getNudgeTimeRenewalBeforeExpiry() != 0L) nudgeData?.launchFrequency
                else nudgeData?.timeFrequency

            val lastTime =
                if (sharedPrefs.getNudgeTimeRenewalBeforeExpiry() == 0L) sharedPrefs.getLoginTimeStamp()
                else sharedPrefs.getNudgeTimeRenewalBeforeExpiry()

            val subscribedPack = sharedPrefs.getSubscribedPack()
            val packConditions = subscribedPack?.fdoRequested != true
                    && subscribedPack?.isInactive == false
                    && subscribedPack.planCTADetails?.renewPlanOption == true
                    && subscribedPack.freeTrialStatus == false
                    && subscribedPack.subscriptionNudgeDetails?.nudgeToShowOnWhichDayBeforeExpiry ?: 0 >= subscribedPack.subscriptionNudgeDetails?.currentDay?.absoluteValue ?: 0

            val conditions = sharedPrefs.getLoginStatus()
                    && packConditions

            return conditions && hasLastNudgeShownTimePassed(
                lastTime,
                changeDaysToMillis(timeGap)
            )
        }

        fun conditionsToShowRenewPlanAfterExpiryNudge(nudgeData: ConfigResponse.UpsellNudge?): Boolean {
            val timeGap =
                if (sharedPrefs.getNudgeTimeRenewalAfterExpiry() != 0L) nudgeData?.launchFrequency
                else nudgeData?.timeFrequency

            val lastTime =
                if (sharedPrefs.getNudgeTimeRenewalAfterExpiry() == 0L) sharedPrefs.getLoginTimeStamp()
                else sharedPrefs.getNudgeTimeRenewalAfterExpiry()

            val subscribedPack = sharedPrefs.getSubscribedPack()
            var packConditions = when (subscribedPack?.freeTrialStatus) {
                true -> {

                    if (subscribedPack.freeTrialStatus == true && (subscribedPack.subscriptionType.equals(
                            subscriptionTypeAtv
                        ) || subscribedPack.subscriptionType.equals(subscriptionTypeFtv))
                    ){
                        false
                    } else {
                        subscribedPack.isInactive == true && subscribedPack.fdoRequested != true && (
                                subscribedPack.freeTrialNudgeDetails?.nudgeToShowOnWhichDayAfterExpiry ?: 0 >= subscribedPack.freeTrialNudgeDetails?.currentDay?.absoluteValue ?: 0
                                        || subscribedPack.freeTrialNudgeDetails?.nudgeToShowOnWhichDayBeforeExpiry ?: 0 >= subscribedPack.freeTrialNudgeDetails?.currentDay?.absoluteValue ?: 0)
                    }
                }
                false -> {
                    subscribedPack?.fdoRequested != true
                            && subscribedPack?.isInactive == true
                            && subscribedPack.subscriptionNudgeDetails?.nudgeToShowOnWhichDayAfterExpiry ?: 0 >= subscribedPack.subscriptionNudgeDetails?.currentDay?.absoluteValue ?: 0
                }
                else -> false
            }

            //extra checks on pack conditions
            if (subscribedPack?.isCombo == true || subscribedPack?.planCTADetails?.renewPlanOption == false || subscribedPack?.subscriptionNudgeDetails == null) {
                packConditions = false
            }


            val conditions = sharedPrefs.getLoginStatus()
                    && packConditions

            return conditions && hasLastNudgeShownTimePassed(
                lastTime,
                changeDaysToMillis(timeGap)
            )
        }

        fun getVerbiage(): Triple<String, String, String> {
            val subscribedPack = sharedPrefs.getSubscribedPack()
            val title =
                if (subscribedPack?.freeTrialStatus == true) subscribedPack.freeTrialNudgeDetails?.nudgeTitle
                    ?: ""
                else subscribedPack?.subscriptionNudgeDetails?.nudgeTitle ?: ""

            val desc =
                if (subscribedPack?.freeTrialStatus == true) subscribedPack.freeTrialNudgeDetails?.nudgeMessage
                    ?: ""
                else subscribedPack?.subscriptionNudgeDetails?.nudgeMessage ?: ""

            val btnText =
                if (subscribedPack?.freeTrialStatus == true) subscribedPack.freeTrialNudgeDetails?.nudgeButton
                    ?: ""
                else subscribedPack?.subscriptionNudgeDetails?.nudgeButton ?: ""

            return Triple(title, desc, btnText)
        }
        val (title, desc, btnText) = getVerbiage()

        val configNudgeData = sharedPrefs.getConfigResponse()?.data?.app?.nudgesDetails?.androidNudgesDetails
        e("showNudges","conditionsToShowRenewPlanAfterExpiryNudge(configNudgeData?.renewNudgeExpired): ${conditionsToShowRenewPlanAfterExpiryNudge(configNudgeData?.renewNudgeExpired)}")
        e("showNudges","conditionsToShowRenewPlanBeforeExpiryNudge(configNudgeData?.renewNudgeActive): ${conditionsToShowRenewPlanBeforeExpiryNudge(configNudgeData?.renewNudgeActive)}")
        e("showNudges","conditionsToShowNotificationNudge(configNudgeData?.notificationNudge): ${conditionsToShowNotificationNudge(configNudgeData?.notificationNudge)}")
        when {

            conditionsToShowRenewPlanAfterExpiryNudge(configNudgeData?.renewNudgeExpired) -> showRenewPlanAfterExpiryNudge(
                title,
                desc,
                btnText
            )
            conditionsToShowRenewPlanBeforeExpiryNudge(configNudgeData?.renewNudgeActive) -> showRenewPlanBeforeExpiryNudge(
                title,
                desc,
                btnText,
                sharedPrefs.getSubscribedPack()?.subscriptionNudgeDetails?.nudgeToShowOnWhichDayBeforeExpiry
                    ?: 0,
                sharedPrefs.getSubscribedPack()?.subscriptionNudgeDetails?.expiryDaysLeft ?: 0
            )
            conditionsToShowNotificationNudge(configNudgeData?.notificationNudge) -> showNotificationOffNudge(
                configNudgeData?.notificationNudge?.verbiage ?: "",
                "",
                getString(R.string.btn_notification_nudge_text)
            )
            /*conditionsToShowUpdateEmailNudge(configNudgeData?.emailNudge) -> showUpdateEmailNudge(
                configNudgeData?.emailNudge?.verbiage?:"",
                "",
                getString(R.string.btn_email_nudge_text)
            )*/
            conditionsToShowGameNudge() -> showGameNudge()
        }
    }

    private fun isManagedAppOpen():Boolean{
      return currentNavController?.value?.currentDestination?.label?.equals("ManagedAppFragment") == true
    }

    private fun conditionsToShowGameBottomNavAnimation() {
        val firstFrequency = sharedPrefs.getConfigResponse()?.data?.app?.gameAnimPopup?.gameAndroid?.launchFrequency
        val periodicFrequency =
            sharedPrefs.getConfigResponse()?.data?.app?.gameAnimPopup?.gameAndroid?.periodicFrequency

        if(firstFrequency == null || periodicFrequency ==null)
            return

        val count = sharedPrefs.getGameAnimOpenCount()
        var nudgeOpenCount = count + 1
        if (nudgeOpenCount<=firstFrequency){
            sharedPrefs.setLastGameAnimTime(System.currentTimeMillis())
            sharedPrefs.setGameAnimOpenCount(nudgeOpenCount)
            bottomNav.setUpLottieAnimation(lv_game_tab,4)
        }
        val diff = getDifference(sharedPrefs.getLastGameAnimTime(), System.currentTimeMillis())
        e("GameAnim : " , "diff: $diff periodicFrequency : $periodicFrequency nudgeOpenCount: $nudgeOpenCount" )
        if (diff>=periodicFrequency){
            bottomNav.setUpLottieAnimation(lv_game_tab,4)
            sharedPrefs.setLastGameAnimTime(System.currentTimeMillis())
        }
    }

    private fun conditionsToShowGameNudge() : Boolean {
        if (!sharedPrefs.isFirstTimeLandingOpen()) {
            return false
        }
        if(!isManagedAppOpen()){
            val firstFrequency = sharedPrefs.getConfigResponse()?.data?.app?.gameNudgePopUp?.gameAndroid?.launchFrequency
            val periodicFrequency =
                sharedPrefs.getConfigResponse()?.data?.app?.gameNudgePopUp?.gameAndroid?.periodicFrequency
            val gameNudgeShown =
                sharedPrefs.getGameNudgeShown()
            val popupEnabled = sharedPrefs.getConfigResponse()?.data?.app?.gameNudgePopUp?.gameAndroid?.popupEnabled?:true

            if(firstFrequency == null || periodicFrequency ==null || gameNudgeShown || !popupEnabled)
                return false

            val count = sharedPrefs.getGameNudgeOpenCount()
            val nudgeOpenCount = count + 1

            if (nudgeOpenCount<= firstFrequency){
                sharedPrefs.setLastGameNudgeTime(System.currentTimeMillis())
                sharedPrefs.setGameNudgeOpenCount(nudgeOpenCount)
//                showGameNudge()
                return true
            }
            val diff = getDifference(sharedPrefs.getLastGameNudgeTime(), System.currentTimeMillis())
            e("GameNudge : " , "diff: $diff periodicFrequency : $periodicFrequency nudgeOpenCount: $nudgeOpenCount" )
            if (diff>= periodicFrequency){
//                showGameNudge()
                sharedPrefs.setLastGameNudgeTime(System.currentTimeMillis())
                return true
            }
            return false
        }

        return false
    }

    private fun showUpdateEmailNudge(title: String, desc: String, btnText: String) {

        if(!isManagedAppOpen()){
            customSnackbarWithTwoActionsUtil.let { util ->
                if (!util.isShown) {
                    util.showCustomSnackbarWithTwoActions(
                        context = this,
                        snackbarType = CustomSnackbarWithTwoActionsType.SnackbarTypeNormalSizeImage,
                        mszTitle = title,
                        mszDesc = desc,
                        imgResourceSmall = R.drawable.ic_nudge_email,
                        imgResourceLarge = null,
                        imgResourceCancel = R.drawable.ic_cross,
                        btnActionText = btnText,
                        maxProgress = 0,
                        currProgress = 0,
                        lambdaAction = {
                            util.hideCustomSnackbarWithTwoActions()
                            showEnterEmailNudge(title,desc,btnText)
                            sharedPrefs.saveNudgeTimeUpdateEmail(Calendar.getInstance().timeInMillis)

                            //todo : need to discuss what to do on ui part
                            miscAnalytics.trackMixPanelEmailNudgeClick()
                        },
                        lambdaCancel = {
                            sharedPrefs.saveNudgeTimeUpdateEmail(Calendar.getInstance().timeInMillis)
                            util.hideCustomSnackbarWithTwoActions()
                            if(conditionsToShowGameNudge())
                                showGameNudge()
                    })
                }
            }
        }

    }

    private fun showEnterEmailNudge(title: String, desc: String, btnText: String) {
        customSnackbarWithEditTextTwoActionsUtil.let { util ->
            if (!util.isShown) {
                util.showCustomSnackbarWithTwoActions(
                    context = this,
                    mszTitle = getString(R.string.enter_email_msg),
                    mszDesc = desc,
                    imgResourceSmall = R.drawable.ic_nudge_email,
                    imgResourceLarge = null,
                    imgResourceCancel = R.drawable.ic_cross,
                    btnActionText = getString(R.string.submit_btn),
                    maxProgress = 0,
                    currProgress = 0,
                    lambdaAction = {email ->


                        editProfileViewModel.getEditProfileResponse().observe(this, Observer { it ->
                            it.getContentIfNotHandled()?.let { response ->
                                showToast(context=this, response.message?:"")
                                val selectedProfile = sharedPrefs.getSelectedProfile()
                                selectedProfile?.let { it1 ->
                                    it1.emailId = email
                                    sharedPrefs.setSelectedProfile(it1)
                                }

                            }
                        })

                        val userData = SubscriberProfileListModel.Data()
                        sharedPrefs.getSelectedProfile().let { savedUserData ->

                            userData.firstName = savedUserData?.firstName
                            userData.lastName = savedUserData?.lastName
                            userData.rmn = sharedPrefs.getClearRMN()

                        }
                        when (sharedPrefs.getDthStatusFreemium()) {
                            DTH_W_BINGE_OLD_USER -> {
                                editProfileViewModel.updateEmailAddress(editProfileViewModel.createRequest(email,userData.rmn,""))
                            }
                            else -> {
                                editProfileViewModel.updateEmailAndName(editProfileViewModel.createRequest(email,userData.rmn," "), HEADER_FROM_NUDGE)
                            }
                        }
                        util.hideCustomSnackbarWithTwoActions()

                    },
                    lambdaCancel = {
                        sharedPrefs.saveNudgeTimeUpdateEmail(Calendar.getInstance().timeInMillis)
                        util.hideCustomSnackbarWithTwoActions()
                        if(conditionsToShowGameNudge())
                            showGameNudge()
                    }
                )
            }
        }
    }

    private fun showNotificationOffNudge(title: String, desc: String, btnText: String) {
        customSnackbarWithTwoActionsUtil.let { util ->
            if (!util.isShown) {
                util.showCustomSnackbarWithTwoActions(
                    context = this,
                    snackbarType = CustomSnackbarWithTwoActionsType.SnackbarTypeNormalSizeImage,
                    mszTitle = title,
                    mszDesc = desc,
                    imgResourceSmall = R.drawable.ic_white_notification_icon,
                    imgResourceLarge = null,
                    imgResourceCancel = R.drawable.ic_cross,
                    btnActionText = btnText,
                    maxProgress = 0,
                    currProgress = 0,
                    lambdaAction = {
                        viewModel.source = SOURCE_NUDGES
                        sharedPrefs.saveNudgeTimeShowNotification(Calendar.getInstance().timeInMillis)
                        util.hideCustomSnackbarWithTwoActions()
                        navDrawerViewModel.toggleSetting(WATCH_NOTI_SETTINGS_KEY)
                        miscAnalytics.trackMixPanelNotificationNudgeClick()
                    },
                    lambdaCancel = {
                        sharedPrefs.saveNudgeTimeShowNotification(Calendar.getInstance().timeInMillis)
                        util.hideCustomSnackbarWithTwoActions()
                    }
                )
            }
        }
    }

    //do not remove: temporary commented on request raised in tsf-3049
//    private fun showNeverMissPlanRenewalNudge(title: String, desc: String, btnText: String) {
//        customSnackbarWithTwoActionsUtil.let { util ->
//            if (!util.isShown) {
//                util.showCustomSnackbarWithTwoActions(
//                    context = this,
//                    snackbarType = CustomSnackbarWithTwoActionsType.SnackbarTypeLargeSizeImage,
//                    mszTitle = title,
//                    mszDesc = desc,
//                    imgResourceSmall = null,
//                    imgResourceLarge = R.drawable.ic_nudge_never_miss_renewal,
//                    imgResourceCancel = R.drawable.ic_cross,
//                    btnActionText = btnText,
//                    maxProgress = 0,
//                    currProgress = 0,
//                    lambdaAction = {
//                        sharedPrefs.saveNudgeTimeNeverMissPlanRenewal(Calendar.getInstance().timeInMillis)
//                        util.hideCustomSnackbarWithTwoActions()
//                        //todo: work here to handle after discussion
//                    },
//                    lambdaCancel = {
//                        sharedPrefs.saveNudgeTimeNeverMissPlanRenewal(Calendar.getInstance().timeInMillis)
//                        util.hideCustomSnackbarWithTwoActions()
//                    }
//                )
//            }
//        }
//    }

    private fun showRenewPlanBeforeExpiryNudge(title: String, desc: String, btnText: String, maxProgress: Int, currProgress: Int) {
        customSnackbarWithTwoActionsUtil.let { util ->
            if (!util.isShown) {
                util.showCustomSnackbarWithTwoActions(
                    context = this,
                    snackbarType = CustomSnackbarWithTwoActionsType.SnackbarTypeSubsRenewal,
                    mszTitle = title,
                    mszDesc = desc,
                    imgResourceSmall = null,
                    imgResourceLarge = null,
                    imgResourceCancel = R.drawable.cross_12dp,
                    btnActionText = btnText,
                    maxProgress = maxProgress,
                    currProgress = currProgress,
                    lambdaAction = {
                        viewModel.source = SOURCE_RENEWAL_NUDE
                        sharedPrefs.saveNudgeTimeRenewalBeforeExpiry(Calendar.getInstance().timeInMillis)
                        util.hideCustomSnackbarWithTwoActions()
                        navDrawerViewModel.isRenew=true
                        navDrawerViewModel.navDrawerAction.postValue(SingleEvent(NavDrawerActions.Renew))
                        miscAnalytics.trackMixPanelRenewalNudgeClick()
                    },
                    lambdaCancel = {
                        sharedPrefs.saveNudgeTimeRenewalBeforeExpiry(Calendar.getInstance().timeInMillis)
                        util.hideCustomSnackbarWithTwoActions()
                        if(conditionsToShowGameNudge())
                            showGameNudge()
                    }
                )
            }
        }
    }

    fun showRenewPlanAfterExpiryNudge(title: String, desc: String, btnText: String) {
        customSnackbarWithTwoActionsUtil.let { util ->
            if (!util.isShown) {
                util.showCustomSnackbarWithTwoActions(
                    context = this,
                    snackbarType = CustomSnackbarWithTwoActionsType.SnackbarTypeSubsExpired,
                    mszTitle = title,
                    mszDesc = desc,
                    imgResourceSmall = R.drawable.ic_nudge_subs_expired,
                    imgResourceCancel = R.drawable.cross_12dp,
                    btnActionText = btnText,
                    maxProgress = 0,
                    currProgress = 0,
                    lambdaAction = {
                        viewModel.source = SOURCE_RENEWAL_NUDE
                        sharedPrefs.saveNudgeTimeRenewalAfterExpiry(Calendar.getInstance().timeInMillis)
                        util.hideCustomSnackbarWithTwoActions()
                        navDrawerViewModel.navDrawerAction.postValue(
                            SingleEvent(
                                if (sharedPrefs.getSubscribedPack()?.planCTADetails?.renewPlanOption == true)
                                    NavDrawerActions.Renew
                                else
                                    NavDrawerActions.GoVipClicked
                            )
                        )
                        miscAnalytics.trackMixPanelRenewalNudgeClick()
                    },
                    lambdaCancel = {
                        sharedPrefs.saveNudgeTimeRenewalAfterExpiry(Calendar.getInstance().timeInMillis)
                        util.hideCustomSnackbarWithTwoActions()
                        if(conditionsToShowGameNudge())
                            showGameNudge()
                    }
                )
            }
        }
    }

    override fun bindPayByBalanceView(parent: ViewGroup?): View? {
        payByBalanceBinding = LayoutTsWalletBalanceBinding.inflate(layoutInflater, parent, false)
        val addOrModifyPackResponse = sharedPrefs.getAddModifyResponse()
        var hasLowBalance = false
        if (viewModel.sharedPrefs.isManagedAppEnabled()) {
            addOrModifyPackResponse?.data?.payByDthWalletData?.let { balanceResponse ->
                payByBalanceBinding?.balance =
                    balanceResponse.balance
                payByBalanceBinding?.walletPaymentVerbiage =
                    balanceResponse.walletPaymentVerbiage
                payByBalanceBinding?.hasLowBalanceForThisTxn =
                    balanceResponse.hasLowBalanceForThisTxn
                hasLowBalance = balanceResponse.hasLowBalanceForThisTxn == true
            }
        } else {
            addOrModifyPackResponse?.data?.let {
                subscriptionViewModel.fetchBalance(
                    it.amount ?: sharedPrefs.getSubscribedPack()?.amountValue,
                    it.productId,
                    fromScreenName = this.javaClass.simpleName
                ) { walletBalanceResponse ->
                    walletBalanceResponse?.data?.let { balanceResponse ->
                        payByBalanceBinding?.balance =
                            balanceResponse.balanceQueryRespDTO?.balance
                        payByBalanceBinding?.walletPaymentVerbiage =
                            balanceResponse.walletPaymentVerbiage
                        payByBalanceBinding?.hasLowBalanceForThisTxn =
                            balanceResponse.hasLowBalanceForThisTxn
                        hasLowBalance = balanceResponse.hasLowBalanceForThisTxn == true
                    }
                }
            }
        }
        payByBalanceBinding?.payByTSBalCB?.setOnCheckedChangeListener { _, isChecked ->
            payByBalanceBinding?.viewShouldShow = isChecked
        }
        payByBalanceBinding?.subIDTV?.text =
            String.format(
                getString(R.string.subscriber_id_template),
                viewModel.sharedPrefs.getOriginalSubscriberId()
            )
        payByBalanceBinding?.makePaymentBtn?.setOnClickListener {
            localBroadcastHelper.sendBroadcast(
                this,
                Intent(localBroadcastHelper.ACTION_MAKE_PAYMENT).apply {
                    Bundle().putBoolean(KEY_HAS_LOW_BALANCE_FOR_THIS_TXN, hasLowBalance)
                }
            )
        }
        return payByBalanceBinding?.root
    }

    override fun fetchPayloadAndInitiateJuspay(showLoader: Boolean) {
        if (hyperInstance?.isInitialised == false)
            navDrawerViewModel.fetchJuspayInitiatePayload(
                HashMap<String, String>().apply {
                    put(
                        PaymentGatewayConstants.KEY_CUSTOMER_ID,
                        viewModel.sharedPrefs.getOriginalSubscriberId()
                    )
                    put(
                        PaymentGatewayConstants.KEY_CUSTOMER_EMAIL,
                        viewModel.sharedPrefs.getSelectedProfile()?.emailId ?: ""
                    )
                    put("rmn", viewModel.sharedPrefs.getClearRMN() ?: "")
                },
                showLoader
            )
    }

    @SuppressLint("CheckResult")
    override fun userLoggedIn(loginSource : String, isNewUser : Boolean) {
        e("CalledFromLogin","inside userLoggedIn of LandingActivity")
        super.userLoggedIn(loginSource, isNewUser)
        Completable.timer(5, TimeUnit.SECONDS)//use atleast 5 sec because fetchProfile take time to update email
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                showNudges()
                showFirestickOfferDialogByFrequency()
            } // FS dialog check after login
        navDrawerViewModel.updateLoggedOutState(false)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        bottomNav.post {
            calculateGameAnimCoordinates(this@LandingActivity, 4, lv_game_tab, bottomNav)
        }
    }

    //This function is to be used only for handling UI when returning from landscape to portrait player
    fun handlePortraitForBottomNav() {
        isScreenFullScreenMode = false
        showBottomNav()
        /*The below code will only run when player moves from landscape to portrait in DetailsFragment
         and game animation frequencies are true all the UI work are for moving to portrait are
         done and the coordinates are calculated which is very rare*/

        if (gameLottieVisible) {
            mHandler.postDelayed({
                calculateGameAnimCoordinates(
                    this@LandingActivity,
                    4,
                    lv_game_tab,
                    bottomNav
                )
            }, 1500)
        }
    }


    companion object {
        // Minor delay to maintain the event sequence based on Timestamp
        private const val EVENT_DELAY_MS = 300L
    }
}
