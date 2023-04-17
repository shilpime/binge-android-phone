package com.tatasky.binge.data.networking.services

import com.tatasky.binge.BuildConfig
import com.tatasky.binge.data.networking.ApplicationApis
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
import com.tatasky.binge.utils.*
import com.tatasky.binge.voot.model.VootPlayebackResponse
import com.tatasky.binge.voot.model.VootRequest
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Query
import java.util.*
import javax.inject.Singleton


@Singleton
class CommonService(private val applicationApis: ApplicationApis) {

    fun getLeftMenuItem(): Single<LeftMenuResponse> {
        return applicationApis.getLeftMenuItem()
    }

    fun getConfig(): Single<ConfigResponse> {
        return applicationApis.getConfig()
    }

    fun getHomePage(request: HomeRequest): Single<HomeResponse> {
        return applicationApis.getHomePage(
            request.pageType,
            request.pageLimit,
            request.pageOffset,
            subscribed = request.subscribed,
            unsubscribed = request.unsubscribed,
            packName =  request.packName
        )
    }

    fun getRailData(railId: String, pageLimit: Int, offset: Int): Single<RecommendationResponse> {
        return applicationApis.getRailData(railId, pageLimit, offset)
    }

    fun getSeriesList(request: SeriesRequest): Single<SeriesListResponse> {
        return applicationApis.getSeriesList(
            request.id,
            request.max,
            request.from,
            request.profileId,
            request.subscriberId,
            request.isLastWatch,
            request.isAutoScroll
        )
    }

    fun getBrandDetails(request: DetailRequest): Single<DetailsResponse> {
        return applicationApis.getBrandDetailsWithCW(request.detailsType, request.id, request.profileId, request.subscriberId)
    }

    fun loginWithPassword(loginDTO: LoginDTO): Single<ValidateOTPResponse> {
        return applicationApis.validatePassword(loginDTO)
    }

    fun loginWithOTP(loginRequest: LoginDTO): Single<ValidateOTPResponse> {
        return if(loginRequest.sid.isNotBlank()){
            applicationApis.validateOtpForSID(loginRequest)
        } else {
            applicationApis.validateOtp(loginRequest)
        }
    }

    fun generateOtpSubscriberId(sid: String,isPassword: Boolean): Single<GetOtpResponse> {
        return applicationApis.generateOtpSubscriberId(sid,isPassword)
    }

    fun generateOtpWithRmn(rmn: String,isPassword: Boolean): Single<GetOtpResponse> {
        return applicationApis.generateOtpLogin(rmn)
    }

    fun generateOtpGuestLogin(mobileNumber: String): Single<GetOtpGuestLoginResponse> {
        return applicationApis.generateOtpGuestLogin(
            mobileNumber)
    }

    fun validateOtpGuestLogin(validateOtpGuestLoginRequest: ValidateOtpGuestLoginRequest): Single<ValidateOTPResponse> {
        return applicationApis.validateOtpGuestLogin(validateOtpGuestLoginRequest)
    }

    fun getPreviouslyUsedMobileNumbers(): Single<PreviouslyUsedMobileNumbersResponse> {
        return applicationApis.getPreviouslyUsedMobileNumbers()
    }


    fun getBaIdList(sid: String): Single<BAIdListResponse> {
        return applicationApis.baIdLookup(sid)
    }

//    fun createNewBingeUser(newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse> {
//        return applicationApis.createNewBingeUser(newBingeUserRequest)
//    }

//    fun loginBingeUser(loginRequest: LoginRequest): Single<NewBingeUserResponse> {
//        return applicationApis.loginBingeUser(loginRequest)
//    }

    fun updateEmail(updateEmailRequest: UpdateEmailRequest): Single<BaseResponse>{
        return applicationApis.updateEmail(updateEmailRequest)
    }

