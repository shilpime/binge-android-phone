package com.tatasky.binge.data.repository

import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.data.networking.services.CommonService
import com.tatasky.binge.domain.repositories.CommonRepository
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


class CommonRepoImpl(private val service: CommonService) : CommonRepository {

    override fun getSearchFilters(intent: String): Single<RecommendationResponse> {
        return service.languageGenreList(intent)
    }

    override fun getSearchRails(intent: String?): Single<HomeResponse> {
        return service.getSearchRails(intent)
    }
    override fun getEpisodeSearchResponse(episodeSearchRequest: EpisodeSearchRequest) : Single<SeriesListResponse>{
        return service.getEpisodeSearchResponse(episodeSearchRequest)
    }


    override fun getCWRails(request: CWRequest): Single<RecommendationResponse> {
        return service.getCWRails(request)
    }

    override fun getRailData(
        railId: String,
        pageLimit: Int,
        offset: Int
    ): Single<RecommendationResponse> {
        return service.getRailData(railId, pageLimit, offset)
    }

    override fun toggleFavourite(toggleFavouriteRequest: ToggleFavouriteRequest): Single<IsFavouriteResponse> {
        return service.toggleFavourite(toggleFavouriteRequest)
    }

    override fun getLeftMenu(): Single<LeftMenuResponse> {
        return service.getLeftMenuItem()
    }

    override fun getConfig(): Single<ConfigResponse> {
        return service.getConfig()
    }

    override fun getHomePage(request: HomeRequest): Single<HomeResponse> {
        return service.getHomePage(request)
    }


    override fun getTARails(request: TARequest): Single<RecommendationResponse> {
        return service.getTARails(request)
    }

    override fun getRecommendation(parameter: DetailRequest): Single<RecommendationResponse> {
        return service.getRecommendation(parameter)
    }

    override fun getSeriesList(parameter: SeriesRequest): Single<SeriesListResponse> {
        return service.getSeriesList(parameter)
    }

    override fun getBrandDetails(request: DetailRequest): Single<DetailsResponse> {
        return service.getBrandDetails(request)
    }

    override fun getFavouritesList(request: WatchRequest): Single<RecommendationResponse> {
        return service.getFavouritesList(request)
    }

    override fun generateOTPWithRmn(rmn: String, isPassword: Boolean): Single<GetOtpResponse> {
        return service.generateOtpWithRmn(rmn,isPassword)
    }

    override fun generateOtpGuestLogin(mobileNumber: String): Single<GetOtpGuestLoginResponse> {
        return service.generateOtpGuestLogin(mobileNumber)
    }

    override fun validateOtpGuestLogin(validateOtpGuestLoginRequest: ValidateOtpGuestLoginRequest): Single<ValidateOTPResponse> {
        return service.validateOtpGuestLogin(validateOtpGuestLoginRequest)
    }

    override fun getPreviouslyUsedMobileNumbers(): Single<PreviouslyUsedMobileNumbersResponse> {
        return service.getPreviouslyUsedMobileNumbers()
    }

    override fun loginViaOtpUser(loginRequest: LoginDTO): Single<ValidateOTPResponse> {
        return service.loginWithOTP(loginRequest)
    }

//    override fun loginBingeUser(loginRequest: LoginRequest): Single<NewBingeUserResponse> {
//        return service.loginBingeUser(loginRequest)
//    }

    override fun loginWithPassword(loginRequest: LoginDTO): Single<ValidateOTPResponse> {
        return service.loginWithPassword(loginRequest)
    }

    override fun changePassword(
        sid: String,
        changePasswordRequest: ChangePasswordRequest
    ): Single<ForgetPasswordResponse> {
        return service.changePassword(sid, changePasswordRequest)
    }

    override fun requestForgetPassword(
        sid: String, forgetPasswordRequest : ForgotPasswordRequest?
    ): Single<BaseResponse> {
        return service.requestForgetPassword(sid, forgetPasswordRequest)
    }

    override fun getSubIdList(rmn: String): Single<SubscriberIdListResponse> {
        return service.getSubIdList(rmn)
    }

