package com.tatasky.binge.ui.features.prime

import android.text.TextUtils
import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import org.json.JSONObject
import java.util.HashMap

/**
 * Created by Srikant on 11/06/21.
 */
class PrimeAnalytics(
	private val mixpanelHelper: MixpanelHelper,
	private val moEngageHelper: MoEngageHelper,
	private val appsFlyerHelper: AppsFlyerHelper
) {

	fun trackPrimePackAdd(type: String) {
		trackMixpanelPrimePackAdd(type)
		trackMoEngagePrimePackAdd()
		trackAppsFlyerPrimeVisit()
	}

	fun trackPrimePackAddContinue(type: String) {
		trackMixpanelPrimePackAddContinue(type)
		trackMoEngagePrimePackAddContinue(type)
		trackAppsFlyerPrimePackAddContinue(type)
	}

	fun trackPrimePackAddSkip(type: String, source: String?) {
		trackMixpanelPrimePackAddSkip(type)
		trackMoEngagePrimePackAddSkip(type)
		trackAppsFlyerPrimePackAddSkip(type, source)
	}

	fun trackPrimeLowBalance(type: String) {
		trackMixpanelPrimeLowBalance(type)
		trackMoEngagePrimeLowBalance(type)
	}

	fun trackPrimeLowBalanceCancel(type: String) {
		trackMixpanelPrimeLowBalanceCancel(type)
		trackMoEngagePrimeLowBalanceCancel(type)
	}

	fun trackPrimeSuspended(action: String) {
		trackMixpanelPrimeSuspended(action)
		trackMoEngagePrimeSuspended(action)
	}

	fun trackPrimeResumeSuccess(sid: String) {
		trackMixpanelPrimeResumeSuccess(sid)
		trackMoEngagePrimeResumeSuccess()
	}

	private fun trackAppsFlyerPrimeVisit() {
		appsFlyerHelper.trackEvent(EVENT_PRIME_UPSELL)
	}

	private fun trackAppsFlyerPrimePackAddSkip(type: String, source: String?) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_PACKAGE_TYPE] = type
//			parameterAndValueMap[PARA_SOURCE] = source ?: ""
			appsFlyerHelper.trackEvent(EVENT_PRIME_ADD_SKIP, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackAppsFlyerPrimePackAddContinue(type: String) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_PACKAGE_TYPE] = type
			appsFlyerHelper.trackEvent(EVENT_PRIME_ADD_CONTINUE, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixpanelPrimePackAdd(type: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.apply {
				put(PARA_PACKAGE_TYPE, type)
			}
			mixpanelHelper.trackEvent(EVENT_PRIME_ADD,mixpanelHelper.mMixpanelAPI)
			mixpanelHelper.trackEvent(EVENT_PRIME_ADD, jsonObject)
		} catch (e: Exception) {
		}
	}

	private fun trackMoEngagePrimePackAdd() {
		try {
			moEngageHelper.trackEvent(EVENT_PRIME_ADD)
		} catch (e: Exception) {
		}
	}

	private fun trackMixpanelPrimePackAddContinue(type: String) {
		try {
			val jsonObject = JSONObject().apply {
			put(PARA_PACKAGE_TYPE, type)
			}
			mixpanelHelper.trackEvent(EVENT_PRIME_ADD_CONTINUE, jsonObject,mixpanelHelper.mMixpanelAPI)
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_PACKAGE_TYPE, type)
			}
			mixpanelHelper.trackEvent(EVENT_PRIME_ADD_CONTINUE, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {
		}
	}

	private fun trackMoEngagePrimePackAddContinue(type: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_PACKAGE_TYPE, type)
			moEngageHelper.trackEvent(EVENT_PRIME_ADD_CONTINUE, payloadBuilder)
		} catch (e: Exception) {
		}
	}

	private fun trackMixpanelPrimePackAddSkip(type: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_PACKAGE_TYPE, type)
			mixpanelHelper.trackEvent(EVENT_PRIME_ADD_SKIP, jsonObject)
		} catch (e: Exception) {
		}
	}

	private fun trackMoEngagePrimePackAddSkip(type: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_PACKAGE_TYPE, type)
			moEngageHelper.trackEvent(EVENT_PRIME_ADD_SKIP, payloadBuilder)
		} catch (e: Exception) {
		}
	}

	private fun trackMixpanelPrimeLowBalance(type: String) {
		try {
			val jsonObject = JSONObject().apply {
			put(PARA_PACKAGE_TYPE, type)
			}
			mixpanelHelper.trackEvent(EVENT_PRIME_LOW_BALANCE, jsonObject,mixpanelHelper.mMixpanelAPI)
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_PACKAGE_TYPE, type)
			}
			mixpanelHelper.trackEvent(EVENT_PRIME_LOW_BALANCE, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {
		}
	}

	private fun trackMoEngagePrimeLowBalance(type: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_PACKAGE_TYPE, type)
			moEngageHelper.trackEvent(EVENT_PRIME_LOW_BALANCE, payloadBuilder)
		} catch (e: Exception) {
		}
	}

	private fun trackMixpanelPrimeLowBalanceCancel(type: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_PACKAGE_TYPE, type)
			mixpanelHelper.trackEvent(EVENT_PRIME_LOW_BALANCE_SKIP, jsonObject)
		} catch (e: Exception) {
		}
	}

	private fun trackMoEngagePrimeLowBalanceCancel(type: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_PACKAGE_TYPE, type)
			moEngageHelper.trackEvent(EVENT_PRIME_LOW_BALANCE_SKIP, payloadBuilder)
		} catch (e: Exception) {
		}
	}

	private fun trackMixpanelPrimeSuspended(action: String) {
		try {
			val jsonObject = JSONObject().apply {
			put(PARA_ACTION, action)
			}
			mixpanelHelper.trackEvent(EVENT_PRIME_SUSPEND, jsonObject,mixpanelHelper.mMixpanelAPI)
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_ACTION, action)
			}
			mixpanelHelper.trackEvent(EVENT_PRIME_SUSPEND, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {
		}
	}

	private fun trackMoEngagePrimeSuspended(action: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_ACTION, action)
			moEngageHelper.trackEvent(EVENT_PRIME_SUSPEND, payloadBuilder)
		} catch (e: Exception) {
		}
	}

	private fun trackMixpanelPrimeResumeSuccess(sid: String) {
		try {
			mixpanelHelper.trackEvent(EVENT_PRIME_RESUME_SUCCESS,mixpanelHelper.mMixpanelAPI)

			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SID, sid)
			}
			mixpanelHelper.trackEvent(EVENT_PRIME_RESUME_SUCCESS, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {
		}
	}

	private fun trackMoEngagePrimeResumeSuccess() {
		try {
			moEngageHelper.trackEvent(EVENT_PRIME_RESUME_SUCCESS)
		} catch (e: Exception) {
		}
	}
}