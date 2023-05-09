package com.tatasky.binge.ui.base

import a.a.a.a.b.b
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.amp.CTPushAmpListener
import com.erosnow.partner.ENSDK
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hungama.sdk.player.HungamaPlayerManager
import com.irdeto.itac.ITACAgent
import com.irdeto.itac.ITACInitOptions
import com.irdeto.itac.ITACResult
//import com.m.x.player.tata.sdk.MxSDK
import com.moe.pushlibrary.MoEHelper
import com.moengage.core.LogLevel
import com.moengage.core.MoEngage
import com.moengage.core.analytics.MoEAnalyticsHelper
import com.moengage.core.config.LogConfig
import com.moengage.core.config.NotificationConfig
import com.moengage.core.model.AppStatus
import com.moengage.core.ktx.MoEngageBuilderKtx
import com.moengage.pushbase.MoEPushHelper
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.ANONYMOUS_ID
import com.tatasky.binge.analytics.MOE_PLATFORM
import com.tatasky.binge.analytics.PLATFORM
import com.tatasky.binge.data.di.modules.NetworkModule
import com.tatasky.binge.data.service.BingeNotificationService
import com.tatasky.binge.data.service.CustomNotificationRedirectionHandler
import com.tatasky.binge.data.service.CustomNotificationRedirectionHandlerClevertap
import com.tatasky.binge.helper.AppSignatureHelper
import com.tatasky.binge.ui.base.di.AppComponent
import com.tatasky.binge.ui.base.di.DaggerAppComponent
import com.tatasky.binge.ui.base.frameworks.base.BaseApplication
import com.tatasky.binge.utils.*
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump


open class MyApp : BaseApplication(),CTPushAmpListener {

    override fun onCreate() {
        ActivityLifecycleCallback.register(this)
        super.onCreate()

        //MX Player Initialization
//        MxSDK.Builder(this).debug(false)
//            .candidate(BuildConfig.FLAVOR == "uat")
////            .verbose()
//            .errorListener { error ->
//            e("MxSDKError","inside MX error : $error")
//            return@errorListener true
//        }.build()

        MoEngage.initialiseDefaultInstance(
            MoEngageBuilderKtx(
                application = this,
                appId = BuildConfig.MOENGAGE_APP_ID,
                notificationConfig = NotificationConfig(
                    smallIcon = R.drawable.ic_notification_icon_small,
                    largeIcon = R.drawable.ic_launcher,
                    notificationColor = R.color.darkPrimaryDark,
                    isMultipleNotificationInDrawerEnabled = true,
                    isBuildingBackStackEnabled = false,
                    isLargeIconDisplayEnabled = true
                ),
                fcmConfig = com.moengage.core.config.FcmConfig(true),
                pushKitConfig = com.moengage.core.config.PushKitConfig(true),
                geofenceConfig = com.moengage.core.config.GeofenceConfig(true)
            ).build()
        )
        val mMoEHelper = MoEAnalyticsHelper
        if (appComponent.sharedPreference().getMOEUserTracked()) {
            mMoEHelper.setAppStatus(this, AppStatus.UPDATE)
        } else {
            appComponent.sharedPreference().setMOEUserTracked(true)
            mMoEHelper.setAppStatus(this, AppStatus.INSTALL)
        }
        MoEPushHelper.getInstance().registerMessageListener(CustomNotificationRedirectionHandler())

        appComponent.appsflyerHelper() // Instantiate appsflyer at app level
        //Clevertap Changes
        CustomNotificationRedirectionHandlerClevertap().initialize(this)

        HungamaPlayerManager.initialize(this)
        if(BuildConfig.DEBUG){
            HungamaPlayerManager.getInstance().setSDKMode(b.b)
            //FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        } else {
            HungamaPlayerManager.getInstance().setSDKMode(b.a)
            //FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        initACA()
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath(getString(R.string.default_font))
                            .addCustomStyle(MaterialButton::class.java, R.attr.materialButtonStyle)
                            .addCustomStyle(SwitchMaterial::class.java, R.attr.switchStyle)
                            .addCustomStyle(MaterialCheckBox::class.java, R.attr.checkboxStyle)
                            .setFontAttrId(R.attr.fontPath)
                            .build()
                    )
                )
                .build()
        )
        if(appComponent.sharedPreference().getLoginStatus()) {
            initPubnub()
        }
        //        NetworkUtil.initialize(ViewPumpContextWrapper.wrap(this))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val transactionalName = getString(R.string.transactional_notification_channel)
            val watchName = getString(R.string.watch_notification_channel)
            val offersName = getString(R.string.offers_notification_channel)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mTransactionalChannel = NotificationChannel(TRANSACTIONAL_CHANNEL_ID, transactionalName, importance)
            val mWatchChannel = NotificationChannel(WATCH_CHANNEL_ID, watchName, importance)
            val mOffersChannel = NotificationChannel(OFFERS_CHANNEL_ID, offersName, importance)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mOffersChannel)
            notificationManager.createNotificationChannel(mTransactionalChannel)
            notificationManager.createNotificationChannel(mWatchChannel)
        }
        //CT Location
        CleverTapAPI.getDefaultInstance(this)?.enableDeviceNetworkInfoReporting(true)
