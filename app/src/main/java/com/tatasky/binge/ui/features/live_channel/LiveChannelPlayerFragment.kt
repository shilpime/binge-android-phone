package com.tatasky.binge.ui.features.live_channel

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaCodec.*
import android.media.MediaDrm
import android.os.*
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.text.CaptionStyleCompat
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.TimeBar
import com.tatasky.binge.R
import com.tatasky.binge.analytics.ANALYTICS_TIME_FORMAT
import com.tatasky.binge.analytics.PARA_ERROR_TYPE_PLAYER
import com.tatasky.binge.analytics.PARA_PI_ERROR_ORIGIN
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.RrmSessionInfo
import com.tatasky.binge.databinding.FragmentLiveChannelPlayerBinding
import com.tatasky.binge.databinding.ToastWatchlistBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.player.MyScaleGestureDetector
import com.tatasky.binge.ui.features.player.MyScaleGestureDetector.Companion.optimumPinchZoomScaleFactor
import com.tatasky.binge.ui.features.player.PlayerModel
import com.tatasky.binge.ui.features.player.listeners.PlayerDurationWatcher
import com.tatasky.binge.ui.features.player.listeners.PlayerListener
import com.tatasky.binge.ui.features.player.model.AudioLanguage
import com.tatasky.binge.ui.features.player.model.Bitrate
import com.tatasky.binge.ui.features.player.model.VideoQuality
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.PlayerUtils.getCurrentSeekBarProgressInPercentage
import com.ttn.ttnplayer.listeners.TtnPlayerListener
import com.ttn.ttnplayer.player.TtnPlayerHelper
import com.ttn.ttnplayer.util.SECURITY_LEVEL_L3
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.coroutines.delay
import okhttp3.Response
import java.util.*
import kotlin.math.max
import kotlin.math.min


