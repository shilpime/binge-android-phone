package com.tatasky.binge.domain.usecase


import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.CommonRepository
import com.tatasky.binge.epicon.PartnerContentAnalyticsRequest
import com.tatasky.binge.epicon.PlanetMarathiAnalyticsRequest
import com.tatasky.binge.hoichoi.HoichoiPlayebackResponse
import com.tatasky.binge.hoichoi.HoichoiRequest
import com.tatasky.binge.shemaroo.helper.ShemarooAnalyticsBody
import com.tatasky.binge.data.networking.models.response.ChaupalUrlResponse
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


open class CommonUseCase(private val commonRepository: CommonRepository) {

    fun executeConfig(): Single<ConfigResponse> {
        return commonRepository.getConfig()
    }

    fun executeHomeMenuItems(): Single<LeftMenuResponse> {
        return commonRepository.getLeftMenu()
    }

    fun executeHomePage(parameter: HomeRequest): Single<HomeResponse> {
        return commonRepository.getHomePage(parameter)
    }

    fun executeTARails(parameter: TARequest): Single<RecommendationResponse> {
        return commonRepository.getTARails(parameter)
    }


    fun executeRecommendationRails(parameter: DetailRequest): Single<RecommendationResponse> {
        return commonRepository.getRecommendation(parameter)
    }

    fun executeSeriesList(parameter: SeriesRequest): Single<SeriesListResponse> {
        return commonRepository.getSeriesList(parameter)
    }

    fun getLastWatchEpisode(request: ContentIdAndTypeRequest):Single<EpisodeListingResponse>{
        return commonRepository.getLastWatchEpisode(request)
    }

    fun executeBrandDetails(parameter: DetailRequest): Single<DetailsResponse> {
        return commonRepository.getBrandDetails(parameter)
    }

    fun toggleFavourite(toggleFavouriteRequest: ToggleFavouriteRequest): Single<IsFavouriteResponse> {
        return commonRepository.toggleFavourite(toggleFavouriteRequest)
    }

    fun executeFavouritesList(request: WatchRequest): Single<RecommendationResponse> {
        return commonRepository.getFavouritesList(request)
    }


    fun executeCWRails(parameter: CWRequest): Single<RecommendationResponse> {
        return commonRepository.getCWRails(parameter)
    }

    fun executeRailsData(
        railId: String,
        pageLimit: Int,
        offset: Int
    ): Single<RecommendationResponse> {
        return commonRepository.getRailData(railId, pageLimit, offset)
    }

    fun getLanguageGenreList(intent: String): Single<RecommendationResponse> {
        return commonRepository.getSearchFilters(intent)
    }


    fun getSearchRails(intent: String?): Single<HomeResponse> {
        return commonRepository.getSearchRails(intent)
    }

    fun getEpisodeSearchResponse(episodeSearchRequest: EpisodeSearchRequest):Single<SeriesListResponse>{
        return commonRepository.getEpisodeSearchResponse(episodeSearchRequest)
    }

    fun generateOTP(loginDTO: LoginDTO,isPassword:Boolean): Single<GetOtpResponse> {
        return if (loginDTO.actualRMN.isNotBlank())
            commonRepository.generateOTPWithRmn(loginDTO.rmn,isPassword)
        else if (loginDTO.sid.isNotBlank())
            commonRepository.generateOTPWithSid(loginDTO.sid,isPassword)
        else Single.just(GetOtpResponse().apply {
            status = -1
            message = "Information not valid"
        })
    }

    fun generateOtpGuestLogin(mobileNumber: String): Single<GetOtpGuestLoginResponse> {
        return commonRepository.generateOtpGuestLogin(mobileNumber)
    }

    fun validateOtpGuestLogin(validateOtpGuestLoginRequest: ValidateOtpGuestLoginRequest): Single<ValidateOTPResponse> {
        return commonRepository.validateOtpGuestLogin(validateOtpGuestLoginRequest)
    }

    fun getPreviouslyUsedMobileNumbers(): Single<PreviouslyUsedMobileNumbersResponse> {
        return commonRepository.getPreviouslyUsedMobileNumbers()
    }

    fun loginWithPassword(loginRequest: LoginDTO): Single<ValidateOTPResponse> {
        return commonRepository.loginWithPassword(loginRequest)
    }

