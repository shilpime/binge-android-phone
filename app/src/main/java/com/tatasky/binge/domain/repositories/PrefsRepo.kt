package com.tatasky.binge.domain.repositories

import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.Languages
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import java.util.ArrayList
import java.util.HashMap

interface PrefsRepo {
    /**
     * All shared preference transaction methods should go here
     */
    fun getCloudenieryUrl(): String?

    fun saveCloudenieryUrl(url: String?)

    fun isConfigured(): Boolean

    fun setConfigured(b: Boolean)

    fun getLoginStatus(): Boolean

    fun getProfileId(): String?

    fun setProfileId(pId: String)

    fun getBaId(): String

    fun setBaId(baId: String)

    fun getOriginalSubscriberId(): String

    fun setOriginalSubscriberId(sId: String)

    fun getAccessToken(): String?

    fun setAccessToken(token: String)

    fun getDeviceToken(): String?

    fun setDeviceToken(token: String)

    fun saveAccountDetails(accountJson: String)

    fun getAccountDetails(): String?

//    fun <T> getLiveValue(key: String, defaultValue: T): LiveData<T>
//
//    fun getLiveStringValue(key: String, defValue: String): LiveData<String>
//
//    fun getLiveBooleanValue(key: String, defValue: Boolean): LiveData<Boolean>

    fun logoutUser()
    fun getEntitlements(): Set<String>?
    fun getMOEUserTracked(): Boolean
    fun setMOEUserTracked(isTracked: Boolean)
    fun getRrmSessionInfo(): RrmSessionInfo?
    fun setConfigAppVersion(androidVersion: ConfigResponse.Android)
    fun getConfigAppVersion(): ConfigResponse.Android?


    fun setTrailerSound(soundOn: Boolean)
    fun setAutoPlayTrailerOn(autoPlay: Boolean)
    fun getTrailerSound(): Boolean
    fun getAutoPlayTrailerOn(): Boolean

    fun saveSearchKeyword(keyWord: String)
    fun clearAllSearchKeyword()
    fun clearRecentSearchItem(keyWord: String)
    fun getSearchKeywords(): List<String>

    fun setAllowTransactionalNotification(allow: Boolean)
    fun setAllowWatchNotification(allow: Boolean)
    fun setAllowOffersNotification(allow: Boolean)
    fun getTransactionalNotificationAllowed(): Boolean
    fun getWatchNotificationAllowed(): Boolean
    fun getOffersNotificationAllowed(): Boolean

    fun clear()
    fun clearSwitchAccountInfo()

    fun saveOtpResentCount(allow: Int)
    fun saveOtpDuration(allow: Int)
    fun getOtpResentCount(): Int
    fun getOtpDuration(): Int

    fun getUserDetails(): LoginResponse.BingeSubscription?
    fun setPasswordCreated(isPassword: Boolean)
    fun isPasswordCreated(): Boolean
    fun setPDFDownloaded(data: String)
    fun getPDFDownloaded(): Set<String>?

    fun addConfigResponse(configResponse: ConfigResponse)
    fun getConfigResponse(): ConfigResponse?

    fun setEntitlements(listOfIds: Set<String>)

    fun setSelectedProfile(data: LoginResponse.BingeSubscription)
    fun getSelectedProfile(): LoginResponse.BingeSubscription?

    fun savePrivacyPolicyUrl(url: String)
    fun getPrivacyPolicyUrl(): String?

    fun saveTnCUrl(url: String)
    fun getTnCUrl(): String?

    fun saveEulaTitle(title:String)
    fun getEulaTitle() : String?

    fun saveEulaSubTitle(subtitle:String)
    fun getEulaSubTitle() : String

    fun saveEulaUrl(url: String)
    fun getEulaUrl(): String?

    fun setSelectedBAID(data: LoginResponse.BingeSubscription)
    fun getSelectedBAID(): LoginResponse.BingeSubscription?

    fun orientationEnabled(): Boolean
    fun setOrientationEnabled(enabled: Boolean)