class LiveChannelPlayerFragment : LiveChannelPlayerBaseFragment<FragmentLiveChannelPlayerBinding>(),
    TtnPlayerListener,
    PlayerListener.TimeChangeListener {

    private val TAG = LiveChannelPlayerFragment::class.java.name
    private var sessionRenewCount = -1
    private var isDeviceLimitReached: Boolean = false
    private var rrmSessionInfo: RrmSessionInfo? = null
    private var isPlayerPaused: Boolean = false
    private var isFavClicked: Boolean = false
    private var playerDurationWatcher: PlayerDurationWatcher? = null
    private var isDialog: Boolean = false
    private var securityLevel : String? = null
    private var isPausedState: Boolean = false

    override fun initializePlayerHelper(savedInstanceState: Bundle?) {
        firstTimeCW = true
        isPlayerEnded = false
        nextEpisodeAvailable = false
        isPlayerStarted = true
        isPausedState = false
        networkErrorView = playerBinding.networkError
        errorView = playerBinding.errorView
        playerBinding.nextEpisodeScreen.clRoot.hide()
        viewModel.timer?.cancel()
        playerBinding.playerView.show()
        registerDisplayListener()
        hideErrorView()
        rrmSessionInfo = viewModel.sharedPrefs.getRrmSessionInfo()
        if (!mIsInFullScreenMode) {
            playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.hide()
            playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
            binding.miniProgressPlayer.show()
            binding.miniProgressPlayer.isEnabled = false
            playerBinding.nextEpisodeScreen.isPortrait = true
        }
        binding.miniProgressPlayer.setPosition(0L)
        playerBinding.playerView.controllerShowTimeoutMs = CONTROLLER_HIDE_TIME_MS.toInt()
        playerBinding.playerView.findViewById<TextView>(R.id.tv_play_next).hide()
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_previous).hide()

        playerModel?.getImage()?.let {
            val url = getCloudinaryUrl(
                viewModel.sharedPrefs.getCloudenieryUrl(),
                getDeviceDimension(requireContext()).x, getDeviceDimension(requireContext()).y,
                it
            )
            imageLoad(playerBinding.playerView.findViewById<ImageView>(R.id.iv_thumbnail), url)
        }
        /*playerModel?.let {
            viewModel.actionCW(
                it.getContentId()!!,
                getContentType(it.getContentType()!!),
                it.getResumeTime().toInt() / 1000,
                it.getTotalDuration().toInt()
            )
        }*/
        setSecurityLevelForEnforceL3()
        createAndPrepareTTNPlayer(savedInstanceState)
        val typeFace =
            Typeface.createFromAsset(
                requireActivity().assets,
                requireActivity().getString(R.string.medium_font)
            )
        val captionStyleCompat =
            CaptionStyleCompat(
                Color.WHITE,
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                Color.BLACK,
                typeFace
            )
        playerBinding.playerView.findViewById<TextView>(R.id.tv_title)?.isSelected = true
        playerBinding.playerView.findViewById<TextView>(R.id.tv_title)?.text = playerModel?.getTitle()
        playerBinding.playerView.subtitleView?.setStyle(captionStyleCompat)
        playerBinding.playerView.subtitleView?.setFixedTextSize(Dimension.SP, 16f)
        playerBinding.playerView.subtitleView?.setApplyEmbeddedStyles(false)

        playerBinding.playerView.findViewById<View>(R.id.llReplay)?.tag = REPLAY_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.llReplay))

        playerBinding.playerView.findViewById<View>(R.id.tv_ffwd)?.tag = FWD_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.tv_ffwd))

        playerBinding.playerView.findViewById<View>(R.id.tv_rew)?.tag = REW_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.tv_rew))

        binding.contentPlaybackBtn.tag = PLAY_TAG

        playerBinding.playerView.findViewById<View>(R.id.tv_video_quality).tag = VIDEO_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.tv_video_quality))

        playerBinding.playerView.findViewById<View>(R.id.tv_video_language).tag = AUDIO_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.tv_video_language))

        playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv).tag = FULLSCREEN_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv))

        playerBinding.playerView.findViewById<CheckBox>(R.id.exo_sound)?.setOnCheckedChangeListener { buttonView, isChecked ->
            onPlayerMuteStateChanged(!isChecked)
        }

        val scaleDetector = ScaleGestureDetector(context,
            MyScaleGestureDetector { scaleFactor ->
                if (scaleFactor > optimumPinchZoomScaleFactor)
                    zoomInPinch()
                else
                    zoomOut()
            }
        )
        playerBinding.playerView.setOnTouchListener { _, event ->
            if (event.pointerCount == 1)
                false
            else scaleDetector.onTouchEvent(event)
        }
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setOnClickListener {
            when (playerBinding.playerView.resizeMode) {
                AspectRatioFrameLayout.RESIZE_MODE_FIT -> zoomIn()
                AspectRatioFrameLayout.RESIZE_MODE_FILL -> zoomOut()
                else -> zoomOut()
            }
        }
    }

    private fun createAndPrepareTTNPlayer(savedInstanceState: Bundle?) {
        firstTimeCW = true
        isPlayerEnded = false
        callProbeEventOnce = true
        ttnPlayerHelper = TtnPlayerHelper.Builder(
            requireContext(),
            playerBinding.playerView,
            playerModel?.getCookies(),
            PROVIDER_CHAUPAL.equals(playerModel?.getProvider(), true),
            false
        )
            .setRepeatModeOn(false)
            .setAutoPlayOn(true)
            .setDrmLicenseUrl(playerModel!!.getDrmLicenseUrl())
            .setControllerTime(CONTROLLER_HIDE_TIME_MS)
            .setMiniProgressBar(binding.miniProgressPlayer)
            .addSavedInstanceState(savedInstanceState)
            .setUiControllersVisibility(true)
            .setVideoUrls(arrayListOf(playerModel!!.getPlaybackUrl()!!))
            .setSubTitlesUrls(playerModel?.getSubtitleUrls())
            .setTtnPlayerEventsListener(this)
            .setOverrideFullScreenButtonFunctionality(false)
            .setOverLayOnVideoPause(true)
            .setResumePosition(playerModel?.getResumeTime() ?: -1)
            .setEnforceL3Settings(playerModel?.enforceL1L3 ?: false)
            .setSecurityLevel(securityLevel)
            .setDrmInfo(playerModel?.getKid(), playerModel?.getToken(), playerModel?.getDrmProxyUrl())
            .enableLiveStreamSupport()
            .enableAddToWatchlist(false)
            .createAndPrepare()
        createTtnPlayerCalled(false)

        ttnPlayerHelper?.getPlayer()?.let{
            playerDurationWatcher = PlayerDurationWatcher(it, this)
            probePlayerEventInitSdk(it,playerModel, ttnPlayerHelper!!.getBandwidthMeter(), sharedPrefs.getOriginalSubscriberId())
        }

        ttnPlayerHelper?.playerPlay()
        ttnPlayerHelper?.disableTimeSeekBarTouch(true)
        trackSelector = ttnPlayerHelper?.getTrackSelector()
        ttnPlayerHelper?.getTrackSelector()?.buildUponParameters()?.let {
            subtitleLang = preferredSubtitleLanguage?:""
            if (preferredSubtitleLanguage != null) {
                subtitle = "on"
                it.setPreferredTextLanguage(preferredSubtitleLanguage)
                it.setRendererDisabled(2, false)
            } else {
                subtitle = "off"
                it.setRendererDisabled(2, true)
            }
            if (preferredAudioLanguage != null) {
                it.setPreferredAudioLanguage(preferredAudioLanguage)
            }
            ttnPlayerHelper?.getTrackSelector()?.parameters = it.build()
        }
        if(!isSoundOn)
            ttnPlayerHelper?.getPlayer()?.volume = 0f
        if(mIsInFullScreenMode){
            ttnPlayerHelper?.setFullScreenFlag(Configuration.ORIENTATION_LANDSCAPE)
        }
        ttnPlayerHelper?.getPlayer()?.let{
            playerDurationWatcher = PlayerDurationWatcher(it, this)
        }
        probePlayerEventPlayClicked()
    }

    private fun setSecurityLevelForEnforceL3() {
        if(securityLevel == null) {
            try {
                securityLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val mediaDrm = MediaDrm(C.WIDEVINE_UUID)
                    mediaDrm.getPropertyString(TtnPlayerHelper.SECURITY_LEVEL)
                } else {
                    SECURITY_LEVEL_L3
                }
            } catch (e : Exception){
                securityLevel = SECURITY_LEVEL_L3
            }
        }
    }

    override fun getPlayerLayoutId(): Int {
        return R.layout.fragment_live_channel_player
    }

    override fun onDrmSessionManagerError(
        error: Exception
    ) {
        e(
            TAG,
            "onDrmSessionManagerError.message : ${error.message}, erroCode ${getErrorCode(error)}"
        )
        activity?.runOnUiThread {
            val msg = error.message ?: ""
            if(msg.contains("Unable to connect")){
                restartPlayerAfterNetworkAvailable()
            }
            else {
                ttnPlayerHelper?.onLoaded(null)
                if(RENTAL.equals(playerModel?.getContractName(), true))
                    isDeviceLimitReached = true
                if (isDeviceLimitReached) {
                    releasePlayer()
                    updateErrorModel(
                        "",
                        getString(R.string.concurrency_error),
                        true,
                        null
                    )
                } else {
                    error.message
                    updateErrorModel(
                        viewModel.VIDEO_UNAVAILABLE_TITLE,
                        parseError(getErrorCode(error)),
                        false,
                        getErrorCode(error).toString()
                    )
                    try {
                        if (sessionRenewCount == 1) {
                            releasePlayer()
                        } else {
                            if (sessionRenewCount < 1) {
                                sessionRenewCount++
                            }
                        }
                    } catch (e: Exception) {
                        e(TAG, " :onDrmSessionManagerError: Exception  catch")

                    }
                }
            }
        }

    }

    override fun toggleWatchlisted() { }

    override fun setObserver() {
        super.setObserver()
        viewModel.getPlayerModel().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                releasePlayer()
                playerModel = it
                initializePlayerHelper(null)
                binding.playerFrame.show()
                d(TAG, "Playback Url: ${it.getPlaybackUrl()}")
                d(TAG, "License Url: ${it.getLA_URL()}")
            }
        })
        viewModel.getIsFavouriteContent().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                var msz:String = ""
                var imgResource:Int
                cancelFavToast()
                val view = DataBindingUtil.inflate<ToastWatchlistBinding>(
                    LayoutInflater.from(context),
                    R.layout.toast_watchlist,
                    null,
                    false
                )

                playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)?.text =
                    getString(
                        R.string.add_to_watchlist
                    )
                if (it) {
                    imgResource = R.drawable.ic_pi_watchlist_selected
                    msz = getString(R.string.added_to_watchlist)
                    view.watchlistIcon.startAvd(true)
                    view.watchlistToastTv.text = getString(R.string.added_to_watchlist)
                    playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)
                        ?.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_pi_watchlist_selected
                            ), null, null, null
                        )
