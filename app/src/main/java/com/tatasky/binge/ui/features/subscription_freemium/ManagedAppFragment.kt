package com.tatasky.binge.ui.features.subscription_freemium

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.browser.customtabs.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.facebook.FacebookSdk
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.analytics.DRAWER_CYOP
import com.tatasky.binge.analytics.SOURCE_MANAGED_APPS
import com.tatasky.binge.data.database.model.SubscriptionInfoModel
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.databinding.FragmentManagedAppBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.helper.DeeplinkHelper.DeeplinkSubscriptionActions.*
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.ManagedAppViewModel
import com.tatasky.binge.utils.*
import kotlinx.android.synthetic.main.activity_recharge.*
import kotlinx.android.synthetic.main.layout_search_view_episode.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


class ManagedAppFragment : BaseFragment<FragmentManagedAppBinding, ManagedAppViewModel>() {


    private val managedAppsArgs by navArgs<ManagedAppFragmentArgs>()

    override fun getViewModelClass(): Class<ManagedAppViewModel> = ManagedAppViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_managed_app

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    @Inject
    lateinit var pubnubHelper: PubnubHelper
    var journeySource: String? = ""
    var journeySourceRefId: String? = ""
    var accessToken: String? = ""
    var pageUrl: String? = ""
    var checksum: String? = ""
    var cartId: String? = ""
    var exitRedirectionUrl: String? = ""
    var anywhereMigrationcase=false
    private val mHandler = Handler(Looper.getMainLooper())

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {

            isEnabled = false
            activity?.onBackPressed()

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            duration = 500
        }
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            duration = 500
        }
        returnTransition = backward
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner, // LifecycleOwner
            onBackPressedCallback
        )
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }


    override fun setObserver() {

        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                goBack()
            }
        })

        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "updateSubscription",
                    it
                )
                if(anywhereMigrationcase){
                    anywhereMigrationcase=false
                    goBack()
                }
            }
        })
        viewModel.getErrorResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if(it.code == 130007){
                    forceLogoutAll(it.title,it.message)
                }
            }
        })
        viewModel.getManagedAppSummeryResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { managedAppResponse ->
                managedAppResponse.data?.href?.let {
                    checksum = managedAppResponse.data?.checksum ?: ""
                    accessToken = managedAppResponse.data?.accessToken ?: ""
                    setWebView(it)
                }
            }
        })
        viewModel.getMigrationResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { migrationResponse ->

                viewModel.fetchSilentLoginResponse(migrationResponse)

            }
        })
        viewModel.getSilentLoginResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { silentLoginResponse ->
                silentLoginResponse.bingeUserData?.let {
                    handleSubscriptionNavigationAfterLogin(it.subscriptionStatusInfo)
                }
            }
        })
        viewModel.getManagedAppResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { managedAppResponse ->
                managedAppResponse.data?.href?.let {
                    journeySource = parent?.currentJourneyRef ?: ""
                    accessToken = managedAppResponse.data?.accessToken ?: ""
                    pageUrl = it
                    setWebView(it)
                } ?: run { onError(ErrorModel()) }
            }
        })
    }

    fun setLoginObserver(cartId: String) {

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("loginCancel")
            ?.observe(viewLifecycleOwner) {
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("loginCancel")
//            findNavController().popBackStack()
//                setRefeshObserver(cartId)
//                parent?.let {
//                    this.cartId = cartId
//                    fetchRefreshTokenAndReload(it, cartId)
//                }
                goBack()
            }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("loginDismiss")
            ?.observe(viewLifecycleOwner) {
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("loginDismiss")
                mHandler.postDelayed(object : Runnable {
                    override fun run() {
                        goBack()
                    }
                }, 200)


            }
        viewModel.previouslyUsedMobileNumberResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                var parentalControlSnackbarUtil = ParentalControlSnackbarUtil()

                parentalControlSnackbarUtil
                    ?.run {
                        val subscriptionInfo = SubscriptionInfoModel(
                            packId = "cartId",
                            selectedTenureId = "",
                            selectedTenureAmount = "",
                            proratedAmount = "",
                            selectedPack = null,
                            selectedTenure = null,
                            cartId = cartId
                        )
                        findNavController().navigateSafe(
                            ManagedAppFragmentDirections.actionManagedAppFragmentToGuestLoginBottomDialogFragment(
                                isParentalPinSetupRequested = false,
                                isParentalPinVerificaitionRequested = false,
                                isLoggedIn = sharedPrefs.getLoginStatus(),
                                subscriptionInfo = subscriptionInfo,
                                previouslyUsedMobileNumbersList = response.data?.mobileNUmberList?.toTypedArray(),
                                loginSource = SOURCE_MANAGED_APPS
                            )
                        )
                    }
            }
        }

        viewModel.previouslyUsedMobileNumberError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                (activity as? LandingActivity)
                    ?.parentalControlSnackbarUtil
                    ?.run {
                        val subscriptionInfo = SubscriptionInfoModel(
                            packId = "cartId",
                            selectedTenureId = "",
                            selectedTenureAmount = "",
                            proratedAmount = "",
                            selectedPack = null,
                            selectedTenure = null,
                            cartId = cartId
                        )
                        findNavController().navigateSafe(
                            ManagedAppFragmentDirections.actionManagedAppFragmentToGuestLoginBottomDialogFragment(
                                isParentalPinSetupRequested = false,
                                isParentalPinVerificaitionRequested = false,
                                isLoggedIn = sharedPrefs.getLoginStatus(),
                                previouslyUsedMobileNumbersList = null,
                                subscriptionInfo = subscriptionInfo,
                                loginSource = SOURCE_MANAGED_APPS
                            )
                        )
                    }
            }
        }
    }

    private fun fetchRefreshTokenAndReload(it: BaseActivity<*>, cartId: String) {
        viewModel.fetchManagedAppRefreshTokenResponse(
            it.currentJourneyRef,
            it.currentJourneyRefKey,
            cartId
        )
    }

    fun setRefeshObserver(cartId: String) {

        viewModel.getManagedAppRefreshTokenResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { managedAppResponse ->
                managedAppResponse.data?.href?.let {
                    accessToken = managedAppResponse.data?.accessToken ?: ""
                    checksum = managedAppResponse.data?.checksum
                    setWebView(it)
                } ?: run { onError(ErrorModel()) }
            }
        })


    }



    private fun handleSubscriptionNavigationAfterLogin(
        subscriptionStatusInfo: LoginResponse.SubscriptionStatusInfo?
    ) {
        sharedPrefs.getLoginResponse()?.let {

            if (subscriptionStatusInfo?.loginToastFlag == true) {
                // Move to home page and show toast with loginToastMessage
                if (activity !is LandingActivity)
                    startHomeScreen(activity, clearTop = false)
                else
                    goBack()
            }
            /* else
                 if (subscriptionStatusInfo?.nonSubscribedToSamePack == true) {
                     //User not subscribed to same plan
                     // take the user to change plan screen

                     //Todo Need discussion
                     findNavController().navigateSafe(
                         GuestLoginBottomDialogFragmentDirections.actionGuestLoginBottomDialogFragmentToManagedAppFragment(
                             isToSummaryPage=true
                         )
                     )

                 } else if (subscriptionStatusInfo?.allowPG == true) {
                     //take the user to the PG
                     //take user to managedappsummry

                     findNavController().navigateSafe(
                         GuestLoginBottomDialogFragmentDirections.actionGuestLoginBottomDialogFragmentToManagedAppFragment(
                             isToSummaryPage=true
                         )
                     )

                 }*/
            else {
                val view = DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_toast_success_failure,
                    null,
                    false
                )
                view.textLoginSuccessfulToast.text = getString(R.string.toast_msg_login_success, "")
                view.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                showCustomToast(
                    context,
                    view?.root,
                    Gravity.FILL_HORIZONTAL
                )
                if (activity is LandingActivity) {
                    goBack()
                } else {
                    startHomeScreen(activity, clearTop = false)
                }
            }

        }


    }


    override fun onResume() {
        super.onResume()
        binding.toolbar.let {
            it.background = ColorDrawable(resources.getColor(R.color.launcher_background))
            it.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_cross)
            it.setNavigationOnClickListener {
                goBack()
            }
//            it.title = "https://tataplaybinge.com"
        }

        //User coming back from payment fail or cancelled payment
        parent?.let {
            if (movedToPayment) {
                if (false) {
                    // user made success payment
                } else {
                    setRefeshObserver(it.cartId)
                    fetchRefreshTokenAndReload(it, it.cartId)
                }
            }
        }

    }

    var parent: BaseActivity<*>? = null
    override fun toBeCalledOnce() {

        sharedPrefs.getConfigResponse()?.data?.config?.tickTickDrawerScreen?.redirectionUrl?.let {
            exitRedirectionUrl = it
        }
        parent = activity as? BaseActivity<*>?
        journeySource = managedAppsArgs.journeySource
        journeySourceRefId = managedAppsArgs.journeySourceRefId
        accessToken = managedAppsArgs.accessToken
        pageUrl = managedAppsArgs.pageUrl
        parent?.let {
            cartId = it.cartId
            (it as? LandingActivity)?.let { landing ->
                landing.hideNudges()
                landing.hideGameNudge()
            }

        }


        binding.lifecycleOwner = viewLifecycleOwner


        val actionFromDeeplink = activity?.intent?.extras?.getString(ACTION)
        if (managedAppsArgs.isToSummaryPage) {
            loadSummeryPage()
        } else if (!TextUtils.isEmpty(pageUrl)) {
            setWebView(pageUrl!!)
        } else if ((!TextUtils.isEmpty(journeySource) && !TextUtils.isEmpty(journeySourceRefId))
            || managedAppsArgs.skipDrawer
        ) {
            viewModel.fetchManagedAppsUrl(
                actionFromDeeplink ?: journeySource,
                actionFromDeeplink?.let {
                    activity?.intent?.extras?.getString(
                        KEY_MANAGED_APP_JOURNEY_SOURCE_REFID
                    )
                } ?: journeySourceRefId
            )
        } else if (!TextUtils.isEmpty(journeySource) && journeySource.equals(DRAWER_CYOP)) {
            viewModel.fetchManagedAppsUrl(
                actionFromDeeplink ?: journeySource,
                actionFromDeeplink?.let {
                    activity?.intent?.extras?.getString(
                        KEY_MANAGED_APP_JOURNEY_SOURCE_REFID
                    )
                } ?: journeySourceRefId
            )
        } else
            goBack()


    }

    private fun loadSummeryPage() {


        parent?.let {
            viewModel.fetchManagedAppSummeryUrl(
                it.currentJourneyRef,
                it.currentJourneyRefKey,
                it.cartId
            )

        }

    }


    fun goBack() {

        parent?.apply {
            journeySource = ""
            journeySourceRefId = ""
            cartId = ""
        }
        if (parent is LandingActivity) {
            findNavController().navigateUp()
        } else
            onBackPressedCallback.handleOnBackPressed()
    }


    var formaLoaded = false
    var movedToLogin = false
    var movedToPayment = false
    //    val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
