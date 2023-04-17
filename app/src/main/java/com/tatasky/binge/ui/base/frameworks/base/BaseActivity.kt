package com.tatasky.binge.ui.base.frameworks.base

import `in`.juspay.hypersdk.core.MerchantViewType
import `in`.juspay.hypersdk.data.JuspayResponseHandler
import `in`.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter
import `in`.juspay.services.HyperServices
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.InAppNotificationButtonListener
import com.moengage.inapp.MoEInAppHelper
import com.moengage.inapp.listeners.InAppLifeCycleListener
import com.moengage.inapp.model.InAppData
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.customviews.ConfettiDialog
import com.tatasky.binge.databinding.LayoutTsWalletBalanceBinding
import com.tatasky.binge.domain.repositories.DataStorePrefsRepo
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.helper.HyperInstanceHelper
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.base.MyApp
import com.tatasky.binge.ui.base.frameworks.extensions.CleverTapNotificationHandler
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.common.AppUpdateDialog
import com.tatasky.binge.ui.features.dialog.CommonDialog
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.dialog.DialogViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.splash.SplashAnalytics
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription.view.AtvCancellationDialog
import com.tatasky.binge.ui.features.subscription.view.DeviceStatusLogoutDialog
import com.tatasky.binge.ui.features.subscription.view.LogoutDialog
import com.tatasky.binge.ui.features.subscription_freemium.IPaymentGateway
import com.tatasky.binge.utils.*
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject


abstract class BaseActivity<VM : BaseViewModel> : DaggerAppCompatActivity(), IPaymentGateway {

    //To keep current journey flags for managed apps
    var currentJourneyRef:String =""
    var currentJourneyRefKey:String =""
    var cartId:String =""

    lateinit var viewModel: VM
    var retryView: View? = null
    protected var isNetworkAvailable = true
    protected var isInAppShown = false
    protected lateinit var dialogViewModel: DialogViewModel

    @Inject
    lateinit var dataStorePrefs: DataStorePrefsRepo
    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    @Inject
    lateinit var subscriptionAnalytics : SubscriptionAnalytics
    @Inject
    lateinit var splashAnalytics: SplashAnalytics
    /*Changes for internet connection checks*/
    lateinit var networkTicker: Job
    @Inject
    lateinit var sharedPrefs: PrefsRepo
    @Inject
    lateinit var pubnubHelper: PubnubHelper
    private var mHandler = Handler(Looper.getMainLooper())
    private var initialNetState = true
    /*End changes*/
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var commonDialog: CommonDialog? = null
    private var confettiDialog: ConfettiDialog? = null
    private var logoutDialog: LogoutDialog? = null
    private var appUpdateDialog: AppUpdateDialog? = null
    private var deviceStatusLogoutDialog: DeviceStatusLogoutDialog? = null
    private var atvCancellationDialog: AtvCancellationDialog? = null


//    @Inject
//    internal lateinit var mixpanelHelper: MixpanelHelper

    abstract fun getContentViewId(): Int
    abstract fun getRootLayoutContainer(): View
    abstract fun getViewModelClass(): Class<VM>

    @Inject
    lateinit var broadcastHelper: LocalBroadcastHelper
    override var juspayProcessPayload: JSONObject? = null
    override var hyperInstance: HyperServices? = null
    override var payByBalanceBinding: LayoutTsWalletBalanceBinding? = null
    override var payByDthBalanceSelected: Boolean = false

    override fun setupPaymentSDK(
        paymentClientId: String,
        paymentServiceId: String,
        paymentBetaAssets: Boolean,
        shouldCallPrefetch: Boolean
    ) {
        if (shouldCallPrefetch)
            preFetchJusPay(paymentClientId, paymentServiceId, paymentBetaAssets)
        hyperInstance = HyperInstanceHelper.getInstance(this).hyperInstance
    }

    override fun terminateJuspayService(activity: FragmentActivity) {
        if (hyperInstance != null && hyperInstance?.isInitialised == true) {
            hyperInstance?.resetActivity(activity)
            hyperInstance?.terminate()
            hyperInstance = null
            payByBalanceBinding = null
        }
    }

