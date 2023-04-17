package com.tatasky.binge.ui.features.subscription

import android.os.Bundle
import com.appsflyer.AFInAppEventParameterName
import com.facebook.appevents.AppEventsConstants
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.SUBSCRIBED
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.facebook.FacebookAnalyticsHelper
import com.tatasky.binge.analytics.firebase.FirebaseAnalyticsHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.ui.features.splash.SplashAnalytics
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.Properties
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SubscriptionAnalytics(
	private val mixpanelHelper: MixpanelHelper,
	private val moEngageHelper: MoEngageHelper,
	private val facebookAnalyticsHelper: FacebookAnalyticsHelper,
	private val firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
	private val appsFlyerHelper: AppsFlyerHelper
) : SplashAnalytics(mixpanelHelper, moEngageHelper,appsFlyerHelper, firebaseAnalyticsHelper,facebookAnalyticsHelper) {

	private fun trackMixpanelSubscriptionInitiate(
		sourceScreen: String,
		packName: String,
		packPrice: String,
		modType: String?,
		packType: String,
		previousPackName: String?,
		previousPackType: String?,
		previousPackPrice: String?,
		previousPackTenure: String?,
		currentPackTenure: String?,
		isFirstSubscription: Boolean?
	) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_PACK_NAME, packName)
			jsonObject.put(PARA_PACK_PRICE, packPrice)
			jsonObject.put(PARA_SOURCE, sourceScreen)
			jsonObject.put(PARA_MOD_TYPE, modType?.uppercase()) //TODO: Refer:https://docs.google.com/document/d/1ghAIM_mTtSMQx8Ikf_3IiWMY-Bchk0KgXybXzfjxfU8/edit?disco=AAAAh-Kfc_4
			jsonObject.put(PACK_TYPE, packType)
			jsonObject.put(PACK_TENURE, currentPackTenure)
			jsonObject.put(EXISTING_PACK_NAME, previousPackName)
			jsonObject.put(EXISTING_PACK_TYPE, previousPackType)
			jsonObject.put(EXISTING_PACK_PRICE, previousPackPrice)
			jsonObject.put(EXISTING_PACK_TENURE, previousPackTenure)
			jsonObject.put(FIRST_SUBSCRIPTION, isFirstSubscription?.let { if (it) YES else NO })
			mixpanelHelper.trackEvent(
				SUBSCRIPTION_INITIATE,
				jsonObject,
				mixpanelHelper.mMixpanelUnifiedAPI
			)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackPaymentFlowExit(
		exitStatus: String,
		paymentMethod: String/*TP-WALLET, PAYMENT-GATEWAY, NOT-SELECTED*/,
		paymentStatus: String,
		pgResponseCode: String,
	) {
		trackMixPanelPaymentFlowExit(exitStatus, paymentMethod, paymentStatus, pgResponseCode)
	}

	private fun trackMixPanelPaymentFlowExit(
		exitStatus: String,
		paymentMethod: String/*TP-WALLET, PAYMENT-GATEWAY, NOT-SELECTED*/,
		paymentStatus: String,
		pgResponseCode: String,
	) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_EXIT_STATUS, exitStatus)
			jsonObject.put(PARA_PAYMENT_METHOD, paymentMethod)
			jsonObject.put(PARA_PAYMENT_STATUS, paymentStatus)
			jsonObject.put(PARA_PG_RESPONSE_CODE, pgResponseCode)
			mixpanelHelper.trackEvent(EVENT_PAYMENT_FLOW_EXIT, jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
		}
		catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackPayment(
		paymentMethod: String/*Wallet/SI/Online Payment*/,
		userDTHBalance: String,
		transactionStatus: String,
		cardType: String /*Credit/Debit*/,
		bankName: String,
		walletName: String/*Paytm, PhonePe*/,
		transactionID: String,
		cardVariant: String/*Regalia/Infinia*/,
		failureReason: String,
		amount: String,
		packName: String? = null,
		packDurationInDays: String? = null,
		packId: String? = null,
		promoCode: String? = null,
		actualAmountPaid: String? = null,
		paymentMode: String? = null,
		selectedTenurePackPrice: String? = null,
		selectedTenureInDaysWithDSuffix: String? = null,
		pgPaymentStatus:String,
		pgResponseCode:String,
		shouldTriggerInMixpanel: Boolean = true
	) {
		if (shouldTriggerInMixpanel)
			trackMixPanelPayment(
				paymentMethod,
				userDTHBalance,
				transactionStatus,
				cardType,
				bankName,
				walletName,
				transactionID,
				cardVariant,
				failureReason,
				amount,
				pgPaymentStatus,
				pgResponseCode
			)
		if (transactionStatus.contains("Completed", true))
			trackAppsflyerPayment(
				packName,
				selectedTenureInDaysWithDSuffix,
				selectedTenurePackPrice,
				packId,
				promoCode,
				actualAmountPaid,
				paymentMode
			)
	}

	private fun trackAppsflyerPayment(
		packName: String?,
		packDurationInDays: String?,
		packPrice: String?,
		packId: String?,
		promoCode: String?,
		actualAmountPaid: String?,
		paymentMode: String?
	) {
		try {
			val parameterAndValueMap =
				HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_PACK_NAME] = packName ?: ""
			parameterAndValueMap[PACK_DURATION] = packDurationInDays ?: ""
			parameterAndValueMap[PACK_PRICE] = packPrice ?: ""
			parameterAndValueMap[PACK_ID] = packId ?: ""
			parameterAndValueMap[PROMO_CODE] = promoCode ?: ""
			parameterAndValueMap[AFInAppEventParameterName.REVENUE] = actualAmountPaid ?: ""
			parameterAndValueMap[PAYMENT_MODE] = paymentMode ?: ""
			appsFlyerHelper.trackEvent(
				EVENT_PAYMENT,
				parameterAndValueMap
			) // First Paid pack subscription success
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackPlanSelectionViews(packName: String){
		trackMixpanelPackSelectionViews(packName)
	}

	fun trackComparePlanSelection(packName:String){
		trackMixpanelComparePlanSelection(packName)
	}

	fun trackComparePlanViews(){
		trackMixpanelComparePlanViews()
	}

	fun trackPackTenureView(){
		trackMixpanelPackTenureView()
	}

	fun trackMyPlanDontCancelPlan(packName: String, packType: String){
		trackMixpanelMyPlanDontCancelPlan(packName,packType)
	}


	fun trackMyPlanCancelPrimeConfirm(packName:String,packType: String){
		trackMixpanelMyPlanCancelPrimeConfirm(packName,packType)
	}

	fun trackMyPlanCancelPrime(packName: String){
		trackMixpanelMyPlanCancelPrime(packName)
	}

	fun trackMyPlanCancelPlanLater(packName: String,daysRemaining: String){
		trackMixPanelMyPlanCancelPlanLater(packName, daysRemaining)
	}


	fun trackMyPlanCancelPlanProceed(packName: String,daysRemaining: String){
		trackMixPanelMyPlanCancelPlanProceed(packName, daysRemaining)
	}


	fun trackMyPlanCancelPlan(packName: String,daysRemaining: String){
		trackMixPanelMyPlanCancelPlan(packName, daysRemaining)
	}

	fun trackMyPlanRenewChangeTenure(packName: String, daysRemaining: String, tenureType: String) {
		trackMixPanelRenewChangeTenure(packName,daysRemaining, tenureType)
	}

	fun trackMyPlanRenewChangePlan(packName:String, daysRemaining: String, tenureType: String){
		trackMixPanelRenewChangePlan(packName,daysRemaining, tenureType)
	}


	fun trackMyPlanRenewOtherOptions(
		packName: String,
		daysRemaining: String,
		tenureType: String
	) {
		trackMixPanelRenewOtherOptions(packName,daysRemaining, tenureType)
	}

	fun trackMyPlanRenewPlan(
		packName: String,
		packActive: String,
		daysRemaining: String,
		tenureType: String
	) {
		trackMixPanelMyPlanRenewPlan(packName, packActive, daysRemaining, tenureType)
	}

	fun trackMyPlanChangePlan(packName: String, tenureType: String){
		trackMixPanelMyPlanChangePlan(packName, tenureType)
	}

	fun trackMyPlanChangetenure(packName: String, tenureType: String){
		trackMixPanelMyPlanChangetenure(packName, tenureType)
	}

	fun trackViewMyPlanScreen(primePackActive: String,packActive: String, packName: String, tenureType: String) {
		trackAppsFlyerViewMyPlan()
		trackMixPanelPlanView(primePackActive,packActive, packName, tenureType)
	}

	fun trackViewPaymentGatewayScreen() {
		trackAppsFlyerViewPGScreen()
	}

	fun trackViewPackListingScreen() {
		trackAppsFlyerViewPackListing()
	}

	fun trackViewRechargeScreen() {
		trackAppsFlyerViewRecharge()
	}

	private fun trackAppsFlyerViewPGScreen() {
		appsFlyerHelper.trackEvent(EVENT_VIEW_PAYMENT)
	}

	private fun trackAppsFlyerViewMyPlan() {
		appsFlyerHelper.trackEvent(EVENT_MY_PLAN)
	}

	private fun trackAppsFlyerViewRecharge() {
		appsFlyerHelper.trackEvent(EVENT_RECHARGE)
	}

	fun trackViewMySubscription() {
		trackAppsFlyerViewMySubscription()
	}

	private fun trackAppsFlyerViewMySubscription() {
		appsFlyerHelper.trackEvent(EVENT_VIEW_MY_SUBSCRIPTION)
	}

	fun trackViewFreeTrialScreen() {
		trackAppsFlyerFreeTrialScreenView()
	}

	private fun trackAppsFlyerFreeTrialScreenView() {
		appsFlyerHelper.trackEvent(EVENT_VIEW_FREE_TRIAL)
	}

	fun trackAddPackSkip() {
		trackMixPanelAddPackSkip()
		trackMoEngageAddPackSkip()
	}

	fun trackAddPackContinue(packName: String, packType: String, packPrice: String) {
		trackMixPanelAddPackContinue(packName, packType, packPrice)
		trackMoEngageAddPackContinue(packName, packType, packPrice)
	}

	fun trackPackSelection(
		packName: String,
		packType: String,
		packPrice: String,
		packPaymentMethod: String,
		packDuration: String,
		packChangePlan: String,
		packChangeTenure: String,
		packPayableAmount: String,
		packDurationInDays: String/*Eg. 30D, 180D, 365D*/,
		packId: String,
		isUserLoggedIn: Boolean,
		trackOnlyInAppsFlyer: Boolean,
		selectedTenurePackPrice: String,
		tenureType: String,
		source: String,
		packTypeFreeOrPaid: String,
		modType: String?,
		previousPackName: String?,
		previousPackType: String?,
		previousPackPrice: String?,
		previousPackTenure: String?,
		isFirstSubscription: Boolean?
	) {
		if (trackOnlyInAppsFlyer)
			trackAppsFlyerPackSelection(
				packName,
				packType,
				packDurationInDays,
				selectedTenurePackPrice,
				packId,
				isUserLoggedIn
			)
		else {
			trackMixPanelPackSelection(
				packName,
				packType,
				packPrice,
				packPaymentMethod,
				packDuration,
				packChangePlan,
				packChangeTenure,
				packPayableAmount,
				tenureType
			)
			trackMoEngagePackSelection(packName, packType, packPrice)
			trackAppsFlyerPackSelection(
				packName,
				packType,
				packDurationInDays,
				selectedTenurePackPrice,
				packId,
				isUserLoggedIn
			)
			trackFirebasePackSelection(
				packName,
				packType,
				packDurationInDays,
				packPrice,
				packId,
				isUserLoggedIn
			)
			trackFacebookPackSelection(
				packName,
				packType,
				packDurationInDays,
				packPrice,
				packId,
				isUserLoggedIn
			)
		}
		trackMixpanelSubscriptionInitiate(
			source,
			packName,
			packPrice,
			modType,
			packTypeFreeOrPaid,
			previousPackName,
			previousPackType,
			previousPackPrice,
			previousPackTenure,
			tenureType,
			isFirstSubscription
		)
	}

	private fun trackFacebookPackSelection(
		packName: String,
		packType: String,
		packDurationInDays: String,
		packPrice: String,
		packId: String,
		isUserLoggedIn: Boolean
	) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_PACK_NAME, packName)
