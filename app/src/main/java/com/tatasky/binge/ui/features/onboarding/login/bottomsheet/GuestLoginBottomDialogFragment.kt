package com.tatasky.binge.ui.features.onboarding.login.bottomsheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.text.set
import androidx.core.text.toSpannable
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.toWindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.erosnow.partner.ENConfiguration
import com.erosnow.partner.ENSDK
import com.erosnow.partner.ErosNowEnvironment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.customviews.LinearGradientSpan
import com.tatasky.binge.data.database.model.SubscriptionInfoModel
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.data.networking.models.response.UsedMobileNumber
import com.tatasky.binge.databinding.FragmentGuestLoginBottomSheetDialogBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.details.KEY_GUEST_LOGIN_BOTTOM_SHEET_RESULT
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.temp.GuestLoginBottomSheetResult
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.PaymentUtility.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_home.bottomNav
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_guest_login_bottom_sheet_dialog.*
import java.util.*

class GuestLoginBottomDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics
    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    @Inject
    lateinit var sharedPrefs: PrefsRepo

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var localBroadcastHelper: LocalBroadcastHelper

    private lateinit var mBinding: FragmentGuestLoginBottomSheetDialogBinding
    private lateinit var mViewModel: GuestLoginViewModel
    private val guestLoginBottomDialogArgs by navArgs<GuestLoginBottomDialogFragmentArgs>()
    private var isBottomSheetResultHandled = false

    private var isLoaded: Boolean = false
    private val mHandler = Handler(Looper.getMainLooper())
    private var showProgress: Runnable = Runnable { }
    var loaderDelayTime = 0L

    private var standardBottomSheetBehavior: BottomSheetBehavior<FrameLayout>? = null

    private fun getSourceOrFromScreenName() =
        activity?.intent?.extras?.get(KEY_FROM_SCREEN) as String?
            ?: guestLoginBottomDialogArgs.loginSource.takeIf {
                it.isNotBlank()
            } ?: SOURCE_HOME

    private fun getSourceOrFromScreenNameForAppsFlyer() =
        activity?.intent?.extras?.get(KEY_FROM_SCREEN) as String?
            ?: SOURCE_HOME

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        this.context?.let{ctx->
            if(isTablet(ctx)){
                return Dialog(ctx, theme)
            }

        }
        return super.onCreateDialog(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment

        mBinding = FragmentGuestLoginBottomSheetDialogBinding.inflate(inflater, container, false)
        return mBinding.root
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel.cartId=guestLoginBottomDialogArgs.subscriptionInfo?.cartId?:""

        if (!isLoaded) {
            hideProgress()
            isLoaded = true
        }
        this.context?.let{ctx->
            if(isTablet(ctx)){
              this.dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }

        }

        loaderDelayTime = sharedPrefs.getLoaderDelayTime()
        showProgress = Runnable {
            showProgress()
        }

        mViewModel.progressListener.observe(viewLifecycleOwner) {
            if (it) {
                if (!allowedTouchWhenLoading()) {
                    mBinding.progressBarOverlay.visibility = View.VISIBLE
                    mBinding.root.closeKeyboard()
                }

                mHandler.postDelayed(
                    showProgress, loaderDelayTime
                )
            } else {
                mHandler.removeCallbacks(showProgress)
                hideProgress()
            }
        }

        activity?.let {
            if(isTablet(it)){
                showHideBottomNavigation()
            }
        }
    }

    private fun showHideBottomNavigation(){
        dialog?.window?.decorView?.setOnApplyWindowInsetsListener{view, insets->
            val insetsCompat = toWindowInsetsCompat(insets, view)
            val isImeVisible = insetsCompat.isVisible(WindowInsetsCompat.Type.ime())
            // below line, do the necessary stuff:
            if(isImeVisible)
                (activity as? LandingActivity)?.hideBottomNav()
            else
                (activity as? LandingActivity)?.showBottomNav()
            view.onApplyWindowInsets(insets)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? LandingActivity)?.showBottomNav()
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        mViewModel = ViewModelProvider(
            this,
            mViewModelFactory
        )[GuestLoginViewModel::class.java]


        //todo: discuss with shilpi if we can do it in improved way
        mBinding.progressBarOverlay.setOnTouchListener { _, _ -> true }

        dialog?.let {
            standardBottomSheetBehavior = (dialog as? BottomSheetDialog?)?.behavior
            standardBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }


        mViewModel.guestLoginResult.observe(viewLifecycleOwner) {
            if (!isBottomSheetResultHandled) {
                it?.getContentIfNotHandled()?.let { result ->
                    isBottomSheetResultHandled = true
                    if (result == GuestLoginBottomSheetResult.SUCCESS)
                        mViewModel.fetchFreemiumCurrentSubscription()
                    else
                        setResultAndDismissBottomSheet(result)
                }
            }
        }

        mViewModel.isSubscriptionFetched.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                mViewModel.guestLoginResult.value?.peekContent()?.let { result ->
                    setResultAndDismissBottomSheet(result)
                    (activity as? LandingActivity)?.showWelcomeDialog()
                }
            }
        }

        loginAnalytics.trackLoginInitiate(guestLoginBottomDialogArgs.loginSource)
        mViewModel.loginSource=guestLoginBottomDialogArgs.loginSource
        /* loginAnalytics.trackLoginPageVisit(
             type = "", //need-confirmation
             guestLoginBottomDialogArgs.loginSource
         )*/


        guestLoginBottomDialogArgs.previouslyUsedMobileNumbersList
            .takeIf { !it.isNullOrEmpty() }
            ?.let {
                mViewModel.previouslyUsedMobileNumberList =
                    it.toList() as MutableList<UsedMobileNumber>
            }

        guestLoginBottomDialogArgs.isParentalPinVerificaitionRequested.let {
            mViewModel.isParentalPinVerificationRequested = it == true
        }

        guestLoginBottomDialogArgs.isParentalPinSetupRequested.let {
            mViewModel.isParentalPinSetupRequested = it == true
        }

        guestLoginBottomDialogArgs.isLoggedIn.let {
            mViewModel.isLoggedIn = it == true
        }

        guestLoginBottomDialogArgs.subscriptionInfo?.selectedTenureId.let{
            mViewModel.packageId = it

        }

