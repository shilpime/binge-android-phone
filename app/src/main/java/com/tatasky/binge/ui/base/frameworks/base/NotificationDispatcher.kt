package com.tatasky.binge.ui.base.frameworks.base

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.tatasky.binge.ui.base.frameworks.extensions.CleverTapNotificationHandler
import com.tatasky.binge.utils.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class NotificationDispatcher : FragmentActivity() {
    val TAG = "NotificationDispatcher "
    companion object{
        fun navigateDeepLink(it: String,context: Context, extras: Bundle = Bundle()) {
            var launchIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(it)
            )
            launchIntent?.let {
                setPackageNameFromResolveInfoList(context, launchIntent)
            }

            launchIntent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (extras != null) {
                launchIntent.putExtras(extras)
            }
            //to prevent calling of pushNotificationClickedEvent(extras) in ActivityLifecycleCallback
            launchIntent.putExtra(WZRK_FROM_KEY, WZRK_FROM)
            if ( /*VERSION.SDK_INT < VERSION_CODES.S && */extras.containsKey(CLOSE_SYSTEM_DIALOGS)) {
                val closeNotificationDrawer =
                    extras.getBoolean(CLOSE_SYSTEM_DIALOGS)
                if (closeNotificationDrawer) {
                    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                }
            }
            context.startActivity(launchIntent)
        }

        var patternsRegx = arrayListOf<Pattern>(
            Pattern.compile("[/\\\\](detail)[/\\\\]([A-Za-z0-9_-]*)[/\\\\]([A-Za-z0-9_-]*)[/\\\\]([A-Za-z0-9_-]*)")
        )
        fun retriveProviderFromDeepLink(url: String):String {
            var providerName = ""
            try {
                var lastPath = Uri.parse(url).path
                if (lastPath != null) {
                    if (lastPath.startsWith("/detail", false)) {
                        for (item in patternsRegx) {
                            if (item.matcher(lastPath).matches()) {
                                val matcher: Matcher = item.matcher(lastPath)
                                while (matcher.find()) {
                                    providerName = matcher.group(4)
                                    return providerName
                                }
                            }
                        }

                    }
                }
            }catch (e:Exception){
                return ""
            }
            return providerName;
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            val intent = intent ?: return
            val extras = intent.extras ?: return
            val notifyMgr =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifyMgr.cancel(intent.getIntExtra("nt_id", -1))
            if (extras.containsKey(DEEP_LINK_KEY)) {
                val uri = intent.getStringExtra(DEEP_LINK_KEY)
                uri?.let {
                    var launchIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(it)
                    )
                    launchIntent?.let {
                        setPackageNameFromResolveInfoList(this, launchIntent)
                    }
                    launchIntent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    if (extras != null) {
                        launchIntent.putExtras(extras)
                    }
                    //to prevent calling of pushNotificationClickedEvent(extras) in ActivityLifecycleCallback
                    launchIntent.putExtra(WZRK_FROM_KEY, WZRK_FROM)
                    if ( /*VERSION.SDK_INT < VERSION_CODES.S && */extras.containsKey(CLOSE_SYSTEM_DIALOGS)) {
                        val closeNotificationDrawer =
                            extras.getBoolean(CLOSE_SYSTEM_DIALOGS)
                        if (closeNotificationDrawer) {
                            sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                        }
                    }
                    startActivity(launchIntent)
                }
            } else {
                CleverTapNotificationHandler().onHandleRedirection(this, extras)
            }

            finish()
        } catch (e: Exception) {
        }
    }



}