    override fun getBaIdList(sid: String): Single<BAIdListResponse> {
        return service.getBaIdList(sid)
    }

    override fun removeDevice(
        str1: String,
        str2: String
    ): Single<BaseResponse> {
        return service.removeDevice(str1, str2)
    }

    override fun getDeviceList(rmn: String): Single<DeviceListResponse> {
        return service.getDeviceList(rmn)
    }

    override fun downloadPDF(url: String): Single<ResponseBody> {
        return service.downloadPDF(url)
    }

    override fun getCWAction(cwRequest: CWRequest): Single<BaseResponse> {
        return service.getCWAction(cwRequest)
    }

    override fun getFaq(): Single<FaqResponse>{
        return service.getFaq()
    }

    override fun getHelpCenterURL(): Single<HelpCenterResponse> {
        return service.getHelpCenterUrl()
    }

    override fun getShemarooUrlData(url: String): Single<ShemarooSafeUrlResponse> {
        return service.getShemarooUrlData(url)
    }

    override fun logout(baId: String): Single<BaseResponse> {
        return service.logout(baId)
    }

    override fun getProfileDetailResponse(request: FetchProfileRequest): Single<SubscriberProfileListModel> {
        return service.getProfileDetail(request)
    }

    override fun editAliasName(baId: String, aliasName: String): Single<BaseResponse> {
        return service.editAliasName(baId, aliasName)
    }

    override fun switchAccount(baId: String,dsn: String?, targetBaId: String): Single<SwitchAccountResponse> {
        return service.switchAccount(targetBaId, dsn, baId)
    }

    override fun updateProfileImage(
        image: MultipartBody.Part,
        baId: String,
        profileId: String
    ): Single<ImageUploadResponse> {
        return service.updateProfileImage(image, baId, profileId)
    }

    override fun fetchNextAndPreviousEpisodeDetails(episodeRequest: EpisodeRequest): Single<NextPreviousEpisodeResponse> {
        return service.fetchNextAndPreviousEpisodeDetails(episodeRequest)
    }

    override fun generateOTPWithSid(sid: String, isPassword: Boolean): Single<GetOtpResponse> {
        return service.generateOtpSubscriberId(sid,isPassword)
    }

    override fun getTvodContent(
        sId: String,
        offset: Int,
        max: Int
    ): Single<RecommendationResponse> {
        return service.getTVoDContent(sId, offset, max)
    }
//
//    override fun createNewBingeUser(newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse> {
//        return service.createNewBingeUser(newBingeUserRequest)
//    }

