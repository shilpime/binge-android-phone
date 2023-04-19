package com.tatasky.binge.ui.features.player

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.databinding.LayoutErrorPlayerBinding
import com.tatasky.binge.databinding.LayoutNetworkErrorBinding
import com.tatasky.binge.ui.base.MyApp
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.details.DetailsFragment
import com.tatasky.binge.ui.features.dialog.CustomFullScreenDialog
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.player.model.AudioLanguage
import com.tatasky.binge.ui.features.player.model.Bitrate
import com.tatasky.binge.ui.features.player.model.VideoQuality
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.ContentUtil.isLiveContent
import com.ttn.ttnplayer.player.TtnPlayerHelper
import com.ttn.ttnplayer.util.L3_MAX_BITRATE
import com.ttn.ttnplayer.util.ProviderSpecificRestrictions.PROVIDER_CHAUPAL_RESTRICTIONS
import java.net.InetAddress

abstract class PlayerBaseFragment<PVB : ViewDataBinding> : DetailsFragment(),
    DisplayManager.DisplayListener {

    private val TAG = this.javaClass.simpleName
    /**
     * Holds duration and state of VTR.
     * @see setOfVtrTriggerPoints
     * @params accepted:
     * [Long] Duration in seconds on which event needs to be triggered.
     * [Boolean] Triggering done state value.
     **/
    protected val mapOfVtrTriggerState = mutableMapOf<Long, Boolean>()
    /**
     * Holds triggering point of event on content watch of 50%, 75% etc.
     * @see mapOfVtrTriggerState
     * @params accepted:
     * [Float] Custom division points, To calculate the duration on which event needs to be fired.
     * [String] Division points in %
     **/
    private val setOfVtrTriggerPoints = setOf(
        Pair((2.toFloat() / 4.toFloat()), VTR_PERCENTAGE_50),
        Pair((3.toFloat() / 4.toFloat()), VTR_PERCENTAGE_75)
    )
    private var selectedQuality: String=""
    protected val ERROR_MSG_NETWORK = "Unable To Connect"
    protected var audioLanguage: AudioLanguage = AudioLanguage()
    protected var subtitleList: AudioLanguage = AudioLanguage()
    protected var videoQuality: VideoQuality? = null
    protected var trackSelections: TrackSelectionArray? = null
    protected lateinit var networkErrorView: LayoutNetworkErrorBinding
    protected lateinit var errorView: LayoutErrorPlayerBinding
    protected var dialog: CustomFullScreenDialog? = null
    protected var isAutoPaused: Boolean = false
    protected var prevWatchDuration = 0
    protected var ttnPlayerHelper: TtnPlayerHelper? = null
    protected var isPlayerEnded = false
    protected var callProbeEventOnce = false
    protected var nextEpisodeClickListener : View.OnClickListener? =null
    protected var defaultVolume = 0f
    protected var isSoundOn = true


    protected val VIDEO_TRACK: Int = 0
    protected val AUDIO_TRACK: Int = 1
    protected val SUBTITLE_TRACK: Int = 2
    protected val ERROR_CODE_DEVICE_LIMIT = -3
    protected val ERROR_CODE_DEVICE_DEACTIVATED = -2
    protected val PLAYER_DEFAULT_ERROR_CODE = -1
    protected val AUDIO_TAG = "optionAudio"
    protected val VIDEO_TAG = "optionVideo"
    protected val WATCHLIST_TAG = "optionWatchlist"
    protected val FWD_TAG = "optionFwd"
    protected val REW_TAG = "optionRew"
    protected val PLAY_TAG = "optionPlay"
    protected val REPLAY_TAG = "optionReplay"
    protected val FULLSCREEN_TAG = "optionFullScreen"
    protected var startInitialBufferTime = 0L
    protected var intialBufferDuration = 0L
    protected var startTime = ""
    protected var stopTime = ""
    protected var firstPlayStartTime = 0L
    protected var watchedDuration = 0L
    protected var startPlayTime: Long = 0
    protected var playDuration: Long = 0
    protected var numberOfPauses = 0
    protected var numberOfResumes = 0
    protected var subtitle = "off"
    protected var subtitleLang = ""

    protected var PLAYER_INCREMENT_DECREMENT_MS = 10000L
    protected var NEXT_EPISODE_TIMER_MS = 11000L
    protected var CONTROLLER_HIDE_TIME_MS = 5000L
    protected var nextEpisodeAvailable = false
    protected lateinit var playerBinding: PVB

    protected var firstTimeCW: Boolean = true
    protected var formatBuilder = StringBuilder()
    protected var trackSelector: DefaultTrackSelector? = null

    //    protected var isNetworkAvailable = true
    protected var isBuffering = true

    var preferredSubtitleLanguage: String? = null
    var preferredAudioLanguage: String? = null
    var scaleDetector: MyScaleGestureDetector? = null

    abstract fun zoomIn()
    abstract fun zoomOut()
    abstract fun zoomInPinch()

    /**
     * Triggers event based on content watch duration.
     * Eg. VTR (View through rate) 50%, 75% etc.
     * @param currentTime
     * @param totalTime
     * */
    protected fun handleViewThroughRateEvents(currentTime: Long, totalTime: Long) {
        val currentTimeInSeconds = currentTime / 1000
        val totalTimeInSeconds = totalTime / 1000
        if (mapOfVtrTriggerState.isEmpty()) {
            setOfVtrTriggerPoints.forEach {
                val calculatedVtrTriggerDuration = (totalTimeInSeconds * it.first).toLong()
                mapOfVtrTriggerState[calculatedVtrTriggerDuration] =
                    (currentTimeInSeconds >= totalTimeInSeconds * it.first)
                d(
                    TAG,
                    "VTR Trigger points assigned at ${it.second}, " +
                            "Duration in seconds: $calculatedVtrTriggerDuration " +
                            "Total time in seconds: $totalTimeInSeconds " +
                            "Triggered: ${mapOfVtrTriggerState[calculatedVtrTriggerDuration]}"
                )
            }
        }
        mapOfVtrTriggerState.onEachIndexed { index, vtrTriggerPoint ->
            if (currentTimeInSeconds >= vtrTriggerPoint.key && !vtrTriggerPoint.value) {
                when (index) {
                    /**Use Index of [setOfVtrTriggerPoints] to identify VTR 50, 75 etc.**/
                    0 -> {
                        val watchedDuration = playDuration
                        trackVtrContentPlay(playerModel, contentItem, VTR_PERCENTAGE_50, watchedDuration)
                    }
                    1 -> {
                        val watchedDuration = playDuration
                        trackVtrContentPlay(playerModel, contentItem, VTR_PERCENTAGE_75, watchedDuration)
                    }
                }
                mapOfVtrTriggerState[vtrTriggerPoint.key] = true
                d(
                    TAG,
                    "VTR Trigger duration in seconds: ${vtrTriggerPoint.key}, Triggered: ${vtrTriggerPoint.value}"
                )
            } else if (currentTimeInSeconds < vtrTriggerPoint.key && vtrTriggerPoint.value) {
                mapOfVtrTriggerState[vtrTriggerPoint.key] = false
                d(
                    TAG,
                    "VTR Reset Duration in seconds: ${vtrTriggerPoint.key}, Triggered: ${vtrTriggerPoint.value}"
                )
            }
        }
    }

    override fun onDestroyView() {
        binding.playerFrame.hide()
        binding.miniProgressPlayer.hide()
        binding.btnPlayerController.hide()
        binding.btnPrimary.show()
        viewModel.timer?.cancel()
        super.onDestroyView()
        isPlayerStarted = false
        actionCWhandler.removeCallbacks(cwHitRunner)
    }

    override fun onResume() {
        super.onResume()
        //requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        dialog?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        if (!isNetworkConnected(requireContext())) {
            isNetworkAvailable = false
//            if (isBuffering)
//                showNetworkAlert()
        }
    }


    abstract fun getPlayerLayoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parentView = super.onCreateView(inflater, container, savedInstanceState)
