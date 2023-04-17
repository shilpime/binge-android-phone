package com.tatasky.binge.data.service

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.sonylivandroidtssdk.SplashActivity
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION_ERROR
import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.base.MyApp
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity

import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.splash.AppSplashActivity
import com.tatasky.binge.utils.*
import java.util.*

class NotificationNavigator {

    private val MY_ACCOUNT = "MY_ACCOUNT"
    private val MANAGE_PACK = "MANAGE_PACK"
    private val SELFCARE_RECHARGE = "SELFCARE_RECHARGE"
    private val HELP_FAQ = "HELP_FAQ"
    private val WATCHLIST = "WATCHLIST"
    private val LOGIN = "LOGIN"
    private val HOME = "HOME"
    private val DETAIL_SCREEN = "DETAIL_SCREEN"
    private val SEE_ALL = "SEE_ALL"


    fun navigate(activity: Activity?, payload: Bundle?) {
        val pushPayload = mutableMapOf<String, String>(
            Pair(
                KEY_SCREEN_DATA,
                payload?.getString(KEY_SCREEN_DATA)!!
            )
        )
        var payloadData: MoEngageGenericModel? = null
        try {
            if (pushPayload[KEY_SCREEN_DATA] != null) {
                payloadData = Gson().fromJson<MoEngageGenericModel>(
                    pushPayload[KEY_SCREEN_DATA],
                    MoEngageGenericModel::class.java
                )

            }
            payloadData?.let {
                if (it.screenName.equals(KEY_NOTIFICATION_DETAIL)) {
                    val contentItem = Gson().fromJson(
                        Gson().toJson(it.any),
                        ContentItem::class.java
                    )
                    if (!isValidContent(contentItem) && contentItem.provider.isNotBlank()) {
                        throw Exception()
                    }
                }
            }
            if (payloadData?.screenName.isNullOrBlank()) {  // this condition is executed when payload is screenData : {}
                throw Exception()
            }
        } catch (e: Exception) {
            var homePayload = "{\n" +
                    "\"screenName\":\"HOME_ERROR\"\n" +
                    "}"

            payloadData = Gson().fromJson<MoEngageGenericModel>(
                homePayload,
                MoEngageGenericModel::class.java
            )
            pushPayload[KEY_SCREEN_DATA] = homePayload
        }
        val className =
            Class.forName(payload.getString("className") ?: AppSplashActivity::class.java.name)
        payloadData?.let {
            activity?.let {
                ContextCompat.startActivity(
                    it,
                    createIntentWithUri(
                        it,
                        className,
                        payloadData,
                        mutableMapOf<String, String>(
                            Pair(
                                KEY_SCREEN_DATA,
                                payload?.getString(KEY_SCREEN_DATA)!!
                            )
                        )
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }, payload
                )
            }
        }
    }

