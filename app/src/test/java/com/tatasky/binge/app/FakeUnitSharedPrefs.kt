package com.tatasky.binge.app

import androidx.lifecycle.LiveData
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.Languages
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import java.util.ArrayList
import java.util.HashMap

class FakeUnitSharedPrefs : PrefsRepo {
    override fun getCloudenieryUrl(): String? = ""

    override fun saveCloudenieryUrl(url: String?) {}

    override fun isConfigured(): Boolean = true

    override fun setConfigured(b: Boolean) {}

    override fun getLoginStatus(): Boolean = false

    override fun getProfileId(): String? = ""

    override fun setProfileId(pId: String) {}

    override fun getBaId(): String = ""

    override fun setBaId(baId: String) {}

    override fun getOriginalSubscriberId(): String = ""

    override fun setOriginalSubscriberId(sId: String) {}

    override fun getAccessToken(): String? = null

    override fun setAccessToken(token: String) {}

    override fun getDeviceToken(): String? = null

    override fun setDeviceToken(token: String) {}

    override fun saveAccountDetails(accountJson: String) {}

    override fun getAccountDetails(): String? = ""

    override fun logoutUser() {}

    override fun getEntitlements(): Set<String>? = null

    override fun getMOEUserTracked(): Boolean = false

    override fun setMOEUserTracked(isTracked: Boolean) {}

    override fun getRrmSessionInfo(): RrmSessionInfo? = null

    override fun setConfigAppVersion(androidVersion: ConfigResponse.Android) {}

    override fun getConfigAppVersion(): ConfigResponse.Android? = null

    override fun setTrailerSound(soundOn: Boolean) {}

    override fun setAutoPlayTrailerOn(autoPlay: Boolean) {}

    override fun getTrailerSound(): Boolean = false

    override fun getAutoPlayTrailerOn(): Boolean = false

    override fun saveSearchKeyword(keyWord: String) {}

    override fun clearAllSearchKeyword() {}
    override fun clearRecentSearchItem(keyWord: String) {

    }

    override fun getSearchKeywords(): List<String> = emptyList()

    override fun setAllowTransactionalNotification(allow: Boolean) {}

    override fun setAllowWatchNotification(allow: Boolean) {}

    override fun setAllowOffersNotification(allow: Boolean) {}

    override fun getTransactionalNotificationAllowed(): Boolean = false

    override fun getWatchNotificationAllowed(): Boolean = false

    override fun getOffersNotificationAllowed(): Boolean = false

    override fun clear() {}

    override fun clearSwitchAccountInfo() {}

    override fun saveOtpResentCount(allow: Int) {}

    override fun saveOtpDuration(allow: Int) {}

    override fun getOtpResentCount(): Int = 3

    override fun getOtpDuration(): Int = 30

    override fun getUserDetails(): LoginResponse.BingeSubscription? = null

    override fun setPasswordCreated(isPassword: Boolean) {}

    override fun isPasswordCreated(): Boolean = false

    override fun setPDFDownloaded(data: String) {}

    override fun getPDFDownloaded(): Set<String>? = null

    override fun addConfigResponse(configResponse: ConfigResponse) {

    }

    override fun getConfigResponse(): ConfigResponse? = null

    override fun setEntitlements(listOfIds: Set<String>) {

    }

    override fun setSelectedProfile(data: LoginResponse.BingeSubscription) {

    }

    override fun getSelectedProfile(): LoginResponse.BingeSubscription? = null

    override fun savePrivacyPolicyUrl(url: String) {

    }

    override fun getPrivacyPolicyUrl(): String? = null

    override fun saveTnCUrl(url: String) {

    }

    override fun getTnCUrl(): String? = null

    override fun saveEulaTitle(title: String) {

    }

    override fun getEulaTitle(): String? = null

    override fun saveEulaSubTitle(subtitle: String) {

    }

    override fun getEulaSubTitle(): String = ""

    override fun saveEulaUrl(url: String) {

    }

    override fun getEulaUrl(): String? = null

    override fun setSelectedBAID(data: LoginResponse.BingeSubscription) {

    }

    override fun getSelectedBAID(): LoginResponse.BingeSubscription? = null

    override fun orientationEnabled(): Boolean = false

    override fun setOrientationEnabled(enabled: Boolean) {

    }

    override fun getTAHeroBanner(): List<TaHeroBanner>? = null

    override fun getTARelatedRail(): List<TaRelatedRail>? = null

    override fun getProviderLogo(): ProviderLogo = ProviderLogo()