//        if(binding.playerFrame.childCount==0) {
        binding.playerFrame.removeAllViews()
        playerBinding = DataBindingUtil.bind(
            inflater.inflate(
                getPlayerLayoutId(),
                container,
                false
            )
        )!!
        binding.playerFrame.addView(playerBinding.root)
        playerBinding.lifecycleOwner = viewLifecycleOwner
        mDisplayManager = requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?
//        }
        return parentView
    }

    override val networkCallback: NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            e("networkCallback", "inside onAvailable $isPlayerStarted , $isNetworkAvailable")
            if (isPlayerStarted) {
//                if (!isNetworkAvailable) {
                actionCWhandler.postDelayed({
                    restartPlayerAfterNetworkAvailable()
                }, 1000)
//                }
            }
            isNetworkAvailable = true
        }

        override fun onLost(network: Network) {
            //only isPlayerStarted not working
            e("networkCallback","inside onLost $isPlayerStarted , $isNetworkAvailable")
            if (isPlayerStarted && isNetworkAvailable) {
                lostConnection()
            }
            isNetworkAvailable = false
        }
    }

    fun checkConnection() {
        try {
            val ipAddr: InetAddress = InetAddress.getByName("google.com")
            e("PlayerBaseFragment", "InetAddress ipAddr: $ipAddr")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    abstract fun lostConnection()

    abstract fun restartPlayerAfterNetworkAvailable()

    /**
     * Time sliding
     */
    protected val actionCWhandler = Handler(Looper.getMainLooper())

    /**
     * Slide
     */
    protected val cwHitRunner = object : Runnable {

        override fun run() {
            //hit action call for CW
            calculateWatchDurationAndPost(this, 0L)
        }
    }

    abstract fun calculateWatchDurationAndPost(runnable: Runnable, watchDuration: Long)

    protected fun publishWatchedContent(
        runnable: Runnable?,
        watchDuration: Int,
        totalDuration: Int,
        playerViewModel: PlayerViewModel
    ) {
        if (prevWatchDuration == watchDuration) return
        var totalDuration = totalDuration
        prevWatchDuration = watchDuration
        var watchDuration = watchDuration
        if (watchDuration > totalDuration) {
            watchDuration = totalDuration
        } else if (runnable != null) {
            actionCWhandler.postDelayed(runnable, 10000)
        }
        val totalDurationMeta: Int = (playerModel?.getTotalDuration() ?: 0).toInt()
        if (totalDurationMeta != 0 && totalDurationMeta != totalDuration) {
            //check 99% of content, if played then send complete duration
            if (checkWatchedReplay(totalDuration, watchDuration)) {
                watchDuration = totalDurationMeta
                totalDuration = totalDurationMeta
            }
        }
        playerModel?.let {
            playerViewModel.actionCW(
                it.getContentId()!!,
                getContentType(it.getContentType()!!),
                watchDuration,
                totalDuration
            )
        }
    }

    /**
     * Used to determine which custom message is used to show to user.
     *
     * @param errorCode
     * @param message
     * @return
     */
    protected fun parseError(errorCode: Int): String? {
        var message = ""
        message = getCustomError(errorCode)
        trackOnPlayerFailure(playerModel, message, contentItem)
        trackOnPlayerError(playerModel, errorCode.toString(), message, PARA_PI_ERROR_ORIGIN, PARA_ERROR_TYPE_PLAYER)
        return if (errorCode == PLAYER_DEFAULT_ERROR_CODE && !isNetworkAvailable) {
            showNetworkAlert()
            null
        } else if (isNetworkAvailable) {
            message
        } else {
            showNetworkAlert()
            null
        }
    }

    /**
     * Used to get the Custom Error on the basis of Error Code received from Player.
     *
     * @param errorCode
     * @return
     */
    protected fun getCustomError(errorCode: Int): String {
        context?.let {
            when (errorCode) {
                -3 -> return getString(R.string.error_device_limit)
                400 -> return getString(R.string.error_400)
                401 -> return getString(R.string.error_session_expire)
                403 -> return getString(R.string.error_not_subscribe)
                500 -> return getString(R.string.error_not_subscribe)
                550 -> return getString(R.string.error_generic)
                -500 -> return getString(R.string.error_network_unavailable)
                else -> return getString(R.string.error_generic)
            }
        }
        return ""
    }

    protected fun showNetworkAlert() {
        viewModel.hideLoader()
        networkErrorView.root.show()
        binding.miniProgressPlayer.isEnabled = false
        binding.btnPlayerController.setOnClickListener(null)
        networkErrorView.btnRetry.setOnClickListener(View.OnClickListener {
            hideNetworkView()
            retryPlayer()
        })
        networkErrorView.btnSettings.setOnClickListener {
            try {
                val intent = Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    protected fun hideNetworkView() {
        networkErrorView.root.hide()
        binding.miniProgressPlayer.isEnabled = true
    }

    abstract fun retryPlayer()

    abstract fun initializePlayerHelper(savedInstanceState: Bundle?)

    protected fun trackOnPause(playerModel: PlayerModel, contentItem: ContentItem?,seekbarProgress:String?) {
        numberOfPauses++
        playerAnalytics.trackPauseContent(
            playerModel.getTitle() ?: "",
            playerModel.getGenre(),
            playerModel.getContentType() ?: "",
            playerModel.getProvider() ?: "",
            startTime,
            "",
            (watchedDuration / 60000).toString(),
            (watchedDuration / 1000).toString(),
            (intialBufferDuration).toString(),
            (intialBufferDuration / 60).toString(),
            numberOfPauses.toString(),
            numberOfResumes.toString(),
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            (contentItem?.origin ?: "").toUpperCase(),
            contentItem?.source?.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            playerModel.getAudioLanguages(),
            sharedPrefs.getSubscribedPack(),
            contentItem?.railPosition ?: "",
            playerModel.getContractName() ?: "",
            contentItem?.partnerSubscriptionType?.contains(FREE, true) == true,
            contentItem?.source?:"",
            contentItem?.origin?:"",
            contentItem?.railCategory?:"",
            contentItem?.language?.getOrNull(0),
            contentItem?.genres?.getOrNull(0),
            if(contentAuth) YES else NO,
            contentItem?.contentType?:"",
            contentItem?.contentPosition?:"",
            detailsResponse?.data?.metaDetails?.rating ?: "",
            detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            detailsResponse?.data?.metaDetails?.actor,
            if (sharedPrefs.getAutoPlayTrailerOn() && isTrailerInitialized) YES else NO,
            liveContent = if (isLiveContent(
                    contentItem?.contentType,
                    contentItem?.liveContent == true
                )
            ) YES else NO,
            seekbarProgress ?: "0",
            selectedQuality,
            contentItem?.contentConfigType ?: ""
        )
    }

    protected fun trackResumeAndInitPlayer(playerModel: PlayerModel, contentItem: ContentItem?,seekbarProgress:String?) {
        numberOfResumes++
        playerAnalytics.trackResumeContent(
            playerModel.getTitle() ?: "",
            playerModel.getGenre(),
            playerModel.getContentType() ?: "",
            playerModel.getProvider()?:"",
            startTime,
            "",
            (watchedDuration / 60000).toString(),
            (watchedDuration / 1000).toString(),
            (intialBufferDuration).toString(),
            (intialBufferDuration / 60).toString(),
            numberOfPauses.toString(),
            numberOfResumes.toString(),
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            (contentItem?.origin ?: "").toUpperCase(),
            contentItem?.source?.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            playerModel.getAudioLanguages(),
            sharedPrefs.getSubscribedPack(),
            contentItem?.railPosition ?: "",
            playerModel.getContractName() ?: "",
            contentItem?.partnerSubscriptionType?.contains(FREE, true) == true,
            contentItem?.source?:"",
            contentItem?.origin?:"",
            contentItem?.railCategory?:"",
            contentItem?.language?.getOrNull(0),
            contentItem?.genres?.getOrNull(0),
            if(contentAuth) YES else NO,
            contentItem?.contentType?:"",
            contentItem?.contentPosition?:"",
            detailsResponse?.data?.metaDetails?.rating ?: "",
            detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            detailsResponse?.data?.metaDetails?.actor,
            if (sharedPrefs.getAutoPlayTrailerOn() && isTrailerInitialized) YES else NO,
            liveContent = if (isLiveContent(
                    contentItem?.contentType,
                    contentItem?.liveContent == true
                )
            ) YES else NO,
            seekbarProgress ?: "0%",
            selectedQuality,
            contentItem?.contentConfigType ?: ""
        )
    }

    protected fun showPopUp(
        trackSelector: DefaultTrackSelector?,
        trackSelectionArray: TrackSelectionArray?,
        metaLanguages: AudioLanguage,
        metaSubtitleList: AudioLanguage,
        trackAudioEnable: Boolean
    ) {
        if (dialog?.isShowing != true) {
            dialog = CustomFullScreenDialog(requireContext(), R.style.DialogThemeTransparent)
            dialog?.setCancelable(false)
            dialog?.setContentView(R.layout.multi_popup_select_player_option)
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            dialog?.window?.decorView?.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            dialog?.findViewById<ImageView>(R.id.iv_close)?.setOnClickListener {
                dialog?.cancel()
            }
            dialog?.setOnCancelListener {
                audioVideoDialogClosed()
            }
            val textViewTitle = dialog?.findViewById<TextView>(R.id.tv_title)
            textViewTitle?.text = getString(R.string.audio_setting)
            val audioTracks = TrackSelector(requireContext(), trackSelector, AUDIO_TRACK)
            val audioLanguages = audioTracks.getFormats()
            val subtitleTracks = TrackSelector(requireContext(), trackSelector, SUBTITLE_TRACK)
            val subtitleList = subtitleTracks.getFormats()
            val audioOptionsView =
                LayoutInflater.from(context).inflate(R.layout.layout_player_options, null)
            val audioOptionsTitleView =
                audioOptionsView.findViewById<View>(R.id.tv_options_title) as TextView
            audioOptionsTitleView.text = getString(R.string.title_audio)
            val recyclerView: RecyclerView =
                audioOptionsView.findViewById<View>(R.id.recyclerView_player_option) as RecyclerView
            e("OpenAudio","audioLanguages from track : ${audioLanguages.isEmpty()}")

            val playerOptionAdapter =
                if (trackAudioEnable && audioLanguages.isNotEmpty()) {
                    e(
                        "selectedAudio ",
                        " selectedAudio ${trackSelectionArray?.get(1)?.selectedFormat}" +
                                ", audioLanguages:$audioLanguages"
                    )
                    TracksAdapter(false,
                        trackSelectionArray?.get(1)?.selectedFormat,
                        audioLanguages,
                        View.OnClickListener { selectedView ->
                            changeAudioTrack(selectedView.tag as Format?)
                        })
                }
                else {
                    val selectedIndex = metaLanguages.defaultIndex
                    if (metaLanguages.list.size == 0) {
                        metaLanguages.list.add("None")
                    }
                    val selectedAudio = metaLanguages.list[selectedIndex]
                    AudioOptionAdapter(
                        metaLanguages.list,
                        View.OnClickListener { selectedView ->
                            val currentSelectedLanguage = selectedView.tag.toString()
                            e(
                                "currentSelectedLanguage ",
                                " currentSelectedLanguage $currentSelectedLanguage"
                            )
                            e("selectedAudio ", " selectedAudio $selectedAudio")
                            if (!currentSelectedLanguage.equals(selectedAudio, ignoreCase = true)) {
                                val currentSelectedLanguageIndex =
                                    metaLanguages.list.indexOf(currentSelectedLanguage)
                                metaLanguages.defaultIndex = currentSelectedLanguageIndex
                                changeAudioTrack(
                                    Format.createAudioSampleFormat(
                                        null, null, null, 0, 0, 0, 0,
                                        emptyList<ByteArray>(), null, 0, currentSelectedLanguage
                                    )
                                )
                            }
                        },
                        selectedAudio
                    )
                }
            val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            val audioParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
            )
            audioParam.marginEnd = 16
            audioOptionsView.layoutParams = audioParam
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = playerOptionAdapter
            (recyclerView.getItemAnimator() as SimpleItemAnimator).setSupportsChangeAnimations(
                false
            )
            dialog?.findViewById<LinearLayout>(R.id.ll_options_container)
                ?.addView(audioOptionsView)
            val subtitleOptionsView =
                LayoutInflater.from(context).inflate(R.layout.layout_player_options, null)
            val subtitleTitleView =
                subtitleOptionsView.findViewById<View>(R.id.tv_options_title) as TextView
            subtitleTitleView.text = getString(R.string.title_subtitle)
            val subtitleRecyclerView: RecyclerView =
                subtitleOptionsView.findViewById<View>(R.id.recyclerView_player_option) as RecyclerView
            val subtitleOptionAdapter =
                if (subtitleList.isNotEmpty())
                    TracksAdapter(true, trackSelectionArray?.get(2)?.selectedFormat,
                        subtitleList, View.OnClickListener { selectedView ->
                            val format = selectedView.tag as Format?
                            changeSubtitle(format?.language ?: null)
                        })
                else {
                    val selectedIndex = metaSubtitleList.defaultIndex
                    if (metaSubtitleList.list.size == 0) {
                        metaSubtitleList.list.add("None")
                    }
                    val selectedAudio = metaSubtitleList.list[selectedIndex]
                    AudioOptionAdapter(
                        metaSubtitleList.list,
                        View.OnClickListener { selectedView ->
                            var currentSelectedLanguage: String? = selectedView.tag.toString()
                            e("selectedAudio ", " selectedAudio $currentSelectedLanguage")
                            if (!currentSelectedLanguage.equals(selectedAudio, ignoreCase = true)) {
                                val currentSelectedLanguageIndex =
                                    metaSubtitleList.list.indexOf(currentSelectedLanguage)
                                metaSubtitleList.defaultIndex = currentSelectedLanguageIndex
                                if (currentSelectedLanguageIndex == 0)
                                    currentSelectedLanguage = null
                                changeSubtitle(currentSelectedLanguage)
                            }
                        },
                        selectedAudio
                    )
                }

            val subtitleParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
            )
            subtitleParams.marginStart = 16
            val subtitlelayoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            subtitleRecyclerView.layoutManager = subtitlelayoutManager
            subtitleOptionsView.layoutParams = subtitleParams
            subtitleRecyclerView.adapter = subtitleOptionAdapter
            dialog?.findViewById<LinearLayout>(R.id.ll_options_container)
                ?.addView(subtitleOptionsView)
        }
        dialog?.show()
    }

    abstract fun audioVideoDialogClosed()

    abstract fun changeAudioTrack(format: Format?)

    protected fun showVideoQualityPopUp(availableLVideoQuality: VideoQuality) {
        val videoQuality: List<Bitrate?>? = availableLVideoQuality.getBitrateArrayList()
        if (videoQuality != null && videoQuality.size > 0 && dialog?.isShowing != true) {
            dialog = CustomFullScreenDialog(requireContext(), R.style.DialogThemeTransparent)
            dialog?.setCancelable(false)
            dialog?.setContentView(R.layout.popup_select_player_option)
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            dialog?.window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
            dialog?.findViewById<ImageView>(R.id.iv_close)?.setOnClickListener {
                dialog?.cancel()
            }
            dialog?.setOnCancelListener {

                audioVideoDialogClosed()
            }
            val textViewTitle = dialog?.findViewById<TextView>(R.id.tv_title)
            textViewTitle?.text = getString(R.string.video_qaultiy)
            val recyclerView: RecyclerView? =
                dialog?.findViewById<RecyclerView>(R.id.recyclerView_player_option)
            val selectedIndex: Int = availableLVideoQuality.getSelectedQualityIndex()
            val selectedVideoQuality: String = videoQuality[selectedIndex]!!.getName()!!
            e(
                "currentSelectedVideo ",
                " selectedIndex: $selectedIndex , selectedVideoQuality : $selectedVideoQuality"
            )
            val playerOptionAdapter =
                PlayerOptionAdapter(videoQuality, View.OnClickListener { selectedView ->
                    val tag = selectedView.tag
                    if (tag is Bitrate) {
                        val currentSelectedVideoQuality: Bitrate = tag as Bitrate
                        e(
                            "currentSelectedVideo ",
                            " currentSelectedVideo " + currentSelectedVideoQuality.getName()
                        )
                        e("selectedVideoQuality ", " selectedVideoQuality $selectedVideoQuality")
                        if (!currentSelectedVideoQuality.getName().equals(
                                selectedVideoQuality,
                                true
                            )
                        ) {
                            val currentSelectedLanguageIndex =
                                videoQuality.indexOf(currentSelectedVideoQuality)

                            e(
                                "currentSelectedVideo ",
                                " currentSelectedLanguageIndex $currentSelectedLanguageIndex"
                            )

                            availableLVideoQuality.setSelectedQualityIndex(
                                currentSelectedLanguageIndex
                            )
                            this.selectedQuality=fetchVideoQualityUsingBitrate(currentSelectedVideoQuality.getBitrate().toInt()).toString()
                            changeVideoQuality(currentSelectedVideoQuality)
                        }
                    }
//                    dialog?.cancel()
                }, selectedVideoQuality)
            val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            recyclerView?.layoutManager = layoutManager
            recyclerView?.adapter = playerOptionAdapter
            val layoutParams = recyclerView?.layoutParams as RelativeLayout.LayoutParams
            layoutParams.apply {
                marginStart = dpToPx(requireContext(), 96)
                marginEnd = dpToPx(requireContext(), 96)
            }
            recyclerView?.layoutParams = layoutParams
            recyclerView?.scrollToPosition(selectedIndex)
            dialog?.show()
        }
    }

    abstract fun changeSubtitle(language: String?)

    abstract fun changeVideoQuality(currentSelectedVideoQuality: Bitrate)

    protected fun trackInitialBuffering(
        durationSeconds: String,
        durationMinutes: String,
        playerModel: PlayerModel?,
        contentItem: ContentItem?,
        quality:Int,
        seekbarProgress:String?
    ) {
        selectedQuality=quality.toString()
        playerAnalytics.trackInitialBufferTime(
            durationSeconds,
            durationMinutes,
            playerModel?.getTitle() ?: "Not Available",
            playerModel?.getGenre(),
            playerModel?.getContentType() ?: "",
            startTime,
            stopTime,
            (watchedDuration / 60000).toString(),
            (intialBufferDuration / 60).toString(),
            numberOfPauses,
            numberOfResumes,
            playerModel?.getProvider() ?: "",
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            (contentItem?.origin ?: "").toUpperCase(),
            contentItem?.source?.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            playerModel?.getAudioLanguages(),
            sharedPrefs.getSubscribedPack(),
            contentItem?.railPosition ?: "",
            playerModel?.getContractName() ?: "",
            playerModel?.getParentTitle()
                ?: detailsResponse?.data?.metaDetails?.getParentTitle() ?: "",
            contentItem?.partnerSubscriptionType?.contains(FREE, true) == true,
            contentItem?.source ?:"",
            contentItem?.origin ?:"",
            contentItem?.railCategory ?:"",
            contentItem?.language?.getOrNull(0),
            contentItem?.genres?.getOrNull(0),
            if(contentAuth)YES else NO,
            contentItem?.contentType?:"",
            contentItem?.contentPosition?:"",
            detailsResponse?.data?.metaDetails?.rating ?: "",
            detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            detailsResponse?.data?.metaDetails?.actor,
            if (sharedPrefs.getAutoPlayTrailerOn() && isTrailerInitialized) YES else NO,
            liveContent = if (isLiveContent(
                    contentItem?.contentType,
                    contentItem?.liveContent == true
                )
            ) YES else NO,
            seekbarProgress ?: "0%",
            selectedQuality,
            contentItem?.contentConfigType ?: ""
        )
    }

    protected fun trackOnRestart(playerModel: PlayerModel?) {
        /* not tracked for now*/

//        playerAnalytics.trackRestart(
//            playerModel?.getTitle() ?: "Not Available",
//            playerModel?.getGenre(),
//            playerModel?.getContentType() ?: ""
//        )
    }
    fun getBitRate(player: SimpleExoPlayer?):Int= player?.videoFormat?.bitrate?:0

    protected fun trackOnPlayerPlayEnd(playerModel: PlayerModel?, contentItem: ContentItem?,seekbarProgress:String?) {
        playerAnalytics.trackContentPlayEnd(
            playerModel?.getTitle() ?: "Not Available",
            playerModel?.getGenre(),
            playerModel?.getContentType() ?: "",
            startTime,
            stopTime,
            (watchedDuration / 60000).toString(),
            (watchedDuration / 1000).toString(),
            (intialBufferDuration).toString(),
            (intialBufferDuration / 60).toString(),
            numberOfPauses.toString(),
            numberOfResumes.toString(),
            playerModel?.getProvider() ?: "",
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?: "",
            (contentItem?.origin ?: "").toUpperCase(),
            contentItem?.source?.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            playerModel?.getAudioLanguages(),
            sharedPrefs.getSubscribedPack(),
            contentItem?.railPosition ?: "",
            playerModel?.getContractName() ?: "",
            playerModel?.getParentTitle()
                ?: detailsResponse?.data?.metaDetails?.getParentTitle() ?: "",
            contentItem?.partnerSubscriptionType?.contains(FREE, true) == true,
            contentItem?.source ?:"",
            contentItem?.origin ?:"",
            contentItem?.railCategory ?:"",
            contentItem?.language?.getOrNull(0),
            contentItem?.genres?.getOrNull(0),
            if(contentAuth) YES else NO,
            contentItem?.contentType?:"",
            contentItem?.contentPosition?:"",
            detailsResponse?.data?.metaDetails?.rating ?: "",
            detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            detailsResponse?.data?.metaDetails?.actor,
            if (sharedPrefs.getAutoPlayTrailerOn() && isTrailerInitialized) YES else NO,
            liveContent = if (isLiveContent(
                    contentItem?.contentType,
                    contentItem?.liveContent == true
                )
            ) YES else NO,
            seekbarProgress ?: "0%",
            selectedQuality,
            contentItem?.contentConfigType ?: ""
        )
    }

    private fun trackVtrContentPlay(
        playerModel: PlayerModel?,
        contentItem: ContentItem?,
        vtrPercentage: String,
        watchedDuration: Long
    ) {
        playerAnalytics.trackViewThroughRateContentPlayEvent(
            playerModel?.getTitle()
                ?: detailsResponse?.data?.metaDetails?.getVodTitle() ?: "",
            playerModel?.getParentTitle()
                ?: detailsResponse?.data?.metaDetails?.getParentTitle() ?: "",
            playerModel?.getAudioLanguages()?.getOrNull(0) ?: "",
            playerModel?.getContentType()
                ?: detailsResponse?.data?.metaDetails?.contentType ?: "",
            (watchedDuration / 1000).toString(),
            (watchedDuration / 60000).toString(),
            contentItem?.contentConfigType ?: "",
            playerModel?.getProvider() ?: "",
            vtrPercentage
        )
    }

    protected fun trackOnPlayerPlay(playerModel: PlayerModel?,contentItem: ContentItem?,quality:Int) {
        this.selectedQuality= quality.toString()
        playerAnalytics.trackPlayContent(
            playerModel?.getTitle() ?: "Not Available",
            playerModel?.getGenre(),
            playerModel?.getContentType() ?: "",
            startTime,
            stopTime,
            (watchedDuration / 60000).toString(),
            (watchedDuration / 1000).toString(),
            (intialBufferDuration).toString(),
            (intialBufferDuration / 60).toString(),
            numberOfPauses.toString(),
            numberOfResumes.toString(),
            playerModel?.getProvider() ?: "",
            detailFragmentArgs.contentAnalyticsModel?.railTitleForAnalytics ?:"",
            (contentItem?.origin?:"").toUpperCase(),
            contentItem?.source?.takeIf { it.isNotEmpty() }?:"Deeplink",
            playerModel?.getAudioLanguages(),
            sharedPrefs.getSubscribedPack(),
            contentItem?.railPosition ?: "",
            playerModel?.getContractName() ?: "",
            detailsResponse?.data?.metaDetails?.getParentTitle()
                ?: playerModel?.getParentTitle() ?: "",
            contentItem?.partnerSubscriptionType?.contains(FREE, true) == true,
            contentItem?.source ?:"",
            contentItem?.origin ?:"",
            contentItem?.railCategory ?:"",
            playerModel?.getAudioLanguages()?.getOrNull(0),
            playerModel?.getGenre()?.getOrNull(0),
            if(contentAuth) YES else NO,
            contentItem?.contentType?:"",
            contentItem?.contentPosition?:"",
            detailsResponse?.data?.metaDetails?.rating ?: "",
            detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            sharedPrefs.getDeviceType() ?: "",
            detailsResponse?.data?.metaDetails?.actor,
            if (sharedPrefs.getAutoPlayTrailerOn() && isTrailerInitialized) YES else NO,
            liveContent = if (isLiveContent(
                    contentItem?.contentType,
                    contentItem?.liveContent == true
                )
            ) YES else NO,
            PlayerUtils.getCurrentSeekBarProgressInPercentage(
                playerModel?.getResumeTime(),
                ((playerModel?.getTotalDuration() ?: 0) * 1000)
            ),
            selectedQuality,
            contentItem?.contentConfigType ?: ""
        )
        if(contentItem?.partnerSubscriptionType?.contains(FREE, true) == true){
            if(!sharedPrefs.getFirstFreeContentPlay()){
                playerAnalytics.trackFirstFreeContentPlay(
                    playerModel?.getTitle() ?: "Not Available",
                    playerModel?.getContentType() ?: "",
                    playerModel?.getProvider() ?: ""
                )
                sharedPrefs.saveFirstFreeContentPlay()
            }
        } else {
            if(!sharedPrefs.getFirstPremiumContentPlay()){
                playerAnalytics.trackFirstPremiumContentPlay(
                    playerModel?.getTitle() ?: "Not Available",
                    playerModel?.getContentType() ?: "",
                    playerModel?.getProvider() ?: ""
                )
                sharedPrefs.saveFirstPremiumContentPlay()
            }
        }
    }

    protected fun trackOnThirdPartyPlayer(playerModel: PlayerModel?) {
        /* Third Party player not tracked for now*/
//        playerAnalytics.trackThirdPartyPlayerLaunch(
//            playerModel?.getTitle() ?: "",
//            playerModel?.getGenre() ?: emptyList(),
//            playerModel?.getContentType() ?: "",
//            playerModel?.getProvider() ?: ""
//        )
    }

    protected fun trackOnAddFavorite() {
        val id = detailsResponse?.data?.metaDetails?.id
        val vodId = detailsResponse?.data?.metaDetails?.vodId
        val contentId = vodId ?: id ?: ""
        viewModel.trackFavoriteLearnAction(
            contentType,
            contentId,
            detailsResponse?.data?.metaDetails?.taShowType ?: "",
            detailsResponse?.data?.metaDetails?.provider ?: "",
            detailsResponse?.data?.detail?.contractName ?: "",
            detailsResponse?.data?.metaDetails?.partnerSubscriptionType,
            contentItem.refId
        )
        watchAnalytics.trackAddFavorite(
            detailsResponse?.data?.metaDetails?.getParentTitle() ?: "",
            parentContentType ?: "",
            detailsResponse?.data?.metaDetails?.genre,
            detailsResponse?.data?.metaDetails?.provider ?: "",
            contentItem.source,
            contentItem.railName
        )
        homeAnalytics.trackAddWatchList(
            contentTitle = detailsResponse?.data?.metaDetails?.getVodTitle()!!,
            contentType = detailsResponse?.data?.metaDetails?.contentType!!,
            contentGenre = detailsResponse?.data?.metaDetails?.genre?.joinToString(",") ?: "",
            pageName = (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
            railTitle = contentItem.railName,
            railPosition = contentItem.railPosition,
            railType = contentItem.origin,
            railCategory = contentItem.railCategory,
            contentLanguage = detailsResponse?.data?.metaDetails?.audio?.joinToString(",")
                ?: "",
            contentGenrePrimary = detailsResponse?.data?.metaDetails?.genre?.getOrNull(0),
            contentPartner = detailsResponse?.data?.metaDetails?.provider
                ?: contentItem.provider,
            contentAuth = if (contentAuth) YES else NO,
            contentCategory = contentItem.categoryType?:"",
            contentPosition = contentItem.contentPosition?:"",
            contentRating = detailsResponse?.data?.metaDetails?.rating ?: "",
            contentParentTitle = playerModel?.getParentTitle()
                ?: detailsResponse?.data?.metaDetails?.getParentTitle() ?: "",
            deviceType = sharedPrefs.getDeviceType() ?: "",
            actors = detailsResponse?.data?.metaDetails?.actor?.joinToString(separator = ",") ?: "",
            packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            source = contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
            liveContent = if (isLiveContent(
                    contentItem.contentType,
                    detailsResponse?.data?.metaDetails?.isLiveContent
                )
            ) YES else NO,
            contentLanguagePrimary = detailsResponse?.data?.metaDetails?.audio?.getOrNull(0),
            freeContent = if (isContentSubscribed) YES else NO,
            releaseYear = detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            contentConfigType = contentItem.contentConfigType
        )
    }

    protected fun trackOnDeleteFavorite() {
        watchAnalytics.trackFavoriteDelete(
            detailsResponse?.data?.metaDetails?.getParentTitle() ?: "",
            parentContentType ?: "",
            detailsResponse?.data?.metaDetails?.genre,
            detailsResponse?.data?.metaDetails?.provider ?: "",
            contentItem.contentConfigType
        )
        homeAnalytics.trackRemoveWatchList(
            contentTitle = detailsResponse?.data?.metaDetails?.getVodTitle()!!,
            contentType = detailsResponse?.data?.metaDetails?.contentType!!,
            contentGenre = detailsResponse?.data?.metaDetails?.genre?.joinToString(",") ?: "",
            pageName = (activity as? LandingActivity)?.getPageName() ?: EVENT_VALUE_SOURCE_DETAIL,
            railTitle = contentItem.railName,
            railPosition = contentItem.railPosition,
            railType = contentItem.origin,
            railCategory = contentItem.railCategory,
            contentLanguage = detailsResponse?.data?.metaDetails?.audio?.joinToString(",")
                ?: "",
            contentGenrePrimary = detailsResponse?.data?.metaDetails?.genre?.getOrNull(0),
            contentPartner = detailsResponse?.data?.metaDetails?.provider
                ?: contentItem.provider,
            contentAuth = if (contentAuth) YES else NO,
            contentCategory = contentItem.categoryType?:"",
            contentPosition = contentItem.contentPosition?:"",
            contentRating = detailsResponse?.data?.metaDetails?.rating ?: "",
            contentParentTitle = playerModel?.getParentTitle()
                ?: detailsResponse?.data?.metaDetails?.getParentTitle() ?: "",
            deviceType = sharedPrefs.getDeviceType() ?: "",
            actors = detailsResponse?.data?.metaDetails?.actor?.joinToString(separator = ",") ?: "",
            packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            source = contentItem.source.takeIf { it.isNotEmpty() } ?: SOURCE_DEEPLINK,
            packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
            liveContent = if (isLiveContent(
                    contentItem.contentType,
                    detailsResponse?.data?.metaDetails?.isLiveContent
                )
            ) YES else NO,
            contentLanguagePrimary = detailsResponse?.data?.metaDetails?.audio?.getOrNull(0),
            freeContent = if (isContentSubscribed) YES else NO,
            releaseYear = detailsResponse?.data?.metaDetails?.releaseYear ?: "",
            contentConfigType = contentItem.contentConfigType
        )
    }




    /**
     * Used to retrieve the error Code from Exception of Player.
     *
     * @param exception
     * @return
     */
    protected fun getErrorCode(exception: Exception): Int {
        try {
            e("TTNPlayerFragment","inside getErrorCode exception: ${exception}")
            return (exception as HttpDataSource.InvalidResponseCodeException).responseCode
        } catch (e: Exception) {
        }

        return PLAYER_DEFAULT_ERROR_CODE
    }

    abstract fun resumePlayerAfterError()
    abstract fun releasePlayer()

    override fun onError(errorModel: ErrorModel) {
        if (isPlayerStarted) {
            showPlayerCommonError(
                if(errorModel.isConcurrency) errorModel.title else viewModel.VIDEO_UNAVAILABLE_TITLE,
                if(errorModel.isConcurrency) errorModel.message ?: getString(R.string.concurrency_error)
                else viewModel.VIDEO_UNAVAILABLE_MESSAGE,
                false
            )
        } else {
            super.onError(errorModel)
        }
    }

    protected fun hideErrorView(){
        errorView.root.hide()
    }

    private fun showPlayerCommonError(title: String, msg: String, isLogout: Boolean) {
        e("HungamaPlayerFragment", "inside showPlayerCommonError() ")
        releasePlayer()
        if (title.isNullOrEmpty()) errorView.tvTitle.hide()
        errorView.tvTitle.text = title
        errorView.tvMessage.text = msg
        errorView.root.show()
        errorView.btnOkay.setOnClickListener {
//            errorView.root.hide()
            activity?.runOnUiThread {
                if (isLogout) {
                    logoutApplication(requireContext())
                } else {
                    releasePlayerWithBack()
                }
            }
            //resumePlayerAfterError()
        }
    }

    protected fun releasePlayerWithBack() {
        e("HungamaPlayerFragment", "inside releasePlayerWithBack() ")
        if(mIsInFullScreenMode){
            activity?.onBackPressed()
        }
        releasePlayer()
        binding.playerFrame.hide()
        binding.miniProgressPlayer.hide()
        binding.btnPlayerController.hide()
        binding.btnPrimary.show()
        viewModel.timer?.cancel()
        isPlayerStarted = false
        actionCWhandler.removeCallbacks(cwHitRunner)
    }

    abstract fun onPlayerMuteStateChanged(isMuted: Boolean)

    override fun forceLogout() {
        //super.forceLogout()
        e("HungamaPlayerFragment", "inside forceLogout() errorMessage:")
        if (isPlayerStarted) {
            val title = getString(R.string.device_removed)
            val msg = getString(R.string.force_logout_message)
            (activity?.application as MyApp).clearAllData()
            if(!mIsInFullScreenMode){
                try {
                    releasePlayer()
                }catch (e:Exception){}
                super.forceLogout()
            } else {
                showPlayerCommonError(title, msg, true)
            }
        } else {
            super.forceLogout()
        }
//        releasePlayer()
    }


    open fun initVideo(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray?,
        isL3DRM : Boolean = false,
        provider: String? = null
    ) {
        this.trackSelections = trackSelections
        if (videoQuality != null) return
        val bitrates = java.util.ArrayList<Bitrate?>()
        bitrates.add(Bitrate(QUALITY_AUTO, 0, -1, -1))
        for (groupIndex in 0 until trackGroups.length) {
            val group = trackGroups[groupIndex]
            val prevBitrate:HashMap<Int, Int> = HashMap()
            for (trackIndex in 0 until group.length) {
                val format = group.getFormat(trackIndex)
                e("ExoPlayerImpl", "groupIndex : $groupIndex , trackIndex:$trackIndex, format : $format")
                if ("video/avc".equals(format.sampleMimeType, ignoreCase = true)) {
                    var height = format.height
                    if ((isL3DRM && (format.bitrate > L3_MAX_BITRATE || format.height > 480)) ||
                        (provider?.equals(PROVIDER_CHAUPAL, true) == true &&
                                format.width > PROVIDER_CHAUPAL_RESTRICTIONS.first ||
                                format.height > PROVIDER_CHAUPAL_RESTRICTIONS.second)
                    ) continue
                    if(height < 0)
                        height = fetchVideoQualityUsingBitrate(format.bitrate)
                    if(trackIndex < group.length-1){
                        val nextFormat = group.getFormat(trackIndex+1)

                        if(format.height == nextFormat.height) {
                            continue
                        }
                    }
                    if (!prevBitrate.contains(height)) {
                        prevBitrate.put(height, format.bitrate / 1000)
                        val quality = " (" + height + "p)"

                        bitrates.add(
                            Bitrate(
                                quality,
                                (format.bitrate / 1000).toLong(),
                                trackIndex,
                                groupIndex
                            )
                        )
                    }
                }
            }
        }
        videoQuality = VideoQuality()
        val sortedBitrates = bitrates.sortedBy {
            it?.getBitrate()
        }
        videoQuality?.setBitrateArrayList(sortedBitrates)
        videoQuality?.setSelectedQualityIndex(0)
    }


    override fun onDisplayChanged(displayId: Int) {
    }

    override fun onDisplayAdded(displayId: Int) {
        if (isExternalDisplayAvailable()) {
            context?.let {
                showToast(it, getString(R.string.casting_not_allowed))
            }
            releasePlayerWithBack()
        }
    }

    override fun onDisplayRemoved(displayId: Int) {
        e("PlayerBaseFragment","inside onDisplayRemoved : $displayId")
        if (!isExternalDisplayAvailable()) {
            e("PlayerBaseFragment","inside onDisplayRemoved : false")
            onExternalDisplayRemoved()
        }
    }

    protected fun registerDisplayListener() {
        e("PlayerBaseFragment","inside registerDisplayListener")
        mDisplayManager!!.registerDisplayListener(this, null)
    }

    protected fun unRegisterDisplayListener() {
        e("PlayerBaseFragment","inside unRegisterDisplayListener")
        mDisplayManager!!.unregisterDisplayListener(this)
    }

    open fun onExternalDisplayRemoved() {
        e("PlayerBaseFragment","inside onExternalDisplayRemoved")
        /*if (mOrientationManager != null) mOrientationManager.enable()
        createPlayer(isToPrepareOnResume)*/
    }

    open fun onExternalDisplayAdded() {
        e("PlayerBaseFragment","inside onExternalDisplayAdded")
        /*if (isFullScreen) {
            exitFullScreen(false)
        }
        if (mOrientationManager != null) mOrientationManager.disable()
        mTTNHelper.hideUiControls()*/
        isTouching(false)
    }

    fun isTouching(yes: Boolean) {
        //mTtnPlayerView.setTouching(yes)
    }


    protected open fun fetchVideoQualityUsingBitrate(bitrate: Int): Int {
        var quality = 1280
        if (bitrate < 200000)
            quality = 144
        else if (bitrate < 500000)
            quality = 240
        else if (bitrate < 750000)
            quality = 360
        else if (bitrate < 1000000)
            quality = 480
        else if (bitrate < 1600000)
            quality = 576
        else if (bitrate < 3000000)
            quality = 720
        else if (bitrate < 6500000)
            quality = 1080
        return quality
    }

}
