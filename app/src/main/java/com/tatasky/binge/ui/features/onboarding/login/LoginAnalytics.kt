package com.tatasky.binge.ui.features.onboarding.login

import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsConstants
import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.facebook.FacebookAnalyticsHelper
import com.tatasky.binge.analytics.firebase.FirebaseAnalyticsHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.utils.getTimeInUTC
import com.tatasky.binge.utils.DeviceInfoUtils
import com.tatasky.binge.utils.UserDetailsUtil
import com.tatasky.binge.utils.maskLast5PhoneNumberDigits
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.HashMap

class LoginAnalytics(private val mixpanelHelper: MixpanelHelper, private val moEngageHelper: MoEngageHelper, private val firebaseAnalytics: FirebaseAnalyticsHelper, private val facebookAnalyticsHelper: FacebookAnalyticsHelper, private val appsFlyerHelper: AppsFlyerHelper) {

	fun trackPinEntryIncorrect(source: String, deviceType: String) {
		trackMixPanelPinEntryIncorrect(source, deviceType)
	}

	fun trackPinEntryNotNow(source: String, deviceType: String) {
		trackMixPanelPinEntryNotNow(source, deviceType)
	}

	fun trackPinEntryForgot(source: String, deviceType: String) {
		trackMixPanelPinEntryForgot(source, deviceType)
	}

	fun trackPinEntryProceed(source: String) {
		trackMixPanelPinEntryProceed(source)
	}

	fun trackPinEntryInitiate(source: String, deviceType: String) {
		trackMixPanelPinEntryInitiate(source, deviceType)
	}
	fun trackCreatePinCancel(source: String) {
		trackMixPanelCreatePinCancel(source)
	}

	fun trackCreatePinProceed(source: String) {
		trackMixPanelCreatePinProceed(source)
	}

	fun trackCreatePinInitiate(source: String) {
		trackMixPanelCreatePinInitiate(source)
	}