    override fun setBingeUpdateNotification(allow: Boolean) {

    }

    override fun setBingeOffersNotification(allow: Boolean) {

    }

    override fun setBingeSurveyNotification(allow: Boolean) {

    }

    override fun getBingeUpdateNotification(): Boolean = false

    override fun getBingeOffersNotification(): Boolean = false

    override fun getBingeSurveyNotification(): Boolean = false

    override fun saveLoginAccessToken(userAuthenticateToken: String?) {

    }

    override fun saveLoginDeviceToken(deviceAuthenticateToken: String?) {

    }

    override fun getLoginAccessToken(): String = ""

    override fun getLoginDeviceToken(): String = ""

    override fun removeTempToken() {

    }

    override fun saveSubscribedPack(pack: PartnerPacks?,subscriptionAnalytics: SubscriptionAnalytics) {

    }

    override fun getSubscribedPack(): PartnerPacks? = null
    override fun savePrimePackDetails(primePack: PrimePack?) {

    }

    override fun getPrimePackDetails(): PrimePack? {
        return null
    }

    override fun setPackSelectionJourneyCompleted() {

    }

    override fun getPartnerIdsList(): Set<String>? = null

    override fun isActivePack(): Boolean = false

    override fun getDthStatus(): String = ""

    override fun getDTHSubStatus(): String? = null

    override fun saveDTHAccountStatus(status: String) {

    }

    override fun saveDTHAccountSubStatus(status: String) {

    }

    override fun saveFirestickTaken(isTaken: Boolean) {

    }

    override fun isFirestickTaken(): Boolean = false

    override fun isFirestickDialogShown(): Boolean = false

    override fun setFirestickDialogShown(isShown: Boolean) {

    }

    override fun setFirestickDialogVisibilityType(ftvDialogVisibilityType: String?) {

    }

    override fun setFirestickDialogTimeFrequency(ftvDialogTimeFrequency: Int) {

    }

    override fun setFirestickDialogLaunchFrequency(ftvDialogLaunchFrequency: Int) {

    }

    override fun getFirestickDialogVisibilityType(): String? = null

    override fun getFirestickDialogTimeFrequency(): Int = 0

    override fun getFirestickDialogLaunchFrequency(): Int = 0

    override fun setFirestickDialogLastVisibleTime(lastVisibleTime: Long) {

    }

    override fun setAppLaunchValue(lastLaunchValue: Int) {

    }

    override fun getFirestickDialogLastVisibleTime(): Long = 0L

    override fun getAppLaunchValue(): Int = 0
    override fun setAppLaunchValueGuest(count: Int) {

    }

    override fun getAppLaunchValueGuest(): Int {
        return 0
    }

    override fun setAppLaunchValueLoggedIn(count: Int) {

    }

    override fun getAppLaunchValueLoggedIn(): Int {
        return 0
    }

    override fun getStartLaunchCount(): Boolean {
        return true
    }

    override fun setStartLaunchCount(b: Boolean) {

    }

    override fun setFirestickDialogFirstVisbileTime(firstVisibleTime: Long) {

    }

    override fun getFirestickDialogFirstVisbileTime(): Long = 0L

    override fun getLoginResponse(): LoginResponse.BingeSubscription? = null

    override fun lastDunningRechargeShownTime(): Long = 0L

    override fun setLastDunningRechargeShownTime(time: Long) {

    }

    override fun isLoggedInWithPassword(): Boolean = false

    override fun setLoggedInWithPassword(t: Boolean) {

    }

    override fun setMaxRechargeAmount(amount: Int) {

    }

    override fun setPasswordRedirectionTime(time: Int) {

    }

    override fun getMaxRechargeAmount(): Int = 0

    override fun getPasswordRedirectionTime(): Int = 0

    override fun getPrefLanguages(): List<String> = listOf()

    override fun setPrefLanguage(list: List<String>) {

    }

    override fun getPrefGenres(): List<String> = listOf()

    override fun setPrefGenre(list: List<String>?) {

    }

    override fun getGenreAPITime(): String? = null

    override fun saveGenreAPITime(time: String) {

    }

    override fun getConnectionTimeout(): Long = 0L

    override fun saveConnectionTimeout(time: Long) {

    }

    override fun getLoaderDelayTime(): Long = 0L

    override fun saveLoaderDelayTime(time: Long) {

    }

    override fun getShowMarketingScreen(): Boolean = false

    override fun saveShowMarketingScreen(showMarketing: Boolean) {

    }

    override fun saveDsn(dsn: String) {

    }

    override fun getDsn(): String = ""