    fun updateEmailAndName(updateEmailRequest: UpdateEmailRequest, calledFrom: String): Single<BaseResponse> {
        return if (calledFrom.equals(HEADER_FROM_NUDGE, true)) {
            val updateEmailRequestWithoutName = UpdateEmailRequestWithoutName(
                updateEmailRequest.emailId,
                updateEmailRequest.rmn,
                updateEmailRequest.subscriberId,
                updateEmailRequest.baId
            )
            applicationApis.updateEmailWithoutName(updateEmailRequestWithoutName, calledFrom)
        } else
            applicationApis.updateEmailAndName(updateEmailRequest, calledFrom)
    }

    fun changePassword(
        sid: String,
        changePasswordRequest: ChangePasswordRequest
    ): Single<ForgetPasswordResponse> {
        if(!changePasswordRequest.isLogin)
            return applicationApis.changePassword(sid, changePasswordRequest)
        else
            return applicationApis.changePasswordBeforeLogin(sid, changePasswordRequest)
    }

    fun requestForgetPassword(
        sid: String,forgetPasswordRequest : ForgotPasswordRequest?
    ): Single<BaseResponse> {
        return forgetPasswordRequest?.let { applicationApis.forgetPasswordRequest(sid, forgetPasswordRequest) }?:applicationApis.initiateForgetPassword(sid)
    }

    fun removeDevice(
        mobileNumber: String,
        changePasswordRequest: String
    ): Single<BaseResponse> {
        return applicationApis.removeDevice(mobileNumber, changePasswordRequest)
    }

    fun getDeviceList(
        baId: String
    ): Single<DeviceListResponse> {
        return applicationApis.getDeviceList(baId)
    }

    fun downloadPDF(
        url: String
    ): Single<ResponseBody> {
        return applicationApis.downloadFileByUrl(url)
    }

    fun getFaq():Single<FaqResponse>{
        return applicationApis.fetchFaq()
    }

    fun getHelpCenterUrl():Single<HelpCenterResponse>{
        return applicationApis.fetchHelpCenterURL()
    }

    fun logout(baId: String) : Single<BaseResponse>{
        return applicationApis.logout(baId)
    }

    fun getProfileDetail(request: FetchProfileRequest) : Single<SubscriberProfileListModel>{
        return applicationApis.fetchProfileDetail(request.baId)
    }

    fun getShemarooUrlData(url: String): Single<ShemarooSafeUrlResponse> {
        return applicationApis.getShemarooUrlData(url)
    }

    fun editAliasName(baId: String,aliasName: String): Single<BaseResponse>{
        return applicationApis.editAliasName(baId,aliasName)
    }

    fun switchAccount(targetBaId: String,dsn:String?,baId: String) : Single<SwitchAccountResponse>{
        return applicationApis.
        switchAccount(targetBaId, dsn,baId)
    }

    fun updateProfileImage(image: MultipartBody.Part, baId: String,profileId : String) : Single<ImageUploadResponse>{
        return applicationApis.uploadImage(image,baId, profileId)
    }

    fun fetchNextAndPreviousEpisodeDetails(episodeRequest: EpisodeRequest) : Single<NextPreviousEpisodeResponse>{
        return applicationApis.fetchNextAndPreviousEpisodeDetails(episodeRequest.profileId, episodeRequest.subscriberId, episodeRequest.episodeId)
    }

    fun getTVoDContent(sId: String, offset: Int, max : Int): Single<RecommendationResponse> {
        return applicationApis.getPurchaseListData(
            sId, offset, max
        )
    }

    fun removeProfileImage(sid: String,profileId : String) : Single<ImageUploadResponse>{
        return applicationApis.removeImage(sid, profileId)
    }

    fun languageGenreList(intent : String): Single<RecommendationResponse> {
        return applicationApis.getLanguageGenreList(
            intent.toLowerCase())
    }

    fun getSearchRails(intent: String?):Single<HomeResponse>{
        return applicationApis.getSearchRails(
            intent?.lowercase(Locale.getDefault()) ?: ""
        )
    }


    fun fetchEligiblePackListing(baid:String,subscriptionType:String): Single<EligiblePackResponse>{
        return applicationApis.fetchEligiblePack(baid,subscriptionType)
    }

    fun fetchEligiblepackForNonLoggedIn():Single<EligiblePackResponse>{
        return applicationApis.fetchEligiblepackForNonLoggedIn()
    }

