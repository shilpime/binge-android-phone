package com.tatasky.binge.data.di.modules

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.gson.GsonBuilder
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.analytics.PLATFORM
import com.tatasky.binge.data.networking.ApplicationApis
import com.tatasky.binge.data.networking.models.response.TransactionHistoryResponse
import com.tatasky.binge.data.networking.services.CommonService
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.utils.*
import dagger.Module
import dagger.Provides
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.collections.ArrayList


@Module
open class NetworkModule(private val baseUrl: String) {

    @Singleton
    @Provides
    fun provideInterceptors(): ArrayList<Interceptor> {

        val interceptors = arrayListOf<Interceptor>()
        if(BuildConfig.DEBUG) {
            val loggingInterceptorBasic = CurlLoggingInterceptor()
            interceptors.add(loggingInterceptorBasic)
        }
        return interceptors
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        interceptors: ArrayList<Interceptor>,
        sharedPref: PrefsRepo,
        application: Application,
        localBroadcastHelper: LocalBroadcastHelper
    ): Retrofit {
        val cache = Cache(application.cacheDir, 10 * 1024 * 1024) // 10 MB
        val timeout = sharedPref.getConnectionTimeout()
        val clientBuilder =
            OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .cache(cache)
        clientBuilder.addNetworkInterceptor(getNetworkInterceptor(application.applicationContext))
        clientBuilder.addInterceptor(provideOfflineCacheInterceptor(application.applicationContext))
        clientBuilder.addInterceptor(
            getHeaderInterceptor(
                sharedPref,
                application.applicationContext
            )
        )
        if (interceptors.isNotEmpty()) {
            interceptors.forEach { interceptor ->
                clientBuilder.addInterceptor(interceptor)
            }
        }
        clientBuilder.addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            if (response.code == RESPONSE_CODE_UNAUTHORIZED) {
                ///check shared pref action
//                if(sharedPref.getLoginStatus())
                localBroadcastHelper.sendBroadcast(application.applicationContext, localBroadcastHelper.ACTION_LOGOUT)
            }
            response
        }

        val gson = GsonBuilder()//.registerTypeAdapter(SubscriberIdListResponse::class.java, MyObjectDeserializer())
            .registerTypeAdapter(TransactionHistoryResponse::class.java, TransactionHistoryResponse.TransactionHistoryDeserializer()).create()

        return Retrofit.Builder()
            .client(clientBuilder.build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(baseUrl)
            .build()
    }

    /**
     * Provide Retrofit API's service from here
     */
    @Singleton
    @Provides
    fun provideUserAccountService(retrofit: Retrofit): CommonService {
        return CommonService(retrofit.create(ApplicationApis::class.java))
    }