    override fun saveBingeButtonsEligibility(packButton: Map<String, PackButton?>?) {

    }

    override fun getBingeButtonsEligibility(): Map<String, PackButton?> = mapOf()

    override fun saveSubscriptionType(type: String) {

    }

    override fun getSubscriptionType(): String = ""

    override fun contentPlaybackAllowed(): Boolean = false

    override fun saveContentPlaybackAllowed(allowed: Boolean) {

    }

    override fun saveRMN(rmn: String) {

    }

    override fun getRMN(): String = ""

    override fun setClearRMN(clearRmn: String) {

    }

    override fun getClearRMN(): String = ""

    override fun getPartnerUniqueId(): String = ""

    override fun savePartnerUniqueId(partnerUniqueId: String?) {

    }

    override fun saveRateLimit(rateLimit: String) {

    }

    override fun getRateLimit(): String? = null

    override fun getHotStarPopupCount(): Int = 0

    override fun increaseHotStarPopupCount() {

    }

    override fun resetHotstarPopupCount() {

    }

    override fun clearHotstarPopupData() {

    }

    override fun setHotstarDialogLaunchFrequency(hotstarDialogLaunchFrequency: Int) {

    }

    override fun getHotstarDialogLaunchFrequency(): Int = 0

    override fun setHotstarDialogPeriodicFrequency(hotstarDialogPeriodicFrequency: Int) {

    }

    override fun getHotstarDialogPeriodicFrequency(): Int = 0

    override fun setHotstarLastFinalPopupShownTime(hotstarLastFinalPopupShownTime: Long) {

    }

    override fun getHotstarLastFinalPopupShownTime(): Long = 0L

    override fun setHotstarPopupFirstCycleCompleted(isCompleted: Boolean) {

    }

    override fun getHotstarPopupFirstCycleCompleted(): Boolean = false

    override fun saveFreeTrialStartupNudgeData(onBannerNudge: ConfigResponse.FreeTrialStartupNudge) {

    }

    override fun getFreeTrialStartupData(): ConfigResponse.FreeTrialStartupNudge? = null
    override fun increaseGameLaunchFrequency() {

    }

    override fun getLaunchFrequencyForGameNudge(): Int {
        return 0
    }

    override fun setGameNudgeLastTimeShown(timeInMillis: Long) {

    }

    override fun getGameNudgeLastTimeShown(): Long {
        return 0L
    }

    override fun getPrimePopupCount(): Int = 0

    override fun setPrimePopupShownCount(count: Int) {

    }

    override fun saveMixPanelId(distinctId: String) {

    }

    override fun getMixPanelId(): String? = null
    override fun saveRefrenceId(distinctId: String) {

    }

    override fun getReferenceId(): String? {
        return null
    }

    override fun isLoginAgain(): Boolean = false

    override fun setLoginAgain(b: Boolean) {

    }

    override fun isPrimeRedirectionEnabled(): Boolean = false

    override fun setPrimeRedirectionEnabled(enabled: Boolean) {

    }

    override fun getPrimeRedirectionDelay(): Int = 0

    override fun setPrimeRedirectionDelay(delayInSec: Int) {

    }

    override fun setPrimePopupFrequency(frequency: Int) {

    }

    override fun getPrimePopupFrequency(): Int = 10

    override fun setExistingPrimeInterstitialFrequency(frequency: Int) {

    }

    override fun getExistingPrimeInterstitialFrequency(): Int = 0

    override fun setPrimePopupInterval(interval: Int) {

    }

    override fun getPrimePopupInterval(): Int = 0

    override fun setExistingPrimeInterstitialInterval(interval: Int) {

    }

    override fun getExistingPrimeInterstitialInterval(): Int = 0

    override fun getExistingPrimeInterstitialClickCount(): Int = 0

    override fun setExistingPrimeInterstitialClickCount(clickCount: Int) {

    }

    override fun setLastExistingPrimeButtonClick(timeStamp: Long) {

    }

    override fun getLastExistingPrimeButtonClick(): Long = 0L

    override fun setLastPrimePopupShownTimeStamp(timestamp: Long) {

    }

    override fun getLastPrimePopupShownTimeStamp(): Long = 0L

    override fun getPartnerUniqueIdInfo(partnerName: String): String = ""

    override fun getPartnerDetail(
        providerShemaroo: String,
        partnerUniqueIdInfo: ProviderInfo?
    ): PartnerUniqueInfo? = null

    override fun saveDeviceCancellationFlag(status: Boolean) {}

