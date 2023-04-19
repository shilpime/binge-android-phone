package com.tatasky.binge.ui.features.details.trailer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import com.erosnow.partner.ENSDK
import com.erosnow.partner.`interface`.EnLoginListener
import com.erosnow.partner.`interface`.ErosPlayActionListener
import com.erosnow.partner.model.ENError
import com.erosnow.partner.model.ENPlaybackAssetInfo
import com.erosnow.partner.model.PlayerData
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import com.hungama.sdk.player.HungamaPlayerManager
import com.hungama.sdk.player.HungamaPlayerView
import com.hungama.sdk.player.events.OnPlayerContentLoadListener
import com.hungama.sdk.player.events.OnPlayerStateChangeListener
import com.hungama.sdk.player.exceptions.PlaybackException
import com.hungama.sdk.player.models.ContentType
import com.hungama.sdk.player.models.PlayerState
import com.tatasky.binge.R
import com.tatasky.binge.customviews.TimeBarCustom
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.player.ContentVO
import com.tatasky.binge.ui.features.player.PlayerModel
import com.tatasky.binge.ui.features.player.listeners.PlayerDurationWatcher
import com.tatasky.binge.ui.features.player.listeners.PlayerListener
import com.tatasky.binge.utils.*
import com.ttn.ttnplayer.listeners.TtnPlayerListener
import com.ttn.ttnplayer.player.TtnPlayerHelper
import com.ttn.ttnplayer.ui.TtnPlayerView
import java.util.*
import kotlin.math.max
import kotlin.math.min