//        if(appComponent.sharedPreference().getLoginStatus())
//            applicationContext?.let { playerEventRegisterForMitigationSession(appComponent.sharedPreference().getClearRMN() , it) }
//        generateHashKey()
    }

    private fun generateHashKey() {
        // This code requires one time to get Hash keys
        // After getting the key do comment this and share key
        val appSignature = AppSignatureHelper(this)
        d("AppSignature", appSignature.appSignatures.toString())
    }

    fun initPubnub() {
        val channelNameSub = "sub_" + appComponent.sharedPreference().getOriginalSubscriberId()
        val channelNameRmn = "rmn_" + appComponent.sharedPreference().getClearRMN()
        when (appComponent.sharedPreference().getDthStatusFreemium()) {
            DTH_W_BINGE_OLD_USER -> appComponent.pubnubHelper().initiatePubnub(channelNameSub)
            else -> appComponent.pubnubHelper().initiatePubnub(channelNameRmn)
        }
    }

    private var currentActivity: Activity? = null
    override val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .application(this)
            .network(NetworkModule(BuildConfig.BASE_URL))
            .build()
    }

    fun initACA(){
        agent = ITACAgent()
        val folderPath = getFolderPath(applicationContext)
        val itacInitOptions = ITACInitOptions(applicationContext, folderPath)
        val itacResult = agent.init(itacInitOptions)
        d("ACA", "itacResult: $itacResult")
        if (itacResult == ITACResult.ITAC_OK) {
            initSucceed = true
            d("ACA", "itacResult: $initSucceed")

        }
        if(BuildConfig.DEBUG) initSucceed = true
    }

    fun clearAllData(){
        ENSDK.logout()
        val channelNameSub = "sub_" + appComponent.sharedPreference().getOriginalSubscriberId()
        val channelNameRmn = "rmn_" + appComponent.sharedPreference().getClearRMN()
        when (appComponent.sharedPreference().getDthStatusFreemium()) {
            DTH_W_BINGE_OLD_USER -> appComponent.pubnubHelper().unsubscribe(channelNameSub)
            else -> appComponent.pubnubHelper().unsubscribe(channelNameRmn)
        }
        appComponent.sharedPreference().logoutUser()
        appComponent.moHelper().deleteAllNotification()
        appComponent.moHelper().logout()
//        appComponent.mpHelper().logout(appComponent.mpHelper().mMixpanelAPI)
//        appComponent.mpHelper().logout(appComponent.mpHelper().mMixpanelUnifiedAPI)
        appComponent.database().clearAllTables()
        appComponent.sharedPreference().getAnonymousId()?.let {
            appComponent.moHelper().setUniqueId(it)
            appComponent.moHelper().updateProperty(ANONYMOUS_ID, it)
            appComponent.moHelper().updateProperty(PLATFORM, MOE_PLATFORM)
        }
        appComponent.appsflyerHelper().generateAdditionalData()
    }

    companion object {
        var initSucceed: Boolean = false
        private lateinit var agent: ITACAgent

        fun getITACAgent(): ITACAgent {
            return agent
        }
    }

    override fun onPushAmpPayloadReceived(extras: Bundle?) {
        extras?.let {
            var localBroadcastHelper = appComponent.localBroadCastHelper()
            BingeNotificationService.renderPushNotification(this,localBroadcastHelper,it)
        }
    }

}