//                    trackOnAddFavorite()
                } else {
                    imgResource = R.drawable.ic_pi_watchlist_unselected
                    msz = getString(R.string.remove_from_watchlist)
                    view.watchlistToastTv.text = getString(R.string.remove_from_watchlist)
                    playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)
                        ?.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_pi_watchlist_unselected
                            ), null, null, null
                        )
//                    trackOnDeleteFavorite()
                }
                showToast(context, msz, imgResource)
            }
        })
        viewModel.forceLogout.observe(viewLifecycleOwner, Observer {
            e("pubnub", "action forceLogout inside TTN")
            forceLogout()
        })

        viewModel.playerProgressListener.observe(viewLifecycleOwner, Observer {
            startProgressing(it)
        })


    }

    private fun startProgressing(shouldProgress: Boolean) {
        playerBinding.playerProgress.startProgressAvd(shouldProgress)
    }

    override fun onResume() {
        super.onResume()
        isDialog = dialog?.isShowing ?: false
        if (isPlayerStarted)
            try {
                isPausedState = false
                if (isExternalDisplayAvailable()) {
                    releasePlayerWithBack()
                } else if (!errorView.root.isVisible && !isDialog && !isPlayerEnded) {
                    if (isAutoPaused) {
                        isAutoPaused = false
                        ttnPlayerHelper?.playerPlay()
//                    ttnPlayerHelper?.onActivityResume()
                        playerDurationWatcher?.stop()
                        playerDurationWatcher = null
                        ttnPlayerHelper?.getPlayer()?.let {
                            playerDurationWatcher = PlayerDurationWatcher(
                                it,
                                this
                            )
                        }
                        if (!isSoundOn)
                            ttnPlayerHelper?.getPlayer()?.volume = 0f
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }


    override fun onPause() {
        isPausedState = true
        super.onPause()
        e(TAG, "inside onPause isPlayerPaused:$isPlayerPaused, " +
                "isPlayerEnded:$isPlayerEnded, " +
                "isAutoPaused:$isAutoPaused")
        if(!isPlayerPaused){
            isAutoPaused = true
            ttnPlayerHelper?.playerPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    /**
     * Used to determine which custom message is used to show to user.
     *
     * @param errorCode
     * @param messageerr
     * @return
     */
    private fun parseError(errorCode: Int, message: String?): String? {
        var message = message
        if (message == null) {
            message = ""
        }
        message = getCustomError(errorCode)
        return if (errorCode == -1 && !isNetworkAvailable) {
            showNetworkAlert()
            null
        } else if (isNetworkAvailable) {
            message
        } else {
            message
        }
    }

    private fun updateErrorModel(
        title: String,
        errorMessage: String?,
        isConcurrency: Boolean = false,
        errorCode: String?
    ) {
        d(TAG, "inside updateErrorModel")
        if(errorView.root.isVisibile()) return
        trackOnPlayerFailure(
            playerModel!!,
            errorMessage?: COMMON_ERROR_TITLE,
            contentItem
        )
        trackOnPlayerError(
            playerModel,
            errorCode.toString(),
            errorMessage?: COMMON_ERROR_TITLE,
            PARA_PI_ERROR_ORIGIN,
            PARA_ERROR_TYPE_PLAYER
        )
        if (errorMessage == null) return

        viewModel.hideLoader()
        onError(ErrorModel(message = errorMessage, title = title, isConcurrency = false))
//        actionCWhandler.removeCallbacks(cwHitRunner)
    }

    override fun onDrmSessionAcquired() {
        d(TAG, "onDrmSessionAcquired")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        d(TAG, "onSaveInstanceState")
        ttnPlayerHelper?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

//    override fun onStart() {
//        super.onStart()
//        d(TAG, "onActivityStart")
//        try {
//            if (!errorView.root.isVisible) {
//                ttnPlayerHelper?.onActivityStart()
//            }
//        } catch (e:Exception){
//            e.printStackTrace()
//        }
//    }

    override fun onLoadingStatusChanged(
        isLoading: Boolean,
        bufferedPosition: Long,
        bufferedPercentage: Int
    ) {
        d(
            TAG,
            "onLoadingStatusChanged, isLoading: " + isLoading +
                    "   Buffered Position: " + bufferedPosition +
                    "   Buffered Percentage: " + bufferedPercentage
        )
        updateSeekButtons()
    }

    private fun updateSeekButtons() {

        playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.hide()
        playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.hide()
    /*if ((ttnPlayerHelper?.duration ?: 0) <= 0) return
        activity?.runOnUiThread {
            val currentTime = ttnPlayerHelper?.currentPosition ?: 0
            val remainingTime = ttnPlayerHelper?.duration?.minus(currentTime) ?: 0
            val remainingTimeVal = Util.getStringForTime(
                formatBuilder,
                Formatter(formatBuilder, Locale.getDefault()),
                remainingTime
            )
            if (!remainingTimeVal.contains("-") && remainingTimeVal.length <= 8) {
                playerBinding.playerView.findViewById<TextView>(R.id.exo_remaining)?.text =
                    remainingTimeVal
                if ((currentTime) > PLAYER_INCREMENT_DECREMENT_MS) {
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.show()
                } else {
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.hide()
                }
                if (remainingTime > PLAYER_INCREMENT_DECREMENT_MS) {
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.show()
                } else {
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.hide()
                }
            }
        }*/
    }

    override fun onPlayerPlaying(currentWindowIndex: Int) {
        e(TAG, "onPlayerPlaying currentWindowIndex : $currentWindowIndex")
        viewModel.hideLoader()
        if(networkErrorView.root.isVisibile())
            hideNetworkView()
        playerBinding.playerView.findViewById<View?>(R.id.iv_thumbnail)?.hide()
        setClickListener(binding.contentPlaybackBtn)
        viewModel.togglePlaybackButton(LiveChannelPlaybackButtonStateEnum.STATE_PAUSE)
        if (firstTimeCW) {
            firstTimeCW = false
//            actionCWhandler.postDelayed(cwHitRunner, 10000)
            intialBufferDuration = (System.currentTimeMillis() - startInitialBufferTime) / 1000
            startTime = getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT)
            firstPlayStartTime = System.currentTimeMillis()
            trackInitialBuffering(
                intialBufferDuration.toString(),
                (intialBufferDuration / 60).toString(),
                playerModel,
                contentItem,
                fetchVideoQualityUsingBitrate(getBitRate(ttnPlayerHelper?.getPlayer())),
                getCurrentSeekBarProgressInPercentage(
                    ttnPlayerHelper?.currentPosition,
                    ttnPlayerHelper?.duration
                )
            )
        }
        playerDurationWatcher?.start()
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.widgetController).show()
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.llReplay).hide()

        if (isPlayerPaused) {
            trackResumeAndInitPlayer(
                playerModel!!,
                contentItem,
                getCurrentSeekBarProgressInPercentage(
                    ttnPlayerHelper?.currentPosition,
                    ttnPlayerHelper?.duration
                )
            )
            isPlayerPaused = false
        }
        updateSeekButtons()
        startPlayTime = System.currentTimeMillis()
    }

    override fun onPlayerPaused(currentWindowIndex: Int) {
        e(TAG, "onPlayerPaused currentWindowIndex : $currentWindowIndex")
        setClickListener(binding.contentPlaybackBtn)
        viewModel.togglePlaybackButton(LiveChannelPlaybackButtonStateEnum.STATE_RESUME)
        lifecycleScope.launchWhenResumed {
            delay(DEFAULT_BUFFER_FOR_PLAYBACK_MS.toLong())
            ttnPlayerHelper?.handleGoLiveButton(true)
        }
        viewModel.hideLoader()
        if (firstTimeCW) {
            ttnPlayerHelper?.playerPlay()
        } else {
            trackOnPause()
        }
        if (startPlayTime != 0L) {
            playDuration += (System.currentTimeMillis() - startPlayTime)
            startPlayTime = 0
        }
        updateSeekButtons()
        isPlayerPaused = true
    }

    override fun onPlayerBuffering(currentWindowIndex: Int) {
        e(TAG, "onPlayerBuffering currentWindowIndex : $currentWindowIndex")
        viewModel.showLoader()
        binding.contentPlaybackBtn.setOnClickListener(null)
        playerBinding.playerView.findViewById<RelativeLayout>(R.id.rl_play_pause)?.invisible()
        if (startPlayTime != 0L) {
            playDuration = playDuration + (System.currentTimeMillis() - startPlayTime)
            startPlayTime = 0
        }
    }

    override fun onBufferStart() {
        startInitialBufferTime = System.currentTimeMillis()
    }

    override fun onBufferEnd() {
        e(TAG, "onBufferEnd isPlayerPaused:$isPlayerPaused, isPausedState:$isPausedState")
        if(dialog?.isShowing == true || isPausedState){
            if(!isPlayerPaused) isAutoPaused = true
            ttnPlayerHelper?.playerPause()
        }
        playerBinding.playerView.findViewById<RelativeLayout>(R.id.rl_play_pause)?.show()
    }

    override fun onPlayerStateEnded(currentWindowIndex: Int) {
        isPlayerPaused = true
        viewModel.hideLoader()
        releasePlayer()
        playerBinding.playerView.findViewById<RelativeLayout>(R.id.rl_play_pause)?.show()
        playerEnded()
    }

    private fun playerEnded() {
        if(isPlayerEnded) return
        if(callProbeEventOnce) {
            callProbeEventOnce = false
            probePlayerEventStopped()
        }
        isPlayerEnded = true
        if (nextEpisodeAvailable) {
            playerBinding.playerView.hide()
            playerBinding.nextEpisodeScreen.clRoot.show()
            viewModel.togglePlaybackButton(LiveChannelPlaybackButtonStateEnum.STATE_PLAY)
            viewModel.timer?.start()
        } else {
            playerBinding.playerView.findViewById<View>(R.id.iv_thumbnail)?.show()
            playerBinding.playerView.findViewById<View>(R.id.llReplay).show()
            binding.contentPlaybackBtn.tag = REPLAY_TAG
            viewModel.togglePlaybackButton(LiveChannelPlaybackButtonStateEnum.STATE_PLAY)
            firstTimeCW = true
        }
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.widgetController).hide()
//        actionCWhandler.removeCallbacks(cwHitRunner)
//        val totalDuration = (playerModel?.getTotalDuration() ?: (ttnPlayerHelper?.duration ?: 1) / 1000).toInt()
//        publishWatchedContent(null, totalDuration, totalDuration, viewModel)
        releasePlayer()
    }

    override fun onPlayerStateIdle(currentWindowIndex: Int) {
        e(TAG, "onPlayerStateIdle currentWindowIndex : $currentWindowIndex")
        viewModel.hideLoader()
        playerBinding.playerView.findViewById<RelativeLayout>(R.id.rl_play_pause)?.show()
    }


    override fun onTTNPlayerError(error: ExoPlaybackException?) {

        error?.let { handlePlayerError(it) }
    }

    private fun handlePlayerError(exception: ExoPlaybackException) {
        e("TTNPlayerFragment","inside handlePlayerError: ${exception.message} type: ${exception.type}")
        var errorMessage = ""
        val msg = exception?.message ?: " "
        if(msg.contains("timeout")){
            releasePlayer()
            showNetworkAlert()
            return
        }
        var errorCode = PLAYER_DEFAULT_ERROR_CODE
        when (exception.type) {
            ExoPlaybackException.TYPE_SOURCE -> {
                errorCode = getErrorCode(exception.sourceException)
                errorMessage = "SOURCE EXCEPTION: " + exception.sourceException.message
                e("TTNPlayerFragment","inside handlePlayerError errorCode: ${errorCode}," +
                        " exception.sourceException.message:${exception.sourceException.message}")
                if(errorCode == 403){
                    updateErrorModel(
                        "",
                        errorMessage,
                        false,
                        errorCode.toString()
                    )
                    return
                }
            }
            ExoPlaybackException.TYPE_RENDERER -> {
                errorCode = getErrorCode(exception.rendererException)
                errorMessage = "RENDERER EXCEPTION: " + exception.rendererException.message
            }
            ExoPlaybackException.TYPE_UNEXPECTED -> errorMessage =
                "UNEXPECTED EXCEPTION: " + exception.unexpectedException.message

            else -> errorMessage = exception.localizedMessage ?: getString(R.string.error_generic)
        }

        updateErrorModel(
            viewModel.VIDEO_UNAVAILABLE_TITLE,
            parseError(errorCode, errorMessage),
            false,
            errorCode.toString()
        )
    }


    override fun createTtnPlayerCalled(isToPrepare: Boolean) {
        // For live content, seek bar will be at current time of live programme
        ttnPlayerHelper?.seekToDefaultPosition()
    }

    override fun releaseTtnPlayerCalled() {
        if(callProbeEventOnce) {
            callProbeEventOnce = false
            probePlayerEventStopped()
        }
        val quality=fetchVideoQualityUsingBitrate(ttnPlayerHelper?.getPlayer()?.videoFormat?.bitrate?:0)
        stopTime = getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT)
        if (startPlayTime != 0L)
            playDuration += (System.currentTimeMillis() - startPlayTime)
        watchedDuration = playDuration
        if(watchedDuration > 1000) {
            trackOnPlayerPlay(playerModel, contentItem,quality)
            trackOnPlayerPlayEnd(
                playerModel,
                contentItem,
                getCurrentSeekBarProgressInPercentage(
                    ttnPlayerHelper?.currentPosition,
                    ttnPlayerHelper?.duration
                )
            )
        }
    }

    override fun onVideoResumeDataLoaded(window: Int, position: Long, isResumeWhenReady: Boolean) {
        binding.contentPlaybackBtn.tag = PLAY_TAG
        d(TAG, "window: $window  position: $position autoPlay: $isResumeWhenReady")
    }

    override fun onPlayBtnTap(): Boolean {
        if (ttnPlayerHelper?.isPlayerCreated == false) {
            ttnPlayerHelper?.createPlayer(true)
        }
        return false
    }

    override fun onPauseBtnTap(): Boolean {
        d(TAG, "onPauseBtnTap")
        return false
    }

    override fun onFullScreenBtnTap() {
        d(TAG, "onFullScreenBtnTap")
    }

    override fun onPlayerUiControlVisibilityChange(visibility: Int) {

        d(TAG, "onPlayerUiControlVisibilityChange : $visibility")
        if (visibility == View.GONE && mIsInFullScreenMode) {
            binding.imgBack.hide()
        } else {
            binding.imgBack.show()
        }
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        if(trackSelector == null)
            trackSelector = ttnPlayerHelper?.getTrackSelector()
        val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
        val trackGroup : TrackGroupArray = mappedTrackInfo?.getTrackGroups(VIDEO_TRACK) ?: trackGroups
        initVideo(trackGroup, trackSelections, (playerModel?.enforceL1L3 == true && SECURITY_LEVEL_L3.equals(securityLevel, true)))
    }

    private fun setClickListener(view: View?) {
        view?.setOnClickListener { view ->
            val `object` = view.tag
            if (`object` is String) {
                val tag = `object`
                if (tag.equals(FWD_TAG, true)) {
                    ttnPlayerHelper?.seekTo(
                        0,
                        min(
                            (ttnPlayerHelper?.currentPosition ?: 0) + PLAYER_INCREMENT_DECREMENT_MS,
                            ttnPlayerHelper?.duration ?: 0
                        )
                    )
                } else if (tag.equals(REW_TAG, true)) {
                    ttnPlayerHelper?.seekTo(
                        0,
                        max(
                            0,
                            (ttnPlayerHelper?.currentPosition ?: 0) - PLAYER_INCREMENT_DECREMENT_MS
                        )
                    )
                } else if (tag.equals(AUDIO_TAG, ignoreCase = true)) {
                    if (trackSelector == null) {
                        trackSelector = ttnPlayerHelper?.getTrackSelector()
                    }
                    openAudioOption()
                } else if (tag.equals(VIDEO_TAG, ignoreCase = true)) {
                    openVideoOption()
                } else if (tag.equals(REPLAY_TAG, ignoreCase = true)) {
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.hide()
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.show()
                    playerBinding.playerView.findViewById<View>(R.id.llReplay).hide()
                    playerBinding.playerView.findViewById<ConstraintLayout>(R.id.widgetController)?.show()
                    trackOnRestart(playerModel)
                    playerModel?.setResumeTime(0L)
                    createAndPrepareTTNPlayer(null)
                } else if (tag.equals(FULLSCREEN_TAG, true)) {
                    changeToLandscapeMode()
                } else if (tag.equals(PLAY_TAG, true)) {
                    isPausedState = false
                    e(TAG,"inside PLAY_TAG : ${ttnPlayerHelper?.isPlaying()}")
                    if (ttnPlayerHelper?.isPlaying() == true) {
                        ttnPlayerHelper?.playerPause()
                    } else {
                        ttnPlayerHelper?.playerPlay()
                    }
                }
            }
        }
    }

    override fun changeToLandscapeMode() {
        super.changeToLandscapeMode()
        zoomOut()
        ttnPlayerHelper?.setFullScreenFlag(Configuration.ORIENTATION_LANDSCAPE)
        binding.miniProgressPlayer.hide()
        playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.show()
        playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
        playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv)?.hide()
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.viewProgress)?.show()
        playerBinding.playerView.findViewById<LinearLayout>(R.id.ll_player_menu)?.show()
        playerBinding.nextEpisodeScreen.isPortrait = false
    }
    override fun changeToTabletPortraitMode() {
        super.changeToTabletPortraitMode()
        ttnPlayerHelper?.setFullScreenFlag(Configuration.ORIENTATION_LANDSCAPE)
        if (isPlayerStarted) {
            binding.miniProgressPlayer.show()
        }
        playerBinding.nextEpisodeScreen.isPortrait = false
        playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.hide()
//        playerBinding.playerView.findViewById<View>(R.id.tv_title)?.hide()
//            playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.invisible()
        playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
        playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv)?.show()
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.viewProgress)?.hide()
        playerBinding.playerView.findViewById<LinearLayout>(R.id.ll_player_menu)?.hide()
        dialog?.cancel()
    }

    override fun changeToPortraitMode() {
        super.changeToPortraitMode()
        zoomIn()
        ttnPlayerHelper?.setFullScreenFlag(Configuration.ORIENTATION_PORTRAIT)
        if (isPlayerStarted) {
            binding.miniProgressPlayer.show()
        }
        playerBinding.nextEpisodeScreen.isPortrait = true
        playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.hide()
//        playerBinding.playerView.findViewById<View>(R.id.tv_title)?.hide()
//            playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.invisible()
        playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
        playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv)?.show()
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.viewProgress)?.hide()
        playerBinding.playerView.findViewById<LinearLayout>(R.id.ll_player_menu)?.hide()
        dialog?.cancel()
    }
    private fun openAudioOption() {
        if (ttnPlayerHelper != null) {
            if (audioLanguage.list == null) {
                val languages = ArrayList<String>()
                languages.addAll(playerModel?.getAudioLanguages() ?: ArrayList())
                audioLanguage.list = languages
            }
            if (subtitleList.list == null) {
                val languages = ArrayList<String>()
                languages.add("None")
                subtitleList.list = languages
            }
            if(PROVIDER_PLANET_MARATHI.equals(playerModel?.getProvider(), true))
                showPopUp(
                    null,
                    null,
                    audioLanguage,
                    subtitleList,
                    true
                )
            else
                showPopUp(
                trackSelector,
                trackSelections,
                audioLanguage,
                subtitleList,
                true
            )
            if (!isPlayerPaused) {
                isAutoPaused = true
                ttnPlayerHelper?.playerPause()
            }

        }
    }

    private fun openVideoOption() {
        if (ttnPlayerHelper != null) {
            val availableVideoQuality: VideoQuality =
                videoQuality ?: VideoQuality()
            val videoQuality: List<Bitrate?>? = availableVideoQuality.getBitrateArrayList()
            if (videoQuality != null && videoQuality.size > 0) {
                if (!isPlayerPaused) {
                    isAutoPaused = true
                    ttnPlayerHelper?.playerPause()
                }
                showVideoQualityPopUp(availableVideoQuality)
            }
        }
    }

    private fun trackOnPause() {
        trackOnPause(
            playerModel!!,
            contentItem,
            getCurrentSeekBarProgressInPercentage(
                ttnPlayerHelper?.currentPosition,
                ttnPlayerHelper?.duration
            )
        )
    }

    override fun onScrubStart(timeBar: TimeBar, position: Long) {
        if(!isPlayerPaused) {
            isAutoPaused = true
            ttnPlayerHelper?.playerPause()
        }
    }

    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
        e("onScrubStop","isPlayerPaused:$isPlayerPaused, isAutoPaused:$isAutoPaused")
        if(isAutoPaused){
            isAutoPaused = false
            ttnPlayerHelper?.playerPlay()
        }
        ttnPlayerHelper?.seekTo(ttnPlayerHelper?.currentWindowIndex ?: 0, position)
