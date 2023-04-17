package com.tatasky.binge.data.networking

import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.epicon.PartnerContentAnalyticsRequest
import com.tatasky.binge.hoichoi.HoichoiPlayebackResponse
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
import retrofit2.http.*
import javax.inject.Singleton


@Singleton
interface ApplicationApis {


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-service/api/v1/akamai/getToken?partner=hoichoi")
    fun getHoichoiPlaybackData(/*@Body request: HoichoiRequest*/): Single<HoichoiPlayebackResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("homescreen-client/pub/api/v1/page/{pageType}")
    fun getHomePage(
        @Path("pageType") pageType: String,
        @Query("pageLimit") pageLimit: String,
        @Query("pageOffset") pageOffset: String,
        @Query("Subscribed") subscribed: Boolean,
        @Query("UnSubscribed") unsubscribed: Boolean,
        @Query("packName") packName: String
    ): Single<HomeResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @POST
    fun getTARecommendation(
        @Url taUrl: String,
        @Query("layout") layoutType: String,
        @Query("id") id: String,
        @Query("contentType") contentType: String,
        @Query("showType") showType: String,
        @Query("provider") provider: String,
        @Query("max") max: String,
        @Body body: EmptyBody
    ): Single<RecommendationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @POST
    fun getTALanguageGenreRecommendation(
        @Url taUrl: String,
        @Query("layout") layoutType: String,
        @Query("max") max: String,
        @Header("filterLanguage") filterLanguage: String,
        @Header("subGenre") subGenre: String,
        @Body body: EmptyBody,
        @Header("freeToggle") freeToggle: Boolean?
    ): Single<RecommendationResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @POST
    fun getTAHeroBanner(
        @Url taUrl: String,
        @Header("pageType") pagetype: String?,
        @Query("max") max: String,
        @Body body: EmptyBody
    ): Single<RecommendationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @POST
    fun getRecommendationsForUseCase(
        @Url taUrl: String,
        @Query("layout") layoutType: String,
        @Query("max") max: String,
        @Body body: EmptyBody
    ): Single<RecommendationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_TVOD")
    @GET("content-detail/api/v1/monetization/tvod/subscriber/list/{SID}")
    fun getPurchaseListData(
        @Path("SID") sid: String,
        @Query("offset") offset: Int,
        @Query("max") max: Int
    ): Single<RecommendationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("homescreen-client/pub/api/v3/pages/BINGE_ANYWHERE")//changed to v3 for Sports in place of Kids
    fun getLeftMenuItem(): Single<LeftMenuResponse>



    //    @GET("utility-application/config/anywhere")
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_CONFIG")
    @GET("binge-mobile-config/pub/v1/api/config/binge/mobile")
    fun getConfig(
//        @Url url: String
    ): Single<ConfigResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET//("recommendation-api/api/recommend/content/{vod_Id}/{content_type}")
    fun getRecommendation(
        @Url url: String,
        @Query("max") max: Int,
        @Query("from") from: Int
    ): Single<RecommendationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_DONGLE:$HEADER_TYPE_VR_WITH_AUTH")
    @GET("content-subscriber-detail/api/series/list/{vod_Id}")
    fun getSeriesList(
        @Path("vod_Id") vodId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("profileId") profileId: String?,
        @Query("subscriberId") subscriberId: String,
        @Query("isLastWatch") isLastWatch: Boolean,
        @Query("isAutoScroll") isAutoScroll: Boolean
    ): Single<SeriesListResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VR_WITH_AUTH")
    @GET("content-subscriber-detail/api/content/info/{detailsType}/{Id}")
    fun getBrandDetailsWithCW(
        @Path("detailsType") detailsType: String,
        @Path("Id") id: String,
        @Query("profileId") profileId: String?,
        @Query("subscriberId") subscriberId: String?
    ): Single<DetailsResponse>