    override fun getDeviceCancellationFlag(): Boolean = false
    override fun saveNumberOfBingeAccount(nosBaids: Int) {
    }

    override fun getNumberOfBingeAccount(): Int = 0
    override fun setLanguageWidgetVisibility(status: Boolean) {

    }

    override fun getLanguageWidgetVisibility(): Boolean {
        return true
    }

    override fun saveAnonymousId(anonymousId: String) {

    }

    override fun getAnonymousId(): String? {
        return null
    }

    override fun saveGuestProfileId(ProfileId: String) {

    }

    override fun getGuestProfileId(): String? {
        return null
    }

    override fun saveGuestPreferredLanguages(preferredLanguages: List<Languages>) {

    }

    override fun getGuestPreferredLanguages(): List<Languages>? {
        return null
    }

    override fun setFirstTimeLanguagePopUpShown(firstTimeUser: Boolean) {

    }

    override fun isFirstTimeLanguagePopUpShown(): Boolean {
        return true
    }

    override fun setParentalControlEnabled(enabled: Boolean) {

    }

    override fun isParentalControlEnabled(): Boolean {
        return true
    }

    override fun setParentalPinExists(parentalPinExistsValue: Boolean) {

    }

    override fun isParentalPinExists(): Boolean {
        return true
    }

    override fun saveDTHStatusFreemium(status: String) {

    }

    override fun getDthStatusFreemium(): String {
        return ""
    }

    override fun setParentalRating(parentalRating: AgeRatingsResponse.AgeRatings) {

    }

    override fun getParentalRating(): AgeRatingsResponse.AgeRatings? {
        return null
    }

    override fun setFetchedProfileData(jsonData: String) {

    }

    override fun getFetchedProfileData(): String? {
        return null
    }

    override fun setFetchedBalanceData(jsonData: String) {

    }

    override fun getFetchedBalanceData(): String? {
        return null
    }

    override fun saveBingeSid(bingeSubscriberId: String?) {

    }

    override fun getBingeSid(): String? {
        return null
    }

    override fun setInterruptCategoryTabStatus(status: Boolean) {

    }

    override fun getInterruptCategoryTabStatus(): Boolean {
        return true
    }

    override fun saveLoginTimeStamp(timeUtils: Long) {

    }

    override fun getLoginTimeStamp(): Long {
        return 0L
    }

    override fun saveNudgeTimeUpdateEmail(timeMillis: Long) {

    }

    override fun saveNudgeTimeShowNotification(timeMills: Long) {

    }

    override fun saveNudgeTimeNeverMissPlanRenewal(timeMills: Long) {

    }

    override fun saveNudgeTimeRenewalBeforeExpiry(timeMills: Long) {

    }

    override fun saveNudgeTimeRenewalAfterExpiry(timeMills: Long) {

    }

    override fun getNudgeTimeUpdateEmail(): Long {
        return 0L
    }

    override fun getNudgeTimeShowNotification(): Long {
        return 0L
    }

    override fun getNudgeTimeNeverMissPlanRenewal(): Long {
        return 0L
    }

    override fun getNudgeTimeRenewalBeforeExpiry(): Long {
        return 0L
    }

    override fun getNudgeTimeRenewalAfterExpiry(): Long {
        return 0L
    }

    override fun tempSaveSid(sId: String, dthStatus: String) {

    }

    override fun getTempSavedSid(): String? {
        return null
    }

    override fun getTempDthStatus(): String? {
        return null
    }

    override fun saveGAuthToken(it: String) {

    }

    override fun getGAuthToken(): String? {
        return null
    }

    override fun setAppLaunchCount(launchCount: Int) {

    }

    override fun getAppLaunchCount(): Int {
        return 0
    }

    override fun getCurrentStartingPosition(): Int {
        return 0
    }

    override fun setCurrentStartingPosition(x: Int) {

    }

    override fun setLogoutCalled(b: Boolean) {

    }

    override fun isLogoutCalled(): Boolean {
        return true
    }

    override fun saveAddModifyResponse(jsonData: String) {

    }

    override fun getAddModifyResponse(): AddPackResponse? {
        return null
    }

    override fun getPreviousSubscribedPack(): PartnerPacks? {
        return null
    }

    override fun savePreviousSubscribedPack(pack: PartnerPacks?) {

    }

    override fun saveFirstInvention() {

    }

    override fun saveFirstContentClick() {

    }

    override fun saveFirstFreeContentPlay() {

    }

    override fun saveFirstPremiumContentPlay() {

    }

    override fun getFirstInvention(): Boolean {
        return true
    }

