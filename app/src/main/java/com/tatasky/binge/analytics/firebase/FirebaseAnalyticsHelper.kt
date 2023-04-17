package com.tatasky.binge.analytics.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.tatasky.binge.utils.checkForValidFirebaseEventName
import com.tatasky.binge.utils.checkForValidFirebaseParamName
import com.tatasky.binge.utils.e
import javax.inject.Singleton

/**
 * Created by Srikant on 18/5/21.
 */
@Singleton
class FirebaseAnalyticsHelper(mContext: Context) {

	private var mFirebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(mContext)

	fun trackEvent(eventName: String, bundle: Bundle? = null) {
		try {
			e(this.javaClass.name, "Event: $eventName, Param: $bundle")
			mFirebaseAnalytics.logEvent(checkForValidFirebaseEventName(eventName), checkForValidFirebaseParamName(bundle))
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackEventWithoutFormatting(eventName: String) {
		try {
			e(this.javaClass.name, "Event: $eventName")
			mFirebaseAnalytics.logEvent(eventName, null)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}