	private fun trackAppsFlyerViewLoginScreen(source: String) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_SOURCE] = source
			appsFlyerHelper.trackEvent(EVENT_VIEW_LOGIN, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackParentalPinOtpProceed(source: String) {
		trackMixPanelParentalPinOtpProceed(source)
	}

	fun trackGetOtp(){
		trackMixPanelLoginGetOtp()
	}

	fun trackLoginPageVisit(type: String, source: String){
		trackMixPanelLoginPageVisit(type, source)
	}

	fun trackLoginPageNewLogin(){
		trackMixPanelLoginPageNewLogin()
	}



	fun trackStartTrialEvent(){
		facebookAnalyticsHelper.trackEventWithoutFormatting(AppEventsConstants.EVENT_NAME_START_TRIAL)
		firebaseAnalytics.trackEventWithoutFormatting(EVENT_START_TRIAL)
	}

	fun trackOTPReceived() {
		trackMixPanelOTPReceived()
		trackMoEngageOTPReceived()
	}

	//LOGIN-ENTER
	fun trackLoginInitiate(source: String) {
		trackMixPanelLoginInitiate(source)
		trackFirebaseLoginInitiate(source)
		trackFacebookLoginInitiate(source)
		trackMoEngageLoginInitiate(source)
		trackAppsFlyerViewLoginScreen(source)
	}

	fun trackLoginMaxDevice(sid: String) {
		trackMixPanelMaxDevice(sid)
		trackMoEngageMaxDevice(sid)
	}

	fun trackOtpEnter(device:String) {
		trackMixPanelOtpEnter(device)
		trackFirebaseOtpEnter()
		trackFacebookOtpEnter()
		trackMoEngageOtpEnter()
	}

	fun trackLoginPageNotNow(){
		trackMixPanelLoginPageNotNow()
	}

	fun trackLoginRmnEnterInvalid(context: Context) {
		trackMixPanelLoginRmnEnterInvalid(context)
	}

	fun trackLoginRmnEnter(){
		trackMixPanelLoginRmnEnter()
	}

	fun trackLicAgreement(){
		trackMixPanelLoginLicAgreement()
	}

	fun trackLoginLicAgreementBack(){
		trackMixPanelLoginLicAgreementBack()
	}

	fun trackPasswordScreen() {
		trackMixPanelPasswordScreen()
		trackFirebasePasswordScreen()
		trackFacebookPasswordScreen()
		trackMoEngagePasswordScreen()
	}

	fun trackForgetPasswordClick(sid: String) {
		trackMixPanelForgetPasswordClick(sid)
		trackMoEngageForgetPasswordClick(sid)
	}

	fun trackPasswordResetFailure(reason: String, sid: String) {
		trackMixPanelPasswordResetFailure(reason, sid)
		trackMoEngagePasswordResetFailure(reason, sid)
	}

	fun trackForgetPasswordSuccess(sid: String) {
		trackMixPanelPasswordResetSuccess(sid)
		trackMoEngagePasswordResetSuccess(sid)
	}

	fun trackOtpResend() {
		trackMixPanelOtpResend()
		trackFirebaseOtpResend()
		trackFacebookOtpResend()
		trackMoEngageOtpResend()
	}

	fun trackOtpInvoked(type: String, auth: String, value: String, source: String) {
		trackMixPanelOtpInvoked()
		trackFirebaseOtpInvoked(type, auth, value, source)
		trackFacebookOtpInvoked(type, auth, value, source)
		trackMoEngageOtpInvoked()
		trackAppsFlyerLoginOtpRequested(type, auth, value, source)
	}

	fun trackLoginSuccess(type: String, auth: String, value: String, source: String,userState:String, isNewUser: Boolean) {
		trackMixPanelLoginSuccess(type, value, source,userState)
		trackFirebaseLoginSignUpSuccess(type, auth, value, source, isNewUser)
		trackFacebookLoginSignUpSuccess(type, auth, value, source, isNewUser)
		trackMoEngageLoginSuccess(type, value, source)
		trackAppsFlyerLoginSignUpSuccess(type, auth, value, source, isNewUser)
	}

	fun trackLogout() {
		trackMixPanelLogout()
		trackMoEngageLogout()
	}

	fun trackLogoutFailed(reason: String) {
		trackMixPanelLogoutFailed(reason)
		trackMoEngageLogoutFailed(reason)
	}

	private fun trackAppsFlyerLoginOtpRequested(
		type: String,
		auth: String,
		value: String,
		source: String
	) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_TYPE] = type
			parameterAndValueMap[PARA_AUTH] = auth
			parameterAndValueMap[PARA_VALUE] = value.maskLast5PhoneNumberDigits()
			parameterAndValueMap[PARA_SOURCE] = source
			appsFlyerHelper.trackEvent(LOGIN_OTP_REQUESTED, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageLogout() {
		try {
			moEngageHelper.trackEvent(EVENT_MOE_LOGOUT)
		} catch (e:Exception){}
	}

	private fun trackMoEngageLogoutFailed(reason: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_REASON, reason)
			moEngageHelper.trackEvent(EVENT_LOGOUT_FAILED, payloadBuilder)
		} catch (e:JSONException){
			e.printStackTrace()
		}
	}
	private fun trackMixPanelLogout() {
		mixpanelHelper.trackEvent(EVENT_LOGOUT, mixpanelHelper.mMixpanelUnifiedAPI)
		mixpanelHelper.updateProperty(
			LAST_LOGOUT_DATE,
			getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT)
		)
		mixpanelHelper.addUpdateSuperProperty(
			USER_TYPE,
			UserDetailsUtil.getUserType(null)
		)

	}

	private fun trackMixPanelLogoutFailed(reason : String) {
		try {
			val jsonObject = JSONObject().apply {
			put(PARA_REASON, reason)
			}
			mixpanelHelper.trackEvent(EVENT_LOGOUT_FAILED, jsonObject,mixpanelHelper.mMixpanelAPI)

			val jsonObjectUnified = JSONObject().apply {
				put(PARA_REASON, reason)
			}
			mixpanelHelper.trackEvent(EVENT_LOGOUT_FAILED, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackAppsFlyerLoginSignUpSuccess(
		type: String,
		auth: String,
		value: String,
		source: String,
		isNewUser: Boolean
	) {
		try {
		    val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_TYPE] = type
			parameterAndValueMap[PARA_AUTH] = auth
			parameterAndValueMap[PARA_VALUE] = value.maskLast5PhoneNumberDigits()
			parameterAndValueMap[PARA_SOURCE] = source
			if (isNewUser) {
				parameterAndValueMap[COMVIVA_ID] = value // RMN from Login Screen
//				put(COMVIVA_ID, sharedPrefs.getClearRMN())//TSF-8332
				appsFlyerHelper.trackEventWithoutComvivaID(EVENT_SIGN_UP, parameterAndValueMap)
			}
			else
				appsFlyerHelper.trackEvent(EVENT_LOGIN_SUCCESS, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackLoginFailure(
		type: String,
		auth: String,
		reason: String,
		value: String,
		source: String,
		errorCode: String
	) {
		trackMixPanelLoginFailure(type, auth, reason, value, source, errorCode)
		trackFirebaseLoginFailure(type, auth, reason, value, source)
		trackFacebookLoginFailure(type, auth, reason, value, source)
		trackMoEngageLoginFailure(type, auth, reason, value, source)
		trackAppsflyerLoginFailure(type, auth, value, reason)
	}

	private fun trackAppsflyerLoginFailure(
		type: String,
		auth: String,
		value: String,
		reason: String
	) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_TYPE] = type
			parameterAndValueMap[PARA_AUTH] = auth
			parameterAndValueMap[PARA_VALUE] = value.maskLast5PhoneNumberDigits()
			parameterAndValueMap[PARA_REASON] = reason
			appsFlyerHelper.trackEvent(EVENT_LOGIN_FAILED, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackNotATSUserClicked() {
		trackMixPanelNotATSUserClicked()
		trackMoEngageNotATSUserClicked()
	}

	private fun trackMoEngageOTPReceived() {
		moEngageHelper.trackEvent(EVENT_LOGIN_OTP_RECEIVED)
	}

	private fun trackMixPanelOTPReceived() {
		mixpanelHelper.trackEvent(EVENT_LOGIN_OTP_RECEIVED, mixpanelHelper.mMixpanelAPI)
		mixpanelHelper.trackEvent(EVENT_LOGIN_OTP_RECEIVED, mixpanelHelper.mMixpanelUnifiedAPI)
	}

	private fun trackMixPanelNotATSUserClicked(){
		mixpanelHelper.trackEvent(EVENT_NOT_A_TS_USER_CLICK, mixpanelHelper.mMixpanelUnifiedAPI)
	}

	private fun trackMoEngageNotATSUserClicked(){
		moEngageHelper.trackEvent(EVENT_NOT_A_TS_USER_CLICK)
	}

	fun trackNonTataSkyRMN(rmn : String){
		trackMixPanelNonTataSkyRMN(rmn)
		trackMoEngageNonTataSkyRMN(rmn)
	}

	private fun trackMixPanelNonTataSkyRMN(rmn :String){
		try {
			val jsonObject = JSONObject()
			jsonObject.put(RMN, rmn)
			mixpanelHelper.trackEvent(EVENT_NON_TS_RMN, jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e:JSONException){
			e.printStackTrace()
		}
	}

	private fun trackMoEngageNonTataSkyRMN(rmn: String){
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(RMN, rmn)
			moEngageHelper.trackEvent(EVENT_NON_TS_RMN, payloadBuilder)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelLoginFailure(
		type: String,
		auth: String,
		reason: String,
		value: String,
		source: String,
		errorCode: String
	) {
		try {
			val jsonObjectUnified = JSONObject()
			jsonObjectUnified.put(ERROR, reason)
			jsonObjectUnified.put(ERROR_CODE, errorCode)
			mixpanelHelper.trackEvent(EVENT_LOGIN_FAILED, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageLoginFailure(type: String, auth: String, reason: String, value: String, source: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_TYPE, type)
			payloadBuilder.addAttribute(PARA_AUTH, auth)
			payloadBuilder.addAttribute(PARA_VALUE, value)
			payloadBuilder.addAttribute(PARA_REASON, reason)
			payloadBuilder.addAttribute(PARA_SOURCE, source)
			moEngageHelper.trackEvent(EVENT_LOGIN_FAILED, payloadBuilder)

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelLoginSuccess(type: String, value: String, source: String,userState:String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_TYPE, type)
			jsonObject.put(PARA_SOURCE, source)
			jsonObject.put(PARA_VALUE, value)
			mixpanelHelper.trackEvent(EVENT_LOGIN_SUCCESS, jsonObject, mixpanelHelper.mMixpanelAPI)

			val jsonObjectUnified = JSONObject()
			jsonObjectUnified.put(PARA_SOURCE, source)
			jsonObjectUnified.put(PARA_USERSTATE, userState)
			mixpanelHelper.trackEvent(EVENT_LOGIN_SUCCESS, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)

		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageLoginSuccess(type: String, value: String, source: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_TYPE, type)
			payloadBuilder.addAttribute(PARA_SOURCE, source)
			payloadBuilder.addAttribute(PARA_VALUE, value)
			moEngageHelper.trackEvent(EVENT_LOGIN_SUCCESS, payloadBuilder)

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	//LOGIN-ENTER
	private fun trackMixPanelLoginInitiate(source : String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_SOURCE, source)
			mixpanelHelper.trackEvent(EVENT_LOGIN_SCREEN_VISIT, jsonObject)

		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageLoginInitiate(source: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_SOURCE, source)
			moEngageHelper.trackEvent(EVENT_LOGIN_SCREEN_VISIT, payloadBuilder)

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}


	private fun trackMixPanelOtpEnter(device: String) {
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_DEVICE_METHOD, device)
			}
			mixpanelHelper.trackEvent(EVENT_LOGIN_OTP_ENTER, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:Exception){

		}
	}

	private fun trackMoEngageOtpEnter() {
		moEngageHelper.trackEvent(EVENT_LOGIN_OTP_ENTER)
	}

	private fun trackMixPanelPasswordScreen() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_PASSWORD, mixpanelHelper.mMixpanelAPI)

		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngagePasswordScreen() {
		try {
			moEngageHelper.trackEvent(EVENT_LOGIN_PASSWORD)

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelForgetPasswordClick(sid: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_SID, sid)
			mixpanelHelper.trackEvent(EVENT_FORGOT_PASSWORD, jsonObject, mixpanelHelper.mMixpanelAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageForgetPasswordClick(sid: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_SID, sid)
			moEngageHelper.trackEvent(EVENT_FORGOT_PASSWORD, payloadBuilder)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelPasswordResetFailure(reason: String, sid: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_REASON, reason)
			jsonObject.put(PARA_SID, sid)
			mixpanelHelper.trackEvent(EVENT_PASSWORD_RESET_FAILED, jsonObject, mixpanelHelper.mMixpanelAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngagePasswordResetFailure(reason: String, sid: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_REASON, reason)
			payloadBuilder.addAttribute(PARA_SID, sid)
			moEngageHelper.trackEvent(EVENT_PASSWORD_RESET_FAILED, payloadBuilder)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelPasswordResetSuccess(sid: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_SID, sid)
			mixpanelHelper.trackEvent(EVENT_PASSWORD_RESET_SUCCESS, jsonObject, mixpanelHelper.mMixpanelAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngagePasswordResetSuccess(sid: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_SID, sid)
			moEngageHelper.trackEvent(EVENT_PASSWORD_RESET_SUCCESS, payloadBuilder)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelOtpResend() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_OTP_RESEND,mixpanelHelper.mMixpanelAPI)
			mixpanelHelper.trackEvent(EVENT_LOGIN_OTP_RESEND,mixpanelHelper.mMixpanelUnifiedAPI)

		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelLoginGetOtp() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_GET_OTP,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageOtpResend() {
		try {
			moEngageHelper.trackEvent(EVENT_LOGIN_OTP_RESEND)

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelOtpInvoked() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_OTP_INVOKE)

		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageOtpInvoked() {
		try {
			moEngageHelper.trackEvent(EVENT_LOGIN_OTP_INVOKE)

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelMaxDevice(sid: String) {
		try {
			val jsonObject = JSONObject().apply {
				put(PARA_SID, sid)
			}
			mixpanelHelper.trackEvent(EVENT_MAX_DEVICE, jsonObject, mixpanelHelper.mMixpanelAPI)

			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SID, sid)
			}
			mixpanelHelper.trackEvent(EVENT_MAX_DEVICE, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}


	private fun trackMixPanelLoginPageVisit(type: String, source: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
//				put(PARA_TYPE, type)
				put(PARA_SOURCE, source)
			}
			mixpanelHelper.trackEvent(EVENT_LOGIN_PAGE_VISIT, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	fun trackParentalControlClick(){
		trackMixPanelParentalControlClick()
	}

	private fun trackMixPanelParentalControlClick(){
		try {
		    mixpanelHelper.trackEvent(EVENT_PARENTAL_CONTROL_CLICK,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:Exception){

		}
	}

	fun trackSetRegistrationInitiate(restriction:String){
		trackMixPanelSetRestrictionInitiate(restriction)
	}
	private fun trackMixPanelSetRestrictionInitiate(restriction: String){
		try {
		    val jsonObjectUnified = JSONObject().apply {
		    	put(PARA_RESTRICTION, restriction)
			}
			mixpanelHelper.trackEvent(EVENT_SET_RESTRICTION_INITIATE, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	fun trackSetRestrictionProceed(restriction: String){
		trackMixPanelSetRestrictionProceed(restriction)
	}

	private fun trackMixPanelSetRestrictionProceed(restriction: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_RESTRICTION, restriction)
			}
			mixpanelHelper.trackEvent(EVENT_SET_RESTRICTION_PROCEED, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	fun trackParentalPinOtpInitiate(source:String){
		trackMixPanelParentalPinOtpInitiate(source)
	}

	private fun trackMixPanelParentalPinOtpInitiate(source: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
			}
			mixpanelHelper.trackEvent(EVENT_PARENTAL_PIN_OTP_INITIATE, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelParentalPinOtpProceed(source: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
			}
			mixpanelHelper.trackEvent(EVENT_PARENTAL_PIN_OTP_PROCEED, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelCreatePinInitiate(source: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
			}
			mixpanelHelper.trackEvent(EVENT_CREATE_PIN_INITIATE, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelCreatePinProceed(source: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
			}
			mixpanelHelper.trackEvent(EVENT_CREATE_PIN_PROCEED, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelCreatePinCancel(source: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
			}
			mixpanelHelper.trackEvent(EVENT_CREATE_PIN_CANCEL, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelPinEntryInitiate(source: String, device: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
				put(PARA_DEVICE, device)
			}
			mixpanelHelper.trackEvent(EVENT_PIN_ENTRY_INITIATE, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelPinEntryProceed(source: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
			}
			mixpanelHelper.trackEvent(EVENT_PIN_ENTRY_PROCEED, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelPinEntryForgot(source: String, device: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
				put(PARA_DEVICE, device)
			}
			mixpanelHelper.trackEvent(EVENT_PIN_ENTRY_FORGOT, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelPinEntryNotNow(source: String, device: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
				put(PARA_DEVICE, device)
			}
			mixpanelHelper.trackEvent(EVENT_PIN_ENTRY_NOT_NOW, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelPinEntryIncorrect(source: String, device: String){
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_SOURCE, source)
				put(PARA_DEVICE, device)
			}
			mixpanelHelper.trackEvent(EVENT_PIN_ENTRY_INCORRECT, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		}catch (e:java.lang.Exception){

		}
	}

	private fun trackMixPanelLoginPageNewLogin() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_PAGE_NEWLOGIN, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {

		}
	}


	private fun trackMixPanelLoginPageNotNow() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_PAGE_NOTNOW, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {

		}
	}


	private fun trackMixPanelLoginRmnEnterInvalid(context: Context) {
		try {
			val jsonObjectUnified = JSONObject()
			jsonObjectUnified.put(DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
			mixpanelHelper.trackEvent(EVENT_LOGIN_RMN_ENTER_INVALID, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {

		}
	}


	private fun trackMixPanelLoginRmnEnter() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_RMN_ENTER, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {

		}
	}

	private fun trackMixPanelLoginLicAgreement() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_LIC_AGREEMENT, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {

		}
	}

	//todo: need to track
	private fun trackMixPanelLoginLicAgreementBack() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_LIC_AGREEMENT_BACK, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {

		}
	}

	fun trackLoginSkip(){
		trackMixPanelLoginSkip()
	}

	private fun trackMixPanelLoginSkip() {
		try {
			mixpanelHelper.trackEvent(EVENT_LOGIN_SKIP, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {

		}
	}


	fun trackMixPanelLoginSubscriptionIdSelect(subscriptionId: String) {
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(SUBSCRIPTION_ID, subscriptionId)
			}
			mixpanelHelper.trackEvent(
				EVENT_LOGIN_SUBSCRIPTIONID_SELECT,
				jsonObjectUnified,
				mixpanelHelper.mMixpanelUnifiedAPI
			)
		} catch (e: Exception) {

		}
	}

	private fun trackMixPanelLoginMyPlanStartWatching() {
		try {
			mixpanelHelper.trackEvent(
				EVENT_LOGIN_MY_PLAN_STARTWATCHING,
				mixpanelHelper.mMixpanelUnifiedAPI
			)
		} catch (e: Exception) {

		}
	}

	public fun trackMixPanelSetDefaultProfile(baId: String) {
		try {
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_TO_BAID, baId)
			}
			mixpanelHelper.trackEvent(
				EVENT_SET_DEFAULT_PROFILE,
				jsonObjectUnified,
				mixpanelHelper.mMixpanelUnifiedAPI
			)
		} catch (e: Exception) {
		}
	}

	private fun trackMoEngageMaxDevice(sid: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_SID, sid)
			moEngageHelper.trackEvent(EVENT_MAX_DEVICE, payloadBuilder)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseLoginInitiate(source: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_SOURCE, source)
			firebaseAnalytics.trackEvent(EVENT_LOGIN_SCREEN_VISIT, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseMaxDevice(sid: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_SID, sid)
			firebaseAnalytics.trackEvent(EVENT_MAX_DEVICE, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseLoginFailure(type: String, auth: String, reason: String, value: String, source: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_TYPE, type)
			bundle.putString(PARA_SOURCE, source)
			bundle.putString(PARA_AUTH, auth)
			bundle.putString(PARA_REASON, reason)
			bundle.putString(PARA_VALUE, value)
			firebaseAnalytics.trackEvent(EVENT_LOGIN_FAILED, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseLoginSignUpSuccess(
		type: String,
		auth: String,
		value: String,
		source: String,
		isNewUser: Boolean
	) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_TYPE, type)
			bundle.putString(PARA_AUTH, auth)
			bundle.putString(PARA_SOURCE, source)
			bundle.putString(PARA_VALUE, value)
			if (isNewUser)
				firebaseAnalytics.trackEvent(EVENT_SIGN_UP, bundle)
			else
				firebaseAnalytics.trackEvent(EVENT_LOGIN_SUCCESS, bundle)
			firebaseAnalytics.trackEventWithoutFormatting(EVENT_COMPLETE_REGISTRATION)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseOtpEnter() {
		try {
			firebaseAnalytics.trackEvent(EVENT_LOGIN_OTP_ENTER)
		} catch (e: Exception) {
		}
	}

	private fun trackFirebasePasswordScreen() {
		try {
			firebaseAnalytics.trackEvent(EVENT_LOGIN_PASSWORD)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseForgetPasswordClick(sid: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_SID, sid)
			firebaseAnalytics.trackEvent(EVENT_FORGOT_PASSWORD, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebasePasswordResetFailure(reason: String, sid: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_REASON, reason)
			bundle.putString(PARA_SID, sid)
			firebaseAnalytics.trackEvent(EVENT_PASSWORD_RESET_FAILED, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebasePasswordResetSuccess(sid: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_SID, sid)
			firebaseAnalytics.trackEvent(EVENT_PASSWORD_RESET_SUCCESS, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseOtpResend() {
		try {
			firebaseAnalytics.trackEvent(EVENT_LOGIN_OTP_RESEND)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseOtpInvoked(type: String, auth: String, value: String, source: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_TYPE, type)
			bundle.putString(PARA_AUTH, auth)
			bundle.putString(PARA_VALUE, value.maskLast5PhoneNumberDigits())
			bundle.putString(PARA_SOURCE, source)
			firebaseAnalytics.trackEvent(EVENT_LOGIN_OTP_INVOKE, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookLoginInitiate(source: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_SOURCE, source)
			facebookAnalyticsHelper.trackEvent(EVENT_LOGIN_SCREEN_VISIT, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookMaxDevice(sid: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_SID, sid)
			facebookAnalyticsHelper.trackEvent(EVENT_MAX_DEVICE, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookLoginFailure(type: String, auth: String, reason: String, value: String, source: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_TYPE, type)
			bundle.putString(PARA_SOURCE, source)
			bundle.putString(PARA_AUTH, auth)
			bundle.putString(PARA_REASON, reason)
			bundle.putString(PARA_VALUE, value)
			facebookAnalyticsHelper.trackEvent(EVENT_LOGIN_FAILED, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookLoginSignUpSuccess(
		type: String,
		auth: String,
		value: String,
		source: String,
		isNewUser: Boolean
	) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_TYPE, type)
			bundle.putString(PARA_AUTH, auth)
			bundle.putString(PARA_SOURCE, source)
			bundle.putString(PARA_VALUE, value)
			if (isNewUser)
				facebookAnalyticsHelper.trackEvent(EVENT_SIGN_UP, bundle)
			else
				facebookAnalyticsHelper.trackEvent(EVENT_LOGIN_SUCCESS, bundle)
			facebookAnalyticsHelper.trackEventWithoutFormatting(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookOtpEnter() {
		try {
			facebookAnalyticsHelper.trackEvent(EVENT_LOGIN_OTP_ENTER)
		} catch (e: Exception) {
		}
	}

	private fun trackFacebookPasswordScreen() {
		try {
			facebookAnalyticsHelper.trackEvent(EVENT_LOGIN_PASSWORD)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookForgetPasswordClick(sid: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_SID, sid)
			facebookAnalyticsHelper.trackEvent(EVENT_FORGOT_PASSWORD, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookPasswordResetFailure(reason: String, sid: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_REASON, reason)
			bundle.putString(PARA_SID, sid)
			facebookAnalyticsHelper.trackEvent(EVENT_PASSWORD_RESET_FAILED, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookPasswordResetSuccess(sid: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_SID, sid)
			facebookAnalyticsHelper.trackEvent(EVENT_PASSWORD_RESET_SUCCESS, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookOtpResend() {
		try {
			facebookAnalyticsHelper.trackEvent(EVENT_LOGIN_OTP_RESEND)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookOtpInvoked(type: String, auth: String, value: String, source: String) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_TYPE, type)
			bundle.putString(PARA_AUTH, auth)
			bundle.putString(PARA_VALUE, value.maskLast5PhoneNumberDigits())
			bundle.putString(PARA_SOURCE, source)
			facebookAnalyticsHelper.trackEvent(EVENT_LOGIN_OTP_INVOKE, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun getMixPanelId() = mixpanelHelper.getMixPanelDistinctId(mixpanelHelper.mMixpanelAPI)
	fun getMixPanelUnifiedId() = mixpanelHelper.getMixPanelDistinctId(mixpanelHelper.mMixpanelUnifiedAPI)
}