    //action-data-provider/view/history
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_CW")
    @GET
    fun getContinueWatchingData(
        @Url url: String,
        @Query("cw") cw: Boolean,
        @Query("seeAll") seeAll: Boolean,
        @Query("profileId") profileId: String,
        @Query("subscriberId") subscriberId: String,
        @Query("pagingState") pagingState: String?,
        @Query("offSet") offset: Int,
        @Query("provider") provider: String?
    ): Single<RecommendationResponse>
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_CW")
    @GET
    fun getContinueWatchingGuestData(
        @Url url: String,
        @Query("cw") cw: Boolean,
        @Query("seeAll") seeAll: Boolean,
        @Query("uniqueId") uniqueId: String,
        @Query("pagingState") pagingState: String?,
        @Query("offSet") offset: Int,
        @Query("provider") provider: String?
    ): Single<RecommendationResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VR_WITH_AUTH")
    @GET
    fun getFavouritesList(
        @Url url: String,
        @Query("profileId") profileId: String,
        @Query("subscriberId") subscriberId: String,
        @Query("pagingState") pagingState: String?,
        @Query("offSet") offset: Int
    ): Single<RecommendationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VR_WITH_AUTH_NO_CACHE")
    @GET
    fun getFavouritesListForceFully(
        @Url url: String,
        @Query("profileId") profileId: String,
        @Query("subscriberId") subscriberId: String,
        @Query("pagingState") pagingState: String?,
        @Query("offSet") offset: Int
    ): Single<RecommendationResponse>


    //Changed in DRP
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("homescreen-client/pub/api/v4/rail/seeAll")
    fun getRailData(
        @Query("id") railId: String,
        @Query("limit") pageLimit: Int,
        @Query("Offset") pageOffset: Int
    ): Single<RecommendationResponse>

    @Streaming
    @GET
    fun downloadFileByUrl(@Url fileUrl: String): Single<ResponseBody>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_CW")
    @POST
    fun actionWatchContent(
        @Url url: String,
        @Body cwRequest: CWRequest): Single<BaseResponse>

    @GET("binge-mobile-services/pub/api/v2/help/tsmore")
    fun fetchFaq():Single<FaqResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/logout/{baId}")
    fun logout(@Path("baId") baId: String): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VR_WITH_AUTH_NO_CACHE")
    @GET
    fun toggleFavourite(
        @Url url: String,
        @Query("profileId") profileId: String,
        @Query("subscriberId") subscriberId: String,
        @Query("contentId") contentId: String,
        @Query("contentType") contentType: String
    ): Single<IsFavouriteResponse>

