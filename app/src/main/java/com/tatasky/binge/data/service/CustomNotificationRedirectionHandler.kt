package com.tatasky.binge.data.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import com.google.gson.Gson
import com.moengage.core.*
import com.moengage.pushbase.model.NotificationPayload
import com.moengage.pushbase.push.PushMessageListener
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION_ERROR
import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.splash.AppSplashActivity
import com.tatasky.binge.utils.*
import java.util.*

class CustomNotificationRedirectionHandler : PushMessageListener() {
    override fun onCreateNotification(
        context: Context,
        notificationPayload: NotificationPayload
    ): NotificationCompat.Builder {
        val builder = super.onCreateNotification(context, notificationPayload)
        builder.setSmallIcon(R.drawable.ic_notification_icon_small)
            .setContentTitle(notificationPayload.text.title)
            .setContentText(notificationPayload.text.message)
            .setAutoCancel(true)
        // Set the intent that will fire when the user taps the notification
        return builder
    }

    override fun onNotificationClick(activity: Activity, payload: Bundle) {
        e("onNotificationClick","payload: $payload")
        val isDefaultAction = payload.getBoolean("moe_isDefaultAction")// IS_DEFAULT_ACTION
        val navigationType = payload.getString(PUSH_NOTIFICATION_TYPE)
        if (isDefaultAction) {
            when (navigationType) {
                PUSH_NOTIFICATION_TYPE_NORMAL_NOTIFICATION -> {
                    if (payload.getString(PUSH_NOTIFICATION_NAVIGATION_ACTIVITY_NAME)
                        == "com.moe.pushlibrary.activities.MoEActivity") {
                        super.onNotificationClick(activity, payload)
                        return
                    }
                }
                PUSH_NOTIFICATION_TYPE_DEEP_LINK_NOTIFICATION -> {
                    navigateUsingDeeplink(activity, payload)
                    return
                }

            }
        }

        val pushPayload = mutableMapOf<String, String>(
            Pair(
                KEY_SCREEN_DATA,
                payload.getString(KEY_SCREEN_DATA)!!
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
                startActivity(it,
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

    /**
     * Navigate using Deep link URI
     * @link https://docs.google.com/spreadsheets/d/1G1keweO5PnYOqKmAIr3FseiezhRrFnumHVFEylvG-zY/edit#gid=0
     * */
    private fun navigateUsingDeeplink(activity: Activity, payload: Bundle) {
        val deeplinkUri =
            payload.getString(PUSH_NOTIFICATION_NAVIGATION_DEEPLINK_LEGACY, null).toUri()
        val className =
            Class.forName(payload.getString("className") ?: AppSplashActivity::class.java.name)
        startActivity(
            activity,
            Intent(
                Intent.ACTION_VIEW,
                deeplinkUri,
                activity,
                className
            ).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
            },
            null
        )
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
                        context.getString(
                            R.string.deeplink_recharge,
                            BuildConfig.hostName
                        ).toUri(),
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
                val subscriptionUri =
                    "${
                        context
                            .getString(
                                R.string.deeplink_subscription_generic_placeholder,
                                BuildConfig.hostName
                            )
                    }?${payloadData.any}"
                Intent(
                    Intent.ACTION_VIEW,
                    subscriptionUri.toUri(),
                    context,
                    className
                ).apply {
                    if (payloadData.any == null)
                        this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_MANAGE_PACK)
                    else
                        this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_MANAGED_APP_USE_CASE)
                }
            }
            KEY_NOTIFICATION_LOGIN -> {
                Intent(
                    Intent.ACTION_VIEW,
                    context.getString(
                        R.string.deeplink_login,
                        BuildConfig.hostName
                    ).toUri(),
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
        if (error)
            intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION_ERROR)
        else
            intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
        return intent
    }
}