//			bundle.putString(PARA_TYPE, packType)
			bundle.putString(PACK_DURATION, packDurationInDays)
			bundle.putString(PACK_PRICE, packPrice)
			bundle.putString(PACK_ID, packId)
			bundle.putString(
				USER_LOGIN_STATE,
				if (isUserLoggedIn) LOGIN_STATE_LOGGED_IN else LOGIN_STATE_NON_LOGGED_IN
			)
			facebookAnalyticsHelper.trackEvent(EVENT_PACK_SELECTED, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebasePackSelection(
		packName: String,
		packType: String,
		packDurationInDays: String,
		packPrice: String,
		packId: String,
		isUserLoggedIn: Boolean
	) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_PACK_NAME, packName)
//			bundle.putString(PARA_TYPE, packType)
			bundle.putString(PACK_DURATION, packDurationInDays)
			bundle.putString(PACK_PRICE, packPrice)
			bundle.putString(PACK_ID, packId)
			bundle.putString(
				USER_LOGIN_STATE,
				if (isUserLoggedIn) LOGIN_STATE_LOGGED_IN else LOGIN_STATE_NON_LOGGED_IN
			)
			firebaseAnalyticsHelper.trackEvent(EVENT_PACK_SELECTED, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackSubscribeSuccess(
		packType: String,
		sourceScreen: String,
		packName: String,
		packPrice: String,
		isFromNudge: Boolean,
		fdoRegistered: Boolean,
		fdoRegisteredDay: String,
		modType: String,
		paymentMethod: String,
		paymentType: String,
		packId: String? = null,
		promoCode: String?= null,
		paymentMode: String? = null /*Actual payment mode*/,
		isSubscribedForPaidPackFirstTime: Boolean = false,
		actualAmountPaid: String? = null  /*Actual payment made*/,
		sourceScreenForAppsFlyer: String? = null,
		selectedTenurePackPrice: String? = null /*Base pack price X Tenure*/,
		selectedTenureInDaysWithDSuffix: String? = null,
		previousPackName: String? = null,
		previousPackType: String? = null,
		previousPackPrice: String? = null,
		previousPackTenure: String? = null,
		currentPackTenure: String? = null,
		isFirstSubscription: Boolean = false,
		shouldTriggerInMixpanel: Boolean = true,
		productType: String? /*MyOP/Curated*/
	) {
		if (shouldTriggerInMixpanel)
			trackMixPanelSubscribeSuccess(
				packType,
				sourceScreen,
				packName,
				packPrice,
				isFromNudge,
				fdoRegistered,
				fdoRegisteredDay,
				modType,
				paymentMethod,
				paymentType,
				previousPackName,
				previousPackType,
				previousPackPrice,
				previousPackTenure,
				currentPackTenure,
				isFirstSubscription
			)
		trackMoEngageSubscribeSuccess(packType,sourceScreen,packName,packPrice, isFromNudge, fdoRegistered, fdoRegisteredDay, modType)
		trackAppsFlyerSubscribeSuccess(
			packType,
			sourceScreenForAppsFlyer,
			packName,
			selectedTenurePackPrice,
			packId,
			selectedTenureInDaysWithDSuffix,
			promoCode,
			paymentMode,
			isSubscribedForPaidPackFirstTime,
			actualAmountPaid,
			fdoRegistered
		)
		if (packType.equals(PACK_TYPE_PAID, true)) {
			trackFirebaseSubscribeSuccess(
				packType,
				sourceScreenForAppsFlyer,
				packName,
				selectedTenurePackPrice,
				packId,
				selectedTenureInDaysWithDSuffix,
				promoCode,
				paymentMode,
				isSubscribedForPaidPackFirstTime,
				actualAmountPaid,
				fdoRegistered,
				productType
			)
			trackFacebookSubscribeSuccess(
				packType,
				sourceScreenForAppsFlyer,
				packName,
				selectedTenurePackPrice,
				packId,
				selectedTenureInDaysWithDSuffix,
				promoCode,
				paymentMode,
				isSubscribedForPaidPackFirstTime,
				actualAmountPaid,
				fdoRegistered
			)
		}
    }

	private fun trackFacebookSubscribeSuccess(
		packType: String,
		sourceScreenForAppsFlyer: String?,
		packName: String,
		selectedTenurePackPrice: String?,
		packId: String?,
		selectedTenureInDaysWithDSuffix: String?,
		promoCode: String?,
		paymentMode: String?,
		isSubscribedForPaidPackFirstTime: Boolean,
		actualAmountPaid: String?,
		fdoRegistered: Boolean
	) {
		try {
			val bundle = Bundle()
			bundle.putString(
				PARA_SOURCE,
				if (SOURCE_PLAY_CLICK.equals(sourceScreenForAppsFlyer, true))
					SOURCE_CONTENT
				else
					sourceScreenForAppsFlyer
			)
			bundle.putString(PARA_PACK_NAME, packName)
			bundle.putString(PACK_PRICE, selectedTenurePackPrice)
			bundle.putString(PACK_ID, packId)
			bundle.putString(PACK_DURATION, selectedTenureInDaysWithDSuffix)
			bundle.putString(PROMO_CODE, promoCode)
			bundle.putString(PARA_PACK_TYPE, packType)
			bundle.putString(PAYMENT_MODE, paymentMode)
			facebookAnalyticsHelper.trackEvent(EVENT_SUBSCRIBE, bundle) // Paid pack subscription success
			if (isSubscribedForPaidPackFirstTime && !fdoRegistered)
				facebookAnalyticsHelper.trackEvent(EVENT_SUBSCRIBE_SUCCESS_NEW, bundle) //First Paid pack subscription success
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseSubscribeSuccess(
		packType: String,
		sourceScreenForAppsFlyer: String?,
		packName: String,
		selectedTenurePackPrice: String?,
		packId: String?,
		selectedTenureInDaysWithDSuffix: String?,
		promoCode: String?,
		paymentMode: String?,
		isSubscribedForPaidPackFirstTime: Boolean,
		actualAmountPaid: String?,
		fdoRegistered: Boolean,
		productType: String?
	) {
		try {
			val bundle = Bundle()
			bundle.putString(
				PARA_SOURCE,
				if (SOURCE_PLAY_CLICK.equals(sourceScreenForAppsFlyer, true))
					SOURCE_CONTENT
				else
					sourceScreenForAppsFlyer
			)
			bundle.putString(PARA_PACK_NAME, packName)
			bundle.putString(PACK_PRICE, selectedTenurePackPrice)
			bundle.putString(PACK_ID, packId)
			bundle.putString(PACK_DURATION, selectedTenureInDaysWithDSuffix)
			bundle.putString(PROMO_CODE, promoCode)
			bundle.putString(PARA_PACK_TYPE, productType)
			bundle.putString(PAYMENT_MODE, paymentMode)
			bundle.putString(REVENUE, actualAmountPaid)
			firebaseAnalyticsHelper.trackEvent(EVENT_SUBSCRIBE, bundle) // Paid pack subscription success
			if (isSubscribedForPaidPackFirstTime && !fdoRegistered)
				firebaseAnalyticsHelper.trackEvent(EVENT_SUBSCRIBE_SUCCESS_NEW, bundle) //First Paid pack subscription success
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelPayment(
		paymentMethod: String/*Wallet/SI/Online Payment*/,
		userDTHBalance: String,
		transactionStatus: String,
		cardType: String /*Credit/Debit*/,
		bankName: String,
		walletName: String/*Paytm, PhonePe*/,
		transactionID: String,
		cardVariant: String/*Regalia/Infinia*/,
		failureReason: String,
		amount: String,
		paymentStatus: String,
		pgResponseCode: String,
	) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARAM_RENEWAL_MODE, paymentMethod)
			jsonObject.put(PARAM_SUBSCRIBER_BALANCE, userDTHBalance)
			jsonObject.put(PARAM_TRANSACTION_STATUS, transactionStatus)
			jsonObject.put(PARAM_CARD_TYPE, cardType)
			jsonObject.put(PARAM_BANK_NAME, bankName)
			jsonObject.put(PARAM_WALLET_NAME,  walletName)
			jsonObject.put(PARAM_TRANSACTION_ID,  transactionID)
			jsonObject.put(PARAM_CARD_NAME,  cardVariant)
			jsonObject.put(PARAM_FAILURE_REASON, failureReason)
			jsonObject.put(PARA_AMOUNT, amount)
			jsonObject.put(PARA_PAYMENT_STATUS, paymentStatus)
			jsonObject.put(PARA_PG_RESPONSE_CODE, pgResponseCode)
			mixpanelHelper.trackEvent(EVENT_PAYMENT, jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
		}
		catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackStartTrialEvent(){
		facebookAnalyticsHelper.trackEventWithoutFormatting(AppEventsConstants.EVENT_NAME_START_TRIAL)
		firebaseAnalyticsHelper.trackEventWithoutFormatting(EVENT_START_TRIAL)
	}

	fun trackPurchaseEvent(){
		facebookAnalyticsHelper.trackEventWithoutFormatting(AppEventsConstants.EVENT_NAME_PURCHASED)
		firebaseAnalyticsHelper.trackEventWithoutFormatting(EVENT_PURCHASE)
	}

	fun trackInitiatePackSelection(
		sourceScreen: String,
		isFromNudge: Boolean,
		isUserLoggedIn: Boolean = false,
		listingType: String = LISTING_TYPE_LANDING_PAGE
	) {
//        trackMixPanelInitiatePackSelection(sourceScreen, isFromNudge)
		trackMoEngageInitiatePackSelection(sourceScreen, isFromNudge)
		trackAppsFlyerInitiatePackSelection(sourceScreen, isUserLoggedIn, listingType)
		trackFirebaseInitiatePackSelection(sourceScreen, isUserLoggedIn, listingType)
		trackFacebookInitiatePackSelection(sourceScreen, isUserLoggedIn, listingType)
    }

	private fun trackFirebaseInitiatePackSelection(
		sourceScreen: String,
		isUserLoggedIn: Boolean = false,
		listingType: String = LISTING_TYPE_LANDING_PAGE
	) {
		try {
			val bundle = Bundle()
			bundle.putString(
				PARA_SOURCE,
				if (SOURCE_PLAY_CLICK.equals(sourceScreen, true))
					SOURCE_CONTENT
				else
					sourceScreen
			)
			bundle.putString(
				USER_LOGIN_STATE,
				if (isUserLoggedIn) LOGIN_STATE_LOGGED_IN else LOGIN_STATE_NON_LOGGED_IN
			)
			bundle.putString(LISTING_TYPE, listingType)
			firebaseAnalyticsHelper.trackEvent(EVENT_PACK_SELECTION_INITIATE, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFacebookInitiatePackSelection(
		sourceScreen: String,
		isUserLoggedIn: Boolean = false,
		listingType: String = LISTING_TYPE_LANDING_PAGE
	) {
		try {
			val bundle = Bundle()
			bundle.putString(
				PARA_SOURCE,
				if (SOURCE_PLAY_CLICK.equals(sourceScreen, true))
					SOURCE_CONTENT
				else
					sourceScreen
			)
			bundle.putString(
				USER_LOGIN_STATE,
				if (isUserLoggedIn) LOGIN_STATE_LOGGED_IN else LOGIN_STATE_NON_LOGGED_IN
			)
			bundle.putString(LISTING_TYPE, listingType)
			facebookAnalyticsHelper.trackEvent(EVENT_PACK_SELECTION_INITIATE, bundle)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackAppsFlyerViewPackListing() {
		appsFlyerHelper.trackEvent(EVENT_VIEW_PACK_LISTING)
	}

	fun trackSubscribeFailure(
		failureMsz: String,
		packName: String,
		packType: String,
		paymentMethod: String = "",
		paymentType: String = "",
		selectedPackTenure: String? = null,
		source: String? = null,
		previousPackName: String? = null,
		previousPackType: String? = null,
		packPrice: String? = null,
		previousPackPrice: String? = null,
		previousPackTenure: String? = null,
		isFirstSubscription: Boolean,
		modType: String?,
		selectedTenurePackPrice: String /*Base pack price X Tenure*/,
		selectedTenureInDaysWithDSuffix: String,
		paymentMode: String?,
		promoCode: String?,
		shouldTriggerInMixpanel: Boolean = true,
		actualAmountPaid: String?,
		productType: String?
	) {
		if (shouldTriggerInMixpanel) {
			trackMixPanelSubscribeFailure(
				failureMsz,
				packName,
				packType,
				paymentMethod,
				paymentType,
				selectedPackTenure,
				source,
				previousPackName,
				previousPackType,
				packPrice,
				previousPackPrice,
				previousPackTenure,
				isFirstSubscription
			)
			trackFirebaseSubscribeFailure(
				packName,
				packType,
				failureMsz,
				modType,
				selectedTenurePackPrice,
				selectedTenureInDaysWithDSuffix,
				paymentMode ?: "",
				promoCode ?: "",
				isFirstSubscription,
				actualAmountPaid,
				productType
			)
		}
		trackMoEngageSubscribeFailure(failureMsz)
		trackAppsFlyerSubscribeFailure(failureMsz, packName, packType)
		trackFacebookSubscribeFailure(
			packName,
			packType,
			failureMsz,
			modType,
			selectedTenurePackPrice,
			selectedTenureInDaysWithDSuffix,
			paymentMode ?: "",
			promoCode ?: "",
			packType,
			isFirstSubscription
		)
	}

	private fun trackFacebookSubscribeFailure(
		packName: String,
		packType: String,
		failureMsz: String,
		modType: String?,
		selectedTenurePackPrice: String /*Base pack price X Tenure*/,
		selectedTenureInDaysWithDSuffix: String,
		paymentMode: String,
		promoCode: String,
		selectedPackCategory: String,
		isFirstSubscription: Boolean
	) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_PACK_NAME, packName)
			bundle.putString(PARA_TYPE, packType)
			bundle.putString(PARA_REASON, failureMsz)
			bundle.putString(PACK_MOD_TYPE, if (!isFirstSubscription) modType else null)
			bundle.putString(PACK_PRICE, selectedTenurePackPrice)
			bundle.putString(PACK_DURATION, selectedTenureInDaysWithDSuffix)
			bundle.putString(
				SUBSCRIBE_TYPE,
				if (isFirstSubscription)
					NEW
				else if (modType == PaymentUtility.SubscriptionModificationType.RENEWAL.modificationType)
					REPEAT
				else
					MODIFY_PACK
			)
			bundle.putString(PAYMENT_MODE, paymentMode)
			bundle.putString(PROMO_CODE, promoCode)
			bundle.putString(PARA_PACK_TYPE, selectedPackCategory)
			facebookAnalyticsHelper.trackEvent(
				EVENT_SUBSCRIBE_FAILED,
				bundle
			)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseSubscribeFailure(
		packName: String,
		packType: String,
		failureMsz: String,
		modType: String?,
		selectedTenurePackPrice: String /*Base pack price X Tenure*/,
		selectedTenureInDaysWithDSuffix: String,
		paymentMode: String,
		promoCode: String,
		isFirstSubscription: Boolean,
		actualAmountPaid: String?,
		productType: String?
	) {
		try {
			val bundle = Bundle()
			bundle.putString(PARA_PACK_NAME, packName)
			bundle.putString(PARA_TYPE, packType)
			bundle.putString(PARA_REASON, failureMsz)
			bundle.putString(PACK_MOD_TYPE, modType)
			bundle.putString(PACK_PRICE, selectedTenurePackPrice)
			bundle.putString(PACK_DURATION, selectedTenureInDaysWithDSuffix)
			bundle.putString(
				SUBSCRIBE_TYPE,
				if (isFirstSubscription)
					NEW
				else if (modType == PaymentUtility.SubscriptionModificationType.RENEWAL.modificationType)
					REPEAT
				else
					MODIFY_PACK
			)
			bundle.putString(PAYMENT_MODE, paymentMode)
			bundle.putString(PROMO_CODE, promoCode)
			bundle.putString(PARA_PACK_TYPE, productType)
			bundle.putString(REVENUE, actualAmountPaid)
			firebaseAnalyticsHelper.trackEvent(
				EVENT_SUBSCRIBE_FAILED,
				bundle
			)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackRechargeFailure(reason: String) {
		trackMixPanelRechargeFailure(reason)
		trackMoEngageRechargeFailure(reason)
		trackAppsFlyerRechargeFailure(reason)
	}

	fun trackRechargeSuccess() {
		trackMixPanelRechargeSuccess()
		trackAppsFlyerRechargeSuccess(rechargeSourceScreen, rechargeAmount)
	}

	private fun trackMixPanelRechargeSuccess() {
		mixpanelHelper.trackEvent(EVENT_TP_RECHARGE_SUCCESS, mixpanelHelper.mMixpanelUnifiedAPI)
	}

	private fun trackAppsFlyerRechargeSuccess(source: String, rechargeAmount: String) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_SOURCE] = source
//			parameterAndValueMap[PARA_AMOUNT] = rechargeAmount
			appsFlyerHelper.trackEvent(EVENT_RECHARGE_SUCCESS, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackAppsFlyerRechargeFailure(reason: String) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_REASON] = reason
			appsFlyerHelper.trackEvent(EVENT_RECHARGE_FAILED, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackAppsFlyerModifyPackSuccess(
		packName: String,
		packPrice: String?,
		modType: String,
		sourceScreen: String? = null,
		packId: String? = null,
		packDuration: String? = null,
		promoCode: String? = null,
		paymentMode: String? = null /*Actual payment mode*/,
		actualAmountPaid: String? = null  /*Actual payment amount paid*/,
		previousPackId: String?
	) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_PACK_NAME] = packName
			parameterAndValueMap[PARA_PACK_PRICE] = packPrice ?: ""
			parameterAndValueMap[PARA_MOD_TYPE] = modType
			parameterAndValueMap[PARA_SOURCE] = if (SOURCE_PLAY_CLICK.equals(sourceScreen, true)) SOURCE_CONTENT else sourceScreen ?: ""
//			appsFlyerHelper.trackEvent(EVENT_MODIFY_SUCCESS, parameterAndValueMap)

			//Parameter added for Other events
			parameterAndValueMap[PACK_ID] = packId ?:  ""
			parameterAndValueMap[PACK_DURATION] = packDuration ?: ""
			parameterAndValueMap[AFInAppEventParameterName.REVENUE] = actualAmountPaid ?: ""
			parameterAndValueMap[PROMO_CODE] = promoCode ?: ""
			parameterAndValueMap[PAYMENT_MODE] = paymentMode ?: ""
			//Parameter removed for Other events
			parameterAndValueMap.remove(PARA_MOD_TYPE)
			if (modType == PaymentUtility.SubscriptionModificationType.RENEWAL.modificationType)
				appsFlyerHelper.trackEvent(EVENT_SUBSCRIBE_SUCCESS_REPEAT, parameterAndValueMap) // Renew Paid pack success
			else {
				parameterAndValueMap[OLD_PACK_ID] = previousPackId ?: ""
				appsFlyerHelper.trackEvent(
					EVENT_SUBSCRIBE_SUCCESS_MODIFY_PACK,
					parameterAndValueMap
				) // Upgrade/Downgrade Paid pack success
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackAppsFlyerInitiateModifyPack(
		packName: String,
		packPrice: String,
		modType: String,
		sourceScreen: String
	) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_PACK_NAME] = packName
			parameterAndValueMap[PARA_PACK_PRICE] = packPrice
			parameterAndValueMap[PARA_MOD_TYPE] = if(modType == "Renewal") "Renew" else modType
			parameterAndValueMap[PARA_SOURCE] = sourceScreen
			appsFlyerHelper.trackEvent(EVENT_MODIFY_INITIATE, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackAppsFlyerSubscribeFailure(failureMsg: String, packName: String, packType: String) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_PACK_NAME] = packName
			parameterAndValueMap[PARA_TYPE] = packType
			parameterAndValueMap[PARA_REASON] = failureMsg
			appsFlyerHelper.trackEvent(EVENT_SUBSCRIBE_FAILED, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackAppsFlyerPackSelection(
		packName: String,
		packType: String,
		packDurationInDays: String,
		packPrice: String,
		packId: String,
		isUserLoggedIn: Boolean
	) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_PACK_NAME] = packName
//			parameterAndValueMap[PARA_TYPE] = packType
			parameterAndValueMap[PACK_DURATION] = packDurationInDays
			parameterAndValueMap[PACK_PRICE] = packPrice
			parameterAndValueMap[PACK_ID] = packId
			parameterAndValueMap[USER_LOGIN_STATE] =
				if (isUserLoggedIn) LOGIN_STATE_LOGGED_IN else LOGIN_STATE_NON_LOGGED_IN
			appsFlyerHelper.trackEvent(EVENT_PACK_SELECTED, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackAppsFlyerInitiatePackSelection(
		source: String,
		isUserLoggedIn: Boolean,
		listingType: String
	) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_SOURCE] = if (SOURCE_PLAY_CLICK.equals(source, true)) SOURCE_CONTENT else source
			parameterAndValueMap[USER_LOGIN_STATE] =
				if (isUserLoggedIn) LOGIN_STATE_LOGGED_IN else LOGIN_STATE_NON_LOGGED_IN
			parameterAndValueMap[LISTING_TYPE] = listingType
			appsFlyerHelper.trackEvent(EVENT_PACK_SELECTION_INITIATE, parameterAndValueMap)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageAddPackSkip() {
		moEngageHelper.trackEvent(EVENT_ADD_PACK_SKIP)
	}

	private fun trackMoEngageAddPackContinue(packName: String, packType: String, packPrice: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_PACK_NAME, packName)
			payloadBuilder.addAttribute(PARA_TYPE, packType)
			payloadBuilder.addAttribute(PARA_PACK_PRICE, packPrice)
			moEngageHelper.trackEvent(EVENT_ADD_PACK_CONTINUE, payloadBuilder)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngagePackSelection(packName: String, packType: String, packPrice: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_PACK_NAME, packName)
			payloadBuilder.addAttribute(PARA_TYPE, packType)
			payloadBuilder.addAttribute(PARA_PACK_PRICE, packPrice)
			moEngageHelper.trackEvent(EVENT_PACK_SELECTED, payloadBuilder)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageSubscribeSuccess(packType: String, sourceScreen:String,packName: String,packPrice: String, isFromNudge: Boolean, fdoRegistered: Boolean, fdoRegisteredDay: String, modType: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_PACK_NAME, packName)
			payloadBuilder.addAttribute(PARA_TYPE, packType)
			payloadBuilder.addAttribute(PARA_PACK_PRICE, packPrice)
			payloadBuilder.addAttribute(PARA_SOURCE, sourceScreen)
			payloadBuilder.addAttribute(PARA_IS_FROM_NUDGE, if(isFromNudge) "YES" else "NO")
			payloadBuilder.addAttribute(PARA_FDO_REGISTERED,  if(fdoRegistered) "YES" else "NO")
			payloadBuilder.addAttribute(PARA_FDO_DAY,  fdoRegisteredDay)
			payloadBuilder.addAttribute(PARA_MOD_TYPE, modType)
			moEngageHelper.trackEvent(EVENT_SUBSCRIBE_SUCCESS, payloadBuilder)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageSubscribeFailure(failureMsz: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_REASON, failureMsz)
			moEngageHelper.trackEvent(
				EVENT_SUBSCRIBE_FAILED,
				payloadBuilder
			)

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageInitiatePackSelection(sourceScreen: String, isFromNudge: Boolean) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_SOURCE, sourceScreen)
			payloadBuilder.addAttribute(PARA_IS_FROM_NUDGE, if(isFromNudge) "YES" else "NO")
			moEngageHelper.trackEvent(
				EVENT_PACK_SELECTION_INITIATE,
				payloadBuilder
			)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}


	private fun trackMixPanelAddPackSkip() {
		mixpanelHelper.trackEvent(EVENT_ADD_PACK_SKIP)
	}

	private fun trackMixPanelAddPackContinue(packName: String, packType: String, packPrice: String) {
		try {
			val jsonObject = JSONObject().apply {
				put(PARA_PACK_NAME, packName)
				put(PARA_TYPE, packType)
				put(PARA_PACK_PRICE, packPrice)
			}
			mixpanelHelper.trackEvent(EVENT_ADD_PACK_CONTINUE, jsonObject,mixpanelHelper.mMixpanelAPI)
			val jsonObjectUnified = JSONObject().apply {
				put(PARA_PACK_NAME, packName)
				put(PARA_TYPE, packType)
				put(PARA_PACK_PRICE, packPrice)
			}
			mixpanelHelper.trackEvent(EVENT_ADD_PACK_CONTINUE, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMixpanelPackSelectionViews(packName: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME, packName)
		}

		mixpanelHelper.trackEvent(
			EVENT_PACK_SELECTION_VIEWS,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)

	}

	private fun trackMixpanelComparePlanSelection(packName: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME, packName)
		}

		mixpanelHelper.trackEvent(
			EVENT_PACK_COMPARE_PLANS_SELECTION,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixpanelComparePlanViews(){
		mixpanelHelper.trackEvent(
			EVENT_PACK_COMPARE_PLANS_VIEW,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixpanelPackTenureView(){
		mixpanelHelper.trackEvent(
			EVENT_PACK_TENURE_VIEW,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}


	private fun trackMixpanelMyPlanDontCancelPlan(packName: String, packType: String) {
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME,packName)
			put(PARA_PACK_TYPE,packType)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_DONT_CANCEL_PLAN,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}



	private fun trackMixpanelMyPlanCancelPrimeConfirm(packName:String, packType: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME,packName)
			put(PARA_PACK_TYPE,packType)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_CANCEL_PRIME_CONFIRM,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixpanelMyPlanCancelPrime(packName: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME,packName)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_CANCEL_PRIME,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}


	private fun trackMixPanelMyPlanCancelPlanLater(packName:String, daysRemaining: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME,packName)
			put(PARA_DAYS_REMAINING,daysRemaining)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_CANCEL_PLAN_LATER,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}


	private fun trackMixPanelMyPlanCancelPlanProceed(packName:String, daysRemaining: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME,packName)
			put(PARA_DAYS_REMAINING,daysRemaining)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_CANCEL_PLAN_PROCEED,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}


	private fun trackMixPanelMyPlanCancelPlan(packName:String, daysRemaining: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME,packName)
			put(PARA_DAYS_REMAINING,daysRemaining)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PlAN_CANCEL_PLAN,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}


	private fun trackMixPanelRenewChangeTenure(
		packName: String,
		daysRemaining: String,
		tenureType: String
	) {
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME,packName)
			put(PARA_DAYS_REMAINING,daysRemaining)
			put(TENURE, tenureType)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_RENEW_CHANGE_TENURE,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelRenewChangePlan(packName:String, daysRemaining: String, tenureType: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME,packName)
			put(PARA_DAYS_REMAINING,daysRemaining)
			put(TENURE,tenureType)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_RENEW_CHANGE_PLAN,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}


	private fun trackMixPanelRenewOtherOptions(
		packName: String,
		daysRemaining: String,
		tenureType: String
	) {
		val jsonObjectUnified = JSONObject().apply {
			put(PACK_NAME, packName)
			put(PARA_DAYS_REMAINING, daysRemaining)
			put(TENURE, tenureType)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_RENEW_OTHER_OPTIONS,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}


	private fun trackMixPanelMyPlanRenewPlan(
		packName: String,
		packActive: String,
		daysRemaining: String,
		tenureType: String
	) {
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME,packName)
			put(PARA_PACK_ACTIVE,packActive)
			put(PARA_DAYS_REMAINING,daysRemaining)
			put(TENURE,tenureType)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_RENEW,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelMyPlanChangePlan(packName:String, tenureType: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME, packName)
			put(TENURE, tenureType)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_CHANGE_PLAN,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelMyPlanChangetenure(packName:String, tenureType: String){
		val jsonObjectUnified = JSONObject().apply {
			put(PARA_PACK_NAME, packName)
			put(TENURE, tenureType)
		}
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_CHANGE_TENURE,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}



	private fun trackMixPanelPlanView(
		primePackActive: String,
		packActive: String,
		packName: String,
		tenureType: String
	) {
		val jsonObjectUnified = JSONObject()
		jsonObjectUnified.put(PACK_NAME, packName)
		jsonObjectUnified.put(PARA_PACK_ACTIVE, packActive)
		jsonObjectUnified.put(PARA_PRIME_PACK_ACTIVE, primePackActive)
		jsonObjectUnified.put(TENURE, tenureType)
		mixpanelHelper.trackEvent(
			EVENT_MY_PLAN_VIEW,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelPackSelection(
		packName: String,
		packType: String,
		packPrice: String,
		packPaymentMethod:String,
		packDuration:String,
		packChangePlan:String,
		packChangeTenure:String,
		packPayableAmount:String,
		tenureType: String
	) {
		try {
			val jsonObjectUnified = JSONObject()
			jsonObjectUnified.put(PARA_PACK_NAME, packName)
			jsonObjectUnified.put(PARA_TYPE, packType)
			jsonObjectUnified.put(TENURE, tenureType)
//			jsonObjectUnified.put(PARA_PAYMENT_METHOD,packPaymentMethod)
			jsonObjectUnified.put(PARA_DURATION,packDuration)
			jsonObjectUnified.put(PARA_CHANGE_PLAN,packChangePlan)
			jsonObjectUnified.put(PARA_CHANGE_TENURE,packChangeTenure)
			jsonObjectUnified.put(PARA_PAYABLE_AMOUNT,packPayableAmount)
			mixpanelHelper.trackEvent(EVENT_PACK_SELECTED, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)


		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelSubscribeSuccess(
		packType: String,
		sourceScreen: String,
		packName: String,
		packPrice: String,
		isFromNudge: Boolean,
		fdoRegistered: Boolean,
		fdoRegisteredDay: String,
		modType: String,
		paymentMethod: String,
		paymentType: String,
		previousPackName: String? = null,
		previousPackType: String? = null,
		previousPackPrice: String? = null,
		previousPackTenure: String? = null,
		currentPackTenure: String? = null,
		isFirstSubscription: Boolean = false
	) {
		try {
			val jsonObjectUnified = JSONObject()
			jsonObjectUnified.put(PARA_PACK_NAME,packName)
			jsonObjectUnified.put(PARA_PACK_TYPE, packType)
			jsonObjectUnified.put(PARA_SOURCE, sourceScreen)
			jsonObjectUnified.put(PARA_PAYMENT_TYPE,paymentType)
			jsonObjectUnified.put(PARA_PAYMENT_METHOD,paymentMethod)
			jsonObjectUnified.put(PARA_PACK_PRICE,packPrice)
			jsonObjectUnified.put(PACK_TENURE, currentPackTenure)
			jsonObjectUnified.put(EXISTING_PACK_NAME, previousPackName)
			jsonObjectUnified.put(EXISTING_PACK_TYPE, previousPackType)
			jsonObjectUnified.put(EXISTING_PACK_PRICE, previousPackPrice)
			jsonObjectUnified.put(EXISTING_PACK_TENURE, previousPackTenure)
			jsonObjectUnified.put(FIRST_SUBSCRIPTION, if (isFirstSubscription) YES else NO)
			mixpanelHelper.trackEvent(EVENT_SUBSCRIBE_SUCCESS, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelSubscribeFailure(
		failureMsz: String,
		packName: String,
		packType: String,
		paymentMethod: String,
		paymentType: String,
		selectedPackTenure: String?,
		source: String?,
		previousPackName: String?,
		previousPackType: String?,
		packPrice: String?,
		previousPackPrice: String?,
		previousPackTenure: String?,
		isFirstSubscription: Boolean = false
	) {
		try {
			val jsonObjectUnified = JSONObject()
			jsonObjectUnified.put(PARA_PACK_NAME, packName)
			jsonObjectUnified.put(PARA_REASON, failureMsz)
			jsonObjectUnified.put(PARA_PACK_TYPE, packType)
			jsonObjectUnified.put(PACK_TENURE, selectedPackTenure)
			jsonObjectUnified.put(PARA_SOURCE, source)
			jsonObjectUnified.put(PARA_PAYMENT_TYPE, paymentType)
			jsonObjectUnified.put(PARA_PAYMENT_METHOD, paymentMethod)
			jsonObjectUnified.put(EXISTING_PACK_NAME, previousPackName)
			jsonObjectUnified.put(EXISTING_PACK_TYPE, previousPackType)
			jsonObjectUnified.put(PARA_PACK_PRICE,packPrice)
			jsonObjectUnified.put(EXISTING_PACK_PRICE, previousPackPrice)
			jsonObjectUnified.put(EXISTING_PACK_TENURE, previousPackTenure)
			jsonObjectUnified.put(FIRST_SUBSCRIPTION, if (isFirstSubscription) YES else NO)
			mixpanelHelper.trackEvent(EVENT_SUBSCRIBE_FAILED, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelInitiatePackSelection(sourceScreen: String, isFromNudge : Boolean) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_SOURCE, sourceScreen)
			jsonObject.put(PARA_IS_FROM_NUDGE, if(isFromNudge) YES else NO)
			mixpanelHelper.trackEvent(
				EVENT_PACK_SELECTION_INITIATE,
				jsonObject
			)
		} catch (e: JSONException) {
			e.printStackTrace()
		}

		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_SOURCE, sourceScreen)
			mixpanelHelper.trackEvent(
				EVENT_PACK_SELECTION_INITIATE,
				jsonObject,
				mixpanelHelper.mMixpanelUnifiedAPI
			)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageRechargeInitiate(fromScreen: String, amount: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_SOURCE, fromScreen)
			payloadBuilder.addAttribute(PARA_AMOUNT, amount)
			moEngageHelper.trackEvent(EVENT_RECHARGE_INITIATE, payloadBuilder)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelRechargeInitiate(fromScreen: String, amount: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_SOURCE, fromScreen)
			jsonObject.put(PARA_AMOUNT, amount)
			mixpanelHelper.trackEvent(EVENT_TP_RECHARGE_INITIATE,jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMoEngageRechargeFailure(reason: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_REASON, reason)
			moEngageHelper.trackEvent(EVENT_RECHARGE_FAILED, payloadBuilder)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	private fun trackMixPanelRechargeFailure(reason: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_REASON, reason)
			mixpanelHelper.trackEvent(EVENT_TP_RECHARGE_FAILED, jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}

	fun updateAdditionalData() {
		appsFlyerHelper.generateAdditionalData()
	}

	fun trackUpdatedPackDetails(partnerPack: PartnerPacks?) {
		trackMixPanelPackDetails(partnerPack)
		trackMoEngagePackDetails(partnerPack)
	}
	fun updateGroupProfileWithSubscription(key :String, value : String,partnerPacks: PartnerPacks?){
		mixpanelHelper.updateGroupProfile(key, value, partnerPacks)
	}
	fun updateProperty(key:String,value:Int){
		mixpanelHelper.updateProperty(key, value)
		mixpanelHelper.updateProperty(key, value,mixpanelHelper.mMixpanelUnifiedAPI)
		moEngageHelper.updateProperty(key, value)
	}

	fun addUpdateSuperProperty(key: String, value: String){
		mixpanelHelper.addUpdateSuperProperty(key, value)
	}

	fun updateProperty(key: String, value: String){
		mixpanelHelper.updateProperty(key, value)
		moEngageHelper.updateProperty(key, value)
	}

	private fun trackMixPanelPackDetails(pack: PartnerPacks?) {

		mixpanelHelper.updateProperty(AQUISITION_SOURCE,pack?.acquisitionSource?:"")
    	mixpanelHelper.updateProperty(SUBSCRIBED, if (true == pack?.subscriptionStatus?.equals(SubscriptionPackStatusEnum.ACTIVE.status, true)) YES else NO,mixpanelHelper.mMixpanelUnifiedAPI)
		mixpanelHelper.updateProperty(FREE_TRIAL, pack?.freeTrialStatus?.let { if (it) YES else NO } ?: "",mixpanelHelper.mMixpanelUnifiedAPI)
		mixpanelHelper.updateProperty(SI_PAYMENT_MODE,
			pack?.planCTADetails?.renewPlanOption?.let { if (it) "NO" else YES } ?: "",mixpanelHelper.mMixpanelUnifiedAPI)
		mixpanelHelper.updateProperty(RECURRING_PAYMENT,
			pack?.planCTADetails?.renewPlanOption?.let { if (it) "NO" else YES } ?: "",mixpanelHelper.mMixpanelUnifiedAPI)
		mixpanelHelper.updateProperty(PACK_NAME, pack?.productName?:FREEMIUM,mixpanelHelper.mMixpanelUnifiedAPI)
		mixpanelHelper.updateProperty(PACK_PRICE, pack?.amountValue?:FREEMIUM,mixpanelHelper.mMixpanelUnifiedAPI)
		//required
		mixpanelHelper.updateProperty(PACK_TYPE, if(pack?.subscribedBingeProduct != null) "PAID" else "FREE",mixpanelHelper.mMixpanelUnifiedAPI)
		mixpanelHelper.updateProperty(FTV_ELIGIBLE, if(pack?.eligibleFirestick == true) YES else NO,mixpanelHelper.mMixpanelUnifiedAPI)
		mixpanelHelper.updateProperty(SUBSCRIPTION_TYPE, pack?.subscriptionType ?: "UNSUBSCRIBED",mixpanelHelper.mMixpanelUnifiedAPI)
		//required
		mixpanelHelper.updateProperty(BURN_RATE_TYPE,pack?.burnRateType ?: "", mixpanelHelper.mMixpanelUnifiedAPI)
		//required
		mixpanelHelper.updateProperty(DATE_OF_SUBSCRIPTION, pack?.packCreationDate?:"",mixpanelHelper.mMixpanelUnifiedAPI)
		//required
		val date = pack?.packCreationDate?.let { getDateObject(it, START_DATE_FORMAT) }
		mixpanelHelper.updateProperty(PACK_START_DATE, date,mixpanelHelper.mMixpanelUnifiedAPI)
		//required
		val expDate = pack?.expirationDate?.let{getDateObject(it, END_DATE_FORMAT)}
		mixpanelHelper.updateProperty(PACK_END_DATE, expDate, mixpanelHelper.mMixpanelUnifiedAPI)
		//required
		val rechargeDate = pack?.rechargeDue?.let{getDateObject(it, END_DATE_FORMAT)}
		mixpanelHelper.updateProperty(PACK_RENEWAL_DATE, rechargeDate,mixpanelHelper.mMixpanelUnifiedAPI)
		//required
		pack?.let {
			mixpanelHelper.updateProperty(RENEWAL_DUE, if(true == it.subscriptionStatus?.equals(SubscriptionPackStatusEnum.ACTIVE.status, true) && !it.isCancelled) YES else NO)
		} ?: mixpanelHelper.updateProperty(RENEWAL_DUE,  NO,mixpanelHelper.mMixpanelUnifiedAPI)
		//required
		pack?.let {
			mixpanelHelper.updateProperty(FREE_TRIAL_ELIGIBLE, NO, mixpanelHelper.mMixpanelUnifiedAPI)
			mixpanelHelper.updateProperty(
				FIRST_PACK_SUBSCRIPTION_DATE,
				it.firstPaidPackSubscriptionDate
					?: "" /*TSF-8151 Due to Free trial disable from BE, Moving it to firstPaidPackSubscriptionDate*/,
				mixpanelHelper.mMixpanelUnifiedAPI
			)
			mixpanelHelper.updateProperty(
				FIRST_PAID_PACK_SUBSCRIPTION_DATE,
				it.firstPaidPackSubscriptionDate ?: "",
				mixpanelHelper.mMixpanelUnifiedAPI
			)
		}
			?: kotlin.run {
				mixpanelHelper.updateProperty(FREE_TRIAL_ELIGIBLE, NO, mixpanelHelper.mMixpanelUnifiedAPI)
				mixpanelHelper.updateProperty(
					FIRST_PACK_SUBSCRIPTION_DATE,
					"" ,
					mixpanelHelper.mMixpanelUnifiedAPI
				)
				mixpanelHelper.updateProperty(
					FIRST_PAID_PACK_SUBSCRIPTION_DATE,
					"",
					mixpanelHelper.mMixpanelUnifiedAPI
				)
			}
	}

	private fun trackMoEngagePackDetails(pack: PartnerPacks?) {

		pack?.let {


			it.subscriptionStatus?.let {
				moEngageHelper.updateProperty(SUBSCRIBED, if (true == it.equals(SubscriptionPackStatusEnum.ACTIVE.status, true)) "YES" else "NO")
			}


			it.acquisitionSource?.let {
				moEngageHelper.updateProperty(AQUISITION_SOURCE, it)
			}

			it.freeTrialStatus?.let {
				moEngageHelper.updateProperty(FREE_TRIAL, if (it) "YES" else "NO")
			}
			it.planCTADetails?.let {
				it.renewPlanOption?.let {
					moEngageHelper.updateProperty(SI_PAYMENT_MODE,  if(it) "NO" else "YES")
					moEngageHelper.updateProperty(RECURRING_PAYMENT,  if(it) "NO" else "YES")
				}
			}
			it.productName?.let {
				moEngageHelper.updateProperty(PACK_NAME, it)
			}
			it.amountValue?.let {
				moEngageHelper.updateProperty(PACK_PRICE, it)
			}

			it.eligibleFirestick.let {
				moEngageHelper.updateProperty(FREE_TRIAL_ELIGIBLE, if(it== true) "YES" else "NO")
			}

			it.currentTenure?.let {
				moEngageHelper.updateProperty(PACK_TYPE,  it)
			}

		}

		/*if (pack?.subscribedBingeProduct != null){
			moEngageHelper.updateProperty(PACK_TYPE,  "PAID")
		}*/

		if(pack?.eligibleFirestick!=null){
			moEngageHelper.updateProperty(FTV_ELIGIBLE, if(pack.eligibleFirestick == true) "YES" else "NO")
		}

		moEngageHelper.updateProperty(SUBSCRIPTION_TYPE, pack?.subscriptionType ?: "UNSUBSCRIBED")

		pack?.let {
			it.burnRateType?.let {
				if (it!=""){
					moEngageHelper.updateProperty(BURN_RATE_TYPE, it)
				}
			}
		}

		val dateOfSub = pack?.packCreationDate?.let{getDateObject(it, START_DATE_FORMAT)}
		dateOfSub?.let {
			moEngageHelper.updateProperty(DATE_OF_SUBSCRIPTION, dateOfSub)
			moEngageHelper.updateProperty(PACK_START_DATE, dateOfSub)
		}
		val endDate = pack?.expiryDateWithTime?.let{getDateObject(it, DEAFULT_DATE_FORMAT_FROM_BE)}
		endDate?.let {
			moEngageHelper.updateProperty(PACK_END_DATE, endDate)
		}
		val renewDate = pack?.rechargeDue?.let{getUtcDateWithOneSecObject(it, END_DATE_FORMAT)}
		renewDate?.let {
			moEngageHelper.updateProperty(PACK_RENEWAL_DATE, renewDate)
		}

		if (pack?.subscriptionStatus!=null){
			moEngageHelper.updateProperty(RENEWAL_DUE, if(true == pack.subscriptionStatus?.equals(SubscriptionPackStatusEnum.ACTIVE.status, true) && !pack.isCancelled) "YES" else "NO")
		}

	}

	fun trackUpSellView(price: String) {
		trackMixPanelUpsellView(price)
		trackMoengageUpsellView(price)
	}

	private fun trackMixPanelUpsellView(price: String) {
		try {
			val jsonObject = JSONObject()
			mixpanelHelper.trackEvent(EVENT_FTV_UPSELL_VIEW, jsonObject)

			val jsonObjectUnified = JSONObject().apply {
				put(PACK_PRICE, price)
			}
			mixpanelHelper.trackEvent(
				EVENT_FTV_UPSELL_VIEW,
				jsonObjectUnified,
				mixpanelHelper.mMixpanelUnifiedAPI
			)
		} catch (e: Exception) {
		}
	}

	private fun trackMoengageUpsellView(price: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PACK_PRICE, price)
			moEngageHelper.trackEvent(EVENT_FTV_UPSELL_VIEW, payloadBuilder)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}


	fun trackUpSellConverted(price: String, upgradedPackPrice: String) {
		trackMixpanelUpSellConverted(price, upgradedPackPrice)
		trackMoengageUpSellConverted(price, upgradedPackPrice)
	}

	private fun trackMixpanelUpSellConverted(price: String, upgradedPackPrice: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PACK_PRICE, price)
			jsonObject.put(UPGRADED_PACK_PRICE, upgradedPackPrice)
			mixpanelHelper.trackEvent(EVENT_FTV_UPSELL_CONVERTED, jsonObject)

			val jsonObjectUnified = JSONObject()
			jsonObjectUnified.put(PACK_PRICE, price)
			jsonObjectUnified.put(UPGRADED_PACK_PRICE, upgradedPackPrice)
			mixpanelHelper.trackEvent(
				EVENT_FTV_UPSELL_CONVERTED,
				jsonObjectUnified,
				mixpanelHelper.mMixpanelUnifiedAPI
			)

		} catch (e: Exception) {
		}
	}

	private fun trackMoengageUpSellConverted(price: String, upgradedPackPrice: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PACK_PRICE, price)
			payloadBuilder.addAttribute(UPGRADED_PACK_PRICE, upgradedPackPrice)
			moEngageHelper.trackEvent(EVENT_FTV_UPSELL_CONVERTED, payloadBuilder)
		} catch (e: Exception) {
		}
	}

	fun trackPaymentInitiate(
		sourceScreen: String,
		packName: String,
		packPrice: String,
		modType: String,
		packType: String,
		previousPackName: String,
		previousPackType: String,
		previousPackPrice: String,
		previousPackTenure: String,
		currentPackTenure: String,
		isFirstSubscription: Boolean = false
	) {
		trackMixpanelPaymentInitiate(
			sourceScreen,
			packName,
			packPrice,
			modType,
			packType,
			previousPackName,
			previousPackType,
			previousPackPrice,
			previousPackTenure,
			currentPackTenure,
			isFirstSubscription
		)
	}

	private fun trackMixpanelPaymentInitiate(
		sourceScreen: String,
		packName: String,
		packPrice: String,
		modType: String,
		packType: String,
		previousPackName: String,
		previousPackType: String,
		previousPackPrice: String,
		previousPackTenure: String,
		currentPackTenure: String,
		isFirstSubscription: Boolean = false
	) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_PACK_NAME, packName)
			jsonObject.put(PARA_PACK_PRICE, packPrice)
			jsonObject.put(PARA_SOURCE, sourceScreen)
			jsonObject.put(PARA_MOD_TYPE, modType.uppercase())
			jsonObject.put(PACK_TYPE, packType)
			jsonObject.put(PACK_TENURE, currentPackTenure)
			jsonObject.put(EXISTING_PACK_NAME, previousPackName)
			jsonObject.put(EXISTING_PACK_TYPE, previousPackType)
			jsonObject.put(EXISTING_PACK_PRICE, previousPackPrice)
			jsonObject.put(EXISTING_PACK_TENURE, previousPackTenure)
			jsonObject.put(FIRST_SUBSCRIPTION, if (isFirstSubscription) YES else NO)
			mixpanelHelper.trackEvent(
				PAYMENT_INITIATE,
				jsonObject,
				mixpanelHelper.mMixpanelUnifiedAPI
			)
		} catch (e: JSONException) {
			e.printStackTrace()
		}
	}


	fun trackModifyPackInitiate(
		sourceScreen: String,
		packName: String,
		packPrice: String,
		modType: String,
		sourceScreenForAppsFlyer: String = "",
		selectedTenurePackPrice: String = "",
		selectedTenureInDaysWithDSuffix: String = "",
		packType: String? = null,
		previousPackName: String? = null,
		previousPackType: String? = null,
		previousPackPrice: String? = null,
		previousPackTenure: String? = null,
		currentPackTenure: String? = null,
		isFirstSubscription: Boolean = false
	) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_PACK_NAME, packName)
			payloadBuilder.addAttribute(PARA_MOD_TYPE, modType)
			payloadBuilder.addAttribute(PARA_PACK_PRICE, packPrice)
			payloadBuilder.addAttribute(PARA_SOURCE, sourceScreen)
			moEngageHelper.trackEvent(EVENT_MODIFY_INITIATE, payloadBuilder)
		} catch (e: Exception) {
		}
		trackAppsFlyerInitiateModifyPack(packName, selectedTenurePackPrice, modType, sourceScreenForAppsFlyer)
	}

	fun trackModifyPackSuccess(
		sourceScreen: String,
		packName: String,
		packPrice: String /*Base pack price*/,
		modType: String,
		packId: String? = null,
		packDuration: String? = null,
		promoCode: String?= null,
		paymentMode: String? = null /*Actual payment mode*/,
		actualAmountPaid: String? = null  /*Actual payment made*/,
		previousPackId: String? = null,
		sourceScreenForAppsFlyer: String? = null,
		selectedTenurePackPrice: String? = null /*Base pack price X Tenure*/,
		selectedTenureInDaysWithDSuffix: String? = null,
		packType: String? = null,
		previousPackName: String? = null,
		previousPackType: String? = null,
		previousPackPrice: String? = null,
		previousPackTenure: String? = null,
		currentPackTenure: String? = null,
		isFirstSubscription: Boolean = false,
		productType: String? /*MyOP/Curated*/
	) {

		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_PACK_NAME, packName)
			payloadBuilder.addAttribute(PARA_MOD_TYPE, modType)
			payloadBuilder.addAttribute(PARA_PACK_PRICE, packPrice)
			payloadBuilder.addAttribute(PARA_SOURCE, sourceScreen)
			moEngageHelper.trackEvent(EVENT_MODIFY_SUCCESS, payloadBuilder)
		} catch (e: Exception) {
		}
		trackAppsFlyerModifyPackSuccess(
			packName,
			selectedTenurePackPrice,
			modType,
			sourceScreenForAppsFlyer,
			packId,
			selectedTenureInDaysWithDSuffix,
			promoCode,
			paymentMode,
			actualAmountPaid,
			previousPackId
		)
		trackFirebaseModifyPackSuccess(
			packName,
			selectedTenurePackPrice,
			modType,
			sourceScreenForAppsFlyer,
			packId,
			selectedTenureInDaysWithDSuffix,
			promoCode,
			paymentMode,
			actualAmountPaid,
			previousPackId,
			packType,
			productType
		)
		trackFacebookModifyPackSuccess(
			packName,
			selectedTenurePackPrice,
			modType,
			sourceScreenForAppsFlyer,
			packId,
			selectedTenureInDaysWithDSuffix,
			promoCode,
			paymentMode,
			actualAmountPaid,
			previousPackId,
			packType
		)
	}

	private fun trackFacebookModifyPackSuccess(
		packName: String,
		selectedTenurePackPrice: String?,
		modType: String,
		sourceScreenForAppsFlyer: String?,
		packId: String?,
		selectedTenureInDaysWithDSuffix: String?,
		promoCode: String?,
		paymentMode: String?,
		actualAmountPaid: String?,
		previousPackId: String?,
		packType: String?
	) {
		try {
			val bundle = Bundle()
			bundle.putString(
				PARA_SOURCE,
				if (SOURCE_PLAY_CLICK.equals(sourceScreenForAppsFlyer, true))
					SOURCE_CONTENT
				else
					sourceScreenForAppsFlyer
			)
			bundle.putString(PARA_PACK_NAME, packName)
			bundle.putString(PACK_PRICE, selectedTenurePackPrice)
			bundle.putString(PACK_ID, packId)
			bundle.putString(PACK_DURATION, selectedTenureInDaysWithDSuffix)
			bundle.putString(PROMO_CODE, promoCode)
			bundle.putString(PARA_PACK_TYPE, packType)
			bundle.putString(PAYMENT_MODE, paymentMode)
			if (modType == PaymentUtility.SubscriptionModificationType.RENEWAL.modificationType)
				facebookAnalyticsHelper.trackEvent(
					EVENT_SUBSCRIBE_SUCCESS_REPEAT,
					bundle
				) //Renew Paid pack success
			else {
				bundle.putString(OLD_PACK_ID, previousPackId)
				facebookAnalyticsHelper.trackEvent(
					EVENT_SUBSCRIBE_SUCCESS_MODIFY_PACK,
					bundle
				) //Upgrade/Downgrade Paid pack success
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun trackFirebaseModifyPackSuccess(
		packName: String,
		selectedTenurePackPrice: String?,
		modType: String,
		sourceScreenForAppsFlyer: String?,
		packId: String?,
		selectedTenureInDaysWithDSuffix: String?,
		promoCode: String?,
		paymentMode: String?,
		actualAmountPaid: String?,
		previousPackId: String?,
		packType: String?,
		productType: String?
	) {
		try {
			val bundle = Bundle()
			bundle.putString(
				PARA_SOURCE,
				if (SOURCE_PLAY_CLICK.equals(sourceScreenForAppsFlyer, true))
					SOURCE_CONTENT
				else
					sourceScreenForAppsFlyer
			)
			bundle.putString(PARA_PACK_NAME, packName)
			bundle.putString(PACK_PRICE, selectedTenurePackPrice)
			bundle.putString(PACK_ID, packId)
			bundle.putString(PACK_DURATION, selectedTenureInDaysWithDSuffix)
			bundle.putString(PROMO_CODE, promoCode)
			bundle.putString(PARA_PACK_TYPE, productType)
			bundle.putString(PAYMENT_MODE, paymentMode)
			if (modType == PaymentUtility.SubscriptionModificationType.RENEWAL.modificationType)
				firebaseAnalyticsHelper.trackEvent(
					EVENT_SUBSCRIBE_SUCCESS_REPEAT,
					bundle
				) //Renew Paid pack success
			else {
				bundle.putString(OLD_PACK_ID, previousPackId)
				firebaseAnalyticsHelper.trackEvent(
					EVENT_SUBSCRIBE_SUCCESS_MODIFY_PACK,
					bundle
				) //Upgrade/Downgrade Paid pack success
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackFsWo(created: Boolean, type: String) {
		trackMixpanelFsWo(created, type)
		trackMoengageFsWo(created, type)
	}

	private fun trackMixpanelFsWo(created: Boolean, type: String) {
		try {
			val jsonObjectUnified = JSONObject()
			jsonObjectUnified.put(ORDERED_FTV, if (created) YES else NO)
			jsonObjectUnified.put(FTV_ORDER_TYPE, type)
			mixpanelHelper.trackEvent(EVENT_FTV_WO, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
		} catch (e: Exception) {
		}
	}

	private fun trackMoengageFsWo(created: Boolean, type: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(ORDERED_FTV, if (created) YES else NO)
			payloadBuilder.addAttribute(FTV_ORDER_TYPE, type)
			moEngageHelper.trackEvent(EVENT_FTV_WO, payloadBuilder)
		} catch (e: Exception) {
		}
	}

	fun trackFreeTrialLater(){
		mixpanelHelper.trackEvent(EVENT_FREE_TRIAL_LATER)
		moEngageHelper.trackEvent(EVENT_FREE_TRIAL_LATER)
	}

	fun trackSubscriptionCancel(bingeCancellationDate : String? , primeCancellationDate : String?){
		trackMixpanelSubscriptionCancel(bingeCancellationDate, primeCancellationDate)
		trackMoengageSubscriptionCancel(bingeCancellationDate, primeCancellationDate)
	}

	fun trackSubscriptionRevoke(source : String){
		trackMixpanelSubscriptionRevoke(source)
		trackMoengageSubscriptionRevoke(source)
	}

	fun trackSubscriptionCancelSkip(){
		mixpanelHelper.trackEvent(EVENT_SUBSCRIPTION_CANCEL_SKIP)
		moEngageHelper.trackEvent(EVENT_SUBSCRIPTION_CANCEL_SKIP)
	}

	fun trackSubscriptionRevokeSkip(){
		mixpanelHelper.trackEvent(EVENT_SUBSCRIPTION_REVOKE_SKIP)
		moEngageHelper.trackEvent(EVENT_SUBSCRIPTION_REVOKE_SKIP)
	}

	fun trackSubscriptionDrawerSelectMYOP(source: String) {
		trackMixPanelSubscriptionDrawerSelectMYOP(source)
	}

	fun trackSubscriptionDrawerSelectCuratedMYOP(source: String) {
		trackMixPanelSubscriptionDrawerSelectCuratedMYOP(source,SUB_DRAWER_CURATED_SELECT_MYOP)
	}
	fun trackSubscriptionDrawerSelectExplore(source: String) {
		trackMixPanelSubscriptionDrawerSelectCuratedMYOP(source, SUB_DRAWER_CURATED_EXPLORE)
	}

	fun trackSubscriptionDrawerInitiate(
		source: String,
		isUserLoggedIn: Boolean = false,
		listingType: String = LISTING_TYPE_DRAWER,
		sourceScreenForAppsFlyer: String
	) {
		trackMixPanelSubscriptionDrawerInitiate(source)
		trackAppsFlyerInitiatePackSelection(sourceScreenForAppsFlyer, isUserLoggedIn, listingType)
		trackFirebaseInitiatePackSelection(sourceScreenForAppsFlyer, isUserLoggedIn, listingType)
		trackFacebookInitiatePackSelection(sourceScreenForAppsFlyer, isUserLoggedIn, listingType)
	}

	fun trackSubscrptionDrawerLater(source: String) {
		trackMixPanelDrawerLater(source)
	}

	fun trackSubscriptionDrawerProceed(source: String, packName: String) {
		trackMixPanelSubscriptionDrawerProceed(source, packName)
	}

	fun trackExistingUserLogin(source: String) {
		trackMixPanelExistingUserLogin(source)
	}

	fun trackSubscriptionDrawerClose(source: String,packName: String){
		trackMixPanelSubscriptionDrawerClose(source, packName)
	}

	private fun trackMixPanelSubscriptionDrawerSelectMYOP(source: String) {
		val jsonObjectUnified = JSONObject()
		jsonObjectUnified.put(
			PARA_SOURCE,
			if (SOURCE_PLAY_CLICK.equals(source,true))
				CONTENT_PLAYBACK
			else if (source.equals(SOURCE_APP_LAUNCH, true))
				APPLAUNCH
			else
				source
		)
		mixpanelHelper.trackEvent(
			SUB_DRAWER_SELECT_MYOP,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelSubscriptionDrawerSelectCuratedMYOP(source: String, key: String) {
		val jsonObjectUnified = JSONObject()
		jsonObjectUnified.put(
			PARA_SOURCE,
			if (SOURCE_PLAY_CLICK.equals(source,true))
				CONTENT_PLAYBACK
			else if (source.equals(SOURCE_APP_LAUNCH, true))
				APPLAUNCH
			else
				source
		)
		mixpanelHelper.trackEvent(
			key,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelSubscriptionDrawerClose(source: String, packName: String) {
		val jsonObjectUnified = JSONObject()
		jsonObjectUnified.put(
			PARA_SOURCE,
			if (SOURCE_PLAY_CLICK.equals(source,true))
				CONTENT_PLAYBACK
			else if (source.equals(SOURCE_APP_LAUNCH, true))
				APPLAUNCH
			else
				source
		)
//		jsonObjectUnified.put(PARA_PACK_NAME, packName)
		mixpanelHelper.trackEvent(
			SUB_DRAWER_CLOSE_MYOP,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelExistingUserLogin(source: String) {
		val jsonObjectUnified = JSONObject()
		jsonObjectUnified.put(
			PARA_SOURCE,
			if (SOURCE_PLAY_CLICK.equals(source,true))
				CONTENT_PLAYBACK
			else if (source.equals(SOURCE_APP_LAUNCH, true))
				APPLAUNCH
			else
				source
		)
		mixpanelHelper.trackEvent(
			EVENT_SUBSCRIPTION_DRAWER_EXISTING_USER_LOGIN,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelSubscriptionDrawerProceed(source: String, packName: String) {
		val jsonObjectUnified = JSONObject()
		jsonObjectUnified.put(
			PARA_SOURCE,
			if (SOURCE_PLAY_CLICK.equals(source,true))
				CONTENT_PLAYBACK
			else if (source.equals(SOURCE_APP_LAUNCH, true))
				APPLAUNCH
			else
				source
		)
		jsonObjectUnified.put(PARA_PACK_NAME, packName)
		mixpanelHelper.trackEvent(
			EVENT_SUBSCRIPTION_DRAWER_PROCEED,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelDrawerLater(source: String) {
		val jsonObjectUnified = JSONObject()
		jsonObjectUnified.put(
			PARA_SOURCE,
			if (SOURCE_PLAY_CLICK.equals(source,true))
				CONTENT_PLAYBACK
			else if (source.equals(SOURCE_APP_LAUNCH, true))
				APPLAUNCH
			else
				source
		)
		mixpanelHelper.trackEvent(
			SUB_DRAWER_DO_IT_LATER_MYOP,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	private fun trackMixPanelSubscriptionDrawerInitiate(source: String) {
		val jsonObjectUnified = JSONObject()
		jsonObjectUnified.put(
			PARA_SOURCE,
			if (SOURCE_PLAY_CLICK.equals(source,true))
				CONTENT_PLAYBACK
			else if (source.equals(SOURCE_APP_LAUNCH, true))
				APPLAUNCH
			else
				source
		)
		mixpanelHelper.trackEvent(
			SUB_DRAWER_LAUNCH_MYOP,
			jsonObjectUnified,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}




	private fun trackMixpanelSubscriptionCancel(bingeCancellationDate: String?, primeCancellationDate: String?){
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_BINGE_CANCELLATION, if(bingeCancellationDate.isNullOrBlank()) NO else YES )
			bingeCancellationDate.takeIf { !it.isNullOrBlank() }?.let {
				jsonObject.put(PARA_BINGE_CANCEL_ON, Date())
				jsonObject.put(PARA_BINGE_CANCEL_DATE, getDateObject(it, END_DATE_FORMAT) )
			}
			primeCancellationDate.takeIf { !it.isNullOrBlank() }?.let {
				jsonObject.put(PARA_PRIME_CANCEL_ON, Date())
				jsonObject.put(PARA_PRIME_CANCEL_DATE, getDateObject(it, END_DATE_FORMAT) )
			}
			jsonObject.put(PARA_PRIME_CANCELLATION, if(primeCancellationDate.isNullOrBlank()) NO else YES )
			mixpanelHelper.trackEvent(EVENT_SUBSCRIPTION_CANCEL, jsonObject)
		} catch (e: Exception) {
		}

	}

	private fun trackMoengageSubscriptionCancel(
		bingeCancellationDate: String?,
		primeCancellationDate: String?
	) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_BINGE_CANCELLATION, if(bingeCancellationDate.isNullOrBlank()) "NO" else "YES")
			bingeCancellationDate.takeIf { !it.isNullOrBlank() }?.let {
				payloadBuilder.addAttribute(PARA_BINGE_CANCEL_ON, Date())
				payloadBuilder.addAttribute(PARA_BINGE_CANCEL_DATE, getDateObject(it, END_DATE_FORMAT) )
			}
			primeCancellationDate.takeIf { !it.isNullOrBlank() }?.let {
				payloadBuilder.addAttribute(PARA_PRIME_CANCEL_ON, Date())
				payloadBuilder.addAttribute(PARA_PRIME_CANCEL_DATE, getDateObject(it, END_DATE_FORMAT) )
			}
			payloadBuilder.addAttribute(PARA_PRIME_CANCELLATION, if(primeCancellationDate.isNullOrBlank()) "NO" else "YES" )
			moEngageHelper.trackEvent(EVENT_SUBSCRIPTION_CANCEL, payloadBuilder)
		} catch (e: Exception) {
		}
	}

	private fun trackMoengageSubscriptionRevoke(source: String) {
		try {
			val payloadBuilder = Properties()
			payloadBuilder.addAttribute(PARA_SOURCE, source)
			payloadBuilder.addAttribute(PARA_REVOKED_ON, Date())
			moEngageHelper.trackEvent(EVENT_SUBSCRIPTION_REVOKE, payloadBuilder)
		} catch (e: Exception) {
		}
	}

	private fun trackMixpanelSubscriptionRevoke(source: String) {
		try {
			val jsonObject = JSONObject()
			jsonObject.put(PARA_SOURCE, source)
			jsonObject.put(PARA_REVOKED_ON, Date())
			mixpanelHelper.trackEvent(EVENT_SUBSCRIPTION_REVOKE, jsonObject)
		} catch (e: Exception) {
		}
	}

	private fun trackAppsFlyerSubscribeSuccess(
		packType: String,
		sourceScreen: String? = null,
		packName: String,
		packPrice: String?,
		packId: String?,
		packDuration: String?,
		promoCode: String?,
		paymentMode: String?,
		isSubscribedForPaidPackFirstTime: Boolean,
		actualAmountPaid: String?,
		fdoRegistered: Boolean
	) {
		try {
			val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
			parameterAndValueMap[PARA_SOURCE] = if (SOURCE_PLAY_CLICK.equals(sourceScreen, true)) SOURCE_CONTENT else sourceScreen ?: ""
			parameterAndValueMap[PARA_PACK_NAME] = packName
			if (packType.equals(PACK_TYPE_FREE, true))
				appsFlyerHelper.trackEvent(EVENT_FREE_TRIAL_SUCCESS, parameterAndValueMap) // Free trial subscription success
			else {
				parameterAndValueMap[PACK_PRICE] = packPrice ?: ""
				parameterAndValueMap[PACK_ID] = packId ?:  ""
				parameterAndValueMap[PACK_DURATION] = packDuration ?: ""
				parameterAndValueMap[AFInAppEventParameterName.REVENUE] = actualAmountPaid ?: ""
				parameterAndValueMap[PROMO_CODE] = promoCode ?: ""
				parameterAndValueMap[PAYMENT_MODE] = paymentMode ?: ""
				parameterAndValueMap[PACK_TYPE] = packType
				appsFlyerHelper.trackEvent(EVENT_SUBSCRIPTION_SUCCESS, parameterAndValueMap) // Paid pack subscription success
				parameterAndValueMap.remove(PACK_TYPE) // Remove after triggering for EVENT_SUBSCRIPTION_SUCCESS
				if (isSubscribedForPaidPackFirstTime && !fdoRegistered)
					appsFlyerHelper.trackEvent(EVENT_SUBSCRIBE_SUCCESS_NEW, parameterAndValueMap) // First Paid pack subscription success
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun trackSubscriptionPageProceed(source: String, packName: String?, amount: String?) {
		trackMixPanelSubscriptionPageProceed(source, packName, amount)
	}

	private fun trackMixPanelSubscriptionPageProceed(
		source: String,
		packName: String?,
		amount: String?
	) {
		val jsonObject = JSONObject()
		jsonObject.put(PARA_SOURCE, source)
		jsonObject.put(PARA_PACK_NAME, packName)
		jsonObject.put(PARA_PACK_AMOUNT, amount)

		mixpanelHelper.trackEvent(
			EVENT_SUBSCRIPTION_PAGE_PROCEED,
			jsonObject,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	fun trackSubscriptionPageInitiate(source: String) {
		trackMixpanelSubscriptionPageInitiate(source)
	}

	private fun trackMixpanelSubscriptionPageInitiate(source: String) {
		val jsonObject = JSONObject()
		jsonObject.put(PARA_SOURCE, source)
		mixpanelHelper.trackEvent(
			EVENT_SUBSCRIPTION_PAGE_INITIATE,
			jsonObject,
			mixpanelHelper.mMixpanelUnifiedAPI
		)
	}

	/*Clevertap_Phase 2- Nikhil
		To update identity in Clevertap
	 */
	fun updateIdentity(sid: String){
		moEngageHelper.updateIdentity(sid)
	}



}