    override fun onDestroy() {
        MoEInAppHelper.getInstance().removeInAppLifeCycleListener(inAppLifeCycleListener)
        super.onDestroy()
    }
    override fun handleJusPayProcessResult(status: String) {
        when (status) {
            // Transaction has no error
            TransactionStatus.CHARGED.transactionStatus.lowercase(Locale.getDefault()) -> {
                payByDthBalanceSelected = false
                subscriptionAnalytics.trackPaymentFlowExit(
                    SUCCESS,
                    PAYMENT_GATEWAY,
                    SUCCESS,
                    status.uppercase()
                )
                // Payment successfully charged
                broadcastHelper.sendBroadcast(this, broadcastHelper
                    .ACTION_PAYMENT_CHARGED)
            }
            else -> {
                // other status, this is also failure
                handleJusPayError(status)
            }
        }
    }

    override fun handleJusPayError(status: String) {
        val intent = Intent(broadcastHelper.ACTION_PAYMENT_ERROR)
        intent.putExtra("status",status)
        broadcastHelper.sendBroadcast(this, intent)
    }

    /**
     * Initiating Juspay SDK as early as possible, So that process() don't take much time to open Payment page.
     * Calling process on another activity using the same instance of HyperService.
     * This is multi-activity integration of Juspay SDK.
     * Note: Don't move it to fragment as this will lead to stuck loader or black UI issue
     */
    override fun initiateJusPay(
        fragmentActivity: FragmentActivity,
        juspayInitiatePayload: JSONObject,
        dthStatus: String?,
        shouldShowInfoOnUI: Boolean,
        alsoCallProcess: Boolean
    ) {
        d(this.javaClass.simpleName, "Juspay: initiateJusPay Payload: $juspayInitiatePayload")
        if (shouldShowInfoOnUI)
            showProgress()
        d(this.javaClass.simpleName, "Juspay: initiate() called")
        hyperInstance?.initiate(fragmentActivity, juspayInitiatePayload, object : HyperPaymentsCallbackAdapter() {
            override fun getMerchantView(parent: ViewGroup?, viewType: MerchantViewType?): View? {
                return when (viewType) {
                    MerchantViewType.HEADER -> {
                        /**
                         * This callback gets called after the process()
                         * and process() is only called after login
                         * So getting the latest DTH status whenever this callback hits
                         */
                        val latestDTHStatus =
                            sharedPrefs.getDthStatusFreemium().takeIf { it.isNotEmpty() }
                                ?: dthStatus
                        if (!NON_DTH_USER.equals(latestDTHStatus, true))
                        // DTH User
                            bindPayByBalanceView(parent)
                        else
                        // Non DTH User
                            null
                    }
                    else -> null
                }
            }

            override fun onEvent(data: JSONObject?, handler: JuspayResponseHandler?) {
                try {
                    when (data?.getString(PaymentGatewayConstants.KEY_EVENT)) {
                        PaymentGatewayEvent.SHOW_LOADER.event -> {
                            // Show some loader here
                            // SDK is opening on activity so using Activity progress bar to show
                            showProgress()
                        }
                        PaymentGatewayEvent.HIDE_LOADER.event -> {
                            // Hide Loader
                            hideProgress()
                        }
                        PaymentGatewayEvent.INITIATE_RESULT.event -> {
                            d(this.javaClass.simpleName, "Juspay initiate_result data $data")
                            val innerPayload: JSONObject = data.getJSONObject(
                                PaymentGatewayConstants.KEY_PAYLOAD)
                            val status = innerPayload.optString(PaymentGatewayConstants.KEY_STATUS)
                            val hasError = data.optBoolean(PaymentGatewayConstants.KEY_ERROR)
                            if(!hasError) {
                                when(status) {
                                    PaymentGatewayInitResult.SUCCESS.result -> {
                                        // Do something or let it initiate silently
                                        if(alsoCallProcess && juspayProcessPayload != null)
                                            processJuspay(juspayProcessPayload)
                                        else
                                            if (shouldShowInfoOnUI)
                                                handleJusPayError(status)
                                    }
                                    else ->
                                        if (shouldShowInfoOnUI)
                                            handleJusPayError(status) // Initiate failed
                                }
                            } else {
                                // Show error on UI if needed.
                                d(this.javaClass.simpleName, data.optString(PaymentGatewayConstants.KEY_ERROR_CODE))
                                d(this.javaClass.simpleName, data.optString(PaymentGatewayConstants.KEY_ERROR_MESSAGE))
                                if (shouldShowInfoOnUI)
                                    handleJusPayError(status)
                            }
                        }
                        PaymentGatewayEvent.PROCESS_RESULT.event -> {
                            val innerPayload: JSONObject = data.getJSONObject(
                                PaymentGatewayConstants.KEY_PAYLOAD)
                            val status = innerPayload.optString(PaymentGatewayConstants.KEY_STATUS)
                            val hasError = data.optBoolean(PaymentGatewayConstants.KEY_ERROR)
                            if (!payByDthBalanceSelected)
                                sharedPrefs.saveLastPgSdkProcessStatus(status)
                            d(this.javaClass.simpleName, "Juspay process_result data $data")
                            if (!hasError) {
                                handleJusPayProcessResult(status)
                            } else {
                                // Transaction has error
                                d(this.javaClass.simpleName, data.optString(PaymentGatewayConstants.KEY_ERROR_CODE))
                                d(this.javaClass.simpleName, data.optString(PaymentGatewayConstants.KEY_ERROR_MESSAGE))
                                handleJusPayError(
                                    status
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * Prefetch the latest JusPay business rules and assets
     * Prefetch can be done multiple times.
     **/
    override fun preFetchJusPay(paymentClientId: String, paymentServiceId: String, paymentBetaAssets: Boolean) {
        val payload = JSONObject()
        val innerPayload = JSONObject()
        try {
            // ClientId is assigned by Juspay as a unique reference to static resources allocated to a merchant
            innerPayload.put(PaymentGatewayConstants.KEY_CLIENT_ID, paymentClientId)
            payload.put(PaymentGatewayConstants.KEY_PAYLOAD, innerPayload)
            //service acts as a product reference
            payload.put(PaymentGatewayConstants.KEY_SERVICE, paymentServiceId)
            // Enables switching between test and production resources.
            if (BuildConfig.FLAVOR == "uat")
                payload.put(PaymentGatewayConstants.KEY_BETA_ASSETS, paymentBetaAssets)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        d(this.javaClass.simpleName, "Juspay: preFetchJusPay Payload: $payload")
        HyperServices.preFetch(this, payload)
    }

    fun showDialog() {
        commonDialog?.dismiss()
        commonDialog = CommonDialog.newInstance()
        commonDialog?.show(supportFragmentManager, DIALOG_TAG)
    }

    fun showConfettiDialog() {
        confettiDialog?.dismiss()
        confettiDialog = ConfettiDialog.newInstance()
        confettiDialog?.show(supportFragmentManager, DIALOG_TAG)
    }

    fun getListener(isRetry: Boolean, viewModel: BaseViewModel): CommonDialogEventListener {
        return object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                hideDialog()
                if (isRetry) {
                    viewModel.setProgressing(true)
                    viewModel.retrySubject.onNext(Any())
                } else
                    viewModel.setErrorOkClicked()
            }

            override fun onSecondaryButtonClick() {
                viewModel.setErrorOkClicked()
                hideDialog()
            }

            override fun onCloseButtonClick() {
                viewModel.setErrorOkClicked()
                hideDialog()
            }
        }
    }

    fun hideDialog() {
        try {
            commonDialog?.dismissAllowingStateLoss()
            commonDialog = null
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
    fun hideConfettiDialog(){
        try{
            confettiDialog?.dismissAllowingStateLoss()
            confettiDialog = null
        }catch (e: java.lang.IllegalStateException){
            e.printStackTrace()
        }
    }

    fun showProgress() {
        val loaderView = findViewById<View>(R.id.progressBar)
        if (loaderView is ImageView)
            (loaderView as ImageView).startProgressAvd(true)
        if(!allowedTouchWhenLoading()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            getRootLayoutContainer().closeKeyboard()
        }
    }

    open fun allowedTouchWhenLoading(): Boolean {
        return false
    }

    fun showAppUpdateDialog(listener: CommonDialogEventListener, dialogModel: DialogModel, imageUrl : String?){
        if(appUpdateDialog==null){
            hideDialog()
            appUpdateDialog = AppUpdateDialog.newInstance(listener, dialogModel, imageUrl)
            appUpdateDialog?.show(supportFragmentManager, APP_UPDATE_DIALOG_TAG)
        }
    }

    fun hideAppUpdateDialog(){
        appUpdateDialog?.dismiss()
        hideDialog()
    }

    open fun showLogoutDialog() {
        if (logoutDialog == null) {
            hideDialog()
            deviceStatusLogoutDialog?.dismissAllowingStateLoss()
            logoutDialog = LogoutDialog.newInstance {
                (this.application as MyApp).clearAllData()
                logoutDialog = null
            }
            logoutDialog?.show(supportFragmentManager, LOGOUT_DIALOG_TAG)
        }
    }
    open fun showAllDeviceLogoutDialog(title: String?="Cancelled Plan(s)", description: String?="You may have one or more cancelled plan(s). Please login again to subscribe to a new plan."){
        if (deviceStatusLogoutDialog == null) {
            hideDialog()
            deviceStatusLogoutDialog = DeviceStatusLogoutDialog.newInstance(title?:"",
                description?:"",
                true)
            deviceStatusLogoutDialog?.show(supportFragmentManager, LOGOUT_DIALOG_TAG)
        }
    }

    open fun showDeviceLogoutDialog(title : String, msg:String) {
        if (deviceStatusLogoutDialog == null) {
            hideDialog()
            deviceStatusLogoutDialog = DeviceStatusLogoutDialog.newInstance(title, msg)
            deviceStatusLogoutDialog?.show(supportFragmentManager, LOGOUT_DIALOG_TAG)
        }
    }

    open fun showATVCancelledDialog(dialogContent:String?){
        atvCancellationDialog?.dismiss()
        atvCancellationDialog = dialogContent?.let {
            hideDialog()
            AtvCancellationDialog.newInstance(it)
        }
        atvCancellationDialog?.show(supportFragmentManager, ATV_DIALOG_TAG)
    }


    fun hideProgress() {
        val loaderView = findViewById<View>(R.id.progressBar)
        if (loaderView is ImageView)
            (loaderView as ImageView).startProgressAvd(false)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) initApp(savedInstanceState)
        else
            if (MyApp.initSucceed) {
                val securityMsz = acaSecurityCheck()
                if (TextUtils.isEmpty(securityMsz)) {
                    /*if(checkIsDeviceTv()){
                        showACAAlert(getString(R.string.aca_failed))
                    }else*/
                    initApp(savedInstanceState)
                } else {
                    showACAAlert(securityMsz)
                }
            } else {
                showACAAlert(getString(R.string.aca_failed))
            }
        MoEInAppHelper.getInstance().addInAppLifeCycleListener(inAppLifeCycleListener)
        //Clevertap Changes
        CleverTapAPI.getDefaultInstance(this)
            ?.setInAppNotificationButtonListener(inAppNotificationButtonListener)
    }

    //Clevertap Changes
    //KV pair handling for Clevertap for In-App Notification using a listner
    lateinit var cleverTapNotificationHandler: CleverTapNotificationHandler
    private val inAppNotificationButtonListener = InAppNotificationButtonListener {
        try {
            if (!it.isNullOrEmpty()) {
                if (it.containsKey(KEY_SCREEN_DATA)) {
                    if (!this::cleverTapNotificationHandler.isInitialized) {
                        cleverTapNotificationHandler = CleverTapNotificationHandler()
                    }
                    cleverTapNotificationHandler.onHandleRedirection(this, Bundle().apply {
                        putString("className", LandingActivity::class.java.name)
                        try {
                            var screenData = it[KEY_SCREEN_DATA];
                            screenData =
                                JSONObject(screenData?.replace(Char(160), ' ')).toString()
                            putString(KEY_SCREEN_DATA, screenData)
                        } catch (e: Exception) {
                            putString(KEY_SCREEN_DATA, it[KEY_SCREEN_DATA])
                        }
                    })
                }
            }
        } catch (e: Exception) {

        }
    }
    val inAppLifeCycleListener = object : InAppLifeCycleListener {
        override fun onDismiss(inAppData: InAppData) {
            isInAppShown = false
        }
        override fun onShown(inAppData: InAppData) {
            isInAppShown = true
            try {
                val childCount =
                    (window.decorView.findViewById<View>(android.R.id.content).rootView as ViewGroup).childCount
                val inAppView =
                    (window.decorView.findViewById<View>(android.R.id.content).rootView as ViewGroup)[childCount - 1]
                inAppView.setOnKeyListener(null)
                inAppView.setOnKeyListener(object : View.OnKeyListener {
                    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                        if (event?.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                            return true
                        }
                        return false
                    }
                })
            } catch (e: Exception) {
            }
        }
    }

    private fun checkIsDeviceTv():Boolean{
        //Determine screen size
        return resources.getBoolean(R.bool.isTablet)
    }

    private fun initApp(savedInstanceState: Bundle?) {
        setContentView(getContentViewId())
        viewModel = ViewModelProvider(this, viewModelFactory)[getViewModelClass()]
        init(savedInstanceState)
    }

    abstract fun init(savedInstanceState: Bundle?)

    private fun showACAAlert(message: String) {
        val dialogViewModel =
            ViewModelProvider(
                this,
                viewModelFactory
            ).get(DialogViewModel::class.java)
        val dialogModel = DialogModel(
            false,
            null,
            message,
            getString(R.string.ok),
            null
        )

        val eventListener = object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                hideDialog()
                if (Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask()
                } else {
                    finish()
                    System.exit(0)
                }
            }

            override fun onCloseButtonClick() {
            }

            override fun onSecondaryButtonClick() {
            }
        }
        dialogViewModel.setDialogModel(dialogModel)
        dialogViewModel.setEventHandler(eventListener)
        showDialog()
    }



    private fun startRepeatedNetworkCheck(timeInterval: Long): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                onInternetConnectionChanged(isInternetAvailable())
//                d("Internet Connection", isInternetAvailable().toString() )
                delay(timeInterval)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NetworkUtil.initialize(this)
//        if(sharedPrefs.getLoginStatus())
//            networkTicker=startRepeatedNetworkCheck(60000)
    }

    override fun onPause() {
        /**Clearing the data here as well, if activity loses focus or
         * user tries to directly close the application in Force logout case
         */
        if (logoutDialog != null)
            (this.application as MyApp).clearAllData()
        super.onPause()
//        if(sharedPrefs.getLoginStatus()) {
//            if (::networkTicker.isInitialized)
//                networkTicker.cancel()
//        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base))
    }

    open fun showDialog(dialogModel: DialogModel, eventListener: CommonDialogEventListener) {
        if (!::dialogViewModel.isInitialized) {
            dialogViewModel =
                ViewModelProvider(
                    this,
                    viewModelFactory
                ).get(DialogViewModel::class.java)
        }
        dialogViewModel.setDialogModel(dialogModel)
        dialogViewModel.setEventHandler(eventListener)
        showDialog()
    }

    fun onInternetConnectionChanged(connected: Boolean) {
        if (connected && !initialNetState) {
            initialNetState = true
            if(sharedPrefs.getLoginStatus())
                mHandler.postDelayed(
                    {
                        d("Internet Connection", "Pubnub")
                        pubnubHelper.getLastStatus(null)
                    }, 2000
                )
        } else if (!connected) {
            initialNetState = false
        }
    }

    @SuppressLint("CheckResult")
    open fun userLoggedIn(loginSource : String, isNewUser : Boolean) {
        var lastLoggedInAppLaunchCount = sharedPrefs.getLoggedInAppLaunchCountForRegionalAppNudge()
        sharedPrefs.saveLoggedInAppLaunchCountForRegionalAppNudge(++lastLoggedInAppLaunchCount)
        e("CalledFromLogin","inside userLoggedIn of BaseActivity")
        //track User Profile
        splashAnalytics.setUserIdentity(sharedPrefs.getMixPanelId())
        sharedPrefs.getLoginResponse()?.let {
            val profile = sharedPrefs.getSelectedProfile()
            it.firstName = profile?.firstName
            it.lastName = profile?.lastName
            it.emailId = profile?.emailId
            it.rmn = sharedPrefs.getClearRMN()
            splashAnalytics.upgradeUserProperty(
                sharedPrefs.getOriginalSubscriberId(),
                it,
                sharedPrefs.getFirstAppLaunchTimeInUTC() ?: "",
                sharedPrefs.getSubscribedPack()?.burnRateType ?: ""
            )
            splashAnalytics.setGroup(
                SID,
                sharedPrefs.getOriginalSubscriberId(),
                it,
                sharedPrefs.getFirstAppLaunchTimeInUTC() ?: "",
                sharedPrefs.getSubscribedPack()?.burnRateType ?: ""
            )
        }
        var userState= FREEMIUM
        sharedPrefs.getSubscribedPack()?.let {
            if (it.freeTrialStatus==true)
                userState= FREE_TRIAL_STARTED
            else{
                if (it.isInactive && it.freeTrialStatus==false)
                    userState= FREEMIUM
                else if (!it.isInactive && it.freeTrialStatus==false)
                    userState = EVENT_SUBSCRIBED
            }
        }
        loginAnalytics.trackLoginSuccess(
            RMN,
            SOURCE_OTP,
            sharedPrefs.getClearRMN(),
            loginSource,
            userState,
            isNewUser
        )
    }

}

inline fun <reified T : ViewModel> FragmentActivity.viewModel(
    factory: ViewModelProvider.Factory,
    body: T.() -> Unit
): T {
    val vm = ViewModelProviders.of(this, factory)[T::class.java]
    vm.body()
    return vm
}