    fun getTAHeroBanner(): List<TaHeroBanner>?
    fun getTARelatedRail(): List<TaRelatedRail>?
    fun getProviderLogo(): ProviderLogo
    fun setBingeUpdateNotification(allow: Boolean)
    fun setBingeOffersNotification(allow: Boolean)
    fun setBingeSurveyNotification(allow: Boolean)
    fun getBingeUpdateNotification(): Boolean
    fun getBingeOffersNotification(): Boolean
    fun getBingeSurveyNotification(): Boolean
    fun saveLoginAccessToken(userAuthenticateToken: String?)
    fun saveLoginDeviceToken(deviceAuthenticateToken: String?)
    fun getLoginAccessToken(): String
    fun getLoginDeviceToken(): String
    fun removeTempToken()
    fun saveSubscribedPack(pack: PartnerPacks?,subscriptionAnalytics: SubscriptionAnalytics)
    fun getSubscribedPack(): PartnerPacks?
    fun savePrimePackDetails(primePack: PrimePack?)
    fun getPrimePackDetails(): PrimePack?
    fun setPackSelectionJourneyCompleted()

    fun getPartnerIdsList(): Set<String>?
    fun isActivePack(): Boolean
    fun getDthStatus(): String
    fun getDTHSubStatus(): String?
    fun saveDTHAccountStatus(status: String)
    fun saveDTHAccountSubStatus(status: String)
    fun saveFirestickTaken(isTaken: Boolean)
    fun isFirestickTaken(): Boolean
    fun isFirestickDialogShown(): Boolean
    fun setFirestickDialogShown(isShown: Boolean)
    fun setFirestickDialogVisibilityType(ftvDialogVisibilityType: String?)
    fun setFirestickDialogTimeFrequency(ftvDialogTimeFrequency: Int)
    fun setFirestickDialogLaunchFrequency(ftvDialogLaunchFrequency: Int)
    fun getFirestickDialogVisibilityType(): String?
    fun getFirestickDialogTimeFrequency(): Int
    fun getFirestickDialogLaunchFrequency(): Int
    fun setFirestickDialogLastVisibleTime(lastVisibleTime: Long)
    fun setAppLaunchValue(lastLaunchValue: Int)
    fun getFirestickDialogLastVisibleTime(): Long
    fun getAppLaunchValue(): Int
    fun setAppLaunchValueGuest(count : Int)
    fun getAppLaunchValueGuest(): Int
    fun setAppLaunchValueLoggedIn(count : Int)
    fun getAppLaunchValueLoggedIn(): Int
    fun getStartLaunchCount(): Boolean
    fun setStartLaunchCount(b: Boolean)
    fun setFirestickDialogFirstVisbileTime(firstVisibleTime: Long)
    fun getFirestickDialogFirstVisbileTime():Long
    fun getLoginResponse(): LoginResponse.BingeSubscription?

    fun lastDunningRechargeShownTime(): Long
    fun setLastDunningRechargeShownTime(time: Long)
    fun isLoggedInWithPassword(): Boolean
    fun setLoggedInWithPassword(t: Boolean)

    fun setMaxRechargeAmount(amount: Int)
    fun setPasswordRedirectionTime(time: Int)

    fun getMaxRechargeAmount(): Int
    fun getPasswordRedirectionTime(): Int
    fun getPrefLanguages(): List<String>
    fun setPrefLanguage(languageList: List<String>)

    fun getPrefGenres(): List<String>
    fun setPrefGenre(list: List<String>?)
    fun getGenreAPITime(): String?
    fun saveGenreAPITime(time: String)
    fun getConnectionTimeout(): Long
    fun saveConnectionTimeout(time: Long)
    fun getLoaderDelayTime(): Long
    fun saveLoaderDelayTime(time: Long)
    fun getShowMarketingScreen(): Boolean
    fun saveShowMarketingScreen(showMarketing: Boolean)
    fun saveDsn(dsn: String)
    fun getDsn(): String