    override fun createNewBingeMobileUser(newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse> {
        return service.createNewBingeMobileUser(newBingeUserRequest)
    }

    override fun updateEmail(updateEmailRequest: UpdateEmailRequest): Single<BaseResponse> {
        return service.updateEmail(updateEmailRequest)
    }

    override fun updateEmailAndName(updateEmailRequest: UpdateEmailRequest, calledFrom: String): Single<BaseResponse> {
        return service.updateEmailAndName(updateEmailRequest, calledFrom)
    }

    override fun getSearchV2(searchRequest: SearchRequest): Single<RecommendationResponse> {
        return service.getSearchV2(searchRequest)
    }

    override fun removeProfileImage(sid: String, profileId: String): Single<ImageUploadResponse> {
        return service.removeProfileImage(sid, profileId)
    }

    override fun getPackList(partnerId : String?, baId: String): Single<PackListResponse> {
        return service.getPackList(partnerId, baId)
    }

    override fun fetchEligiblePackList(baId: String,subscriptionType:String): Single<EligiblePackResponse> {
        return service.fetchEligiblePackListing(baId,subscriptionType)
    }

    override fun fetchEligiblepackForNonLoggedIn(): Single<EligiblePackResponse>{
        return service.fetchEligiblepackForNonLoggedIn()
    }

    override fun requestSubscriptionCreation(subscriptionCreationRequest: SubscriptionCreationRequest): Single<PurchasePackResponse> {
        return service.requestSubscriptionCreation(subscriptionCreationRequest)
    }

    override fun requestSubscriptionModification(revoked: Boolean,subscriptionModificationRequest: SubscriptionModificationRequest): Single<PurchasePackResponse> {
        return service.requestSubscriptionModification(revoked, subscriptionModificationRequest)
    }

    override fun requestSubscriptionCancellation(subscriptionCreationRequest: SubscriptionCancellationRequest):Single<CancellationResponse>{
        return service.requestSubscriptionCancellation(subscriptionCreationRequest)
    }

    override fun freemiumRequestSubscriptionCancellation(subscriptionCreationRequest: FreemiumSubscriptionCancellationRequest):Single<FreemiumCancellationResponse>{
        return service.freemiumRequestSubscriptionCancellation(subscriptionCreationRequest)
    }

    override fun requestSubscriptionRevokeCancellation(baId: String):Single<CancellationResponse>{
        return service.requestSubscriptionRevokeCancellation(baId)
    }

    override fun fetchBalance(walletBalanceRequest: WalletBalanceRequest): Single<WalletBalanceResponse>{
        return service.fetchBalance(walletBalanceRequest)
    }
    override fun refreshAccount(baId: String, dthStatus: String): Single<BaseResponse>{
        return service.refreshAccount(baId, dthStatus)
    }

    override fun getCurrentPack(sid: String, baId: String): Single<PurchasePackResponse> {
        return service.getCurrentPack(sid, baId)
    }

    override fun getFreemiumCurrentPack(
        baId: String,
        accountId: String,
        freemiumUserType: String,
        userIsOnTickTick: Boolean?
    ): Single<PurchasePackResponse> {
        return service.fetchFreemiumCurrentPlan(baId, accountId, freemiumUserType, userIsOnTickTick)
    }

    override fun callFavouriteLearnAction(contentType: String,
                                           id: String,
                                           showType: String,
                                           provider: String,
                                           refUsecase : String): Single<BaseResponse> {
        return service.callFavouriteLearnAction(contentType, id, showType, provider,refUsecase)
    }

    override fun callLearnAction(
        contentType: String,
        id: String,
        showType: String,
        provider: String,
        isLoggedIn: Boolean,
        learnActionType: String,
        refUsecase: String
    ): Single<BaseResponse> {
        return service.callLearnAction(contentType, id, showType, provider,isLoggedIn,learnActionType,refUsecase)
    }

    override fun callWorkOrderFS(baId: String,workOrderRequest: WorkOrderRequest): Single<BaseResponse> {
        return service.callWorkOrderFS(baId, workOrderRequest)
    }

    override fun initiateRecharge(sid: String,amount: String): Single<RechargeResponse>{
        return service.initiateRecharge(sid, amount)
    }

    override fun initiateRecharge(sid: String): Single<RechargeResponse>{
        return service.initiateRecharge(sid)
    }

    override fun rechargeNotification(sid: String, baId: String): Single<DunningResponse> {
        return service.rechargeNotification(sid, baId)
    }

    override fun getVootPlaybackData(request: VootRequest): Single<VootPlayebackResponse> {
        return service.getVootPlaybackData(request)
    }
    override fun getVootKidsPlaybackData(request: VootRequest): Single<VootPlayebackResponse> {
        return service.getVootKidsPlaybackData(request)
    }

    override fun changeSetting(baid: String, settingType: String) : Single<BaseResponse> {
        return service.changeSetting(baid, settingType)
    }

    override fun fetchAddress(): Single<AddressResponse> {
        return service.fetchAddress()
    }

    override fun fetchSlots(slotsRequest: SlotsRequest): Single<SlotsResponse> {
        return service.fetchSlots(slotsRequest)
    }

    override fun confirmSlots(slotsRequest: SlotsRequest): Single<BaseResponse> {
        return service.confirmSlots(slotsRequest)
    }

    override fun requestSubscriptionReactivation(subscriptionCreationRequest: SubscriptionCreationRequest): Single<PurchasePackResponse> {
        return service.requestSubscriptionReactivation(subscriptionCreationRequest)
    }

    override fun getPrefLangGenre(isLoggedIn: Boolean,
                                  type: String): Single<GenreListResponse> {
        return service.getPrefLangGenre(isLoggedIn, type)
    }

    override fun fetchZee5Tag() : Single<Zee5TagResponse>{
        return service.fetchZee5Tag()
    }

    override fun generateControlToken(controlRequest: ControlRequest): Single<ControlTokenResponse> {
        return service.generateControlToken(controlRequest)
    }

    override fun getTransactionHistory(transactionHistoryRequest: TransactionHistoryRequest): Single<TransactionHistoryResponse> {
        return service.getTransactionHistory(transactionHistoryRequest)
    }

    override fun getTransactionHistoryForNonDTHUser(baId: String): Single<TransactionHistoryResponse> {
        return service.getTransactionHistoryForNonDTHUser(baId)
    }

    override fun fetchLastWatch(lastWatchRequest: ToggleFavouriteRequest, subscriptionType: PartnerPacks?) : Single<IsFavouriteResponse> {
        return service.fetchLastWatch(lastWatchRequest,subscriptionType)
    }

    override fun fetchBoxsetDetails(vodId: String): Single<DetailsResponse> {
        return service.fetchBoxsetDetails(vodId)
    }

    override fun callWatchViewAction(
        contentType: String,
        id: String,
        subscriberId: String,
        profileId: String
    ): Single<BaseResponse> {
        return service.callWatchViewAction(contentType, id, subscriberId, profileId)
    }

    override fun contentPlaybackExpiry(vodId: String): Single<PlaybackExpiryResponse> {
        return service.contentPlaybackExpiry(vodId)
    }


    override fun fetchPubnubHistory(url : String): Single<ResponseBody> {
        return service.fetchPubnubHistory(url)
    }

    override fun getLastWatchEpisode(request: ContentIdAndTypeRequest): Single<EpisodeListingResponse> {
        return service.getLastWatchEpisode(request)
    }

    override fun switchAccountAtv(
        baId: String
    ): Single<SwitchAccountResponse> {
        return service.switchAccountAtv(baId)
    }


    override fun generateSonylivToken(): Single<ControlTokenResponse> {
        return service.generateSonylivToken()
    }

    override fun fetchMarketingResponse():Single<MarketingResponse>{
        return service.fetchMarketingResponse()
    }

    override fun activatePrime(primeActivationRequest: PrimeActivationRequest): Single<PrimeActivationResponse>{
        return service.activatePrime(primeActivationRequest)
    }
    override fun fetchPrimePackList():Single<PrimePackListResponse>{
        return service.fetchPrimePackList()
    }

    override fun fetchInterstitialPageResponse():Single<PrimeInterstitialResponse>{
        return service.fetchInterstitialPageResponse()
    }

    override fun requestPrimeResume(): Single<BaseResponse> {
        return service.requestPrimeResume()
    }

    override fun shemarooMeAnalytics(body: ShemarooAnalyticsBody): Single<BaseResponse> {
        return service.shemarooMeAnalytics(body)
    }

    override fun lionsgateAnalytics(body: LionsgateAnalyticsBody): Single<BaseResponse> {
        return service.lionsgateAnalytics(body)
    }

    override fun createBingeAccount(sid:String): Single<NewBingeUserResponse> {
        return service.createBingeAccount(sid)
    }

    override fun hitEpiconAnalytics(deviceId: String, body : PartnerContentAnalyticsRequest): Single<BaseResponse> {
        return service.hitEpiconAnalytics(deviceId, body)
    }

    override fun fetchTrialUpgradeDetails(upgradeTrialRequest: UpgradeTrialRequest) : Single<TrialUpgradeResponse>{
        return service.fetchTrialUpgradeDetails(upgradeTrialRequest)
    }

    /*Freemium APIs*/
    override fun generateAid(): Single<AnonymousResponse> {
        return service.generateAid()
    }

    override fun saveLanguages(
        saveLanguagesBody: SaveLanguageBody
    ): Single<BaseResponse> {
        return service.saveLanguages(
            saveLanguagesBody
        )
    }

    override fun saveParentalPin(parentalPinRequest: ParentalPinRequest, authToken: String): Single<BaseResponse> {
        return service.saveParentalPin(parentalPinRequest, authToken)
    }

    override fun validateParentalPin(parentalPinRequest: ParentalPinRequest): Single<BaseResponse> {
        return service.validateParentalPin(parentalPinRequest)
    }
    override fun checkForGuestUserPlaybackEligibility(): Single<UserPlaybackEligibilityResponse> {
        return service.checkForGuestUserPlaybackEligibility()
    }

    override fun checkManagedAppEligibility(): Single<UserPlaybackEligibilityResponse> {
        return service.checkManagedAppEligibility()
    }

    override fun getAgeRatings(): Single<AgeRatingsResponse> {
        return service.getAgeRatings()
    }

    override fun updateAgeRating(updateAgeRatingRequest: UpdateAgeRatingRequest): Single<BaseResponse> {
        return service.updateAgeRating(updateAgeRatingRequest)
    }

    override fun validateContentRating(validateContentRatingRequest: ValidateContentRatingRequest): Single<ValidateContentRatingResponse> {
        return service.validateContentRating(validateContentRatingRequest)
    }

    override fun fetchCategories(): Single<LeftMenuResponse> {
        return service.fetchCategories()
    }

    override fun fetchUserPreferredLanguage(
        body: preferredLanguageBody
    ): Single<UserPreferredLanguage> {
        return service.fetchUserPreferredLanguage(body)
    }

    override fun initiateJuspay(juspayInitiateRequest: HashMap<String, String>?): Single<JuspayInitiationResponse> {
        return service.initiateJuspay(juspayInitiateRequest)
    }

    override fun removeBingeList(request: ContentIdAndTypeRequest): Single<BaseResponse> {
        return service.removeBingeList(request)
    }

    override fun packValidate(baId: String, packId: String): Single<PackValidationResponse> {
        return service.packValidate(baId, packId)
    }

    override fun addPack(addPackRequest: AddPackRequest): Single<AddPackResponse> {
        return service.addPack(addPackRequest)
    }

    override fun modifyPack(addPackRequest: AddPackRequest): Single<AddPackResponse> {
        return service.modifyPack(addPackRequest)
    }

    override fun proratedBalance(proRatedBalanceRequest: ProRatedBalanceRequest) : Single<ProRatedResponse>{
        return service.proratedBalance(proRatedBalanceRequest)
    }

    override fun payByDTHBalance(payByDTHBalanceRequest: PayByDTHBalanceRequest): Single<BaseResponse> {
        return service.payByDTHBalance(payByDTHBalanceRequest)
    }

    override fun fetchCampaign(baId: String): Single<FtvCampaignResponse>{
        return service.fetchCampaign(baId)
    }



    override fun fetchInvoiceDownloadLink(invoiceDownloadRequest: InvoiceDownloadRequest, baId: String): Single<InvoiceDownloadResponse> {
        return service.getInvoiceDownload(invoiceDownloadRequest,baId)
    }

    override fun fetchPaymentStatus(paymentStatusRequest:PaymentStatusRequest) : Single<PaymentStatusResponse>{
        return service.fetchPaymentStatus(paymentStatusRequest)
    }

    override fun getHoichoiPlaybackData(request: HoichoiRequest): Single<HoichoiPlayebackResponse> {
        return service.getHoichoiPlaybackData(request)
    }

    override fun migrateUser(): Single<MigrateUserResponse> {
        return service.migrateUser()
    }
    override fun migrateUserInfo(cartId: String): Single<MigrateUserTickTickResponse> {
        return service.migrateUserInfo(cartId)
    }

    override fun removeAllDevices(sid: String): Single<BaseResponse> {
        return service.removeAllDevice(sid)
    }

    override fun getChaupalUrlData(
        contenId: String,
        contentType: String
    ): Single<ChaupalUrlResponse> {
        return service.getChaupalUrlData(contenId, contentType)
    }

    override fun getPlanetMarathiPlayUrl(
        contenId: String,
        contentType: String
    ): Single<ChaupalUrlResponse> {
        return service.getPlanetMarathiPlayUrl(contenId, contentType)
    }

    override fun fetchMixpanelUniqueId(referenceId : String): Single<MixpanelUniqueResponse>{
        return service.fetchMixpanelUniqueId(referenceId)
    }

    override fun getManagedAppsUrl(
        baid: String?,
        journeySource: String?,
        journeySourceRefId: String?,
        mixpanelId: String,
        source: String,
        rmn: String,
        origin: String,
        subscriptionType: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse> {
        return service.getManagedAppsUrl(
            baid,
            journeySource,
            journeySourceRefId,
            mixpanelId,
            source,
            rmn,
            origin,
            subscriptionType,
            appsFlyerId,
            appVersionName
        )
    }

    override fun getManagedAppSummeryUyl(
        baid: String,
        journeySource: String?,
        journeySourceRefId: String?,
        mixpanelId: String,
        cartId: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse> {
        return service.getManagedAppSummeryUrl(
            baid,
            journeySource,
            journeySourceRefId,
            mixpanelId,
            cartId,
            appsFlyerId,
            appVersionName
        )
    }

    override fun getManagedAppRefreshToken(
        baid: String,
        journeySource: String?,
        journeySourceRefId: String?,
        mixpanelId: String,
        cartId: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse> {
        return service.getManagedAppRefreshToken(
            baid,
            journeySource,
            journeySourceRefId,
            mixpanelId,
            cartId,
            appsFlyerId,
            appVersionName
        )
    }




    override fun setAppRatingEligibility(requestBody: SetAppRatingRequest): Single<AppRatingResponse> {
        return service.setAppRatingEligibility(requestBody)
    }

    override fun getAppRatingEligibility(requestBody: GetAppRatingRequest): Single<AppRatingResponse> {
        return service.getAppRatingEligibility(requestBody)
    }




    override fun planetMarathiAnalytics(
        requestBody: PlanetMarathiAnalyticsRequest,
        contentType: String
    ): Single<BaseResponse> {
        return service.planetMarathiAnalytics(requestBody, contentType)
    }

    override fun fetchGameFavs(request: WatchRequest) : Single<RecommendationResponse>{
        return service.fetchGameFavs(request)
    }

    override fun addFavGame(profileId : String , sid: String , contentId : String , contentType : String) : Single<GameFavResponse>{
        return service.addGameToFav(profileId, sid ,contentId , contentType)
    }

    override fun generateVootPwaToken(request: HashMap<String, String>): Single<VootPwaResponse> {
        return service.generateVootPwaToken(request)
    }

    override fun fetchLionsGateToken(request: LionsgateRequest): Single<LionsGateResponse> {
        return service.fetchLionsGateToken(request)
    }
    override fun fetchTAHomeHierarchy(pageType: String, packName : String, isLoggedIn: Boolean, sId: String) : Single<HierarchyResponse>{
        return service.fetchTAHomeHierarchy(pageType, packName, isLoggedIn, sId)
    }

    override fun fetchVRHomeHierarchy(pageType: String, packName : String) : Single<HierarchyResponse>{
        return service.fetchVRHomeHierarchy(pageType, packName)
    }

    override fun fetchRailData(railId : String) : Single<RecommendationResponse>{
        return service.getEditorialRailData(railId)
    }

    override fun fetchLanguageRailData(): Single<RecommendationResponse> {
        return service.fetchLanguageRailData()
    }

    override fun fetchGenericPartnerDRMAPI(
        providerContentId: String?,
        provider: String
    ): Single<GenericPartnerDRMResponse> {
        return service.fetchGenericPartnerDRMAPI(providerContentId, provider)
    }

    override fun fetchGenreRailData(): Single<RecommendationResponse> {
        return service.fetchGenreRailData()
    }


}