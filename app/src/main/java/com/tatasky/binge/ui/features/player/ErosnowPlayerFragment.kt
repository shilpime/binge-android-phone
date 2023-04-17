package com.tatasky.binge.ui.features.player

import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.erosnow.partner.ENSDK
import com.erosnow.partner.`interface`.ErosPlayActionListener
import com.erosnow.partner.model.ENError
import com.erosnow.partner.model.ENPlaybackAssetInfo
import com.erosnow.partner.model.PlayerData
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.text.CaptionStyleCompat
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.text.TextOutput
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.tatasky.binge.R
import com.tatasky.binge.analytics.ANALYTICS_TIME_FORMAT
import com.tatasky.binge.analytics.PARA_PI_ERROR_ORIGIN
import com.tatasky.binge.analytics.PARA_ERROR_TYPE_PLAYER
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.databinding.FragmentErosnowPlayerBinding
import com.tatasky.binge.databinding.ToastWatchlistBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.shemaroo.helper.ShemarooHelper
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.player.listeners.PlayerDurationWatcher
import com.tatasky.binge.ui.features.player.listeners.PlayerListener
import com.tatasky.binge.ui.features.player.model.AudioLanguage
import com.tatasky.binge.ui.features.player.model.Bitrate
import com.tatasky.binge.ui.features.player.model.VideoQuality
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.PlayerUtils.getCurrentSeekBarProgressInPercentage
import com.tatasky.binge.voot.model.VootRequest
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