    fun saveBingeButtonsEligibility(packButton: Map<String, PackButton?>?)
    fun getBingeButtonsEligibility(): Map<String, PackButton?>

    fun saveSubscriptionType(type: String)
    fun getSubscriptionType(): String

    fun contentPlaybackAllowed() : Boolean

    fun saveContentPlaybackAllowed(allowed : Boolean)

    fun saveRMN(rmn:String)
    fun getRMN():String
    fun setClearRMN(clearRmn: String)
    fun getClearRMN(): String
    fun getPartnerUniqueId() : String
    fun savePartnerUniqueId(partnerUniqueId: String?)
    fun saveRateLimit(rateLimit:String)
    fun getRateLimit():String?
    fun getHotStarPopupCount():Int
    fun increaseHotStarPopupCount()
    fun resetHotstarPopupCount()
    fun clearHotstarPopupData()
    fun setHotstarDialogLaunchFrequency(hotstarDialogLaunchFrequency: Int)
    fun getHotstarDialogLaunchFrequency(): Int
    fun setHotstarDialogPeriodicFrequency(hotstarDialogPeriodicFrequency: Int)
    fun getHotstarDialogPeriodicFrequency(): Int
    fun setHotstarLastFinalPopupShownTime(hotstarLastFinalPopupShownTime: Long)
    fun getHotstarLastFinalPopupShownTime(): Long
    fun setHotstarPopupFirstCycleCompleted(isCompleted: Boolean)
    fun getHotstarPopupFirstCycleCompleted(): Boolean
    fun saveFreeTrialStartupNudgeData(onBannerNudge: ConfigResponse.FreeTrialStartupNudge)
    fun getFreeTrialStartupData(): ConfigResponse.FreeTrialStartupNudge?

    fun increaseGameLaunchFrequency()
    fun getLaunchFrequencyForGameNudge():Int
    fun setGameNudgeLastTimeShown(timeInMillis: Long)
    fun getGameNudgeLastTimeShown(): Long


    fun getPrimePopupCount():Int
    fun setPrimePopupShownCount(count : Int)

    fun saveMixPanelId(distinctId : String)
    fun getMixPanelId() : String?
    fun saveRefrenceId(distinctId : String)
    fun getReferenceId() : String?
    fun isLoginAgain(): Boolean
    fun setLoginAgain(b : Boolean)

    fun isPrimeRedirectionEnabled() : Boolean
    fun setPrimeRedirectionEnabled(enabled : Boolean)

    fun getPrimeRedirectionDelay() : Int
    fun setPrimeRedirectionDelay(delayInSec : Int)

    fun setPrimePopupFrequency(frequency : Int)
    fun getPrimePopupFrequency() : Int

    fun setExistingPrimeInterstitialFrequency(frequency : Int)
    fun getExistingPrimeInterstitialFrequency() : Int

    fun setPrimePopupInterval(interval : Int)
    fun getPrimePopupInterval() : Int

    fun setExistingPrimeInterstitialInterval(interval: Int)
    fun getExistingPrimeInterstitialInterval() : Int

    fun getExistingPrimeInterstitialClickCount():Int
    fun setExistingPrimeInterstitialClickCount(clickCount : Int)

    fun setLastExistingPrimeButtonClick(timeStamp : Long)
    fun getLastExistingPrimeButtonClick() : Long

    fun setLastPrimePopupShownTimeStamp(timestamp : Long)
    fun getLastPrimePopupShownTimeStamp() : Long

    fun getPartnerUniqueIdInfo(partnerName : String) : String
    fun getPartnerDetail(providerShemaroo: String, partnerUniqueIdInfo: ProviderInfo?): PartnerUniqueInfo?

    fun saveDeviceCancellationFlag(status:Boolean)
    fun getDeviceCancellationFlag():Boolean
    fun saveNumberOfBingeAccount(nosBaids: Int)
    fun getNumberOfBingeAccount():Int
    fun setLanguageWidgetVisibility(status:Boolean)
    fun getLanguageWidgetVisibility():Boolean