    fun getPackList(partnerId : String?, baId: String): Single<PackListResponse>{
        return applicationApis.fetchPackList(partnerId, baId)
    }

    fun requestSubscriptionCreation(subscriptionCreationRequest: SubscriptionCreationRequest): Single<PurchasePackResponse>{
        return applicationApis.requestSubscriptionCreation(subscriptionCreationRequest)
    }

    fun requestSubscriptionModification(revoked: Boolean ,subscriptionModificationRequest: SubscriptionModificationRequest):Single<PurchasePackResponse>{
        return applicationApis.requestSubscriptionModification(revoked ,subscriptionModificationRequest)
    }

    fun requestSubscriptionCancellation(subscriptionCreationRequest: SubscriptionCancellationRequest):Single<CancellationResponse>{
        return applicationApis.requestSubscriptionCancellation(subscriptionCreationRequest)
    }

    fun freemiumRequestSubscriptionCancellation(subscriptionCreationRequest: FreemiumSubscriptionCancellationRequest):Single<FreemiumCancellationResponse>{
        return applicationApis.freemiumRequestSubscriptionCancellation(subscriptionCreationRequest)
    }


    fun requestSubscriptionRevokeCancellation(baId: String):Single<CancellationResponse>{
        return applicationApis.requestSubscriptionRevokeCancellation(baId)
    }


    fun fetchBalance(walletBalanceRequest: WalletBalanceRequest): Single<WalletBalanceResponse>{
        return applicationApis.fetchBalance(walletBalanceRequest)
    }

    fun refreshAccount(baId: String, dthStatus: String): Single<BaseResponse>{
        if (NON_DTH_USER.equals(dthStatus, true)
            || DTH_W_BINGE_NEW_USER.equals(dthStatus, true)
        )
            return applicationApis.refreshAccount(baId)
        else
            return applicationApis.refreshAccountForOldUser(baId)
    }

    fun getCurrentPack(sid: String, baId: String): Single<PurchasePackResponse> {
        return applicationApis.fetchCurrentPack(SubscriptionCreationRequest(sid =  sid, baid =  baId, packId = ""))
    }

    fun fetchFreemiumCurrentPlan(
        baId: String,
        accountId: String,
        freemiumUserType: String,
        userIsOnTickTick: Boolean?
    ): Single<PurchasePackResponse> {
        return applicationApis.fetchFreemiumCurrentPack(
            CurrentSubscriptionRequest(
                baId = baId,
                accountId = accountId,
                freemiumUserType,
                userIsOnTickTick
            )
        )
    }
    fun callWorkOrderFS(baId: String, workOrderRequest: WorkOrderRequest): Single<BaseResponse> {
        return applicationApis.callWorkOrderFS(baId, workOrderRequest)
    }

    fun initiateRecharge(sid: String,amount: String): Single<RechargeResponse>{
        return applicationApis.initiateRecharge(sid, amount)
    }

    fun initiateRecharge(sid: String): Single<RechargeResponse>{
        return applicationApis.initiateRecharge(sid)
    }

    fun rechargeNotification(sid: String, baId:String): Single<DunningResponse>{
        return applicationApis.rechargeNotifications(sid, baId)
    }

    fun getVootPlaybackData(request: VootRequest): Single<VootPlayebackResponse> {
        return applicationApis.getVootPlaybackData(request)
    }
    fun getVootKidsPlaybackData(request: VootRequest): Single<VootPlayebackResponse> {
        return applicationApis.getVootKidsPlaybackData(request)
    }

    fun changeSetting(baid: String, settingType: String) : Single<BaseResponse> {
        return applicationApis.changeSetting(baid, settingType)
    }


    fun fetchAddress() : Single<AddressResponse> {
        return applicationApis.fetchSubscriberAddress(true)     //pass isFreemium = true in case of freemium build
    }

    fun fetchSlots(slotsRequest: SlotsRequest) : Single<SlotsResponse> {
        return applicationApis.fetchSlots(true,slotsRequest)    //pass isFreemium = true in case of freemium build
    }