class ErosnowPlayerFragment: PlayerBaseFragment<FragmentErosnowPlayerBinding>(),
    PlayerListener.TimeChangeListener, Player.EventListener, TextOutput,
    PlaybackPreparer {

    private var isPausedState: Boolean = false
    private var mInitialized: Boolean = false
    private var isPlayerPaused: Boolean = false
    private val TAG = ErosnowPlayerFragment::class.java.name
    private var playerDurationWatcher: PlayerDurationWatcher? = null
    private var mPreRollURI: Uri? = null
    private var resumeDuration: Int = 0
    private var mediaSource : MergingMediaSource? = null
    /**
     * DefaultTrackSelector parameters to track current playing parameters
     *
     * */
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var mPlayerView: PlayerView? = null
    var mPlayer: SimpleExoPlayer? = null
    private var mTempCurrentVolume = 0f
    private var playerData: PlayerData? = null

    override fun zoomIn() {
        playerBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setImageResource(R.drawable.ic_zoom_out)
    }

    override fun zoomOut() {
        playerBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setImageResource(R.drawable.ic_zoom_in)
    }


    override fun setObserver() {
        super.setObserver()
        viewModel.getLastWatchResponse().observe(viewLifecycleOwner, Observer { it ->
            e("fetchLastWatch","inside getLastWatchResponse")
            handlePrimaryButtonText()
            updateWatchlist(it.getContentIfNotHandled()?.data?.favourite)
            playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)?.text =
                getString(
                    R.string.add_to_watchlist
                )
            if (it.getContentIfNotHandled()?.data?.favourite == true) {
                playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)
                    ?.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pi_watchlist_selected
                        ), null, null, null
                    )
            } else {
                playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)
                    ?.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pi_watchlist_unselected
                        ), null, null, null
                    )

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
                updateWatchlist(it)
                playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)?.text =
                    getString(
                        R.string.add_to_watchlist
                    )
                if (it) {
                    imgResource=R.drawable.ic_pi_watchlist_selected
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

                    binding.trailerView.setWatchlisted(true)
                    trackOnAddFavorite()

                } else {
                    msz = getString(R.string.remove_from_watchlist)
                    imgResource=R.drawable.ic_pi_watchlist_unselected
                    view.watchlistToastTv.text = getString(R.string.remove_from_watchlist)
                    playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)
                        ?.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_pi_watchlist_unselected
                            ), null, null, null
                        )

                    binding.trailerView.setWatchlisted(false)
                    trackOnDeleteFavorite()
                }
                showToast(requireContext(), msz, imgResource)
            }
        })
        viewModel.forceLogout.observe(viewLifecycleOwner, Observer {
            e("pubnub", "action forceLogout inside TTN")
            forceLogout()
        })
        viewModel.getNextEpisodePlayItem().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                playEpisode(it)
            }
        })
        viewModel.getPlayerModel().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                releasePlayer()
                playerModel = it
                initializePlayerHelper(null)
                binding.playerFrame.show()
            }
        })
        viewModel.getNextPreviousEpisodeDetails().observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.data?.let { episodeDetails ->
                if (episodeDetails.nextEpisodeExists && episodeDetails.nextEpisode != null) {
                    nextEpisodeAvailable = true
                    setTimer(episodeDetails.nextEpisode!!)
                    val url = getCloudinaryUrl(
                        viewModel.sharedPrefs.getCloudenieryUrl(),
                        getDeviceDimension(requireContext()).x,
                        getDeviceDimension(requireContext()).y,
                        episodeDetails.nextEpisode!!.getImageItem()
                    )
                    playerBinding.nextEpisode = episodeDetails.nextEpisode
                    imageLoad(playerBinding.nextEpisodeScreen.ivPoster, url)
                    playerBinding.playerView.findViewById<TextView>(R.id.tv_play_next).show()
                    playerBinding.playerView.findViewById<TextView>(R.id.tv_play_next)
                        .setOnClickListener { playEpisode(episodeDetails.nextEpisode!!) }
                    playerBinding.nextEpisodeScreen.btnPlay.setOnClickListener {
                        playEpisode(
                            episodeDetails.nextEpisode!!
                        )
                    }
                }
                if (episodeDetails.previousEpisodeExists && episodeDetails.previousEpisode != null) {
                    playerBinding.playerView.findViewById<ImageView>(R.id.iv_previous).show()
                    playerBinding.playerView.findViewById<ImageView>(R.id.iv_previous)
                        .setOnClickListener {
                            playEpisode(episodeDetails.previousEpisode!!)
                        }
                }
            }
        })

        viewModel.playerProgressListener.observe(viewLifecycleOwner, Observer {
            startProgressing(it)
        })


    }
    override fun getPlayerLayoutId(): Int {
        return R.layout.fragment_erosnow_player
    }

    override fun lostConnection() {
        if (isBuffering) {
            activity?.runOnUiThread {
                showNetworkAlert()
            }
        }
    }

    override fun restartPlayerAfterNetworkAvailable() {
        activity?.runOnUiThread {
            hideNetworkView()
            val isDialog = dialog?.isShowing ?: false
            if (!mInitialized) {
                initializePlayer()
            } else if (!isAutoPaused
                && !isDialog && isBuffering
            ) {
                mPlayer?.playWhenReady = true
                mediaSource?.let {
                    mPlayer?.prepare(it, false, false);
                }
//                mPlayer?.retry()
            }
        }
    }

    override fun calculateWatchDurationAndPost(runnable: Runnable, watchDurationlong: Long) {
        if (mPlayer != null) {
//            val watchDuration : Long = (mPlayer?.currentPosition ?: 0) / 1000
            val watchDuration: Int
            if (watchDurationlong == 0L) {
                watchDuration = ((mPlayer?.currentPosition ?: 0) / 1000).toInt()
            } else
                watchDuration = watchDurationlong.toInt() / 1000

            val totalDuration: Long = (mPlayer?.duration ?: 0) / 1000

            publishWatchedContent(runnable, watchDuration.toInt(), totalDuration.toInt(), viewModel)
        }
    }

    override fun retryPlayer() {
        activity?.runOnUiThread {
            hideNetworkView()
            mPlayer?.playWhenReady = true
            mediaSource?.let {
                mPlayer?.prepare(it, false, false);
            }
            //mPlayer?.retry()
        }
    }

    val erosInitializeListener = object  : ErosPlayActionListener{

        /*Content Load Error*/
        override fun onError(error: ENError) {
            e(TAG, "inside onError onloading cotnent error: $error")
            mInitialized = false
            /*if("1003".equals(error.error_code)){ // concurrency error
                context?.let{
                    updateErrorModel(
                        "",
                        getString(R.string.concurrency_error),
                        true,
                        error.error_code
                    )
                }
            }
            else*/
            updateErrorModel("", error.message, false, error.error_code)
//            onError(ErrorModel())
        }

        /*Success on content load*/
        override fun onSuccess(data: PlayerData) {
            e("ENSDK", "playerData:$data,")
            mInitialized = true
            if(mPlayer == null) return
            playerData = data
            playPlayerContent()
        }
    }

    private fun playPlayerContent() {
        playerData?.let {
            ENSDK.initializePlayer(
                ENPlaybackAssetInfo(
                    it.stream_url,
                    it.asset_id,
                    it.asset_title,
                    it.content_id
                )
                , mPlayer!!
            )


            var concatenatingMediaSource: ConcatenatingMediaSource? = null
            val dataSourceFactory = buildDataSourceFactory(requireContext())

            mPreRollURI =
                Uri.parse("https://originalvideohls-a.erosnow.com/hls/original/1/1056821/original/6900474/1056821_6900474_latest_IPAD_ALL_multi.m3u8")
            val preRollMediaSource =
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mPreRollURI!!)
            val uri = Uri.parse(it.stream_url)
            val contentMediaSource =
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)

            var araSubTitleSource: MediaSource? = null
            var engSubTitleSource: MediaSource? = null

            if(!isSoundOn) {
                mPlayer?.volume = 0f
            }
            if(it.subtitles != null) {
                if(it.subtitles.eng != null) {
                    engSubTitleSource = SingleSampleMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(
                            Uri.parse(it.subtitles.eng),
                            Format.createTextSampleFormat(
                                null,
                                MimeTypes.TEXT_VTT,
                                Format.NO_VALUE,
                                "en"
                            ),
                            C.TIME_UNSET
                        )
                }
                if(it.subtitles.ara != null) {
                    araSubTitleSource = SingleSampleMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(
                            Uri.parse(it.subtitles.ara ?: it.subtitles?.eng),
                            Format.createTextSampleFormat(
                                null,
                                MimeTypes.TEXT_VTT,
                                Format.NO_VALUE,
                                "ar"
                            ),
                            C.TIME_UNSET
                        )
                }
//                concatenatingMediaSource =
//                    ConcatenatingMediaSource(preRollMediaSource, mergingMediaSource)
            }
            if(engSubTitleSource != null && araSubTitleSource != null){
                mediaSource = MergingMediaSource(contentMediaSource, engSubTitleSource, araSubTitleSource)
            }
            else if(engSubTitleSource != null){
                mediaSource = MergingMediaSource(contentMediaSource, engSubTitleSource)
            }
            else if(araSubTitleSource != null){
                mediaSource = MergingMediaSource(contentMediaSource, araSubTitleSource)
            }
            else
                mediaSource = MergingMediaSource(contentMediaSource)

            mediaSource?.let {
                mPlayer?.prepare(it)
            }
            mPlayer?.playWhenReady = true
        }
    }

    fun createPlayer() {
        if (mPlayer != null) {
            return
        }
        resumeDuration = (playerModel?.getResumeTime() ?: 0L).toInt()
        startProgressing(true)
        //load control
        val builder = DefaultLoadControl.Builder()
        builder.setAllocator(DefaultAllocator(true, 2 * 1024 * 1024))
        builder.setBufferDurationsMs(
            30000, 120000,
            15000, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
        )
        builder.setPrioritizeTimeOverSizeThresholds(false)
        val mLoadControl: DefaultLoadControl = builder.createDefaultLoadControl()
        if (trackSelectorParameters == null) {
            trackSelectorParameters = DefaultTrackSelector.ParametersBuilder(requireContext()).build()
        }
        val trackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(
            AdaptiveTrackSelection.DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS,
            AdaptiveTrackSelection.DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
            AdaptiveTrackSelection.DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS,
            AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION
        )
        trackSelector = DefaultTrackSelector(requireContext(), trackSelectionFactory)
        trackSelector?.parameters = trackSelectorParameters!!
        mPlayer = SimpleExoPlayer.Builder(requireContext())
            .setLoadControl(mLoadControl).setTrackSelector(trackSelector!!).build()
        mPlayer?.setHandleWakeLock(true)
        mPlayer?.setHandleAudioBecomingNoisy(true)
        //for handling audio focus
        val audioAttribute =
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MOVIE)
                .build()
        mPlayer?.setAudioAttributes(audioAttribute, true)
        playerBinding.playerView.requestFocus()
        playerBinding.playerView.player = mPlayer
        playerBinding.playerView.controllerShowTimeoutMs = 3000
        playerBinding.playerView.controllerAutoShow = false
        playerBinding.playerView.controllerHideOnTouch = true
        mTempCurrentVolume = mPlayer?.volume!!
        val parametersBuilder = trackSelector?.buildUponParameters()
        parametersBuilder?.let {
            if (preferredSubtitleLanguage != null) {
                it.setPreferredTextLanguage(preferredSubtitleLanguage)
                it.setRendererDisabled(2, false)
            } else {
                it.setRendererDisabled(2, true)
            }
            if (preferredAudioLanguage != null) {
                it.setPreferredAudioLanguage(preferredAudioLanguage)
            }
            trackSelector?.setParameters(it)
        }
        mPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        mPlayer?.playWhenReady = true
        e(
            TAG,
            "inside createandplay"
        )
        mPlayer?.addListener(this)
        mPlayer?.addTextOutput(this)
        mPlayer?.addAnalyticsListener(EventLogger(trackSelector))
        playerBinding.playerView.setPlaybackPreparer(this)
        (playerBinding.playerView.videoSurfaceView as SurfaceView).setSecure(true)
    }

    override fun initializePlayerHelper(savedInstanceState: Bundle?) {
        firstTimeCW = true
        isAutoPaused = false
        nextEpisodeAvailable = false
        isPlayerStarted = true
        isPausedState = false
        networkErrorView = playerBinding.networkError
        errorView = playerBinding.errorView
        playerBinding.nextEpisodeScreen.clRoot.hide()
        viewModel.timer?.cancel()
        playerBinding.playerView.show()
        mPlayerView = playerBinding.playerView

        hideErrorView()
        hideNetworkView()
        initializePlayer()
        if (!mIsInFullScreenMode) {
            playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.hide()
//            playerBinding.playerView.findViewById<View>(R.id.tv_title)?.hide()
//            playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.invisible()
            playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
            binding.miniProgressPlayer.show()
            playerBinding.nextEpisodeScreen.isPortrait = true
        }
        binding.miniProgressPlayer.setPosition(0L)
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
        playerModel?.let {
            viewModel.actionCW(
                it.getContentId()!!,
                getContentType(it.getContentType()!!),
                it.getResumeTime().toInt() / 1000,
                it.getTotalDuration().toInt()
            )
        }

        trackOnThirdPartyPlayer(playerModel)

        viewModel.fetchNextAndPreviousEpisode(playerModel?.getVodId()?:playerModel!!.getContentId()!!)
        //create instance of mPlayer

        mPlayer?.let {
            playerDurationWatcher = PlayerDurationWatcher(it, this)
        }

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

        binding.btnPlayerController.tag = PLAY_TAG

        playerBinding.playerView.findViewById<View>(R.id.tv_video_quality).tag = VIDEO_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.tv_video_quality))

        playerBinding.playerView.findViewById<View>(R.id.tv_video_language).tag = AUDIO_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.tv_video_language))

        playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv).tag = FULLSCREEN_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv))

        playerBinding.playerView.findViewById<CheckBox>(R.id.exo_sound)?.setOnCheckedChangeListener { buttonView, isChecked ->
            onPlayerMuteStateChanged(!isChecked)
        }
        playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)?.tag =
            WATCHLIST_TAG
        setClickListener(playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist))

        playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv)?.tag = FULLSCREEN_TAG
        setClickListener(playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv))


        playerBinding.playerView.findViewById<DefaultTimeBar>(R.id.exo_progress)
            ?.addListener(miniTimeBarListener)
        binding.miniProgressPlayer.addListener(miniTimeBarListener)

        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setOnClickListener {
            when (playerBinding.playerView.resizeMode) {
                AspectRatioFrameLayout.RESIZE_MODE_FIT -> zoomIn()
                AspectRatioFrameLayout.RESIZE_MODE_FILL -> zoomOut()
            }
        }
    }

    private fun initializePlayer() {
        try {
            createPlayer()
            ENSDK.contentProfile(
                requireContext(),
//                "7004461",
                playerModel?.getProviderContentId() ?: "7004586",//6675433
                erosInitializeListener
            )
        } catch (e: Exception) {
            d(TAG, e.printStackTrace().toString())
            d(TAG, e.message!!)
        }
    }


    private fun setClickListener(view: View?) {
        view?.setOnClickListener { view ->
            val `object` = view.tag
            if (`object` is String) {
                val tag = `object`
                if (tag.equals(FWD_TAG, true)) {
                    mPlayer?.seekTo(
                        0,
                        min(
                            (mPlayer?.currentPosition ?: 0) + PLAYER_INCREMENT_DECREMENT_MS,
                            mPlayer?.duration ?: 0
                        )
                    )
                } else if (tag.equals(REW_TAG, true)) {
                    mPlayer?.seekTo(
                        0,
                        max(
                            0,
                            (mPlayer?.currentPosition ?: 0) - PLAYER_INCREMENT_DECREMENT_MS
                        )
                    )
                } else if (tag.equals(AUDIO_TAG, ignoreCase = true)) {
                    openAudioOption()
                } else if (tag.equals(VIDEO_TAG, ignoreCase = true)) {
                    openVideoOption()
                } else if (tag.equals(REPLAY_TAG, ignoreCase = true)) {
                    if (!isContentPlayable(playerModel?.getPartnerSubType())) {
                        showContentPlaybackDialog()
                        return@setOnClickListener
                    }
                    trackOnRestart(playerModel)
                    playerModel?.setResumeTime(0L)

                    /*initializePlayerHelper(null)*/
                    playPlayerContent()
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.hide()
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.show()
                    playerBinding.playerView.findViewById<View>(R.id.llReplay).hide()

                }
                else if (tag.equals(PLAY_TAG, true)) {
                    isAutoPaused = false
                    isPausedState = false
                    togglePlayPause()
                }
                else if (tag.equals(WATCHLIST_TAG, ignoreCase = true)) {
                    viewModel.markFavourite(parentId, getContentType(parentContentType), true)
                }
                else if (tag.equals(FULLSCREEN_TAG, true)) {
                    changeToLandscapeMode()
                }
            }
        }
    }

    override fun audioVideoDialogClosed() {
        isPausedState = false
        if (isAutoPaused) {
            isAutoPaused = false
            mPlayer?.playWhenReady = true
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
        val parametersBuilder = trackSelector?.buildUponParameters()
        parametersBuilder?.let {
            preferredSubtitleLanguage = language
            if (language != null) {
                it.setPreferredTextLanguage(language)
                it.setRendererDisabled(2, false)
            } else {
                it.setRendererDisabled(2, true)
            }
            trackSelector?.setParameters(it)
        }
    }

    override fun changeVideoQuality(bitrate: Bitrate) {
        val mappedTrackInfo = trackSelector?.currentMappedTrackInfo ?: return
        val rendererIndex = VIDEO_TRACK
        var override: DefaultTrackSelector.SelectionOverride? = null
        val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)

        val parametersBuilder = trackSelector?.buildUponParameters()

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
                trackSelector?.parameters =
                    DefaultTrackSelector.ParametersBuilder().build()
                parametersBuilder.setSelectionOverride(rendererIndex, trackGroups, override)
            } else {
                parametersBuilder.clearSelectionOverrides(rendererIndex)
            }
            playerBinding.playerView.useArtwork = true
            playerBinding.playerView.setKeepContentOnPlayerReset(true)
            trackSelector?.setParameters(parametersBuilder)
        }
    }

    override fun resumePlayerAfterError() {
        activity?.runOnUiThread {
            if (isPlayerEnded) {
                activity?.onBackPressed()
            } else {
                isAutoPaused = false
                mediaSource?.let {
                    mPlayer?.prepare(it, false, false);
                }
//                mPlayer?.retry()
            }
        }
    }

    override fun releasePlayer() {
        val quality=fetchVideoQualityUsingBitrate(getBitRate(mPlayer))
        e("Laksh",quality.toString())

        playerDurationWatcher?.stop()
        playerDurationWatcher = null
        stopTime = getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT)
        if (startPlayTime != 0L)
            playDuration += (System.currentTimeMillis() - startPlayTime)
        watchedDuration = playDuration //System.currentTimeMillis() - firstPlayStartTime

        mPlayer?.release()

        d(TAG, "releaseExoPlayerCalled : watchedDuration : $watchedDuration, " +
                "System.currentTimeMillis() - watchedDuration: ${System.currentTimeMillis() - watchedDuration}")
        if(watchedDuration > 1000) {
            trackOnPlayerPlay(playerModel, contentItem,quality)
            trackOnPlayerPlayEnd(
                playerModel,
                contentItem,
                getCurrentSeekBarProgressInPercentage(
                    mPlayer?.currentPosition,
                    mPlayer?.duration
                )
            )
        }
        mPlayer = null
        mapOfVtrTriggerState.clear()
    }

    override fun onPlayerMuteStateChanged(isMuted: Boolean) {
        if (!isMuted) {
            isSoundOn = true
            mPlayer?.volume = defaultVolume
        } else {
            isSoundOn = false
            defaultVolume = mPlayer?.volume ?: 0f
            mPlayer?.volume = 0f
        }
    }

    override fun onTimeChanged(currentTime: Long, totalTime: Long) {
        val bufferedPosition = if (mPlayer == null) 0L else mPlayer!!.bufferedPosition
        updateProgressBar(currentTime, bufferedPosition)
        updateSeekButtons()
        if (contentItem.partnerSubscriptionType == null ||
            contentItem.partnerSubscriptionType?.contains(PREMIUM, true) == true)
            handleViewThroughRateEvents(currentTime, totalTime)
    }

    private fun updateProgressBar(position: Long, bufferedPosition: Long) {
        binding.miniProgressPlayer.setDuration(mPlayer?.duration ?: 0L)
        binding.miniProgressPlayer.setPosition(position)
        binding.miniProgressPlayer.setBufferedPosition(bufferedPosition)
    }

    private fun updateSeekButtons() {
        if ((mPlayer?.duration ?: 0) <= 0) return
        activity?.runOnUiThread {
            val currentTime = mPlayer?.currentPosition ?: 0
            val remainingTime = mPlayer?.duration?.minus(currentTime) ?: 0
            val remainingTimeVal = Util.getStringForTime(
                formatBuilder,
                Formatter(formatBuilder, Locale.getDefault()),
                remainingTime
            )
            playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.show()
            if (!remainingTimeVal.contains("-") && remainingTimeVal.length <= 8) {
                playerBinding.playerView.findViewById<TextView>(R.id.exo_remaining)?.text = remainingTimeVal
                if ((currentTime) > PLAYER_INCREMENT_DECREMENT_MS) {
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.show()
                } else {
                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.hide()
                }
            }
        }
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        this.trackSelections = trackSelections
        //initVideo(trackGroups, trackSelections)
    }


    override fun onPlayerStateChanged(it: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(it, playbackState)
        e(TAG, "onPlayerStateChanged it : $playbackState, isAutoPaused:$isAutoPaused")
        when(playbackState) {
            Player.STATE_IDLE ->
                isBuffering = true
            Player.STATE_BUFFERING -> {
                if (startPlayTime != 0L) {
                    playDuration += (System.currentTimeMillis() - startPlayTime)
                    startPlayTime = 0
                }
                hideErrorMessage()
                isBuffering = true
                startInitialBufferTime = System.currentTimeMillis()
                e(TAG, "inside Buffering")
                startProgressing(true)
                playerBinding.playerView.findViewById<ConstraintLayout>(R.id.llReplay).hide()
                binding.btnPlayerController.setOnClickListener(null)
//                playerBinding.tvErrorMessage.hide()
            }
            Player.STATE_ENDED -> {
                playerEnded()
            }

            Player.STATE_READY -> {
                isBuffering = false
                startProgressing(false)
                setClickListener(binding.btnPlayerController)

                if(networkErrorView.root.isVisibile())
                    hideNetworkView()
                playerBinding.playerView.findViewById<View?>(R.id.iv_thumbnail)?.hide()
                playerBinding.playerView.findViewById<RelativeLayout>(R.id.rl_play_pause)
                    .show()
                playerBinding.playerView.findViewById<ConstraintLayout>(R.id.llReplay).hide()
                playerDurationWatcher?.start()
                playerBinding.playerView.findViewById<ConstraintLayout>(R.id.widgetController).show()
                playerBinding.playerView.findViewById<ConstraintLayout>(R.id.llReplay).hide()

                if (it) {
                    startPlayTime = System.currentTimeMillis()
                    mPlayerView?.controllerAutoShow = false
                    mPlayerView?.controllerHideOnTouch = true
                    mPlayerView?.hideController()
                    onPlayerPlaying()
                    if ((isPausedState)
                        || (dialog?.isShowing == true && !isAutoPaused)) {
                        isAutoPaused = true
                        mPlayer?.let { it.playWhenReady = false }
                    }
                } else {
                    isPlayerPaused = true
                    mPlayerView?.controllerAutoShow = true
                    mPlayerView?.controllerHideOnTouch = false
                    onPlayerPaused()
                    if (startPlayTime != 0L) {
                        playDuration += (System.currentTimeMillis() - startPlayTime)
                        startPlayTime = 0
                    }
                }
            }
        }
    }

    private fun startProgressing(shouldProgress: Boolean) {
        playerBinding.playerProgress.startProgressAvd(shouldProgress)
        if (shouldProgress)
            playerBinding.playerView.findViewById<RelativeLayout>(R.id.rl_play_pause)
                ?.invisible()
        else
            playerBinding.playerView.findViewById<RelativeLayout>(R.id.rl_play_pause)
                ?.show()
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        e(TAG, "inside onPlayerError $error")
//        super.onPlayerError(error)
        handlePlayerError(error)
    }

    private fun handlePlayerError(exception: ExoPlaybackException?) {
        e(TAG,"inside handlePlayerError: ${exception?.message} type: ${exception?.type}")
        var errorMessage = ""
        val msg = exception?.message ?: " "
        if(msg.contains("timeout") || msg.contains(ERROR_MSG_NETWORK)){
            releasePlayer()
            showNetworkAlert()
            return
        }
        var errorCode = PLAYER_DEFAULT_ERROR_CODE
        when (exception!!.type) {
            ExoPlaybackException.TYPE_SOURCE -> {
                errorCode = getErrorCode(exception.sourceException)
                errorMessage = "SOURCE EXCEPTION: " + exception.sourceException.message
                e(TAG,"inside handlePlayerError errorCode: $errorCode,  errorMessage: $errorMessage")
//                if(errorCode == 403)
//                    updateErrorModel("","Something went wrong, please try again later",true)
            }
            ExoPlaybackException.TYPE_RENDERER -> {
                errorCode = getErrorCode(exception.rendererException)
                errorMessage = "RENDERER EXCEPTION: " + exception.rendererException.message
            }
            ExoPlaybackException.TYPE_UNEXPECTED -> {
                errorCode = getErrorCode(exception.unexpectedException)
                errorMessage = "UNEXPECTED EXCEPTION: " + exception.unexpectedException.message
            }

            else -> errorMessage = getString(R.string.error_generic)
        }
        updateErrorModel("", parseError(errorCode), false, errorCode.toString())
    }

    private fun updateErrorModel(
        title: String,
        errorMessage: String?,
        isConcurrency: Boolean,
        errorCode: String
    ) {
        d(TAG, "inside updateErrorModel")
        if(errorView.root.isVisibile()) return
        if (errorMessage == null) return
        trackOnPlayerFailure(
            playerModel!!,
            if(isConcurrency) errorMessage else getString(R.string.concurrency_error),
            contentItem
        )
        trackOnPlayerError(playerModel,
            errorCode,
            if(isConcurrency) errorMessage else getString(R.string.concurrency_error),
            PARA_PI_ERROR_ORIGIN,
            PARA_ERROR_TYPE_PLAYER
        )
        viewModel.hideLoader()
        onError(ErrorModel(message = errorMessage, title = title, isConcurrency = isConcurrency))
        actionCWhandler.removeCallbacks(cwHitRunner)
    }
    override fun onCues(cues: MutableList<Cue>) {

    }

    override fun preparePlayback() {
        d(TAG, "inside preparePlayback")
        hideErrorMessage()
        mediaSource?.let {
            mPlayer?.prepare(it, false, false);
        }
//        mPlayer?.retry()
    }

    private fun hideErrorMessage() {
        errorView.root.hide()
    }
    override fun onPause() {
        isPausedState = true
        if (isPlayerStarted && mPlayer?.isPlaying == true) {
            isAutoPaused = true
            mPlayer?.let { it.playWhenReady = false }
            //trackOnPause(playerModel!!)
        }
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.timer?.cancel()
        e(TAG, "inside destroyView : ${playerModel?.getTitle()}")
        releasePlayer()
    }
    override fun onResume() {
        super.onResume()
        if (isPlayerStarted)
            try {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                if(isExternalDisplayAvailable()){
                    releasePlayerWithBack()
                }
                else if (!errorView.root.isVisible) {
                    isPausedState = false
                    val isDialog = dialog?.isShowing ?: false
                    if (isAutoPaused && !isDialog && mPlayer != null && mPlayer?.isPlaying == false) {
                        isAutoPaused = false
                        mPlayer?.let { it.playWhenReady = true }
//                        trackResumeAndInitPlayer(playerModel!!)
                    }
                }
                if(!isSoundOn)
                    mPlayer?.volume = 0f
            } catch (e:Exception){
                e.printStackTrace()
            }
    }
    override fun changeToLandscapeMode() {
        super.changeToLandscapeMode()
        playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv)?.hide()
        binding.miniProgressPlayer.hide()
        playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.show()
        playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.viewProgress)?.show()
        playerBinding.nextEpisodeScreen.isPortrait = false
        playerBinding.playerView.findViewById<LinearLayout>(R.id.ll_player_menu)?.show()
        playerBinding.executePendingBindings()
    }

    override fun changeToPortraitMode() {
        super.changeToPortraitMode()
        playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv)?.show()
        if (isPlayerStarted) {
            binding.miniProgressPlayer.show()
        }
        playerBinding.nextEpisodeScreen.isPortrait = true
        playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.hide()
