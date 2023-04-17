package com.tatasky.binge.ui.base.frameworks.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION_ERROR
import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.ui.base.frameworks.base.NotificationDispatcher
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.splash.AppSplashActivity
import com.tatasky.binge.utils.*
import org.json.JSONObject
import java.util.*

class CleverTapNotificationHandler {

    fun onHandleRedirection(activity: Context?, payload: Bundle?) {
        var baseBundle = payload
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
                var intent = createIntentWithUri(
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
                }
                ContextCompat.startActivity(
                    it,
                    intent, payload
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
            KEY_BINGE_LIST.uppercase(Locale.ENGLISH),
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
                try {
                    var contentItem = Gson().fromJson<HomeResponse.Items>(
                        Gson().toJson(payloadData.any),
                        HomeResponse.Items::class.java
                    )
                    val uri = context?.getString(
                        R.string.deeplink_subscription,
                        BuildConfig.hostName,
                        contentItem.action,
                        contentItem.packName
                    )
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(uri),
                        context,
                        className
                    )
                } catch (e: Exception) {
                    error = true
                    val uri = context?.getString(
                        R.string.deeplink_subscription,
                        BuildConfig.hostName,
                        "",
                        ""
                    )
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(uri),
                        context,
                        className
                    )
//                    e.printStackTrace()
                }
            }
            KEY_NOTIFICATION_LOGIN -> {
//                Intent(
//                    Intent.ACTION_VIEW,
//                    null,
//                    context,
//                    AppSplashActivity::class.java
//                ).apply { putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_LOGIN) }
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context?.getString(
                            R.string.deeplink_login,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_SEARCH.uppercase(Locale.ENGLISH) -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context?.getString(
                            R.string.deeplink_search,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_SETTING.uppercase(Locale.ENGLISH) -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context?.getString(
                            R.string.deeplink_settings,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_DEVICE_MANAGEMENT.uppercase(Locale.ENGLISH) -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context?.getString(
                            R.string.deeplink_device_management,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_CATEGORIES.uppercase(Locale.ENGLISH) -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context?.getString(
                            R.string.deeplink_categories,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                ).apply {
//                    try {
//                        var data = Gson().fromJson<MoEngageGenericModel>(
//                            pushPayload[KEY_SCREEN_DATA],
//                            MoEngageGenericModel::class.java
//                        )
//                        var menuItem = Gson().fromJson<LeftMenuItem>(
//                            data.any.toString(),
//                            LeftMenuItem::class.java
//                        )
//                        putExtra("category", Pair(menuItem.pageName, menuItem.pageType))
//                    } catch (e: Exception) {
//
//                    }
                }
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
//
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
}