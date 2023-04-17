package com.tatasky.binge.domain.repositories

import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.epicon.PartnerContentAnalyticsRequest
import com.tatasky.binge.hoichoi.HoichoiPlayebackResponse
import com.tatasky.binge.hoichoi.HoichoiRequest
import com.tatasky.binge.shemaroo.helper.ShemarooAnalyticsBody
import com.tatasky.binge.data.networking.models.response.ChaupalUrlResponse
import com.tatasky.binge.epicon.PlanetMarathiAnalyticsRequest
import com.tatasky.binge.lionsgatehelper.LionsgateAnalyticsBody
import com.tatasky.binge.shemaroo.modal.ShemarooSafeUrlResponse
import com.tatasky.binge.ui.features.home.bottomsheet.select_language.models.SaveLanguageBody
import com.tatasky.binge.ui.features.recharge.JuspayInitiationResponse
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.UserPreferredLanguage
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.preferredLanguageBody
import com.tatasky.binge.voot.model.VootPlayebackResponse
import com.tatasky.binge.voot.model.VootRequest
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody

interface CommonRepository {
    fun getHomePage(request: HomeRequest): Single<HomeResponse>
    fun getConfig(): Single<ConfigResponse>
    fun getLeftMenu(): Single<LeftMenuResponse>
    fun getTARails(request: TARequest): Single<RecommendationResponse>
    fun getRecommendation(parameter: DetailRequest): Single<RecommendationResponse>

    fun getSeriesList(parameter: SeriesRequest): Single<SeriesListResponse>
    fun getBrandDetails(request: DetailRequest): Single<DetailsResponse>

    fun toggleFavourite(toggleFavouriteRequest: ToggleFavouriteRequest): Single<IsFavouriteResponse>
    fun getFavouritesList(request: WatchRequest): Single<RecommendationResponse>

    fun getCWRails(request: CWRequest): Single<RecommendationResponse>
    fun getRailData(railId: String, pageLimit: Int, offset: Int): Single<RecommendationResponse>
    fun getSearchFilters(intent: String): Single<RecommendationResponse>
    fun getSearchRails(intent:String?) : Single<HomeResponse>
    fun getEpisodeSearchResponse(episodeSearchRequest: EpisodeSearchRequest) : Single<SeriesListResponse>

    fun loginViaOtpUser(loginRequest: LoginDTO): Single<ValidateOTPResponse>
    fun loginWithPassword(loginRequest: LoginDTO): Single<ValidateOTPResponse>
    fun generateOTPWithRmn(rmn: String,isPassword: Boolean): Single<GetOtpResponse>
    fun generateOTPWithSid(sid: String,isPassword: Boolean): Single<GetOtpResponse>
    fun generateOtpGuestLogin(mobileNumber: String): Single<GetOtpGuestLoginResponse>
    fun validateOtpGuestLogin(validateOtpGuestLoginRequest: ValidateOtpGuestLoginRequest): Single<ValidateOTPResponse>
    fun getPreviouslyUsedMobileNumbers(): Single<PreviouslyUsedMobileNumbersResponse>

    fun changePassword(
        sid: String,
        changePasswordRequest: ChangePasswordRequest
    ): Single<ForgetPasswordResponse>

    fun getSubIdList(rmn: String): Single<SubscriberIdListResponse>
    fun getBaIdList(sid: String): Single<BAIdListResponse>
//    fun loginBingeUser(loginRequest: LoginRequest): Single<NewBingeUserResponse>
    fun removeDevice(str1: String, str2: String): Single<BaseResponse>
    fun getDeviceList(baId: String): Single<DeviceListResponse>

    fun downloadPDF(url: String): Single<ResponseBody>

    fun getCWAction(cwRequest: CWRequest): Single<BaseResponse>
    fun logout(baid: String): Single<BaseResponse>
    fun getShemarooUrlData(url: String): Single<ShemarooSafeUrlResponse>
    fun getProfileDetailResponse(request: FetchProfileRequest): Single<SubscriberProfileListModel>
    fun editAliasName(baId: String, aliasName: String): Single<BaseResponse>
    fun updateProfileImage(image: MultipartBody.Part, baId: String, profileId: String): Single<ImageUploadResponse>
    fun fetchNextAndPreviousEpisodeDetails(episodeRequest: EpisodeRequest): Single<NextPreviousEpisodeResponse>

    fun getTvodContent(sId: String, offset: Int, max: Int): Single<RecommendationResponse>
    fun getSearchV2(searchRequest: SearchRequest): Single<RecommendationResponse>

