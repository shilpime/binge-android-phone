package com.tatasky.binge.data.service


import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.Logger
import com.clevertap.android.sdk.Utils
import com.clevertap.android.sdk.pushnotification.CTNotificationIntentService
import com.clevertap.android.sdk.pushnotification.PushNotificationHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper

import com.tatasky.binge.R
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.ui.base.frameworks.base.NotificationDispatcher
import com.tatasky.binge.ui.base.frameworks.extensions.toBundle
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.splash.AppSplashActivity
import com.tatasky.binge.utils.*
import dagger.android.AndroidInjection
import org.json.JSONArray
import javax.inject.Inject

class BingeNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var sharedPrefs: PrefsRepo

    @Inject
    lateinit var localBroadcastHelper: LocalBroadcastHelper

    @Inject
    lateinit var moengageHelper: MoEngageHelper

    private val MOENGAGE = "MOENGAGE"
    private val CLEVERTAP = "CLEVERTAP"


    /**
     * Clevertap Changes
     */
    override fun onNewToken(refreshedToken: String) {
        super.onNewToken(refreshedToken)

        /**
         * If you intend to use FCM with more than one platform, SDK, or both,
         * you must implement logic that collects the Device Token and passes
         * it to all relevant platforms. To do by extending a new instance of
         * the FirebaseMessagingService
         */
        // Sending new token to AppsFlyer
        Log.d("push_token", refreshedToken)
        moengageHelper.sendTokenToClevertap(refreshedToken)
        AppsFlyerLib.getInstance().updateServerUninstallToken(applicationContext, refreshedToken)
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }
    /**
     * Clevertap Changes
     */
    override fun onMessageReceived(p0: RemoteMessage) {
        val pushPayload = p0.data
        var payloadData: MoEngageGenericModel? = null
        if (MoEPushHelper.getInstance()
                .isFromMoEngagePlatform(p0.data) && sharedPrefs.getWatchNotificationAllowed()
        ) {
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
                /*var homePayload = "{\n" +
                        "\"screenName\":\"HOME_ERROR\"\n" +
                        "}"

                payloadData = Gson().fromJson<MoEngageGenericModel>(
                    homePayload,
                    MoEngageGenericModel::class.java
                )
                pushPayload[KEY_SCREEN_DATA] = homePayload*/
            }
            val isForeground = isAppOnForeground(this)
            pushPayload["className"] =
                if (isForeground) LandingActivity::class.java.name else AppSplashActivity::class.java.name
            //check for login notification and isLoggedIn
            /*TO DO...*/
            payloadData?.let {
                createNotification(
                    pushPayload
                )
            } ?: run {
                MoEFireBaseHelper.getInstance().passPushPayload(this, pushPayload)
            }
            localBroadcastHelper.sendBroadcast(
                this,
                localBroadcastHelper.ACTION_NOTIFICATION_RECEIVED
            )
        } else {
            //////////===============clevertap starts=================////////////////
            val pushPayload = p0.data
            val extras = pushPayload.toBundle()
            val info = CleverTapAPI.getNotificationInfo(extras)
            if (info.fromCleverTap) {
                renderPushNotification(this, localBroadcastHelper, pushPayload.toBundle())
            } else {
                //this case scenario will happen if recieved notification is not from Moengage or Clevertap
            }

            //////////===============clevertap ends=================////////////////
        }
    }

    private fun createNotification(
        pushPayload: MutableMap<String, String>
    ) {
        MoEFireBaseHelper.getInstance().passPushPayload(this, pushPayload)
    }


    companion object {

        fun renderPushNotification(
            context: Context,
            localBroadcastHelper: LocalBroadcastHelper,
            bundle: Bundle
        ) {

            var pushPayload = convertBundleToMap(bundle)
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
                val homePayload = "{\n" +
                        "\"screenName\":\"HOME_ERROR\"\n" +
                        "}"

                payloadData = Gson().fromJson<MoEngageGenericModel>(
                    homePayload,
                    MoEngageGenericModel::class.java
                )
                pushPayload[KEY_SCREEN_DATA] = homePayload
            }
            val isForeground = isAppOnForeground(context)
            pushPayload["className"] =
                if (isForeground) LandingActivity::class.java.name else AppSplashActivity::class.java.name
            var type = pushPayload["renderType"]
            if (type.equals("default", true)) {
                var bundle = Bundle()
                for (item in pushPayload.iterator()) {
                    bundle.putString(item.key, item.value)
                }
                CleverTapAPI.createNotification(context, bundle)
            } else {
                CleverTapAPI.processPushNotification(context, bundle)
                payloadData?.let {
                    createNativeNotification(
                        pushPayload, context
                    )
                }
            }
            localBroadcastHelper.sendBroadcast(
                context,
                localBroadcastHelper.ACTION_NOTIFICATION_RECEIVED
            )

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

        private fun createNativeNotification(
            pushPayload: MutableMap<String, String>,
            context: Context
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pushPayload[WZRK_SOUND]?.let {
                    CleverTapAPI.createNotificationChannel(
                        context,
                        pushPayload.get(WZRK_CHANNEL_ID).toString(),
                        pushPayload.get(WZRK_CHANNEL_ID).toString(),
                        "ChannelDescription",
                        NotificationManager.IMPORTANCE_MAX,
                        true,
                        pushPayload.get(WZRK_SOUND).toString()
                    )
                }
            } else {
                //This scenario happens when Device is less than Android 8 and notification channel is not supported for Android version less than 8
            }
            var bundle = Bundle()
            for (item in pushPayload.iterator()) {
                bundle.putString(item.key, item.value)
            }
            //val noificationId: Int = bundle.getInt("notificationId", -1)
            var notificationId: Int = bundle.getInt("notificationId", EMPTY_NOTIFICATION_ID)
            if (notificationId == EMPTY_NOTIFICATION_ID) {
                try {
                    val collapse_key: Any? = bundle.get(WZRK_COLLAPSE)
                    if (collapse_key != null) {
                        if (collapse_key is Number) {
                            notificationId = collapse_key.toInt()
                        } else if (collapse_key is String) {
                            notificationId = try {
                                collapse_key.toString().toInt()
                            } catch (e: NumberFormatException) {
                                (collapse_key.toString().hashCode())
                            }
                        }
                        notificationId =
                            kotlin.math.abs(notificationId) //Notification Id always needs to be positive
                    }
                } catch (e: NumberFormatException) {
                    // no-op
                }
            }


            val channelId: String = bundle.getString(WZRK_CHANNEL_ID, "")
            var priorityInt = NotificationCompat.PRIORITY_DEFAULT

            var notificationMessage: String = bundle.getString(NOTIF_MSG, "")
            notificationMessage = notificationMessage ?: ""
            var notificationTitle: String = bundle.getString(
                NOTIF_TITLE,
                ""
            )
            val priority: String? = bundle.getString(NOTIF_PRIORITY)
            if (priority != null) {
                if (priority == PRIORITY_HIGH_CT) {
                    priorityInt = NotificationCompat.PRIORITY_HIGH
                }
                if (priority == PRIORITY_MAX_CT) {
                    priorityInt = NotificationCompat.PRIORITY_MAX
                }
            }

            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val notifyIntent = Intent(context, NotificationDispatcher::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            notifyIntent.putExtras(bundle)
            notifyIntent.putExtra("nt_id", notificationId)
            val notifyPendingIntent = PendingIntent.getActivity(
                context, 0, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder: NotificationCompat.Builder = NotificationCompat.Builder(
                context, channelId
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    pushPayload.get(WZRK_CHANNEL_ID).toString(),
                    pushPayload.get(WZRK_CHANNEL_ID).toString(),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                setNotficationSound(bundle, builder, notificationChannel, context)
                notificationManager.createNotificationChannel(notificationChannel)
            }

            /// New Changes


            // uncommon - START
            var style: NotificationCompat.Style
            val bigPictureUrl: String? = bundle.getString(WZRK_BIG_PICTURE)
            if (bigPictureUrl != null && bigPictureUrl.startsWith("http")) {
                try {
                    val bpMap =
                        Utils.getNotificationBitmap(bigPictureUrl, false, context)
                            ?: throw java.lang.Exception("Failed to fetch big picture!")
                    style = if (bundle.containsKey(WZRK_MSG_SUMMARY)) {
                        val summaryText: String? = bundle.getString(WZRK_MSG_SUMMARY)
                        NotificationCompat.BigPictureStyle()
                            .setSummaryText(summaryText)
                            .bigPicture(bpMap)
                    } else {
                        NotificationCompat.BigPictureStyle()
                            .setSummaryText(notificationMessage)
                            .bigPicture(bpMap)
                    }
                } catch (t: Throwable) {
                    style = NotificationCompat.BigTextStyle()
                        .bigText(notificationMessage)
                }
            } else {
                style = NotificationCompat.BigTextStyle()
                    .bigText(notificationMessage)
            }
            style.let {
                builder.setStyle(it)
            }

            val requiresChannelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            if (requiresChannelId && bundle.containsKey(WZRK_SUBTITLE)) {
                builder.setSubText(bundle.getString(WZRK_SUBTITLE))
            }

            if (bundle.containsKey(WZRK_COLOR)) {
                val color = Color.parseColor(bundle.getString(WZRK_COLOR))
                builder.color = color
                builder.setColorized(true)
            }

            // Uncommon - START
            // add actions if any
            var actions: JSONArray? = null
            val actionsString: String? = bundle.getString(WZRK_ACTIONS)
            if (actionsString != null) {
                try {
                    actions = JSONArray(actionsString)
                } catch (t: Throwable) {

                }
            }
            setActionButtons(context, bundle, notificationId, builder, actions)
            // new changes
            //Constants.WZRK_SUBTITLE
            builder.priority = priorityInt
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
            builder.setContentTitle(notificationTitle); // make suer change the channel for image
            builder.setContentText(notificationMessage);
            val icoPath: String? = bundle.getString(NOTIF_ICON, "")
            if (icoPath?.isNotEmpty() == true) {
                builder.setLargeIcon(Utils.getNotificationBitmap(icoPath, true, context)) //uncommon
            } else {
                builder.setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.ic_launcher
                    )
                )
            }
            builder.setSmallIcon(R.drawable.ic_notification_icon_small)
            builder.setContentIntent(notifyPendingIntent)

            builder.setAutoCancel(true)
            val n: Notification = builder.build()
            notificationManager.notify(notificationId, n)
            CleverTapAPI.getDefaultInstance(context)?.pushNotificationViewedEvent(bundle)

        }

        private fun setNotficationSound(
            bundle: Bundle,
            builder: NotificationCompat.Builder,
            notificationChannel: NotificationChannel,
            context: Context
        ) {
            try {
                if (bundle.containsKey(WZRK_SOUND)) {
                    val soundUri: Uri? = getSoundUri(bundle, context)
                    if (soundUri != null) {
                        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build()

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // builder.setSound(soundUri)
                            notificationChannel.setSound(soundUri, audioAttributes)
                            // builder.setSound(soundUri)
                        } else {
                            builder.setSound(soundUri)
                        }
                    }
                }
            } catch (t: Throwable) {
            }
        }

        private fun getSoundUri(bundle: Bundle, context: Context): Uri? {
            var soundUri: Uri? = null
            val o: Any? = bundle.get(WZRK_SOUND)
            if (o is Boolean && o) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } else if (o is String) {
                var s = o
                if (s == "true") {
                    soundUri =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                } else if (!s.isEmpty()) {
                    if (s.contains(".mp3") || s.contains(".ogg") || s.contains(".wav")) {
                        s = s.substring(0, s.length - 4)
                    }
                    soundUri = Uri
                        .parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName
                                    + "/raw/" + s
                        )
                }
            }
            return soundUri
        }

        private fun setActionButtons(
            context: Context,
            extras: Bundle,
            notificationId: Int,
            nb: NotificationCompat.Builder, actions: JSONArray?
        ): NotificationCompat.Builder {
            val intentServiceName = getManifestKeyValue(LABEL_INTENT_SERVICE, context)
            var clazz: Class<*>? = null
            if (intentServiceName != null) {
                try {
                    clazz = Class.forName(intentServiceName)
                } catch (e: ClassNotFoundException) {
                    try {
                        clazz =
                            Class.forName("com.clevertap.android.sdk.pushnotification.CTNotificationIntentService")
                    } catch (ex: ClassNotFoundException) {
                        Logger.d("No Intent Service found")
                    }
                }
            } else {
                try {
                    clazz =
                        Class.forName("com.clevertap.android.sdk.pushnotification.CTNotificationIntentService")
                } catch (ex: ClassNotFoundException) {
                    Logger.d("No Intent Service found")
                }
            }
            val isCTIntentServiceAvailable = isServiceAvailable(context, clazz)
            if (actions != null && actions.length() > 0) {
                for (i in 0 until actions.length()) {
                    try {
                        val action = actions.getJSONObject(i)
                        val label = action.optString("l")
                        val dl = action.optString("dl")
                        val ico = action.optString(NOTIF_ICON)
                        val id = action.optString("id")
                        val autoCancel = action.optBoolean("ac", true)
                        if (label.isEmpty() || id.isEmpty()) {
                            Logger.d("not adding push notification action: action label or id missing")
                            continue
                        }
                        var icon = 0
                        if (!ico.isEmpty()) {
                            try {
                                icon = context.resources.getIdentifier(
                                    ico,
                                    "drawable",
                                    context.packageName
                                )
                            } catch (t: Throwable) {
                                Logger.d("unable to add notification action icon: " + t.localizedMessage)
                            }
                        }
                        var sendToCTIntentService =
                            (Build.VERSION.SDK_INT < 31 && autoCancel
                                    && isCTIntentServiceAvailable)
                        val dismissOnClick = extras.getString("pt_dismiss_on_click")
                        /**
                         * Send to CTIntentService in case (OS >= S) and notif is for Push templates with remind action
                         */
                        if (!sendToCTIntentService && PushNotificationHandler.isForPushTemplates(
                                extras
                            )
                            && id.contains("remind") && dismissOnClick != null &&
                            dismissOnClick.equals("true", ignoreCase = true) && autoCancel &&
                            isCTIntentServiceAvailable
                        ) {
                            sendToCTIntentService = true
                        }
                        /**
                         * Send to CTIntentService in case (OS >= S) and notif is for Push templates with pt_dismiss_on_click
                         * true
                         */
                        if (!sendToCTIntentService && PushNotificationHandler.isForPushTemplates(
                                extras
                            )
                            && dismissOnClick != null && dismissOnClick.equals(
                                "true",
                                ignoreCase = true
                            )
                            && autoCancel && isCTIntentServiceAvailable
                        ) {
                            sendToCTIntentService = true
                        }
                        var actionLaunchIntent: Intent?
                        if (sendToCTIntentService) {
                            actionLaunchIntent = Intent(CTNotificationIntentService.MAIN_ACTION)
                            actionLaunchIntent.setPackage(context.packageName)
                            actionLaunchIntent.putExtra(
                                KEY_CT_TYPE,
                                CTNotificationIntentService.TYPE_BUTTON_CLICK
                            )
                            if (!dl.isEmpty()) {
                                actionLaunchIntent.putExtra("dl", dl)
                            }
                        } else {
                            actionLaunchIntent = if (!dl.isEmpty()) {
//                            Intent(Intent.ACTION_VIEW, Uri.parse(dl))
                                Intent(context, NotificationDispatcher::class.java).apply {
                                    putExtra(DEEP_LINK_KEY, dl)
                                }
                            } else {
                                context.packageManager
                                    .getLaunchIntentForPackage(context.packageName)
                            }
                        }
                        if (actionLaunchIntent != null) {
                            actionLaunchIntent.putExtras(extras)
                            actionLaunchIntent.removeExtra(WZRK_ACTIONS)
                            actionLaunchIntent.putExtra("actionId", id)
                            actionLaunchIntent.putExtra("autoCancel", autoCancel)
                            actionLaunchIntent.putExtra("wzrk_c2a", id)
                            actionLaunchIntent.putExtra("notificationId", notificationId)
                            actionLaunchIntent.putExtra("nt_id", notificationId)
                            if (!dl.isEmpty()) {
                                actionLaunchIntent.putExtra(DEEP_LINK_KEY, dl)
                            }
                            actionLaunchIntent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        var actionIntent: PendingIntent?
                        val requestCode = System.currentTimeMillis().toInt() + i
                        var flagsActionLaunchPendingIntent = PendingIntent.FLAG_UPDATE_CURRENT
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            flagsActionLaunchPendingIntent =
                                flagsActionLaunchPendingIntent or PendingIntent.FLAG_IMMUTABLE
                        }
                        actionIntent = if (sendToCTIntentService) {
                            PendingIntent.getService(
                                context, requestCode,
                                actionLaunchIntent!!, flagsActionLaunchPendingIntent
                            )
                        } else {
                            PendingIntent.getActivity(
                                context, requestCode,
                                actionLaunchIntent, flagsActionLaunchPendingIntent
                            )
                        }
                        nb.addAction(icon, label, actionIntent)
                    } catch (t: Throwable) {
                        Logger.d("error adding notification action : " + t.localizedMessage)
                    }
                }
            } // Uncommon - END
            return nb
        }

        private fun isServiceAvailable(context: Context, clazz: Class<*>?): Boolean {
            if (clazz == null) {
                return false
            }
            val pm = context.packageManager
            val packageName = context.packageName
            val packageInfo: PackageInfo
            try {
                packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES)
                val services = packageInfo.services
                for (serviceInfo in services) {
                    if (serviceInfo.name == clazz.name) {
                        Logger.v("Service " + serviceInfo.name + " found")
                        return true
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Logger.d("Intent Service name not found exception - " + e.localizedMessage)
            }
            return false
        }

        private fun getManifestKeyValue(name: String, context: Context): String? {
            var metaData: Bundle? = null
            try {
                val pm: PackageManager = context.packageManager
                val ai = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                metaData = ai.metaData
            } catch (t: Throwable) {
                // no-op
            }
            if (metaData == null) {
                metaData = Bundle()
            }
            return try {
                val o = metaData[name]
                o?.toString()
            } catch (t: Throwable) {
                null
            }
        }

        private fun convertBundleToMap(bundle: Bundle): MutableMap<String, String> {
            val map: MutableMap<String, String> = mutableMapOf()
            val ks: Set<String> = bundle.keySet()
            val iterator = ks.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                var value = bundle.getString(key)
                value?.let {
                    map[key] = it
                }
            }
            return map
        }

    }


}