    override fun getFirstContentClick(): Boolean {
        return true
    }

    override fun getFirstFreeContentPlay(): Boolean {
        return true
    }

    override fun getFirstPremiumContentPlay(): Boolean {
        return true
    }

    override fun resetHeroBannerCounts() {

    }

    override fun setFirstTimeLandingOpen(b: Boolean) {

    }

    override fun isFirstTimeLandingOpen(): Boolean {
        return true
    }

    override fun getSubscriptionDrawerLastSeen(): Long {
        return 0L
    }

    override fun setSubscriptionDrawerLastSeen(currentTime: Long) {

    }

    override fun saveUpdateInPackStatus(b: Boolean) {

    }

    override fun isUpdateInPackStatus(): Boolean {
        return true
    }

    override fun getPaymentPendingStatus(): Boolean {
        return true
    }

    override fun setPaymentPendingStatus(paymentPendingStatus: Boolean) {

    }

    override fun removeSubscribedPack() {

    }

    override fun getFaqData(): HelpCenterResponse.Data? {
        return null
    }

    override fun setFaqData(url: HelpCenterResponse.Data) {

    }

    override fun setFirstAppLaunchTimeInUTC(currentTimeInUTC: String) {

    }

    override fun getFirstAppLaunchTimeInUTC(): String? {
        return ""
    }

    override fun saveNumberOfContentPlaybackForAppRating(value: Int) {

    }

    override fun getNumberOfContentPlaybackForAppRating(): Int {
        return 0
    }

    override fun setIsEligibleForAppRating(value: Boolean) {

    }

    override fun isEligibleForAppRating(): Boolean {
        return true
    }

    override fun getNextGameNudgeTime(): Long {
        return 0L
    }

    override fun setNextGameNudgeTime(time: Long) {

    }

    override fun setIsEligibleForFreeTrial(value: Boolean) {

    }

    override fun getEligibleForFreeTrial(): Boolean {
        return true
    }

    override fun getGameNudgeOpenCount(): Int {
        return 0
    }

    override fun setGameNudgeOpenCount(count: Int) {

    }

    override fun getLastGameNudgeTime(): Long {
        return 0L
    }

    override fun setLastGameNudgeTime(time: Long) {

    }

    override fun getGameAnimOpenCount(): Int {
        return 0
    }

    override fun setGameAnimOpenCount(count: Int) {

    }

    override fun getLastGameAnimTime(): Long {
        return 0L
    }

    override fun setLastGameAnimTime(time: Long) {

    }

    override fun resetFSDialog() {

    }

    override fun saveSilentLoginTimestamp(time: String) {

    }

    override fun getSilentLoginTimestamp(): String? {
        return ""
    }

    override fun getGameNudgeShown(): Boolean {
        return true
    }

    override fun setGameNudgeShown(b: Boolean) {

    }

    override fun setSilentLoginCalled(b: Boolean) {

    }

    override fun isSilentLoginCalled(): Boolean {
        return true
    }

    override fun setManagedAppResponse(configAppVersion: ManagedAppResponse.Data) {
    }

    override fun getManagedAppResponse(): ManagedAppResponse.Data? {
        return null
    }

    override fun setMxUpsellClosed(b: Boolean) {
    }

    override fun getMxUpsellClosed(): Boolean {
        return true
    }

    override fun saveLoggedInAppLaunchCountForRegionalAppNudge(launchCount: Int) {
    }

    override fun getLoggedInAppLaunchCountForRegionalAppNudge(): Int {
        return 0
    }

    override fun saveLastPgSdkProcessStatus(status: String?) {
    }

    override fun getLastPgSdkProcessStatus(): String? {
        return ""
    }

    override fun saveAvailableProviders(map: HashMap<String, ConfigResponse.AvailableProviders>) {
    }

    override fun getAllowedProviderList(): List<String>? {
        return null
    }

    override fun getAllowedProviderInfo(): HashMap<String, ConfigResponse.AvailableProviders>? {
        return null
    }

    override fun saveAllowedProviders(list: ArrayList<String>) {
    }

    override fun setManagedAppEnabled(b: Boolean) {
    }

    override fun isManagedAppEnabled(): Boolean {
        return true
    }

    override fun removeHierarchyData() {
        TODO("Not yet implemented")
    }

    override fun saveHierarchyData(key: String, hierarchyResponse: HierarchyResponse) {
        TODO("Not yet implemented")
    }

    override fun getHierarchyData(pageNameDrp: String): HierarchyResponse? {
        TODO("Not yet implemented")
    }
}