    /*Freemium Data*/
    fun saveAnonymousId(anonymousId : String)
    fun getAnonymousId() : String?
    fun saveGuestProfileId(ProfileId : String)
    fun getGuestProfileId() : String?
    fun saveGuestPreferredLanguages(preferredLanguages : List<Languages>)
    fun getGuestPreferredLanguages() : List<Languages>?
    fun setFirstTimeLanguagePopUpShown(firstTimeUser: Boolean)
    fun isFirstTimeLanguagePopUpShown(): Boolean
    fun setParentalControlEnabled(enabled: Boolean)
    fun isParentalControlEnabled(): Boolean
    fun setParentalPinExists(parentalPinExistsValue: Boolean)
    fun isParentalPinExists(): Boolean
    fun saveDTHStatusFreemium(status: String)
    fun getDthStatusFreemium(): String
    fun setParentalRating(parentalRating: AgeRatingsResponse.AgeRatings)
    fun getParentalRating(): AgeRatingsResponse.AgeRatings?
    fun setFetchedProfileData(jsonData: String)
    fun getFetchedProfileData() : String?
    fun setFetchedBalanceData(jsonData: String)
    fun getFetchedBalanceData() : String?
    fun saveBingeSid(bingeSubscriberId: String?)
    fun getBingeSid() : String?
    fun setInterruptCategoryTabStatus(status:Boolean)
    fun getInterruptCategoryTabStatus(): Boolean
    fun saveLoginTimeStamp(timeUtils : Long)
    fun getLoginTimeStamp() : Long
    fun saveNudgeTimeUpdateEmail(timeMillis: Long)
    fun saveNudgeTimeShowNotification(timeMills: Long)
    fun saveNudgeTimeNeverMissPlanRenewal(timeMills: Long)
    fun saveNudgeTimeRenewalBeforeExpiry(timeMills: Long)
    fun saveNudgeTimeRenewalAfterExpiry(timeMills: Long)
    fun getNudgeTimeUpdateEmail(): Long
    fun getNudgeTimeShowNotification(): Long
    fun getNudgeTimeNeverMissPlanRenewal(): Long
    fun getNudgeTimeRenewalBeforeExpiry(): Long
    fun getNudgeTimeRenewalAfterExpiry(): Long
    fun tempSaveSid(sId: String, dthStatus: String)
    fun getTempSavedSid(): String?
    fun getTempDthStatus(): String?
    fun saveGAuthToken(it: String)
    fun getGAuthToken() : String?

    fun setAppLaunchCount(launchCount:Int)
    fun getAppLaunchCount():Int

    fun getCurrentStartingPosition():Int
    fun setCurrentStartingPosition(x:Int)

    fun setLogoutCalled(b: Boolean)
    fun isLogoutCalled(): Boolean

    fun saveAddModifyResponse(jsonData: String)
    fun getAddModifyResponse(): AddPackResponse?
    fun getPreviousSubscribedPack(): PartnerPacks?
    fun savePreviousSubscribedPack(pack: PartnerPacks?)

    fun saveFirstInvention()
    fun saveFirstContentClick()
    fun saveFirstFreeContentPlay()
    fun saveFirstPremiumContentPlay()

    fun getFirstInvention() : Boolean
    fun getFirstContentClick() : Boolean
    fun getFirstFreeContentPlay() : Boolean
    fun getFirstPremiumContentPlay() : Boolean

    fun resetHeroBannerCounts()
    fun setFirstTimeLandingOpen(b: Boolean)
    fun isFirstTimeLandingOpen(): Boolean
    fun getSubscriptionDrawerLastSeen(): Long
    fun setSubscriptionDrawerLastSeen(currentTime: Long)
    fun saveUpdateInPackStatus(b: Boolean)
    fun isUpdateInPackStatus() : Boolean