//        calculateWatchDurationAndPost(cwHitRunner, position)
    }

    override fun lostConnection() {
        activity?.runOnUiThread {
            if (ttnPlayerHelper?.isBuffering != false) {
                showNetworkAlert()
            }
        }
    }

    override fun restartPlayerAfterNetworkAvailable() {
        activity?.runOnUiThread {
            hideNetworkView()
            if (ttnPlayerHelper?.isBuffering != false && !isPlayerPaused) {
                ttnPlayerHelper?.resumePlayer()
            }
        }
        e("TTNPlayerFragment", "inside restartPlayerAfterNetworkAvailable isPlayerPaused:$isPlayerPaused")
    }

    override fun retryPlayer() {
        activity?.runOnUiThread {
            hideNetworkView()
            ttnPlayerHelper?.resumePlayer()
        }
    }

    override fun changeVideoQuality(bitrate: Bitrate) {
        val mappedTrackInfo = ttnPlayerHelper?.getTrackSelector()?.currentMappedTrackInfo ?: return
        val rendererIndex = VIDEO_TRACK
        var override: DefaultTrackSelector.SelectionOverride? = null
        val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)

        val parametersBuilder = ttnPlayerHelper?.getTrackSelector()?.buildUponParameters()

        if (!bitrate.getName().equals("Auto", true)) {
            for (groupIndex in 0 until trackGroups.length) {
                val group = trackGroups[groupIndex]
                for (trackIndex in 0 until group.length) {
                    val format = group.getFormat(trackIndex)
                    if (mappedTrackInfo.getTrackSupport(
                            rendererIndex,
                            groupIndex,
                            trackIndex
                        ) == RendererCapabilities.FORMAT_HANDLED
                    ) {
                        if (trackIndex == bitrate.getTrackIndex())
                            override = DefaultTrackSelector.SelectionOverride(
                                groupIndex,
                                bitrate.getTrackIndex()
                            )
                    }
                }
            }
        }
        if (parametersBuilder != null) {
            if (override != null) {
                parametersBuilder.clearSelectionOverrides(rendererIndex)
                    .setRendererDisabled( /* rendererIndex= */
                        rendererIndex,
                        false)
                context?.let {
                    ttnPlayerHelper?.getTrackSelector()?.parameters =
                        DefaultTrackSelector.ParametersBuilder(it).build()
                }
                parametersBuilder.setSelectionOverride(rendererIndex, trackGroups, override)
            } else {
                parametersBuilder.clearSelectionOverrides(rendererIndex)
            }
            playerBinding.playerView.useArtwork = true
            playerBinding.playerView.setKeepContentOnPlayerReset(true)
            ttnPlayerHelper?.getTrackSelector()?.setParameters(parametersBuilder)
        }
    }

    override fun audioVideoDialogClosed() {
        isPausedState = false
        if(isDialog) {
            ttnPlayerHelper?.onActivityResume()
            if(!isSoundOn)
                ttnPlayerHelper?.getPlayer()?.volume = 0f
        }
        if (isAutoPaused) {
            isAutoPaused = false
            ttnPlayerHelper?.playerPlay()
        }
    }

    override fun zoomInPinch() {
        playerBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setImageResource(R.drawable.ic_zoom_out)
    }

    override fun zoomIn() {
        playerBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setImageResource(R.drawable.ic_zoom_out)
    }

    override fun zoomOut() {
        playerBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setImageResource(R.drawable.ic_zoom_in)
    }


    override fun calculateWatchDurationAndPost(runnable: Runnable, watchDurationlong: Long) {
        if (ttnPlayerHelper != null) {
//            val watchDuration : Long = (ttnPlayerHelper?.currentPosition ?: 0) / 1000
            //val watchDuration: Int
            if (watchDurationlong == 0L) {
                watchedDuration = ((ttnPlayerHelper?.currentPosition ?: 0) / 1000)
            } else
                watchedDuration = watchDurationlong / 1000

            val totalDuration: Long = (ttnPlayerHelper?.duration ?: 0) / 1000

//            publishWatchedContent(runnable, watchedDuration.toInt(), totalDuration.toInt(), viewModel)
        }
    }

    override fun changeAudioTrack(format: Format?) {
        if (format?.language != null) {
            val parametersBuilder = trackSelector?.buildUponParameters()
            parametersBuilder?.let {
                preferredAudioLanguage = format.language
                it.setPreferredAudioLanguage(format.language!!)
                trackSelector?.setParameters(it)
            }
        }
    }

    override fun changeSubtitle(language: String?) {
        e("changeSubtitle","preferredSubtitleLanguage : $preferredSubtitleLanguage, " +
                "language : $language")
        val parametersBuilder = trackSelector?.buildUponParameters()
        parametersBuilder?.let {
            e("changeSubtitle","isndie parametersBuilder")
            preferredSubtitleLanguage = language
            if (language != null) {
                subtitle = "on"
                subtitleLang = language
                e("changeSubtitle","isndie setPreferredTextLanguage")
                it.setPreferredTextLanguage(language)
                it.setRendererDisabled(2, false)
            } else {
                subtitle = "off"
                subtitleLang = ""
                e("changeSubtitle","isndie not setPreferredTextLanguage")
                it.setRendererDisabled(2, true)
            }
            trackSelector?.setParameters(it)
        }
    }

    private fun navigateToPlayer(playerModel: PlayerModel) {
        this.playerModel = playerModel
        firstTimeCW = true
        trackSelections = null
        trackSelector = null
        audioLanguage = AudioLanguage()
        subtitleList = AudioLanguage()
        videoQuality = null
        isPlayerPaused = false
        releasePlayer()
        initializePlayerHelper(null)
    }

    override fun releasePlayer() {
        e("PlayerBaseFragment","inside releasePlayer")
        playerDurationWatcher?.stop()
        playerDurationWatcher = null
        ttnPlayerHelper?.releasePlayer()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        mapOfVtrTriggerState.clear()
    }

    override fun onTimeChanged(currentTime: Long, totalTime: Long) {
        val bufferedPosition =
            try{
                ttnPlayerHelper?.getPlayer()?.bufferedPosition?:0L
            } catch (e:Exception){
                0L
            }
        updateProgressBar(currentTime, bufferedPosition)
        updateSeekButtons()
        /*if (contentItem.partnerSubscriptionType == null ||
            contentItem.partnerSubscriptionType?.contains(PREMIUM, true) == true)
            handleViewThroughRateEvents(currentTime, totalTime)*/
    }

    private fun updateProgressBar(position: Long, bufferedPosition: Long) {
        binding.miniProgressPlayer.setDuration(ttnPlayerHelper?.duration ?: 0L)
        binding.miniProgressPlayer.setPosition(position)
        binding.miniProgressPlayer.setBufferedPosition(bufferedPosition)
    }

    override fun resumePlayerAfterError() {
        activity?.runOnUiThread {
            if (isPlayerEnded) {
                activity?.onBackPressed()
                /*isPlayerEnded = false
                playerBinding.nextEpisodeScreen.clRoot.hide()
                playerBinding.playerView.show()
                iv_thumbnail.show()
                player_view.findViewById<ConstraintLayout>(R.id.llReplay).show()
                firstTimeCW = true*/
            } else {
                isAutoPaused = false
                ttnPlayerHelper?.resumePlayer()
            }
        }
    }

    override fun onPlayerMuteStateChanged(isMuted: Boolean) {
        if (!isMuted) {
            isSoundOn = true
            ttnPlayerHelper?.getPlayer()?.volume = defaultVolume
        } else {
            isSoundOn = false
            defaultVolume = ttnPlayerHelper?.getPlayer()?.volume ?: 0f
            ttnPlayerHelper?.getPlayer()?.volume = 0f
        }
    }

    override fun onPlayerResponse(response: Response) {
        e("TTNPlayerFragment", "inside onPlayerResponse $response")
        if (response.code != 200) {
            if (response.code == ERROR_CODE_CONCURRENCY) {
                updateErrorModel(
                    "",
                    getString(R.string.concurrency_error),
                    true,
                    response.code.toString()
                )
                return
            } else {
                updateErrorModel(
                    viewModel.VIDEO_UNAVAILABLE_TITLE,
                    "license error",
                    false,
                    response.code.toString()
                )
            }
        }
    }
}