    fun confirmSlots(slotsRequest: SlotsRequest) : Single<BaseResponse> {
        return applicationApis.confirmSlot(slotsRequest)
    }

    fun requestSubscriptionReactivation(subscriptionCreationRequest: SubscriptionCreationRequest) : Single<PurchasePackResponse> {
        return applicationApis.requestSubscriptionReActivation(subscriptionCreationRequest)
    }

    fun getPrefLangGenre(
        isLoggedIn: Boolean,
        type: String
    ): Single<GenreListResponse> {
        var useCase = USER_PREFERRED_LANGUAGE_USE_CASE
        if(type == USER_PREFERRED_GENRE_TYPE )
            useCase = USER_PREFERRED_GENRE_USE_CASE
        var baseTaUrl =
            "${BuildConfig.SEARCH_BASE_URL}/ta-recommendation/api/v1/binge/guest/recommend/$type/$useCase?layout=LANDSCAPE"
        if (isLoggedIn) {
            baseTaUrl =
                "${BuildConfig.SEARCH_BASE_URL}/ta-recommendation/api/v1/binge/recommend/$type/$useCase?layout=LANDSCAPE"
        }
        return applicationApis.fetchPrefLangGenre(
            baseTaUrl,
            EmptyBody()
        )
    }

    fun fetchZee5Tag() : Single<Zee5TagResponse> {
        return applicationApis.fetchZee5Tag()
    }

    fun generateControlToken(controlRequest: ControlRequest): Single<ControlTokenResponse> {
        return applicationApis.generateControlToken(controlRequest)
    }
    fun getTransactionHistory(transactionHistoryRequest: TransactionHistoryRequest):Single<TransactionHistoryResponse>{
        return applicationApis.getTransactionHistory(transactionHistoryRequest)
    }

    fun getTransactionHistoryForNonDTHUser(baId: String):Single<TransactionHistoryResponse>{
        return applicationApis.getTransactionHistoryForNonDTHUser(baId)
    }

    fun fetchBoxsetDetails(vodId: String): Single<DetailsResponse> {
        return applicationApis.fetchBoxsetDetails(vodId)
    }
    fun callWatchViewAction(
        contentType: String,
        id: String,
        subscriberId: String,
        profileId: String
    ): Single<BaseResponse> {
        return applicationApis.callWatchViewAction(ViewActionRequest(
            contentId = id,
            contentType = contentType,
            profileId = profileId,
            subscriberId = subscriberId
        ))
    }

    fun contentPlaybackExpiry(vodId: String): Single<PlaybackExpiryResponse> {
        return applicationApis.contentPlaybackExpiry(vodId)
    }

    fun fetchPubnubHistory(url : String): Single<ResponseBody> {
        return applicationApis.fetchPubnubHistory(url)
    }

    fun switchAccountAtv(sId:String):Single<SwitchAccountResponse>{
        return applicationApis.switchAccountAtv(sId)
    }

    fun generateSonylivToken(): Single<ControlTokenResponse> {
        return applicationApis.generateSonylivToken()
    }

    fun fetchMarketingResponse():Single<MarketingResponse>{
        return applicationApis.fetchMarketingResponse()
    }

    fun activatePrime(primeActivationRequest: PrimeActivationRequest): Single<PrimeActivationResponse>{
        return applicationApis.activatePrime(primeActivationRequest)
    }
    fun fetchPrimePackList():Single<PrimePackListResponse>{
        return applicationApis.fetchPrimePackList()
    }

    fun fetchInterstitialPageResponse():Single<PrimeInterstitialResponse>{
        return applicationApis.fetchInterstitialPageResponse()
    }

    fun requestPrimeResume():Single<BaseResponse>{
        return applicationApis.requestPrimeResume()
    }

    fun shemarooMeAnalytics(body: ShemarooAnalyticsBody): Single<BaseResponse> {
        return applicationApis.shemarooMeAnalytics(body)
    }

    fun lionsgateAnalytics(body: LionsgateAnalyticsBody): Single<BaseResponse> {
        return applicationApis.lionsgateAnalytics(body)
    }