    /**
     * Network Interceptor For API Call
     */
    private fun provideOfflineCacheInterceptor(context: Context): Interceptor {

        return object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()
                val maxAge = 24 * 60 * 60
                val cacheHeaderValue =
                    if (DeviceInfoUtils.hasNetwork(context))
                        "max-age=$maxAge"
                    else "only-if-cached, max-stale=$maxAge"
//                val request = originalRequest.newBuilder().build()
                val request = if (DeviceInfoUtils.hasNetwork(context)) {
                    originalRequest.newBuilder()
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build()
                } else {
                    originalRequest.newBuilder()
                        //.cacheControl(CacheControl.FORCE_CACHE)
                        .build()
                }
                val response = chain.proceed(request)
                return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheHeaderValue)
                    .build()
            }
        }
    }

    private fun getNetworkInterceptor(context: Context): Interceptor {
        return object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()
                val maxAge = 24 * 60 * 60//24hours
                val cacheHeaderValue =
                    if (DeviceInfoUtils.hasNetwork(context))
                        "public, max-age=$maxAge"
                    else "public, only-if-cached, max-stale=$maxAge"

                //val request = originalRequest.newBuilder().build()
                val request = if (DeviceInfoUtils.hasNetwork(context)) {
                    originalRequest.newBuilder()
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build()
                } else {
                    originalRequest.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build()
                }
                val response = chain.proceed(request)
                return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheHeaderValue)
                    .build()
            }

        }
    }

    /**
     * Header Interceptor For API Call
     */

    private fun getHeaderInterceptor(sharedPref: PrefsRepo, context: Context): Interceptor {
        return object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                var headerType = chain.request().headers(KEY_HEADER_TYPE)

                if (headerType.isNullOrEmpty()) {
                    headerType = arrayListOf(HEADER_TYPE_NONE)
                }
                val requestBuilder = chain.request().newBuilder()
                /*sharedPref.getAnonymousId()?.let {
                    requestBuilder.addHeader(
                        KEY_HEADER_ANONYMOUS_ID,
                        it
                    )
                }*/
                for(headers in headerType) {
                    when (headers) {
                        HEADER_TYPE_VRTA -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                            if (sharedPref.getLoginStatus())
                                requestBuilder.addHeader("rule", "BA")
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_BA)
                        }

                        HEADER_TYPE_VRTARAIL -> {
                            if (sharedPref.getLoginStatus()) {
                                requestBuilder.addHeader(
                                    KEY_HEADER_PROFILE_ID,
                                    sharedPref.getProfileId()!!
                                )

                                requestBuilder.addHeader(KEY_HEADER_DTH_STATUS,
                                    sharedPref.getDthStatusFreemium())

                                if (NON_DTH_USER.equals(sharedPref.getDthStatusFreemium(), true)) {
                                    requestBuilder.addHeader(KEY_HEADER_BAID,
                                        sharedPref.getBaId())
                                }

                                requestBuilder.addHeader(
                                    KEY_HEADER_AUTH,
                                    "bearer " + (sharedPref.getAccessToken()!!)
                                )
                                var bingeProduct = "FREE"
                                if(SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPref.getSubscribedPack()?.subscriptionStatus, true))
                                    bingeProduct = sharedPref.getSubscribedPack()?.subscribedBingeProduct ?: ""
                                requestBuilder.addHeader(
                                    KEY_HEADER_BINGE_PRODUCT,
                                    bingeProduct
                                )

                                requestBuilder.addHeader(
                                    KEY_HEADER_TICK_TICK,
                                    (sharedPref.getSubscribedPack()?.flexiPlan?:false).toString()
                                )
                                if(sharedPref.getSubscribedPack()?.flexiPlan == true){
                                    var partners = ArrayList<String>()
                                    sharedPref?.getSubscribedPack()?.getSelectedComponentAppList?.let {
                                        for(partner in it){
                                            partner.partnerName?.let { it1 -> partners.add(it1) }
                                        }
                                    }
                                    requestBuilder.addHeader(
                                        KEY_HEADER_PARTNERS,
                                        partners.joinToString (",")
                                    )
                                }
                            } else {
                                requestBuilder.addHeader(KEY_HEADER_DTH_STATUS,
                                    GUEST_USER)
                                sharedPref.getAnonymousId()?.let {
                                    requestBuilder.addHeader(KEY_HEADER_ANONYMOUS_ID, it)
                                }
                                sharedPref.getGuestProfileId()?.let {
                                    requestBuilder.addHeader(
                                        KEY_HEADER_PROFILE_ID,
                                        it
                                    )
                                }


                            }
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_TA)
                        }

                        HEADER_TYPE_IPAD -> {
                            if (sharedPref.getLoginStatus()) {
                                requestBuilder.addHeader(
                                    KEY_HEADER_PROFILE_ID,
                                    sharedPref.getProfileId()!!
                                )

                                requestBuilder.addHeader(KEY_HEADER_DTH_STATUS,
                                    sharedPref.getDthStatusFreemium())

                                if (NON_DTH_USER.equals(sharedPref.getDthStatusFreemium(), true)) {
                                    requestBuilder.addHeader(KEY_HEADER_BAID,
                                        sharedPref.getBaId())
                                }

                                requestBuilder.addHeader(
                                    KEY_HEADER_AUTH,
                                    "bearer " + (sharedPref.getAccessToken()!!)
                                )
                                var bingeProduct = "FREE"
                                if(SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPref.getSubscribedPack()?.subscriptionStatus, true))
                                    bingeProduct = sharedPref.getSubscribedPack()?.subscribedBingeProduct ?: ""
                                requestBuilder.addHeader(
                                    KEY_HEADER_BINGE_PRODUCT,
                                    bingeProduct
                                )

                                requestBuilder.addHeader(
                                    KEY_HEADER_TICK_TICK,
                                    (sharedPref.getSubscribedPack()?.flexiPlan?:false).toString()
                                )
                                if(sharedPref.getSubscribedPack()?.flexiPlan == true){
                                    var partners = ArrayList<String>()
                                    sharedPref?.getSubscribedPack()?.getSelectedComponentAppList?.let {
                                        for(partner in it){
                                            partner.partnerName?.let { it1 -> partners.add(it1) }
                                        }
                                    }
                                    requestBuilder.addHeader(
                                        KEY_HEADER_PARTNERS,
                                        partners.joinToString (",")
                                    )
                                }
                            } else {
                                requestBuilder.addHeader(KEY_HEADER_DTH_STATUS,
                                    GUEST_USER)
                                sharedPref.getAnonymousId()?.let {
                                    requestBuilder.addHeader(KEY_HEADER_ANONYMOUS_ID, it)
                                }
                                sharedPref.getGuestProfileId()?.let {
                                    requestBuilder.addHeader(
                                        KEY_HEADER_PROFILE_ID,
                                        it
                                    )
                                }


                            }
                            if(isTablet(context)){
                                requestBuilder.addHeader(KEY_DEVICE_TYPE, HEADER_VALUE_DEVICE_TYPE_TABLET)
                                requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_TA_TABLET)
                            }else{
                                requestBuilder.addHeader(KEY_DEVICE_TYPE, HEADER_VALUE_DEVICE_TYPE)
                                requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_TA)
                            }
                        }

                        HEADER_TYPE_TVOD -> {
                            requestBuilder.addHeader(
                                KEY_HEADER_AUTH,
                                "bearer " + (sharedPref.getAccessToken()!!)
                            )
                        }
                        HEADER_TYPE_NONE -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                        }
                        HEADER_TYPE_OLD_BA -> {
                            //deviceId
                            requestBuilder.addHeader(KEY_DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
                            sharedPref.getAccessToken()?.let {
                                requestBuilder.addHeader(KEY_HEADER_AUTH,
                                    it
                                )
                            }
                            requestBuilder.addHeader(KEY_HEADER_BAID, sharedPref.getBaId())
//                            requestBuilder.addHeader(KEY_HEADER_SUBSCRIBER_ID, sharedPref.getOriginalSubscriberId())
                            requestBuilder.addHeader(KEY_HEADER_MOBILE_NUMBER, sharedPref.getRMN())
                        }
                        HEADER_TYPE_BA -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_BA)
                            sharedPref.getDeviceToken()?.let {
                                requestBuilder.addHeader(KEY_HEADER_DEVICE_TOKEN, it)
                            }
                        }
                        HEADER_TYPE_BA_DEVICE_MANAGEMENT -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_BA)
                            sharedPref.getDeviceToken()?.let {
                                requestBuilder.addHeader(KEY_HEADER_DEVICE_TOKEN, it)
                            }
                            requestBuilder.addHeader(
                                KEY_HEADER_BEFORE_LOGIN,
                                if (sharedPref.getLoginStatus()) "false" else "true"
                            )
                            if(sharedPref.getDthStatusFreemium().isBlank()){
                                requestBuilder.addHeader(KEY_HEADER_DTH_STATUS, //"Non DTH User")
                                    sharedPref.getTempDthStatus()?:"")
                            }
                            if (sharedPref.getOriginalSubscriberId().isBlank())
                                sharedPref.getTempSavedSid()?.let {
                                    requestBuilder.addHeader(KEY_HEADER_SUBSCRIBER_ID, it)
                                }
                        }
                        HEADER_TYPE_PARENTAL_CONTROL -> {
                            requestBuilder.addHeader(KEY_HEADER_DTH_STATUS,sharedPref.getDthStatusFreemium())
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_BA)
                        }
                        HEADER_TYPE_FREEMIUM_ACCOUNT_DETAILS -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, "Android")
                            sharedPref.getDeviceToken()?.let {
                                requestBuilder.addHeader(KEY_HEADER_DEVICE_TOKEN, it)
                            }
                            requestBuilder.addHeader(KEY_HEADER_FREEMIUM_USER, (sharedPref.getDthStatusFreemium().equals(
                                NON_DTH_USER, true)).toString())
                            requestBuilder.addHeader(KEY_HEADER_AUTH_USER, sharedPref.getOriginalSubscriberId())
                        }
                        HEADER_TYPE_BA_CREATE -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, "Android")
                            sharedPref.getDeviceToken()?.let {
                                requestBuilder.addHeader(KEY_HEADER_DEVICE_TOKEN, it)
                            }
                        }
                        HEADER_TYPE_ALIAS -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_DEVICE_TYPE)
                            sharedPref.getDeviceToken()?.let {
                                requestBuilder.addHeader(KEY_HEADER_DEVICE_TOKEN, it)
                            }
                        }
                        HEADER_TYPE_VR_WITH_AUTH -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_BA)
                        }
                        HEADER_TYPE_VR_WITH_AUTH_NO_CACHE -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_BA)
                            requestBuilder.cacheControl(CacheControl.FORCE_NETWORK)
                        }
                        HEADER_TYPE_SID_AUTH -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_BA)
                            requestBuilder.addHeader(KEY_HEADER_BAID, sharedPref.getBaId())
                        }
                        HEADER_TYPE_DONGLE -> {
                            requestBuilder.addHeader(
                                KEY_HEADER_AUTH,
                                "bearer " + (sharedPref.getAccessToken())
                            )
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM)
                        }
                        HEADER_TYPE_CONFIG -> {
                            requestBuilder.addHeader(KEY_HEADER_VERSION, "v3")
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_DEVICE_TYPE)
                            requestBuilder.addHeader(KEY_HEADER_APP_VERSION, BuildConfig.VERSION_NAME)
                        }
                        HEADER_TYPE_VR_VALIDATE_OTP -> {
                            addVRHeader(requestBuilder, sharedPref, context)
                        }
                        HEADER_TYPE_SA -> {
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_DEVICE_TYPE)
                            requestBuilder.addHeader("rule", "BA")
                            requestBuilder.addHeader(KEY_HEADER_LOCALE, DeviceInfoUtils.locale)
                            requestBuilder.addHeader(KEY_DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
                            requestBuilder.addHeader(KEY_DEVICE_TYPE, HEADER_VALUE_DEVICE_TYPE)
                            requestBuilder.addHeader(
                                KEY_DEVICE_NAME,
                                DeviceInfoUtils.getDeviceName() ?: Build.MODEL
                            )

                            requestBuilder.addHeader(
                                KEY_HEADER_AUTH,
                                "bearer " + (sharedPref.getAccessToken()!!)
                            )
                        }
                        HEADER_TYPE_BA_LOGIN -> {
                            sharedPref.getAnonymousId()?.let {
                                requestBuilder.addHeader(
                                    KEY_HEADER_ANONYMOUS_ID,
                                    it
                                )
                            }
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, "Android")
                            requestBuilder.addHeader(KEY_HEADER_LOCALE, DeviceInfoUtils.locale)
                            requestBuilder.addHeader(KEY_DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
                            requestBuilder.addHeader(KEY_DEVICE_TYPE, HEADER_VALUE_DEVICE_TYPE)
                            requestBuilder.addHeader(
                                KEY_DEVICE_NAME,
                                DeviceInfoUtils.getDeviceName() ?: Build.MODEL
                            )
                            requestBuilder.addHeader(
                                KEY_HEADER_AUTH,
                                "bearer " + (sharedPref.getLoginAccessToken())
                            )
                            sharedPref.getLoginDeviceToken().let {
                                requestBuilder.addHeader(KEY_HEADER_DEVICE_TOKEN, it)
                            }
                            requestBuilder.cacheControl(CacheControl.FORCE_NETWORK)
                        }
                        HEADER_TYPE_CONTROL_CHANGE ->{
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_TA)
                            requestBuilder.addHeader(KEY_HEADER_AUTH_APP_ID, BuildConfig.AUTH_APP_ID)
                            requestBuilder.addHeader(KEY_HEADER_AUTH_APP_KEY, BuildConfig.AUTH_APP_KEY)
                            sharedPref.getOriginalSubscriberId().takeIf { !it.isBlank() }?.let {
                                requestBuilder.addHeader(KEY_HEADER_AUTH_SUB_ID, it)
                            }
                            requestBuilder.addHeader(KEY_HEADER_AUTHORIZATION, "bearer " +sharedPref.getAccessToken()!!)
                            requestBuilder.addHeader(KEY_HEADER_AUTH_DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
                            requestBuilder.addHeader(KEY_HEADER_AUTH_DEVICE_TYPE, HEADER_VALUE_DEVICE_TYPE.toUpperCase())
                            requestBuilder.addHeader(KEY_HEADER_AUTH_SUB_NAME, "Riaz")
                            requestBuilder.addHeader(KEY_HEADER_AUTH_DEVICE_PLATFORM, "MOBILE")
                            requestBuilder.cacheControl(CacheControl.FORCE_NETWORK)
                        }
                        HEADER_TYPE_NO_CACHE->{
                            requestBuilder.cacheControl(CacheControl.FORCE_NETWORK)
                        }
                        HEADER_TYPE_DSN->{
                            requestBuilder.addHeader(KEY_HEADER_TYPE_PARTNER_UNIQUE_ID, sharedPref.getPartnerUniqueId())
                        }
                        HEADER_TYPE_ZEE5_UNIQUE_ID -> {
                            requestBuilder.addHeader(KEY_HEADER_TYPE_PARTNER_UNIQUE_ID,
                                sharedPref.getPartnerUniqueIdInfo(
                                    PROVIDER_ZEE5
                                )
                            )
                        }
                        HEADER_TYPE_VOOT_KIDS_UNIQUE_ID -> {
                            requestBuilder.addHeader(KEY_HEADER_TYPE_PARTNER_UNIQUE_ID,
                                sharedPref.getPartnerUniqueIdInfo(
                                    PROVIDER_VOOTKIDS
                                )
                            )
                        }
                        HEADER_TYPE_VOOT_SELECT_UNIQUE_ID -> {
                            requestBuilder.addHeader(KEY_HEADER_TYPE_PARTNER_UNIQUE_ID,
                                sharedPref.getPartnerUniqueIdInfo(
                                    PROVIDER_VOOTSELECT
                                )
                            )
                        }
                        HEADER_TYPE_BAID->{
                            requestBuilder.addHeader(KEY_HEADER_BAID, sharedPref.getBaId())
                        }
                        HEADER_TYPE_HELP_CENTER_URL->{
                            addVRHeader(requestBuilder, sharedPref, context)
                            requestBuilder.addHeader(KEY_HEADER_BAID, sharedPref.getBaId())
                        }

                        HEADER_TYPE_CW -> {
                            requestBuilder.addHeader(KEY_HEADER_PLATFORM, HEADER_VALUE_PLATFORM_BA)
                            if (sharedPref.getLoginStatus()) {
                                addVRHeader(requestBuilder, sharedPref, context)
                            } else {
                                sharedPref.getAnonymousId()?.let {
                                    requestBuilder.addHeader(KEY_HEADER_UNIQUE_ID, it)
                                }
                                sharedPref.getGAuthToken()?.let {
                                    requestBuilder.addHeader(
                                        KEY_HEADER_GAUTH_TOKEN,
                                        it
                                    )
                                }
                            }
                        }
                    }
                }
                sharedPref.getOriginalSubscriberId().takeIf { !it.isBlank() }?.let {
                    requestBuilder.addHeader(KEY_HEADER_SUBSCRIBER_ID, it)
                }
                if(BuildConfig.FLAVOR.equals("staging")){
                    requestBuilder.addHeader(KEY_SOURCE_TYPE, KEY_SOURCE_STAGE)
                }
                else if(BuildConfig.FLAVOR.equals("uat")){
                    requestBuilder.addHeader(KEY_SOURCE_TYPE, KEY_SOURCE_UAT)
                }
                else{
                    requestBuilder.addHeader(KEY_SOURCE_TYPE, KEY_SOURCE_PROD)
                }
                val method = chain.request().method
                if(method.equals("POST", true)){
                    requestBuilder.cacheControl(CacheControl.FORCE_NETWORK)
                }
                requestBuilder.removeHeader(KEY_HEADER_TYPE)
                val request = requestBuilder.build()
                return chain.proceed(request)
            }
        }
    }

    /**
     * Add VR Header Values
     * @param requestBuilder
     * @return Request.Builder
     */
    private fun addVRHeader(
        requestBuilder: Request.Builder,
        sharedPref: PrefsRepo,
        context: Context
    ): Request.Builder {
        requestBuilder.addHeader("rule", "BA")
        requestBuilder.addHeader(KEY_HEADER_LOCALE, DeviceInfoUtils.locale)
        requestBuilder.addHeader(KEY_DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
        requestBuilder.addHeader(KEY_DEVICE_TYPE, HEADER_VALUE_DEVICE_TYPE)
        requestBuilder.addHeader(KEY_DEVICE_NAME, DeviceInfoUtils.getDeviceName() ?: Build.MODEL)
        sharedPref.getAnonymousId()?.let {
            requestBuilder.addHeader(
                KEY_HEADER_ANONYMOUS_ID,
                it
            )
        }
        if (!sharedPref.getLoginStatus()) {
            requestBuilder.addHeader(
                KEY_HEADER_AUTH,
                "bearer " + (sharedPref.getLoginAccessToken())
            )
            sharedPref.getLoginDeviceToken().let {
                requestBuilder.addHeader(KEY_HEADER_DEVICE_TOKEN, it)
            }

        }
        else{
            requestBuilder.addHeader(
                KEY_HEADER_PROFILE_ID,
                sharedPref.getProfileId()!!
            )
            requestBuilder.addHeader(
                KEY_HEADER_AUTH,
                "bearer " + (sharedPref.getAccessToken()!!)
            )
            requestBuilder.addHeader(KEY_HEADER_DTH_STATUS, //"Non DTH User")
                sharedPref.getDthStatusFreemium())
            requestBuilder.addHeader(
                KEY_SUBSCRIPTION_TYPE,
                sharedPref.getSubscriptionType()
            )
        }
        return requestBuilder
    }

}