    @GET
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_NO_HEADER")
    fun getShemarooUrlData(@Url url: String): Single<ShemarooSafeUrlResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VR_WITH_AUTH")
    @GET("content-subscriber-detail/api/content/episode/info")
    fun fetchNextAndPreviousEpisodeDetails(
        @Query("profileId") profileId: String?,
        @Query("subscriberId") subscriberId: String?,
        @Query("id") episodeId: String
    ): Single<NextPreviousEpisodeResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("homescreen-client/pub/api/v2/search/{intent}?platform=BINGE")
    fun getLanguageGenreList(
        @Path("intent") intent: String
    ): Single<RecommendationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("/homescreen-client/pub/api/v3/search/landing/?platform=BINGE")
    fun getSearchRails(
        @Query("type") intent: String
    ): Single<HomeResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST
    fun getEpiosdeSearchResponse(
        @Url url: String,
        @Body episodeSearchRequest: EpisodeSearchRequest
    ) : Single<SeriesListResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v1/subscription/packs/{baId}")
    fun fetchEligiblePack(
        @Path("baId") baId: String,
        @Header("subscriptionType") subscriptionType:String
    ):Single<EligiblePackResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/pub/api/v3/subscription/packs")
    fun fetchEligiblepackForNonLoggedIn():Single<EligiblePackResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @POST("binge-mobile-services/api/v2/login/user")
    fun loginBingeUser(@Body loginRequest: LoginRequest): Single<NewBingeUserResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @POST("binge-mobile-services/api/v2/create/user/new/journey")
    fun createNewBingeUser(@Body newBingeUserRequest: NewBingeUserRequest): Single<NewBingeUserResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v3/subscriber/fetch/profile/{baId}")
    fun fetchProfileDetail(@Path("baId") baId: String): Single<SubscriberProfileListModel>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @GET("binge-mobile-services/pub/api/v3/auth/subscriber/{sid}/otp")
    fun generateOtpSubscriberId(
        @Path("sid") sid: String,
        @Query("isPassword") isPassword: Boolean
    ): Single<GetOtpResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_HELP_CENTER_URL")
    @GET("binge-mobile-services/api/v4/helpCenter/redirection/url")
    fun fetchHelpCenterURL() : Single<HelpCenterResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @GET("binge-mobile-services/pub/api/v2/auth/rmn/{rmn}/otp")
    fun generateOtpLogin(
        @Path("rmn") rmn: String
    ): Single<GetOtpResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
//    @POST("binge-mobile-services/pub/api/v1/user/authentication/generateOTP")
    @POST("binge-mobile-services/pub/api/v1/user/authentication/generateOTP")
    fun generateOtpGuestLogin(
        @Header("mobileNumber") mobileNumber: String
    ): Single<GetOtpGuestLoginResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
//    @POST("binge-mobile-services/pub/api/v1/user/authentication/validateOTP")
    @POST("binge-mobile-services/pub/api/v1/user/authentication/validateOTP")
    fun validateOtpGuestLogin(
        @Body validateOtpGuestLoginRequest: ValidateOtpGuestLoginRequest
    ): Single<ValidateOTPResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
//    @GET("binge-mobile-services/pub/api/v1/user/login/rmn")
    @GET("binge-mobile-services/pub/api/v1/user/login/rmn")
    fun getPreviouslyUsedMobileNumbers(): Single<PreviouslyUsedMobileNumbersResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @POST("binge-mobile-services/pub/api/v2/auth/validate/otp")
    fun validateOtp(@Body otpLogin: LoginDTO): Single<ValidateOTPResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @POST("binge-mobile-services/pub/api/v2/auth/subscriber/validate/otp")
    fun validateOtpForSID(@Body otpLogin: LoginDTO): Single<ValidateOTPResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @POST("binge-mobile-services/pub/api/v2/auth/validate/pwd")
    fun validatePassword(@Body loginDTO: LoginDTO): Single<ValidateOTPResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA","$KEY_HEADER_TYPE:$HEADER_TYPE_NO_CACHE")
    @GET("binge-mobile-services/api/v2/migration/account/details/sid/{sid}")
    fun baIdLookup(@Path("sid") rmn: String): Single<BAIdListResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VR_WITH_AUTH")
    @PUT("binge-mobile-services/api/v2/subscribers/password/{sid}/change")
    fun changePassword(
        @Path("sid") sid: String,
        @Body changePasswordRequest: ChangePasswordRequest
    ): Single<ForgetPasswordResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VR_VALIDATE_OTP")
    @POST("binge-mobile-services/pub/api/v2/subscribers/forgot/{sid}/password")
    fun forgetPasswordRequest(
        @Path("sid") sid: String,
        @Body forgetPasswordRequest: ForgotPasswordRequest
    ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VR_VALIDATE_OTP")
    @POST("binge-mobile-services/pub/api/v2/subscribers/forgot/{sid}/password")
    fun initiateForgetPassword(
        @Path("sid") sid: String
    ): Single<BaseResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_DEVICE_MANAGEMENT")
    @POST("binge-mobile-services/api/v2/subscription/transaction/history")
    fun getTransactionHistory(
        @Body transactionHistoryRequest: TransactionHistoryRequest
    ) : Single<TransactionHistoryResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v1/subscription/transaction/history")
    fun getTransactionHistoryForNonDTHUser(
        @Header("baId") baId: String
    ) : Single<TransactionHistoryResponse>

    @PUT("binge-mobile-services/pub/api/v2/subscribers/{sid}/change-password")
    fun changePasswordBeforeLogin(
        @Path("sid") sid: String,
        @Body changePasswordRequest: ChangePasswordRequest
    ): Single<ForgetPasswordResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_DEVICE_MANAGEMENT")
    @DELETE("binge-mobile-services/api/v2/remove/devices/{baId}/{deviceId}")
    fun removeDevice(
        @Path("baId") subscriberId: String,
        @Path("deviceId") deviceId: String
    ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_DEVICE_MANAGEMENT")
    @GET("binge-mobile-services/api/v2/subscriber/devices/{baId}")
    fun getDeviceList(@Path("baId") baId: String): Single<DeviceListResponse>

    @Multipart
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_SID_AUTH")
    @POST("binge-mobile-services/api/v3/subscribers/{sId}/upload/image")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @Path("sId") baId: String,
        @Query("profileId") profileId: String
    ): Single<ImageUploadResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_ALIAS")
    @POST("binge-mobile-services/api/v2/subscriber/{baId}/update/alias")
    fun editAliasName(
        @Path("baId") baId: String,
        @Query("aliasName") alias: String
    ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_SA")
    @POST("binge-mobile-services/api/v2/switch/account/{baId}")
    fun switchAccount(
        @Header("targetBaId") targetBaId: String,
        @Header("dsn") dsn: String?,
        @Path("baId") baId: String
    ): Single<SwitchAccountResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/subscriber/update/email")
    fun updateEmail(@Body updateEmailRequest: UpdateEmailRequest): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v3/subscriber/update/email/name")
    fun updateEmailAndName(
        @Body updateEmailRequest: UpdateEmailRequest,
        @Header(KEY_HEADER_CALLED_FROM) calledFrom: String,
    ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v3/subscriber/update/email/name")
    fun updateEmailWithoutName(
        @Body updateEmailRequest: UpdateEmailRequestWithoutName,
        @Header(KEY_HEADER_CALLED_FROM) calledFrom: String,
    ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_SID_AUTH")
    @DELETE("binge-mobile-services/api/v3/subscribers/{sid}/delete/image")
    fun removeImage(
        @Path("sid") sid: String,
        @Query("profileId") profileId: String
    ): Single<ImageUploadResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v5/packs/{baId}")
    fun fetchPackList(@Header("partnerId") partnerId : String?, @Path("baId") baId: String): Single<PackListResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/binge/mobile/subscription")
    fun requestSubscriptionCreation(@Body subscriptionCreationRequest: SubscriptionCreationRequest): Single<PurchasePackResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/binge/reactivate/subscription")
    fun requestSubscriptionReActivation(@Body subscriptionCreationRequest: SubscriptionCreationRequest): Single<PurchasePackResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/binge/modify/subscription")
    fun requestSubscriptionModification(@Header("revoked") revoked : Boolean, @Body subscriptionModificationRequest: SubscriptionModificationRequest): Single<PurchasePackResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/binge/subscription/cancel")
    fun requestSubscriptionCancellation(@Body subscriptionCreationRequest: SubscriptionCancellationRequest): Single<CancellationResponse>

//    curl --location --request POST 'https://tatasky-qa-tsmore-kong.videoready.tv/binge-mobile-services/api/v1/subscription/cancel' \
//    --header 'platform: ios' \
//    --header 'Authorization: X3AZfX6t7WpqICPtjgsUt1CkXtgRr2uw' \
//    --header 'Content-Type: application/json' \
//    --data-raw '{
//    "accountId":"24r72346",
//    "primeCancellation":true,
//    "bingeCancellation":true
//}'

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v1/subscription/cancel")
    fun freemiumRequestSubscriptionCancellation(@Body subscriptionCreationRequest: FreemiumSubscriptionCancellationRequest): Single<FreemiumCancellationResponse>



    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/revoke/subscription/{baId}")
    fun requestSubscriptionRevokeCancellation(@Path("baId") baId: String): Single<CancellationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v3/accountInfo/getBalance")
    fun fetchBalance(@Body walletBalanceRequest: WalletBalanceRequest): Single<WalletBalanceResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v1/accountRefresh")
    fun refreshAccount(@Header("baId") baId: String): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v3/binge/mobile/current/subscription")
    fun fetchCurrentPack(@Body request: SubscriptionCreationRequest): Single<PurchasePackResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_FREEMIUM_ACCOUNT_DETAILS")
    @POST("binge-mobile-services/api/v1/subscription/current")
    fun fetchFreemiumCurrentPack(@Body request: CurrentSubscriptionRequest): Single<PurchasePackResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @POST
    fun callFavouriteLearnAction(
        @Url taUrl: String,
        @Header("provider") provider: String,
        @Body body: EmptyBody,
        @Query("refUsecase") refUsecase : String
    ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @POST
    fun callLearnAction(
        @Url taUrl: String,
        @Header("provider") provider: String,
        @Body body: EmptyBody,
        @Query("refUsecase") refUsecase : String
    ): Single<BaseResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v2/subscribers/{sid}/recharge/{amountOfRecharge}/amount")
    fun initiateRecharge(@Path("sid") sid: String, @Path("amountOfRecharge") amount: String): Single<RechargeResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v2/subscribers/{sid}/recharge/")
    fun initiateRecharge(@Path("sid") sid: String): Single<RechargeResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/subscribers/{sid}/recharge/notification")
    fun rechargeNotifications(@Path("sid") sid: String, @Query("baId") baId: String): Single<DunningResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/create/wo")
    fun callWorkOrderFS(@Query("baId") baId: String, @Body woRequest: WorkOrderRequest): Single<BaseResponse>

    @FormUrlEncoded
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("voot-select-playback-api/voot/freemium/fetch/token")
    fun generateVootPwaToken(@FieldMap request: Map<String, String>) : Single<VootPwaResponse>


    /*Added v1 for L3 support changes in voot api*/
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA", "$KEY_HEADER_TYPE:$HEADER_TYPE_VOOT_SELECT_UNIQUE_ID")
    @POST("voot-select-playback-api/voot/v1/playback")
    fun getVootPlaybackData(@Body request: VootRequest): Single<VootPlayebackResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA", "$KEY_HEADER_TYPE:$HEADER_TYPE_VOOT_KIDS_UNIQUE_ID")
    @POST("voot-kids-playback-api/s/partner/playback")
    fun getVootKidsPlaybackData(@Body request: VootRequest): Single<VootPlayebackResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA", "$KEY_HEADER_TYPE:$HEADER_TYPE_NO_CACHE")
    @GET("binge-mobile-services/api/v2/change/notificationTrailer/status")
    fun changeSetting(@Query("baId") baid: String, @Query("type") settingType: String) : Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v2/subscriber/profile/address")
    fun fetchSubscriberAddress(@Header("isFreemium") isFreemium: Boolean):Single<AddressResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/get/slot")
    fun fetchSlots(@Header("isFreemium") isFreemium: Boolean, @Body slotsRequest: SlotsRequest):Single<SlotsResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/confirm/slot")
    fun confirmSlot(@Body slotsRequest: SlotsRequest):Single<BaseResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/switch/account/atv/{baId}")
    fun switchAccountAtv(
        @Path("baId") baId: String
    ): Single<SwitchAccountResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @POST
    fun fetchPrefLangGenre(
        @Url taUrl: String,
        @Body body: EmptyBody
    ): Single<GenreListResponse>

    @Headers(
        "$KEY_HEADER_TYPE:$HEADER_TYPE_BA",
        "$KEY_HEADER_TYPE:$HEADER_TYPE_ZEE5_UNIQUE_ID",
        "$KEY_HEADER_TYPE:$HEADER_TYPE_BAID"
    )
    @POST("zee5-playback-api/v2/tag/fetch")
    fun fetchZee5Tag() : Single<Zee5TagResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_CONTROL_CHANGE")
    @POST("auth-service/v1/oauth/token-service/token")
    fun generateControlToken(@Body controlRequest: ControlRequest): Single<ControlTokenResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_CW")
    @POST
    fun fetchLastWatchDetails(
        @Url url:String,
        @Body lastWatchRequest: ToggleFavouriteRequest,
        @Header("subscriptionType") subscriptionType: String) : Single<IsFavouriteResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("content-detail/pub/api/v1/box-subset/vod/{vodId}")
    fun fetchBoxsetDetails(@Path("vodId") vodId: String): Single<DetailsResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_DONGLE")
    @POST("la-proxy-app/ts/composer/learnAction")
    fun callWatchViewAction(@Body lastWatchRequest: ViewActionRequest) : Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("/content-detail/api/v1/tvod/digital/playback/expiry/{vodId}")
    fun contentPlaybackExpiry(@Path("vodId") vodId: String?): Single<PlaybackExpiryResponse>

    @GET
    fun fetchPubnubHistory(@Url url: String) : Single<ResponseBody>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_CW")
    @POST
    fun lastWatchEpisode(
        @Url url: String,
        @Body contentIdAndTypeRequest: ContentIdAndTypeRequest): Single<EpisodeListingResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @POST
    fun getTASubPageRecommendation(
        @Url taUrl: String,
        @Query("layout") layoutType: String,
        @Header("subPage") provider: String,
        @Query("max") max: String,
        @Body body: EmptyBody
    ): Single<RecommendationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("zee5-playback-api/sony/fetch/token")
    fun generateSonylivToken(): Single<ControlTokenResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_ALIAS" , "$KEY_HEADER_TYPE:$HEADER_TYPE_NO_CACHE")
    @GET("binge-mobile-config/pub/v1/api/screen")
    fun fetchMarketingResponse():Single<MarketingResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v3/mobile/activate/prime")
    fun activatePrime(@Body primeActivationRequest: PrimeActivationRequest): Single<PrimeActivationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v1/prime/packs")
    fun fetchPrimePackList(): Single<PrimePackListResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-config/prime/interstital/screen")
    fun fetchInterstitialPageResponse(): Single<PrimeInterstitialResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-service/api/s/prime/resendSMS")
    fun requestPrimeResume(): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_CREATE")
    @POST("binge-mobile-services/api/v2/create/cancel/subscriber/{sid}/account")
    fun createBingeAccount(@Path("sid") sid: String): Single<NewBingeUserResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("/tnap/thirdparty/watch/shemaroo/analytics")
    fun shemarooMeAnalytics(@Body body : ShemarooAnalyticsBody): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("/partner-content-analytics/lionsgate-api/analytic/event-data")
    fun lionsgateAnalytics(@Body body : LionsgateAnalyticsBody): Single<BaseResponse>

    /**
     * This API is only used for EpicON and Docubay content playback analytics
     */
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("partner-content-analytics/epicon/analytics")
    fun hitEpiconAnalytics(
        @Header("Device-ID") dsn: String,
        @Body partnerContentAnalyticsRequest: PartnerContentAnalyticsRequest
    ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v2/subscriber/upgrade/transition/details")
    fun fetchUpgradePageDetails(@Body upgradeTrialRequest: UpgradeTrialRequest) : Single<TrialUpgradeResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/pub/api/v1/user/guest/register")
    fun generateAid(@Body body : EmptyBody): Single<AnonymousResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/pub/api/v1/user/login/preferredLanguage")
    fun saveLanguages(@Body saveLanguagesBody: SaveLanguageBody)
            : Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @POST("/binge-mobile-services/api/v3/create/new/user")
    fun createNewBingeMobileUser(
        @Body newBingeUserRequest: NewBingeUserRequest
    ): Single<NewBingeUserResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @POST("binge-mobile-services/api/v3/update/exist/user")
    fun createExistBingeMobileUser(
        @Body newBingeUserRequest: NewBingeUserRequest
    ): Single<NewBingeUserResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA_LOGIN")
    @GET("binge-mobile-services/api/v3/subscriber/details")
    fun subscriberIdLookup(
        @Header("mobileNumber") mobileNumber: String
    ): Single<SubscriberIdListResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_PARENTAL_CONTROL")
    @POST("binge-mobile-services/api/v1/save/parental/pin")
    fun saveParentalPin(
        @Body parentalPinRequest: ParentalPinRequest,
        @Header(KEY_HEADER_AUTH) authToken: String
    ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v1/validate/parental/pin")
    fun validateParentalPin(@Body parentalPinRequest: ParentalPinRequest): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/pub/api/v1/user/guest/playback/check-eligibility")
    fun checkForGuestUserPlaybackEligibility(): Single<UserPlaybackEligibilityResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/pub/api/v1/manage/app/fallback")
    fun checkManagedAppEligibility(): Single<UserPlaybackEligibilityResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v1/age_ratings")
    fun getAgeRatings(): Single<AgeRatingsResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("/binge-mobile-services/api/v1/update/age_rating")
    fun updateAgeRating(
        @Body updateAgeRatingRequest: UpdateAgeRatingRequest
    ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("/binge-mobile-services/api/v1/validate/content_rating")
    fun validateContentRating(
        @Body validateContentRatingRequest: ValidateContentRatingRequest
    ): Single<ValidateContentRatingResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("/homescreen-client/pub/api/v2/categoriesPage/BINGE_ANYWHERE")
    fun fetchCategories(): Single<LeftMenuResponse>


    //TODO : SPORTS
//    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
//    @GET("/homescreen-client/pub/api/v3/categoriesPage/BINGE_ANYWHERE")//changed to v3 for Sports in place of Kids
//    fun fetchCategories(): Single<LeftMenuResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v1/user/profile")
    fun fetchUserPreferredLanguage(
        @Body preferredLanguageBody: preferredLanguageBody
    ): Single<UserPreferredLanguage>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST
    fun removeBingeList(
        @Url url: String,
        @Body request: ContentIdAndTypeRequest): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("/binge-mobile-services/api/pg/customer/initiate/payload")
    fun initiateJuspay(
        @HeaderMap juspayInitiateRequest: HashMap<String, String>
    ): Single<JuspayInitiationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/pub/api/pg/v3/customer/initiate/payload")
    fun initiateJuspayForGuests(): Single<JuspayInitiationResponse>


//    curl --location --request GET 'https://tatasky-qa-tsmore-kong.videoready.tv/binge-mobile-services/api/v1/pack/validation' \
//    --header 'Authorization: Bearer bal5V4wH5BCJ0x9eGwCr0oeNuHVuTVjU' \
//    --header 'platform: web' \
//    --header 'Content-Type: application/json' \
//    --data-raw '{
//    "packId" : "BM99",
//    "baId" : "5000003899"
//}'

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("/binge-mobile-services/api/v1/pack/validation/{baId}/{productId}")
    fun packValidate(
        @Path("baId") baId: String,
        @Path("productId") productId: String
    ): Single<PackValidationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_FREEMIUM_ACCOUNT_DETAILS")
    @POST("/binge-mobile-services/api/v1/subscription/add/pack")
    fun addPack(
        @Body addPackRequest: AddPackRequest
    ): Single<AddPackResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_FREEMIUM_ACCOUNT_DETAILS")
    @POST("/binge-mobile-services/api/v1/subscription/modify/pack")
    fun modifyPack(
        @Body addPackRequest: AddPackRequest
    ): Single<AddPackResponse>



    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("/binge-mobile-services/api/v1/account/prorated/balance")
    fun proratedBalance(
        @Body proRatedBalanceRequest: ProRatedBalanceRequest
    ): Single<ProRatedResponse>



    /*Search API Changes*/
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST//("search-connector/bingeMobile/search/results")
    fun getSearchV2(
        @Url url: String,
        @Body searchRequest: SearchRequest
    ): Single<RecommendationResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("/binge-mobile-services/api/v1/post/charge/request")
    fun payByDTHBalance(
        @Body payByDTHBalanceRequest: PayByDTHBalanceRequest
    ): Single<BaseResponse>



    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("/binge-mobile-services/api/v1/fetch/subscriber/{baId}/campaign")
    fun fetchCampaign(
        @Path("baId") baId: String
    ): Single<FtvCampaignResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("/binge-mobile-services/api/v1/subscription/invoice/download")
    fun getInvoiceDownloadLink(@Body invoiceDownloadRequest: InvoiceDownloadRequest,@Header("baId") baId: String):Single<InvoiceDownloadResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("/binge-mobile-services/api/v1/payment/status")
    fun fetchPaymentStatus(@Body paymentStatusRequest: PaymentStatusRequest): Single<PaymentStatusResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("/pub/api/mixpanel/unique-id/fetch")
    fun fetchMixpanelUniqueId(@Header("referenceId") referenceId: String): Single<MixpanelUniqueResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_OLD_BA")
    @GET("binge-mobile-services/api/v3/migrate/user")
    fun migrateUser(): Single<MigrateUserResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/pub/api/v2/logout/users/{baId}")
    fun removeAllDevices(@Path("baId") sid: String,
                         @Body body: EmptyBody ): Single<BaseResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @PUT("binge-mobile-services/api/v1/refresh/subscriber/{baId}/account")
    fun refreshAccountForOldUser(@Path("baId") baId: String): Single<BaseResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v1/content/playback/{contentId}")
    fun getChaupalUrlData(
        @Path("contentId") contenId: String,
        @Header("contentType") contentType: String,
        @Body emptyBody: EmptyBody
    ): Single<ChaupalUrlResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("binge-mobile-services/api/v1/pm/play/url")
    fun getPlanetMarathiPlayUrl(
        @Header("providerContentId") providerContentId: String,
        @Header("contentType") contentType: String,
        @Body emptyBody: EmptyBody
    ): Single<ChaupalUrlResponse>
    @POST("binge-mobile-services/pub/api/v1/save/customer/app/rating")
    fun setAppRatingEligibility(@Body body: SetAppRatingRequest): Single<AppRatingResponse>

    @POST("binge-mobile-services/pub/api/v1/save/customer/app/rating")
    fun getAppRatingEligibility(@Body body: GetAppRatingRequest): Single<AppRatingResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/pub/api/v1/account/access/info")
    fun getManagedAppsUrl(
        @Header("baId") baId: String?,
        @Header("journeySource") journeySource: String?,
        @Header("journeySourceRefId") journeySourceRefId: String?,
        @Header("mixpanelId") mixpanelId: String,
        @Header("analyticSource") analyticSource: String,
        @Header("rmn") rmn: String,
        @Header("initiateSubscription") origin: String,
        @Header("subscriptionType") subscriptionType: String,
        @Header("appsflyerId") appsflyerId: String,
        @Header("appVersionName") appVersionName: String
    ):Single<ManagedAppResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/api/v1/subscription/plan/summary?pgScreen=1")
    fun getManagedAppSummeryUrl(
        @Header("baId") baId: String,
        @Header("journeySource") journeySource: String?,
        @Header("journeySourceRefId") journeySourceRefId: String?,
        @Header("mixpanelId") mixpanelId: String,
        @Header("cartId") cartId: String,
        @Header("appsflyerId") appsflyerId: String,
        @Header("appVersionName") appVersionName: String
    ): Single<ManagedAppResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("binge-mobile-services/pub/api/v1/subscription/refresh/token")
    fun getManagedAppRefreshToken(
        @Header("baId") baId: String,
        @Header("journeySource") journeySource: String?,
        @Header("journeySourceRefId") journeySourceRefId: String?,
        @Header("mixpanelId") mixpanelId: String,
        @Header("cartId") cartId: String,
        @Header("appsflyerId") appsflyerId: String,
        @Header("appVersionName") appVersionName: String
    ): Single<ManagedAppResponse>

    @POST("partner-content-analytics/pm/media/analytics")
    fun planetMarathiAnalytics(
        @Body requestBody: PlanetMarathiAnalyticsRequest,
        @Header("contentType") contentType: String
    ): Single<BaseResponse>


    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("/action-data-provider/gamezop/subscriber/favourite")
    fun addGameToFav(
        @Query("profileId") profileId : String,
        @Query("subscriberId") subscriberId: String,
        @Query("contentId") contentId : String,
        @Query("contentType") contentType : String
    ) : Single<GameFavResponse>

    //GAMEZOP FAVOURITE LISTING
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @GET("/action-data-provider/gamezop/subscriber/favourite/listing")
    fun fetchGameFavs(
        @Query("profileId") profileId: String,
        @Query("subscriberId") subscriberId: String,
        @Query("pagingState") pagingState: String?,
        @Query("offSet") offset: Int
    ) : Single<RecommendationResponse>



    //TA hirearchy API
    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @GET
    fun homeTAHierarchy(
        @Url url: String,
        @Header("packName") packName : String,
        @Header("rule") rule :String = "DRPALLVRTABA",
        @Header ("subscriberId") subscriberId : String
    ): Single<HierarchyResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_VRTARAIL")
    @GET("/homescreen-client/pub/api/v2/hierarchy/{pageType}")
    fun homeVRHierarchy(
        @Path("pageType") pageType: String,
        @Query("packName") packName: String,
        @Header("rule") rule: String = "DRPALLVRTABA"
    ): Single<HierarchyResponse>

    @GET("homescreen-client/pub/api/v3/rail")
    fun getRailData(@Query("id") railId: String) : Single<RecommendationResponse>


    @GET("homescreen-client/pub/api/v3/search/genre")
    fun fetchGenreRailData(
        @Query("platform") platform: String,
    ) : Single<RecommendationResponse>

    @GET("homescreen-client/pub/api/v3/search/language")
    fun fetchLanguageRailData(
        @Query("platform") platform: String,
    ) : Single<RecommendationResponse>

    @GET("binge-mobile-services/pub/api/v1/subscription/migrate/user/info")
    fun migrateUserInfo(
        @Header("cartId") cartId: String,
        @Header("securityKey") securityKey: String,
    ): Single<MigrateUserTickTickResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("zee5-playback-api/lionsgate-api/fetch-playback-api/token")
    fun fetchLionsGateToken(@Body request: LionsgateRequest) : Single<LionsGateResponse>

    @Headers("$KEY_HEADER_TYPE:$HEADER_TYPE_BA")
    @POST("zee5-playback-api/generic-playback-Info-api/token")
    fun fetchGenericPartnerDRMAPI(
        @Body request : GenericDRMRequest
    ): Single<GenericPartnerDRMResponse>
}