    private fun createIntentWithUri(
        context: Context,
        className: Class<*>,
        payloadData: MoEngageGenericModel,
        pushPayload: MutableMap<String, String>
    ): Intent {
        var error: Boolean = false

        val intent = when (payloadData.screenName.toUpperCase(Locale.getDefault())) {
            KEY_NOTIFICATION_GAMES -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_games,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_GAMES_HOME -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_games,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_MY_ACCOUNT -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_account,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_DETAIL -> {
                Intent(
                    Intent.ACTION_VIEW,
                    null,
                    context,
                    className
                ).apply { pushPayload.forEach { putExtra(it.key, it.value) } }
            }
            KEY_NOTIFICATION_SELFCARE -> {
                if (className == AppSplashActivity::class.java)
                    Intent(
                        Intent.ACTION_VIEW,
                        null,
                        context,
                        className
                    ).apply { this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_SELFCARE) }
                else {
                    Intent(context, RechargeActivity::class.java).apply {
                        putExtra(RechargeActivity.RECHARGE_SID, "YES")
                    }
                }
            }
            KEY_NOTIFICATION_WATCHLIST -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context?.getString(
                            R.string.deeplink_watchlist,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_HELP -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context?.getString(
                            R.string.deeplink_faq,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_HOME -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context?.getString(
                            R.string.deeplink_home,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                ).putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
            }
            KEY_NOTIFICATION_HOME_ERROR -> {
                error = true
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context?.getString(
                            R.string.deeplink_home,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_SEE_ALL -> {
                try {
                    val railItem = Gson().fromJson<HomeResponse.Items>(
                        Gson().toJson(payloadData.any),
                        HomeResponse.Items::class.java
                    )
                    if (railItem.sectionSource.equals(
                            ItemViewType.PROVIDER.name,
                            true
                        )
                    ) {
                        val uri = context?.getString(
                            R.string.deeplink_app_see_all,
                            BuildConfig.hostName,
                            railItem.id,
                            railItem.title
                        )
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri),
                            context,
                            className
                        )
                    } else {
                        val uri = context?.getString(
                            R.string.deeplink_see_all,
                            BuildConfig.hostName,
                            railItem.id,
                            railItem.title,
                            railItem.sectionSource,
                            railItem.placeHolder
                        )
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri),
                            context,
                            className
                        )
                    }
                } catch (e: Exception) {
                    error = true
                    Intent(
                        Intent.ACTION_VIEW,
                        null,
                        context,
                        className
                    ).apply { pushPayload.forEach { putExtra(it.key, it.value) } }
                }
            }
            KEY_NOTIFICATION_MANAGE_PACK -> {
                if (className == AppSplashActivity::class.java)
                    Intent(
                        Intent.ACTION_VIEW,
                        null,
                        context,
                        className
                    ).apply { this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_MANAGE_PACK) }
                else
                    getSubscriptionActivityIntent(
                        context,
                        fromLogin = true,
                        selectedAppId = null,
                        fromScreen = SOURCE_NOTIFICATION
                    )
            }
            KEY_NOTIFICATION_LOGIN -> {
                Intent(
                    Intent.ACTION_VIEW,
                    null,
                    context,
                    AppSplashActivity::class.java
                ).apply { putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_LOGIN) }
            }
            KEY_NOTIFICATION_PARTNER -> {
                try {
                    val args = Gson().fromJson<ContentItem>(
                        Gson().toJson(payloadData.any),
                        ContentItem::class.java
                    )
                    val uri = context?.getString(
                        R.string.deeplink_app_page,
                        BuildConfig.hostName,
                        args.pageType,
                        args.provider,
                        args.partnerId
                    )
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            uri
                        ),
                        context,
                        className
                    )
                } catch (e: Exception) {
                    error = true
                    Intent(
                        Intent.ACTION_VIEW,
                        null,
                        context,
                        className
                    ).apply { pushPayload.forEach { putExtra(it.key, it.value) } }
                }
            }
            else -> {
                error = true
                Intent(
                    Intent.ACTION_VIEW,
                    null,
                    context,
                    className
                )
            }
        }

//        val intent=Intent(
//            Intent.ACTION_VIEW,
//            Uri.parse(
//                context?.getString(
//                    R.string.deeplink_watchlist,
//                    BuildConfig.hostName
//                )
//            ),
//            context,
//            className
//        )
        if (error)
            intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION_ERROR)
        else
            intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
        return intent
    }


    fun startActivity(
        context: Context,
        sharedPreference: PrefsRepo,
        bundle: Bundle,
        java: Class<LandingActivity>
    ) {
        var intent: Intent? = null
        if (sharedPreference.isLoggedInWithPassword()) {
            intent = Intent(context, java)
        } else {
            intent = Intent(context, SplashActivity::class.java)
        }

        intent.putExtras(bundle)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
        Log.d("dfjkdf", "kdfjkdjfkd")
    }

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager: ActivityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(
                    packageName
                )
            ) {
                return true;
            }
        }
        return false;
    }


}