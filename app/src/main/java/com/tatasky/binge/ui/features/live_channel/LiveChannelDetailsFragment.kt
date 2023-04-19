package com.tatasky.binge.ui.features.live_channel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import com.google.gson.Gson
import com.tatasky.binge.NavDetailsDirections
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.databinding.FragmentLiveChannelDetailsBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.interfaces.*
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.SingleEventParcelizeWrapper
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.MiscAnalytics
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.details.DetailAnalytics
import com.tatasky.binge.ui.features.details.DetailsFragmentDirections
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.adapter.RailAdapter
import com.tatasky.binge.ui.features.home.model.RailItemsModel
import com.tatasky.binge.ui.features.home.model.RailsModel
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.*
import com.tatasky.binge.ui.features.player.PlayerAnalytics
import com.tatasky.binge.ui.features.player.PlayerModel
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.OrientationManager.ScreenOrientation.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import java.util.*
import java.util.concurrent.TimeUnit

open class LiveChannelDetailsFragment :
    CancellationBaseFragment<FragmentLiveChannelDetailsBinding, LiveChannelDetailsViewModel>(),
    CommonLoadMoreClickListener,
    OrientationManager.OrientationListener {

    val liveChannelDetailsFragmentArgs by navArgs<LiveChannelDetailsFragmentArgs>()
    private var shouldTrackPIView: Boolean = true
    private var isUpgradePlan: Boolean = false
    private var ratingValidationType: RatingValidationType? = null
    private lateinit var orientationManager: OrientationManager
    private var incrementCountOnlyOnce: Boolean = false
    private var isPackUpdated: Boolean = false
    private var isResultHandled = false
    private var isDeviceTablet = false

    @Inject
    lateinit var playerAnalytics: PlayerAnalytics

    protected var mDisplayManager: DisplayManager? = null
    private var mWidth: Int = 0
    private var favToast: Toast? = null
    protected var isPlayerStarted = false
    private val REQUEST_FOR_PACK_SELECTION: Int = 1012
    private var isPaused: Boolean = false
    private var liveOrientation =
        MutableLiveData<SingleEvent<OrientationManager.ScreenOrientation>>()
    protected var isTrailerInitialized = false
    protected var playerModel: PlayerModel? = null
    private var isPlaying: Boolean = false
    private var commonViewModel: CommonSampleViewModel? = null
    private lateinit var channelId: String
    var mIsInFullScreenMode: Boolean = false
    protected lateinit var contentItem: ContentItem
    private var isNavigatedToSubscription = false
    protected var contentAuth = false
    private var isTabletSwitchToFullScreen: Boolean =false
    private var isTabletLandscape: Boolean =false
    private var isNavigateToOther: Boolean = false

    @Inject
    lateinit var miscAnalytics: MiscAnalytics

    @Inject
    lateinit var detailAnalytics: DetailAnalytics

    private val args by navArgs<LiveChannelDetailsFragmentArgs>()

    override fun getViewModelClass() = LiveChannelDetailsViewModel::class.java

    override fun layoutId() = R.layout.fragment_live_channel_details

    override fun getViewModelOwner() = this

    private val otherChannelItemClickListener = object : CommonDTOClickListener {
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
            if (viewModel.getCurrentChannelDetails()?.id != iListItem.id) {
                val extras = if (!transitions.isNullOrEmpty())
                    FragmentNavigatorExtras(*transitions.toTypedArray())
                else FragmentNavigatorExtras()
                iListItem.railName = railTitle
                iListItem.source = EVENT_VALUE_SOURCE_DETAIL
                iListItem.origin = origin ?: EventConstants.TYPE_EDITORIAL
                iListItem.contentPosition = (iItemPosition + 1).toString()
                iListItem.railPosition = iSectionPosition.toString()
                findNavController().navigateSafe(
                    LiveChannelDetailsFragmentDirections.actionToDetail(
                        iListItem,
                        false,
                        railItemsModel,
                        contentAnalyticsModel
                    ),
                    extras
                )
            }
        }
    }

    override fun onDestroy() {
        /**If user is logged in or there is update in user's pack
         * then set value on savedStateHandle when user exits from current
         * screen either via back press or with direct bottom tab changes
         * to observe in previous screen, After observing refresh the data if required
         */
        if (isPackUpdated) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(UPDATE_IN_PACK,
                SingleEventParcelizeWrapper(SingleEvent(isPackUpdated)))
            //User's pack details has changed...Refresh the data fro Crown visibility
            d(this.javaClass.simpleName, "UPDATE_IN_PACK $isPackUpdated")
        }
        if (incrementCountOnlyOnce && sharedPrefs.isEligibleForAppRating()) sharedPrefs.saveNumberOfContentPlaybackForAppRating(
            sharedPrefs.getNumberOfContentPlaybackForAppRating().inc())
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (true == savedInstanceState?.containsKey("id")) {
            channelId = savedInstanceState.getString("id", "")
        }
        if (true == savedInstanceState?.containsKey("contentItem")) {
            contentItem = Gson().fromJson(savedInstanceState.getString("contentItem", null),
                ContentItem::class.java)
        }
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

    @SuppressLint("SourceLockedOrientationActivity")
    override fun toBeCalledOnce() {
        isTabletLandscape=activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
        args.contentItem?.let { contentItem = it } ?: run {
            onError(ErrorModel(message = com.tatasky.binge.utils.COMMON_ERROR_MSG))
            return
        }
        channelId = contentItem.id
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        commonViewModel = ViewModelProvider(requireActivity(),
            viewModelFactory)[CommonSampleViewModel::class.java]
        context?.let {
            if(!isTablet(it)) {
                commonViewModel?.saveOrientation(OrientationManager.ScreenOrientation.PORTRAIT)
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }else{
                if(it.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE){
                    changeToTabletPortraitMode()
                }
            }

        }
        orientationManager = OrientationManager(activity, SensorManager.SENSOR_DELAY_NORMAL, this)
        mWidth = Resources.getSystem().displayMetrics.widthPixels
        setClickListeners()
        contentAuth = viewModel.isContentSubscribed(channelId)
        handleApiCall(channelId, true)

        activity?.let {
            if(isTablet(it)){
                isDeviceTablet=true
            }
        }
    }

    private fun setClickListeners() {
        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onOrientationChange(screenOrientation: OrientationManager.ScreenOrientation?) {
        liveOrientation.postValue(SingleEvent(screenOrientation ?: PORTRAIT))
    }

    override fun setObserver() {
        super.setObserver()
        viewModel.getContentToken().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { token ->
                playerModel?.setLA_URL(playerModel?.getLA_URL() + "&ls_session=$token")
                playerModel?.setDrmLicenseUrl(playerModel?.getLA_URL() ?: "")
                playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
            }
        })
        viewModel.digitalFeedUrls.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.data?.let {
                playerModel?.let { playerModel ->
                    val updateDigitalFeedPlaybackUrls = viewModel.getUpdatedDigitalFeedPlaybackUrls(
                        it.dashUrl,
                        it.ldashUrl
                    )
                    playerModel.setPlaybackUrl(updateDigitalFeedPlaybackUrls.first)
                    playerModel.setDrmLicenseUrl(updateDigitalFeedPlaybackUrls.second)
                    navigateToPlayer(playerModel)
                }
            }
        })
        viewModel.playbackBtnStateHolder.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { playBtnState ->
                handlePlaybackButtonState(playBtnState)
            }
        })
        viewModel.liveChannelDetails.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.data?.let { liveChannelDetails ->
                liveChannelDetails.meta?.getOrNull(0)?.let { contentMetaDetails ->
                    contentMetaDetails.boxCoverImage?.let { boxCoverImgUrl ->
                        val finalImgUrl = getCloudinaryUrl(viewModel.getCloudinaryUrl() ?: "",
                            mWidth,
                            (mWidth * 0.9999 * 0.56).toInt(),
                            boxCoverImgUrl
                        )
                        imageLoad(
                            binding.ivPoster,
                            finalImgUrl,
                        )
                    }
                    liveChannelDetails.channelMeta?.let { channelMetaDetails ->
                        channelId = channelMetaDetails.id ?: ""
                        channelMetaDetails.logo?.let { logoUrl ->
                            updateRoundedImage(
                                binding.channelLogoIv,
                                viewModel.getCloudinaryUrl(),
                                logoUrl,
                                R.drawable.ic_detail_placeholder
                            )
                        }
                    }
                    binding.contentTitleTv.text = contentMetaDetails.title
                    binding.contentTimingTv.text = getString(R.string.live_channel_duration_time,
                        contentMetaDetails.startTime.getEpochTimeInFormat(
                            HH_MM_WITHOUT_MERIDIAN_TIME_FORMAT),
                        contentMetaDetails.endTime.getEpochTimeInFormat())
                    args.railItemsModel?.let { railItemsModel ->
                        val title = getString(R.string.live_channels)
                        showOtherChannelRail(railItemsModel)
                    }
                    binding.scrollingContent.show()
                    if (shouldTrackPIView) {
                        shouldTrackPIView = false
                        trackPIView()
                    }
                } ?: onError(ErrorModel())
            }
        })

        commonViewModel?.deviceForceLogout()?.observe(requireActivity(), Observer {
            it.getContentIfNotHandled()?.let {
                context?.let {
                    logoutApplication(it)
                }
            }
        })

        commonViewModel?.validateContentRatingResponse?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                if (response.data?.pinRequired == true) {
                    // Validate pin
                    ratingValidationType = RatingValidationType.ContentRatingValidation
                    isResultHandled = false
                    (activity as? LandingActivity)?.isResultHandled = true
                    findNavController().navigateSafe(NavDetailsDirections.actionGlobalParentalControlBottomDialogFragment(
                        ACTION_PIN_VERIFICATION,
                        null,
                        false,
                        PLAY))
                } else {
                    // No need to validate rating, play content
                    prepareContentPlayback()
                }
            }
        })

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<SingleEventParcelizeWrapper>(
            UPDATE_IN_PACK)?.observe(viewLifecycleOwner, Observer {
            it.booleanEventValue.getContentIfNotHandled()?.let { isPackUpdated ->
                this.isPackUpdated = isPackUpdated
            }
        })

        viewModel.previouslyUsedMobileNumberResponse.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                findNavController().navigateSafe(NavDetailsDirections.actionGlobalLoginBottomSheetDialogFragment(
                    isParentalPinSetupRequested = false,
                    isParentalPinVerificaitionRequested = false,
                    isLoggedIn = false,
                    previouslyUsedMobileNumbersList = response.data?.mobileNUmberList?.toTypedArray(),
                    loginSource = SOURCE_PLAY_CLICK))
            }
        })

        viewModel.previouslyUsedMobileNumberError.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateSafe(NavDetailsDirections.actionGlobalLoginBottomSheetDialogFragment(
                    isParentalPinSetupRequested = false,
                    isParentalPinVerificaitionRequested = false,
                    isLoggedIn = false,
                    previouslyUsedMobileNumbersList = null,
                    loginSource = SOURCE_PLAY_CLICK))
            }
        })

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ParentalControlBottomSheetResult>(
                KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT
            )?.observe(viewLifecycleOwner) { result ->
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
                                            false)
                                    toastView.textLoginSuccessfulToast.text =
                                        getString(R.string.toast_msg_pin_changed_successful)
                                    toastView.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                                    showCustomToast(context,
                                        toastView?.root,
                                        Gravity.FILL_HORIZONTAL)
                                }
                            }
                        }
                        ParentalControlBottomSheetResultStatus.PIN_VERIFIED -> {
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_PIN_VERIFICATION -> {
                                    // Play content with other checks
                                    if (ratingValidationType == RatingValidationType.ContentRatingValidation) {
                                        ratingValidationType = null
                                        prepareContentPlayback()
                                    } else if (ratingValidationType == RatingValidationType.NonContentRatingValidation) {
                                        ratingValidationType = null
                                        navigateToSubscriptionActivity(
                                            isUpgradePlan = isUpgradePlan,
                                            ratingValidationRequired = false
                                        )
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

        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                handleUpdateInPack(it)
            }
        })
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!viewModel.onlyMessage) findNavController().navigateUp()
            }
        })
        activity?.let {
            if(!isTablet(it)) {
                liveOrientation.observe(viewLifecycleOwner, Observer {
                    it?.getContentIfNotHandled()?.let { orientation ->
                        when (orientation) {
                            PORTRAIT, REVERSED_PORTRAIT -> {
                                changeToPortraitMode()
                            }
                            LANDSCAPE, REVERSED_LANDSCAPE -> {
                                changeToLandscapeMode()
                            }
                        }
                    }
                })
            }
        }
    }

    private fun showOtherChannelRail(railItemsModel: RailItemsModel) {
        binding.railsModel = RailsModel(
            railItemsModel.railTitle,
            RailAdapter(
                otherChannelItemClickListener,
                railItemsModel.railItems?.toList() ?: listOf(),
                railItemsModel.railItemLayoutType,
                0,
                viewModel.getCloudinaryUrl(),
                railItemsModel.railSectionSource,
                false,
                railItemsModel.railTitle,
                viewModel.sharedPrefs.getProviderLogo(),
                RailPoint(),
                viewModel.sharedPrefs,
                refId = contentItem.refId,
                railSectionType = railItemsModel.railSectionType
            )
        )
        binding.otherChannelsRv.homeSeeAll.hide()
        binding.otherChannelsRv.ivLiveIndicator.show()
        binding.otherChannelsRv.root.show()
    }

    private fun prepareContentPlayback() {
        viewModel.liveChannelDetails.value?.peekContent()?.data?.let { details ->
            playerModel = viewModel.generatePlayerModel(details)
            val currentChannelDetails = viewModel.getCurrentChannelDetails()
            if (currentChannelDetails?.digitalFeed == true) {
                val partnerName = currentChannelDetails.digitalPartner
                val channelId = currentChannelDetails.id
                if (partnerName != null && channelId != null)
                    viewModel.fetchPlaybackUrlsForDigitalFeed(
                        partnerName,
                        channelId.toString()
                    )
                else
                    onError(ErrorModel())
            } else {
                val epids = details.detail?.offerId?.epids
                epids?.let { epidsList ->
                    var epidToUse: List<Epid> = epidsList
                    for (epid in epidsList) {
                        if (!(epid.bid.isBlank() && epid.epid.isBlank())) {
                            epidToUse = listOf(epid)
                            break
                        }
                    }
                    val contentId = viewModel.getCurrentContentMetaDetails()?.id ?: ""
                    if (viewModel.isTokenExpired(contentId))
                        viewModel.generateControlToken(
                            epidToUse,
                            contentId,
                            true,
                            currentChannelDetails?.channelName ?: ""
                        )
                    else {
                        val token = viewModel.getToken(contentId)
                        playerModel?.setLA_URL(playerModel?.getLA_URL() + "&ls_session=$token")
                        playerModel?.setDrmLicenseUrl(playerModel?.getLA_URL() ?: "")
                        playerModel?.let { playerModel: PlayerModel -> navigateToPlayer(playerModel) }
                    }
                } ?: run {
                    context?.let { showToast(it, getString(R.string.unable_to_process)) }
                }
            }
        }
    }

    private fun checkAndMoveToContentPlayback() {
        if (viewModel.isLoggedIn()) {
            if (viewModel.isContentSubscribed(channelId)) {
                if (sharedPrefs.isParentalPinExists()) {
                    commonViewModel?.validateContentRating(viewModel.getCurrentContentMetaDetails()?.rating)
                } else
                    prepareContentPlayback()
            } else {
                viewModel.checkForManagedAppEligibility { doSubscriptionChecks() }
            }
        } else {
            viewModel.getPreviouslyUsedMobileNumbers()
        }
    }

    private fun doSubscriptionChecks() {
        changeToPortraitMode()
        viewModel.onlyMessage = true
        if (shouldStartCancellationTrigger()) {
            viewModel.fetchBaIdList(sharedPrefs.getOriginalSubscriberId())
            return
        }
        handleSubscriptionDialogAndNavigation()
    }

    private fun handlePlaybackButtonState(playBtnState: LiveChannelPlaybackButtonStateEnum) {
        when (playBtnState) {
            LiveChannelPlaybackButtonStateEnum.STATE_PLAY -> {
                binding.contentPlaybackBtn.text = getString(R.string.play_now)
                binding.contentPlaybackBtn.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_play)
                binding.contentPlaybackBtn.setOnClickListener(null)
                binding.contentPlaybackBtn.setSingleOnClick {
                    checkAndMoveToContentPlayback()
                }
            }
            LiveChannelPlaybackButtonStateEnum.STATE_RESUME -> {
                binding.contentPlaybackBtn.text = getString(R.string.resume)
                binding.contentPlaybackBtn.icon = null
            }
            LiveChannelPlaybackButtonStateEnum.STATE_PAUSE -> {
                binding.contentPlaybackBtn.text = getString(R.string.pause)
                binding.contentPlaybackBtn.icon = null
            }
        }
    }

    private fun handleUpdateInPack(it: Boolean) {
        if (it) {
            //show alert or update pack
            handleApiCall(channelId, false)
            findNavController().previousBackStackEntry?.savedStateHandle?.set(UPDATE_IN_PACK,
                SingleEventParcelizeWrapper(SingleEvent(it)))
        }
    }

    private fun trackPIView() {
        val currentContentMetaDetails = viewModel.getCurrentContentMetaDetails()
        val currentChannelDetails = viewModel.getCurrentChannelDetails()
        detailAnalytics.trackViewContentDetail(
            currentChannelDetails?.channelName ?: "",
            currentContentMetaDetails?.contentType ?: "",
            currentContentMetaDetails?.genre,
            currentContentMetaDetails?.audio,
            contentItem.origin.uppercase(Locale.ROOT),
            args.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            TATAPLAY,
            currentChannelDetails?.channelName?:"",
            true,
            (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
            contentItem.railPosition,
            railType = contentItem.railConfigType,
            railCategory = contentItem.railSectionType,
            currentContentMetaDetails?.audio?.getOrNull(0),
            currentContentMetaDetails?.genre?.getOrNull(0),
            contentAuth = if (contentAuth) YES else NO,
            contentItem.contentType,
            contentItem.contentPosition,
            currentContentMetaDetails?.rating ?: "",
             "",
            sharedPrefs.getDeviceType() ?: "",
            viewModel.getCurrentContentMetaDetails()?.actor,
            sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
            if (isTrailerInitialized) YES else NO,
            if (contentItem.contentType.equals(TYPE_LIVE, true)) YES else NO,
            contentItem.contentConfigType,
            contentItem.searchKeyword,
            contentItem.suggestorForMixpanel
        )
    }

    /**
     * Method call to hit api initially
     */
    private fun handleApiCall(channelId: String, showLoader: Boolean) {
        viewModel.fetchSelectedLiveChannelDetails(channelId, showLoader)
    }

    override fun onResume() {
        super.onResume()
        binding.executePendingBindings()
        isPaused = false
        if (isNavigateToOther) {
            activity?.let {
                if(isTablet(it) && it.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE){
                    disableFullscreenForLandscape()
                }
            }
            isNavigateToOther = false
         }
        if (isPlayerStarted && ::orientationManager.isInitialized) orientationManager.enable()
        if (isNavigatedToSubscription) Handler(Looper.getMainLooper()).postDelayed(Runnable {
            binding.scrollingContent.smoothScrollTo(0, 0)
            isNavigatedToSubscription = false
        }, 500)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("updateSubscription")
            ?.observe(viewLifecycleOwner, Observer {
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("updateSubscription")
                handleApiCall(channelId, it)
            })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::channelId.isInitialized) outState.putString("id", channelId)
        if (::contentItem.isInitialized) outState.putString("contentItem",
            Gson().toJson(contentItem))
        super.onSaveInstanceState(outState)
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
        binding.ivPoster.alpha = 1f
        binding.ivPosterOverlay.show()
        binding.clDetails.show()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    open fun changeToPortraitMode() {
        commonViewModel?.saveOrientation(PORTRAIT)
        activity?.let {
            if (!isTablet(it))
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
        }
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val result = Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        handlePortraitMode()
    }

    open fun changeToLandscapeMode() {
        commonViewModel?.saveOrientation(LANDSCAPE)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        handleLandscapeMode()
    }

    override fun showDeviceStatusLogout() {
        if (mIsInFullScreenMode) {
            changeToPortraitMode()
        }
        super.showDeviceStatusLogout()
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
            if (viewModel.getCurrentContentMetaDetails() != null) return
        }
        showDialog(DialogModel(false, null, errorModel.message, "Ok", null),
            object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    hideDialog()
                    if (!viewModel.onlyMessage) findNavController().navigateUp()
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

    override fun onPause() {
        super.onPause()
        isPaused = true
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun handleLandscapeMode() {
        e("HungamaPlayerFragmnet", "inside handleLandscapeMode")
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
        mIsInFullScreenMode = true
        (activity as LandingActivity).isScreenFullScreenMode=true
    }

    private fun handlePortraitMode() {
        activity?.let {
            if (!isTablet(it) && !mIsInFullScreenMode)
                return
        }
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
        mIsInFullScreenMode = false
        binding.scrollingContent.show()
        (activity as? LandingActivity)?.apply {
            isScreenFullScreenMode = false
            handlePortraitForBottomNav()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true // default to enabled
            ) {
                override fun handleOnBackPressed() {
                    if (mIsInFullScreenMode) {
                        if(isTabletLandscape && isDeviceTablet)
                            changeToTabletPortraitMode()
                        else changeToPortraitMode()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun navigateToPlayer(playerModel: PlayerModel) {
        if (isExternalDisplayAvailable()) {
            showToast(context, getString(R.string.casting_not_allowed))
            return
        }
        if (!playerModel.getPlaybackUrl().isNullOrEmpty()) {
            if (::orientationManager.isInitialized) orientationManager.enable()
            actionOnPlayClick()
            isNavigateToOther = true
            viewModel.togglePlaybackButton(LiveChannelPlaybackButtonStateEnum.STATE_PAUSE)
            viewModel.startPlayer(playerModel)
            binding.ivPoster.alpha = 1f
        } else {
            showToast(context, "Unable to play Content")
        }
    }

    private fun showACAAlert(message: String) {
        showDialog(DialogModel(false, null, message, getString(R.string.ok), null),
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

    override fun onLoadMoreClick(pageOffset: Int) {}

    private fun disableOrientation() {
        if (::orientationManager.isInitialized) orientationManager.disable()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FOR_PACK_SELECTION && resultCode == Activity.RESULT_OK) {
            handleApiCall(channelId, false)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun navigateToSubscriptionActivity(
        isUpgradePlan: Boolean = false,
        ratingValidationRequired: Boolean = true,
    ) {
        isNavigatedToSubscription = true
        activity?.let {
            if (sharedPrefs.isParentalPinExists()
                && sharedPrefs.getParentalRating()?.ageRatingName.takeIf { rating ->
                    rating.isNullOrEmpty() || rating == getString(R.string.no_restrictions)
                } == null && ratingValidationRequired
            ) {
                ratingValidationType = RatingValidationType.NonContentRatingValidation
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
                if (!sharedPrefs.isManagedAppEnabled()) {
                    val channelId = viewModel.getCurrentChannelDetails()?.id ?: ""
                    startActivityForResult(
                        getSubscriptionActivityIntent(
                            it,
                            fromLogin = false,
                            selectedAppId = channelId,
                            fromScreen = SOURCE_PLAY_CLICK,
                            startPackListing = true,
                            partnerId = channelId
                        ),
                        REQUEST_FOR_PACK_SELECTION
                    )
                } else {
                    showMiniDrawer(
                        context = it,
                        fromLogin = false,
                        fromScreen = SOURCE_PLAY_CLICK,
                        startPackListing = true,
                        journeyRef = if (isUpgradePlan) HOME_CONTENT else ""
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
        journeyRef: String = "",
    ) {
        val channelId = viewModel.getCurrentChannelDetails()?.id ?: ""
        val currentPack = sharedPrefs.getSubscribedPack()
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
                        channelId,
                        skipDrawer = true
                    )

                } else (activity as? LandingActivity)?.showMiniDrawer(
                    CONTENT_PLAYBACK,
                    journeyRef,
                    channelId,
                    skipDrawer = false
                )
            }
        }
    }

    private fun handleSubscriptionDialogAndNavigation() {
        var trackSubscribePopup = false
        var trackUpgradePopup = false
        val subscribedPack = sharedPrefs.getSubscribedPack()
        val currentContentDetails = viewModel.getCurrentContentMetaDetails()
        val currentChannelDetails = viewModel.getCurrentChannelDetails()
        val dialogListener = object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                if (trackSubscribePopup)
                    miscAnalytics.trackMixPanelSusbscribePopupSubscribe(
                        currentContentDetails?.title ?: ""
                    )
                else if (trackUpgradePopup) {
                    trackUpgradePopup = false
                    miscAnalytics.trackMixPanelUpgradePopupUpgrade(
                        currentContentDetails?.title ?: "",
                        currentChannelDetails?.channelName ?: ""
                    )
                }
                hideDialog()
                isUpgradePlan = true
                navigateToSubscriptionActivity(isUpgradePlan = true)
            }
            override fun onSecondaryButtonClick() {
                if (trackSubscribePopup)
                    miscAnalytics.trackMixPanelSusbscribePopupCancel()
                else if (trackUpgradePopup) {
                    trackUpgradePopup = false
                    miscAnalytics.trackMixPanelUpgradePopupCancel()
                }
                isNavigatedToSubscription = false
                hideDialog()
            }

            override fun onCloseButtonClick() {
                isNavigatedToSubscription = false
                hideDialog()
            }
        }
        if (sharedPrefs.getUserDetails()?.freeTrialAvailed == false && subscribedPack == null || subscribedPack?.let {
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
        } else if (subscribedPack.downgradeRequested == true) {
            showDialog(
                DialogModel(
                    false,
                    R.drawable.ic_subscription_error,
                    "",
                    getString(R.string.done),
                    "",
                    subscribedPack.downgradeRequestedMessage ?: ""
                ),
                object : CommonDialogEventListener {
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
        } else if (subscribedPack.upgradeFDOCheck == true) {
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
        } else if (!subscribedPack.isCancelled) {
            val upgradeMsg =
                String.format(
                    getString(R.string.upgrade_subscription_message),
                    getString(R.string.content)
                )
            miscAnalytics.trackMixPanelUpgradePopup()
            trackUpgradePopup = true
            if (sharedPrefs.getConfigResponse()?.data?.config?.enableTickTickJourney == false) {
                isUpgradePlan = true
                navigateToSubscriptionActivity(isUpgradePlan = true)
                return
            } else showDialog(
                DialogModel(
                    false,
                    R.drawable.ic_subscribe,
                    getString(R.string.upgrade_subscription),
                    getString(R.string.upgrade),
                    getString(R.string.cancel),
                    upgradeMsg
                ), dialogListener
            )
        } else if (!subscribedPack.isInactive) {
            miscAnalytics.trackMixPanelUpgradePopup()
            trackUpgradePopup = true
            if (sharedPrefs.getConfigResponse()?.data?.config?.enableTickTickJourney == false) {
                isUpgradePlan = true
                navigateToSubscriptionActivity(isUpgradePlan = true)
                return
            } else showDialog(
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
        } else if (subscribedPack.isInactive) { //Pack expired free/paid
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
                it.showRenewPlanAfterExpiryNudge(title, desc, btnText)
            }
        } else {
            navigateToSubscriptionActivity()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
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

    // TODO [Live]: Use as per Live requirement
    private fun actionOnPlayClick() {
        incrementCountOnlyOnce = true
//        val id = detailsResponse?.data?.metaDetails?.id
//        val vodId = detailsResponse?.data?.metaDetails?.vodId
//        val contentId = vodId ?: id ?: ""
        /*playerModel?.let {
            viewModel.actionCW(it.getContentId()!!,
                getContentType(it.getContentType()!!),
                it.getResumeTime().toInt() / 1000,
                it.getTotalDuration().toInt())
            if (PROVIDER_SHEMAROO.equals(it.getProvider(),
                    true)
            ) viewModel.callShemaroomeAnalyticsPlayEvent(it, "play", "play", 1)

            if (PROVIDER_PLANET_MARATHI.equals(it.getProvider(),
                    true)
            ) viewModel.callPlanetMarathiAnalyticsPlayEvent(it, "play")
        }*/
        /*viewModel.trackOnceIn24hrLearnAction(detailsResponse?.data?.metaDetails?.partnerSubscriptionType,
            contentType,
            contentId,
            detailsResponse?.data?.metaDetails?.taShowType ?: "",
            detailsResponse?.data?.metaDetails?.provider ?: "",
            detailsResponse?.data?.detail?.contractName ?: "",
            CLICK_LEARN_ACTION,
            contentItem.refId)*/

    }

    protected fun isExternalDisplayAvailable() =
        mDisplayManager != null && mDisplayManager?.displays != null && mDisplayManager!!.displays.size > 1

    override fun forceLogout() {
        try {
            binding.playerFrame.hide()
            binding.miniProgressPlayer.hide()
            binding.ivPoster.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (mIsInFullScreenMode) changeToPortraitMode()
        super.forceLogout()
    }

    protected fun trackOnPlayerFailure(
        playerModel: PlayerModel?,
        errorMsg: String,
        contentItem: ContentItem?,
    ) {
        playerAnalytics.trackPlaybackFailure(playerModel?.getTitle() ?: "Not Available",
            playerModel?.getGenre(),
            playerModel?.getContentType() ?: "",
            errorMsg,
            playerModel?.getProvider() ?: "",
            args.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            (contentItem?.origin ?: "").toUpperCase(),
            contentItem?.source?.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            playerModel?.getAudioLanguages(),
            sharedPrefs.getSubscribedPack(),
            contentItem?.railPosition ?: "",
            viewModel.getCurrentContentMetaDetails()?.title ?: playerModel?.getParentTitle()
            ?: "",
            true,
            contentItem?.source ?: "",
            railType = contentItem?.railConfigType ?: "",
            railCategory = contentItem?.railSectionType ?: "",
            playerModel?.getAudioLanguages()?.getOrNull(0),
            playerModel?.getGenre()?.getOrNull(0),
            if (contentAuth) YES else NO,
            contentItem?.contentType ?: "",
            contentItem?.contentPosition ?: "",
            viewModel.getCurrentContentMetaDetails()?.rating ?: "",
            /*detailsResponse?.data?.metaDetails?.releaseYear ?:*/ "",
            sharedPrefs.getDeviceType() ?: "",
            viewModel.getCurrentContentMetaDetails()?.actor,
            if (sharedPrefs.getAutoPlayTrailerOn() && isTrailerInitialized) YES else NO,
            if (contentItem?.contentType.equals(TYPE_LIVE, true)) YES else NO,
            contentItem?.contentConfigType ?: "")
    }

    /**
     * Track player playback errors with error details
     */
    protected fun trackOnPlayerError(
        playerModel: PlayerModel?,
        errorCode: String?,
        errorMsg: String?,
        origin: String?,
        type: String?,
    ) {
        playerAnalytics.trackPlaybackError(errorCode = errorCode ?: "",
            errorMsg = errorMsg ?: "",
            origin = (origin ?: com.tatasky.binge.utils.EDITORIAL).toUpperCase(),
            type = type ?: "",
            partnerName = playerModel?.getProvider() ?: "")
    }

    open fun changeToTabletPortraitMode(){
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        disableFullscreenForLandscape()
    }

    protected fun disableFullscreenForLandscape(){
        commonViewModel?.saveOrientation(OrientationManager.ScreenOrientation.LANDSCAPE)
        var widthTablet:Int = 0
        var heightTablet:Int =0
        context?.let {
            widthTablet=it.resources.getDimension(R.dimen.player_view_width).toInt()
            heightTablet=it.resources.getDimension(R.dimen.player_view_height).toInt()
        }
        val posterLayoutParams = binding.ivPoster.layoutParams as ConstraintLayout.LayoutParams
        binding.ivPoster.layoutParams = setPlayerViewLayouts(posterLayoutParams,widthTablet,heightTablet)
        val playerFrameParams = binding.playerFrame.layoutParams as ConstraintLayout.LayoutParams
        binding.playerFrame.layoutParams = setPlayerViewLayouts(playerFrameParams,widthTablet,heightTablet)
        binding.ivPoster.alpha = 1f
        binding.imgBack.show()
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isTabletLandscape=newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        activity?.let {
            if (isTablet(it) &&  !(activity as LandingActivity).isScreenFullScreenMode) {

                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    disableFullscreenForLandscape()
                } else {
                    changeToPortraitMode()
                }



            }
        }
    }
    }
