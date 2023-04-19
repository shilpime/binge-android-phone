package com.tatasky.binge.ui.features.details

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.observe
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.erosnow.partner.ENSDK
import com.erosnow.partner.`interface`.EnLoginListener
import com.erosnow.partner.model.ENError
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.transition.MaterialSharedAxis
import com.google.gson.Gson
import com.irdeto.itac.ITACResult
import com.irdeto.itac.ITACStatus
//import com.m.x.player.tata.sdk.MxSDK
import com.sonylivandroidtssdk.SDKStatus
import com.sonylivandroidtssdk.SonyLIVSDKListener
import com.sonylivandroidtssdk.SonyLIVSDKManager
import com.sonylivandroidtssdk.SonyLivSDKInitializeModel
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.analytics.util.emptyContentAnalyticsModel
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.HoichoiRequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.databinding.FragmentDetailBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.interfaces.*
import com.tatasky.binge.shemaroo.helper.ShemarooHelper
import com.tatasky.binge.ui.base.MyApp
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.SingleEventParcelizeWrapper
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.MiscAnalytics
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.details.adapter.SeriesAdapter
import com.tatasky.binge.ui.features.details.trailer.ITrailerListener
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.home.HomeAnalytics
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.PlayAuthTypeEnum
import com.tatasky.binge.ui.features.home.PlayButtonType
import com.tatasky.binge.ui.features.home.adapter.RailAdapter
import com.tatasky.binge.ui.features.home.model.RailItemsModel
import com.tatasky.binge.ui.features.home.model.RailsModel
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.temp.GuestLoginBottomSheetResult
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.*
import com.tatasky.binge.ui.features.player.PlayerAnalytics
import com.tatasky.binge.ui.features.player.PlayerModel
import com.tatasky.binge.ui.features.player.PlayerViewModel
import com.tatasky.binge.ui.features.watchlist.WatchlistAnalytics
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.ContentUtil.isLiveContent
import com.tatasky.binge.utils.OrientationManager.ScreenOrientation.*
import com.tatasky.binge.utils.RECOMMENDATION
import com.tatasky.binge.voot.model.VootRequest
import com.ttn.ttnplayer.player.SubtitleDTO
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.custom_tab.*
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit

const val KEY_GUEST_LOGIN_BOTTOM_SHEET_RESULT = "keyGuestLoginBottomSheetResult"

open class DetailsFragment : CancellationBaseFragment<FragmentDetailBinding, PlayerViewModel>(),
    ITrailerListener,
    CommonLoadMoreClickListener, OrientationManager.OrientationListener {
    private var incrementCountOnlyOnce: Boolean = false
    private val TAG: String = DetailsFragment::class.java.simpleName
    private var isPackUpdated: Boolean =false
    private var isTabletSwitchToFullScreen: Boolean =false
    private var isTabletLandscape: Boolean =false
    private var partnerSubscriptionTypeForPlayBtn: String? = null
    private var partnerSubscriptionTypeOnPlay: String? = null
    private var isResultHandled = false
    private var iListItem: ContentItem? = null
    private var openSubscriptionActivity = false
    private var isDeviceTablet = false
    private var isNavigateToPlayer = false

    @Inject
    lateinit var playerAnalytics: PlayerAnalytics

    protected var mDisplayManager: DisplayManager? = null
    private val CHROME_PACKAGE_NAME = "com.android.chrome"
    private var isNavigateToOther: Boolean = false
    private var metaDetailResponse: MetaDetails? = null
    private var isDetailShown: Boolean = false
    private val PARTNER_INTEGRATION_MSG: String = "Partner playback integration is pending"
    private var isWebShort: Boolean = false
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    protected var isPlayButtonClick: Boolean = false
    protected var favToast: Toast? = null
    private var topOffset: Int = 0
    private var isLoadingSeries: Boolean = true
    protected var isPlayerStarted = false
    private var clearSeriesList: Boolean = true
    private var partnerIds: Set<String>? = null
    private var isPausedTrailer: Boolean = false
    private val TRAILER_AUTO_START_TIME = 2L
    private var isErrorInRental: Boolean = false
    private var totalContentDuration: Int = 0
    private var isBackFromPlayer = false
    private val REQUEST_FOR_PLAYER: Int = 1011
    private val REQUEST_FOR_PACK_SELECTION: Int = 1012
    private var alreadyAddedSeason: Boolean = false
    private var alreadyFetchedRelatedRail: Boolean = false
    private var seriesLastOffset: Int = 0
    private var isPaused: Boolean = false
    private var watchedSeconds: Int = 0
    private var isBrandResumeContent: Boolean = false

    //    private var isDeepLinkContent: Boolean = false
    private var liveOrientation =
        MutableLiveData<SingleEvent<OrientationManager.ScreenOrientation>>()
    var isContentSubscribed = false
    private var isAutoPlayTrailer: Boolean = false
    protected var isTrailerInitialized = false
    protected var playerModel: PlayerModel? = null
    protected var parentContentType: String = ""
    protected var parentId: String = ""
    private var isPlaying: Boolean = false
    protected var detailsResponse: DetailsResponse? = null
    private var commonViewModel: CommonSampleViewModel? = null
    private var mSelectedPosition: Int = 0
    protected lateinit var id: String
    private var vodId: String = ""
    protected var contentType: String = ""
        set(value) {
            field = value
        }
    var mIsInFullScreenMode: Boolean = false
    val detailFragmentArgs by navArgs<DetailsFragmentArgs>()
    protected lateinit var contentItem: ContentItem
    private var selectedSeriesId: String? = null
    private var trailerUrl: String? = null
    private var playbackUrl: String? = null
    private var licenseUrl: String? = null
    private var isBtnClicked = false
    private var isTrailerEnded = false
    private var initialImageUrl = ""
    private var isEpisode = false
    private var playEpisodeFromSeeAll = false
    private var recommendationResponse: RecommendationResponse? = null
    private var refreshHomeRequired = false
    var contentAuth = false
    private val SON_LIV_PHONE_PERMISSION: Int = 100
    private var mIsSeries: Boolean = false
    private var mIsMovieOrShowStarted: Boolean = false

    @Inject
    lateinit var watchAnalytics: WatchlistAnalytics

    @Inject
    lateinit var homeAnalytics: HomeAnalytics

    @Inject
    lateinit var miscAnalytics: MiscAnalytics

    @Inject
    lateinit var detailAnalytics: DetailAnalytics

    private val mSeeAllClickListener = object : CommonSeeAllClickListener {
        override fun onSeeAllClick(
            railIdName: Pair<Int, String>,
            sectionType: String,
            railPosition: Int?,
            placeHolder: String,
            configType: String?,
            provider: String?,
            isMixedRail: Boolean,
            isPrepand: Boolean,
            item: HomeResponse.Items?,
            backgroundImage: String?,
            layoutType: String?,
            refId: String,
            packName: String?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                this.duration = 250
            }
            var railResponse: RecommendationResponse? = null
            if (!placeHolder.isNullOrEmpty()) {
                railResponse = RecommendationResponse()
                railResponse.data = item
            }
            findNavController().navigateSafe(
                DetailsFragmentDirections.actionDetailsFragmentToSeeAllFragment(
                    viewModel.taContentId,
                    railIdName.second,
                    sectionType,
                    true,
                    viewModel.taContentType,
                    detailsResponse?.data?.metaDetails?.provider,
                    placeHolder,
                    EVENT_VALUE_SOURCE_DETAIL,
                    configType ?: RECOMMENDATION,
                    taContentResponse = railResponse,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            )
            homeAnalytics.trackHomeSeeAll(
                EVENT_VALUE_SOURCE_DETAIL,
                (configType ?: RECOMMENDATION).toUpperCase(),
                SOURCE_MIX,
                railIdName.second,
                "",
                false,
                sectionType,
                "RAIL",
                false
            )
        }
    }


    private var secondaryButtonState: SecondaryButtonStateEnum =
        SecondaryButtonStateEnum.STATE_SEASON
    private var primaryButtonState: PrimaryButtonStateEnum = PrimaryButtonStateEnum.STATE_PLAY


    override fun getViewModelClass(): Class<PlayerViewModel> {
        return PlayerViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_detail
    }

    override fun onDestroy() {
        /**If user is logged in or there is update in user's pack
         * then set value on savedStateHandle when user exits from current
         * screen either via back press or with direct bottom tab changes
         * to observe in previous screen, After observing refresh the data if required
         */
        if (isPackUpdated) {
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set(UPDATE_IN_PACK, SingleEventParcelizeWrapper(SingleEvent(isPackUpdated)))
            //User's pack details has changed...Refresh the data fro Crown visibility
            d(this.javaClass.simpleName, "UPDATE_IN_PACK $isPackUpdated")
        }
        if (incrementCountOnlyOnce && sharedPrefs.isEligibleForAppRating())
            sharedPrefs.saveNumberOfContentPlaybackForAppRating(
                sharedPrefs.getNumberOfContentPlaybackForAppRating().inc()
            )
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (true == savedInstanceState?.containsKey("id")) {
            id = savedInstanceState.getString("id", "")
        }
        if (true == savedInstanceState?.containsKey("contentItem")) {
            contentItem = Gson().fromJson(
                savedInstanceState.getString("contentItem", null),
                ContentItem::class.java
            )
        }
        commonViewModel?.saveOrientation(OrientationManager.ScreenOrientation.PORTRAIT)
        context?.let {
            if(!isTablet(it)) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

        }
//        sharedElementEnterTransition = MaterialContainerTransform().apply {
//            drawingViewId = R.id.fragment_container
//        }
        val forward = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            this.duration = 250
        }
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            this.duration = 250
        }
        returnTransition = backward
        reenterTransition = backward
        exitTransition = backward
    }

    override fun toBeCalledOnce() {

        isTabletLandscape = activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE

        mWidth = when {
            detailFragmentArgs.contentItem?.railName == EVENT_VALUE_RAIL_HB -> (getDisplayMatics().widthPixels * 0.9999).toInt() + 1
            detailFragmentArgs.fromGrid -> (getNormalThumbnailDimensionGrid(requireContext()).x)
            else -> getNormalThumbnailDimension(requireContext()).x
        }
        mHeight = when {
            detailFragmentArgs.contentItem?.railName == EVENT_VALUE_RAIL_HB -> (mWidth.toDouble() * 0.9999 * 0.56).toInt()
            detailFragmentArgs.fromGrid -> getNormalThumbnailDimensionGrid(requireContext()).y
            else -> getNormalThumbnailDimension(requireContext()).y
        }
        viewModel.checkForManagedAppEligibility { eligible ->

        }


        if (detailFragmentArgs.contentItem?.provider.isNullOrBlank()) {
            binding.ivPoster.hide()
        } else
            binding.ivPoster.show()
        binding.ivPoster.apply {
            transitionName = detailFragmentArgs.contentItem?.id + "image"
        }
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>?,
                sharedElements: MutableMap<String, View>?
            ) {
                super.onMapSharedElements(names, sharedElements)
                if (!sharedElements.isNullOrEmpty()) {
                    initialImageUrl = getCloudinaryUrl(
                        viewModel.getCloudinaryUrl(),
                        mWidth, mHeight,
                        detailFragmentArgs.contentItem?.getImageItem() ?: ""
                    )
                    startEnterTransitionAfterLoadingImage(initialImageUrl, binding.ivPoster, true)
                } else {
                    binding.scrollingContent.hide()
                }
            }
        })
        partnerIds = viewModel.sharedPrefs.getPartnerIdsList()
        viewModel.setProgressing(true)
        //Delay for animation to finish

        Handler(Looper.getMainLooper()).postDelayed({
            if (context != null) {
                if (detailFragmentArgs.contentItem == null) {
                    onError(ErrorModel(message = com.tatasky.binge.utils.COMMON_ERROR_MSG))
                    return@postDelayed
                } else {
                    contentItem = detailFragmentArgs.contentItem!!
                    fetchArguments()
                }
//                checkRental()
                if (isErrorInRental) return@postDelayed

                handleApiCall(true)
            }
        }, 250)

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.watchlistBtn.setOnClickListener {
            if (NetworkUtil.checkInternetBeforeNavigate())
                if (sharedPrefs.getLoginStatus())
                    viewModel.markFavourite(parentId, getContentType(parentContentType), true)
                else
                    loginPopup(false)
        }
        binding.shareBtn.setOnClickListener {
            binding.shareBtn.isClickable = false
            val sharingIntent = getShareIntent()
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
            detailAnalytics.trackShare(
                detailsResponse?.data?.metaDetails?.getVodTitle()!!,
                detailsResponse?.data?.metaDetails?.contentType!!,
                detailsResponse?.data?.metaDetails?.genre,
                detailsResponse?.data?.metaDetails?.provider ?: contentItem.provider,
                detailsResponse?.data?.metaDetails?.audio,
                contentItem.origin.uppercase(Locale.ROOT),
                detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
                contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
                detailsResponse?.data?.metaDetails?.getVodTitle() ?: "",
                /*Using Key partnerSubscriptionType to identify if the content is Free or Premium*/
                detailsResponse?.data?.metaDetails?.partnerSubscriptionType?.contains(
                    FREE,
                    true
                ) == true,
                (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
                contentItem.railPosition,
                contentItem.contentConfigType.uppercase(Locale.getDefault()),
                contentItem.railCategory,
                detailsResponse?.data?.metaDetails?.audio?.getOrNull(0),
                detailsResponse?.data?.metaDetails?.genre?.getOrNull(0),
                if (contentAuth) YES else NO,
                contentItem.categoryType ?: "",
                contentItem.contentPosition ?: "",
                detailsResponse?.data?.metaDetails?.rating ?: "",
                detailsResponse?.data?.metaDetails?.releaseYear ?: "",
                sharedPrefs.getDeviceType() ?: "",
                detailsResponse?.data?.metaDetails?.actor,
                sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                if (isAutoPlayTrailer && isTrailerInitialized) YES else NO,
                liveContent = if (isLiveContent(
                        contentItem.contentType,
                        detailsResponse?.data?.metaDetails?.isLiveContent
                    )
                ) YES else NO,
                contentItem.contentConfigType
            )
        }

        binding.whatsappBtn.setOnClickListener {
            val sendIntent = getShareIntent()
            val whatsappPackageName = "com.whatsapp"
            sendIntent.setPackage(whatsappPackageName)
            detailAnalytics.trackWhatsAppShare(
                detailsResponse?.data?.metaDetails?.getVodTitle()!!,
                detailsResponse?.data?.metaDetails?.contentType!!,
                detailsResponse?.data?.metaDetails?.genre,
                detailsResponse?.data?.metaDetails?.provider ?: contentItem.provider,
                detailsResponse?.data?.metaDetails?.audio,
                contentItem.origin.toUpperCase(),
                detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
                contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
                detailsResponse?.data?.metaDetails?.getVodTitle() ?: "",
                /*Using Key partnerSubscriptionType to identify if the content is Free or Premium*/
                detailsResponse?.data?.metaDetails?.partnerSubscriptionType?.contains(
                    FREE,
                    true
                ) == true,
                (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
                contentItem.railPosition,
                contentItem.contentConfigType.toUpperCase(),
                contentItem.railCategory,
                detailsResponse?.data?.metaDetails?.audio?.getOrNull(0),
                detailsResponse?.data?.metaDetails?.genre?.getOrNull(0),
                if (contentAuth) YES else NO,
                contentItem.categoryType ?: "",
                contentItem.contentPosition ?: "",
                detailsResponse?.data?.metaDetails?.rating ?: "",
                detailsResponse?.data?.metaDetails?.releaseYear ?: "",
                sharedPrefs.getDeviceType() ?: "",
                detailsResponse?.data?.metaDetails?.actor,
                sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                if (isAutoPlayTrailer && isTrailerInitialized) YES else NO,
                liveContent = if (isLiveContent(
                        contentItem.contentType,
                        detailsResponse?.data?.metaDetails?.isLiveContent
                    )
                ) YES else NO,
                contentItem.contentConfigType
            )
            context?.let { ctx ->
                if (ctx.packageManager.isPackageInstalled(whatsappPackageName))
                    try {
                        startActivity(sendIntent)
                    } catch (e: Exception) {
                    }
                else {
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$whatsappPackageName")
                            )
                        )
                    } catch (anfe: ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$whatsappPackageName")
                            )
                        )
                    }
                }
            }
        }
        binding.allEpisodes.setOnClickListener {
            miscAnalytics.trackMixPanelSeeAllEpisodes(
                contentTitle = detailsResponse?.data?.metaDetails?.getVodTitle()!!,
                contentType = detailsResponse?.data?.metaDetails?.contentType!!,
                contentGenre = detailsResponse?.data?.metaDetails?.genre?.joinToString(",") ?: "",
                pageName = (activity as? LandingActivity)?.getPageName()
                    ?: EVENT_VALUE_SOURCE_DETAIL,
                railTitle = detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
                railPosition = contentItem.railPosition,
                railType = contentItem.origin,
                railCategory = contentItem.railCategory,
                contentLanguage = detailsResponse?.data?.metaDetails?.audio?.joinToString(",")
                    ?: "",
                contentGenrePrimary = detailsResponse?.data?.metaDetails?.genre?.getOrNull(0),
                contentPartner = detailsResponse?.data?.metaDetails?.provider
                    ?: contentItem.provider,
                contentAuth = if (contentAuth) YES else NO,
                contentCategory = contentItem.categoryType ?: "",
                contentPosition = contentItem.contentPosition ?: "",
                contentRating = detailsResponse?.data?.metaDetails?.rating ?: "",
                contentParentTitle = detailsResponse?.data?.metaDetails?.getParentTitle()
                    ?: playerModel?.getParentTitle() ?: "",
                contentFreeContent = if (!(PREMIUM.equals(
                        detailsResponse?.data?.metaDetails?.partnerSubscriptionType,
                        true
                    ))
                ) YES else NO,
                contentReleaseYear = metaDetailResponse?.releaseYear ?: "",
                deviceType = sharedPrefs.getDeviceType() ?: "",
                actors = metaDetailResponse?.actor?.joinToString(separator = ",") ?: "",
                packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                source = contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
                packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                autoPlayed = if (isAutoPlayTrailer && isTrailerInitialized) YES else NO,
                liveContent = if (isLiveContent(
                        contentItem.contentType,
                        detailsResponse?.data?.metaDetails?.isLiveContent
                    )
                ) YES else NO,
                contentConfigType = contentItem.contentConfigType,
                contentLanguagePrimary = detailsResponse?.data?.metaDetails?.audio?.getOrNull(0)
                    ?: "",
            )
            playEpisodeFromSeeAll = false
            detailsResponse?.let { detailsResponse ->
                selectedSeriesId?.let { selectedSeriesId ->
                    findNavController().navigateSafe(
                        DetailsFragmentDirections.actionDetailToEpisodeSeeAll(
                            detailsResponse,
                            binding.tabLayoutSeasons.selectedTabPosition,
                            selectedSeriesId,
                            isContentSubscribed = isContentSubscribed,
                            detailFragmentArgs.contentAnalyticsModel
                        )
                    )
                }
            }
        }

        binding.ivActivateApple.setOnClickListener {

            trackAppleActivateClick()

            showAppleActivationPopup(true)

        }

        activity?.let {
            if(isTablet(it)){
                isDeviceTablet=true
            }
        }


    }

    fun showAppleActivationPopup(hideLinkButton: Boolean) {
        val verbiageData = sharedPrefs.getConfigResponse()?.data?.config?.getLanguageVerbiage(
            CATEGORY_APPLE_ACTIVATION_POPUP
        )?.data
        showDialog(
            DialogModel(
                cancelable = true,
                imageIdBig = R.drawable.ic_premium_crown,
                title = verbiageData?.header ?: "Activate Apple TV+",
                text = verbiageData?.subHeader
                    ?: "You are eligible to watch Apple TV+ content at no extra cost on Tata Play Binge App.",
                primaryButtonText = verbiageData?.others?.buttonTitle ?: "Activate Now",
                secondaryButtonText = (if (hideLinkButton) {
                    null
                } else {
                    verbiageData?.others?.buttonHeader
                })
            ),
            object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {

                    trackAppleActivateFromPopupClick()
                    viewModel.fetchAppleRedemptionUrl()
                    hideDialog()
                }

                override fun onSecondaryButtonClick() {

                    trackAppleLinkAccountClick()
                    detailsResponse?.data?.metaDetails?.partnerDeepLinkUrl?.let {
                        playInAppBrowserContent(it)
                    }
                    hideDialog()
                }

                override fun onCloseButtonClick() {
                    hideDialog()
                }

            }
        )
    }


    private lateinit var orientationManager: OrientationManager

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            this.duration = 250
        }
        commonViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[CommonSampleViewModel::class.java]
        commonViewModel?.deviceForceLogout()?.observe(requireActivity(), Observer {
            it.getContentIfNotHandled()?.let {
                context?.let {
                    logoutApplication(it)
                }
            }
        })

        commonViewModel?.validateContentRatingResponse?.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                if (response.data?.pinRequired == true) {
                    //validate pin
                    isResultHandled = false
                    (activity as? LandingActivity)?.isResultHandled = true
                    findNavController().navigateSafe(
                        DetailsFragmentDirections.actionGlobalParentalControlBottomDialogFragment(
                            ACTION_PIN_VERIFICATION,
                            null,
                            false,
                            PLAY
                        )
                    )
                } else {
                    //no need to validate rating, play content
                    playAfterRattingCheck()
                }

            }
        }

        commonViewModel?.validateContentRatingError?.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                //show error message here
            }
        }
        orientationManager = OrientationManager(activity, SensorManager.SENSOR_DELAY_FASTEST, this)
        context?.let {
            if (isTablet(it) && ::orientationManager.isInitialized) orientationManager.enable()
        }
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onOrientationChange(screenOrientation: OrientationManager.ScreenOrientation?) {
        liveOrientation.postValue(SingleEvent(screenOrientation ?: PORTRAIT))
    }

    override fun setObserver() {
        super.setObserver()

        viewModel.getAppleRedemptionUrl().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.data?.redemption_url?.let { url ->
                openChromeTab(Uri.parse(url))
            }
        })

        viewModel.getGenericPlaybackUrls().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.data?.playerDetail?.let { playerDetails ->
                playerModel?.setPlaybackUrl(playerDetails.playUrl)
                playerModel?.setDrmLicenseUrl(playerDetails.licenseUrl ?: "")
                playerDetails.token?.let{playerModel?.setToken(it)}
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
            }
        })

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<SingleEventParcelizeWrapper>(UPDATE_IN_PACK)
            ?.observe(viewLifecycleOwner, Observer {
                it.booleanEventValue.getContentIfNotHandled()?.let { isPackUpdated ->
                    this.isPackUpdated = isPackUpdated
                    //                    if (isPackUpdated) {
//                        findNavController().previousBackStackEntry
//                            ?.savedStateHandle
//                            ?.set(UPDATE_IN_PACK, SingleEventParcelizeWrapper(SingleEvent(isPackUpdated)))
//                        //User's pack details has changed...Refresh the data fro Crown visibility
//                        d(this.javaClass.simpleName, "UPDATE_IN_PACK $isPackUpdated")
//                    }
                }
            })

        viewModel.getChaupalPlaybackUrls().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.data?.let {
                if (PROVIDER_PLANET_MARATHI.equals(
                        detailsResponse?.data?.metaDetails?.provider,
                        true
                    )
                ) {
                    playbackUrl = it.playUrl
                } else {
                    /*Need to handle Subtitles Urls as well*/
                    for (adaptiveUrl in it.playUrls!!) {
                        if ("widevine".equals(adaptiveUrl.drmType, ignoreCase = true)) {
                            playbackUrl = adaptiveUrl.url
                            licenseUrl = adaptiveUrl.licenceUrl
                            break
                        }
                    }
                    val subtitleUrl = ArrayList<SubtitleDTO>()
                    if (it.subtitles != null)
                        for (adaptiveUrl in it.subtitles!!) {
                            val dto = SubtitleDTO()
                            dto.lang = adaptiveUrl.language
                            dto.url = adaptiveUrl.url
                            subtitleUrl.add(dto)
                        }
                    playerModel?.setPlaybackSubtitleUrl(subtitleUrl)
                    if (contentType == TYPE_MOVIES) {
                        detailsResponse?.data?.detail?.dashWidewinePlayUrl = playbackUrl ?: ""
                        detailsResponse?.data?.detail?.dashWidewineLicenseUrl = licenseUrl ?: ""
                    }
                    licenseUrl?.let { it1 -> playerModel?.setDrmLicenseUrl(it1) }
                }
            }
            playerModel?.setPlaybackUrl(playbackUrl)
            playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
            isPlayButtonClick = false
        })
        viewModel.getChaupalTrailerUrls().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.data?.let {

                for (adaptiveUrl in it.playUrls!!) {
                    if ("widevine".equals(adaptiveUrl.drmType, ignoreCase = true)) {
                        detailsResponse?.data?.detail?.dashWidewineTrailerUrl =
                            adaptiveUrl.url
                        trailerUrl = adaptiveUrl.url
                        break
                    }
                }
                startTrailer(detailsResponse?.data!!.detail!!.dashWidewineTrailerUrl, null)
            }
        })
        viewModel.previouslyUsedMobileNumberResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                findNavController().navigateSafe(
                    DetailsFragmentDirections.actionGlobalLoginBottomSheetDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = false,
                        previouslyUsedMobileNumbersList = response.data?.mobileNUmberList?.toTypedArray(),
                        loginSource = SOURCE_PLAY_CLICK
                    )
                )
            }
        }

        viewModel.previouslyUsedMobileNumberError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateSafe(
                    DetailsFragmentDirections.actionGlobalLoginBottomSheetDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = false,
                        previouslyUsedMobileNumbersList = null,
                        loginSource = SOURCE_PLAY_CLICK
                    )
                )
            }
        }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(KEY_GUEST_LOGIN_BOTTOM_SHEET_RESULT)
            ?.observe(viewLifecycleOwner) { result ->
                when (result) {
                    GuestLoginBottomSheetResult.SUCCESS -> {
//                        commonViewModel?.refreshHome?.postValue(SingleEvent(true))
                    }
                }
                e("pubnub", "inside PI resultOnBack : $result")
            }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ParentalControlBottomSheetResult>(
                KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT
            )
            ?.observe(viewLifecycleOwner) { result ->
                e("ParentalBack", "isResultHandled: $isResultHandled")
                if (!isResultHandled) {
                    isResultHandled = true
                    when (result.resultStatus) {
                        ParentalControlBottomSheetResultStatus.SUCCESS_DISMISS -> {
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_PIN_FORGOT -> {
                                    val toastView =
                                        DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                                            LayoutInflater.from(context),
                                            R.layout.layout_toast_success_failure,
                                            null,
                                            false
                                        )
                                    toastView.textLoginSuccessfulToast.text =
                                        getString(R.string.toast_msg_pin_changed_successful)
                                    toastView.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                                    showCustomToast(
                                        context,
                                        toastView?.root,
                                        Gravity.FILL_HORIZONTAL
                                    )
                                }
                            }
                        }
                        ParentalControlBottomSheetResultStatus.PIN_VERIFIED -> {
                            e("ParentalBack", "PIN_VERIFIED: $isResultHandled")
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_PIN_VERIFICATION -> {
                                    //play content
                                    e(
                                        "ParentalBack",
                                        "partnerSubscriptionTypeOnPlay: $partnerSubscriptionTypeOnPlay"
                                    )
                                    if (isContentPlayable(partnerSubscriptionTypeOnPlay)) {
                                        playAfterRattingCheck()
                                    } else {
                                        if (!openSubscriptionActivity) {
                                            handleNudgeClick()
                                        } else if (!sharedPrefs.isManagedAppEnabled()) {
                                            startActivity(
                                                getSubscriptionActivityIntent(
                                                    activity,
                                                    startPackListing = true,
                                                    fromScreen = SOURCE_NUDGES,
                                                    partnerId = detailsResponse?.data?.metaDetails?.partnerId
                                                        ?: ""
                                                )
                                            )

                                        } else if (upgradePlanClick) {
                                            showMiniDrawer(
                                                context = activity,
                                                fromLogin = false,
                                                fromScreen = SOURCE_PLAY_CLICK,
                                                startPackListing = true,
                                                journeyRef = HOME_CONTENT
                                            )
                                        } else {
                                            showMiniDrawer(
                                                context = activity,
                                                startPackListing = true,
                                                fromScreen = SOURCE_NUDGES,
                                            )
                                        }

                                    }
                                }
                            }
                        }
                        ParentalControlBottomSheetResultStatus.DISMISS -> {
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_PIN_VERIFICATION -> {
                                    //show error message here
                                }
                            }
                        }
                    }
                }
            }

        viewModel.getSonylivShortToken().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                e("SonyLIVSDKListener", "observer t.data?.token: ${it}")
                val oldToken = sharedPrefs.getSonyOldToken() ?: ""
                if(oldToken == it && (!sharedPrefs.isLoginAgain() && SonyLIVSDKManager.getInstance().status == SDKStatus.SUCCESS)) {
                    e("SonyLIVSDKListener","play using old token")
                    playSonyLivContent()
                }
                else {
                    e("SonyLIVSDKListener","play using new token")
                    initSonyLiv(it)
                }
            }
        })
        viewModel.getCSPlaybackUrl().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                playerModel?.let { model ->
                    viewModel.lastWatched?.dashWidewinePlayUrl = it
                    model.setPlaybackUrl(it)
                    navigateToPlayer(model)
                }
            }
        })
        viewModel.getContentToken().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                playerModel?.setLA_URL(playerModel?.getLA_URL() + "&ls_session=$it")
                playerModel?.setDrmLicenseUrl(playerModel?.getLA_URL() ?: "")
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
            }
        })
        viewModel.getSignoutResponse().observe(requireActivity(), Observer {
            it.getContentIfNotHandled()?.let {
                activity?.let { act ->
                    context?.let {
                        localBroadcastHelper.unregisterBroadcast(it, mLogoutListener)
                    }
                    logoutApplication(act)
                }
            }
        })
        viewModel.getVootPlaybackUrls().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                playbackUrl = it.data?.url ?: ""
                if (!it.data?.filteredDRM.isNullOrEmpty()) {
                    licenseUrl = it.data?.filteredDRM?.get(0)?.licenseURL
                    detailsResponse?.data?.detail?.dashWidewineLicenseUrl = licenseUrl
                    playerModel?.setDrmLicenseUrl(licenseUrl ?: "")
                }

                detailsResponse?.data?.detail?.dashWidewinePlayUrl = playbackUrl ?: ""
                playerModel?.setPlaybackUrl(playbackUrl)
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
                isPlayButtonClick = false
            }
        })
        viewModel.getVootTrailerUrls().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val trailerUrl = it.data?.url ?: ""
                var trailerLicense: String? = null
                if (!it.data?.filteredDRM.isNullOrEmpty()) {
                    trailerLicense = it.data?.filteredDRM?.get(0)?.licenseURL
                }

                startTrailer(trailerUrl, trailerLicense)
            }
        })

        viewModel.getHoichoiPlaybackUrls().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                playHoiChoiContent(playerModel?.getPlaybackUrl(), it.data?.token ?: "")
            }
        })

        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                handleUpdateInPack(it)
            }
        })
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!viewModel.onlyMessage)
                    findNavController().navigateUp()
            }
        })


       activity?.let {
           if(!isTablet(it)) {
               liveOrientation.observe(viewLifecycleOwner, Observer {
                   it?.getContentIfNotHandled()?.let { screenOrientation ->
                       when (screenOrientation) {
                           PORTRAIT, REVERSED_PORTRAIT -> {
                               changeToPortraitMode()
                           }
                           LANDSCAPE, REVERSED_LANDSCAPE -> {
                                   changeToLandscapeMode()
                           }
                           else -> {
                               changeToPortraitMode()
                           }
                       }
                   }
               })
           }
       }
        viewModel.getShemarooPlaybackUrls().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {

                for (adaptiveUrl in it.adaptiveUrls!!) {
                    if ("main".equals(adaptiveUrl.label, ignoreCase = true)) {
                        playbackUrl = adaptiveUrl.playback_url
                        break
                    }
                }
                if (contentType == TYPE_MOVIES) {
                    detailsResponse?.data?.detail?.dashWidewinePlayUrl = playbackUrl ?: ""
                }
                playerModel?.setPlaybackUrl(playbackUrl)
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
                isPlayButtonClick = false
            }
        })
        viewModel.getShemarooTrailerUrls().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {

                for (adaptiveUrl in it.adaptiveUrls!!) {
                    if ("main".equals(adaptiveUrl.label!!, ignoreCase = true)) {
                        detailsResponse?.data?.detail?.dashWidewineTrailerUrl =
                            adaptiveUrl.playback_url!!
                        trailerUrl = adaptiveUrl.playback_url
                        break
                    }
                }
                startTrailer(detailsResponse?.data!!.detail!!.dashWidewineTrailerUrl, null)
            }
        })

        viewModel.getDetailsResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                if (isErrorInRental) return@Observer
                detailsResponse = response
