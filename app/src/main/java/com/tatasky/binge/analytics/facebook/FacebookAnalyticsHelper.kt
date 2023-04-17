package com.tatasky.binge.analytics.facebook

import android.content.Context
import android.os.Bundle
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.utils.checkForValidEventName
import javax.inject.Singleton

/**
 * Created by Srikant on 18/5/21.
 */
@Singleton
class FacebookAnalyticsHelper(mContext: Context) {

	private var mFacebookEventsLogger: AppEventsLogger = AppEventsLogger.newLogger(mContext)

	init {
		if (BuildConfig.DEBUG) {
			FacebookSdk.setIsDebugEnabled(true);
			FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
		}
	}

	fun trackEvent(eventName: String, bundle: Bundle? = null) {
		try {
			mFacebookEventsLogger.logEvent(checkForValidEventName(eventName), bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackEventWithoutFormatting(eventName: String) {
		try {
			mFacebookEventsLogger.logEvent(eventName)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}