    fun getPaymentPendingStatus() : Boolean
    fun setPaymentPendingStatus(paymentPendingStatus: Boolean)
    fun removeSubscribedPack()
//    fun getFaqUrl(): String?
//    fun setFaqUrl(url : String)
    fun getFaqData(): HelpCenterResponse.Data?
    fun setFaqData(url : HelpCenterResponse.Data)

    fun setFirstAppLaunchTimeInUTC(currentTimeInUTC: String)
    fun getFirstAppLaunchTimeInUTC(): String?
    /*App rating prefs start*/
    fun saveNumberOfContentPlaybackForAppRating(value: Int) /*Note: Don't use for any other usage, It will be reset as per App rating conditions*/
    fun getNumberOfContentPlaybackForAppRating(): Int /*Note: Don't use for any other usage, It will be reset as per App rating conditions*/
    fun setIsEligibleForAppRating(value: Boolean)
    fun isEligibleForAppRating(): Boolean
    fun getNextGameNudgeTime(): Long
    fun setNextGameNudgeTime(time:Long)
    /*App rating prefs end*/

    fun setIsEligibleForFreeTrial(value: Boolean)
    fun getEligibleForFreeTrial(): Boolean

    fun getGameNudgeOpenCount(): Int
    fun setGameNudgeOpenCount(count:Int)

    fun getLastGameNudgeTime(): Long
    fun setLastGameNudgeTime(time:Long)


    fun getGameAnimOpenCount(): Int
    fun setGameAnimOpenCount(count:Int)

    fun getLastGameAnimTime(): Long
    fun setLastGameAnimTime(time:Long)
    fun resetFSDialog()
    fun saveSilentLoginTimestamp(time:String)
    fun getSilentLoginTimestamp(): String?

    //To check wehter the game nudge is shown in the app session
    fun getGameNudgeShown() : Boolean
    fun setGameNudgeShown(b: Boolean)

    fun setSilentLoginCalled(b: Boolean)
    fun isSilentLoginCalled(): Boolean
    fun setManagedAppResponse(configAppVersion: ManagedAppResponse.Data)
    fun getManagedAppResponse(): ManagedAppResponse.Data?

    fun setMxUpsellClosed(b: Boolean)
    fun getMxUpsellClosed() : Boolean

    fun saveLoggedInAppLaunchCountForRegionalAppNudge(launchCount: Int)
    fun getLoggedInAppLaunchCountForRegionalAppNudge(): Int

    fun saveLastPgSdkProcessStatus(status: String?)
    fun getLastPgSdkProcessStatus(): String?
    fun saveAvailableProviders(map: HashMap<String, ConfigResponse.AvailableProviders>)
    fun getAllowedProviderList() : List<String>?
    fun getAllowedProviderInfo() : HashMap<String, ConfigResponse.AvailableProviders>?
    fun saveAllowedProviders(list: ArrayList<String>)
    fun setManagedAppEnabled(b: Boolean)
    fun isManagedAppEnabled() : Boolean

    fun isGameHapticFeedbackEnabled(): Boolean
    fun enableGameHapticFeedback(status: Boolean)
    fun saveGenericAppLaunchCount(count: Int)
    fun getGenericAppLaunchCount(): Int
    fun saveCoachMarkLaunchFrequency(count: Int?)
    fun getCoachMarkLaunchFrequency(): Int
    fun enableHomeScreenSearchCoachMark(status: Boolean)
    fun isHomeScreenSearchCoachMarkEnabled(): Boolean
    fun enableSearchScreenMicCoachMark(status: Boolean)
    fun isSearchScreenMicCoachMarkEnabled(): Boolean

    fun removeHierarchyData()
    fun saveHierarchyData(key: String, hierarchyResponse: HierarchyResponse)
    fun getHierarchyData(pageNameDrp : String) : HierarchyResponse?
    fun saveNotificationClickedId(ctId:String)
    fun isNotificationClickedIdExit(ctId: String):Boolean

    fun setWelcomeDialogStatus(status: Boolean)
    fun getWelcomeDialogStatus(): Boolean

}