//        mViewModel.checkForManagedAppEligibility { eligible ->
//        }
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(showProgress)
        view?.closeKeyboard()
    }

    override fun getTheme(): Int {
        return R.style.GuestLoginBottomSheetDialogTheme
    }

    private fun setResultAndDismissBottomSheet(result: String) {
        val view = DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
            LayoutInflater.from(context),
            R.layout.layout_toast_success_failure,
            null,
            false
        )
        when (result) {
            GuestLoginBottomSheetResult.SUCCESS -> {

                loginAnalytics.trackMixPanelSetDefaultProfile(sharedPrefs.getBaId())
                initErosNow()
                sharedPrefs.saveLoginTimeStamp(Calendar.getInstance().timeInMillis)
                sharedPrefs.resetHotstarPopupCount()
                sharedPrefs.clearHotstarPopupData()
                sharedPrefs.setMxUpsellClosed(false)

                if(activity is LandingActivity){
                    (activity as LandingActivity).userLoggedIn(mViewModel.loginSource, mViewModel.isNewUser)
                }
                else
                    (activity as? BaseActivity<*>)?.userLoggedIn(mViewModel.loginSource, mViewModel.isNewUser)

                val subscriptionStatusInfo = mViewModel.getSubscriptionStatusInfo()
                if(sharedPrefs.getMixPanelId() == null && sharedPrefs.getReferenceId() != null)
                    mViewModel.fetchMixpanelUniqueId(sharedPrefs.getReferenceId()!!)
                if(!NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(),true))
                    mViewModel.fetchBalance()
                if (mViewModel.showFreeTrailUI) //If true, User was eligible for Free trial
                    showFreeTrialMsg(view)
                else {
                    val loginToastMessage =
                        if (subscriptionStatusInfo?.loginToastFlag == true)
                            subscriptionStatusInfo.loginToastMessage ?: getString(
                                R.string.toast_msg_login_success,
                                ""
                            )
                        else
                            getString(
                                R.string.toast_msg_login_success,
                                ""
                            )
                    view.textLoginSuccessfulToast.text =
                        loginToastMessage
                    view.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                    view.viewBackgroundLoginSuccessfulToast.setBackgroundResource(R.drawable.toast_login_bg)

                }
                showCustomToast(
                    context,
                    view?.root,
                    Gravity.FILL_HORIZONTAL
                )
                context?.let { localBroadcastHelper.sendBroadcast(it, localBroadcastHelper.ACTION_SUBSCRIPTION_UPDATED) }
                (activity as? LandingActivity)?.handleDeeplink()
                if(guestLoginBottomDialogArgs.isLoginToHome){
                    startHomeScreen(activity, clearTop = false)
                }

                else if(sharedPrefs.isManagedAppEnabled()){
                    handleSubscriptionNavigationAfterLoginTickTick(mViewModel.getSubscriptionStatusInfo())

                }else{
                    guestLoginBottomDialogArgs.subscriptionInfo?.let{ selectedSubscriptionInfo->
                        handleSubscriptionNavigationAfterLogin(selectedSubscriptionInfo,mViewModel.getSubscriptionStatusInfo())
                    }

                }

            }
            GuestLoginBottomSheetResult.FAILURE -> {

                val loginNotSuccessfulMessage = String.format(getString(R.string.toast_msg_login_failure), getString(R.string.please_try_again))
                val spannable = loginNotSuccessfulMessage.toSpannable()
                spannable[(getString(R.string.toast_msg_login_failure).length-2)..loginNotSuccessfulMessage.length] =
                    LinearGradientSpan(
                        loginNotSuccessfulMessage,
                        getString(R.string.please_try_again),
                        intArrayOf(getColor(requireContext(), R.color.darkSecondary), getColor(requireContext(), R.color.darkSecondary)),
                        null
                    )
                view.textLoginSuccessfulToast.text = spannable
                view.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_warning_login_failure)
                if (!mViewModel.isLoggedIn) showCustomToast(context, view?.root, Gravity.FILL_HORIZONTAL)
            }
        }
        dialog?.dismiss()


        findNavController().previousBackStackEntry
            ?.savedStateHandle
            ?.set(KEY_GUEST_LOGIN_BOTTOM_SHEET_RESULT, result)

    }


    var updateBackStack=true
    private fun dismissSubscriptionLoginDialog(){
//        if (activity is LandingActivity) {
//            (activity as LandingActivity).subscriptionBottomSheetDialog?.dismiss()
//        } else {
//            dialog?.dismiss()
        updateBackStack=false
            activity?.onBackPressed()
//        }
    }

    private fun handleSubscriptionNavigationAfterLoginTickTick(
        subscriptionStatusInfo: LoginResponse.SubscriptionStatusInfo?
    ) {
        sharedPrefs.getLoginResponse()?.let {

            if (subscriptionStatusInfo?.loginToastFlag == true) {
                // Move to home page and show toast with loginToastMessage
                if (activity !is LandingActivity)
                    startHomeScreen(activity, clearTop = false)
                else
                    dismissSubscriptionLoginDialog()
            }
            else
                if (subscriptionStatusInfo?.nonSubscribedToSamePack == true) {
                //User not subscribed to same plan
                // take the user to change plan screen

                //Todo Need discussion
                findNavController().navigateSafe(
                    GuestLoginBottomDialogFragmentDirections.actionGuestLoginBottomDialogFragmentToManagedAppFragment(
                        isToSummaryPage=true,
                        source=guestLoginBottomDialogArgs.source
                    )
                )

            } else if (subscriptionStatusInfo?.allowPG == true) {
                //take the user to the PG
                //take user to managedappsummry

                findNavController().navigateSafe(
                    GuestLoginBottomDialogFragmentDirections.actionGuestLoginBottomDialogFragmentToManagedAppFragment(
                        isToSummaryPage=true,
                        source=guestLoginBottomDialogArgs.source
                    )
                )

            } else {
                val view = DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_toast_success_failure,
                    null,
                    false
                )
                view.textLoginSuccessfulToast.text = getString(R.string.toast_msg_login_success, "")
                view.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                    view.viewBackgroundLoginSuccessfulToast.setBackgroundResource(R.drawable.toast_login_bg)

                  /*  showCustomToast(
                    context,
                    view?.root,
                    Gravity.FILL_HORIZONTAL
                )*/
                if (activity is LandingActivity) {
                    updateBackStack=false
                    dialog?.dismiss()
                } else {
                    startHomeScreen(activity, clearTop = false)
                }
            }

        }


    }
    private fun handleSubscriptionNavigationAfterLogin(
        selectedSubscriptionInfo: SubscriptionInfoModel,
        subscriptionStatusInfo: LoginResponse.SubscriptionStatusInfo?
    ) {



        if (subscriptionStatusInfo?.loginToastFlag == true) {
            // Move to home page and show toast with loginToastMessage
            if (activity !is LandingActivity)
                startHomeScreen(activity, clearTop = false)
            else
                dismissSubscriptionLoginDialog()
        } else if (subscriptionStatusInfo?.nonSubscribedToSamePack == true) {
            // take the user to change plan screen
            if (activity is LandingActivity) {
                activity?.startActivity(
                    context?.let { ctx ->
                        getSubscriptionActivityIntent(
                            ctx,
                            startPackListing = sharedPrefs.getSubscribedPack()?.planCTADetails?.changePlanOption == true,
                            packId = selectedSubscriptionInfo.packId
                        )
                    }
                )
            } else {
                activity?.finish()
                activity?.startActivity(
                    context?.let { ctx ->
                        getSubscriptionActivityIntent(
                            ctx,
                            startPackListing = sharedPrefs.getSubscribedPack()?.planCTADetails?.changePlanOption == true,
                            packId = selectedSubscriptionInfo.packId
                        )
                    }
                )
                dismissSubscriptionLoginDialog()
            }
        } else if (subscriptionStatusInfo?.allowPG == true) {
            //take the user to the PG
            activity?.startActivity(
                context?.let { ctx ->
                    getPaymentActivityIntent(
                        context = ctx,
                        packId = selectedSubscriptionInfo.packId,
                        selectedTenureId = selectedSubscriptionInfo.selectedTenureId,
                        selectedTenureAmount = selectedSubscriptionInfo.selectedTenureAmount,
                        isMigrated = false,
                        migratedVerbiage = "",
                        proratedAmount = selectedSubscriptionInfo.proratedAmount,
                        fromScreen = getSourceOrFromScreenName(),
                        sharedPrefs = sharedPrefs,
                        newUserDelay = true
                    ).apply {
                        putExtra(
                            KEY_ACTUAL_PRORATED_AMOUNT_FROM_API,
                            //Passing actual prorated amount from API to identify modification is upgrade or downgrade
                            //If it is null then its downgrade otherwise upgrade
                            guestLoginBottomDialogArgs.subscriptionInfo?.proratedAmount
                        )
                        putExtra(
                            KEY_APPSFLYER_SOURCE,
                            activity?.intent?.getStringExtra(KEY_APPSFLYER_SOURCE)
                                ?: getSourceOrFromScreenNameForAppsFlyer()
                        )
                        putExtra(KEY_PACK_PRICE, selectedSubscriptionInfo.selectedPack?.amountValue)
                        putExtra(KEY_PACK_NAME, selectedSubscriptionInfo.selectedPack?.productName)
                        putExtra(
                            KEY_SELECTED_TENURE_PACK_PRICE,
                            selectedSubscriptionInfo.selectedTenure?.offeredPriceValue
                        )
                        putExtra(
                            KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                            selectedSubscriptionInfo.selectedTenure?.tenureDurationInDaysWithDSuffix
                        )
                        putExtra(
                            KEY_SELECTED_TENURE_TYPE,
                            selectedSubscriptionInfo.selectedTenure?.tenureType
                        )
                    }
                }
            )
            dismissSubscriptionLoginDialog()
        } else {
            val view = DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                LayoutInflater.from(context),
                R.layout.layout_toast_success_failure,
                null,
                false
            )
            view.textLoginSuccessfulToast.text = getString(R.string.toast_msg_login_success, "")
            view.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
            view.viewBackgroundLoginSuccessfulToast.setBackgroundResource(R.drawable.toast_login_bg)

            /*showCustomToast(
                context,
                view?.root,
                Gravity.FILL_HORIZONTAL
            )*/
            if (activity is LandingActivity) {
                (activity as LandingActivity).subscriptionBottomSheetDialog?.dismiss()
            } else {
                startHomeScreen(activity, clearTop = false)
            }
        }
    }


    private fun showFreeTrialMsg(view: LayoutToastSuccessFailureBinding) {
        // User login success and user is eligible free trial
        // By default free trial pack is added by BE to new user's account,
        // there is no API as of now to activate free trial manually
        (activity as? LandingActivity)?.showFreeTrialStartedUI()
        val loginFreeTrialStartedSuccess = String.format(getString(R.string.toast_msg_login_success), getString(R.string.free_trial_started))
        val spannable = loginFreeTrialStartedSuccess.toSpannable()
        spannable[getString(R.string.toast_msg_login_success).length-2..loginFreeTrialStartedSuccess.length] =
            LinearGradientSpan(
                loginFreeTrialStartedSuccess,
                getString(R.string.free_trial_started),
                intArrayOf(getColor(requireContext(), R.color.color_golden_vip), Color.parseColor("#FFF389"), getColor(requireContext(), R.color.color_golden_vip)),
                null
            )
        loginAnalytics.trackStartTrialEvent()
        subscriptionAnalytics.trackSubscribeSuccess(
            mViewModel.sharedPrefs.getSubscribedPack()?.packType ?: PACK_TYPE_FREE,
            activity?.intent?.extras?.getString(KEY_FROM_SCREEN) ?: guestLoginBottomDialogArgs.loginSource,
            mViewModel.sharedPrefs.getSubscribedPack()?.productName ?: "" /*Pack name*/,
            mViewModel.sharedPrefs.getSubscribedPack()?.amountValue ?: "",
            false,
            false,
            "",
            "",
            "",
            "",
            mViewModel.sharedPrefs.getSubscribedPack()?.productId,
            mViewModel.sharedPrefs.getSubscribedPack()?.promoCode,
            mViewModel.sharedPrefs.getSubscribedPack()?.paymentMethod,
            mViewModel.sharedPrefs.getSubscribedPack()?.userIsOnFirstPaidPack == true,
            null,
            getSourceOrFromScreenNameForAppsFlyer(),
            mViewModel.sharedPrefs.getSubscribedPack()?.amountValue,
            mViewModel.sharedPrefs.getSubscribedPack()?.packDurationInDaysWithDSuffix,
            FREEMIUM,
            PACK_TYPE_FREE,
            FREEMIUM,
            null,
            mViewModel.sharedPrefs.getSubscribedPack()?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType,
            false,
            productType = null
        )
        view.textLoginSuccessfulToast.text = spannable
        view.viewBackgroundLoginSuccessfulToast.background = getDrawable(requireContext(), R.drawable.layer_gradient_border)
    }

    private fun allowedTouchWhenLoading() = false

    private fun showProgress() {
        mBinding.progressBar.startProgressAvd(true)
    }

    private fun hideProgress() {
        mBinding.progressBar.startProgressAvd(false)
        mBinding.progressBarOverlay.visibility = View.GONE
    }

    override fun onDismiss(dialog: DialogInterface) {
        mViewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.FAILURE))
        mViewModel.setProgressing(false)
        mViewModel.removeDisposable()
        if(sharedPrefs.isManagedAppEnabled()){
            if(updateBackStack)
                findNavController().previousBackStackEntry?.savedStateHandle?.set("loginCancel", true) // reload ManagedApp here
            else
                findNavController().previousBackStackEntry?.savedStateHandle?.set("loginDismiss", true)
        }
        super.onDismiss(dialog)
    }


    private fun initErosNow() {
        /*Initialization of ErosNow*/
        context?.let {
            val configuration = ENConfiguration(
                erosNowEnvironment = if (BuildConfig.FLAVOR == "uat")
                    ErosNowEnvironment.STAGING
                else
                    ErosNowEnvironment.PRODUCTION,
                partnerCode = BuildConfig.EROSNOW_PARTNER_CODE,
                apiClientId = BuildConfig.EROSNOW_API_CLIENTID,
                country = "IN",
                deviceId = DeviceInfoUtils.getDeviceId(it)
            )
            ENSDK.setup(it, configuration)
        }
    }
}