//                detailsResponse?.data?.metaDetails?.contentTypeLocal = contentType
                if (contentType == WEB_SHORTS)
                    isWebShort = true
                e(
                    "ContentDetails",
                    "partnerSubscriptionType: ${detailsResponse?.data?.metaDetails?.partnerSubscriptionType}"
                )
                response.data!!.metaDetails?.let { it1 -> onDetailFetched(it1) }
                trackPIView()
                fetchLastWatchData()
                handleMxUpsell(response.data?.metaDetails?.provider)
                if (!isBackFromPlayer) {
                    handleRRMSession()
                }
                checkRental()
            }
        })

        viewModel.getRecommendationResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                recommendationResponse = response
                onRecommendationFetched(response)
            }
        })
        viewModel.getSeriesList().observe(viewLifecycleOwner, Observer {

            it.getContentIfNotHandled()?.let { response ->
//                if (response.code == RESPONSE_CODE_NETWORK_ERROR) {
//                    binding.seriesRecycler.hide()
//                    binding.networkView.show()
//                } else {
                mIsSeries = true
                binding.seriesRecycler.show()
                binding.networkView.hide()
                if (response.code == CUSTOM_RESPONSE_CODE_SERIES_ADDING) {
                    (binding.seriesRecycler.adapter as SeriesAdapter).addLoading()
                    (binding.seriesRecycler.adapter as SeriesAdapter).notifyItemInserted((binding.seriesRecycler.adapter as SeriesAdapter).itemCount)
                } else {
                    onSeriesFetched(response)
                }
//                }
                if (!alreadyFetchedRelatedRail)
                    handleRecommendationApi()
            }
            e("onSeriesFetched", "isDetailShown: $isDetailShown")
            if (!isDetailShown) {
                showDetailViews()
            } else
                viewModel.setProgressing(false)
        })
        viewModel.getZee5TagResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                playZee5(it.data?.tag ?: "")
            }
        })
        viewModel.getLionsgateTokenResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (it.data?.kid == null) {
                    showToast(context, "Unable to play Content")
                } else {
                    playerModel?.setKid(it.data?.kid ?: "")
                    playerModel?.setToken(it.data?.token ?: "")
                    playerModel?.setDrmProxyUrl(
                        it.data?.widevineLicenceUrl ?: "https://widevine-proxy.drm.technology/proxy"
                    )
                    playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
                }
            }
        })

        viewModel.getVootPwaTokenResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { url ->
                d("VootPwaDeeplinkUrl", url)
                actionOnPlayClick()
                openChromeTab(Uri.parse(url))
            }
        })

    }

    fun handleUpdateInPack(it: Boolean) {
        if (it && !binding.trailerView.isTrailerStarted && !binding.trailerView.isVisibile()  /*When trailer is playing, Avoid API calls due to Pubnub push*/) {
            e("pubnub", "inside DetailsFragment updateInPack : $it")
            //show alert or update pack
            updateUIWithDeeplink()
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set(UPDATE_IN_PACK, SingleEventParcelizeWrapper(SingleEvent(it)))
        }
    }

    private fun handleMxUpsell(provider: String?) {
        if (!PROVIDER_MXPLAYER.equals(provider, true)
            || (sharedPrefs.getConfigResponse()?.data?.config?.mxUpsellInfo?.mxUpsellPopup == false)
            || sharedPrefs.getMxUpsellClosed()
        ) {
            binding.mxUpsellVisible = false
            return
        }

        sharedPrefs.getConfigResponse()?.data?.config?.mxUpsellInfo?.mxUpsellBannerUrl?.let {
            imageLoad(binding.ivMxUpsellBanner.ivMxBanner, it)
        } ?: run {
            binding.ivMxUpsellBanner.ivMxBanner.setImageDrawable(context?.let {
                ContextCompat.getDrawable(
                    it, R.drawable.mx_upsell_banner
                )
            })
        }

        val isPartnerSubscribed = isFreeContent(
            detailsResponse?.data?.detail?.contractName,
            viewModel.sharedPrefs.getPartnerIdsList(),
            detailsResponse?.data?.metaDetails?.partnerId ?: "",
            sharedPrefs.getSubscribedPack()?.subscriptionStatus
        )

        binding.mxUpsellVisible = !isPartnerSubscribed
        binding.ivMxUpsellBanner.upsellCloseCta.setOnClickListener {
            binding.mxUpsellVisible = false
            sharedPrefs.setMxUpsellClosed(true)
        }
        binding.ivMxUpsellBanner.ivMxBanner.setOnClickListener {
            navigateToSubscriptionActivity()
        }
    }


    private fun fetchLastWatchData() {
        val isPartnerSubscribed = isFreeContent(
            detailsResponse?.data?.detail?.contractName,
            viewModel.sharedPrefs.getPartnerIdsList(),
            detailsResponse?.data?.metaDetails?.partnerId ?: "",
            sharedPrefs.getSubscribedPack()?.subscriptionStatus
        )
        viewModel.fetchLastWatchedFavourite(id, getContentType(contentType), isPartnerSubscribed)
    }

    private fun trackPIView() {
        detailAnalytics.trackViewContentDetail(
            detailsResponse?.data?.metaDetails?.getVodTitle() ?: "",
            detailsResponse?.data?.metaDetails?.contentType ?: "",
            detailsResponse?.data?.metaDetails?.genre,
            detailsResponse?.data?.metaDetails?.audio,
            contentItem.origin.toUpperCase(),
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            detailsResponse?.data?.metaDetails?.provider ?: contentItem.provider,
            detailsResponse?.data?.metaDetails?.getParentTitle() ?: "",
            /*Using Key partnerSubscriptionType to identify if the content is Free or Premium*/
            detailsResponse?.data?.metaDetails?.partnerSubscriptionType?.contains(
                FREE,
                true
            ) == true,
            (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
            contentItem.railPosition,
            contentItem.origin,
            contentItem.railCategory,
            detailsResponse?.data?.metaDetails?.audio?.getOrNull(0),
            detailsResponse?.data?.metaDetails?.genre?.getOrNull(0),
            contentAuth = if (contentAuth) YES else NO,
            contentItem.contentType,
            contentItem.contentPosition,
            detailsResponse?.data?.metaDetails?.rating ?: "",
            detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            detailsResponse?.data?.metaDetails?.actor,
            sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
            if (isAutoPlayTrailer && isTrailerInitialized) YES else NO,
            liveContent = if (isLiveContent(
                    contentItem.contentType,
                    detailsResponse?.data?.metaDetails?.isLiveContent
                )
            ) YES else NO,
            contentItem.contentConfigType,
            contentItem.searchKeyword,
            contentItem.suggestorForMixpanel,
            getAppleStatusValue()
        )
        if (contentItem.isQuerySubmitted) {
            val contentId = detailsResponse?.data?.metaDetails?.vodId
                ?: detailsResponse?.data?.metaDetails?.id
                ?: ""
            viewModel.trackOnceIn24hrLearnAction(
                detailsResponse?.data?.metaDetails?.partnerSubscriptionType,
                contentType,
                contentId,
                detailsResponse?.data?.metaDetails?.taShowType ?: "",
                detailsResponse?.data?.metaDetails?.provider ?: "",
                detailsResponse?.data?.detail?.contractName ?: "",
                SEARCH_LEARN_ACTION,
                contentItem.refId
            )
        }
    }

    fun playHoiChoiContent(url: String?, token: String) {
        playbackUrl = url

        playbackUrl?.let {
            playbackUrl = "$it?$token"
//        val playUrl = "https://hoichoihlsns.akamaized.net/vhoichoiindia2/Renditions/20220221/1645105070311_rb_trailer_stream_now/dash/master.mpd?" + token
            playerModel?.setPlaybackUrl(playbackUrl)
//            val dto = SubtitleDTO()
//            dto.url = "https://cchoichoi.viewlift.com/2022/02/1645549566567_byadh_hindi_ep01.srt"
//            dto.language = "en"
//            playerModel?.setPlaybackSubtitleUrl(arrayListOf(dto))
            playerModel?.let { it1 -> navigateToPlayer(it1) }
        }

    }

    private fun handleNudgeClick() {
        if (sharedPrefs.getSubscribedPack()?.upgradeFDOCheck == true) {
            showDialog(
                DialogModel(
                    false,
                    R.drawable.ic_subscription_error,
                    sharedPrefs.getSubscribedPack()?.upgradeFDOHeader ?: "",
                    getString(R.string.done),
                    "",
                    sharedPrefs.getSubscribedPack()?.upgradeFDOMessage ?: ""
                ), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }
                }
            )
        } else {
            when (sharedPrefs.getSubscribedPack()?.planCTADetails?.renewPlanOption == true) {
                true -> {
                    val currentPack =
                        sharedPrefs.getSubscribedPack()
                    if (currentPack?.planCTADetails?.getPlanOption == true) {
                        if (!sharedPrefs.isManagedAppEnabled()) {
                            startActivity(
                                getSubscriptionActivityIntent(
                                    activity,
                                    startPackListing = true,
                                    fromScreen = SOURCE_NUDGES,
                                    partnerId = detailsResponse?.data?.metaDetails?.partnerId ?: ""
                                )
                            )

                        } else {
                            showMiniDrawer(
                                context = activity,
                                startPackListing = true,
                                fromScreen = SOURCE_NUDGES
                            )
                        }

                    } else {
                        if (currentPack?.migrated == true) {
                            showDialog(
                                DialogModel(
                                    false,
                                    null,
                                    currentPack.migratedVerbiage,
                                    getString(R.string.ok),
                                    null
                                ),
                                object :
                                    CommonDialogEventListener {
                                    override fun onPrimaryButtonClick() {
                                        commonViewModel?.setErrorOkClicked()
                                        hideDialog()
                                    }

                                    override fun onCloseButtonClick() {
                                        hideDialog()
                                    }

                                    override fun onSecondaryButtonClick() {
                                    }
                                })
                        } else {
                            if (currentPack != null) {
                                currentPack.tenure?.find {
                                    it.currentTenure == true
                                }?.let {
                                    startActivity(
                                        getPaymentActivityIntent(
                                            activity, packId = currentPack.productId,
                                            selectedTenureId = it.tenureId,
                                            selectedTenureAmount = it.offeredPriceValue,
                                            isMigrated = currentPack.migrated,
                                            migratedVerbiage = currentPack.migratedVerbiage,
                                            proratedAmount = null,
                                            fromScreen = SOURCE_NUDGES,
                                            sharedPrefs = sharedPrefs
                                        ).apply { putExtra(KEY_IS_RENEW, true) }
                                    )
                                } ?: run {
                                    startActivity(
                                        getPaymentActivityIntent(
                                            activity, packId = currentPack.productId,
                                            selectedTenureId = currentPack.productId,
                                            selectedTenureAmount = currentPack.amountValue,
                                            isMigrated = currentPack.migrated,
                                            migratedVerbiage = currentPack.migratedVerbiage,
                                            proratedAmount = null,
                                            fromScreen = SOURCE_NUDGES,
                                            sharedPrefs = sharedPrefs
                                        ).apply { putExtra(KEY_IS_RENEW, true) }
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    if (!sharedPrefs.isManagedAppEnabled()) {
                        startActivity(
                            getSubscriptionActivityIntent(
                                activity,
                                fromScreen = SOURCE_NUDGES,
                                partnerId = detailsResponse?.data?.metaDetails?.partnerId ?: "",
                                startPackListing = true
                            )
                        )
                    } else
                        showMiniDrawer(
                            context = activity,
                            fromScreen = SOURCE_NUDGES,
                            startPackListing = true
                        )
                }
            }
        }
    }

    private fun getUrlWithoutParameters(url: String): String? {
        val uri = URI(url)
        return URI(
            uri.scheme,
            uri.authority,
            uri.path,
            null,  // Ignore the query part of the input url
            uri.fragment
        ).toString()
    }

    protected fun updateUIWithDeeplink() {
        handleApiCall(false)
    }

    /**
     * Method call to hit api initially
     */
    private fun handleApiCall(showLoader: Boolean) {
        //64177
        val contentType = this.contentType.toUpperCase()
        if (contentType.contains(TYPE_MOVIES) || contentType.contains(TYPE_WEB_SHORTS)) {
            viewModel.getBrandDetails(id, getDetailsContentType(contentType), showLoader)
        } else if (getDetailsContentType(contentType).isNotBlank()) {
            if (getDetailsContentType(contentType) == DetailTypeEnum.VOD.type)
                viewModel.getBrandDetails(vodId, getDetailsContentType(contentType), showLoader)
            else
                viewModel.getBrandDetails(id, getDetailsContentType(contentType), showLoader)
        } else {
            findNavController().navigateUp()
        }

        //update crown icon of recommendation rail on login on PI without changing the content of the recommendation rail
        recommendationResponse?.let { onRecommendationFetched(it) }

    }

    private fun handleRecommendationApi() {
        alreadyFetchedRelatedRail = true
        val provider = detailsResponse?.data?.metaDetails?.provider ?: ""
        val taShowType = detailsResponse?.data?.metaDetails?.taShowType
        val data = taShowType?.split("-".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
        if (RENTAL.equals(
                detailsResponse?.data?.detail?.contractName,
                ignoreCase = true
            )
            || data == null
        ) {
            fetchTtnRecommendation()
        } else {
            val id = detailsResponse?.data?.metaDetails?.id
            val vodId = detailsResponse?.data?.metaDetails?.vodId
            val contentId = vodId ?: id ?: ""
            val showType = if (data[0] == "CatchupEPG") "EPG" else "VOD"
            val contentType = data[1]

            val taRelateRailConfigData = viewModel.sharedPrefs.getTARelatedRail() ?: ArrayList()
            var taRelatedRail: TaRelatedRail? = null
            taRelateRailConfigData.let {
                for (relatedRail in taRelateRailConfigData) {
                    e("DetailsFragment", "relatedRail.contentType : ${relatedRail.contentType}")
                    if (relatedRail.contentType == contentType) {
                        taRelatedRail = relatedRail
                    }
                }
            }
            if (taRelatedRail == null) {
                fetchTtnRecommendation()
                return
            }
            viewModel.taContentType = taShowType
            viewModel.taContentId = contentId
            e("getTARecommendations", "contentType: $contentType, contentId: $contentId")
            e("getTARecommendations", "parentContentType: $parentContentType, parentId: $parentId")
            taRelatedRail?.let {
                viewModel.getTARecommendations(
                    it.useCase,
                    contentId,
                    contentType,
                    showType,
                    provider,
                    parentContentType,
                    parentId,
                    it.fallbackUseCase
                )
            }
        }
    }

    private fun fetchTtnRecommendation() {
        viewModel.taContentType = getContentType(contentType)
        viewModel.taContentId = id
        viewModel.fetchRecommendations(id, viewModel.taContentType)
    }


    private fun fetchArguments() {
        id = contentItem.id
        contentType = contentItem.contentType
        if (contentType == TYPE_TV_SHOWS) {
            vodId = id
        }
        if (contentItem.freeEpisodesAvailable && !contentItem.isPartnerSubscribed)
            binding.tvEpisodeFree.show()
        mWidth = Resources.getSystem().displayMetrics.widthPixels
        binding.viewModel = viewModel
    }


    private fun onSeriesFetched(seriesListResponse: SeriesListResponse) {
        isLoadingSeries = false
        var moreContentAvailable = false
        val limit = seriesListResponse.data?.limit ?: 0
        val total = seriesListResponse.data?.total ?: 0
        val offset = seriesListResponse.data?.offset ?: 0

        var isPrepand = true
        if (offset >= seriesLastOffset) {
            isPrepand = false
            seriesLastOffset = offset
            if (limit + offset < total)
                moreContentAvailable = true
        }
        e(
            "DetailsFragment", "onSeriesFetched isPrepand : $isPrepand , offset : $offset " +
                "seriesLastOffset : $seriesLastOffset topOffset : $topOffset"
        )
        if (seriesListResponse.data != null)
            if (clearSeriesList) {
                topOffset = offset
                binding.seriesRecycler.show()
                mIsSeries = true
                (binding.seriesRecycler.adapter as? SeriesAdapter)?.updateList(
                    seriesListResponse.data!!.contentItem,
                    moreContentAvailable,
                    isContentSubscribed
                )
                clearSeriesList = false
            } else if (isPrepand) {
                (binding.seriesRecycler.adapter as? SeriesAdapter)?.prepandToList(
                    seriesListResponse.data!!.contentItem,
                    isContentSubscribed
                )
            } else {
                (binding.seriesRecycler.adapter as? SeriesAdapter)?.addToList(
                    seriesListResponse.data!!.contentItem,
                    moreContentAvailable,
                    isContentSubscribed,
                    detailFragmentArgs.contentAnalyticsModel ?: emptyContentAnalyticsModel()
                )
            }
    }

    private fun onRecommendationFetched(recommendationResponse: RecommendationResponse) {
        if (recommendationResponse.data?.filteredContentItems?.size ?: 0 > 0) {
            binding.llRecommended.show()
            var title = recommendationResponse.data?.title ?: "You may also like"
            if (title.isEmpty()) {

                val titleBuffer = StringBuffer("Related ")
                if (contentType.contains(TYPE_MOVIES)) {
                    titleBuffer.append("Movies")
                } else if (contentType.contains(TYPE_TV_SHOWS)
                    || contentType.contains(TYPE_CATCH_UP)
                ) {
                    titleBuffer.append("Shows")
                } else if (contentType.contains(TYPE_WEB_SHORTS)) {
                    titleBuffer.append("Shorts")
                } else if (contentType.contains(TYPE_BRAND)) {
                    titleBuffer.append("Brand")
                } else if (contentType.contains(TYPE_SERIES)) {
                    titleBuffer.append("Series")
                }
                title = titleBuffer.toString()
                recommendationResponse.data?.title = title
            }

            binding.recommendedRecycler.railsModel = RailsModel(
                title, RailAdapter(
                    mBannerClick,
                    recommendationResponse.data?.filteredContentItems!!,
                    recommendationResponse.data?.layoutType!!,
                    0,
                    viewModel.getCloudinaryUrl(),
                    sectionSource = recommendationResponse.data?.sectionSource
                        ?: EventConstants.TYPE_EDITORIAL,
                    continueWatching = false,
                    railTitle = title,
                    providerLogos = viewModel.sharedPrefs.getProviderLogo(),
                    railPoint = RailPoint(),
                    viewModel.sharedPrefs,
                    refId = recommendationResponse.data?.refId ?: "",
                    railSectionType = recommendationResponse.data?.sectionType ?: RAIL.uppercase(),
                )
            )
            binding.recommendedRecycler.homeRecyclerView.clearOnScrollListeners()
            binding.recommendedRecycler.homeRecyclerView.addOnScrollListener(CustomScrollListener {
                mRailScrollListener.onRailScrolled(
                    title,
                    if (mIsSeries) 2 else 1,
                    contentItem.contentConfigType.toUpperCase(),
                    recommendationResponse.data?.sectionSource
                        ?: EventConstants.TYPE_EDITORIAL
                )
            })


            recommendationResponse.data?.id?.let { recommendationId ->
                binding.recommendedRecycler.homeSeeAll.setOnClickListener {
                    mSeeAllClickListener.onSeeAllClick(
                        Pair(recommendationId, recommendationResponse.data?.title ?: ""),
                        recommendationResponse.data?.sectionSource ?: "",
                        null,
                        recommendationResponse.data?.placeHolder ?: "",
                        recommendationResponse.data?.configType,
                        item = recommendationResponse.data,
                        backgroundImage = "",
                        layoutType = null,
                        refId = "",
                        contentAnalyticsModel = ContentAnalyticsModel(
                            recommendationResponse.data?.sectionSource,
                            recommendationResponse.data?.sectionType,
                            recommendationResponse.data?.title
                        )
                    )
                }
            }
        } else {
            binding.llRecommended.hide()
        }
    }

    private val mEpisodeClickListener = object : EpisodeClickListener {
        override fun selectedEpisode(
            currentEpisode: ContentItem,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            mSeriesClickListener.onSubItemClick(
                currentEpisode,
                0,
                0,
                EventConstants.TYPE_RAIL,
                null,
                contentAnalyticsModel = contentAnalyticsModel
            )
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
        }
    }


    private val mBannerClick = object : CommonDTOClickListener {
        override fun onSubItemClick(
            iListItem: ContentItem,
            iItemPosition: Int,
            iSectionPosition: Int,
            iSectionType: String,
            transitions: List<Pair<View, String>>?,
            railTitle: String,
            origin: String?,
            gamesMixpanelInfoModel: GamesMixpanelInfoModel?,
            railItemsModel: RailItemsModel?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            val extras = if (!transitions.isNullOrEmpty())
                FragmentNavigatorExtras(*transitions.toTypedArray())
            else FragmentNavigatorExtras()
            iListItem.railName = railTitle
            iListItem.source =
                EVENT_VALUE_SOURCE_DETAIL //binding.recommendedRecycler.homeRecyclerViewTitle.text.toString()
            iListItem.origin = origin ?: EventConstants.TYPE_EDITORIAL
            iListItem.contentPosition = (iItemPosition + 1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            findNavController().navigateSafe(
                DetailsFragmentDirections.actionToDetail(
                    iListItem,
                    false,
                    contentAnalyticsModel = contentAnalyticsModel
                ), extras
            )
        }
    }

    private val mEpisodeInfoClickListener = object : CommonDTOClickListener {
        override fun onSubItemClick(
            iListItem: ContentItem,
            iItemPosition: Int,
            iSectionPosition: Int,
            iSectionType: String,
            transitions: List<Pair<View, String>>?,
            railTitle: String,
            origin: String?,
            gamesMixpanelInfoModel: GamesMixpanelInfoModel?,
            railItemsModel: RailItemsModel?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            iListItem.railName = railTitle
            iListItem.origin = origin ?: EventConstants.TYPE_EDITORIAL
            iListItem.contentPosition = (iItemPosition + 1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            findNavController().navigateSafe(
                DetailsFragmentDirections.actionDetailEpisodeBotttomSheet(
                    iListItem,
                    mEpisodeClickListener,
                    contentAnalyticsModel
                )
            )

        }
    }


    private val mSeriesClickListener = object : CommonDTOClickListener {
        override fun onSubItemClick(
            iListItem: ContentItem,
            iItemPosition: Int,
            iSectionPosition: Int,
            iSectionType: String,
            transitions: List<Pair<View, String>>?,
            railTitle: String,
            origin: String?,
            gamesMixpanelInfoModel: GamesMixpanelInfoModel?,
            railItemsModel: RailItemsModel?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            iListItem.railName = railTitle
            iListItem.origin = origin ?: EventConstants.TYPE_EDITORIAL
            iListItem.contentPosition = (iItemPosition + 1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            if (PROVIDER_TATA_SKY.equals(detailsResponse?.data?.metaDetails?.provider, true))
                iListItem.partnerSubscriptionType = "Free"
            partnerSubscriptionTypeOnPlay = iListItem.partnerSubscriptionType
            if (isContentPlayable(iListItem.partnerSubscriptionType)) {
                checkForGuestUserPlaybackEligibility({
                    playContentWithParentalCheck(iListItem)
//                            playContent(iListItem)
                })
            } else if (sharedPrefs.getLoginStatus()) {
                showContentPlaybackDialog(iListItem)
            } else
                loginPopup()
        }
    }

    protected fun showContentPlaybackDialog(iListItem: ContentItem?=null) {
        if(!isTabletLandscape)
            changeToPortraitMode()
//        val subscribedPack = sharedPrefs.getSubscribedPack()
        viewModel.onlyMessage = true
        if (shouldStartCancellationTrigger()) {
            viewModel.fetchBaIdList(sharedPrefs.getOriginalSubscriberId())
            return
        }
        handleSubscriptionDialog()
    }

    lateinit var trailerDisposable: Disposable

    private fun onDetailFetched(it: MetaDetails) {
        metaDetailResponse = it
        if (!isBackFromPlayer) {
            if (!(contentType.equals(TYPE_MOVIES, true)
                    || contentType.equals(TYPE_TV_SHOWS, true)
                    || contentType.equals(TYPE_SERIES, true)
                    || contentType.equals(TYPE_SERIES_CHILD, true)
                    || contentType.equals(TYPE_BRAND_CHILD, true)
                    || contentType.equals(TYPE_BRAND, true)
                    || contentType.equals(TYPE_WEB_SHORTS, true))
            ) {
                binding.ivPartnerLogo.setImageResource(R.drawable.logo_tatasky_details)
            }
            val mNonSubscribedPartnerList = HashSet<String>()
            sharedPrefs.getSubscribedPack()?.nonSubscribedPartnerList?.let { partnerList ->
                for (partner in partnerList) {
                    mNonSubscribedPartnerList.add((partner.partnerName ?: "").toLowerCase())
                }
            }
            val isPackAvailed = sharedPrefs.getSubscribedPack() != null
            val isPartnerSubscribed = isPackAvailed && !mNonSubscribedPartnerList.contains(
                (metaDetailResponse?.provider ?: "").toLowerCase()
            )
            if (!RENTAL.equals(detailsResponse?.data?.detail?.contractName, true)
                && isShowCrownOnContent(
                    isPartnerSubscribed = isPartnerSubscribed,
                    isGuestUser = !sharedPrefs.getLoginStatus(),
                    metaDetailResponse?.provider,
                    metaDetailResponse?.partnerSubscriptionType,
                    sharedPrefs.getSubscribedPack()?.appleRedemptionStatus
                )
            ) {
                contentAuth = false
                binding.ivPremiumIndicator.show()
            } else {
                contentAuth = true
                binding.ivPremiumIndicator.hide()
            }
            binding.ivPoster.show()
            updateProviderLogo(
                binding.ivPartnerLogo,
                metaDetailResponse?.provider ?: "",
                viewModel.sharedPrefs.getProviderLogo(),
                R.drawable.ic_detail_placeholder,
                viewModel.getCloudinaryUrl()
            )
            if (PROVIDER_APPLE.equals(detailsResponse?.data?.metaDetails?.provider, true) &&
                sharedPrefs.getSubscribedPack()?.appleRedemptionStatus?.equals(
                    "Pending",
                    true
                ) == true
                && isPartnerSubscribed
            ) {
                val ctaTitle = sharedPrefs.getConfigResponse()?.data?.config?.getLanguageVerbiage(
                    CATEGORY_APPLE_ACTIVATION_CTA
                )?.data?.header
                ctaTitle?.let {
                    binding.tvActivateAppleVeribage.text = it
                    binding.btnActivateApple.show()
                }
            }



            binding.btnPrimary.setSingleOnClick(1000) {
                handleBtnPlayClick()
            }
            binding.btnSecondary.setOnClickListener {
                handleBtnSecondaryClick()
            }

            parentContentType = contentType
            parentId = id
            if (it.parentContentType != contentType
                && (it.parentContentType == TYPE_BRAND
                    || it.parentContentType == TYPE_SERIES)
            ) {
//                isEpisode = true // Episode PI Handling
                val url = getCloudinaryUrl(
                    viewModel.getCloudinaryUrl(),
                    mWidth, (mWidth * 0.9999 * 0.56).toInt(),
                    it.getImageItem()
                )
                startEnterTransitionAfterLoadingImage(url, binding.ivPoster)
                it.vodContentType = contentType
//                it.contentType = contentType //Episode PI handling
                contentType = it.parentContentType ?: contentType
                it.contentType = contentType

            } else {
                val url = getCloudinaryUrl(
                    viewModel.getCloudinaryUrl() ?: "",
                    mWidth, (mWidth * 0.9999 * 0.56).toInt(),
                    it.boxCoverImage!!
                )
//                imageLoad(binding.ivPoster,url)
                startEnterTransitionAfterLoadingImage(url, binding.ivPoster)
            }
            if (it.parentContentType == TYPE_BRAND) {
                parentId = it.brandId ?: id
                parentContentType = TYPE_BRAND
            } else if (it.parentContentType == TYPE_SERIES) {
                parentId = it.seriesId ?: id
                parentContentType = TYPE_SERIES
            }
            id = parentId
            handleSecondaryBtnText()
        }
        //handlePrimaryButtonText()
    }

    private fun getAppleStatus(): Boolean {
        if (sharedPrefs.getSubscribedPack()?.appleRedemptionStatus?.equals("Pending",true) == true) {
            return false
        } else return sharedPrefs.getSubscribedPack()?.appleRedemptionStatus?.equals("consumed",true) == true
    }
    private fun getAppleStatusValue(): String {
        val status=sharedPrefs.getSubscribedPack()?.appleRedemptionStatus
        if(status==null)
            return "Null"
        else
            return status
    }
    private fun handleAppleTvPlayAction(playbackUrl: String?) {
        if (getAppleStatus() == true) {
            playInAppBrowserContent(playbackUrl)
        } else
            showAppleActivationPopup(false)
    }

    private fun playInAppBrowserContent(playbackUrl: String?) {

        val uri = Uri.parse(playbackUrl)
        try {
            actionOnPlayClick()
            trackOnThirdPartyPlayerPlay(playerModel)
            openChromeTab(uri)
        } catch (e: ActivityNotFoundException) {
            onError(
                ErrorModel(
                    message = getString(R.string.no_browser),
                    statusCode = CUSTOM_RESPONSE_CODE_ZEE5_ERROR
                )
            )
        }
    }

    private fun handleSecondaryBtnText() {
        /*if (detailsResponse != nullZEE5
            && (contentType.contains(TYPE_MOVIES)
                    || contentType.contains(TYPE_WEB_SHORTS))
        ) {*/
        var trailerProvider = detailsResponse?.data?.metaDetails?.provider ?: ""
        if (PROVIDER_CHAUPAL.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_HOTSTAR.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_ZEE5.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_APPLE.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_MXPLAYER.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_SONYLIV.equals(detailsResponse?.data?.metaDetails?.provider, true)
        ) {
            // these are deeplink partner so we can't use partnerTrailerInfo here
            detailsResponse?.data?.metaDetails?.partnerTrailerInfo = null
        } else if (
            PROVIDER_HUNGAMA.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_EROSNOW.equals(detailsResponse?.data?.metaDetails?.provider, true)
        ) {
            trailerUrl = detailsResponse?.data?.metaDetails?.partnerTrailerInfo
        }
        if (PROVIDER_EPIC_ON.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_DOCU_BAY.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_PLANET_MARATHI.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_NAMMAFLIX.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_LIONSGATE.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_HOICHOI.equals(detailsResponse?.data?.metaDetails?.provider, true)
        ) {
            detailsResponse?.data?.metaDetails?.partnerTrailerInfo =
                detailsResponse?.data?.detail?.trailerUrl
            trailerUrl = detailsResponse?.data?.detail?.trailerUrl
            trailerProvider = ""
        } else if (detailsResponse?.data?.metaDetails?.partnerTrailerInfo.isNullOrEmpty()) {
//                detailsResponse?.data?.detail?.dashWidewineTrailerUrl = "https://cdn-s3-ts.videoready.tv/bitmovin-outputs/dcdrights_storyofmaths_101_eng_f25_sd_3548.mp4/2be4f2df4418f0d28edf0fcc82d121c2.mpd"
            detailsResponse?.data?.metaDetails?.partnerTrailerInfo =
                detailsResponse?.data?.detail?.dashWidewineTrailerUrl
            trailerUrl = detailsResponse?.data?.detail?.dashWidewineTrailerUrl
            trailerProvider = ""
        }
        if (detailsResponse?.data?.metaDetails?.partnerTrailerInfo.isNullOrEmpty()) {
            binding.btnSecondary.hide()
            binding.buttonView?.hide()
            updateButtonLayoutWeight()
            return
        }

        isAutoPlayTrailer = viewModel.sharedPrefs.getAutoPlayTrailerOn()
        if (!isAutoPlayTrailer) {
            secondaryButtonState = SecondaryButtonStateEnum.STATE_TRAILER
            binding.btnSecondary.text = getString(R.string.watch_trailer)
            if (!isBackFromPlayer) {
                binding.apply {
                    btnSecondary.show()
                    btnSecondary.enable()
                    buttonView?.show()
                }
                updateButtonLayoutWeight()
            }
        } else {
            binding.btnSecondary.hide()
            binding.buttonView?.hide()
            updateButtonLayoutWeight()
        }
        if (!isTrailerInitialized) {
            binding.trailerView.init(
                trailerProvider,
                detailsResponse?.data?.detail?.cookies
            )
        }
        if (!binding.trailerView.isVisible
            && !isTrailerInitialized
        ) {
            playTrailer()
            if (isAutoPlayTrailer)
                trailerDisposable =
                    Completable.timer(
                        TRAILER_AUTO_START_TIME,
                        TimeUnit.SECONDS,
                        AndroidSchedulers.mainThread()
                    )
                        .subscribe(this::showTrailer)
        }
        /*} else {
            binding.trailerView.removeAllViews()
            binding.trailerView.onDestroyView()
        }*/
    }

    override fun onResume() {
        super.onResume()
        binding.executePendingBindings()
        binding.shareBtn.isClickable = true
        isPaused = false
        if (isNavigateToOther) {
            activity?.let {
                if(isTablet(it)){
                    if(it.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE)
                       disableFullscreenForLandscape()
                    else updateButtonLayoutWeight()
                }
            }
            isNavigateToOther = false
            fetchLastWatchData()
//            handleApiCall(false)
            if (PROVIDER_SONYLIV.equals(detailsResponse?.data?.metaDetails?.provider, true))
                SonyLIVSDKManager.getInstance().stopContent()
        }
        if (isPlayerStarted && ::orientationManager.isInitialized)
            orientationManager.enable()
        if (isBtnClicked)
            Handler(Looper.getMainLooper()).postDelayed(
                Runnable {
                    binding.scrollingContent.smoothScrollTo(0, 0)
                    isBtnClicked = false
                },
                500
            )

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("updateSubscription")
            ?.observe(viewLifecycleOwner) {
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("updateSubscription")
                handleApiCall(it)
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::id.isInitialized)
            outState.putString("id", id)
        if (::contentItem.isInitialized)
            outState.putString("contentItem", Gson().toJson(contentItem))
        super.onSaveInstanceState(outState)
    }

    private fun checkRental() {
        try {
            e(
                "checkRental",
                "isErrorInRental : $isErrorInRental, contentItem.rentalExpiry:${contentItem.rentalExpiry}"
            )
            if (RENTAL.equals(detailsResponse?.data?.detail?.contractName, ignoreCase = true) ||
                RENTAL.equals(contentItem.contractName, ignoreCase = true)
            ) {
                binding.whatsappBtn.hide()
                binding.shareBtn.hide()
                if (detailsResponse?.data?.metaDetails?.purchaseExpiry.isNullOrBlank()) {
                    detailsResponse?.data?.metaDetails?.purchaseExpiry = contentItem.rentalExpiry
                }
                if (detailsResponse?.data?.metaDetails?.purchaseExpiry.isNullOrBlank()) {
                    isErrorInRental = true
                    viewModel.onlyMessage = false
                    onError(
                        ErrorModel(
                            statusCode = 190,
                            message = getString(R.string.expired_content_msg)
                        )
                    )
                } else if (!isErrorInRental) {
                    detailsResponse?.data?.metaDetails?.purchaseExpiry?.toLong()?.let {
                        val hour = changeMillisToHours(it)
                        e("checkRental", "hour : $hour")
                        if (hour <= 0) {
                            val min = changeMillisToMins(it)
                            if (min <= 1) {
                                isErrorInRental = true
                                viewModel.onlyMessage = false
                                onError(
                                    ErrorModel(
                                        statusCode = 190,
                                        message = getString(R.string.expired_content_msg)
                                    )
                                )
                            }
                        }
                        binding.tvExpireValue.text =
                            "Expires in: " + getExpiryTime(it, TimeLevel.DAY, TimeLevel.MINUTE)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPlayerEnded() {
        isTrailerEnded = true
        binding.trailerView.alpha = 0.725f
//        binding.ivPoster.show()
        binding.ivPoster.alpha = 1f
//        hidePlayer()
        if (mIsInFullScreenMode)
            changeToPortraitMode()
    }

    override fun onPlayerFailure() {
        if (binding.trailerView.isTrailerStarted
            && binding.trailerView.isVisibile()
            && binding.trailerView.alpha != 0f
        ) {
            viewModel.onlyMessage = true
            onError(
                ErrorModel(
                    title = viewModel.VIDEO_UNAVAILABLE_TITLE,
                    message = viewModel.VIDEO_UNAVAILABLE_MESSAGE
                )
            )
        }
        isTrailerEnded = true
        binding.trailerView.alpha = 0f
        binding.ivPoster.alpha = 1f
        if (mIsInFullScreenMode)
            changeToPortraitMode()
    }

    override fun onPlayerReady() {
        isPlaying = true
        isTrailerEnded = false
        binding.trailerView.alpha = 1f
        binding.ivPoster.alpha = 0f
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (::orientationManager.isInitialized)
            orientationManager.enable()
        if ((activity as AppCompatActivity).supportFragmentManager.findFragmentByTag(DIALOG_TAG) != null
            || (activity as AppCompatActivity).supportFragmentManager.findFragmentByTag(
                LOGOUT_DIALOG_TAG
            ) != null
        ) {
            binding.trailerView.pauseTrailer(false)
        }
    }

    override fun onTrailerSoundChanged(sound: Boolean) {
        viewModel.sharedPrefs.setTrailerSound(sound)
    }

    override fun switchToFullScreen(switchFullScreen: Boolean) {
        (activity as LandingActivity).isScreenFullScreenMode=switchFullScreen
        if (switchFullScreen){
            changeToLandscapeMode()
        }
        else {
            if(!isTabletLandscape || !isDeviceTablet)
                changeToPortraitMode()
            else{
                if(mIsInFullScreenMode)
                    changeToTabletPortraitMode()
                else changeToLandscapeMode()
            }
        }
    }

    override fun onPlayerPause() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        isPlaying = false
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            disableOrientation()
        }
    }

    override fun showHideLoader(isLoader: Boolean) {
        viewModel.showHideLoader(isLoader)
    }

    override fun getContentTitle(): String {
        return detailsResponse?.data?.metaDetails?.getVodTitle()!!
    }

    override fun getNetWorkStatus(): Boolean {
        return isNetworkAvailable
    }

    override fun addToWatchList() {
        viewModel.markFavourite(parentId, getContentType(parentContentType), true)
    }

    override fun onControllerShown() {
        e("TrailerView", "inside onControllerShown")
        if (mIsInFullScreenMode) {
//            binding.rlToolbar.show()
            binding.imgBack.show()
        }
//        else {
//            binding.trailerView.pauseTrailer(false)
//        }
    }

    override fun onControllerHidden() {
        if (mIsInFullScreenMode) {
//            binding.rlToolbar.hide()
            binding.imgBack.hide()
        }
    }

    private fun showPlayer() {
        binding.trailerView.startPlayingTrailer()
        binding.trailerView.show()
        binding.ivPoster.alpha = 0f

        if (context != null)
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (!isTrailerEnded)
                    binding.miniProgress.show()
            } else
                binding.miniProgress.hide()
    }

    private fun hidePlayer() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            context?.let {
                if(!isTablet(it)){
                    changeToPortraitMode()
                }
            }
        }
        disableOrientation()
        isPlaying = false
        binding.miniProgress.hide()
        binding.trailerView.hide()
//        binding.ivPoster.show()
        binding.ivPoster.alpha = 1f

        binding.clDetails.show()
        binding.trailerView.onDestroyView()
        trailerUrl = null
    }

    private fun startTrailer(trailerUrl: String?, trailerLicense: String?) {
        if (context != null && isNetworkConnected(requireContext()) && !isPaused) {
            binding.trailerView.onDestroyView()
            /*Need to add this for QoE Probe Mitigation*/
            binding.trailerView.setPlayerModel(id, contentType,
                detailsResponse?.data?.metaDetails?.provider,
                detailsResponse?.data?.metaDetails?.title)
            binding.trailerView.startTrailer(
                trailerUrl,
                this,
                viewModel.sharedPrefs.getTrailerSound(),
                binding.miniProgress,
                trailerLicense,
                viewModel.sharedPrefs
            )
            isTrailerInitialized = true
        }
    }

    private fun showTrailer() {
        try {
            if (isNetworkConnected(requireContext())) {
                stopOtherApplicationAudio(context)
                showPlayer()
                detailAnalytics.trackPlayTrailer(
                    detailsResponse?.data?.metaDetails?.getVodTitle()!!,
                    detailsResponse?.data?.metaDetails?.contentType!!,
                    detailsResponse?.data?.metaDetails?.genre,
                    if (!sharedPrefs.isActivePack()) NO else YES,
                    detailsResponse?.data?.metaDetails?.provider ?: contentItem.provider,
                    detailsResponse?.data?.metaDetails?.audio,
                    pageName = (activity as? LandingActivity)?.getPageName()
                        ?: EVENT_VALUE_SOURCE_DETAIL,
                    railTitle = detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
                    railPosition = contentItem.railPosition,
                    railType = contentItem.origin,
                    railCategory = contentItem.railCategory,
                    contentItem.genres.getOrNull(0) ?: "",
                    detailsResponse?.data?.metaDetails?.provider ?: contentItem.provider,
                    contentAuth = if (contentAuth) YES else NO,
                    contentItem.categoryType,
                    contentItem.contentPosition ?: "",
                    detailsResponse?.data?.metaDetails?.rating ?: "",
                    contentParentTitle = detailsResponse?.data?.metaDetails?.getParentTitle()
                        ?: playerModel?.getParentTitle() ?: "",
                    contentFreeContent = if (!(PREMIUM.equals(
                            detailsResponse?.data?.metaDetails?.partnerSubscriptionType,
                            true
                        ))
                    ) YES else NO,
                    metaDetailResponse?.releaseYear ?: "",
                    deviceType = sharedPrefs.getDeviceType() ?: "",
                    metaDetailResponse?.actor?.joinToString(separator = ",") ?: "",
                    contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
                    sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                    packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                    autoPlayed = if (isAutoPlayTrailer && isTrailerInitialized) YES else NO,
                    liveContent = if (isLiveContent(
                            contentItem.contentType,
                            detailsResponse?.data?.metaDetails?.isLiveContent
                        )
                    ) YES else NO,
                    contentItem.contentConfigType
                )
                binding.trailerView.alpha = 1f
                binding.trailerView.trailerStarted(true)
                binding.btnSecondary.disable()
                if (::trailerDisposable.isInitialized)
                    trailerDisposable.dispose()
            } else {
                if (!isAutoPlayTrailer)
                    onNetworkError("", false)
            }
        } catch (e: Exception) {
        }
    }


    open fun changeToPortraitMode() {
        commonViewModel?.saveOrientation(OrientationManager.ScreenOrientation.PORTRAIT)
        activity?.let {
            if(!isTablet(it))
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        handlePortraitMode()
    }
    open fun changeToTabletPortraitMode(){
        disableFullscreenForLandscape()
    }

    open fun changeToLandscapeMode() {
        commonViewModel?.saveOrientation(OrientationManager.ScreenOrientation.LANDSCAPE)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        handleLandscapeMode()
    }


    private fun handlePrimaryBtnTextForEpisode(isReplay: Boolean) {
        if (metaDetailResponse?.vodId == viewModel.lastWatched?.vodId) {
            watchedSeconds = viewModel.lastWatched?.secondsWatched ?: 0
            var resumeBtnText = StringBuilder(getString(R.string.play))
            if (!isReplay) {
                binding.btnPrimary.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_play)
                resumeBtnText =
                    if (watchedSeconds > 0)
                        StringBuilder(getString(R.string.resume))
                    else StringBuilder(getString(R.string.play))
            } else {
                contentItem.secondsWatched = 0
                binding.btnPrimary.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_replay)
                resumeBtnText = StringBuilder(getString(R.string.replay))
            }
            if (contentType.equals(TYPE_BRAND, ignoreCase = true) ||
                contentType.equals(TYPE_SERIES, ignoreCase = true)
            ) {
                if (viewModel.lastWatched?.season != 0) {
                    isBrandResumeContent = true
                    resumeBtnText.append(" S${viewModel.lastWatched?.season}")
                }
                if (viewModel.lastWatched?.episodeId != 0) {
                    resumeBtnText.append(" E${viewModel.lastWatched?.episodeId}")
                }
            }
            binding.btnPrimary.text = resumeBtnText
            primaryButtonState = PrimaryButtonStateEnum.STATE_RESUME
        } else {
            var watchedSecondsEpisode = contentItem.secondsWatched
            val totalDurationEpisode = contentItem.durationInSeconds
            val isReplayEpisode = checkWatchedReplay(totalDurationEpisode, watchedSecondsEpisode)
            val resumeBtnText =
                if (watchedSecondsEpisode > 0)
                    StringBuilder(getString(R.string.resume))
                else StringBuilder(getString(R.string.play))
            if (watchedSecondsEpisode > totalDurationEpisode)
                watchedSecondsEpisode = totalDurationEpisode

            if (watchedSecondsEpisode in 1..totalDurationEpisode) {
                if (isReplayEpisode) {
                    contentItem.secondsWatched = 0
                    binding.btnPrimary.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_replay)
                    binding.btnPrimary.text = getString(R.string.replay)
                } else {
                    if (contentItem.season != "0") {
                        resumeBtnText.append(" S${contentItem?.season}")
                    }
                    if (contentItem.season != "0") {
                        resumeBtnText.append(" E${contentItem?.episodeId}")
                    }

                    binding.btnPrimary.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_play)
                    binding.btnPrimary.text = resumeBtnText
                }
            } else {
                binding.btnPrimary.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_play)
                binding.btnPrimary.text = getString(R.string.play)
            }
        }
    }

    open fun handlePrimaryButtonText() {
        if (PROVIDER_TATA_SKY.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            viewModel.lastWatched?.partnerSubscriptionType = "Free"
            detailsResponse?.data?.metaDetails?.partnerSubscriptionType = "Free"
        }
        partnerSubscriptionTypeForPlayBtn =
            detailsResponse?.data?.metaDetails?.partnerSubscriptionType
        if (detailFragmentArgs.playEpisode && !playEpisodeFromSeeAll) {
            detailFragmentArgs.contentItem?.let {
                if (PROVIDER_TATA_SKY.equals(detailsResponse?.data?.metaDetails?.provider, true))
                    it.offerIds = detailsResponse?.data?.detail?.offerIds
                mEpisodeClickListener.selectedEpisode(
                    it,
                    detailFragmentArgs.contentAnalyticsModel ?: emptyContentAnalyticsModel()
                )
            }
            playEpisodeFromSeeAll = true
        }
        var totalDuration = viewModel.lastWatched?.durationInSeconds ?: 0
        if (totalDuration == 0) {
            totalDuration = detailsResponse?.data?.metaDetails?.duration ?: 1
            viewModel.lastWatched?.durationInSeconds = totalDuration
        }
        val watchDuration = viewModel.lastWatched?.secondsWatched ?: 0
        val isReplay = checkWatchedReplay(totalDuration, watchDuration)
        totalContentDuration = detailsResponse?.data?.metaDetails?.duration ?: 0
        if (watchedSeconds > totalContentDuration)
            watchedSeconds = totalContentDuration
        if (isEpisode) {
            handlePrimaryBtnTextForEpisode(isReplay)
        } else {
            if (isReplay) {
                watchedSeconds = 0
                binding.btnPrimary.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_replay)
                val replayBtnText = StringBuilder(getString(R.string.replay))

                if (contentType.equals(TYPE_BRAND, ignoreCase = true) ||
                    contentType.equals(TYPE_SERIES, ignoreCase = true)
                ) {
                    partnerSubscriptionTypeForPlayBtn =
                        viewModel.lastWatched?.partnerSubscriptionType
                            ?: detailsResponse?.data?.metaDetails?.firstEpisodeSubscriptionType
                    if (viewModel.lastWatched?.season != 0) {
                        replayBtnText.append(" S${viewModel.lastWatched?.season}")
                    }
                    if (viewModel.lastWatched?.episodeId != 0) {
                        replayBtnText.append(" E${viewModel.lastWatched?.episodeId}")
                    }
                }
//            detailsResponse?.data?.metaDetails?.vodId = viewModel.lastWatched?.id
                binding.btnPrimary.text = replayBtnText
                primaryButtonState = PrimaryButtonStateEnum.STATE_REPLAY
            } else if (!isReplay && viewModel.lastWatched?.contentTitle != null) {
                watchedSeconds = viewModel.lastWatched?.secondsWatched ?: 0
                binding.btnPrimary.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_play)
                val resumeBtnText =
                    if (watchedSeconds > 0)
                        StringBuilder(getString(R.string.resume))
                    else StringBuilder(getString(R.string.play))
                if (contentType.equals(TYPE_BRAND, ignoreCase = true) ||
                    contentType.equals(TYPE_SERIES, ignoreCase = true)
                ) {
                    partnerSubscriptionTypeForPlayBtn =
                        viewModel.lastWatched?.partnerSubscriptionType
                            ?: detailsResponse?.data?.metaDetails?.firstEpisodeSubscriptionType
                    if (viewModel.lastWatched?.season != 0) {
                        isBrandResumeContent = true
                        resumeBtnText.append(" S${viewModel.lastWatched?.season}")
                    }
                    if (viewModel.lastWatched?.episodeId != 0) {
                        resumeBtnText.append(" E${viewModel.lastWatched?.episodeId}")
                    }
                }
                binding.btnPrimary.text = resumeBtnText
                primaryButtonState = PrimaryButtonStateEnum.STATE_RESUME

            } else {
                watchedSeconds = 0
                binding.btnPrimary.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_play)
                val replayBtnText = StringBuilder(getString(R.string.play))
                primaryButtonState = PrimaryButtonStateEnum.STATE_PLAY
                if (contentType.equals(TYPE_BRAND, ignoreCase = true) ||
                    contentType.equals(TYPE_SERIES, ignoreCase = true)
                ) {
                    partnerSubscriptionTypeForPlayBtn =
                        detailsResponse?.data?.metaDetails?.firstEpisodeSubscriptionType
                            ?: detailsResponse?.data?.metaDetails?.partnerSubscriptionType
                    if (detailsResponse?.data?.metaDetails?.season != 0) {
                        replayBtnText.append(" S${detailsResponse?.data?.metaDetails?.season}")
                    }
                    if (detailsResponse?.data?.metaDetails?.episodeId != 0) {
                        replayBtnText.append(" E${detailsResponse?.data?.metaDetails?.episodeId}")
                    }
                }
                binding.btnPrimary.text = replayBtnText
            }
        }

//        if(watchedSeconds>0) {
        if (viewModel.lastWatched?.partnerWebUrl?.isNotEmpty() == true)
            detailsResponse?.data?.metaDetails?.partnerWebUrl =
                viewModel.lastWatched?.partnerWebUrl ?: ""
        if (viewModel.lastWatched?.providerContentId?.isNotEmpty() == true)
            detailsResponse?.data?.metaDetails?.providerContentId =
                viewModel.lastWatched?.providerContentId
        if (viewModel.lastWatched?.dashWidewinePlayUrl?.isNotEmpty() == true)
            detailsResponse?.data?.detail?.dashWidewinePlayUrl =
                viewModel.lastWatched?.dashWidewinePlayUrl
        if (viewModel.lastWatched?.cookies?.isNotEmpty() == true)
            detailsResponse?.data?.detail?.cookies = viewModel.lastWatched?.cookies
        if (viewModel.lastWatched?.partnerDeepLinkUrl?.isNotEmpty() == true)
            detailsResponse?.data?.metaDetails?.partnerDeepLinkUrl =
                viewModel.lastWatched?.partnerDeepLinkUrl ?: ""
//        }
        if (PROVIDER_HOTSTAR.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            detailsResponse?.data?.detail?.dashWidewinePlayUrl =
                viewModel.lastWatched?.hotstarAppDeeplink
                    ?: detailsResponse?.data?.metaDetails?.hotstarAppDeeplink
                        ?: "hotstar://${detailsResponse?.data?.metaDetails?.providerContentId}"//1260049386
        }

        if (!isContentPlayable(partnerSubscriptionTypeForPlayBtn)) {
            primaryButtonState = PrimaryButtonStateEnum.STATE_BLOCKED_PLAYBACK //SUBSCRIBE
        }
        detailsResponse?.let { handleBrandSeriesRecyclerView(it) }
    }

    override fun showDeviceStatusLogout() {
        if (mIsInFullScreenMode) {
            changeToPortraitMode()
        }
        binding.trailerView.pauseTrailer(false)
        super.showDeviceStatusLogout()
    }

    protected fun isContentPlayable(partnerSubscriptionType: String?): Boolean {
        e(
            "isContentPlayable", "contractName : ${detailsResponse?.data?.detail?.contractName}," +
                " partnerSubscriptionType: ${partnerSubscriptionType}"
        )
        var partnerSubType = partnerSubscriptionType ?: PREMIUM
        e(
            "isContentPlayable", "sharedPrefs.getLoginStatus() : ${sharedPrefs.getLoginStatus()}," +
                " partnerSubType: $partnerSubType"
        )
        if (
            detailsResponse?.data?.metaDetails?.partnerId.equals("0") ||//TODO this line needs to be removed
            RENTAL.equals(detailsResponse?.data?.detail?.contractName, true)
        )
            partnerSubType = RENTAL
        isContentSubscribed =
            PROVIDER_TATA_SKY.equals(detailsResponse?.data?.metaDetails?.provider, true)
                || isFreeContent(
                detailsResponse?.data?.detail?.contractName,
                viewModel.sharedPrefs.getPartnerIdsList(),
                detailsResponse?.data?.metaDetails?.partnerId ?: "",
                sharedPrefs.getSubscribedPack()?.subscriptionStatus
            )
        e(
            "isContentPlayable", "isContentSubscribed : $isContentSubscribed" +
                " viewModel.sharedPrefs.getPartnerIdsList(): ${viewModel.sharedPrefs.getPartnerIdsList()}," +
                " detailsResponse?.data?.metaDetails?.partnerId: ${detailsResponse?.data?.metaDetails?.partnerId}"
        )
        return if (!sharedPrefs.getLoginStatus() && PREMIUM.equals(partnerSubType, true))
            false
        else if (sharedPrefs.getLoginStatus()) { //TODO Need to remove this
            (!PREMIUM.equals(partnerSubType, true) ||
                detailsResponse?.data?.detail?.contractName.equals("RENTAL") ||
                isContentSubscribed)
                && viewModel.sharedPrefs.contentPlaybackAllowed() && !shouldStartCancellationTrigger()
        } else
            true
    }

    private fun handleBtnSecondaryClick() {
        when (secondaryButtonState) {
            SecondaryButtonStateEnum.STATE_TRAILER -> {
                showTrailer()
            }
        }
    }

    private fun playTrailer() {
        if (isAutoPlayTrailer) {
            miscAnalytics.trackMixPanelTrailerAutoplay(
                contentTitle = detailsResponse?.data?.metaDetails?.getVodTitle()!!,
                contentType = detailsResponse?.data?.metaDetails?.contentType!!,
                contentGenre = detailsResponse?.data?.metaDetails?.genre?.joinToString(",") ?: "",
                pageName = (activity as? LandingActivity)?.getPageName()
                    ?: EVENT_VALUE_SOURCE_DETAIL,
                railTitle = detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
                railPosition = contentItem.railPosition,
                railType = contentItem.origin,
                railCategory = contentItem.railCategory,
                contentLanguage = detailsResponse?.data?.metaDetails?.audio?.joinToString(",")
                    ?: "",
                contentGenrePrimary = detailsResponse?.data?.metaDetails?.genre?.getOrNull(0),
                contentPartner = detailsResponse?.data?.metaDetails?.provider
                    ?: contentItem.provider,
                contentAuth = if (contentAuth) YES else NO,
                contentCategory = contentItem.categoryType ?: "",
                contentPosition = contentItem.contentPosition ?: "",
                contentRating = detailsResponse?.data?.metaDetails?.rating ?: "",
                contentParentTitle = detailsResponse?.data?.metaDetails?.getParentTitle()
                    ?: playerModel?.getParentTitle() ?: "",
                contentFreeContent = if (!(PREMIUM.equals(
                        detailsResponse?.data?.metaDetails?.partnerSubscriptionType,
                        true
                    ))
                ) YES else NO,
                contentReleaseYear = metaDetailResponse?.releaseYear ?: "",
                deviceType = sharedPrefs.getDeviceType() ?:"",
                actors = metaDetailResponse?.actor?.joinToString(separator = ",") ?: "",
                packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                source = contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
                packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                autoPlayed = YES,
                liveContent = if (isLiveContent(
                        contentItem.contentType,
                        detailsResponse?.data?.metaDetails?.isLiveContent
                    )
                ) YES else NO,
                contentConfigType = contentItem.contentConfigType,
                contentLanguagePrimary = detailsResponse?.data?.metaDetails?.audio?.getOrNull(0)
                    ?: "",
            )
        }
        if (PROVIDER_CHAUPAL.equals(
                detailsResponse?.data?.metaDetails?.provider,
                ignoreCase = true
            )
            && trailerUrl == null
        ) {
            val trailerId = detailsResponse?.data?.metaDetails?.partnerTrailerInfo ?: ""
            playChaupalContent(trailerId, contentType, true, false)
        } else if (PROVIDER_SHEMAROO.equals(
                detailsResponse?.data?.metaDetails?.provider,
                ignoreCase = true
            )
            && trailerUrl == null
        ) {
            val smartURL = detailsResponse?.data?.metaDetails?.partnerTrailerInfo ?: ""
            playShemarooMeContent(smartURL, true, false)
        } else if ((PROVIDER_VOOTKIDS.equals(
                detailsResponse?.data?.metaDetails?.provider,
                true
            )
                || PROVIDER_VOOTSELECT.equals(
                detailsResponse?.data?.metaDetails?.provider,
                true
            ))
            && trailerUrl == null
        ) {
            playVootContent(
                detailsResponse?.data!!.metaDetails!!.partnerTrailerInfo ?: "",
                true,
                false,
                contentType
            )
        } else if ((PROVIDER_VOOTKIDS.equals(
                detailsResponse?.data?.metaDetails?.provider,
                true
            ))
            && trailerUrl == null
        ) {
            playVootContent(
                detailsResponse?.data!!.metaDetails!!.partnerTrailerInfo ?: "",
                true,
                false,
                contentType, isKids = true
            )
        } else {
            detailsResponse?.data?.detail?.dashWidewineTrailerUrl = trailerUrl
            startTrailer(detailsResponse?.data?.detail?.dashWidewineTrailerUrl, null)
        }
    }

    private fun scrollToSeasonsTab() {
        val scrollTo = ((binding.tabLayoutSeasonsParent as View).top)
        binding.scrollingContent.smoothScrollTo(0, scrollTo)
    }

    private fun handleBtnPlayClick() {
        partnerSubscriptionTypeOnPlay = partnerSubscriptionTypeForPlayBtn ?: PREMIUM
        try {
            if (isNetworkConnected(requireContext())) {
                binding.trailerView.unregisterTrailerNetworkCallback()
                when (primaryButtonState) {
                    PrimaryButtonStateEnum.STATE_PLAY, PrimaryButtonStateEnum.STATE_RESUME, PrimaryButtonStateEnum.STATE_REPLAY -> {
                        isBtnClicked = true
                        mIsMovieOrShowStarted = true
                        if (isContentPlayable(partnerSubscriptionTypeForPlayBtn)) {
                            checkForGuestUserPlaybackEligibility({
                                pauseTrailerInvoked()
                                playContentWithParentalCheck(null)
                            })
                        } else if (sharedPrefs.getLoginStatus()) {
                            pauseTrailerInvoked()
                            showContentPlaybackDialog()
                        } else
                            loginPopup()
                    }
                    PrimaryButtonStateEnum.STATE_BLOCKED_PLAYBACK -> {
                        if (sharedPrefs.getLoginStatus()) {
                            pauseTrailerInvoked()
                            showContentPlaybackDialog()
                        } else {
                            //Show Login Prompt
                            loginPopup()
                        }
                    }
                }
            } else {
                onNetworkError("", false)
            }
        } catch (e: Exception) {
        }

    }

    private fun loginPopup(moveToSubscription: Boolean = true) {
        pauseTrailerInvoked()
        if (!moveToSubscription) {
            viewModel.getPreviouslyUsedMobileNumbers()
            return
        }
        if (partnerSubscriptionTypeOnPlay == null)
            partnerSubscriptionTypeOnPlay = PREMIUM
        if (PREMIUM.equals(partnerSubscriptionTypeOnPlay, true)) {
            if (!sharedPrefs.isManagedAppEnabled())
                startActivity(
                    getSubscriptionActivityIntent(
                        activity,
                        fromScreen = SOURCE_PLAY_CLICK,
                        startPackListing = true,
                        partnerId = detailsResponse?.data?.metaDetails?.partnerId ?: ""
                    )
                )
            else
                showMiniDrawer(
                    context = activity,
                    fromScreen = SOURCE_PLAY_CLICK,
                    startPackListing = true
                )
        } else {
            viewModel.getPreviouslyUsedMobileNumbers()
        }
    }

    private fun showDetailViews() {
        var delayTime = 5L
        if (PROVIDER_HUNGAMA.equals(
                detailsResponse?.data?.metaDetails?.provider,
                true
            ) && trailerUrl?.isNotEmpty() == true
        )
            delayTime = 1000L
        //add delay of 500 milliseconds if Hungama trailer content
        Handler().postDelayed(Runnable {
            viewModel.showHideLoader(false)
            binding.scrollingContent.show()
            if (!isDetailShown) {
//                binding.detailDesc.movementMethod = MovementMethod()
//                makeTextViewResizable(
//                    binding.detailDesc, 3,
//                    " + More", true
//                )
                populateDataInViews(metaDetailResponse)
            }
            if (RENTAL.equals(detailsResponse?.data?.detail?.contractName, true)) {
                binding.tvExpireValue.show()
            }
            isDetailShown = true


            binding.clDetails.show()

        }, delayTime)
    }


    private fun populateDataInViews(meta: MetaDetails?) {
        binding.detailDesc.text = meta?.getVodDescription()
        binding.model = meta
//        if(!meta?.getVodDescription().isNullOrEmpty())
//            makeTextViewResizable(binding.detailDesc, 2, "...")
        binding.more.setOnClickListener(object : SingleClickListener() {
            override fun onClicked(v: View?) {
                miscAnalytics.trackMixPanelSynopsisMoreClick(
                    contentTitle = detailsResponse?.data?.metaDetails?.getVodTitle()!!,
                    contentType = detailsResponse?.data?.metaDetails?.contentType!!,
                    contentGenre = detailsResponse?.data?.metaDetails?.genre?.joinToString(",")
                        ?: "",
                    pageName = (activity as? LandingActivity)?.getPageName()
                        ?: EVENT_VALUE_SOURCE_DETAIL,
                    railTitle = detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
                    railPosition = contentItem.railPosition,
                    railType = contentItem.origin,
                    railCategory = contentItem.railCategory,
                    contentLanguage = detailsResponse?.data?.metaDetails?.audio?.joinToString(",")
                        ?: "",
                    contentGenrePrimary = detailsResponse?.data?.metaDetails?.genre?.getOrNull(0),
                    contentPartner = detailsResponse?.data?.metaDetails?.provider
                        ?: contentItem.provider,
                    contentAuth = if (contentAuth) YES else NO,
                    contentCategory = contentItem.contentType,
                    contentPosition = contentItem.contentPosition,
                    contentRating = detailsResponse?.data?.metaDetails?.rating ?: "",
                    contentParentTitle = detailsResponse?.data?.metaDetails?.getParentTitle()
                        ?: playerModel?.getParentTitle() ?: "",
                    contentFreeContent = if (!(PREMIUM.equals(
                            detailsResponse?.data?.metaDetails?.partnerSubscriptionType,
                            true
                        ))
                    ) YES else NO,
                    contentReleaseYear = metaDetailResponse?.releaseYear ?: "",
                    deviceType = sharedPrefs.getDeviceType() ?:"",
                    actors = metaDetailResponse?.actor?.joinToString(separator = ",") ?: "",
                    packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                    source = contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
                    packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                    autoPlayed = if (isAutoPlayTrailer && isTrailerInitialized) YES else NO,
                    liveContent = if (isLiveContent(
                            contentItem.contentType,
                            detailsResponse?.data?.metaDetails?.isLiveContent
                        )
                    ) YES else NO,
                    contentConfigType = contentItem.contentConfigType,
                    contentLanguagePrimary = detailsResponse?.data?.metaDetails?.audio?.getOrNull(0)
                        ?: "",
                )
                findNavController().navigateSafe(
                    DetailsFragmentDirections.actionMoreInfoBotttomSheet(
                        metaDetailResponse
                    )
                )
            }
        })
    }


    override fun onError(errorModel: ErrorModel) {
        if (errorModel.statusCode == CUSTOM_RESPONSE_CODE_ZEE5_ERROR) {
            showToast(context, errorModel.message ?: viewModel.VIDEO_UNAVAILABLE_MESSAGE_URL)
            return
        }
        if (viewModel.onlyMessage) {
            super.onError(errorModel)
            return
        } else if (errorModel.statusCode != 190) {
            if (detailsResponse != null) return
        }
        showDialog(DialogModel(false, null, errorModel.message, "Ok", null),
            object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    hideDialog()
                    if (!viewModel.onlyMessage)
                        findNavController().navigateUp()
                }

                override fun onCloseButtonClick() {
                    hideDialog()
                    findNavController().navigateUp()
                }

                override fun onSecondaryButtonClick() {
                    hideDialog()
                }
            })
    }

    private fun handleBrandSeriesRecyclerView(response: DetailsResponse) {
        if (!alreadyAddedSeason) {
            selectedSeriesId =
                viewModel.lastWatched?.seriesId ?: response.data?.metaDetails?.seriesId

            if (response.data?.metaDetails?.parentContentType == TYPE_SERIES
                || response.data?.metaDetails?.contentType == TYPE_SERIES
            ) {
                alreadyAddedSeason = true
                binding.seriesRecycler.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                binding.seriesRecycler.adapter = SeriesAdapter(
                    mSeriesClickListener,
                    mutableListOf(),
                    0,
                    primaryButtonState,
                    contentItem.contentType,
                    contentItem.id,
                    viewModel.getCloudinaryUrl(),
                    this,
                    mEpisodeInfoClickListener,
                    isContentSubscribed,
                )

                binding.seriesRecycler.clearOnScrollListeners()
                binding.seriesRecycler.addOnScrollListener(CustomScrollListener {
                    mRailScrollListener.onRailScrolled(
                        TYPE_SEASONS,
                        1,
                        contentItem.contentType,
                        contentItem.railCategory
                    )
                })

                binding.tabLayoutSeasons.removeAllTabs()
                val tab =
                    LayoutInflater.from(context).inflate(
                        R.layout.custom_tab,
                        null
                    ) as LinearLayout
                val tabItem = tab.findViewById(R.id.tabItem) as TextView
                tabItem.text = getString(R.string.other_episodes)
                binding.tabLayoutSeasons.addTab(
                    binding.tabLayoutSeasons.newTab().setCustomView(
                        tab
                    )
                )
                binding.tabLayoutSeasonsParent.show()
                binding.rlSeasons.show()
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        binding.tabLayoutSeasons.getTabAt(mSelectedPosition)?.select()
                    }, 500
                )
            } else if (response.data?.metaDetails?.parentContentType == TYPE_BRAND
                || response.data?.metaDetails?.contentType == TYPE_BRAND
            ) {
                if ((response.data?.seriesList?.size ?: 0) == 0) {
                    showDetailViews()
                    if (!alreadyFetchedRelatedRail)
                        handleRecommendationApi()
                    return
                }
                alreadyAddedSeason = true
                binding.seriesRecycler.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                binding.seriesRecycler.adapter = SeriesAdapter(
                    mSeriesClickListener,
                    mutableListOf(),
                    0,
                    primaryButtonState,
                    contentItem.contentType,
                    contentItem.id,
                    viewModel.getCloudinaryUrl(),
                    this,
                    mEpisodeInfoClickListener,
                    isContentSubscribed,
                )

                binding.seriesRecycler.clearOnScrollListeners()
                binding.seriesRecycler.addOnScrollListener(CustomScrollListener {
                    mRailScrollListener.onRailScrolled(
                        TYPE_SEASONS,
                        1,
                        contentItem.contentType,
                        contentItem.railCategory
                    )
                })

                binding.tabLayoutSeasons.removeAllTabs()
                response.data?.seriesList?.forEachIndexed { index, season ->
                    val tab =
                        LayoutInflater.from(context).inflate(
                            R.layout.custom_tab,
                            null
                        ) as LinearLayout
                    val tabItem = tab.findViewById(R.id.tabItem) as TextView
                    tabItem.text = season.seriesName
                    if (season.id == selectedSeriesId)
                        mSelectedPosition = index
                    binding.tabLayoutSeasons.addTab(
                        binding.tabLayoutSeasons.newTab().setCustomView(
                            tab
                        )
                    )
                }
                binding.tabLayoutSeasonsParent.show()
                binding.rlSeasons.show()
//                binding.btnSecondary.show()
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        binding.tabLayoutSeasons.getTabAt(mSelectedPosition)?.select()
                    }, 500
                )
                var id1: String? = null
                if ((response.data?.seriesList?.size ?: 0) > mSelectedPosition) {
                    id1 = response.data!!.seriesList!![mSelectedPosition].id.toString()
                    if (id1 != null) {
                        selectedSeriesId = id1
                    }
                }
                binding.tabLayoutSeasons.addOnTabSelectedListener(object :
                    TabLayout.OnTabSelectedListener {
                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        tab?.let {
                            e(
                                "fetchSeries",
                                "inside onTabSelected seriesLastOffset :  $seriesLastOffset"
                            )
                            val id2: String? =
                                response.data!!.seriesList!![it.position].id.toString()
                            if (id2 != null && id2 != id1) {
                                clearSeriesList = true
                                seriesLastOffset = 0
                                id1 = id2
                                selectedSeriesId = id2
                                mSelectedPosition = it.position
                                //(binding.seriesRecycler.adapter as SeriesAdapter).clearList()
                                viewModel.clearSeriesApiCalls()
//                                fetchSeries(false, 0, viewModel.SERIES_LIMIT, false, true)
                                var pageOffset = 0
                                /*if(selectedSeriesId == viewModel.lastWatched?.seriesId){
                                    pageOffset = viewModel.lastWatched?.episodeId?:0
                                } else {
                                    pageOffset = 0
                                }*/
                                fetchSeries(
                                    false,
                                    pageOffset,
                                    viewModel.SERIES_LIMIT,
                                    pageOffset > 0,
                                    true
                                )
                            }
                        }
                    }
                })
            } else {
                if (!alreadyFetchedRelatedRail)
                    handleRecommendationApi()
            }
        }
        if (alreadyAddedSeason) {
            clearSeriesList = true
            var pageOffset = 0//viewModel.lastWatched?.episodeId ?:0
            var isLastWatch = false

            /*if(selectedSeriesId == viewModel.lastWatched?.seriesId){
                pageOffset = viewModel.lastWatched?.episodeId?:0
            } else {
                pageOffset = 0
            }*/

            if (pageOffset > 0) {
                isLastWatch = true
            }

            val pageLimit = viewModel.SERIES_LIMIT + seriesLastOffset
            fetchSeries(
                loader = false,
                pageOffset = pageOffset, pageLimit = pageLimit,
                isLastWatch = isLastWatch, isAutoScroll = true
            )
        } else {
            showDetailViews()
        }
    }

    private fun fetchSeries(
        loader: Boolean, pageOffset: Int, pageLimit: Int,
        isLastWatch: Boolean, isAutoScroll: Boolean
    ) {
        viewModel.fetchSeriesList(
            selectedSeriesId!!,
            pageOffset,
            pageLimit,
            loader,
            isLastWatch,
            isAutoScroll
        )
    }

    private fun getDetailsContentType(contentType: String): String {
        return when {
            contentType.contains(TYPE_BRAND_CHILD) || contentType.contains(TYPE_SERIES_CHILD)
                || contentType.contains(TYPE_MOVIES) || contentType.contains(TYPE_WEB_SHORTS)
                || contentType.contains(TYPE_TV_SHOWS) -> DetailTypeEnum.VOD.type
            contentType.contains(TYPE_BRAND) -> DetailTypeEnum.BRAND.type
            contentType.contains(TYPE_SERIES) -> DetailTypeEnum.SERIES.type
            contentType.contains(TYPE_CATCH_UP) -> DetailTypeEnum.CATCHUP.type
            else -> ""
        }
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
        pauseTrailerInvoked()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun pauseTrailerInvoked() {
        if (isTrailerInitialized)
            binding.trailerView.pauseTrailer(false)
        if (::trailerDisposable.isInitialized)
            trailerDisposable.dispose()
        disableOrientation()
        binding.trailerView.trailerStarted(false)
    }

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    protected fun handleLandscapeMode() {
        e("HungamaPlayerFragmnet", "inside handleLandscapeMode")
        val layoutParams = binding.trailerView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.height = mWidth
        activity?.let {
            if(isTablet(it)) {
                layoutParams.width=ConstraintLayout.LayoutParams.MATCH_PARENT
                layoutParams.height=ConstraintLayout.LayoutParams.MATCH_PARENT
            }
        }
        binding.trailerView.layoutParams = layoutParams
        val posterLayoutParams = binding.ivPoster.layoutParams as ConstraintLayout.LayoutParams
        posterLayoutParams.height = mWidth
        activity?.let {
            if(isTablet(it)){
                posterLayoutParams.width=ConstraintLayout.LayoutParams.MATCH_PARENT
                posterLayoutParams.height=ConstraintLayout.LayoutParams.MATCH_PARENT
            }
        }
        binding.ivPoster.layoutParams = posterLayoutParams
        val playerFrameParams = binding.playerFrame.layoutParams as ConstraintLayout.LayoutParams
        playerFrameParams.height = mWidth
        activity?.let {
            if(isTablet(it)) {
                playerFrameParams.width=ConstraintLayout.LayoutParams.MATCH_PARENT
                playerFrameParams.height=ConstraintLayout.LayoutParams.MATCH_PARENT
            }
        }
        binding.playerFrame.layoutParams = playerFrameParams
        binding.scrollingContent.hide()
        binding.miniProgress.hide()
        binding.watchlistBtn.hide()
        if (isTrailerInitialized)
            binding.trailerView.switchToFullScreen()
        mIsInFullScreenMode = true
        (activity as LandingActivity).isScreenFullScreenMode=true
    }

    protected fun disableFullscreenForLandscape(){
        commonViewModel?.saveOrientation(OrientationManager.ScreenOrientation.LANDSCAPE)
        var widthTablet:Int = 0
        var heightTablet:Int =0
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        context?.let {
            widthTablet=it.resources.getDimension(R.dimen.player_view_width).toInt()
            heightTablet=it.resources.getDimension(R.dimen.player_view_height).toInt()
        }
        updateButtonLayoutWeight()

        val layoutParams = binding.trailerView.layoutParams as ConstraintLayout.LayoutParams
        binding.trailerView.layoutParams = setPlayerViewLayouts(layoutParams,widthTablet,heightTablet)
        val posterLayoutParams = binding.ivPoster.layoutParams as ConstraintLayout.LayoutParams
        binding.ivPoster.layoutParams = setPlayerViewLayouts(posterLayoutParams,widthTablet,heightTablet)
        val playerFrameParams = binding.playerFrame.layoutParams as ConstraintLayout.LayoutParams
        binding.playerFrame.layoutParams = setPlayerViewLayouts(playerFrameParams,widthTablet,heightTablet)
        binding.ivPoster.alpha = 1f
        binding.imgBack.show()
        binding.watchlistBtn.show()
        if (binding.trailerView.isVisible && !isTrailerEnded)
            binding.miniProgress.show()
        if (isTrailerInitialized)
            binding.trailerView.switchToMiniScreen()
        mIsInFullScreenMode = false
        binding.scrollingContent.show()
        (activity as LandingActivity).isScreenFullScreenMode=false
    }

    private fun setPlayerViewLayouts(layoutParams: ConstraintLayout.LayoutParams, width: Int, height:Int):ConstraintLayout.LayoutParams{
        layoutParams.width=width
        layoutParams.height=height
        layoutParams.startToStart = ConstraintSet.PARENT_ID
        layoutParams.endToEnd = ConstraintSet.PARENT_ID
        return layoutParams
    }


    protected fun handlePortraitMode() {
        e("HungamaPlayerFragmnet", "inside handlePortraitMode")
        if(!isDeviceTablet) {
            if (!mIsInFullScreenMode) return
        }
        else updateButtonLayoutWeight()

        val layoutParams = binding.trailerView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        layoutParams.dimensionRatio = "16:9"
        binding.trailerView.layoutParams = layoutParams
        val posterLayoutParams = binding.ivPoster.layoutParams as ConstraintLayout.LayoutParams
        posterLayoutParams.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        posterLayoutParams.dimensionRatio = "16:9"
        binding.ivPoster.layoutParams = posterLayoutParams
        val playerFrameParams = binding.playerFrame.layoutParams as ConstraintLayout.LayoutParams
        playerFrameParams.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        playerFrameParams.dimensionRatio = "16:9"
        binding.playerFrame.layoutParams = playerFrameParams
        binding.ivPoster.alpha = 1f
        binding.imgBack.show()
        binding.watchlistBtn.show()
        if (binding.trailerView.isVisible && !isTrailerEnded)
            binding.miniProgress.show()
        if (isTrailerInitialized)
            binding.trailerView.switchToMiniScreen()
        mIsInFullScreenMode = false
        binding.scrollingContent.show()
        (activity as? LandingActivity)?.handlePortraitForBottomNav()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                if (mIsInFullScreenMode) {
                    if(isTabletLandscape && isDeviceTablet)
                        changeToTabletPortraitMode()
                    else changeToPortraitMode()
                } else {
                    if (refreshHomeRequired) {
                        //commonViewModel?.refreshHome?.postValue(SingleEvent(true))
                        refreshHomeRequired = false
                    }
                    findNavController().popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }

    private fun checkIrdetoSecurity(seriesItem: ContentItem?) {
        var message = ""
        if (!MyApp.initSucceed) {
            message = getString(R.string.aca_failed)
        } else {
            val itacResult = MyApp.getITACAgent().check(
                {
                    if (seriesItem == null)
                        playerModel = viewModel.generatePlayerModel(detailsResponse!!)
                    else {
                        playerModel = viewModel.generatePlayerModel(
                            seriesItem,
                            detailsResponse?.data?.metaDetails?.taShowType ?: ""
                        )
                        if (RENTAL.equals(contentItem.contractName, ignoreCase = true)) {
                            val id = if (seriesItem.vodId.isNotEmpty()) seriesItem.vodId
                            else seriesItem.id
                            val token = viewModel.getToken(id)
                            if (!token.isNullOrEmpty()) {
                                playerModel?.setLA_URL(playerModel?.getLA_URL() + "&ls_session=" + token)
                                playerModel?.let { playerModel: PlayerModel ->
                                    navigateToPlayer(
                                        playerModel
                                    )
                                }
                            } else {
                                viewModel.generateControlToken(
                                    detailsResponse?.data?.detail?.offerIds?.epids!!,
                                    id,
                                    true
                                )
                            }
                        }
                    }

                },
                null,
                ITACStatus()
            )
            d("ACA", "itacResult: $itacResult")

            if (itacResult != ITACResult.ITAC_OK) {
                message = "check failed."
            } else {
                message = acaSecurityCheck()
            }
        }
        if (!TextUtils.isEmpty(message)) {
            showACAAlert(message)
        }
    }

    private fun playContentWithParentalCheck(iListItem: ContentItem?) {
        this.iListItem = iListItem
        if (sharedPrefs.getLoginStatus() && sharedPrefs.isParentalPinExists()) {
            hideTrailerButtonOnAutoEpPlay()
            //do rating check - true then playback
            //if rating gives false then validate pin then success - trigger playback else show toast
            commonViewModel?.validateContentRating(detailsResponse?.data?.metaDetails?.masterRating)
        } else {
            playAfterRattingCheck()
        }
    }

    private fun hideTrailerButtonOnAutoEpPlay() {
        if (detailFragmentArgs.playEpisode &&
            secondaryButtonState == SecondaryButtonStateEnum.STATE_TRAILER
        ) {
            binding.btnSecondary.disable()
            binding.btnSecondary.hide()
            binding.buttonView?.hide()
            updateButtonLayoutWeight()

        }
    }

    //to do for episode check
    private fun checkAndPlayContent() {
        if (isEpisode && (metaDetailResponse?.vodId != viewModel.lastWatched?.vodId)) {
            playContent(contentItem)
        } else {
            playContent()
        }
    }

    private fun playAfterRattingCheck() {
        if (iListItem == null)
            checkAndPlayContent()
        else
            playContent(iListItem!!)
    }

    private fun playContent() {
        viewModel.lastWatched?.secondsWatched = watchedSeconds
        if (PROVIDER_TATA_SKY.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            detailsResponse?.data?.detail?.offerIds?.epids?.let {
                val epids = viewModel.lastWatched?.offerIds?.epids ?: it
                if (!viewModel.tvodToken.isNullOrEmpty()
                    && RENTAL.equals(contentItem.contractName, ignoreCase = true)
                ) {
                    playerModel?.setLA_URL(playerModel?.getLA_URL() + "&ls_session=" + viewModel.tvodToken)
                    playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
                } else {
                    viewModel.generateControlToken(
                        epids,
                        id,
                        true
                    )
                } ?: kotlin.run {
                    showToast(context, "No Entitlements found")
                }
            }

        } else if (PROVIDER_HUNGAMA.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            val playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            navigateToPlayer(playerModel)
        } else if (PROVIDER_SHEMAROO.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playShemarooMeContent(
                detailsResponse?.data!!.metaDetails!!.partnerDeepLinkUrl,
                false,
                true
            )
        } else if (PROVIDER_VOOTSELECT.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playVootContent(
                detailsResponse?.data!!.metaDetails!!.providerContentId ?: "",
                false,
                true,
                playerModel?.getContentType() ?: "TV_SHOWS",
                partnerSubscriptionType = partnerSubscriptionTypeForPlayBtn,
                partnerDeepLinkUrl = detailsResponse?.data?.metaDetails?.partnerDeepLinkUrl ?: ""
            )
        } else if (PROVIDER_VOOTKIDS.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playVootContent(
                detailsResponse?.data!!.metaDetails!!.providerContentId ?: "",
                false,
                true,
                playerModel?.getContentType() ?: "TV_SHOWS",
                isKids = true,
                partnerSubscriptionType = partnerSubscriptionTypeForPlayBtn,
                partnerDeepLinkUrl = detailsResponse?.data?.metaDetails?.partnerDeepLinkUrl ?: ""
            )
        } else if (PROVIDER_CURIOSITY_STREAM.equals(
                detailsResponse?.data?.metaDetails?.provider,
                true
            )
        ) {
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            detailsResponse?.data?.detail?.dashWidewinePlayUrl =
                viewModel.lastWatched?.dashWidewinePlayUrl
                    ?: detailsResponse?.data?.detail?.dashWidewinePlayUrl
            if (detailsResponse?.data?.detail?.dashWidewinePlayUrl == null
                && viewModel.lastWatched?.vodId != null
            ) {
                viewModel.fetchCSBoxsetDetails(
                    viewModel.lastWatched?.vodId!!,
                    detailsResponse?.data?.metaDetails?.provider!!
                )
            } else
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
        } else if (PROVIDER_ZEE5.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            detailsResponse?.data?.detail?.dashWidewinePlayUrl =
                detailsResponse?.data?.metaDetails?.partnerWebUrl
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playZee5Content(partnerSubscriptionTypeForPlayBtn)
        } else if (PROVIDER_HOTSTAR.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)

            playHotstarContent()
        } else if (PROVIDER_EROSNOW.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playErosNowContent()
        } else if (PROVIDER_SONYLIV.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            checkAndPlaySonyContent()
        } else if (PROVIDER_EPIC_ON.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_DOCU_BAY.equals(detailsResponse?.data?.metaDetails?.provider, true)
        ) {
            playerModel = null
            detailsResponse?.data?.detail?.dashWidewinePlayUrl =
                viewModel.lastWatched?.playUrl ?: detailsResponse?.data?.detail?.playUrl
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            if (detailsResponse?.data?.detail?.playUrl == null
                && viewModel.lastWatched?.vodId != null
            ) {
                viewModel.fetchCSBoxsetDetails(
                    viewModel.lastWatched?.vodId!!,
                    detailsResponse?.data?.metaDetails?.provider!!
                )
            } else
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
        } else if (PROVIDER_HOICHOI.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null
            detailsResponse?.data?.detail?.dashWidewinePlayUrl =
                viewModel.lastWatched?.playUrl ?: detailsResponse?.data?.detail?.playUrl ?: ""
            playbackUrl = detailsResponse?.data?.detail?.dashWidewinePlayUrl

            playerModel = viewModel.generatePlayerModel(detailsResponse!!)

            val token = viewModel.getToken(id)
            if (playerModel?.getPlaybackUrl().isNullOrEmpty())
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
            else if (!token.isNullOrEmpty()) {
                playHoiChoiContent(playbackUrl, token)
            } else {
                val request = HoichoiRequest(partner = "hoichoi")
                viewModel.fetchHoichoiPlaybackUrl(request, id)
            }
        } else if (PROVIDER_MXPLAYER.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null
            playbackUrl = detailsResponse?.data?.detail?.dashWidewinePlayUrl
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playMXPlayer()
        } else if (PROVIDER_CHAUPAL.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playChaupalContent(
                detailsResponse?.data?.metaDetails?.providerContentId ?: "",
                playerModel?.getContentType() ?: contentType, false, true
            )
        } else if (PROVIDER_PLANET_MARATHI.equals(
                detailsResponse?.data?.metaDetails?.provider,
                true
            )
        ) {
            playerModel = null
            isPlayButtonClick = true
            detailsResponse?.data?.detail?.dashWidewinePlayUrl =
                viewModel.lastWatched?.playUrl ?: detailsResponse?.data?.detail?.playUrl
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            if (TYPE_WEB_SHORTS.equals(contentType, true) && !playerModel?.getPlaybackUrl()
                    .isNullOrEmpty()
            )
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
            else {
                viewModel.fetchPlanetMarathiPlayUrl(
                    playerModel?.getProviderContentId() ?: "",
                    playerModel?.getContentType() ?: ""
                )
            }
        } else if (PROVIDER_LIONSGATE.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playbackUrl = playerModel?.getPlaybackUrl()
            viewModel.fetchLionsgateToken(playbackUrl)
        } else {

            playerModel = null
            if (detailsResponse?.data?.detail?.dashWidewinePlayUrl.isNullOrEmpty()) {
                detailsResponse?.data?.detail?.dashWidewinePlayUrl =
                    viewModel.lastWatched?.playUrl ?: detailsResponse?.data?.detail?.playUrl
            } else {
                detailsResponse?.data?.detail?.dashWidewinePlayUrl =
                    viewModel.lastWatched?.dashWidewinePlayUrl
                        ?: detailsResponse?.data?.detail?.dashWidewinePlayUrl
            }
            playbackUrl = detailsResponse?.data?.detail?.dashWidewinePlayUrl
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            val epids = viewModel.lastWatched?.offerIds?.epids
                ?: detailsResponse?.data?.detail?.offerIds?.epids
            playerModel?.setEpids(epids)
            playerModel?.let {
                val contentMetaDetails=detailsResponse?.data?.metaDetails
                playGenericPartnerWithAuthType(
                    contentMetaDetails?.provider ?: "",
                    playbackUrl,
                    it,
                    contentMetaDetails?.partnerDeepLinkUrl,
                    contentMetaDetails?.liveContent ?: false,
                    it.getContentType() ?: TYPE_TV_SHOWS
                )
            }
        }
    }

    private fun playErosNowContent() {
        e("Eros", "partnerUniqueId : ${viewModel.sharedPrefs.getPartnerUniqueId()}")
        context?.let {
            if (ENSDK.getLoggedIn()) {
//                trackOnThirdPartyPlayerPlay(playerModel)
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
            } else {
                viewModel.showHideLoader(true)
                erosnowLogin(
                    it,
                    enListener,
                    viewModel.sharedPrefs.getPartnerUniqueIdInfo(PROVIDER_EROSNOW),
//                    viewModel.sharedPrefs.getPartnerUniqueId(),
                    viewModel.sharedPrefs.getDeviceToken() ?: ""
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            SON_LIV_PHONE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkAndPlaySonyContent(true)
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                    showDialog(
                        DialogModel(
                            false,
                            null,
                            getString(R.string.msg_sonyliv_permission),
                            getString(R.string.open_setting_btn),
                            getString(R.string.btn_cancel)
                        ), object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
                                intent.data = uri
                                startActivity(intent)
                                hideDialog()
                            }

                            override fun onSecondaryButtonClick() {
                                hideDialog()
                            }

                            override fun onCloseButtonClick() {
                                hideDialog()
                            }
                        })
                } else {
                    showToast(context, getString(R.string.permission_denied))
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun checkRuntimePermission(activity: Activity) {
        //check permission at runtime'
        when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkAndPlaySonyContent(true)
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    SON_LIV_PHONE_PERMISSION
                )
            }
        }
    }


    private fun checkAndPlaySonyContent(permissionGranted: Boolean = false) {
        if (Build.VERSION.SDK_INT >= 31 && !permissionGranted) {
            activity?.let { it ->
                checkRuntimePermission(it)
            }
            return
        }


        e(
            "SonyLIVSDKListener",
            "SonyLIVSDKManager.getInstance().status:${SonyLIVSDKManager.getInstance().status}"
        )
        /*if (!sharedPrefs.isLoginAgain() && SonyLIVSDKManager.getInstance().status == SDKStatus.SUCCESS)
            playSonyLivContent()
        else*/
        /*Commented for this Task TSF-16639*/
        viewModel.generateSonylivShortToken(true)
    }

    private fun playContent(seriesItem: ContentItem) {
        if (seriesItem == null)
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
        else {
            playerModel = viewModel.generatePlayerModel(
                seriesItem,
                detailsResponse?.data?.metaDetails?.taShowType ?: ""
            )
        }
        if (PROVIDER_TATA_SKY.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            seriesItem.playerDetails?.offerIds?.epids?.let {
                viewModel.generateControlToken(
                    it,
                    id,
                    true
                )
            } ?: kotlin.run {
                showToast(context, "No Entitlements found")
            }

        } else if (PROVIDER_CURIOSITY_STREAM.equals(seriesItem.provider, true)) {
            playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
        } else if (PROVIDER_HUNGAMA.equals(seriesItem.provider, true)) {
            playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
        } else if (PROVIDER_SHEMAROO.equals(seriesItem.provider, true)) {
            playShemarooMeContent(seriesItem.partnerDeepLinkUrl, false, false)
        } else if (PROVIDER_VOOTSELECT.equals(seriesItem.provider, true)) {
            playVootContent(
                seriesItem.providerContentId,
                false,
                false,
                playerModel?.getContentType() ?: "TV_SHOWS",
                partnerSubscriptionType = seriesItem.partnerSubscriptionType,
                partnerDeepLinkUrl = seriesItem.partnerDeepLinkUrl
            )
        } else if (PROVIDER_VOOTKIDS.equals(seriesItem.provider, true)) {
            playVootContent(
                seriesItem.providerContentId,
                false,
                false,
                playerModel?.getContentType() ?: "TV_SHOWS",
                isKids = true,
                partnerSubscriptionType = seriesItem.partnerSubscriptionType,
                partnerDeepLinkUrl = seriesItem.partnerDeepLinkUrl
            )
        } else if (PROVIDER_ZEE5.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            seriesItem.playerDetails?.dashWidewinePlayUrl = seriesItem.partnerWebUrl
            if (seriesItem == null)
                playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            else
                playerModel = viewModel.generatePlayerModel(
                    seriesItem,
                    detailsResponse?.data?.metaDetails?.taShowType ?: ""
                )
            e(
                "playZee5Content", "seriesItem : ${seriesItem.partnerDeepLinkUrl}, " +
                    "partnerWebUrl : ${seriesItem.partnerWebUrl}, " +
                    "getPlaybackUrl: ${playerModel?.getPlaybackUrl()}"
            )
            playZee5Content(seriesItem.partnerSubscriptionType ?: partnerSubscriptionTypeForPlayBtn)
        } else if (PROVIDER_HOTSTAR.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            //seriesItem.playerDetails?.dashWidewinePlayUrl = seriesItem.hotstarAppDeeplink ?: "hotstar://${seriesItem.providerContentId}"//1260049386
            seriesItem.playerDetails?.dashWidewinePlayUrl = seriesItem.playerDetails?.partnerWebUrl
            if (seriesItem == null)
                playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            else {
                seriesItem.playerDetails?.dashWidewinePlayUrl = seriesItem.hotstarAppDeeplink
                    ?: "hotstar://${seriesItem.providerContentId}"//1260049386
                playerModel = viewModel.generatePlayerModel(
                    seriesItem,
                    detailsResponse?.data?.metaDetails?.taShowType ?: ""
                )
            }
            playHotstarContent()
        } else if (PROVIDER_EROSNOW.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playErosNowContent()
        } else if (PROVIDER_SONYLIV.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null
            playerModel = viewModel.generatePlayerModel(
                seriesItem,
                detailsResponse?.data?.metaDetails?.taShowType ?: ""
            )
            checkAndPlaySonyContent()
        } else if (PROVIDER_EPIC_ON.equals(detailsResponse?.data?.metaDetails?.provider, true) ||
            PROVIDER_DOCU_BAY.equals(detailsResponse?.data?.metaDetails?.provider, true)

        ) {
            playerModel = null
            seriesItem.playerDetails?.dashWidewinePlayUrl = seriesItem.playerDetails?.playUrl
            playerModel = viewModel.generatePlayerModel(
                seriesItem,
                detailsResponse?.data?.metaDetails?.taShowType ?: ""
            )
            if (viewModel.lastWatched?.dashWidewinePlayUrl == null) {
                viewModel.fetchCSBoxsetDetails(seriesItem.id, seriesItem.provider)
            } else
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
        } else if (PROVIDER_HOICHOI.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null

            val token = viewModel.getToken(id)
            seriesItem.playerDetails?.dashWidewinePlayUrl = seriesItem.playerDetails?.playUrl
            playbackUrl = seriesItem.playerDetails?.dashWidewinePlayUrl
            playerModel = viewModel.generatePlayerModel(
                seriesItem,
                detailsResponse?.data?.metaDetails?.taShowType ?: ""
            )
//            playerModel?.setPlaybackSubtitleUrl(seriesItem.subtitlePlayUrl)
            if (playerModel?.getPlaybackUrl().isNullOrEmpty())
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
            else if (!token.isNullOrEmpty()) {
                playHoiChoiContent(playbackUrl, token)
            } else {
                val request = HoichoiRequest(partner = "hoichoi")

                viewModel.fetchHoichoiPlaybackUrl(request, id)
            }


        } else if (PROVIDER_MXPLAYER.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = viewModel.generatePlayerModel(
                seriesItem,
                detailsResponse?.data?.metaDetails?.taShowType ?: ""
            )
            playMXPlayer()
        } else if (PROVIDER_CHAUPAL.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playerModel = null
            seriesItem.playerDetails?.dashWidewinePlayUrl = seriesItem.playerDetails?.playUrl
            playerModel = viewModel.generatePlayerModel(
                seriesItem,
                detailsResponse?.data?.metaDetails?.taShowType ?: ""
            )
            playChaupalContent(
                seriesItem.providerContentId,
                seriesItem.contentType, false, false
            )
        } else if (PROVIDER_PLANET_MARATHI.equals(
                detailsResponse?.data?.metaDetails?.provider,
                true
            )
        ) {
            viewModel.fetchPlanetMarathiPlayUrl(
                playerModel?.getProviderContentId() ?: "",
                playerModel?.getContentType() ?: ""
            )
        } else if (PROVIDER_LIONSGATE.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
            playbackUrl = seriesItem.playerDetails?.dashWidewinePlayUrl
            viewModel.fetchLionsgateToken(playbackUrl)
        } else {
            playerModel = null
            seriesItem.playerDetails?.dashWidewinePlayUrl =
                seriesItem.playerDetails?.dashWidewinePlayUrl ?: seriesItem.playerDetails?.playUrl
            playbackUrl = seriesItem.playerDetails?.dashWidewinePlayUrl
            playerModel = viewModel.generatePlayerModel(
                seriesItem,
                detailsResponse?.data?.metaDetails?.taShowType ?: ""
            )
            playerModel?.setEpids(seriesItem.playerDetails?.offerIds?.epids)
            playerModel?.let {
                playGenericPartnerWithAuthType(
                    detailsResponse?.data?.metaDetails?.provider ?: "",
                    playbackUrl,
                    it,
                    seriesItem.partnerDeepLinkUrl,
                    seriesItem.liveContent,
                    it.getContentType() ?: TYPE_TV_SHOWS
                )
            }
        }
    }

    private fun navigateToPlayer(playerModel: PlayerModel) {
        if (isExternalDisplayAvailable()) {
            showToast(context, getString(R.string.casting_not_allowed))
            return
        }
        if (PROVIDER_HUNGAMA.equals(playerModel.getProvider(), true)
            || PROVIDER_EROSNOW.equals(playerModel.getProvider(), true)
            || !playerModel.getPlaybackUrl().isNullOrEmpty()
        ) {
            if (::orientationManager.isInitialized)
                orientationManager.enable()
            if (::trailerDisposable.isInitialized)
                trailerDisposable.dispose()
            binding.trailerView.onDestroyView()
            binding.trailerView.hide()
            isNavigateToPlayer = true
            actionOnPlayClick()

            binding.btnPlayerController.setText("Pause", null)
            binding.btnPlayerController.setOnClickListener(null)
            binding.btnPlayerController.show()
            if (secondaryButtonState == SecondaryButtonStateEnum.STATE_TRAILER) {
                binding.btnSecondary.disable()
                binding.btnSecondary.hide()
                binding.buttonView?.hide()
            }
            if(isDeviceTablet)
                updateButtonLayoutWeight()
            else binding.layLinearButtons.weightSum=1.0f
            binding.btnPrimary.hide()
            viewModel.startPlayer(playerModel)
            isBackFromPlayer = true
            binding.ivPoster.alpha = 1f
        } else {
            showToast(context, "Unable to play Content")
        }
    }

    private fun showACAAlert(message: String) {
        showDialog(
            DialogModel(
                false,
                null,
                message,
                getString(R.string.ok),
                null
            ),
            object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    hideDialog()

                    activity?.finishAndRemoveTask()
                }

                override fun onCloseButtonClick() {
                    /*
                    * Nothing to do
                    * */
                }

                override fun onSecondaryButtonClick() {
                    /*
                    * Nothing to do
                    * */
                }
            })
    }

    override fun onLoadMoreClick(pageOffset: Int) {
        isLoadingSeries = true
        seriesLastOffset = pageOffset
        fetchSeries(
            loader = true, pageOffset = seriesLastOffset, pageLimit = viewModel.SERIES_LIMIT,
            isLastWatch = false, isAutoScroll = false
        )
    }


    private fun playShemarooMeContent(
        smartURL: String, isTrailer: Boolean, isPlayButtonClick: Boolean
    ) {
        this.isPlayButtonClick = isPlayButtonClick
        detailsResponse?.data?.detail?.dashWidewinePlayUrl = playbackUrl
        detailsResponse?.data?.detail?.dashWidewineLicenseUrl = licenseUrl
        if (!isTrailer && detailsResponse?.data?.detail?.dashWidewinePlayUrl != null
            && contentType == TYPE_MOVIES
        ) {
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
        } else {
            val signedURL = ShemarooHelper.decryptMd5(smartURL)
            viewModel.fetchShemarooMeContentPlayback(signedURL, isTrailer)
        }
    }

    protected fun playVootContent(
        providerContentId: String, isTrailer: Boolean,
        isPlayButtonClick: Boolean, contentType: String, isKids: Boolean = false,
        partnerSubscriptionType: String? = "",
        partnerDeepLinkUrl: String = ""
    ) {
        if (!isContentSubscribed && !PREMIUM.equals(partnerSubscriptionType, true) && !isTrailer) {
            if (URLUtil.isValidUrl(partnerDeepLinkUrl)) {
                var partnerDeepLinkUrl =
                    partnerDeepLinkUrl//getUrlWithoutParameters(partnerDeepLinkUrl)
                if (partnerDeepLinkUrl != null && !partnerDeepLinkUrl.contains("utm_source")) {
                    partnerDeepLinkUrl += "?&url_source=marketing&utm_source=tataplaybinge"
                }
                viewModel.generateVootPwaToken(partnerDeepLinkUrl ?: "")
            } else {
                showToast(context, viewModel.VIDEO_UNAVAILABLE_MESSAGE_URL)
            }
            return
        }

        var contentType = contentType
        this.isPlayButtonClick = isPlayButtonClick
        detailsResponse?.data?.detail?.dashWidewinePlayUrl = playbackUrl

        if (contentType.equals(BRAND, true) ||
            contentType.equals(SERIES, true)
        ) {
            contentType = TV_SHOWS
        }
        val partner = (detailsResponse?.data?.metaDetails?.provider ?: "VOOT_KIDS").replace("_", "")
        val request = VootRequest(
            contentId = providerContentId,
            contentType = contentType,
            partner = partner,
            baId = viewModel.sharedPrefs.getBaId(),
            type = "DASH"
        )
        if (isKids)
            viewModel.fetchVootKidsPlaybackUrl(request, isTrailer)
        else
            viewModel.fetchVootPlaybackUrl(request, isTrailer)
    }

    private fun disableOrientation() {
        context?.let {
            if(!isTablet(it)){
                if (::orientationManager.isInitialized)
                    orientationManager.disable()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FOR_PACK_SELECTION && resultCode == Activity.RESULT_OK) {
            updateUIWithDeeplink()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun navigateToSubscriptionActivity(upgrade: Boolean = false) {
        isBtnClicked = true
        activity?.let {
            if (sharedPrefs.isParentalPinExists()
                && sharedPrefs.getParentalRating()?.ageRatingName.takeIf { rating ->
                    rating.isNullOrEmpty() || rating == getString(R.string.no_restrictions)
                } == null
            ) {
                isResultHandled = false
                openSubscriptionActivity = true
                (activity as? LandingActivity)?.isResultHandled = true
                findNavController().navigateSafe(
                    DetailsFragmentDirections.actionGlobalParentalControlBottomDialogFragment(
                        ACTION_PIN_VERIFICATION,
                        null,
                        false,
                        PLAY
                    )
                )
            } else
                if (!sharedPrefs.isManagedAppEnabled()) {
                    startActivityForResult(
                        getSubscriptionActivityIntent(
                            it,
                            fromLogin = false,
                            selectedAppId = detailsResponse?.data?.metaDetails?.partnerId ?: "",
                            fromScreen = SOURCE_PLAY_CLICK,
                            startPackListing = true,
                            partnerId = detailsResponse?.data?.metaDetails?.partnerId ?: ""
                        ), REQUEST_FOR_PACK_SELECTION
                    )
                } else {
                    if (upgrade) {
                        showMiniDrawer(
                            context = it,
                            fromLogin = false,
                            fromScreen = SOURCE_PLAY_CLICK,
                            startPackListing = true,
                            journeyRef = HOME_CONTENT
                        )
                    } else {
                        showMiniDrawer(
                            context = it,
                            fromLogin = false,
                            fromScreen = SOURCE_PLAY_CLICK,
                            startPackListing = true,
                        )
                    }
                }
        }
    }


    private fun showMiniDrawer(
        context: Context?,
        fromLogin: Boolean = false,
        fromScreen: String = SOURCE_NOTIFICATION,
        fromDialog: Boolean = false,
        startPackListing: Boolean = false,
        journeyRef: String = ""
    ) {
        val currentPack = sharedPrefs.getSubscribedPack()
        // start my plan (susbcription activity startpacklisting = false )
        if (!startPackListing || currentPack?.fdoRequested == true) {
            startActivity(
                getSubscriptionActivityIntent(
                    context = context,
                    fromLogin = fromLogin,
                    fromScreen = fromScreen,
                    startPackListing = startPackListing
                )
            )
        } else {
            if (activity is LandingActivity) {
                if (sharedPrefs.getConfigResponse()?.data?.config?.enableTickTickJourney == false) {
                    //Added journeyRef so that directly open managed app by skipping drawer

                    (activity as? LandingActivity)?.showMiniDrawer(
                        CONTENT_PLAYBACK,
                        HOME_CONTENT,
                        detailsResponse?.data?.metaDetails?.partnerId ?: "",
                        skipDrawer = true
                    )

                } else
                    (activity as? LandingActivity)?.showMiniDrawer(
                        CONTENT_PLAYBACK,
                        journeyRef,
                        detailsResponse?.data?.metaDetails?.partnerId ?: "",
                        skipDrawer = false
                    )
            }
        }
    }

    var upgradePlanClick = false
    private fun handleSubscriptionDialog() {

        isPausedTrailer = true
        var trackSubscribePopup = false
        var trackUpgradePopup = false
        val subscribedPack = sharedPrefs.getSubscribedPack()
        val dialogListener = object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                if (trackSubscribePopup)
                    miscAnalytics.trackMixPanelSusbscribePopupSubscribe(contentItem.contentTitle)
                else if (trackUpgradePopup) {
                    trackUpgradePopup = false
                    miscAnalytics.trackMixPanelUpgradePopupUpgrade(
                        detailsResponse?.data?.metaDetails?.getVodTitle() ?: "",
                        detailsResponse?.data?.metaDetails?.provider ?: ""
                    )
                }
                hideDialog()
                upgradePlanClick = true
                navigateToSubscriptionActivity(upgradePlanClick)
            }

            override fun onSecondaryButtonClick() {
                if (trackSubscribePopup)
                    miscAnalytics.trackMixPanelSusbscribePopupCancel()
                else if (trackUpgradePopup) {
                    trackUpgradePopup = false
                    miscAnalytics.trackMixPanelUpgradePopupCancel()
                }
                isBtnClicked = false
                hideDialog()
            }

            override fun onCloseButtonClick() {
                isBtnClicked = false
                hideDialog()
            }
        }

        if (subscribedPack?.downgradeRequested == true) {
            showDialog(
                DialogModel(
                    false,
                    R.drawable.ic_subscription_error,
                    "",
                    getString(R.string.done),
                    "",
                    subscribedPack.downgradeRequestedMessage ?: ""
                ), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }
                }
            )
        } else if (subscribedPack?.upgradeFDOCheck == true) {
            showDialog(
                DialogModel(
                    false,
                    R.drawable.ic_subscription_error,
                    subscribedPack.upgradeFDOHeader ?: "",
                    getString(R.string.done),
                    "",
                    subscribedPack.upgradeFDOMessage ?: ""
                ), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }
                }
            )
        } else if (subscribedPack?.isInactive == true) { //Pack expired free/paid
            fun getVerbiage(): Triple<String, String, String> {
                val title =
                    if (subscribedPack.freeTrialStatus == true) subscribedPack.freeTrialNudgeDetails?.nudgeTitle
                        ?: ""
                    else subscribedPack.subscriptionNudgeDetails?.nudgeTitle ?: ""

                val desc =
                    if (subscribedPack.freeTrialStatus == true) subscribedPack.freeTrialNudgeDetails?.nudgeMessage
                        ?: ""
                    else subscribedPack.subscriptionNudgeDetails?.nudgeMessage ?: ""

                val btnText =
                    if (subscribedPack.freeTrialStatus == true) subscribedPack.freeTrialNudgeDetails?.nudgeButton
                        ?: ""
                    else subscribedPack.subscriptionNudgeDetails?.nudgeButton ?: ""

                return Triple(title, desc, btnText)
            }
            val (title, desc, btnText) = getVerbiage()
            (activity as? LandingActivity)?.let {
                it.customSnackbarWithTwoActionsUtil.hideCustomSnackbarWithTwoActions()
                it.showRenewPlanAfterExpiryNudge(
                    title,
                    desc,
                    btnText
                )
            }
        } else { //binge anywhere
            if ((sharedPrefs.getUserDetails()?.freeTrialAvailed == false && sharedPrefs.getSubscribedPack() == null) || sharedPrefs.getSubscribedPack()
                    ?.let {
                        it.mobileUpgradable && it.subscriptionDetailInfo?.bingeAccountStatus.equals(
                            SubscriptionPackStatusEnum.ACTIVE.status,
                            true
                        ) && !it.isCancelled
                    } == true
            ) {
                navigateToSubscriptionActivity()
            } else if (subscribedPack == null || subscribedPack.doNotConsiderThePack) {
                trackSubscribePopup = true
                trackUpgradePopup = false
                navigateToSubscriptionActivity()


            } else {
                val subscribedEntitlements = viewModel.sharedPrefs.getPartnerIdsList()

                if (!subscribedEntitlements.isNullOrEmpty()
                    && !subscribedEntitlements.contains(detailsResponse?.data?.metaDetails?.partnerId)
                    && !subscribedPack.isCancelled
                ) {
                    var type = "show"
                    if (contentType.equals(MOVIES, true))
                        type = "movie"
                    val upgrade_subscription_message = String.format(
                        getString(R.string.upgrade_subscription_message), type
                    )
                    miscAnalytics.trackMixPanelUpgradePopup()
                    trackUpgradePopup = true


                    if (PROVIDER_APPLE.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
                        val verbiage =
                            sharedPrefs.getConfigResponse()?.data?.config?.getLanguageVerbiage(
                                CATEGORY_APPLE_UPGRADE_POPUP
                            )
                        showDialog(
                            DialogModel(
                                cancelable = false,
                                imageIdBig = R.drawable.ic_premium_crown,
                                title = verbiage?.data?.header ?: "Upgrade to Binge Mega Plan",
                                text = verbiage?.data?.subHeader
                                    ?: "Enjoy, star-studded, award winning, series, films & more from Apple TV+ on Tata Play Binge App.",
                                primaryButtonText = verbiage?.data?.others?.buttonTitle
                                    ?: getString(R.string.upgrade),
                                secondaryButtonText = verbiage?.data?.others?.exitButtonTitle?:getString(R.string.not_now)
                            ),
                            object : CommonDialogEventListener {
                                override fun onPrimaryButtonClick() {
                                    upgradePlanClick = true
                                    navigateToSubscriptionActivity(upgradePlanClick)
                                    hideDialog()
                                }

                                override fun onSecondaryButtonClick() {
                                    hideDialog()
                                }

                                override fun onCloseButtonClick() {
                                    hideDialog()
                                }

                            }
                        )
                    } else
                        if (sharedPrefs.getConfigResponse()?.data?.config?.enableTickTickJourney == false) {
                            upgradePlanClick = true
                            navigateToSubscriptionActivity(upgradePlanClick)
                            return
                        } else
                            showDialog(
                                DialogModel(
                                    false,
                                    R.drawable.ic_subscribe,
                                    getString(R.string.upgrade_subscription),
                                    getString(R.string.upgrade),
                                    getString(R.string.cancel),
                                    upgrade_subscription_message
                                ), dialogListener
                            )
                } else if (!subscribedEntitlements.isNullOrEmpty()
                    && !subscribedEntitlements.contains(detailsResponse?.data?.metaDetails?.partnerId)
                    && subscribedPack.isCancelled && !subscribedPack.isInactive
                ) {
                    miscAnalytics.trackMixPanelUpgradePopup()
                    trackUpgradePopup = true
                    if (sharedPrefs.getConfigResponse()?.data?.config?.enableTickTickJourney == false) {
                        upgradePlanClick = true
                        navigateToSubscriptionActivity(upgradePlanClick)
                        return
                    } else
                        showDialog(
                            DialogModel(
                                false,
                                R.drawable.ic_subscribe,
                                subscribedPack.verbiage?.contentSubs?.title
                                    ?: getString(R.string.upgrade_subscription),
                                getString(R.string.upgrade),
                                getString(R.string.cancel),
                                subscribedPack.verbiage?.contentSubs?.message
                            ), dialogListener
                        )
                } else {
                    navigateToSubscriptionActivity()
                }
            }
        }
    }


    private fun handleRRMSession() {
        if (!detailsResponse?.data?.detail?.offerIds?.epids.isNullOrEmpty()) {
            if (viewModel.isTokenExpired(id))
                viewModel.generateControlToken(
                    detailsResponse?.data?.detail?.offerIds?.epids!!,
                    id,
                    false
                )
        }

    }


    private fun loadTopSeriesList() {
        isLoadingSeries = true
        var limit = topOffset
        topOffset -= viewModel.SERIES_LIMIT
        if (topOffset < 0) {
            topOffset = 0
        } else {
            limit = viewModel.SERIES_LIMIT
        }
        if (limit > 0)
            fetchSeries(
                loader = true, pageOffset = topOffset, pageLimit = limit,
                isLastWatch = false, isAutoScroll = false
            )
    }

    override fun onStop() {
        super.onStop()
        isNavigateToOther = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            context?.let {
                if(!isTablet(it)){
                    commonViewModel?.saveOrientation(OrientationManager.ScreenOrientation.PORTRAIT)
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            isNavigateToOther = true
            hidePlayer()
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
        cancelFavToast()
    }

    protected fun cancelFavToast() {
        if (favToast != null) {
            favToast?.cancel()
        }
    }

    private fun getTypeOfContentToShow(): String {
        return if (contentType.contains(TYPE_MOVIES))
            "Movie"
        else
            "Show"
    }

    private fun startEnterTransitionAfterLoadingImage(
        imageAddress: String,
        imageView: ImageView,
        animate: Boolean = false
    ) {
        Glide.with(this)
            .load(imageAddress)
            .dontAnimate()
            .thumbnail(
                Glide.with(this)
                    .load(initialImageUrl)
            )
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (animate)
                        startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (animate)
                        startPostponedEnterTransition()
                    return false
                }
            })
            .into(imageView)
    }

    private fun playZee5Content(partnerSubscriptionType: String?) {
        if (isContentSubscribed || PREMIUM.equals(partnerSubscriptionType, true))
            viewModel.fetchZee5Tag(playerModel?.getPlaybackUrl())
        else
            playZee5("")
    }

    private fun playZee5(tag: String) {
        actionOnPlayClick()
        val uri = Uri.parse(
            getUrlWithoutParameters(
                playerModel?.getPlaybackUrl() ?: ""
            ) + ("?utm_source=tataskybinge&utm_medium=amazonstick&utm_campaign=zee5campaign&partner=tataskybinge&tag=$tag")
        )
        try {
            openChromeTab(uri)
            trackOnThirdPartyPlayerPlay(playerModel, tag)
        } catch (e: ActivityNotFoundException) {
            onError(
                ErrorModel(
                    message = getString(R.string.no_browser),
                    statusCode = CUSTOM_RESPONSE_CODE_ZEE5_ERROR
                )
            )
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun playHotstarContent() {
        if (playerModel?.getPlaybackUrl().isNullOrBlank() && playerModel?.getProviderContentId()
                .isNullOrBlank()
        ) {
            onError(
                ErrorModel(
                    code = CUSTOM_RESPONSE_CODE_ZEE5_ERROR,
                    message = viewModel.VIDEO_UNAVAILABLE_MESSAGE_URL
                )
            )
            return
        }

        val url = playerModel?.getPlaybackUrl()
            ?: "hotstar://${playerModel?.getProviderContentId()}"//1260049386
        e("playHotstar", "url : $url")
        activity?.let {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url.trim())
                if (!sharedPrefs.getLoginStatus()) {
                    startActivity(i)
                    return
                }
                var launchFrequency = 0
                launchFrequency = if (sharedPrefs.getHotstarPopupFirstCycleCompleted()) {
                    1
                } else {
                    sharedPrefs.getHotstarDialogLaunchFrequency()
                }
                if (sharedPrefs.getHotStarPopupCount() < launchFrequency) {
                    if (i.resolveActivity(it.packageManager) != null) {
                        val userRMN = sharedPrefs.getClearRMN().maskPhoneNumberWithx()
                        showDialog(
                            DialogModel(
                                cancelable = false,
                                title = getString(R.string.hotstar_head_installed),
                                primaryButtonText = getString(R.string.proceed),
                                secondaryButtonText = getString(R.string.cancel),
                                imageIdBig = R.drawable.hotstar_popup_logo,
                                text = getString(
                                    R.string.hotstar_body_installed,
                                    userRMN
                                )
                            ), object : CommonDialogEventListener {
                                override fun onPrimaryButtonClick() {
                                    actionOnPlayClick()
                                    startActivity(i)
                                    trackOnThirdPartyPlayerPlay(playerModel)
                                    hideDialog()
                                }

                                override fun onSecondaryButtonClick() {
                                    hideDialog()
                                }

                                override fun onCloseButtonClick() {
                                    hideDialog()
                                }
                            })
                    } else {
                        showDialog(
                            DialogModel(
                                cancelable = false,
                                title = getString(R.string.hotstar_head_not_installed),
                                primaryButtonText = getString(R.string.install),
                                secondaryButtonText = getString(R.string.cancel),
                                imageIdBig = R.drawable.hotstar_popup_logo,
                                text = getString(
                                    R.string.hotstar_body_not_installed,
                                    sharedPrefs.getClearRMN().toLowerCase()
                                )
                            ), object : CommonDialogEventListener {
                                override fun onPrimaryButtonClick() {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data =
                                        Uri.parse("https://play.google.com/store/apps/details?id=in.startv.hotstar")
                                    startActivity(intent)
                                    trackOnThirdPartyPlayerPlay(playerModel)
                                    hideDialog()
                                }

                                override fun onSecondaryButtonClick() {
                                    hideDialog()
                                }

                                override fun onCloseButtonClick() {
                                    hideDialog()
                                }
                            })
                    }
                    sharedPrefs.increaseHotStarPopupCount()
                    if ((sharedPrefs.getHotStarPopupCount() == sharedPrefs.getHotstarDialogLaunchFrequency())
                        && sharedPrefs.getHotstarLastFinalPopupShownTime() == 0L
                    ) {
                        val currentDate = Calendar.getInstance()
                        currentDate.apply {
                            this.set(Calendar.HOUR_OF_DAY, 0)
                            this.set(Calendar.MINUTE, 0)
                            this.set(Calendar.SECOND, 0)
                            this.set(Calendar.MILLISECOND, 0)
                        }
                        sharedPrefs.setHotstarLastFinalPopupShownTime(currentDate.timeInMillis)
                        sharedPrefs.setHotstarPopupFirstCycleCompleted(true)
                    }
                } else {
                    trackOnThirdPartyPlayerPlay(playerModel)
                    if (i.resolveActivity(it.packageManager) != null) {
                        actionOnPlayClick()
                        startActivity(i)
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data =
                            Uri.parse("https://play.google.com/store/apps/details?id=in.startv.hotstar")
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                showToast(context, "Something went wrong!")
                e.printStackTrace()
            }
        }
    }

    private fun actionOnPlayClick() {
        incrementCountOnlyOnce = true
        val id = detailsResponse?.data?.metaDetails?.id
        val vodId = detailsResponse?.data?.metaDetails?.vodId
        val contentId = vodId ?: id ?: ""
        playerModel?.let {
            viewModel.actionCW(
                it.getContentId()!!,
                getContentType(it.getContentType()!!),
                it.getResumeTime().toInt() / 1000,
                it.getTotalDuration().toInt()
            )
            if (PROVIDER_SHEMAROO.equals(it.getProvider(), true))
                viewModel.callShemaroomeAnalyticsPlayEvent(it, "play", "play", 1)

            if (PROVIDER_PLANET_MARATHI.equals(it.getProvider(), true))
                viewModel.callPlanetMarathiAnalyticsPlayEvent(it, "play")
        }
        viewModel.trackOnceIn24hrLearnAction(
            detailsResponse?.data?.metaDetails?.partnerSubscriptionType,
            contentType,
            contentId,
            detailsResponse?.data?.metaDetails?.taShowType ?: "",
            detailsResponse?.data?.metaDetails?.provider ?: "",
            detailsResponse?.data?.detail?.contractName ?: "",
            CLICK_LEARN_ACTION,
            contentItem.refId
        )
    }

    @Throws(ActivityNotFoundException::class)
    private fun openChromeTab(uri: Uri) {
        activity?.let {
            isNavigateToOther = true
            commonViewModel?.isCCTOpen = true
            if (getCustomTabsPackages()?.find { it.activityInfo.packageName == CHROME_PACKAGE_NAME } != null) {
                try {
                    val builder = CustomTabsIntent.Builder()
                    builder.setShowTitle(false)
                    builder.setCloseButtonIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.ic_back
                        )
                    )
                    builder.setUrlBarHidingEnabled(true)
                    builder.setDefaultColorSchemeParams(
                        CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(
                                ContextCompat.getColor(
                                    it,
                                    R.color.darkBackground
                                )
                            )
                            .build()
                    )
                    builder.setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                    val customTabsIntent = builder.build()
                    customTabsIntent.intent.setPackage(CHROME_PACKAGE_NAME)
                    customTabsIntent.launchUrl(it, uri)
                } catch (e: Exception) {
                    val builder = CustomTabsIntent.Builder()
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(it, uri)
                }
            } else {
                try {
                    val builder = CustomTabsIntent.Builder()
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(it, uri)
                } catch (e: Exception) {
                    onError(
                        ErrorModel(
                            message = getString(R.string.no_browser),
                            statusCode = CUSTOM_RESPONSE_CODE_ZEE5_ERROR
                        )
                    )
                }

            }
        }
    }


    private fun getCustomTabsPackages(): ArrayList<ResolveInfo>? {
        val pm: PackageManager = requireActivity().packageManager
        // Get default VIEW intent handler.
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))

        // Get all apps that can handle VIEW intents.
        val resolvedActivityList: List<ResolveInfo> =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL)
            } else {
                pm.queryIntentActivities(activityIntent, 0)
            }
        val packagesSupportingCustomTabs = ArrayList<ResolveInfo>()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION)
            serviceIntent.setPackage(info.activityInfo.packageName)
            e(
                "getCustomTabsPackages",
                "info.activityInfo.packageName : ${info.activityInfo.packageName}"
            )
            // Check if this package also resolves the Custom Tabs service.
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info)
            }
        }
        return packagesSupportingCustomTabs
    }


    val enListener = object : EnLoginListener {
        override fun onError(error: ENError) {
            viewModel.showHideLoader(false)
            e("ErosNowListener", "onerror : ${error.message}")
            trackOnPlayerFailure(playerModel, error.error_code + " : " + error.message, contentItem)
            trackOnPlayerError(
                playerModel,
                error.error_code,
                error.message,
                PARA_PI_ERROR_ORIGIN,
                PARA_ERROR_TYPE_SDK
            )
            trackProbeSSOError(error.error_code, error.message, PARA_ERROR_TYPE_SDK)
            context?.let {
                val errorMsg = String.format(getString(R.string.error_sso), error.error_code)
                showToast(context, errorMsg)
            }
        }

        override fun onSuccess() {
            viewModel.showHideLoader(false)
            playerModel?.let {
//                trackOnThirdPartyPlayerPlay(it)
                navigateToPlayer(it)
            }
        }
    }

    private fun getShareIntent(): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val urlTitle = detailsResponse?.data?.metaDetails?.getVodTitle()?.replace(" ", "-")
        val shareBody = getString(
            R.string.deeplink_detail,
            BuildConfig.hostName,
            contentType.lowercase(),
            id, urlTitle
        )

        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_SUBJECT, "Watch " + (detailsResponse?.data?.metaDetails?.getVodTitle()
                ?: detailFragmentArgs.contentItem?.title)?.plus(
                " on Tata Play Binge! "
            )
        )
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Watch " + (detailsResponse?.data?.metaDetails?.getVodTitle()
                ?: detailFragmentArgs.contentItem?.title)?.plus(
                " on Tata Play Binge! "
            ) + shareBody
        )
        return shareIntent
    }


    protected fun isExternalDisplayAvailable(): Boolean {
        e(
            "PlayerBaseFragment", "inside isExternalDisplayAvailable" +
                ", mDisplayManager!!.displays.size: ${mDisplayManager?.displays?.size}"
        )
        return mDisplayManager != null && mDisplayManager?.displays != null && mDisplayManager!!.displays.size > 1
    }

    private fun playSonyLivContent() {
        sharedPrefs.setLoginAgain(false)
        context?.let {
            if (playerModel?.isTrailer() == false) {
                actionOnPlayClick()
                trackOnThirdPartyPlayerPlay(playerModel)
                isNavigateToOther = true
            }
            e(
                "SonyLIVSDKListener",
                "inside playSonyLivConent playerModel?.getContentId(): ${playerModel?.getProviderContentId()}"
            )
            SonyLIVSDKManager.getInstance().playContent(
                playerModel?.getProviderContentId(),
                it
            )
        }

    }


    protected fun updatePurchseExpiryView() {
        checkRental()
    }

    protected fun trackOnThirdPartyPlayerPlay(playerModel: PlayerModel?, tag: String = "") {
        playerAnalytics.trackPlayContent(
            playerModel?.getTitle() ?: "Not Available",
            playerModel?.getGenre(),
            playerModel?.getContentType() ?: "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            playerModel?.getProvider() ?: "",
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            contentItem.origin.toUpperCase(),
            contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            playerModel?.getAudioLanguages(),
            sharedPrefs.getSubscribedPack(),
            contentItem.railPosition,
            playerModel?.getContractName() ?: "",
            detailsResponse?.data?.metaDetails?.getParentTitle()
                ?: playerModel?.getParentTitle() ?: contentItem.title,
            contentItem.partnerSubscriptionType?.contains(PREMIUM, true) == false,
            (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
            contentItem.origin,
            contentItem.railCategory,
            playerModel?.getAudioLanguages()?.getOrNull(0),
            playerModel?.getGenre()?.getOrNull(0),
            contentAuth = if (contentAuth) YES else NO,
            contentItem.contentType,
            contentItem.contentPosition,
            detailsResponse?.data?.metaDetails?.rating ?: "",
            detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            detailsResponse?.data?.metaDetails?.actor,
            if (sharedPrefs.getAutoPlayTrailerOn() && isTrailerInitialized) YES else NO,
            liveContent = if (isLiveContent(
                    contentItem.contentType,
                    detailsResponse?.data?.metaDetails?.isLiveContent
                )
            ) YES else NO,
            "",
            "",
            contentItem.contentConfigType,
            tag,
            getAppleStatusValue()
        )
        if (contentItem.partnerSubscriptionType?.contains(PREMIUM, true) == false) {
            if (!sharedPrefs.getFirstFreeContentPlay()) {
                playerAnalytics.trackFirstFreeContentPlay(
                    playerModel?.getTitle() ?: "Not Available",
                    playerModel?.getContentType() ?: "",
                    playerModel?.getProvider() ?: ""
                )
                sharedPrefs.saveFirstFreeContentPlay()
            }
        } else {
            if (!sharedPrefs.getFirstPremiumContentPlay()) {
                playerAnalytics.trackFirstPremiumContentPlay(
                    playerModel?.getTitle() ?: "Not Available",
                    playerModel?.getContentType() ?: "",
                    playerModel?.getProvider() ?: ""
                )
                sharedPrefs.saveFirstPremiumContentPlay()
            }
        }
    }

    override fun forceLogout() {
        try {
            binding.playerFrame.hide()
            binding.miniProgressPlayer.hide()
            binding.trailerView.hide()
            binding.miniProgress.hide()
            binding.ivPoster.show()
            binding.trailerView.onDestroyView()
        } catch (e: Exception) {
        }
        if (mIsInFullScreenMode)
            changeToPortraitMode()
        super.forceLogout()
    }

    private fun initSonyLiv(shortToken: String) {
        sharedPrefs.setSonyOldToken(shortToken)
        viewModel.showHideLoader(true)
        val sonyLivSDKInitializeModel = SonyLivSDKInitializeModel()
        sonyLivSDKInitializeModel.partnerLoginToken = viewModel.sharedPrefs.getDeviceToken()
        sonyLivSDKInitializeModel.shortValidityToken = shortToken
        sonyLivSDKInitializeModel.partnerDSN = viewModel.sharedPrefs.getDsn()
        sonyLivSDKInitializeModel.partnerName = "TATASky"
        sonyLivSDKInitializeModel.partnerSource = "TSMOBILE"
        sonyLivSDKInitializeModel.sonyLIVSDKListener =
            SonyLIVSDKListener { status, errorModel ->
                viewModel.showHideLoader(false)
                SonyLIVSDKManager.getInstance().isSplashDisplayed = false
                if (SonyLIVSDKManager.getInstance().status == SDKStatus.SUCCESS)
                    playSonyLivContent()
                else {
                    trackOnPlayerFailure(
                        playerModel,
                        errorModel.errorCode + " : " + errorModel.errorMessage,
                        contentItem
                    )
                    trackOnPlayerError(
                        playerModel,
                        errorModel.errorCode,
                        errorModel.errorMessage,
                        PARA_PI_ERROR_ORIGIN,
                        PARA_ERROR_TYPE_SDK
                    )
                    trackProbeSSOError(errorModel.errorCode, errorModel.errorMessage, PARA_ERROR_TYPE_SDK)
                    context?.let {
                        val errorMsg =
                            String.format(getString(R.string.error_sso), errorModel.errorCode)
                        showToast(context, errorMsg)
                    }
                }

            }

        e(
            "SonyLIVSDKListener",
            "shortValidityToken : ${sonyLivSDKInitializeModel.shortValidityToken}" +
                ", partnerLoginToken : ${sonyLivSDKInitializeModel.partnerLoginToken}" +
                ", partnerDSN : ${sonyLivSDKInitializeModel.partnerDSN}" +
                ", partnerName : ${sonyLivSDKInitializeModel.partnerName}" +
                ", partnerSource : ${sonyLivSDKInitializeModel.partnerSource}" +
                ""
        )
        context?.let {
            SonyLIVSDKManager.getInstance().initSDK(it, sonyLivSDKInitializeModel)
        }
    }

    protected fun trackOnPlayerFailure(
        playerModel: PlayerModel?,
        errorMsg: String,
        contentItem: ContentItem?
    ) {
        playerAnalytics.trackPlaybackFailure(
            playerModel?.getTitle() ?: "Not Available",
            playerModel?.getGenre(),
            playerModel?.getContentType() ?: "",
            errorMsg,
            playerModel?.getProvider() ?: "",
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            (contentItem?.origin ?: "").toUpperCase(),
            contentItem?.source?.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            playerModel?.getAudioLanguages(),
            sharedPrefs.getSubscribedPack(),
            contentItem?.railPosition ?: "",
            detailsResponse?.data?.metaDetails?.getParentTitle()
                ?: playerModel?.getParentTitle() ?: "",
            contentItem?.partnerSubscriptionType?.contains(FREE, true) == true,
            contentItem?.source ?: "",
            contentItem?.origin ?: "",
            contentItem?.railCategory ?: "",
            playerModel?.getAudioLanguages()?.getOrNull(0),
            playerModel?.getGenre()?.getOrNull(0),
            if (contentAuth) YES else NO,
            contentItem?.contentType ?: "",
            contentItem?.contentPosition ?: "",
            detailsResponse?.data?.metaDetails?.rating ?: "",
            detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            detailsResponse?.data?.metaDetails?.actor,
            if (sharedPrefs.getAutoPlayTrailerOn() && isTrailerInitialized) YES else NO,
            liveContent = if (isLiveContent(
                    contentItem?.contentType,
                    detailsResponse?.data?.metaDetails?.isLiveContent
                )
            ) YES else NO,
            contentItem?.contentConfigType ?: ""
        )
    }

    /**
     * Track player playback errors with error details
     */
    protected fun trackOnPlayerError(
        playerModel: PlayerModel?,
        errorCode: String?,
        errorMsg: String?,
        origin: String?,
        type: String?
    ) {
        playerAnalytics.trackPlaybackError(
            errorCode = errorCode ?: "",
            errorMsg = errorMsg ?: "",
            origin = (origin ?: com.tatasky.binge.utils.EDITORIAL).toUpperCase(),
            type = type ?: "",
            partnerName = playerModel?.getProvider() ?: ""
        )
    }

    private fun checkForGuestUserPlaybackEligibility(
        eligibleLambda: () -> Unit,
        notEligibleLambda: (() -> Unit)? = null
    ) {
        if (sharedPrefs.getLoginStatus()) {
            eligibleLambda.invoke()
        } else {
            viewModel.checkForGuestUserPlaybackEligibility { eligible ->
                if (eligible) {
                    eligibleLambda.invoke()
                } else {
                    notEligibleLambda?.invoke()
                    loginPopup()
                }
            }
        }
    }

    fun updateWatchlist(it: Boolean?) {
        if (it == true) {
            binding.watchlistBtn.setCompoundDrawablesWithIntrinsicBounds(
                null, ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_pi_watchlist_selected
                ), null, null
            )
            binding.watchlistBtn.text = getString(R.string.add_to_watchlist)
            binding.trailerView.setWatchlisted(true)
        } else {
            binding.watchlistBtn.setCompoundDrawablesWithIntrinsicBounds(
                null, ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_pi_watchlist_unselected
                ), null, null
            )
            binding.watchlistBtn.text = getString(R.string.add_to_watchlist)
            binding.trailerView.setWatchlisted(false)
        }
    }

    private fun playMXPlayer() {
//        MxSDK.testPlayback(activity)
        /*activity?.let {
            MxSDK.startPlay()
                .withContent("4694edb9f78497662881d646798d5425", "tvshow_episode")
                .play(it)
        }*/
//        activity?.let {
//            playerModel?.let { model ->
//                actionOnPlayClick()
//                trackOnThirdPartyPlayerPlay(model)
//                e(
//                    "MXplayer",
//                    "getMXContentType(model.getContentType() ?: TYPE_TV_SHOWS): ${
//                        getMXContentType(model.getContentType() ?: TYPE_TV_SHOWS)
//                    }," +
//                        "viewModel.sharedPrefs.getAccessToken(): ${viewModel.sharedPrefs.getAccessToken()}," +
//                        "viewModel.sharedPrefs.getDsn() :  ${viewModel.sharedPrefs.getDsn()}"
//                )
//                if (sharedPrefs.getLoginStatus() && isContentSubscribed) {
//                    MxSDK.startPlay()
//                        .withContent(
//                            model.getProviderContentId()
//                                ?: "",//"tvshow_episode","4694edb9f78497662881d646798d5425"
//                            getMXContentType(model.getContentType() ?: TYPE_TV_SHOWS)
//                        )
//                        .withToken(viewModel.sharedPrefs.getAccessToken())
//                        .withDSN(viewModel.sharedPrefs.getDsn())
////                        .withUserId(viewModel.sharedPrefs.getOriginalSubscriberId())
//                        .play(it)
//                } else {
//                    MxSDK.startPlay()
//                        .withContent(
//                            model.getProviderContentId()
//                                ?: "4694edb9f78497662881d646798d5425",//"tvshow_episode"
//                            getMXContentType(model.getContentType() ?: TYPE_TV_SHOWS)
//                        )
//                        .play(it)
//                }
//                isNavigateToOther = true
//            }
//        }
    }

    fun getMXContentType(contentType: String): String {
        return when {
            contentType.contains(TYPE_TV_SHOWS) -> MXPlayerTypeEnum.TV_SHOW.type
            contentType.contains(TYPE_BRAND) -> MXPlayerTypeEnum.TV_SHOW.type
            contentType.contains(TYPE_SERIES) -> MXPlayerTypeEnum.TV_SHOW.type
            contentType.contains(TYPE_MOVIES) -> MXPlayerTypeEnum.MOVIES.type
            contentType.contains(TYPE_WEB_SHORTS) -> MXPlayerTypeEnum.SHORTS.type
            else -> MXPlayerTypeEnum.TV_SHOW.type
        }
    }

    private fun playChaupalContent(
        contentId: String,
        contentType: String,
        isTrailer: Boolean,
        isPlayButtonClick: Boolean
    ) {
        this.isPlayButtonClick = isPlayButtonClick
        playerModel?.setCookies(null)
        detailsResponse?.data?.detail?.dashWidewinePlayUrl = playbackUrl
        detailsResponse?.data?.detail?.dashWidewineLicenseUrl = licenseUrl
        if (!isTrailer && detailsResponse?.data?.detail?.dashWidewinePlayUrl != null
            && contentType == TYPE_MOVIES
        ) {
            playerModel = viewModel.generatePlayerModel(detailsResponse!!)
            playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
        } else {
            val contentType = if (isTrailer) "TRAILER" else contentType //"MOVIE"//
            viewModel.fetchChaupalContentPlayback(/*"78fe0ac8-c994-4048-9c5a-acb85778ce01"*/
                contentId,
                contentType,
                isTrailer
            )
        }
    }

    fun playGenericPartnerWithAuthType(
        provider: String,
        playbackUrl: String?,
        playerModel: PlayerModel,
        partnerDeeplinkUrl: String?,
        isLiveContent: Boolean,
        contentType: String
    ) {
        val playAuthType =
            ProvidersCache.availableProviders[provider.lowercase()]?.authType?.android?.playAuthType
                ?: PlayAuthTypeEnum.NONE.value
        if (playAuthType.lowercase() == PlayAuthTypeEnum.NONE.value) {
            playerModel.setPlaybackUrl(
                playbackUrl
            )
            playerModel.let {
                navigateToPlayer(it)
            }
        } else if (playAuthType.lowercase() == PlayAuthTypeEnum.JWT_TOKEN.value) {
            playerModel.getEpids()?.let {
                viewModel.generateControlToken(
                    it,
                    id,
                    true,
                    provider
                )
            } ?: kotlin.run {
                showToast(context, "No Entitlements found")
            }
        } else if (playAuthType.lowercase() == PlayAuthTypeEnum.DRM_TOKENAPI.value) {
            /*Handling of new api to play drm content*/
            viewModel.fetchGenericPartnerDRMAPI(
                playerModel.getProviderContentId(),
                provider,
                if (isLiveContent) TYPE_LIVE.lowercase() else TYPE_VOD.lowercase(),
                if (contentType.equals(BRAND, true) ||
                    contentType.equals(SERIES, true)
                ) {
                    TV_SHOWS
                } else {
                    contentType
                }
            )
        } else if (playAuthType.lowercase() == PlayAuthTypeEnum.INAPPBROWSER.value) {

            if (PROVIDER_APPLE.equals(detailsResponse?.data?.metaDetails?.provider, true)) {
                trackApplePlayClick()
                handleAppleTvPlayAction(playbackUrl)
            }else
                playInAppBrowserContent(partnerDeeplinkUrl)

        } else if (playAuthType.equals(PlayAuthTypeEnum.DEEPLINK.value, true)) {
            val packageName =
                ProvidersCache.availableProviders.getOrDefault(
                    provider.lowercase(),
                    null
                )?.authType?.android?.partnerAppPackageId
            checkAndRedirectToPartnerApp(
                deeplinkUrl = partnerDeeplinkUrl,
                provider = provider,
                packageName = packageName
            )
        } else {
            val appUpgrade = sharedPrefs.getConfigAppVersion()
            val message =
                appUpgrade?.partnerUpdateMessage.takeIf { !it.isNullOrBlank() } ?: (String.format(
                    getText(R.string.recommended_upgrade_message).toString(),
                    appUpgrade?.recommendedVersion.toString()
                ))
            showForceUpdateForProvider(
                title = getString(R.string.new_version_available),
                message = message,
                positiveBtnText = getString(R.string.update),
                negativeBtnText = getString(R.string.not_now),
                primaryBtnLink = null,
                isForceUpdate = true
            )
        }
    }



    private fun checkAndRedirectToPartnerApp(
        deeplinkUrl: String?,
        provider: String,
        packageName: String?,
    ) {
        if (deeplinkUrl != null && packageName != null)
            context?.let {
                try {
                    actionOnPlayClick()
                    val launchIntent = Intent(Intent.ACTION_VIEW, deeplinkUrl.trim().toUri()).apply {
                        `package` = packageName
                    }
                    startActivity(launchIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    this.showAppNotInstalledDialog(
                        provider = provider,
                        packageName = packageName
                    )
                }
            }
        else
            onError(
                ErrorModel(
                    title = viewModel.VIDEO_UNAVAILABLE_TITLE,
                    message = viewModel.VIDEO_UNAVAILABLE_MESSAGE
                )
            )
    }

    private val mRailScrollListener = object : RailScrollListener {
        override fun onRailScrolled(
            railName: String,
            position: Int,
            railType: String,
            railCategory: String
        ) {
            detailAnalytics.trackDetailsRailWatched(
                railName,
                position.toString(),
                pageName = (activity as? LandingActivity)?.getPageName()
                    ?: EVENT_VALUE_SOURCE_DETAIL,
                partnerName = detailsResponse?.data?.metaDetails?.provider ?: contentItem.provider,
                railType,
                railCategory,
                getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT),
                DEVICE_TYPE,
                sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            )
        }
    }

    private fun trackApplePlayClick(){
        val metaDetails =detailsResponse?.data?.metaDetails

        detailAnalytics.trackApplePlayCTAClick(
            metaDetails?.getVodTitle() ?: "",
            metaDetails?.contentType ?: "",
            metaDetails?.genre,
            metaDetails?.audio,
            contentItem.origin.toUpperCase(),
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            metaDetails?.provider ?: contentItem.provider,
            metaDetails?.getParentTitle() ?: "",
            metaDetails?.partnerSubscriptionType?.contains(
                FREE,
                true
            ) == true,
            (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
            contentItem.railPosition,
            contentItem.origin,
            contentItem.railCategory,
            metaDetails?.audio?.getOrNull(0),
            metaDetails?.genre?.getOrNull(0),
            contentAuth = if (contentAuth) YES else NO,
            contentItem.contentType,
            contentItem.contentPosition,
            metaDetails?.rating ?: "",
            metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            metaDetails?.actor,
            sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
            if (isAutoPlayTrailer && isTrailerInitialized) YES else NO,
            contentItem.contentConfigType,
            getAppleStatusValue()
        )
    }

    private fun trackAppleActivateFromPopupClick(){
        val metaDetails =detailsResponse?.data?.metaDetails

        detailAnalytics.trackAppleActivateNowClick(
            metaDetails?.getVodTitle() ?: "",
            metaDetails?.contentType ?: "",
            metaDetails?.genre,
            metaDetails?.audio,
            contentItem.origin.toUpperCase(),
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            metaDetails?.provider ?: contentItem.provider,
            metaDetails?.getParentTitle() ?: "",
            /*Using Key partnerSubscriptionType to identify if the content is Free or Premium*/
            metaDetails?.partnerSubscriptionType?.contains(
                FREE,
                true
            ) == true,
            (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
            contentItem.railPosition,
            contentItem.origin,
            contentItem.railCategory,
            metaDetails?.audio?.getOrNull(0),
            metaDetails?.genre?.getOrNull(0),
            contentAuth = if (contentAuth) YES else NO,
            contentItem.contentType,
            contentItem.contentPosition,
            metaDetails?.rating ?: "",
            metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            metaDetails?.actor,
            sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
            if (isAutoPlayTrailer && isTrailerInitialized) YES else NO,
            liveContent = if (isLiveContent(
                    contentItem.contentType,
                    detailsResponse?.data?.metaDetails?.isLiveContent
                )
            ) YES else NO,
            contentItem.contentConfigType,
            getAppleStatusValue(),
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        )
    }

    private fun trackAppleActivateClick(){
        val metaDetails =detailsResponse?.data?.metaDetails

        detailAnalytics.trackActivateAppleTvSubscriptionClick(
            metaDetails?.getVodTitle() ?: "",
            metaDetails?.contentType ?: "",
            metaDetails?.genre,
            metaDetails?.audio,
            contentItem.origin.toUpperCase(),
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            metaDetails?.provider ?: contentItem.provider,
            metaDetails?.getParentTitle() ?: "",
            /*Using Key partnerSubscriptionType to identify if the content is Free or Premium*/
            metaDetails?.partnerSubscriptionType?.contains(
                FREE,
                true
            ) == true,
            (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
            contentItem.railPosition,
            contentItem.origin,
            contentItem.railCategory,
            metaDetails?.audio?.getOrNull(0),
            metaDetails?.genre?.getOrNull(0),
            contentAuth = if (contentAuth) YES else NO,
            contentItem.contentType,
            contentItem.contentPosition,
            metaDetails?.rating ?: "",
            metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            metaDetails?.actor,
            sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
            NO,
            liveContent = if (isLiveContent(
                    contentItem.contentType,
                    detailsResponse?.data?.metaDetails?.isLiveContent
                )
            ) YES else NO,
            contentItem.contentConfigType,
            getAppleStatusValue()
        )




    }

    private fun trackAppleLinkAccountClick(){
        detailAnalytics.trackLinkAppleTvAccountClick(
            contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
            sharedPrefs.getDeviceType() ?: "",
            sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
            getAppleStatusValue()
        )
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isTabletLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        activity?.let {
            if (isTablet(it) &&  !(activity as LandingActivity).isScreenFullScreenMode) {
                (binding.seriesRecycler.adapter as? SeriesAdapter)?.resetContentCardDimension()
                var margin:Int=0
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    disableFullscreenForLandscape()
                    margin=it.resources.getDimension(R.dimen.margin_150dp).toInt()
                } else {
                    changeToPortraitMode()
                    margin=it.resources.getDimension(R.dimen.margin_100dp).toInt()
                }
                binding.shareBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    setMargins(margin,0,0,0)
                }
                binding.watchlistBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    setMargins(0,0,margin,0)
                }
            }
        }
    }

    private fun updateButtonLayoutWeight(){
        if(isDeviceTablet) {
            isAutoPlayTrailer = viewModel.sharedPrefs.getAutoPlayTrailerOn()
            context?.let {
                setPlayButtonsWeight(binding.layLinearButtons,getPlayButtonType(isPortrait(it)))
            }
        } else {
            if(detailsResponse?.data?.metaDetails?.partnerTrailerInfo.isNullOrEmpty()){
                binding.layLinearButtons.weightSum = 1F
                return
            }
            if(!isAutoPlayTrailer){
                binding.layLinearButtons.weightSum = if(isNavigateToPlayer) 1F else 2F
            }else binding.layLinearButtons.weightSum = 1F
        }
    }


    private fun getPlayButtonType(isPortrait: Boolean):PlayButtonType{
        return when{
            detailsResponse?.data?.metaDetails?.partnerTrailerInfo.isNullOrEmpty() ->
                if(isPortrait) PlayButtonType.MATCH_PLAY_BUTTON else PlayButtonType.CENTER_PLAY_BUTTON

            isAutoPlayTrailer -> if(isPortrait) PlayButtonType.MATCH_PLAY_BUTTON else PlayButtonType.CENTER_PLAY_BUTTON
            isNavigateToPlayer -> if(isPortrait) PlayButtonType.MATCH_PLAY_BUTTON else PlayButtonType.CENTER_PLAY_BUTTON
            else -> if(isPortrait) PlayButtonType.PLAY_BUTTON_WITH_TRAILER_PORTRAIT else PlayButtonType.PLAY_BUTTON_WITH_TRAILER_LANDSCAPE
        }
    }

    private fun checkForAutoPlayTrailer():Boolean{
        if(detailsResponse?.data?.metaDetails?.partnerTrailerInfo.isNullOrEmpty())
            return false
        if(isAutoPlayTrailer)
            return true
        return false
    }

    private fun trackProbeSSOError(
        errorCode: String?,
        errorMessage: String?,
        type: String
    ) {
        probePlayerEventInitSdk(null, playerModel, DefaultBandwidthMeter(), sharedPrefs.getOriginalSubscriberId())
        probePlayerEventError(errorCode, type?:"NA", errorMessage?:"NA")
    }
}