    fun loginViaOtpUser(loginRequest: LoginDTO): Single<ValidateOTPResponse> {
        return commonRepository.loginViaOtpUser(loginRequest)
    }

    fun changePassword(
        sid: String,
        changePasswordRequest: ChangePasswordRequest
    ): Single<ForgetPasswordResponse> {
        return commonRepository.changePassword(sid, changePasswordRequest)
    }

    fun requestForgetPassword(
        sid: String, forgetPasswordRequest : ForgotPasswordRequest?
    ): Single<BaseResponse> {
        return commonRepository.requestForgetPassword(sid, forgetPasswordRequest)
    }

    fun getSubIdList(registeredMobileNumber: String): Single<SubscriberIdListResponse> {
        return commonRepository.getSubIdList(registeredMobileNumber)
    }

    fun getBaIdList(sid: String): Single<BAIdListResponse> {
        return commonRepository.getBaIdList(sid)
    }

//    fun loginBingeUser(loginRequest: LoginRequest): Single<NewBingeUserResponse> {
//        return commonRepository.loginBingeUser(loginRequest)
//    }

    fun removeDevice(
        baId: String,
        deviceId: String
    ): Single<BaseResponse> {
        return commonRepository.removeDevice(baId, deviceId)
    }

    fun downloadPDF(url: String): Single<ResponseBody> {
        return commonRepository.downloadPDF(url)
    }


    fun getDeviceList(rmn: String): Single<DeviceListResponse> {
        return commonRepository.getDeviceList(rmn)
    }

    fun getCWAction(cwRequest: CWRequest): Single<BaseResponse> {
        return commonRepository.getCWAction(cwRequest)
    }

    fun logout(baid: String): Single<BaseResponse> {
        return commonRepository.logout(baid)
    }

    fun getShemarooUrlData(cwRequest: String): Single<ShemarooSafeUrlResponse> {
        return commonRepository.getShemarooUrlData(cwRequest)
    }

    fun getProfileInfo(request: FetchProfileRequest): Single<SubscriberProfileListModel> {
        return commonRepository.getProfileDetailResponse(request)
    }

    fun editAliasName(baId: String, aliasName: String): Single<BaseResponse> {
        return commonRepository.editAliasName(baId, aliasName)
    }

    fun switchAccount(baId: String, dsn:String?, targetBaId: String): Single<SwitchAccountResponse> {
        return commonRepository.switchAccount(baId, dsn, targetBaId)
    }

    fun updateProfileImage(
        image: MultipartBody.Part,
        baId: String,
        profileId: String
    ): Single<ImageUploadResponse> {
        return commonRepository.updateProfileImage(image, baId, profileId)
    }

    fun fetchNextAndPreviousEpisodeDetails(episodeRequest: EpisodeRequest): Single<NextPreviousEpisodeResponse> {
        return commonRepository.fetchNextAndPreviousEpisodeDetails(episodeRequest)
    }

    fun getTvodContent(sid: String, offset: Int, max: Int): Single<RecommendationResponse> {
        return commonRepository.getTvodContent(sid, offset, max)
    }

//    fun createNewBingeUser(newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse> {
//        return commonRepository.createNewBingeUser(newBingeUserRequest)
//    }