    fun removeProfileImage(sid:String, profileId: String): Single<ImageUploadResponse>
//    fun createNewBingeUser(newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse>
    fun createNewBingeMobileUser(newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse>
    fun updateEmail(updateEmailRequest: UpdateEmailRequest): Single<BaseResponse>
    fun updateEmailAndName(updateEmailRequest: UpdateEmailRequest, calledFrom: String): Single<BaseResponse>
    fun requestForgetPassword(sid: String, forgetPasswordRequest : ForgotPasswordRequest?): Single<BaseResponse>
    fun getPackList(partnerId : String?, baId: String): Single<PackListResponse>
    fun fetchEligiblePackList(baId: String,subscriptionType:String): Single<EligiblePackResponse>
    fun fetchEligiblepackForNonLoggedIn(): Single<EligiblePackResponse>
    fun requestSubscriptionCreation(subscriptionCreationRequest: SubscriptionCreationRequest): Single<PurchasePackResponse>
    fun requestSubscriptionModification(revoked: Boolean, subscriptionModificationRequest: SubscriptionModificationRequest): Single<PurchasePackResponse>
    fun fetchBalance(walletBalanceRequest: WalletBalanceRequest): Single<WalletBalanceResponse>
    fun refreshAccount(baId: String, dthStatus: String): Single<BaseResponse>
    fun getCurrentPack(sid: String, baId: String): Single<PurchasePackResponse>
    fun getFreemiumCurrentPack(
        baId: String,
        accountId: String,
        freemiumUserType: String,
        userIsOnTickTick: Boolean?
    ): Single<PurchasePackResponse>

    fun requestSubscriptionCancellation(subscriptionCreationRequest: SubscriptionCancellationRequest): Single<CancellationResponse>
    fun freemiumRequestSubscriptionCancellation(subscriptionCreationRequest: FreemiumSubscriptionCancellationRequest): Single<FreemiumCancellationResponse>

    fun requestSubscriptionRevokeCancellation(baId: String): Single<CancellationResponse>
    fun getFaq(): Single<FaqResponse>
    fun getHelpCenterURL(): Single<HelpCenterResponse>
    fun fetchMarketingResponse(): Single<MarketingResponse>
    fun callFavouriteLearnAction(
        contentType: String,
        id: String,
        showType: String,
        provider: String,
        refUsecase : String
    ): Single<BaseResponse>

    fun callLearnAction(
        contentType: String,
        id: String,
        showType: String,
        provider: String,
        isLoggedIn:Boolean,
        learnActionType: String,
        refUsecase: String
    ): Single<BaseResponse>

    fun callWorkOrderFS(baId: String, workOrderRequest: WorkOrderRequest): Single<BaseResponse>

    fun initiateRecharge(sid: String, amount: String): Single<RechargeResponse>
    fun initiateRecharge(sid: String): Single<RechargeResponse>

    fun rechargeNotification(sid: String, baId: String): Single<DunningResponse>
    fun getVootPlaybackData(request: VootRequest): Single<VootPlayebackResponse>

    fun getVootKidsPlaybackData(request: VootRequest): Single<VootPlayebackResponse>
    fun changeSetting(baid: String, settingType: String): Single<BaseResponse>

    fun fetchAddress(): Single<AddressResponse>

    fun fetchSlots(slotsRequest: SlotsRequest): Single<SlotsResponse>

    fun confirmSlots(slotsRequest: SlotsRequest): Single<BaseResponse>
    fun requestSubscriptionReactivation(subscriptionCreationRequest: SubscriptionCreationRequest): Single<PurchasePackResponse>
    fun getPrefLangGenre(
        isLoggedIn: Boolean,
        type: String
    ): Single<GenreListResponse>
    fun switchAccount(baId: String, dsn: String?, targetBaId: String): Single<SwitchAccountResponse>
    fun fetchZee5Tag(): Single<Zee5TagResponse>
    fun generateControlToken(controlRequest: ControlRequest): Single<ControlTokenResponse>
    fun fetchLastWatch(lastWatchRequest: ToggleFavouriteRequest, subscriptionType: PartnerPacks?): Single<IsFavouriteResponse>
    fun getTransactionHistory(transactionHistoryRequest: TransactionHistoryRequest):Single<TransactionHistoryResponse>
    fun getTransactionHistoryForNonDTHUser(baId: String):Single<TransactionHistoryResponse>
    fun fetchBoxsetDetails(vodId: String): Single<DetailsResponse>
    fun callWatchViewAction(
        contentType: String,
        id: String,
        subscriberId: String,
        profileId: String
    ): Single<BaseResponse>
    fun contentPlaybackExpiry(vodId: String): Single<PlaybackExpiryResponse>
    fun getLastWatchEpisode(request: ContentIdAndTypeRequest): Single<EpisodeListingResponse>
    fun fetchPubnubHistory(url : String): Single<ResponseBody>
    fun switchAccountAtv(baId:String):Single<SwitchAccountResponse>

    fun generateSonylivToken(): Single<ControlTokenResponse>
    fun activatePrime(primeActivationRequest: PrimeActivationRequest): Single<PrimeActivationResponse>
    fun fetchPrimePackList(): Single<PrimePackListResponse>
	fun fetchInterstitialPageResponse(): Single<PrimeInterstitialResponse>
	fun requestPrimeResume() : Single<BaseResponse>
	fun createBingeAccount(sid:String) : Single<NewBingeUserResponse>

    fun shemarooMeAnalytics(body: ShemarooAnalyticsBody): Single<BaseResponse>
    fun lionsgateAnalytics(body: LionsgateAnalyticsBody): Single<BaseResponse>

    fun hitEpiconAnalytics(
        deviceId: String,
        body: PartnerContentAnalyticsRequest
    ): Single<BaseResponse>

    fun fetchTrialUpgradeDetails(upgradeTrialRequest: UpgradeTrialRequest) : Single<TrialUpgradeResponse>

    fun generateAid(): Single<AnonymousResponse>
    fun saveLanguages(
        saveLanguagesBody: SaveLanguageBody
    ): Single<BaseResponse>
    fun saveParentalPin(
        parentalPinRequest: ParentalPinRequest,
        authToken: String
    ): Single<BaseResponse>
    fun validateParentalPin(
        parentalPinRequest: ParentalPinRequest
    ): Single<BaseResponse>

    fun checkForGuestUserPlaybackEligibility(): Single<UserPlaybackEligibilityResponse>
    fun checkManagedAppEligibility(): Single<UserPlaybackEligibilityResponse>
    fun getAgeRatings(): Single<AgeRatingsResponse>
    fun updateAgeRating(updateAgeRatingRequest: UpdateAgeRatingRequest): Single<BaseResponse>
    fun validateContentRating(validateContentRatingRequest: ValidateContentRatingRequest): Single<ValidateContentRatingResponse>

    fun fetchCategories(): Single<LeftMenuResponse>

    fun fetchUserPreferredLanguage(body: preferredLanguageBody): Single<UserPreferredLanguage>
    fun removeBingeList(request: ContentIdAndTypeRequest): Single<BaseResponse>
    fun packValidate(baId: String, packId: String): Single<PackValidationResponse>
    fun initiateJuspay(juspayInitiateRequest: HashMap<String, String>?): Single<JuspayInitiationResponse>
    fun addPack(addPackRequest: AddPackRequest): Single<AddPackResponse>
    fun modifyPack(addPackRequest: AddPackRequest): Single<AddPackResponse>
    fun proratedBalance(proRatedBalanceRequest: ProRatedBalanceRequest) : Single<ProRatedResponse>
    fun payByDTHBalance(payByDTHBalanceRequest: PayByDTHBalanceRequest): Single<BaseResponse>
    fun fetchCampaign(baId:String) : Single<FtvCampaignResponse>
    fun fetchPaymentStatus(paymentStatusRequest:PaymentStatusRequest) : Single<PaymentStatusResponse>
    fun fetchInvoiceDownloadLink(invoiceDownloadRequest: InvoiceDownloadRequest,baId:String):Single<InvoiceDownloadResponse>
    fun fetchMixpanelUniqueId(referenceId : String): Single<MixpanelUniqueResponse>
    fun getHoichoiPlaybackData(request: HoichoiRequest): Single<HoichoiPlayebackResponse>
    fun migrateUser(): Single<MigrateUserResponse>
    fun migrateUserInfo(cartId: String): Single<MigrateUserTickTickResponse>
    fun removeAllDevices(sid: String): Single<BaseResponse>
    fun getChaupalUrlData(contenId: String, contentType: String): Single<ChaupalUrlResponse>
    fun getPlanetMarathiPlayUrl(contenId: String, contentType: String): Single<ChaupalUrlResponse>
    fun setAppRatingEligibility(requestBody: SetAppRatingRequest): Single<AppRatingResponse>
    fun getAppRatingEligibility(requestBody: GetAppRatingRequest): Single<AppRatingResponse>
    fun getManagedAppsUrl(
        baid: String?,
        journeySource: String? = null,
        journeySourceRefId: String? = null,
        mixpanelId: String,
        source: String,
        rmn: String,
        origin: String,
        subscriptionType: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse>
    fun getManagedAppSummeryUyl(
        baid: String,
        journeySource: String? = null,
        journeySourceRefId: String? = null,
        mixpanelId: String,
        cartId: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse>

    fun getManagedAppRefreshToken(
        baid: String,
        journeySource: String? = null,
        journeySourceRefId: String? = null,
        mixpanelId: String,
        cartId: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse>
    fun planetMarathiAnalytics(requestBody: PlanetMarathiAnalyticsRequest, contentType: String): Single<BaseResponse>
    fun fetchGameFavs(request: WatchRequest) : Single<RecommendationResponse>
    fun addFavGame(profileId : String , sid: String , contentId : String , contentType : String) : Single<GameFavResponse>
    fun generateVootPwaToken(request: HashMap<String, String>) : Single<VootPwaResponse>
    fun fetchLionsGateToken(request: LionsgateRequest) : Single<LionsGateResponse>

    fun fetchTAHomeHierarchy(pageType: String,packName : String, isLoggedIn: Boolean, sId : String) : Single<HierarchyResponse>
    fun fetchVRHomeHierarchy(pageType: String,packName : String) : Single<HierarchyResponse>
    fun fetchRailData(railId : String) : Single<RecommendationResponse>
    fun fetchGenreRailData() : Single<RecommendationResponse>
    fun fetchLanguageRailData() : Single<RecommendationResponse>
    fun fetchGenericPartnerDRMAPI(
        providerContentId: String?,
        provider: String
    ): Single<GenericPartnerDRMResponse>

}