//        playerBinding.playerView.findViewById<View>(R.id.tv_title)?.hide()
//            playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.invisible()
            playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.viewProgress)?.hide()
        playerBinding.playerView.findViewById<LinearLayout>(R.id.ll_player_menu)?.hide()
        playerBinding.executePendingBindings()
        dialog?.cancel()
    }
    private fun togglePlayPause() {
        mPlayer?.let {
            it.playWhenReady = it.isPlaying != true
            if (!it.isPlaying) {
                binding.btnPlayerController.setText(getString(R.string.resume), null)
            } else {
                binding.btnPlayerController.setText(getString(R.string.pause), null)
            }
        }
    }

    private val miniTimeBarListener = object :
        TimeBar.OnScrubListener {
        override fun onScrubMove(timeBar: TimeBar, position: Long) {
            // Percentage of Progress of width
            updateCurrentDragPosition(position)
        }

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
            if (mPlayer?.isPlaying == true || isBuffering) {
                isAutoPaused = true
                mPlayer?.playWhenReady = false
            }
            mPlayerView?.showController()
            playerBinding.playerView.show()
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            playerBinding.playerView.findViewById<TextView>(R.id.exo_position)?.hide()
            /*if (!isNetworkConnected(requireContext())) {
                isNetworkAvailable = false
                if (mPlayer?.isPlaying == true) {
                    isAutoPaused = true
                    togglePlayPause()
                }
                showNetworkAlert()
            } else {*/
            if (isAutoPaused)
                mPlayer?.playWhenReady = true
            isAutoPaused = false
            mPlayer?.seekTo(position)
            playerBinding.playerView.findViewById<DefaultTimeBar>(R.id.exo_progress)
                ?.setPosition(position)
            binding.miniProgressPlayer.setPosition(position)
            calculateWatchDurationAndPost(cwHitRunner, position)
            handleFwdRewButtons(position)

            mPlayerView?.hideController()
            //}
        }
    }

    private fun handleFwdRewButtons(progress: Long) {
        playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.show()
        if (progress > PLAYER_INCREMENT_DECREMENT_MS) {
            playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.show()
        } else {
            playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.hide()
        }
    }

    fun updateCurrentDragPosition(position:Long) {
        val currentTimeBar = if (mIsInFullScreenMode)
            playerBinding.playerView.findViewById<View>(R.id.exo_progress)
        else
            binding.miniProgressPlayer as View

        val exoPosition = playerBinding.playerView.findViewById<TextView?>(R.id.exo_position)

        exoPosition?.text =
            Util.getStringForTime(
                formatBuilder, Formatter(formatBuilder, Locale.getDefault()), position
            )
        val l =
            (position.toFloat() / (mPlayer?.duration ?: 0))

        // position of thumb
        val l1 =
            l * (currentTimeBar?.width ?: 1)
        val xOfExoPosition =
            ((currentTimeBar?.x
                ?: 0f) + l1) - ((exoPosition?.width ?: 1) / 2)
        exoPosition?.x =
            if (xOfExoPosition < (currentTimeBar?.x ?: 0f))
                currentTimeBar?.x ?: 0f
            else if (xOfExoPosition > (((currentTimeBar?.x ?: 0f) + (currentTimeBar?.width ?: 0)) - (exoPosition?.width ?: 0)))
                (((currentTimeBar?.x ?: 0f) + (currentTimeBar?.width ?: 0)) - (exoPosition?.width ?: 0))
            else
                xOfExoPosition
        currentTimeBar?.post {
            val point = IntArray(2)
            currentTimeBar?.getLocationOnScreen(point)
            val yOfExoPosition =
                point[1] - ((exoPosition?.height ?: 1) * 1.25)
            exoPosition?.y = if (mIsInFullScreenMode)
                yOfExoPosition.toFloat()
            else
                currentTimeBar.y - ((exoPosition?.height ?: 0) * 1.25).toFloat()
        }
        playerBinding.playerView.findViewById<TextView>(R.id.exo_position)?.show()
    }
    private fun openAudioOption() {
        if (mPlayer != null) {
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
            showPopUp(
                trackSelector,
                trackSelections,
                audioLanguage,
                subtitleList,
                trackAudioEnable = false
            )
            if (mPlayer?.isPlaying == true) {
                isAutoPaused = true
                mPlayer?.playWhenReady = false
            }

        }
    }

    private fun openVideoOption() {
        if (mPlayer != null) {
            if(videoQuality == null){
                val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
                val trackGroups = mappedTrackInfo?.getTrackGroups(VIDEO_TRACK) ?: return
                initVideo(trackGroups, trackSelections)
            }
            val availableVideoQuality: VideoQuality =
                videoQuality ?: VideoQuality()
            val videoQuality: List<Bitrate?>? = availableVideoQuality.getBitrateArrayList()
            if (videoQuality != null && videoQuality.size > 0) {
                if (mPlayer?.isPlaying == true) {
                    isAutoPaused = true
                    mPlayer?.playWhenReady = false
                }
                showVideoQualityPopUp(availableVideoQuality)
            }
        }
    }
    private fun onPlayerPlaying() {
        binding.btnPlayerController.setText(getString(R.string.pause), null)

        e(TAG, "inside onPlayerPlaying firstTimeCW: $firstTimeCW")
        if (firstTimeCW) {
            playerBinding.playerView.findViewById<RelativeLayout>(R.id.rl_play_pause)
                .show()
            playerBinding.playerView.findViewById<ConstraintLayout>(R.id.llReplay).hide()
            firstTimeCW = false
            actionCWhandler.postDelayed(cwHitRunner, 10000)
            intialBufferDuration =
                (System.currentTimeMillis() - startInitialBufferTime) / 1000
            startTime = getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT)
            firstPlayStartTime = System.currentTimeMillis()

            if (resumeDuration > 0) {
                mPlayer?.seekTo(resumeDuration.toLong())
                resumeDuration = 0
            }
            trackInitialBuffering(
                intialBufferDuration.toString(),
                (intialBufferDuration / 60).toString(),
                playerModel,
                contentItem,
                fetchVideoQualityUsingBitrate(getBitRate(mPlayer)),
                getCurrentSeekBarProgressInPercentage(
                    resumeDuration.toLong(),
                    mPlayer?.duration
                )
            )
        }
        if (isPlayerPaused) {
            trackResumeAndInitPlayer(
                playerModel!!,
                contentItem,
                getCurrentSeekBarProgressInPercentage(
                    mPlayer?.currentPosition,
                    mPlayer?.duration
                )
            )
            isPlayerPaused = false
        }
    }

    private fun onPlayerPaused() {
        binding.btnPlayerController.setText(getString(R.string.resume), null)
        if (firstTimeCW) {
            mPlayer?.playWhenReady = true
        } else {
            trackOnPause(
                playerModel!!, contentItem,
                getCurrentSeekBarProgressInPercentage(
                    mPlayer?.currentPosition,
                    mPlayer?.duration
                )
            )
        }
    }

    private fun playerEnded() {
        startProgressing(false)
        playerBinding.playerView.findViewById<RelativeLayout>(R.id.rl_play_pause)?.show()
        isPlayerEnded = true
        if (nextEpisodeAvailable) {
            playerBinding.playerView.hide()
            playerBinding.nextEpisodeScreen.clRoot.show()
            binding.btnPlayerController.setText(getString(R.string.play_now), null)
            binding.btnPlayerController.setOnClickListener(nextEpisodeClickListener)
            viewModel.timer?.start()
        } else {
            playerBinding.playerView.findViewById<View>(R.id.iv_thumbnail)?.show()
            playerBinding.playerView.findViewById<View>(R.id.llReplay).show()
            binding.btnPlayerController.tag = REPLAY_TAG
            binding.btnPlayerController.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_replay)
            binding.btnPlayerController.setText(getString(R.string.replay), null)
            firstTimeCW = true
        }
        mPlayerView?.controllerAutoShow = true
        mPlayerView?.controllerHideOnTouch = false
        mPlayerView?.showController()
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.widgetController).hide()
        actionCWhandler.removeCallbacks(cwHitRunner)

        val totalDuration = (playerModel?.getTotalDuration() ?: (mPlayer?.duration ?: 1) / 1000).toInt()
        publishWatchedContent(null, totalDuration, totalDuration, viewModel)
    }


    private fun setTimer(contentItem: ContentItem) {
        viewModel.timer = object : CountDownTimer(NEXT_EPISODE_TIMER_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var secs = (millisUntilFinished / 1000)

                if (secs == 0L) {
                    secs = 1L
                }
                context?.let {
                    playerBinding.nextEpisodeScreen.tvStartingTimer.text =
                        String.format(
                            Locale.US,
                            getString(R.string.seconds_timer),
                            (secs).toString()
                        )
                }
            }

            override fun onFinish() {
//                playerBinding.nextEpisodeScreen.tvStarting.invisible()
//                playerBinding.nextEpisodeScreen.tvStartingTimer.text = "..."
                playerBinding.nextEpisodeScreen.tvStartingTimer.text =
                    String.format(
                        Locale.US,
                        getString(R.string.seconds_timer),
                        "..."
                    )
                viewModel.setNextEpisodeToPlay(contentItem)
            }
        }
        nextEpisodeClickListener = View.OnClickListener { playEpisode(contentItem) }
    }

    private fun playEpisode(contentItem: ContentItem) {
        viewModel.timer?.cancel()
        if (!isContentPlayable(contentItem.partnerSubscriptionType)) {
            showContentPlaybackDialog(contentItem)
            return
        }
        /*if (!isPlayerPaused) {
            isAutoPaused = true
            ttnPlayerHelper?.playerPause()
        }*/
        if (PROVIDER_SHEMAROO.equals(contentItem.provider, true)) {
            playerModel = null
            playerModel = viewModel.generatePlayerModel(
                contentItem,
                playerModel?.getTAShowType() ?: ""
            )
            val signedURL = ShemarooHelper.decryptMd5(contentItem.partnerDeepLinkUrl)
            viewModel.fetchShemarooMeContentPlayback(signedURL, false)
        } else if (PROVIDER_VOOTSELECT.equals(contentItem.provider, true)) {
            playerModel = null
            playerModel = viewModel.generatePlayerModel(
                contentItem,
                playerModel?.getTAShowType() ?: ""
            )
            val partner = (contentItem.provider).replace("_", "")
            val request = VootRequest(
                contentId = contentItem.providerContentId,
                contentType = playerModel?.getContentType() ?: "",
                partner = partner,
                baId = viewModel.sharedPrefs.getBaId(),
                type = "DASH"
            )
            viewModel.fetchVootPlaybackUrl(request, false)
        } else if (PROVIDER_VOOTKIDS.equals(contentItem.provider, true)) {
            playerModel = null
            playerModel = viewModel.generatePlayerModel(
                contentItem,
                playerModel?.getTAShowType() ?: ""
            )
            val partner = (contentItem.provider).replace("_", "")
            val request = VootRequest(
                contentId = contentItem.providerContentId,
                contentType = playerModel?.getContentType() ?: "",
                partner = partner,
                baId = viewModel.sharedPrefs.getBaId(),
                type = "DASH"
            )
            viewModel.fetchVootKidsPlaybackUrl(request, false)
        } else {
            val playerModel =
                viewModel.generatePlayerModel(contentItem, playerModel?.getTAShowType() ?: "")
            navigateToPlayer(playerModel)
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
        releasePlayer()
        initializePlayerHelper(null)
    }


}