    fun createNewBingeMobileUser(newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse> {
        return commonRepository.createNewBingeMobileUser(newBingeUserRequest)
    }

    fun updateEmail(updateEmailRequest: UpdateEmailRequest): Single<BaseResponse> {
        return commonRepository.updateEmail(updateEmailRequest)
    }

    fun updateEmailAndName(updateEmailRequest: UpdateEmailRequest, calledFrom: String): Single<BaseResponse> {
        return commonRepository.updateEmailAndName(updateEmailRequest, calledFrom)
    }

    fun getSearchV2(searchRequest: SearchRequest): Single<RecommendationResponse> {
        return commonRepository.getSearchV2(searchRequest)
    }

    fun removeProfileImage(sid: String, profileId: String): Single<ImageUploadResponse> {
        return commonRepository.removeProfileImage(sid, profileId)
    }

    fun fetchPackList(partnerId : String?, baId: String): Single<PackListResponse> {
        return commonRepository.getPackList(partnerId, baId)
    }

    fun fetchPaymentStatus(paymentStatusRequest:PaymentStatusRequest): Single<PaymentStatusResponse> {
        return commonRepository.fetchPaymentStatus(paymentStatusRequest)
    }

    fun fetchEligiblePackList(baId:String,subscriptionType:String): Single<EligiblePackResponse>{
        return commonRepository.fetchEligiblePackList(baId,subscriptionType)
    }

    fun fetchEligiblepackForNonLoggedIn():Single<EligiblePackResponse>{
        return commonRepository.fetchEligiblepackForNonLoggedIn()
    }

    fun requestSubscriptionCreation(subscriptionCreationRequest: SubscriptionCreationRequest): Single<PurchasePackResponse> {
        return commonRepository.requestSubscriptionCreation(subscriptionCreationRequest)
    }

    fun requestSubscriptionReactivation(subscriptionCreationRequest: SubscriptionCreationRequest): Single<PurchasePackResponse> {
        return commonRepository.requestSubscriptionReactivation(subscriptionCreationRequest)
    }

    fun requestSubscriptionModification(revoked : Boolean, subscriptionModificationRequest: SubscriptionModificationRequest): Single<PurchasePackResponse> {
        return commonRepository.requestSubscriptionModification(revoked, subscriptionModificationRequest)
    }

    fun requestSubscriptionCancellation(subscriptionCreationRequest: SubscriptionCancellationRequest):Single<CancellationResponse>{
        return commonRepository.requestSubscriptionCancellation(subscriptionCreationRequest)
    }

    fun freemiumRequestSubscriptionCancellation(subscriptionCreationRequest: FreemiumSubscriptionCancellationRequest):Single<FreemiumCancellationResponse>{
        return commonRepository.freemiumRequestSubscriptionCancellation(subscriptionCreationRequest)
    }

    fun requestSubscriptionRevokeCancellation(baId: String):Single<CancellationResponse>{
        return commonRepository.requestSubscriptionRevokeCancellation(baId)
    }

    fun fetchBalance(walletBalanceRequest: WalletBalanceRequest): Single<WalletBalanceResponse> {
        return commonRepository.fetchBalance(walletBalanceRequest)
    }

    fun refreshAccount(baId: String, dthStatus: String): Single<BaseResponse> {
        return commonRepository.refreshAccount(baId, dthStatus)
    }

    fun getCurrentPack(sid: String, baId: String): Single<PurchasePackResponse> {
        return commonRepository.getCurrentPack(sid, baId)
    }

    fun getFreemiumCurrentPack(
        baId: String,
        accountId: String,
        freemiumUserType: String,
        userIsOnTickTick: Boolean?
    ): Single<PurchasePackResponse> {
        return commonRepository.getFreemiumCurrentPack(
            baId,
            accountId,
            freemiumUserType,
            userIsOnTickTick
        )
    }

    fun callFavouriteLearnAction(contentType: String,
                                 id: String,
                                 showType: String,
                                 provider: String,
                                 refUsecase : String): Single<BaseResponse> {
        return commonRepository.callFavouriteLearnAction(contentType, id, showType, provider,refUsecase)
    }

    fun callLearnAction(
        contentType: String,
        id: String,
        showType: String,
        provider: String,
        isLoggedIn: Boolean,
        learnActionType: String,
        refUsecase: String
    ): Single<BaseResponse> {
        return commonRepository.callLearnAction(contentType, id, showType, provider,isLoggedIn,learnActionType,refUsecase)
    }
    fun fetchFaq(): Single<FaqResponse> {
        return commonRepository.getFaq()
    }

    fun getManagedAppsUrl(
        baid: String,
        journeySource: String?,
        journeySourceRefId: String?,
        mixpanelId: String,
        source: String?,
        rmn: String,
        origin: String,
        subscriptionType: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse> {
        return commonRepository.getManagedAppsUrl(
            baid,
            journeySource,
            journeySourceRefId,
            mixpanelId,
            source ?: "",
            rmn = rmn,
            origin,
            subscriptionType,
            appsFlyerId,
            appVersionName
        )
    }

    fun getManagedAppSummeryUrl(
        baid: String,
        currentJourneyRef: String,
        currentJourneyRefKey: String,
        mixpanelId: String,
        cartId: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse> {
        return commonRepository.getManagedAppSummeryUyl(
            baid,
            currentJourneyRef,
            currentJourneyRefKey,
            mixpanelId,
            cartId,
            appsFlyerId,
            appVersionName
        )
    }
    fun getManagedAppRefreshToken(
        baid: String,
        currentJourneyRef: String,
        currentJourneyRefKey: String,
        mixpanelId: String,
        cartId: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse> {
        return commonRepository.getManagedAppRefreshToken(
            baid,
            currentJourneyRef,
            currentJourneyRefKey,
            mixpanelId,
            cartId,
            appsFlyerId,
            appVersionName
        )
    }


    fun getHelpCenterURL(): Single<HelpCenterResponse> {
        return commonRepository.getHelpCenterURL()
    }

    fun executeWorkOrder(baid :String, workOrderRequest: WorkOrderRequest): Single<BaseResponse> {
        return commonRepository.callWorkOrderFS(baid, workOrderRequest)
    }

    fun initiateRecharge(sid: String,amount: String): Single<RechargeResponse>{
        return commonRepository.initiateRecharge(sid, amount)
    }

    fun initiateRecharge(sid: String): Single<RechargeResponse>{
        return commonRepository.initiateRecharge(sid)
    }

    fun rechargeNotification(sid: String, baId: String) : Single<DunningResponse>{
        return commonRepository.rechargeNotification(sid, baId)
    }

    fun getVootPlaybackData(request: VootRequest): Single<VootPlayebackResponse> {
        return commonRepository.getVootPlaybackData(request)
    }

    fun getVootKidsPlaybackData(request: VootRequest): Single<VootPlayebackResponse> {
        return commonRepository.getVootKidsPlaybackData(request)
    }

    fun changeSetting(baid: String, settingType: String) : Single<BaseResponse> {
        return commonRepository.changeSetting(baid, settingType)
    }

    fun fetchAddress() : Single<AddressResponse> {
        return commonRepository.fetchAddress()
    }

    fun fetchSlots(slotsRequest: SlotsRequest) : Single<SlotsResponse> {
        return commonRepository.fetchSlots(slotsRequest)
    }

    fun confirmSlots(slotsRequest: SlotsRequest) : Single<BaseResponse> {
        return commonRepository.confirmSlots(slotsRequest)
    }

    fun getPrefLangGenre(
        isLoggedIn: Boolean,
        type: String
    ): Single<GenreListResponse> {
        return commonRepository.getPrefLangGenre(isLoggedIn, type)
    }

    fun fetchZee5Tag() : Single<Zee5TagResponse> {
        return commonRepository.fetchZee5Tag()
    }

    fun generateControlToken(controlRequest: ControlRequest): Single<ControlTokenResponse> {
        return commonRepository.generateControlToken(controlRequest)
    }
    fun getTransactionHistory(transactionHistoryRequest: TransactionHistoryRequest): Single<TransactionHistoryResponse> {
        return commonRepository.getTransactionHistory(transactionHistoryRequest)
    }

    fun getTransactionHistoryForNonDTHUser(baid :String): Single<TransactionHistoryResponse> {
        return commonRepository.getTransactionHistoryForNonDTHUser(baid)
    }

    fun fetchLastWatch(lastWatchRequest: ToggleFavouriteRequest, subscriptionType: PartnerPacks?) : Single<IsFavouriteResponse> {
        return commonRepository.fetchLastWatch(lastWatchRequest,subscriptionType)
    }

    fun fetchBoxsetDetails(vodId: String): Single<DetailsResponse> {
        return commonRepository.fetchBoxsetDetails(vodId)
    }

    fun callWatchViewAction(contentType: String,
                             id: String,
                            subscriberId: String,
                            profileId: String): Single<BaseResponse> {
        return commonRepository.callWatchViewAction(contentType, id, subscriberId, profileId)
    }


    fun contentPlaybackExpiry(contentId: String): Single<PlaybackExpiryResponse> {
        return commonRepository.contentPlaybackExpiry(contentId)
    }

    fun fetchPubnubHistory(url : String): Single<ResponseBody> {
        return commonRepository.fetchPubnubHistory(url)
    }

    fun switchAccountAtv(sId: String):Single<SwitchAccountResponse> {
        return commonRepository.switchAccountAtv(sId)
    }

    fun generateSonylivToken(): Single<ControlTokenResponse> {
        return commonRepository.generateSonylivToken()
    }

    fun fetchMarketingResponse():Single<MarketingResponse>{
        return commonRepository.fetchMarketingResponse()
    }

    fun activatePrime(primeActivationRequest: PrimeActivationRequest) : Single<PrimeActivationResponse>{
        return commonRepository.activatePrime(primeActivationRequest)
    }

    fun fetchPrimePackList():Single<PrimePackListResponse>{
        return commonRepository.fetchPrimePackList()
    }

    fun fetchInterstitialPageResponse():Single<PrimeInterstitialResponse>{
        return commonRepository.fetchInterstitialPageResponse()
    }

    fun requestPrimeResume() : Single<BaseResponse>{
        return commonRepository.requestPrimeResume()
    }

    fun shemarooMeAnalytics(body: ShemarooAnalyticsBody): Single<BaseResponse> {
        return commonRepository.shemarooMeAnalytics(body)
    }

  fun lionsgateAnalytics(body: LionsgateAnalyticsBody): Single<BaseResponse> {
        return commonRepository.lionsgateAnalytics(body)
    }

    fun createBingeAccount(sid:String) : Single<NewBingeUserResponse>{
        return commonRepository.createBingeAccount(sid)
    }
    fun hitEpiconAnalytics(deviceId: String, body : PartnerContentAnalyticsRequest): Single<BaseResponse> {
        return commonRepository.hitEpiconAnalytics(deviceId, body)
    }

    fun fetchTrialUpgradeDetails(upgradeTrialRequest: UpgradeTrialRequest) : Single<TrialUpgradeResponse>{
        return commonRepository.fetchTrialUpgradeDetails(upgradeTrialRequest)
    }

    fun generateAid(): Single<AnonymousResponse> {
        return commonRepository.generateAid()
    }

    fun saveLanguages(
        saveLanguagesBody: SaveLanguageBody
    ): Single<BaseResponse> {
        return commonRepository.saveLanguages(
            saveLanguagesBody
        )
    }

    fun saveParentalPin(
        parentalPinRequest: ParentalPinRequest,
        authToken: String
    ): Single<BaseResponse> {
        return commonRepository.saveParentalPin(parentalPinRequest, authToken)
    }

    fun validateParentalPin(parentalPinRequest: ParentalPinRequest): Single<BaseResponse> {
        return commonRepository.validateParentalPin(parentalPinRequest)
    }

    fun checkForGuestUserPlaybackEligibility(): Single<UserPlaybackEligibilityResponse> {
        return commonRepository.checkForGuestUserPlaybackEligibility()
    }

    fun checkManagedAppEligibility(): Single<UserPlaybackEligibilityResponse> {
        return commonRepository.checkManagedAppEligibility()
    }

    fun getAgeRatings(): Single<AgeRatingsResponse> {
        return commonRepository.getAgeRatings()
    }

    fun updateAgeRating(updateAgeRatingRequest: UpdateAgeRatingRequest): Single<BaseResponse> {
        return commonRepository.updateAgeRating(updateAgeRatingRequest)
    }

    fun validateContentRating(validateContentRatingRequest: ValidateContentRatingRequest): Single<ValidateContentRatingResponse> {
        return commonRepository.validateContentRating(validateContentRatingRequest)
    }

    fun fetchCategories(): Single<LeftMenuResponse> {
        return commonRepository.fetchCategories()
    }

    fun fetchUserPreferredLanguage(body: preferredLanguageBody): Single<UserPreferredLanguage> {
        return commonRepository.fetchUserPreferredLanguage(body)
    }

    fun removeBingeList(request: ContentIdAndTypeRequest):Single<BaseResponse>{
        return commonRepository.removeBingeList(request)
    }

    fun packValidate(baId: String, packId: String): Single<PackValidationResponse> {
        return commonRepository.packValidate(baId,packId)
    }

    fun initiateJuspay(juspayInitiateRequest: HashMap<String, String>?): Single<JuspayInitiationResponse>{
        return commonRepository.initiateJuspay(juspayInitiateRequest)
    }

    fun addPack(addPackRequest: AddPackRequest): Single<AddPackResponse> {
        return commonRepository.addPack(addPackRequest)
    }

    fun modifyPack(addPackRequest: AddPackRequest): Single<AddPackResponse>{
        return commonRepository.modifyPack(addPackRequest)
    }

    fun proratedBalance(proRatedBalanceRequest: ProRatedBalanceRequest): Single<ProRatedResponse> {
        return commonRepository.proratedBalance(proRatedBalanceRequest)
    }

    fun payByDTHBalance(payByDTHBalanceRequest: PayByDTHBalanceRequest): Single<BaseResponse> =
        commonRepository.payByDTHBalance(payByDTHBalanceRequest)

    fun fetchCampaign(baId: String):Single<FtvCampaignResponse> = commonRepository.fetchCampaign(baId)

    fun fetchInvoiceDownloadLink(invoiceDownloadRequest: InvoiceDownloadRequest,baId: String):Single<InvoiceDownloadResponse>{
        return commonRepository.fetchInvoiceDownloadLink(invoiceDownloadRequest,baId)
    }

    fun fetchMixpanelUniqueId(referenceId : String): Single<MixpanelUniqueResponse>{
        return commonRepository.fetchMixpanelUniqueId(referenceId)
    }
    fun getHoichoiPlaybackData(request: HoichoiRequest): Single<HoichoiPlayebackResponse> {
        return commonRepository.getHoichoiPlaybackData(request)
    }

    fun migrateUser(): Single<MigrateUserResponse> {
        return commonRepository.migrateUser()
    }

    fun migrateUserInfo(cartId: String): Single<MigrateUserTickTickResponse> {
        return commonRepository.migrateUserInfo(cartId)
    }

    fun removeAllDevices(sid: String): Single<BaseResponse> {
        return commonRepository.removeAllDevices(sid)
    }

    fun getChaupalUrlData(contenId: String, contentType : String): Single<ChaupalUrlResponse> {
        return commonRepository.getChaupalUrlData(contenId, contentType)
    }

    fun getPlanetMarathiPlayUrl(contenId: String, contentType : String): Single<ChaupalUrlResponse> {
        return commonRepository.getPlanetMarathiPlayUrl(contenId, contentType)
    }

    fun setAppRatingEligibility(requestBody: SetAppRatingRequest): Single<AppRatingResponse> {
        return commonRepository.setAppRatingEligibility(requestBody)
    }

    fun getAppRatingEligibility(requestBody: GetAppRatingRequest): Single<AppRatingResponse> {
        return commonRepository.getAppRatingEligibility(requestBody)
    }

    fun planetMarathiAnalytics(requestBody: PlanetMarathiAnalyticsRequest, contentType: String): Single<BaseResponse> {
        return commonRepository.planetMarathiAnalytics(requestBody, contentType)
    }

    fun fetchGameFavs(request: WatchRequest): Single<RecommendationResponse>{
        return commonRepository.fetchGameFavs(request)
    }

    fun addFavGame(profileId : String , sid: String , contentId : String , contentType : String): Single<GameFavResponse>{
        return commonRepository.addFavGame(profileId, sid, contentId ,contentType)
    }

    fun generateVootPwaToken(request:HashMap<String,String>): Single<VootPwaResponse> {
        return commonRepository.generateVootPwaToken(request)
    }

    fun fetchLionsGateToken(request:LionsgateRequest): Single<LionsGateResponse> {
        return commonRepository.fetchLionsGateToken(request)
    }


    fun fetchTAHomeHierarchy(
        pageType: String,
        packName: String,
        isLoggedIn: Boolean,
        sId: String
    ): Single<HierarchyResponse> {
        return commonRepository.fetchTAHomeHierarchy(pageType, packName, isLoggedIn, sId)
    }


    fun fetchVRHomeHierarchy(
        pageType: String,
        packName: String
    ): Single<HierarchyResponse> {
        return commonRepository.fetchVRHomeHierarchy(pageType, packName)
    }

    fun fetchEditorialRailData(railId : String) : Single<RecommendationResponse>{
        return commonRepository.fetchRailData(railId)
    }

    fun fetchLanguageRailData() : Single<RecommendationResponse>{
        return commonRepository.fetchLanguageRailData()
    }

    fun fetchGenreRailData() : Single<RecommendationResponse>{
        return commonRepository.fetchGenreRailData()
    }

    fun fetchGenericPartnerDRMAPI(
        providerContentId: String?,
        provider: String
    ): Single<GenericPartnerDRMResponse> {
        return commonRepository.fetchGenericPartnerDRMAPI(providerContentId, provider)
    }
}