class TrailerView : FrameLayout,
    PlayerListener.TimeChangeListener,
    TtnPlayerListener, OnPlayerStateChangeListener,
    OnPlayerContentLoadListener,
    Player.EventListener {

    private var playerModel: PlayerModel? = null

    //    private lateinit var mScaleDetector: ScaleGestureDetector
    private var isReleasePlayer: Boolean = false
    private var isFirstTime: Boolean = true
    private var isAutoPlayOn: Boolean = false
    private var isPausedTrailer: Boolean = false
    private var mSoundOn: Boolean = false
    private var mIsInFullScreen = false
    private var zoomed = false
    private var progress: Long = 0L
    private val FWD_TAG = "optionFwd"
    private val REW_TAG = "optionRew"
    private val PLAY_TAG = "optionPlay"
    private val REPLAY_TAG = "optionReplay"
    private val ZOOM_TAG = "optionZoom"
    private val FULLSCREEN_TAG = "optionFullscreen"
    private var totalLength: Long = 0L
    private var ffwdBckTime: Long = 10000L

    private var formatBuilder = StringBuilder()
    private lateinit var mListener: ITrailerListener
    private val PLAYER_DEFAULT_TIMEOUT: Long = 30
    private var miniProgressBar: TimeBarCustom? = null
    private lateinit var playUrl: String
    private var drmLicenseUrl : String? = null
    private var cookies : String? = null
    var mPlayer: SimpleExoPlayer? = null
    private var defaultVolume = 0f
    private var header_key_requests: Array<String>? = null
    private var isAlreadyStarted = false
    private var isHungamaTrailer = false

    private var mPlaybackController: HungamaPlayerManager? = null
    private var playerDurationWatcher: PlayerDurationWatcher? = null
    private var ttnPlayerHelper: TtnPlayerHelper? = null
    private var ttnPlayerListener:TtnPlayerListener? =null
    private var fullScreenCheckBox: CheckBox? = null
    private var soundCheckBox: CheckBox? = null
    private var ffwdBtn: View? = null
    private var rewBtn: View? = null
    private var playBtn: View? = null
    private var pauseBtn: View? = null
    private var netWorkView: View? = null
    private var replayBtn: View? = null
    private var watchlistBtn:TextView? = null
    private var progressBar: DefaultTimeBar? = null
    private var totalDurationTV: TextView? = null
    private var currentPositionTV: TextView? = null
    private var loaderIV: ImageView? = null
    private var clContainer: View? = null
    private var playerBottomMenu: View? = null
    private var zoomBtn: ImageView? = null
    private var middleView: View? = null
    private var mainContainer: View? = null
    private var playerViewHungama: HungamaPlayerView? = null
    private var playerView: TtnPlayerView? = null
    private val mHandler = Handler()
    private var titleTv:TextView? = null
    var isTrailerStarted:Boolean = false
    private var mZoomIn = false
    private var pausedDuration:Long? = 0L
    private var controllerHidingTime = 5000L

    private var provider : String? = null
    private var sharedPrefs : PrefsRepo? = null

    private val controlsHidder = Runnable {
        clContainer?.hide()
        miniProgressBar?.setScrubberColor(
            ContextCompat.getColor(
                context,
                R.color.transparent
            )
        )
    }

    constructor(context: Context) : super(context) {
        //init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        //init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        //init()
    }

    var isInit = false
    val networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val activity: Activity = context as Activity
            activity.runOnUiThread(Runnable {
                e("TrailerView","networkCallback isAutoPlayOn isTrailerStarted:$isTrailerStarted")
                if(isTrailerStarted && ::mListener.isInitialized) {
                    hideNetworkAlert()
                }
            })
        }
    }
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val builder = NetworkRequest.Builder()

    fun init(provider: String, cookies : String?) {
        this.cookies = cookies
        this.provider = provider
//        removeAllViews()
        if (PROVIDER_HUNGAMA.equals(provider, true)) {
            val inflateView = View.inflate(context, R.layout.layout_trailer_hungama, this)
            isHungamaTrailer = true
            playUrl = "4948932"
            var frameLayout = inflateView.findViewById<ConstraintLayout>(R.id.player_controller)
            clContainer = frameLayout
            replayBtn = frameLayout.findViewById(R.id.llReplay)
            mainContainer = frameLayout.findViewById(R.id.mainController)
            playerViewHungama = inflateView.findViewById(R.id.player_view_hungama)
            progressBar = frameLayout.findViewById(R.id.exo_progress)
            soundCheckBox = frameLayout.findViewById(R.id.exo_sound_cb)
            fullScreenCheckBox = frameLayout.findViewById(R.id.exo_fullscreen)
            currentPositionTV = frameLayout.findViewById(R.id.exo_position)
            totalDurationTV = frameLayout.findViewById(R.id.exo_duration)
            playBtn = frameLayout.findViewById(R.id.exo_play)
            pauseBtn = frameLayout.findViewById(R.id.exo_pause)
            ffwdBtn = frameLayout.findViewById(R.id.tv_ffwd)
            rewBtn = frameLayout.findViewById(R.id.tv_rew)
            middleView = frameLayout.findViewById(R.id.rl_play_pause)
            playerBottomMenu = frameLayout.findViewById(R.id.player_bottom_menu)
            zoomBtn = frameLayout.findViewById(R.id.zoom_btn)
            watchlistBtn = frameLayout.findViewById(R.id.tv_add_to_watchlist)
            titleTv = frameLayout.findViewById(R.id.tv_title)
            loaderIV = inflateView.findViewById(R.id.ui_loader)
            netWorkView = inflateView.findViewById(R.id.network_error)
            progressBar?.invisible()
            totalDurationTV?.invisible()
            currentPositionTV?.invisible()

            mainContainer?.hide()
            replayBtn?.hide()

            playBtn?.tag = PLAY_TAG
            setClickListener(playBtn)

            pauseBtn?.tag = PLAY_TAG
            setClickListener(pauseBtn)
        }
        else {
            val inflateView = View.inflate(context, R.layout.layout_trailer, this)
            playUrl =
                "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"
            replayBtn = inflateView.findViewById(R.id.llReplay)
            mainContainer = inflateView.findViewById(R.id.mainController)
            playerView = inflateView.findViewById(R.id.player_view)
            playerView?.controllerAutoShow = false
            progressBar = inflateView.findViewById(R.id.exo_progress)
            soundCheckBox = inflateView.findViewById(R.id.exo_sound_cb)
            fullScreenCheckBox = inflateView.findViewById(R.id.exo_fullscreen)
            currentPositionTV = inflateView.findViewById(R.id.exo_current_position)
            totalDurationTV = inflateView.findViewById(R.id.exo_duration)
            playBtn = inflateView.findViewById(R.id.exo_play)
            pauseBtn = inflateView.findViewById(R.id.exo_pause)
            loaderIV = inflateView.findViewById(R.id.ui_loader)
            ffwdBtn = inflateView.findViewById(R.id.tv_ffwd)
            middleView = inflateView.findViewById(R.id.rl_play_pause)
            playerBottomMenu = inflateView.findViewById(R.id.player_bottom_menu)
            titleTv = inflateView.findViewById(R.id.tv_title)
            zoomBtn = inflateView.findViewById(R.id.zoom_btn)
            watchlistBtn = inflateView.findViewById(R.id.tv_add_to_watchlist)
            rewBtn = inflateView.findViewById(R.id.tv_rew)
            netWorkView = inflateView.findViewById(R.id.network_error)
        }

        //connectivityManager.registerNetworkCallback(builder.build(), networkCallback)

        if(PROVIDER_EROSNOW.equals(provider, ignoreCase = true)){

            playBtn?.tag = PLAY_TAG
            setClickListener(playBtn)

            pauseBtn?.tag = PLAY_TAG
            setClickListener(pauseBtn)
        }
        ffwdBtn?.tag = FWD_TAG
        setClickListener(ffwdBtn)

        rewBtn?.tag = REW_TAG
        setClickListener(rewBtn)

        replayBtn?.tag = REPLAY_TAG
        setClickListener(replayBtn)

        zoomBtn?.tag = ZOOM_TAG
        setClickListener(zoomBtn)

        fullScreenCheckBox?.setOnCheckedChangeListener { button, isChecked ->
            mListener.switchToFullScreen(isChecked)
        }

        soundCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->
            mListener.onTrailerSoundChanged(isChecked)
            if (isChecked) {
                mSoundOn = true
                mPlayer?.volume = defaultVolume
                if (isHungamaTrailer)
                    HungamaPlayerManager.getInstance().a.i?.volume = defaultVolume
            } else {
                mSoundOn = false
                defaultVolume = mPlayer?.volume ?: 0f
                mPlayer?.volume = 0f
                if (isHungamaTrailer) {
                    defaultVolume = HungamaPlayerManager.getInstance().a.i?.volume ?: 0f
                    HungamaPlayerManager.getInstance().a.i?.volume = 0f
                }
            }
        }


        watchlistBtn?.setOnClickListener{
            mListener.addToWatchList()
        }

        netWorkView?.findViewById<Button>(R.id.btn_settings)?.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(context,intent,null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        netWorkView?.findViewById<Button>(R.id.btn_retry)?.setOnClickListener {
            if(mListener.getNetWorkStatus()) {
                hideNetworkAlert()
            }
        }

        var myPinchdetector = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private var scaleFactor = 0f

            override fun onScale(
                detector: ScaleGestureDetector
            ): Boolean {
                scaleFactor = detector.scaleFactor
                return true
            }

            override fun onScaleBegin(
                detector: ScaleGestureDetector
            ) : Boolean{
                return true
            }
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                if(scaleFactor == 1.0f){
                    if(mZoomIn)
                        zoomOut()
                    else
                        zoomIn()
                }
                else {
                    if (scaleFactor > 1) {
                        zoomIn()
                    } else {
                        zoomOut()
                    }
                }
            }
        }
        isInit = true
    }

    fun setWatchlisted(isWatchlisted:Boolean){
        try {
            if (isWatchlisted) {
                watchlistBtn?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pi_watchlist_selected, 0, 0, 0)
                watchlistBtn?.text = context.getString(R.string.add_to_watchlist)
            } else if (!isWatchlisted) {
                watchlistBtn?.text = context.getString(R.string.add_to_watchlist)
                watchlistBtn
                    ?.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_pi_watchlist_unselected,
                        0,
                        0,
                        0
                    )
            }
        }catch (e:Exception){}
    }

    fun unregisterTrailerNetworkCallback(){
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {

        }
    }

    fun registerTrailerNetworkCallback(){
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
    }

    fun startTrailer(
        trailerUrl: String?,
        listener: ITrailerListener,
        soundOn: Boolean,
        seekBar: TimeBarCustom?,
        drmLicenseUrl : String?,
        sharedPrefs : PrefsRepo?
    ) {
        this.sharedPrefs = sharedPrefs
        isReleasePlayer = false
        isFirstTime = true
        isPausedTrailer = false
        if (isAlreadyStarted) return
        isAlreadyStarted = true
        mListener = listener
        mSoundOn = soundOn
        playerView?.show()
        playerViewHungama?.show()
        miniProgressBar?.setPosition(0L)
        titleTv?.text = mListener.getContentTitle() + " Trailer"
        titleTv?.show()
        isTrailerStarted = true
        netWorkView?.hide()
        playerViewHungama?.isClickable = true
        playerView?.useController = true
        ttnPlayerHelper?.showUiControls()
        miniProgressBar?.enable()
        miniProgressBar?.show()
        this.drmLicenseUrl = drmLicenseUrl
        e("TrailerView","isAutoPlayOn:$isAutoPlayOn")
        if (isHungamaTrailer && mPlaybackController == null) {
            playUrl = trailerUrl ?: playUrl
            val contentVO = ContentVO(
                playUrl,
                "",
                ContentType.MOVIE
                //getHungamaContentType(playerModel?.getContentType())
            )
            try {
                mPlaybackController = HungamaPlayerManager.getInstance()
                HungamaPlayerManager.getInstance().loadContent(contentVO, this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            playerViewHungama?.b?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        }
        else if(PROVIDER_EROSNOW.equals(provider, true)) {
            playUrl = trailerUrl ?: "7004461"
            if(ENSDK.getLoggedIn()) {
                try {
                    ENSDK.contentProfile(
                        context,
                        playUrl ?: "7004586",
                        erosInitializeListener
                    )
                } catch (e: Exception) {
                    d("TrailerView", e.printStackTrace().toString())
                    d("TrailerView", e.message!!)
                }
            }
            else{
                erosnowLogin(
                    context,
                    object : EnLoginListener {
                        override fun onError(error: ENError) {
                            e("ErosNowListener","onerror : ${error.message}")
                            mListener.showHideLoader(false)
                            onDestroyView()
                        }

                        override fun onSuccess() {
                            ENSDK.contentProfile(
                                context,
                                playUrl ?: "7004586",
                                erosInitializeListener
                            )
                        }
                    },
                    sharedPrefs?.getDsn()?:"",
                    sharedPrefs?.getDeviceToken()?:""
                )
            }

            playerView?.setControllerVisibilityListener { visibility ->
                if (visibility == View.GONE) {
                    miniProgressBar?.setScrubberColor(
                        ContextCompat.getColor(
                            context,
                            R.color.transparent
                        )
                    )
                    mListener.onControllerHidden()
                } else {
                    miniProgressBar?.setScrubberColor(
                        ContextCompat.getColor(
                            context,
                            R.color.darkAccent
                        )
                    )
                    mListener.onControllerShown()
                }
            }
        }
        else if (!isHungamaTrailer && mPlayer == null) {
            mainContainer?.show()
            replayBtn?.hide()
            if (!trailerUrl.isNullOrBlank())
                playUrl = trailerUrl
            e("TrailerView","init ttnplayer isAutoPlayOn:$isAutoPlayOn")
            ttnPlayerHelper = TtnPlayerHelper.Builder(context,
                playerView!!,
                cookies,
                playbackQualityRestrictionsEnabled =
                provider?.equals(PROVIDER_CHAUPAL, true) == true
            )
                .setRepeatModeOn(false)
                .setAutoPlayOn(isAutoPlayOn)
                .setDrmLicenseUrl(drmLicenseUrl)
                .addSavedInstanceState(null)
                .setUiControllersVisibility(true)
                .setVideoUrls(arrayListOf(playUrl))
                .setTtnPlayerEventsListener(this)
                .setOverrideFullScreenButtonFunctionality(false)
                .setOverLayOnVideoPause(true)
                .createAndPrepareTrailer(playUrl)
            ttnPlayerListener = this
            mPlayer = ttnPlayerHelper?.getPlayer()
            playerDurationWatcher = PlayerDurationWatcher(mPlayer!!, this)
            defaultVolume = mPlayer?.volume ?: 0f
            startProgressing(true)
            if (!soundOn)
                mPlayer?.volume = 0f
            progressBar?.addListener(timeBarListener)

            playerView?.setControllerVisibilityListener { visibility ->
                if (visibility == View.GONE) {
                    miniProgressBar?.setScrubberColor(
                        ContextCompat.getColor(
                            context,
                            R.color.transparent
                        )
                    )
                    mListener.onControllerHidden()
                } else {
                    miniProgressBar?.setScrubberColor(
                        ContextCompat.getColor(
                            context,
                            R.color.darkAccent
                        )
                    )
                    mListener.onControllerShown()
                }
            }
        }
        soundCheckBox?.isChecked = soundOn
        miniProgressBar = seekBar
    }

    fun startPlayingTrailer() {
        registerTrailerNetworkCallback()
        isPausedTrailer = false
        if(!mIsInFullScreen)
            miniProgressBar?.show()
        if (isHungamaTrailer) {
            if (HungamaPlayerManager.getInstance().a.i == null) {
                e("TrailerView","hungama set isAutoPlayOn:$isAutoPlayOn")
                isAutoPlayOn = true
            } else {
                if(!isAutoPlayOn)
                    initProbeSDK(HungamaPlayerManager.getInstance().a.i)
                HungamaPlayerManager.getInstance().a.i.playWhenReady = true
            }
        } else {
            //ttnPlayerHelper?.playerPlay()
            initProbeSDK(mPlayer)
            mPlayer?.playWhenReady = true
        }
    }

    fun hideNetworkAlert() {
        isTrailerStarted = true
        netWorkView?.hide()
        playerViewHungama?.isClickable = true
        playerView?.useController = true
        ttnPlayerHelper?.showUiControls()
        miniProgressBar?.enable()

        mHandler.post {
            e("TrailerView","init handler isAutoPlayOn:$isAutoPlayOn")
            isAutoPlayOn = true
            startTrailer(playUrl, mListener, mSoundOn, miniProgressBar!!, drmLicenseUrl, sharedPrefs)
            mPlayer?.seekTo(pausedDuration!!)
            miniProgressBar?.setPosition(pausedDuration!!)
            ttnPlayerHelper?.playerPlay()
        }
    }

    private fun showNetworkAlert() {
        netWorkView?.show()
        clContainer?.hide()
        playerViewHungama?.isClickable = false
        startProgressing(false)
        playerView?.useController = false
        miniProgressBar?.disable()
        releasePlayer()
    }

    val timeBarListener = object :
        TimeBar.OnScrubListener {
        override fun onScrubMove(timeBar: TimeBar, position: Long) {
            // Percentage of Progress of width
            movePositionOfPlayerScrubber(position)
        }

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
            isAutoPlayOn = false
            movePositionOfPlayerScrubber(position)
            if(!isPausedTrailer) {
                isAutoPlayOn = true
                pauseTrailer(false)
            }
            if (isHungamaTrailer) {
                mHandler.removeCallbacks(controlsHidder)
                clContainer?.show()
//                currentPositionTV?.show()
            } else
                currentPositionTV?.show()
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            if(!isPausedTrailer || isAutoPlayOn) {
                isAutoPlayOn = false
                resumeTrailer()
            }
            if (isHungamaTrailer) {
                progressBar?.setPosition(position)
                mHandler.postDelayed(controlsHidder, controllerHidingTime)
                HungamaPlayerManager.getInstance().seekTo(position)
                currentPositionTV?.invisible()
            } else
                currentPositionTV?.invisible()
        }
    }

    fun zoomIn() {
        mZoomIn = true
        if (isHungamaTrailer) {
            playerViewHungama?.b?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
        zoomBtn?.setImageResource(R.drawable.ic_zoom_out)
    }

    fun zoomOut() {
        mZoomIn = false
        if (isHungamaTrailer) {
            playerViewHungama?.b?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        } else {
            playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        zoomBtn?.setImageResource(R.drawable.ic_zoom_in)
    }

    private fun movePositionOfPlayerScrubber(position: Long) {
        var l =
            (position.toFloat() / (mPlayer?.duration ?: Long.MAX_VALUE))
        if (isHungamaTrailer) {
            l = (position.toFloat() / (totalLength))

        }
        currentPositionTV?.text = Util.getStringForTime(
            formatBuilder, Formatter(formatBuilder, Locale.getDefault()), position
        )

        // position of thumb
        val l1 = l * (progressBar!!.width)
        val xOfExoPosition = (progressBar!!.x + l1) - (currentPositionTV!!.width / 2)
        currentPositionTV!!.x =
            if (xOfExoPosition < progressBar!!.x)
                progressBar!!.x
            else if (xOfExoPosition > ((progressBar!!.x + progressBar!!.width) - currentPositionTV!!.width))
                ((progressBar!!.x + progressBar!!.width) - currentPositionTV!!.width)
            else
                xOfExoPosition
//        if(isHungamaTrailer){
        currentPositionTV!!.y = (progressBar!!.y - currentPositionTV!!.height * 1.25).toFloat()
//        }
        currentPositionTV?.show()
    }

    private fun invalidateAndHideControls() {
        mHandler.removeCallbacks(controlsHidder)
        clContainer?.hide()
        miniProgressBar?.setScrubberColor(
            ContextCompat.getColor(
                context,
                R.color.transparent
            )
        )
    }

    private fun invalidateAndShowControls() {
        if (clContainer?.isVisibile() == true) {
            invalidateAndHideControls()
            mListener.onControllerHidden()
        } else {
            mHandler.removeCallbacks(controlsHidder)
            clContainer?.show()
            miniProgressBar?.setScrubberColor(
                ContextCompat.getColor(
                    context,
                    R.color.darkAccent
                )
            )
            mHandler.postDelayed(controlsHidder, controllerHidingTime)
            mListener.onControllerShown()
        }
    }

    private fun updateProgressBar(position: Long, bufferedPosition: Long) {
        miniProgressBar?.setPosition(position)
        miniProgressBar?.setBufferedPosition(bufferedPosition)
    }

    private fun scheduleProgress(position: Long) {
        // Schedule an update if necessary.
        val playbackState = if (mPlayer == null) Player.STATE_IDLE else mPlayer!!.playbackState
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            var delayMs = 0L;
            if (mPlayer!!.playWhenReady && playbackState == Player.STATE_READY) {
                delayMs = 1000 - (position % 1000)
                if (delayMs < 200) {
                    delayMs += 1000
                }
            } else {
                delayMs = 1000
            }
            handler?.postDelayed(updateProgressAction, delayMs)
        }
        // Remove scheduled updates.
        handler?.removeCallbacks(updateProgressAction)
    }

    private val updateProgressAction: Runnable = Runnable {
        if (isVisible) {
            val position = if (mPlayer == null) 0L else mPlayer!!.currentPosition
            val bufferedPosition = if (mPlayer == null) 0L else mPlayer!!.bufferedPosition
            updateProgressBar(position, bufferedPosition)
            scheduleProgress(position)
        }
    }

    fun switchToFullScreen() {
        playerBottomMenu?.show()
        titleTv?.show()
        zoomBtn?.show()
        if (!mIsInFullScreen) {
            mIsInFullScreen = true
            fullScreenCheckBox?.isChecked = true
            progressBar?.show()
            totalDurationTV?.show()
            if (zoomed)
                zoomIn()
            else
                zoomOut()
        }
    }

    fun switchToMiniScreen() {
//        titleTv?.hide()
        zoomBtn?.invisible()
        playerBottomMenu?.hide()
        if (mIsInFullScreen) {
            mIsInFullScreen = false
            zoomOut()
            fullScreenCheckBox?.isChecked = false
            progressBar?.invisible()
            totalDurationTV?.invisible()
        }
    }
    fun trailerStarted(flag:Boolean){
        isTrailerStarted = flag
    }

    fun onDestroyView() {
//        super.onDestroyView()
        isFirstTime = true
        handler?.removeCallbacks(updateProgressAction)
        playerDurationWatcher?.stop()
        miniProgressBar?.hide()
        playerView?.hide()
        playerViewHungama?.hide()
        releasePlayer()
//        releaseMediaDrm()
    }

    fun pauseTrailer(isMoveToPlayer: Boolean) {
        if (isHungamaTrailer) {
            if (isMoveToPlayer) {
                isReleasePlayer = true
                miniProgressBar?.setPosition(totalLength)
                playerEnded()
                mPlaybackController = null
            }
            mHandler.removeCallbacks(controlsHidder)
//            clContainer?.show()
            try {
                if (HungamaPlayerManager.getInstance().isPlaying) {
                    HungamaPlayerManager.getInstance().togglePlayPause()
                    mListener.onPlayerPause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            e("TrailerView", "inside pauseTrailer : $isPausedTrailer")
            if(mPlayer?.isPlaying() == true)
                mPlayer?.playWhenReady = false
            playerView?.controllerAutoShow = true
            ttnPlayerHelper?.showUiControls()
//            ttnPlayerHelper?.onActivityPause()
        }
        isPausedTrailer = true
    }

    fun resumeTrailer() {
        e("TrailerView", "inside resumeTrailer : $isPausedTrailer")
        isPausedTrailer = false
//        ttnPlayerHelper?.playerPlay()
        mPlayer?.playWhenReady = true
        if (isHungamaTrailer) {
            if (!HungamaPlayerManager.getInstance().isPlaying)
                HungamaPlayerManager.getInstance().togglePlayPause()
            mHandler.postDelayed(controlsHidder, controllerHidingTime)
        } else {
            playerView?.controllerAutoShow = false
        }
    }

    private fun releasePlayer() {
        unregisterTrailerNetworkCallback()
        isAlreadyStarted = false
        playerDurationWatcher?.stop()
        if (mPlaybackController != null) {
            try {
                HungamaPlayerManager.getInstance().setWakeMode(C.WAKE_MODE_NONE)
                HungamaPlayerManager.getInstance().stop()
                probePlayerEventStopped()
                HungamaPlayerManager.getInstance().releasePlayer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mPlaybackController = null
        }
        if(mPlayer != null)
            probePlayerEventStopped()
        mPlayer?.release()
        mPlayer=null
        ttnPlayerHelper?.releasePlayer()
//        try {
//            connectivityManager.unregisterNetworkCallback(networkCallback)
//        }catch (e:Exception){
//
//        }
        ttnPlayerHelper = null
    }

    val miniTimeBarListener = object :
        TimeBar.OnScrubListener {
        override fun onScrubMove(timeBar: TimeBar, position: Long) {
            moveScrubberOfDetailPage(position)

        }

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
//            moveScrubberOfDetailPage(position)
            isAutoPlayOn = false
            if(!isPausedTrailer) {
                isAutoPlayOn = true
                pauseTrailer(false)
            }
            if (!isHungamaTrailer) {
                playerView?.showController()
//                findViewById<TextView>(R.id.exo_position).show()
            } else {
                mHandler.removeCallbacks(controlsHidder)
                clContainer?.show()
                currentPositionTV?.show()
            }
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            if(!isPausedTrailer || isAutoPlayOn){
                isAutoPlayOn = false
                resumeTrailer()
            }
            miniProgressBar?.setPosition(position)
            if (isHungamaTrailer) {
                mHandler.postDelayed(controlsHidder, controllerHidingTime)
                HungamaPlayerManager.getInstance().seekTo(position)
                currentPositionTV?.invisible()
            } else {
                currentPositionTV?.invisible()
                mPlayer?.seekTo(position)
            }
        }
    }

    private fun moveScrubberOfDetailPage(position: Long) {
        // Percentage of Progress of width
        var exoPosition = currentPositionTV!!
        exoPosition.text = Util.getStringForTime(
            formatBuilder, Formatter(formatBuilder, Locale.getDefault()), position
        )
        var l =
            (position.toFloat() / (mPlayer?.duration ?: Long.MAX_VALUE))
        if (isHungamaTrailer) {
            l =
                (position.toFloat() / (totalLength))
        }
        miniProgressBar?.let {

            // position of thumb
            val l1 = l * (it.width)
            val xOfExoPosition =
                ((it.x) + l1) - (exoPosition.width / 2)
            exoPosition.x =
                if (xOfExoPosition < (it.x))
                    (it.x)
                else if (xOfExoPosition > ((it.x + it.width) - exoPosition.width))
                    ((it.x + it.width) - exoPosition.width)
                else
                    xOfExoPosition
            val yOfExoPosition = (it.y) - exoPosition.height * 1.25
            if (yOfExoPosition < (it.y))
                (it.y)
            else
                yOfExoPosition
            exoPosition.y = yOfExoPosition.toFloat()
        }
        exoPosition.show()
    }


    private fun onPlayerFailure() {
        releasePlayer()
        startProgressing(false)
        miniProgressBar?.hide()
        mListener.onPlayerFailure()
    }

    fun playerEnded() {
        releasePlayer()
        startProgressing(false)
        if (isHungamaTrailer) {
            mainContainer?.hide()
            replayBtn?.show()
//            playerViewHungama?.removeAllViews()
            mHandler.removeCallbacks(controlsHidder)
            clContainer?.show()
        } else {
            mainContainer?.hide()
            replayBtn?.show()
            playerView?.controllerAutoShow = true
            ttnPlayerHelper?.showUiControls()
        }
        miniProgressBar?.hide()
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
    }

    private fun setClickListener(view: View?) {
        view?.setOnClickListener { view ->
            val `object` = view.tag
            if (`object` is String) {
                val tag = `object`
                if (tag.equals(FWD_TAG, true)) {
                    if(isHungamaTrailer &&
                        (HungamaPlayerManager.getInstance().currentPosition + ffwdBckTime) < HungamaPlayerManager.getInstance().totalDuration) {
                        HungamaPlayerManager.getInstance()
                            .seekTo(HungamaPlayerManager.getInstance().currentPosition + ffwdBckTime)
                        /*ttnPlayerHelper?.seekTo(0, min((ttnPlayerHelper?.currentPosition ?: 0) + ffwdBckTime,
                            ttnPlayerHelper?.duration ?: 0))*/
                        e("FWD_TAG", "mPlayer?.currentPosition:${mPlayer?.currentPosition}")
                        e("FWD_TAG", "mPlayer?.duration:${mPlayer?.duration}")

                    }
                    mPlayer?.seekTo(
                        0,
                        min(
                            (mPlayer?.currentPosition ?: 0) + ffwdBckTime,
                            mPlayer?.duration ?: 0
                        )
                    )
                } else if (tag.equals(REW_TAG, true)) {
                    HungamaPlayerManager.getInstance().seekTo(HungamaPlayerManager.getInstance().currentPosition - ffwdBckTime)
                    mPlayer?.seekTo(0, max(0, (mPlayer?.currentPosition ?: 0) - ffwdBckTime))
                } else if (tag.equals(PLAY_TAG, true)) {
                    isPausedTrailer = HungamaPlayerManager.getInstance().isPlaying
                    HungamaPlayerManager.getInstance().togglePlayPause()
                    mPlayer?.let {
                        it.playWhenReady = it.isPlaying != true
                    }
                } else if (tag.equals(REPLAY_TAG, ignoreCase = true)) {
                    isAutoPlayOn = true
                    startTrailer(playUrl, mListener, mSoundOn, miniProgressBar!!, drmLicenseUrl, sharedPrefs)
                }
                else if(tag.equals(ZOOM_TAG, ignoreCase = true)){
                    when(playerView?.resizeMode){
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> zoomOut()
                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> zoomIn()
                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> zoomOut()
                        else -> zoomOut()
                    }
                }
                mHandler.postDelayed(controlsHidder, controllerHidingTime)
            }
        }
    }

    private fun startProgressing(shouldProgress: Boolean) {
        loaderIV?.startProgressAvd(shouldProgress)
        if (shouldProgress) {
            middleView?.invisible()
        } else {
            middleView?.show()
        }
    }

    override fun onTimeChanged(currentTime: Long, totalTime: Long) {
        val bufferedPosition = if (mPlayer == null) 0L else mPlayer!!.bufferedPosition
        if (currentTime < ffwdBckTime) {
            rewBtn?.hide()
        } else {
            rewBtn?.show()
        }
        ffwdBtn?.show()
        updateProgressBar(currentTime, bufferedPosition)
        //scheduleProgress(position)
        if (isHungamaTrailer) {
            this.progress = currentTime
//            updateProgressBar(progress, progress)
            progressBar?.setPosition(progress)
            val remainingTime = totalLength - progress
            if (remainingTime != totalLength)
                totalDurationTV?.text =
                    Util.getStringForTime(
                        formatBuilder,
                        Formatter(formatBuilder, Locale.getDefault()),
                        totalLength
                    )
        }
    }


    override fun onContentLoadFailed(p0: String?) {
        mListener.showHideLoader(false)
        onDestroyView()
    }

    override fun onContentLoadSuccess() {
        //hideLoader() // hide detail fragment laoder
        e("TrailerView", "onContentLoadSuccess() $isReleasePlayer")
        if (isReleasePlayer) return

        mListener.showHideLoader(false)
        clContainer?.hide()
        mainContainer?.show()
        replayBtn?.hide()
        startProgressing(true)
        mHandler.postDelayed(controlsHidder, controllerHidingTime)

        playerViewHungama?.setOnClickListener {
            invalidateAndShowControls()
        }
        startPlayback()
    }

    private fun startPlayback() {
        try {
            e("TrailerView","hungama startplayback isAutoPlayOn:$isAutoPlayOn")
            HungamaPlayerManager.getInstance().initializePlayer()
            HungamaPlayerManager.getInstance().preparePlayer(playerViewHungama, this)
            if(isAutoPlayOn){
                initProbeSDK(HungamaPlayerManager.getInstance().a.i)
            }
            HungamaPlayerManager.getInstance().start()
            HungamaPlayerManager.getInstance().a.i.playWhenReady = isAutoPlayOn

            defaultVolume = HungamaPlayerManager.getInstance().a.i?.volume ?: 0f

            if (!mSoundOn)
                HungamaPlayerManager.getInstance().a.i?.volume = 0f
            HungamaPlayerManager.getInstance().setWakeMode(C.WAKE_MODE_NETWORK)
            playerDurationWatcher = PlayerDurationWatcher(
                HungamaPlayerManager.getInstance().a.i,
                this
            )
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    private fun initProbeSDK(player: SimpleExoPlayer?) {
        if(sharedPrefs?.getLoginStatus() == true) {
            if(!isHungamaTrailer)
                playerModel?.setPlaybackUrl(playUrl)
            sharedPrefs?.getOriginalSubscriberId()?.let {
                probePlayerEventInitSdk(
                    player, playerModel,
                    bandWidthMeter = DefaultBandwidthMeter(), it
                )
                probePlayerEventPlayClicked()
            }
        }
    }

    override fun onLoadingStatusChanged(isLoading: Boolean, bufferedPosition: Long, bufferedPercentage: Int) {
    }

    override fun onPlayerPlaying(currentWindowIndex: Int) {
        startProgressing(false)

        e("TrailerView", "inside onPlayerPlaying : $isPausedTrailer")
        if(isPausedTrailer){
            mPlayer?.playWhenReady = false
            isPausedTrailer=false
        }
        if (isFirstTime) {
            isFirstTime = false
            miniProgressBar?.setDuration(mPlayer!!.duration)
            miniProgressBar?.removeListener(miniTimeBarListener)
            miniProgressBar?.addListener(miniTimeBarListener)
            playerDurationWatcher?.start()
            mListener.onPlayerReady()
        }
    }

    override fun onPlayerPaused(currentWindowIndex: Int) {
        startProgressing(false)
        mListener.onPlayerPause()
    }

    override fun onPlayerBuffering(currentWindowIndex: Int) {
        startProgressing(true)
    }

    override fun onBufferStart() {
    }

    override fun onBufferEnd() {
        startProgressing(false)
    }

    override fun onPlayerStateEnded(currentWindowIndex: Int) {
        mListener.onPlayerEnded()
        playerEnded()
    }

    override fun onPlayerStateIdle(currentWindowIndex: Int) {
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        if (error.cause is HttpDataSource.HttpDataSourceException) {
            pausedDuration = mPlayer?.currentPosition
            showNetworkAlert()
        }
        else
            onPlayerFailure()
    }

    override fun onTTNPlayerError(error: ExoPlaybackException?) {
        if (error?.cause is HttpDataSource.HttpDataSourceException) {
            pausedDuration = mPlayer?.currentPosition
//            showNetworkAlert()
            onPlayerFailure()
        }
        else
            onPlayerFailure()
    }

    override fun createTtnPlayerCalled(isToPrepare: Boolean) {

    }

    override fun releaseTtnPlayerCalled() {
    }

    override fun onVideoResumeDataLoaded(window: Int, position: Long, isResumeWhenReady: Boolean) {
    }

    override fun onMuteStateChanged(isMuted: Boolean) {
        super.onMuteStateChanged(isMuted)
    }

    override fun onPlayBtnTap(): Boolean {
        isPausedTrailer = false
        mPlayer?.playWhenReady = true
        return false
    }

    override fun onPauseBtnTap(): Boolean {
        isPausedTrailer = true
        mPlayer?.playWhenReady = false
        return false
    }

    override fun onFullScreenBtnTap() {
    }

    override fun onPlayerUiControlVisibilityChange(visibility: Int) {
        super.onPlayerUiControlVisibilityChange(visibility)
    }

    override fun onScrubStart(timeBar: TimeBar, position: Long) {
        super.onScrubStart(timeBar, position)
    }

    override fun onScrubMove(timeBar: TimeBar, position: Long) {
        super.onScrubMove(timeBar, position)
    }

    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
        super.onScrubStop(timeBar, position, canceled)
    }

    override fun onDrmSessionAcquired() {
    }

    override fun onDrmSessionManagerError(error: Exception) {
    }

    override fun toggleWatchlisted() {
        mListener.addToWatchList()
    }

    override fun onPlayerError(p0: PlaybackException?) {
        if (p0?.cause is HttpDataSource.HttpDataSourceException) {
            pausedDuration = mPlaybackController?.currentPosition
            showNetworkAlert()
        }
        mListener.showHideLoader(false)
    }

    override fun onPlayerStateChanged(p0: PlayerState?) {
        e("TrailerView","inside onPlayerStateChanged $p0")
        val activity: Activity = context as Activity
        activity.runOnUiThread(Runnable {
            if (!HungamaPlayerManager.getInstance().isPlaying) {
                pauseBtn?.hide()
                playBtn?.show()
            } else {
                playBtn?.hide()
                pauseBtn?.show()
            }
        })
        when {
            p0 == PlayerState.BUFFERING -> {
                startProgressing(true)
            }
            p0 == PlayerState.ENDED -> {
                mListener.onPlayerEnded()
                playerEnded()
            }
            p0 == PlayerState.READY -> {
                startProgressing(false)
                if(isHungamaTrailer && HungamaPlayerManager.getInstance().a.i.playWhenReady) {
                    if (isFirstTime) {
                        isFirstTime = false
                        if (isHungamaTrailer) {
                            totalLength = HungamaPlayerManager.getInstance().a.i?.duration ?: 0
                            miniProgressBar?.setDuration(totalLength)
                            progressBar?.setDuration(totalLength)
                            progressBar?.addListener(timeBarListener)
                            if (HungamaPlayerManager.getInstance().isPlaying && isPausedTrailer) {
                                HungamaPlayerManager.getInstance().togglePlayPause()
                            }
                        }
                        miniProgressBar?.removeListener(miniTimeBarListener)
                        miniProgressBar?.addListener(miniTimeBarListener)

                        playerDurationWatcher?.start()
                    }
                    if (pausedDuration!! > 0) {
                        HungamaPlayerManager.getInstance().seekTo(pausedDuration!!)
                        pausedDuration = 0
                    }
                    mListener.onPlayerReady()
                }
            }
        }
    }

    fun onPause() {
        ttnPlayerHelper?.let {
            playerDurationWatcher?.stop()
            it.onActivityPause()
        }
        mPlayer?.playWhenReady = false
    }

    /*Erosnow implementation*/


    val erosInitializeListener = object  : ErosPlayActionListener {

        /*Content Load Error*/
        override fun onError(error: ENError) {
            e("TrailerView", "inside onError onloading cotnent")
            mListener.showHideLoader(false)
            onDestroyView()
        }

        /*Success on content load*/
        override fun onSuccess(playerData: PlayerData) {
            e("TrailerView", "onContentLoadSuccess() $isReleasePlayer")
            if (isReleasePlayer) return
            mListener.showHideLoader(false)
            startProgressing(true)
            createPlayer()
            mainContainer?.show()
            replayBtn?.hide()

            e("ENSDK", "playerData:$playerData,")
            ENSDK.initializePlayer(
                ENPlaybackAssetInfo(
                    playerData.stream_url,
                    playerData.asset_id,
                    playerData.asset_title,
                    playerData.content_id
                ), mPlayer!!
            )

            val dataSourceFactory = buildDataSourceFactory(context)

            val mPreRollURI =
                Uri.parse("https://originalvideohls-a.erosnow.com/hls/original/1/1056821/original/6900474/1056821_6900474_latest_IPAD_ALL_multi.m3u8")
            val preRollMediaSource =
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mPreRollURI)
            val uri = Uri.parse(playerData.stream_url)
            val contentMediaSource =
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            val mergingMediaSource = MergingMediaSource(contentMediaSource)
            val concatenatingMediaSource =
                ConcatenatingMediaSource(preRollMediaSource, mergingMediaSource)
            mPlayer?.prepare(mergingMediaSource)
            e("TrailerView","create Player isAutoPlayOn:$isAutoPlayOn")
            mPlayer?.playWhenReady = isAutoPlayOn
        }
    }

    fun createPlayer() {
        if (mPlayer != null) {
            return
        }
        //load control
        val builder = DefaultLoadControl.Builder()
        builder.setAllocator(DefaultAllocator(true, 2 * 1024 * 1024))
        builder.setBufferDurationsMs(
            30000, 120000,
            15000, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
        )
        builder.setPrioritizeTimeOverSizeThresholds(false)
        val mLoadControl: DefaultLoadControl = builder.createDefaultLoadControl()
        mPlayer = SimpleExoPlayer.Builder(context)
            .setLoadControl(mLoadControl).build()
        mPlayer?.setHandleWakeLock(true)
        mPlayer?.setHandleAudioBecomingNoisy(true)
        //for handling audio focus
        val audioAttribute =
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MOVIE)
                .build()
        mPlayer?.setAudioAttributes(audioAttribute, true)
        playerView?.requestFocus()
        playerView?.player = mPlayer
        playerView?.controllerShowTimeoutMs = 3000
        playerView?.controllerAutoShow = false
        playerView?.controllerHideOnTouch = true
        defaultVolume = mPlayer?.volume!!
        if (!mSoundOn)
            mPlayer?.volume = 0f
        mPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        playerDurationWatcher = PlayerDurationWatcher(mPlayer!!, this)
        mPlayer?.addListener(this)
        /*mPlayer?.addListener(this)
        mPlayer?.addTextOutput(this)
        mPlayer?.addAnalyticsListener(EventLogger(trackSelector))
        playerView?.setPlaybackPreparer(this)*/
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        e("TrailerView",
            "onPlayerStateChanged playebals: $playbackState" +
                    " and playWhenReady:$playWhenReady"
        )
        mPlayer?.let {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (playWhenReady) {
                        playerView?.controllerAutoShow = false
                        playerView?.controllerHideOnTouch = true
                        onPlayerPlaying(it.currentWindowIndex)
                    } else {
                        playerView?.controllerAutoShow = true
                        playerView?.controllerHideOnTouch = false
                        onPlayerPaused(it.currentWindowIndex)
                    }
                }
                Player.STATE_BUFFERING -> {
                    onPlayerBuffering(it.currentWindowIndex)
                }
                Player.STATE_ENDED -> {
                    onPlayerStateEnded(it.currentWindowIndex)
                    playerView?.controllerAutoShow = true
                    playerView?.controllerHideOnTouch = false
                }
                Player.STATE_IDLE -> onPlayerStateIdle(mPlayer!!.currentWindowIndex)
                else -> e(
                    "TrailerView",
                    "onPlayerStateChanged unknown: $playbackState"
                )
            }
        }
    }
    fun playVideoWithUrl(playUrl: String) {
        ttnPlayerHelper?.playVideo(playUrl, null)
    }
    /*Need to add this for QoE Probe Mitigation*/
    fun setPlayerModel(id: String, contentType: String, provider: String?, title: String?) {
        playerModel = PlayerModel()
        playerModel?.setTitle(title?:"")
        playerModel?.setContentId(id)
        playerModel?.setProvider(provider ?: "")
        playerModel?.setContentType(contentType)
    }
}