//
//    var referenceTime=Date().time
    private fun setWebView(url: String) {
        binding.wvManagedApp.visibility = VISIBLE

        if (isNetworkConnected(requireContext())) {
//            binding.toolbar.title="WebLaunched:"+ dateFormat.format(Date())
//            Log.e("runtimeRecordWebLaunched", Date().time.toString())
//            referenceTime=Date().time

            viewModel.setProgressing(true)
            binding.wvManagedApp.settings.javaScriptEnabled = true
            binding.wvManagedApp.settings.loadWithOverviewMode = true
            binding.wvManagedApp.settings.useWideViewPort = true
            binding.wvManagedApp.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.darkBackground
                )
            )
            binding.wvManagedApp.setBackgroundColor(resources.getColor(android.R.color.black))
            binding.wvManagedApp.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            binding.wvManagedApp.webChromeClient = MyChrome()

            binding.wvManagedApp.settings.apply {
                domStorageEnabled = true
                setAppCacheEnabled(true)
                cacheMode = LOAD_CACHE_ELSE_NETWORK

            }
            binding.wvManagedApp.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    Log.e("ManagedAppsClientServer", "onPageStarted date :"+Date().time.toString()+ " url: $url")
                    viewModel.setProgressing(true)
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    viewModel.setProgressing(false)
                    super.onPageCommitVisible(view, url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {

                    Log.e("ManagedAppsClientServer", "onPageFinished date :"+Date().time.toString()+ " url: $url")

                    viewModel.setProgressing(false)
                    if (formaLoaded) {
                        formaLoaded = false
                        view?.clearHistory()
                        binding.wvManagedApp.loadUrl("javascript:clickFunction()")
                    } else {
                        val pageEndCheck=sharedPrefs.getConfigResponse()?.data?.config?.choosePlanManagedApp?:"choose-plan"
                        if(url?.contains(pageEndCheck) == true) initializeJuspaySDK()
                        super.onPageFinished(view, url)
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: ""
                    d("ManagedApp", "url: $url")
                    return try {
                        val cartId = Uri.parse(url).getQueryParameter("cartId")?:""
                        parent?.cartId = cartId
                        if (url.contains("?status=login") && url.contains("subscription=ANYWHERE")) {
                            if(TextUtils.isEmpty(cartId)){
                                anywhereMigrationcase=true
                                viewModel.fetchFreemiumCurrentSubscription()
                            }else{
                                goBack()
//                                viewModel.fetchMigrateUserResponse(cartId)
                            }
                        } else
                            if (url.contains("?status=success")) {


                                if(sharedPrefs.getSubscriptionType().equals(subscriptionTypeBingeMobile)){

                                    if(TextUtils.isEmpty(cartId)){
                                        anywhereMigrationcase=true
                                        viewModel.fetchFreemiumCurrentSubscription()
                                    }else{
                                        viewModel.fetchMigrateUserResponse(cartId)

                                    }

                                }else{
                                    startActivity(
                                        getPaymentActivityIntent(
                                            activity,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            sharedPrefs = sharedPrefs,
                                            cartId = cartId
                                        ).apply {
                                            putExtra(KEY_IS_RENEW, false)
                                        }
                                    )
                                    lifecycleScope.launch {
                                        delay(400)
                                        findNavController().popBackStack()
                                    }
                                }

                            } else if (url.contains("?status=login")) {
                                movedToLogin = true
                                setLoginObserver(cartId)
                                viewModel.getPreviouslyUsedMobileNumbers()

                            } else if (url.contains("?status=existingLogin")) {
                                movedToLogin = true
                                setLoginObserver(cartId)
                                viewModel.getPreviouslyUsedMobileNumbers()

                            } else if (!TextUtils.isEmpty(exitRedirectionUrl) && url.contains(
                                    exitRedirectionUrl!!
                                ) && !movedToPayment
                            ) {

                                goBack()

                            } else {
                                super.shouldOverrideUrlLoading(view, request)
                            }
                        return true
                    } catch (e: Exception) {
                        super.shouldOverrideUrlLoading(view, request)
                    }
                }
            }

            d("ManagedAppAccessToken", accessToken)

            binding.wvManagedApp.apply {
                formaLoaded = true
                loadData(getForm(url), "text/html", "UTF-8")

            }

        } else {
            onNetworkError("", false)
        }
    }

    private fun initializeJuspaySDK() {
        lifecycleScope.launchWhenResumed {
            delay(2000)
            activity?.let {
                when (it) {
                    //Initiate PG/Juspay SDK
                    is LandingActivity -> it.fetchPayloadAndInitiateJuspay(false)
                    is FreemiumSubscriptionActivity -> it.fetchPayloadAndInitiateJuspay(false)
                    else -> Unit
                }
            }
        }
    }


    fun getForm(url: String): String {
        var formScript = "<hmtl>\n" +
                "<head>\n" +
                "<script>\n" +
                "function clickFunction(){\n" +
                "      var form = document.getElementById(\"myform\");\n" +
                "      form.submit();\n" +
                " }\n" +
                "</script>\n" +
                "<form style=\"background-color:black;min-height:100vh\" id=\"myform\" action=\"" + url + "\" method=\"post\">\n"
        if (managedAppsArgs.isToSummaryPage || movedToLogin) {
            movedToLogin = false
            formScript =
                formScript + "<input type=\"hidden\" name=\"refreshToken\" value=\"" + accessToken + "\">\n"
            formScript =
                formScript + "<input type=\"hidden\" name=\"checksum\" value=\"" + checksum + "\">\n"
            formScript =
                formScript + "<input type=\"hidden\" name=\"cartId\" value=\"" + cartId + "\">\n"
        } else {
            formScript =
                "$formScript<input type=\"hidden\" name=\"accessToken\" value=\"$accessToken\">\n"
        }


        formScript = formScript + "</form>\n" +
                " </body>\n" +
                "</html>"

        d("ManagedForm", formScript)
        return formScript
    }

    inner class MyChrome internal constructor() : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: WebChromeClient.CustomViewCallback? = null
        protected var mFullscreenContainer: FrameLayout? = null
        private var mOriginalOrientation = 0
        private var mOriginalSystemUiVisibility = 0
        override fun getDefaultVideoPoster(): Bitmap? {
            return if (mCustomView == null) {
                null
            } else BitmapFactory.decodeResource(
                FacebookSdk.getApplicationContext().getResources(),
                2130837573
            )
        }


        override fun onHideCustomView() {
            (activity?.window?.decorView as FrameLayout).removeView(
                mCustomView
            )
            mCustomView = null
            activity?.getWindow()?.getDecorView()?.setSystemUiVisibility(
                mOriginalSystemUiVisibility
            )
            activity?.setRequestedOrientation(mOriginalOrientation)
            mCustomViewCallback?.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(
            paramView: View?,
            paramCustomViewCallback: WebChromeClient.CustomViewCallback?
        ) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            mOriginalSystemUiVisibility =
                activity?.window?.decorView?.systemUiVisibility ?: View.SYSTEM_UI_FLAG_FULLSCREEN
            mOriginalOrientation =
                activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            mCustomViewCallback = paramCustomViewCallback
            (activity?.window?.decorView as FrameLayout?)?.addView(
                mCustomView,
                FrameLayout.LayoutParams(-1, -1)
            )
            activity?.window?.decorView?.systemUiVisibility =
                3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

}