    fun createBingeAccount(sid:String):Single<NewBingeUserResponse>{
        return applicationApis.createBingeAccount(sid)
    }

    fun hitEpiconAnalytics(deviceId: String, body : PartnerContentAnalyticsRequest) : Single<BaseResponse>{
        return applicationApis.hitEpiconAnalytics(deviceId, body)
    }

    fun fetchTrialUpgradeDetails(upgradeTrialRequest: UpgradeTrialRequest) : Single<TrialUpgradeResponse>{
        return applicationApis.fetchUpgradePageDetails(upgradeTrialRequest)
    }

    fun generateAid(): Single<AnonymousResponse> {
        return applicationApis.generateAid(
            EmptyBody())
    }

    fun saveLanguages(
        saveLanguagesBody: SaveLanguageBody
    ): Single<BaseResponse> {
        return applicationApis.saveLanguages(
            saveLanguagesBody
        )
    }

    fun createNewBingeMobileUser(newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse> {
        if(newBingeUserRequest.isCreate)
            return applicationApis.createNewBingeMobileUser(newBingeUserRequest)
        else
            return applicationApis.createExistBingeMobileUser(newBingeUserRequest)

    }
    fun updateBingeMobileUser(newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse> {
        return applicationApis.createExistBingeMobileUser(newBingeUserRequest)
    }

    fun getSubIdList(rmn: String): Single<SubscriberIdListResponse> {
        return applicationApis.subscriberIdLookup(
            rmn
        )
    }

    fun saveParentalPin(
        parentalPinRequest: ParentalPinRequest,
        authToken: String
    ): Single<BaseResponse> {
        return applicationApis.saveParentalPin(parentalPinRequest, "bearer $authToken")
    }

    fun validateParentalPin(parentalPinRequest: ParentalPinRequest): Single<BaseResponse> {
        return applicationApis.validateParentalPin(parentalPinRequest)
    }

    fun checkForGuestUserPlaybackEligibility(): Single<UserPlaybackEligibilityResponse> {
        return applicationApis.checkForGuestUserPlaybackEligibility()
    }

    fun checkManagedAppEligibility(): Single<UserPlaybackEligibilityResponse> {
        return applicationApis.checkManagedAppEligibility()
    }

    fun getAgeRatings(): Single<AgeRatingsResponse> {
        return applicationApis.getAgeRatings()
    }

    fun updateAgeRating(updateAgeRatingRequest: UpdateAgeRatingRequest): Single<BaseResponse> {
        return applicationApis.updateAgeRating(
            updateAgeRatingRequest
        )
    }

    fun validateContentRating(validateContentRatingRequest: ValidateContentRatingRequest): Single<ValidateContentRatingResponse> {
        return applicationApis.validateContentRating(
            validateContentRatingRequest
        )
    }

    fun fetchCategories(): Single<LeftMenuResponse> {
        return applicationApis.fetchCategories()
    }

    fun fetchUserPreferredLanguage(
        body: preferredLanguageBody
    ): Single<UserPreferredLanguage> {
        return applicationApis.fetchUserPreferredLanguage(body)
    }


    fun packValidate(baId: String, packId: String): Single<PackValidationResponse> {
        return applicationApis.packValidate(baId, packId)
    }

    fun initiateJuspay(juspayInitiateRequest: HashMap<String, String>?): Single<JuspayInitiationResponse> {
        return if (juspayInitiateRequest == null)
            applicationApis.initiateJuspayForGuests()
        else
            applicationApis.initiateJuspay(juspayInitiateRequest)
    }

    fun addPack(addPackRequest: AddPackRequest): Single<AddPackResponse> {
        return applicationApis.addPack(addPackRequest)
    }

    fun modifyPack(addPackRequest: AddPackRequest): Single<AddPackResponse> {
        return applicationApis.modifyPack(addPackRequest)
    }

    fun proratedBalance(proRatedBalanceRequest: ProRatedBalanceRequest) : Single<ProRatedResponse>{
        return applicationApis.proratedBalance(proRatedBalanceRequest)
    }

    /*Search api changes*/
    fun getEpisodeSearchResponse(episodeSearchRequest: EpisodeSearchRequest):Single<SeriesListResponse>{
        return applicationApis.getEpiosdeSearchResponse(
            "${BuildConfig.SEARCH_BASE_URL}" +
                    "/search-connector/freemium/episode/data",
            episodeSearchRequest)
    }

    fun getSearchV2(searchRequest: SearchRequest) : Single<RecommendationResponse>{
        return applicationApis.getSearchV2(
            "${BuildConfig.SEARCH_BASE_URL}/search-connector/freemium/search/results",
            searchRequest)
    }

    /*Guest API changes*/
    fun getLastWatchEpisode(request: ContentIdAndTypeRequest): Single<EpisodeListingResponse> {
        if(!request.isLoggedIn)
            return applicationApis.lastWatchEpisode(
                "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/pub/guest/episode/listing/history"
                ,request
            )
        else
            return applicationApis.lastWatchEpisode(
                "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/episode/listing/histroy"
                ,request
            )
    }

    fun removeBingeList(request: ContentIdAndTypeRequest): Single<BaseResponse> {
        return applicationApis.removeBingeList(
            "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/subscriber/favourite/bulk/remove",
            request)
    }

    fun fetchLastWatch(request: ToggleFavouriteRequest, subscriptionType: PartnerPacks?): Single<IsFavouriteResponse> {
        if(!request.isLoggedIn)
            return applicationApis.fetchLastWatchDetails(
                "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/pub/guest/last-watch",
                request,
                "freemium"
            )
        else{
            return applicationApis.fetchLastWatchDetails(
                "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/api/v1/last-watch",
                request, request.subscriptionType

            )
        }
    }


    fun getCWRails(request: CWRequest): Single<RecommendationResponse> {
        if(!request.isLoggedIn)
            return applicationApis.getContinueWatchingGuestData(
                "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/pub/guest/recently/watched",
                request.cw,
                request.seeAll,
                request.uniqueId,
                request.pagingState,
                request.offset,
                request.provider
            )
        else
            return applicationApis.getContinueWatchingData(
                "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/recently/watched",
                request.cw,
                request.seeAll,
                request.profileId,
                request.subscriberId,
                request.pagingState,
                request.offset,
                request.provider
            )
    }

    fun getFavouritesList(request: WatchRequest): Single<RecommendationResponse> {
        if(request.isForceRefresh){
            return applicationApis.getFavouritesListForceFully(
                "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/subscriber/favourite/listing",
                request.profileId,request.subscriberId,
                request.pagingState,request.offset)
        }
        return applicationApis.getFavouritesList(
            "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/subscriber/favourite/listing",
            request.profileId,request.subscriberId,
            request.pagingState,request.offset)
    }

    fun getCWAction(request: CWRequest): Single<BaseResponse> {
        if(!request.isLoggedIn)
            return applicationApis.actionWatchContent("${BuildConfig.SEARCH_BASE_URL}/action-listener/pub/api/guest/watching", request)
        else
            return applicationApis.actionWatchContent("${BuildConfig.SEARCH_BASE_URL}/action-listener/api/watching", request)
    }

    fun toggleFavourite(request: ToggleFavouriteRequest): Single<IsFavouriteResponse> {
        return applicationApis.toggleFavourite(
            "${BuildConfig.SEARCH_BASE_URL}/action-data-provider/subscriber/favourite",
            request.profileId,
            request.subscriberId,
            request.contentId,
            request.contentType)
    }

    fun callFavouriteLearnAction(contentType: String,
                                 id: String,
                                 showType: String,
                                 provider: String,
                                 refUsecase : String): Single<BaseResponse> {
        return applicationApis.callFavouriteLearnAction(
            "${BuildConfig.SEARCH_BASE_URL}/ta-recommendation/api/v1/binge/learn/FAVOURITE/${contentType}/${id}/${showType}",
            provider,
            EmptyBody(),
            refUsecase
        )
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

        var baseTaUrl =
            "${BuildConfig.SEARCH_BASE_URL}/ta-recommendation/api/v1/binge/guest/learn/$learnActionType/$contentType/$id/$showType"
        if (isLoggedIn) {
            baseTaUrl =
                "${BuildConfig.SEARCH_BASE_URL}/ta-recommendation/api/v1/binge/learn/$learnActionType/$contentType/$id/$showType"
        }
        return applicationApis.callLearnAction(
            baseTaUrl,
            provider,
            EmptyBody(),
            refUsecase
            )
    }

    fun getTARails(request: TARequest): Single<RecommendationResponse> {
        var baseTaUrl = "${BuildConfig.SEARCH_BASE_URL}/ta-recommendation/api/v1/binge/guest/recommend/${request.placeHolder}"
        if (request.isLoggedIn) {
            baseTaUrl = "${BuildConfig.SEARCH_BASE_URL}/ta-recommendation/api/v1/binge/recommend/${request.placeHolder}"
        }
        when {
            request.subPage -> return applicationApis.getTASubPageRecommendation(
                baseTaUrl,
                request.layoutType,
                request.provider,
                request.pageLimit,
                request.body
            )

            request.languageGenre -> return applicationApis.getTALanguageGenreRecommendation(
                baseTaUrl,
                request.layoutType,
                request.pageLimit,
                request.filterLanguage,
                request.subGenre,
                request.body,
                request.freeToggle
            )
            request.isRelated -> return applicationApis.getTARecommendation(
                baseTaUrl,
                request.layoutType,
                request.id,
                request.contentType,
                request.showType,
                request.provider,
                request.pageLimit,
                request.body
            )

            request.pageType == null -> return applicationApis.getRecommendationsForUseCase(
                baseTaUrl,
                request.layoutType,
                request.pageLimit,
                request.body
            )
            else -> return applicationApis.getTAHeroBanner(
                baseTaUrl,
                request.pageType,
                request.pageLimit,
                request.body
            )
        }
    }


    fun getRecommendation(request: DetailRequest): Single<RecommendationResponse> {
        val recommendationUrl = "${BuildConfig.SEARCH_BASE_URL}/search-connector/binge/recommendations/${request.id}/${request.detailsType}"
        return applicationApis.getRecommendation(recommendationUrl, request.max, request.from)
    }

    fun payByDTHBalance(payByDTHBalanceRequest: PayByDTHBalanceRequest): Single<BaseResponse> =
        applicationApis.payByDTHBalance(payByDTHBalanceRequest)

    fun fetchCampaign(baId:String): Single<FtvCampaignResponse> {
        return applicationApis.fetchCampaign(
            baId
        )
    }

    fun fetchPaymentStatus(paymentStatusRequest:PaymentStatusRequest): Single<PaymentStatusResponse> {
        return applicationApis.fetchPaymentStatus(paymentStatusRequest)
    }

    fun getInvoiceDownload(invoiceDownloadRequest: InvoiceDownloadRequest,baId: String):Single<InvoiceDownloadResponse>
    {
        return applicationApis.getInvoiceDownloadLink(invoiceDownloadRequest,baId)
    }

    fun fetchMixpanelUniqueId(referenceId : String): Single<MixpanelUniqueResponse>{
        return applicationApis.fetchMixpanelUniqueId(referenceId)
    }

    fun getHoichoiPlaybackData(request: HoichoiRequest): Single<HoichoiPlayebackResponse> {
        return applicationApis.getHoichoiPlaybackData(/*request*/)
    }

    fun migrateUser(): Single<MigrateUserResponse> {
        return applicationApis.migrateUser()
    }

    fun migrateUserInfo(cartId: String): Single<MigrateUserTickTickResponse> {
        return applicationApis.migrateUserInfo(cartId,"8c18b54a457711edb8780242ac120002")
    }

    fun removeAllDevice(sid: String): Single<BaseResponse> {
        return applicationApis.removeAllDevices(sid, EmptyBody())
    }

    fun getChaupalUrlData(contenId: String, contentType: String): Single<ChaupalUrlResponse> {
        return applicationApis.getChaupalUrlData(contenId, contentType, EmptyBody())
    }

    fun getPlanetMarathiPlayUrl(contenId: String, contentType: String): Single<ChaupalUrlResponse> {
        return applicationApis.getPlanetMarathiPlayUrl(contenId, contentType, EmptyBody())
    }

    fun setAppRatingEligibility(requestBody: SetAppRatingRequest): Single<AppRatingResponse> {
        return applicationApis.setAppRatingEligibility(requestBody)
    }

    fun getAppRatingEligibility(requestBody: GetAppRatingRequest): Single<AppRatingResponse> {
        return applicationApis.getAppRatingEligibility(requestBody)
    }
    fun getManagedAppsUrl(
        baid: String? = "",
        journeySource: String? = "",
        journeySourceRefId: String? = "",
        mixpanelId: String,
        source: String,
        rmn: String,
        origin: String,
        subscriptionType: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse> {
        return applicationApis.getManagedAppsUrl(
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
    fun getManagedAppSummeryUrl(
        baid: String,
        journeySource: String? = "",
        journeySourceRefId: String? = "",
        mixpanelId: String,
        cartId: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse> {
        return applicationApis.getManagedAppSummeryUrl(
            baid,
            journeySource,
            journeySourceRefId,
            mixpanelId,
            cartId,
            appsFlyerId,
            appVersionName
        )
    }
    fun getManagedAppRefreshToken(
        baid: String,
        journeySource: String? = "",
        journeySourceRefId: String? = "",
        mixpanelId: String,
        cartId: String,
        appsFlyerId: String,
        appVersionName: String
    ): Single<ManagedAppResponse> {
        return applicationApis.getManagedAppRefreshToken(
            baid,
            journeySource,
            journeySourceRefId,
            mixpanelId,
            cartId,
            appsFlyerId,
            appVersionName
        )
    }

    fun planetMarathiAnalytics(
        requestBody: PlanetMarathiAnalyticsRequest,
        contentType: String
    ): Single<BaseResponse> {
        return applicationApis.planetMarathiAnalytics(requestBody, contentType)
    }


    fun fetchGameFavs(request: WatchRequest): Single<RecommendationResponse>{
        return applicationApis.fetchGameFavs(
            request.profileId,request.subscriberId,
            request.pagingState, request.offset)
    }

    fun addGameToFav(
        profileId: String,
        sid: String,
        contentId: String,
        contentType: String
    ): Single<GameFavResponse> {
        return applicationApis.addGameToFav(profileId, sid, contentId, contentType)
    }

    fun generateVootPwaToken(request: HashMap<String, String>) : Single<VootPwaResponse>{
        return applicationApis.generateVootPwaToken(request)
    }

    fun fetchLionsGateToken(request : LionsgateRequest) : Single<LionsGateResponse>{
        return applicationApis.fetchLionsGateToken(request)
    }

    fun fetchTAHomeHierarchy(pageType: String, packName:String, isLoggedIn: Boolean, sId: String) : Single<HierarchyResponse>{
        var url = "${BuildConfig.BASE_URL}/ta-recommendation/api/v1/binge/guest/hierarchy/${pageType}"
        if (isLoggedIn) {
            url = "${BuildConfig.BASE_URL}/ta-recommendation/api/v1/binge/hierarchy/${pageType}"
        }
        return applicationApis.homeTAHierarchy(url, packName = packName, subscriberId = sId)
    }

    fun fetchVRHomeHierarchy(pageType: String, packName: String) : Single<HierarchyResponse>{
        return applicationApis.homeVRHierarchy(pageType, packName)
    }


    fun getEditorialRailData(railId : String) : Single<RecommendationResponse>{
        return applicationApis.getRailData(railId)
    }

    fun fetchGenreRailData() : Single<RecommendationResponse> {
        return applicationApis.fetchGenreRailData("binge")
    }

    fun fetchLanguageRailData() : Single<RecommendationResponse> {
        return applicationApis.fetchLanguageRailData("binge")
    }

    fun fetchGenericPartnerDRMAPI(
        providerContentId: String?,
        provider: String
    ): Single<GenericPartnerDRMResponse> {
        return applicationApis.fetchGenericPartnerDRMAPI(
            GenericDRMRequest(providerContentId?:"", provider)
        )
    }


}