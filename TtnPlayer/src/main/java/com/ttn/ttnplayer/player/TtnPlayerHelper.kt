package com.ttn.ttnplayer.player

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.DefaultLoadControl.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.drm.DrmSessionManager.getDummyDrmSessionManager
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.text.TextOutput
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.probe.sdk.otherutils.ProbeInterface
import com.ttn.ttnplayer.R
import com.ttn.ttnplayer.listeners.*
import com.ttn.ttnplayer.ui.TtnPlayerView
import com.ttn.ttnplayer.ui.TtnTrackSelector
import com.ttn.ttnplayer.util.*
import com.ttn.ttnplayer.util.ProviderSpecificRestrictions.PROVIDER_CHAUPAL_RESTRICTIONS
import okhttp3.Call
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*
import kotlin.math.max

class TtnPlayerHelper constructor(context: Context) :
    View.OnClickListener, OnTouchListener,
    TtnPlayerControl,
    TtnPlayerStatus,
    Player.EventListener,
    VideoAdPlayer.VideoAdPlayerCallback,
    MediaSourceFactory,
    AdEvent.AdEventListener,
    PlaybackPreparer,
    TextOutput,
    DefaultDrmSessionEventListener
    , PlayerControlView.VisibilityListener, OrientationManager.OrientationChangeListener,
    TimeBar.OnScrubListener {

    private var playbackQualityRestrictionsEnabled: Boolean = false
    private var mAllCookieEnable: Boolean = false
    var mCookieValue : String? = null
    var isBuffering: Boolean = true
    private var formatBuilder= StringBuilder()
    private var enforceL1L3: Boolean = false
    private var securityLevel: String? = null
    private lateinit var bandwidthMeter: DefaultBandwidthMeter

    /**
     * flag to track playback ready state
     *
     * */
    private var mPlayBackStateLoaded: Boolean = false
    /**
     * flag to track dialog visibility
     *
     * */
    private var mDialogShowing: Boolean = false

    /**
     * DrmSessionManager for protected content
     *
     * */
    private var mDrmSessionManager: DefaultDrmSessionManager<ExoMediaCrypto>? = null
    /**
     *Orientation Manager for managing Sensor based orientation
     *
     * */
    private var orientationManager: OrientationManager? = null
    /**
     * Boolean flag to track should hide phone soft navigation and status bar
     *
     * */
    private var mHidePhoneNavigationAndStatusBar: Boolean = true
    /**
     * DefaultTimeBar to show time seeking for content
     *
     * */
    private var mDefaultTimeBar: DefaultTimeBar? = null

    private var mMiniTimeBar: TimeBar? = null
    /**
     * Boolean flag to track if stream is a live stream
     *
     * */
    private var isLiveStream: Boolean = false
    /**
     * Player default orientation
     *
     * */
    private var mOrientation: Int = DEFAULT_ORIENTATION

    /**
     * PlayerView initial height
     *
     * */
    private var mPlayerViewInitialHeight: Int? = null

    /**
     * Player control to play content
     *
     * */
    private var mIvPlay: ImageView? = null

    /**
     * Player control to pause content
     *
     * */
    private var mIvPause: ImageView? = null
    /**
     * Player control to show available video quality or resolution options
     *
     * */
    private var mIvVideoQuality: TextView? = null
    /**
     * Player control to show available audio and subtitle language options
     *
     * */
    private var mIvVideoLanguage: TextView? = null

    /**
     * Player control to mark or unmark favourite
     *
     * */
    private var mIvWatchList: TextView? = null

    /**
     * Player control to play next
     *
     * */
    private var mIvPlayNext: TextView? = null

    /**
     * Player control to play previous
     *
     * */
    private var mIvPlayPrevious: ImageView? = null

    /**
     * Player control to start over content
     *
     * */
    private var mIvStartOver: ImageView? = null

    /**
     * Player control parent layout
     *
     * */
    private var mFlParent: FrameLayout? = null
    /**
     * SimpelCache for managing playing content cache
     *
     * */
    private var simpleCache: SimpleCache? = null
    /**
     * Activity context
     *
     * */
    private val mContext: Context

    /**
     * TtnPlayerView
     * @see com.google.android.exoplayer2.ui.PlayerView
     *
     * */
    private lateinit var mTtnPlayerView: TtnPlayerView

    /**
     * ExoPlayer
     * @see ExoPlayer
     *
     * */
    private var mPlayer: SimpleExoPlayer? = null
    /**
     * DataSource factory as per the type of palying content
     *
     * */
    private var mDataSourceFactory: DataSource.Factory? = null
    /**
     * MediaSource for the playing content
     *
     * */
    private var mMediaSource: MediaSource? = null
    /**
     * Ad related events while playing add
     * @see TtnPlayerAdListener
     *
     * */
    private var mTtnPlayerAdListener: TtnPlayerAdListener? = null
    /**
     * ImaAdsloader responsible for playing ads
     *
     * */
    private var mImaAdsLoader: ImaAdsLoader? = null
    /**
     * TtnPlayerListener contains important callbacks for user form player
     *
     * */
    private var mTtnPlayerListener: TtnPlayerListener? = null
    /**
     * TtnPlayer listener for available thumb image
     *
     * */
    private var mTtnPlayerThumbListener: TtnPlayerThumbListener? = null
    /**
     * ProgressBar
     *
     * */
    private var mProgressBar: ProgressBar? = null

    /**
     * Player control to play content in full screen or landscape mode
     *
     * */
    private var mBtnFullScreen: ImageView? = null
    /**
     * To show available thumb image for content
     *
     * */
    private var mThumbImage: ImageView? = null
    /**
     * Video content urls to play
     *
     * */
    private var mVideosUris: Array<Uri?>? = null
    /**
     * Subtitle url for playing content
     *
     * */
    private var mSubTitlesUrls: ArrayList<SubtitleDTO>? = null
    /**
     * Ad url
     *
     * */
    private var mTagUrl: String? = null
    /**
     * Flag to track content resume position i.e at what position content is playing content
     *
     * */
    private var mResumePosition = C.TIME_UNSET
    /**
     * Current playing url position if a playlist is loaded
     *
     * */
    private var mResumeWindow = C.INDEX_UNSET
    /**
     * Flag to track player current volume level
     *
     * */
    private var mTempCurrentVolume = 0f
    /**
     * Volume control listener
     *
     * */
    private var mVolumeControlListener: TtnPlayerVolumeControlListener? = null
    /**
     * Drm license url to play drm protected content
     *
     * */
    private var mDrmLicenseUrl: String? = null

    /**
     * kid to generate drmLicense
     *
     * */
    private var kid: String? = null
    /**
     * token to generate drmLicense
     *
     * */
    private var drmLicenseToken: String? = null
    /**
     * drmProxyUrl to generate drmLicense
     *
     * */
    private var drmProxyUrl: String? = null
    /**
     * AudioManager to control device volume from player
     *
     * */
    private var mAudioManager: AudioManager
    /**
     * Player control to show Live tag if content is live or dynamic
     *
     * */
    private var mTextViewLive: TextView? = null
    /**
     * Player current Duration
     *
     * */
    private var mTvCurrentDuration: TextView? = null

    /**
     * Player current Duration
     *
     * */
    private var mTvRemainingDuration: TextView? = null

    /**
     * DefaultTrackSelector to store current playing track information like audio, subtitle, video...etc
     *
     * */
    private lateinit var trackSelector: DefaultTrackSelector
    /**
     * DefaultTrackSelector parameters to track current playing parameters
     *
     * */
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    private var mSelectedAudioTrack: String = ""
    private var mSelectedSubtitleTrack: String = ""
    private var mSelectedVideoTrack: String = ""
    /**
     * Boolean flag to track if player repeat mode is on
     *
     * */

    private var mControllerTime: Long = 5000L

    private var isRepeatModeOn = false
    /**
     * Boolean flag to track if player auto play control is on
     *
     * */
    private var isAutoPlayOn = false
    /**
     * Boolean flag to track if player full screen button control functionality is overriden
     *
     * */
    private var mIsFullScreenButtonFunctionalityOverridden = false
    /**
     * Boolean flag to track if player will show overlay when video paused
     *
     * */
    private var mIsOverlayNeedToSetOnPause = true
    /**
     * Boolean flag to track player's playWhenReady flag on player resume on orientation change
     *
     * */
    private var isResumePlayWhenReady = false
    /**
     * Boolean flag to track if player is showing Ad
     *
     * */
    private var isAdWasShown = false
    /**
     * Boolean flag to track player should prepare to play on resume
     *
     * */
    private var isToPrepareOnResume = true
    /**
     * Boolean flag to track player's to show thumbview image
     *
     * */
    private var isThumbImageViewEnabled = false
    /**
     * Boolean flag to track if Live content related control should show
     *
     * */
    private var isLiveStreamSupportEnabled = false
    /**
     * Player control layout for bottom time showing progress of content
     *
     * */
    private var mBottomProgress: LinearLayout? = null
    /**
     * Boolean flag to track if player is in full screen or landscape mode
     *
     * */
    var mIsInFullScreenMode: Boolean = false
    /**
     * Player control textview to show error message if any while playing content
     *
     * */
    private var mTvErrorMessage: TextView? = null

    /**
     * Boolean flag to track if device volume is muted
     *
     * */
    override var isPlayerVideoMuted: Boolean = false
        private set

    /**
     * Boolean flag to track if player is prepared to play content
     *
     * */
    override var isPlayerPrepared: Boolean = false
        private set


    class Builder(
        context: Context,
        ttnPlayerView: TtnPlayerView,
        cookies: String?,
        isAllCookieEnable: Boolean = false,
        playbackQualityRestrictionsEnabled: Boolean
    ) {
        private val ttnPlayerHelper: TtnPlayerHelper = TtnPlayerHelper(
            context,
            ttnPlayerView,
            cookies,
            isAllCookieEnable,
            playbackQualityRestrictionsEnabled
        )
        /**
         * @param visibility
         * If set true all ui controls will be visible to user.
         * */
        fun setUiControllersVisibility(visibility: Boolean): Builder {
            ttnPlayerHelper.setUiControllersVisibility(visibility)
            return this
        }

        /**
         * Set list of urls to play
         *@param urls ArrayList<String> of video url's
         *
         * */
        fun setVideoUrls(urls: ArrayList<String>): Builder {
            ttnPlayerHelper.setVideoUrls(urls)
            return this
        }

        /**
         * If you are playing DRM content then pass license url for decryption.
         * @param licenseUrl drm license url
         * */
        fun setDrmLicenseUrl(licenseUrl: String?): Builder {
            if (licenseUrl != null)
                ttnPlayerHelper.setDRMLicenseUrl(licenseUrl)
            return this
        }

        fun setDrmInfo(kid: String?, token : String?, drmUrl : String?): Builder {
            ttnPlayerHelper.setDRMInfo(kid, token, drmUrl)
            return this
        }

        fun setControllerTime(time : Long) : Builder{
            ttnPlayerHelper.setControllerTime(time)
            return this
        }

        fun setSubTitlesUrls(list: ArrayList<SubtitleDTO>?): Builder {
            ttnPlayerHelper.setSubtitlesUrls(list)
            return this
        }

        fun setTagUrl(tagUrl: String?): Builder {
            ttnPlayerHelper.mTagUrl = tagUrl
            return this
        }

        fun overrideOrientationHandling(): Builder {
            ttnPlayerHelper.orientationManager?.disable()
            return this
        }

        /**
         *
         * */
        fun setRepeatModeOn(isOn: Boolean): Builder {
            ttnPlayerHelper.isRepeatModeOn = isOn
            return this
        }

        /**
         * If set true content will play automatically when ready
         * @param isAutoPlayOn default value is false if set true content will play automatically when ready
         * */
        fun setAutoPlayOn(isAutoPlayOn: Boolean): Builder {
            ttnPlayerHelper.isAutoPlayOn = isAutoPlayOn
            return this
        }


        fun setTtnPlayerEventsListener(TtnPlayerListener: TtnPlayerListener?): Builder {
            ttnPlayerHelper.setTtnPlayerEventsListener(TtnPlayerListener)
            return this
        }

        fun setTtnAdEventsListener(TtnPlayerAdListener: TtnPlayerAdListener?): Builder {
            ttnPlayerHelper.setTtnAdListener(TtnPlayerAdListener)
            return this
        }

        fun setMiniProgressBar(miniTimeBar : TimeBar?): Builder{
            ttnPlayerHelper.setMiniTimeBar(miniTimeBar)
            return this
        }

        /**
         * @param savedInstanceState pass value of bundle that is saved in onSaveInstanceState on configuration change
         * */
        fun addSavedInstanceState(savedInstanceState: Bundle?): Builder {
            ttnPlayerHelper.addSavedInstanceState(savedInstanceState)
            return this
        }

        fun setThumbImageViewEnabled(TtnPlayerThumbListener: TtnPlayerThumbListener?): Builder {
            ttnPlayerHelper.setTtnThumbListener(TtnPlayerThumbListener)
            return this
        }

        private fun enableCache(maxCacheSizeMb: Int): Builder {
            ttnPlayerHelper.enableCache(maxCacheSizeMb)
            return this
        }

        /**
         * @param toPrepareOnResume
         * If you have a list of videos set isToPrepareOnResume to be false
         * to prevent auto prepare on activity onResume/onCreate
         */
        fun setToPrepareOnResume(toPrepareOnResume: Boolean): Builder {
            ttnPlayerHelper.isToPrepareOnResume = toPrepareOnResume
            return this
        }

        /**
         * This method is to enable live stream support features.
         * */
        fun enableLiveStreamSupport(): Builder {
            ttnPlayerHelper.isLiveStreamSupportEnabled = true
            ttnPlayerHelper.isLiveStream = true
            return this
        }

        fun enableAddToWatchlist(isVisible: Boolean): Builder {
            if (isVisible) ttnPlayerHelper.showAddToWatchlist()
            else ttnPlayerHelper.hideAddToWatchlist()
            return this
        }

        /**
         * This method is to add progressBar for the playing content. If content is in buffering state than a progressBar will be showed.
         * @param colorAccent color for the progressBar
         * */
        fun addProgressBarWithColor(colorAccent: Int): Builder {
            ttnPlayerHelper.addProgressBar(colorAccent)
            return this
        }

        fun addCustomProgressBar(progressBar: ProgressBar, height: Float, width: Float): Builder {
            ttnPlayerHelper.addCustomProgressBar(progressBar, height, width)
            return this
        }

        fun addCustomProgressBar(progressBar: ProgressBar): Builder {
            ttnPlayerHelper.addCustomProgressBar(progressBar)
            return this
        }

        fun setFullScreenBtnVisible(): Builder {
            ttnPlayerHelper.setFullScreenBtnVisibility(true)
            return this
        }

        fun setEpisodeMode(): Builder {
            ttnPlayerHelper.setEpisodeMode()
            return this
        }

        fun disableDeviceSoftNavigationIcons(value: Boolean): Builder {
            ttnPlayerHelper.disableDeviceSoftNavigationIcons(value)
            return this
        }

        /**
         * Probably you will feel a need to use that method when you need to show pre-roll ad
         * and you not interested in auto play. That method allows to separate player creation
         * from calling prepare()
         * Note: To play ad/content you ned to call preparePlayer()
         *
         * @return ExoPlayerHelper instance
         */
        fun create(): TtnPlayerHelper {
            ttnPlayerHelper.createPlayer(false)
            return ttnPlayerHelper
        }

        fun setOverrideFullScreenButtonFunctionality(isFullScreenButtonFunctionalityOverridden: Boolean): Builder {
            ttnPlayerHelper.mIsFullScreenButtonFunctionalityOverridden =
                isFullScreenButtonFunctionalityOverridden
            return this
        }

        fun setOverLayOnVideoPause(isOverlayNeedToSetOnPause: Boolean): Builder {
            ttnPlayerHelper.mIsOverlayNeedToSetOnPause = isOverlayNeedToSetOnPause
            return this
        }


        /**
         * Note: If you added tagUrl ad would start playing automatic even if you had set setAutoPlayOn(false)
         *
         * @return ExoPlayerHelper instance
         */
        fun createAndPrepare(): TtnPlayerHelper {
            ttnPlayerHelper.createPlayer(true)
            return ttnPlayerHelper
        }

        fun setResumePosition(resumeDuration: Long) : Builder{
            ttnPlayerHelper.updateResumeDuration(resumeDuration)
            return this
        }

        /*
        * Use to set L3 support inside player drm
        * */
        fun setEnforceL3Settings(value: Boolean): Builder {
            ttnPlayerHelper.enforceL1L3 = value
            return this
        }

        fun setSecurityLevel(s: String?): Builder {
            ttnPlayerHelper.securityLevel = s
            return this
        }

        fun createAndPrepareTrailer(playUrl: String): TtnPlayerHelper {
            ttnPlayerHelper.createPlayerTrailer(playUrl)
            return ttnPlayerHelper
        }
    }

    private fun updateResumeDuration(resumeDuration: Long) {
        mResumeWindow = 0
        mResumePosition = resumeDuration
        isResumePlayWhenReady = true
    }

    constructor(
        context: Context,
        ttnPlayerView: TtnPlayerView,
        cookies: String?,
        isAllCookieEnable: Boolean,
        playbackQualityRestrictionsEnabled: Boolean
    ) : this(context) {
        this.playbackQualityRestrictionsEnabled = playbackQualityRestrictionsEnabled
        /*This Cookie handling is only required for Chaupal*/
        if(isAllCookieEnable) {
            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
            CookieHandler.setDefault(cookieManager)
        }
        mAllCookieEnable = isAllCookieEnable
        mCookieValue = cookies
        mTtnPlayerView = ttnPlayerView
        setVideoClickable()
        initViews()
        setControllerListener()
        getPlayerViewHeight()
        setViewOrientationAccordingToDevice()
        loadDataSource()
        getCurrentOrientation()
//        hideNavigationAndStatusBar()
//        initOrientationManger()
//        mTtnPlayerView.setShowMultiWindowTimeBar(true)
    }

    init {
        require(context is Activity) { "TtnPlayerHelper constructor - Context must be an instance of Activity" }
        mContext = context
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    }

    override fun disableDeviceSoftNavigationIcons(value: Boolean) {
        mHidePhoneNavigationAndStatusBar = value
    }

    override fun onDrmSessionAcquired() {
        super.onDrmSessionAcquired()
        mTtnPlayerListener?.onDrmSessionAcquired()
    }

    private fun addProgressBar(color: Int) {
        val frameLayout = mTtnPlayerView.overlayFrameLayout ?: return
        mProgressBar = frameLayout.findViewById(R.id.progressBar)
        if (mProgressBar != null) {
            return
        }
        mProgressBar = ProgressBar(mContext, null, android.R.attr.progressBarStyleLarge)
        mProgressBar!!.id = R.id.progressBar
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER
        mProgressBar!!.layoutParams = params
        mProgressBar!!.isIndeterminate = true
        val cFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            if (color == 0) Color.RED else color,
            BlendModeCompat.SRC_IN
        )
        mProgressBar!!.indeterminateDrawable.colorFilter = cFilter
        mProgressBar!!.visibility = View.GONE
        frameLayout.addView(mProgressBar)
    }

    override fun onDrmSessionManagerError(error: Exception) {
        super.onDrmSessionManagerError(error)
        mTtnPlayerListener?.onDrmSessionManagerError(error)

    }

    private fun addCustomProgressBar(progressBar: ProgressBar, height: Float, width: Float) {
        val frameLayout = mTtnPlayerView.overlayFrameLayout ?: return
        mProgressBar = progressBar
        mProgressBar!!.id = R.id.progressBar
        val params = FrameLayout.LayoutParams(
            width.toInt(),
            height.toInt()
        )
        params.gravity = Gravity.CENTER
        mProgressBar!!.layoutParams = params
        mProgressBar!!.visibility = View.GONE
        frameLayout.addView(mProgressBar)
    }


    private fun addCustomProgressBar(progressBar: ProgressBar) {
        val frameLayout = mTtnPlayerView.overlayFrameLayout ?: return
        mProgressBar = progressBar
        mProgressBar!!.id = R.id.progressBar
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER
        mProgressBar!!.layoutParams = params
        mProgressBar!!.visibility = View.GONE
        frameLayout.addView(mProgressBar)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setVideoClickable() {
        mTtnPlayerView.setOnTouchListener(this)
    }

    private fun initViews() {
        mIvPlay = mTtnPlayerView.findViewById(R.id.exo_play)
        mIvPause = mTtnPlayerView.findViewById(R.id.exo_pause)
        mTvCurrentDuration = mTtnPlayerView.findViewById(R.id.position)
        mTvRemainingDuration = mTtnPlayerView.findViewById(R.id.exo_remaining)
        mBtnFullScreen = mTtnPlayerView.findViewById(R.id.iv_full_screen_enter_exit)
        mTextViewLive = mTtnPlayerView.findViewById(R.id.tv_live)
        mBottomProgress = mTtnPlayerView.findViewById(R.id.ll_bottom_controller)
        mIvVideoQuality = mTtnPlayerView.findViewById(R.id.tv_video_quality)
        mIvWatchList = mTtnPlayerView.findViewById(R.id.tv_add_to_watchlist)
        mIvPlayNext = mTtnPlayerView.findViewById(R.id.tv_play_next)
        mIvPlayPrevious = mTtnPlayerView.findViewById(R.id.iv_previous)
        mIvVideoLanguage = mTtnPlayerView.findViewById(R.id.tv_video_language)
        mIvStartOver = mTtnPlayerView.findViewById(R.id.iv_start_over)
        mFlParent = mTtnPlayerView.findViewById(R.id.fl_parent)
        mTvErrorMessage = mTtnPlayerView.findViewById(R.id.tv_error_message)
        mDefaultTimeBar = mTtnPlayerView.findViewById(R.id.exo_progress)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setControllerListener() {
        mIvPause?.setOnTouchListener(this)
        mIvPlay?.setOnTouchListener(this)
        mBtnFullScreen?.setOnClickListener(this)
        mIvWatchList?.setOnClickListener(this)
//        mIvVideoQuality?.setOnClickListener(this)
        mIvPlayPrevious?.setOnClickListener(this)
        mIvPlayNext?.setOnClickListener(this)
//        mIvVideoLanguage?.setOnClickListener(this)
        mIvStartOver?.setOnClickListener(this)
        mTtnPlayerView.setControllerVisibilityListener(this)
        mDefaultTimeBar?.addListener(this)
    }

    fun handleGoLiveButton(showGoLive: Boolean) {
        if (showGoLive) {
            mTextViewLive?.text = mContext.getString(R.string.live)
            mTextViewLive?.background = null
            mTextViewLive?.setOnClickListener(this)
        } else {
            mTextViewLive?.text = mContext.getString(R.string.live)
            mTextViewLive?.background = ContextCompat.getDrawable(mContext, android.R.color.holo_red_dark)
            mTextViewLive?.setOnClickListener(null)
        }
    }

    fun showUiControls() {
        mTtnPlayerView.showController()
    }

    fun hideUiControls() {
        mTtnPlayerView.hideController()
    }

    fun resumePlayer() {
        mPlayer?.playWhenReady = true
        mMediaSource?.let {
            mPlayer?.prepare(it, false, false);
        }
    }

    private fun getHttpFactory(): Call.Factory {
        var builder = OkHttpClient.Builder()
        builder.addInterceptor(HttpLoggingInterceptor(mTtnPlayerListener))
        return builder.build()
    }

    private fun loadDataSource() {
        mVideosUris = emptyArray()
        // Measures bandwidth during playback. Can be null if not required.
        /*This DefaultDataSourceFactory handling is only required for Chaupal
        * For others we need to use OkHttpDataSourceFactory*/
        bandwidthMeter = DefaultBandwidthMeter.Builder(mContext).build()
        val defaultHttpDataSourceFactory =
            if(mAllCookieEnable)
                DefaultHttpDataSourceFactory(
                    Util.getUserAgent(mContext, mContext.getString(R.string.app_name)), bandwidthMeter)
            else{
                OkHttpDataSourceFactory(getHttpFactory(),
                    Util.getUserAgent(mContext, mContext.getString(R.string.app_name)), bandwidthMeter)
            }
        // Produces DataSource instances through which media data is loaded.
        /* val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory(
             Util.getUserAgent(mContext, mContext.getString(R.string.app_name)),
             bandwidthMeter
         )*/
        if(!mAllCookieEnable && !mCookieValue.isNullOrEmpty()) {
            try {
//                var DEFAULT_COOKIE_MANAGER: CookieManager? = null
//                DEFAULT_COOKIE_MANAGER = CookieManager ()
//                DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
                val obj = JSONObject(mCookieValue)
                val cookieValue = "CloudFront-Policy="+obj.getString("CloudFront-Policy")+
                        ";CloudFront-Signature="+obj.getString("CloudFront-Signature")+
                        ";CloudFront-Key-Pair-Id="+obj.getString("CloudFront-Key-Pair-Id")
                //TODO need to uncomment below line with OKHttp
                defaultHttpDataSourceFactory.defaultRequestProperties.set("Cookie", cookieValue)

//                defaultHttpDataSourceFactory.defaultRequestProperties.set("Cookie", mCookieValue)
//                if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
//                    CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER)
//                }
            }
            catch (e: java.lang.Exception){
                e.printStackTrace()
            }
        }
        mDataSourceFactory= DefaultDataSourceFactory(
            mContext,
            bandwidthMeter,
            defaultHttpDataSourceFactory
        )

    }


    // Player creation and release
    private fun setVideoUrls(urls: ArrayList<String>) {
        mVideosUris = arrayOfNulls(urls.size)
        for (i in urls.indices) {
            mVideosUris!![i] = Uri.parse(urls[i])
        }
    }

    private fun setSubtitlesUrls(list: ArrayList<SubtitleDTO>?) {
        mSubTitlesUrls = list
    }

    private fun setDRMLicenseUrl(url: String) {
        mDrmLicenseUrl = url
    }

    private fun setDrmToken(token : String){
        drmLicenseToken = token
    }

    private fun setDRMInfo(kid: String?, token : String?, drmUrl : String?) {
        this.kid = kid
        this.drmLicenseToken = token
        this.drmProxyUrl = drmUrl
    }
    private fun setControllerTime(time : Long){
        mControllerTime = time
    }

    public fun createMediaSource(/*subTitlesUrl:  ArrayList<SubtitleDTO>?*/) : MediaSource{
        // A MediaSource defines the media to be played, loads the media, and from which the loaded media can be read.
        // A MediaSource is injected via ExoPlayer.prepare at the start of playback.
        val mediaSources = arrayOfNulls<MediaSource>(mVideosUris!!.size)
        for (i in mVideosUris!!.indices) {
            mediaSources[i] = mVideosUris!![i]?.let { buildMediaSource(it) }
            if ((mSubTitlesUrls?.size ?: 0) > 0) {

                mediaSources[i] = addSubTitlesToMediaSource(mediaSources[i], mSubTitlesUrls)
            }
        }
        val mediaSource =
            if (mediaSources.size == 1) mediaSources[0]!! else ConcatenatingMediaSource(*mediaSources)
        addAdsToMediaSource()
        return mediaSource
    }


    private fun addSubTitlesToMediaSource(
        mediaSource: MediaSource?,
        subTitlesUrl:  ArrayList<SubtitleDTO>?
    ): MediaSource {
        var mediaSourcetemp=mediaSource
        subTitlesUrl?.let {
            val mediaSourcesSub = arrayOfNulls<MediaSource>(mSubTitlesUrls?.size!!)

            for(i in mSubTitlesUrls!!.indices) {
                val textFormat = Format.createTextSampleFormat(
                    null, MimeTypes.APPLICATION_SUBRIP,
                    null, Format.NO_VALUE, Format.NO_VALUE, it[i].lang, Format.NO_VALUE, null
                )
                val uri = Uri.parse(it[i].url?:"")
                val subtitleSource: MediaSource = SingleSampleMediaSource.Factory(mDataSourceFactory)
                    .createMediaSource(uri, textFormat, C.TIME_UNSET)
                mediaSourcesSub[i]=subtitleSource
            }


            mediaSourcetemp=MergingMediaSource(mediaSource,*mediaSourcesSub)

        }
        return mediaSourcetemp!!
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val type = Util.inferContentType(uri)
        bandwidthMeter = DefaultBandwidthMeter.Builder(mContext).build()
        if(kid != null) {
            mDrmSessionManager = DRMSessionManager().buildDrmSessionManager(kid, drmLicenseToken, drmProxyUrl)
        }
        else if (!mDrmLicenseUrl.isNullOrBlank()) {
            val defaultHttpDataSourceFactory =
                if(mAllCookieEnable)
                    DefaultHttpDataSourceFactory(
                        Util.getUserAgent(mContext, mContext.getString(R.string.app_name)), bandwidthMeter)
                else
                    OkHttpDataSourceFactory(getHttpFactory(),
                        Util.getUserAgent(mContext, mContext.getString(R.string.app_name)), bandwidthMeter)

            val drmCallback = HttpMediaDrmCallback(
                mDrmLicenseUrl!!,
                defaultHttpDataSourceFactory)
            if(drmLicenseToken?.isNotEmpty() == true)
                drmCallback.setKeyRequestProperty("X-AxDRM-Message", drmLicenseToken!!)
            mDrmSessionManager = if (enforceL1L3) DRMSessionManager().buildSessionManager(drmCallback, securityLevel)
            else
                DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
//                    .setMultiSession(true)
                    .build(drmCallback)

            mDrmSessionManager!!.addListener(Handler(), this)
        }
        if (mDrmSessionManager != null) {
            return getMediaSource(type, uri, mDrmSessionManager!!)
        } else {
            return getMediaSource(type, uri)
        }
    }

    private fun getMediaSource(
        type: Int,
        uri: Uri,
        drmSessionManager: DefaultDrmSessionManager<ExoMediaCrypto>
    ): MediaSource {
        return when (type) {
            C.TYPE_SS -> SsMediaSource.Factory(mDataSourceFactory!!).setDrmSessionManager(
                drmSessionManager
            ).createMediaSource(uri)
            C.TYPE_DASH -> {
                DashMediaSource.Factory(mDataSourceFactory!!).setDrmSessionManager(
                    drmSessionManager
                ).createMediaSource(uri)
            }
            C.TYPE_HLS -> HlsMediaSource.Factory(mDataSourceFactory!!).setDrmSessionManager(
                drmSessionManager
            ).createMediaSource(uri)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(mDataSourceFactory!!)
                .setDrmSessionManager(
                    drmSessionManager
                ).createMediaSource(uri)
            else -> {
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    private fun getMediaSource(type: Int, uri: Uri): MediaSource {
        val drmSessionManager = getDummyDrmSessionManager<ExoMediaCrypto>()
        return when (type) {
            C.TYPE_SS -> SsMediaSource.Factory(mDataSourceFactory!!).setDrmSessionManager(
                drmSessionManager
            ).createMediaSource(uri)
            C.TYPE_DASH -> DashMediaSource.Factory(mDataSourceFactory!!).setDrmSessionManager(
                drmSessionManager
            ).createMediaSource(uri)
            C.TYPE_HLS -> HlsMediaSource.Factory(DefaultHttpDataSourceFactory(
                Util.getUserAgent(mContext, mContext.getString(R.string.app_name))))
                /*.setDrmSessionManager(
                drmSessionManager
            )*/.createMediaSource(uri)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(mDataSourceFactory!!)
                .setDrmSessionManager(
                    drmSessionManager
                ).createMediaSource(uri)
            else -> {
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    private fun addAdsToMediaSource() {
        if(mMediaSource != null) {
            if (mTagUrl == null || isAdWasShown) {
                return
            }
            if (mImaAdsLoader == null) {
                mImaAdsLoader = ImaAdsLoader.Builder(mContext)
                    .setAdEventListener(this)
                    .buildForAdTag(Uri.parse(mTagUrl))
//            mImaAdsLoader?.addCallback(this)
                mImaAdsLoader?.setPlayer(mPlayer)
            }
            //Set player using adsLoader.setPlayer before preparing the player.
            mMediaSource =
                AdsMediaSource(mMediaSource, mDataSourceFactory, mImaAdsLoader, mTtnPlayerView)
        }

    }

    private fun setProgressVisible(visible: Boolean) {
        if (visible) mTtnPlayerListener?.onBufferStart() else mTtnPlayerListener?.onBufferEnd()
        if (mProgressBar != null) {
            mProgressBar!!.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    private fun addThumbImageView() {
        if (mThumbImage != null) {
            return
        }
        val frameLayout: AspectRatioFrameLayout =
            mTtnPlayerView.findViewById(R.id.exo_content_frame)
        mThumbImage = ImageView(mContext)
        mThumbImage!!.id = R.id.thumbImg
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        params.gravity = Gravity.CENTER
        mThumbImage!!.layoutParams = params
        mThumbImage!!.setBackgroundColor(Color.BLACK)
        frameLayout.addView(mThumbImage)
        if (mTtnPlayerThumbListener != null) {
            mTtnPlayerThumbListener!!.onThumbImageViewReady(mThumbImage)
        }
    }

    private fun removeThumbImageView() {
        if (mThumbImage != null) {
            val frameLayout: AspectRatioFrameLayout =
                mTtnPlayerView.findViewById(R.id.exo_content_frame)
            frameLayout.removeView(mThumbImage)
            mThumbImage = null
        }
    }

    private fun setUiControllersVisibility(visibility: Boolean) {
        mTtnPlayerView.useController = visibility
        if (!visibility) {
            val frameLayout: AspectRatioFrameLayout =
                mTtnPlayerView.findViewById(R.id.exo_content_frame)
            frameLayout.setOnClickListener(this)
        }
    }

    override fun setEpisodeMode() {
        mIvPlayPrevious?.visibility = View.VISIBLE
        mIvPlayNext?.visibility = View.VISIBLE
    }

    private fun enableCache(maxCacheSizeMb: Int) {
        val evictor = LeastRecentlyUsedCacheEvictor((maxCacheSizeMb * 1024 * 1024).toLong())
        val databaseProvider: DatabaseProvider = ExoDatabaseProvider(mContext)
        val file = File(mContext.cacheDir, "media")
        d(TAG, "enableCache (" + maxCacheSizeMb + " MB), file: " + file.absolutePath)
        simpleCache = SimpleCache(file, evictor, databaseProvider)
        mDataSourceFactory = CacheDataSourceFactory(
            simpleCache,
            mDataSourceFactory,
            FileDataSource.Factory(),
            CacheDataSinkFactory(simpleCache, 2 * 1024 * 1024),
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
            object : CacheDataSource.EventListener {
                override fun onCacheIgnored(reason: Int) {
                    d(TAG, "onCacheIgnored")
                }

                override fun onCachedBytesRead(cacheSizeBytes: Long, cachedBytesRead: Long) {
                    d(
                        TAG,
                        "onCachedBytesRead , cacheSizeBytes: $cacheSizeBytes   cachedBytesRead: $cachedBytesRead"
                    )
                }
            })
    }


    private fun handleEnterExitFullScreenButton() {
        setOrientationFromEnterExitButton()
        updatePlayerWidthHeight()
        updateBtnFullScreenImage()
        hideNavigationAndStatusBar()


    }

    private fun setViewOrientationAccordingToDevice() {
        mOrientation = mContext.resources.configuration.orientation
        updatePlayerWidthHeight()
        updateBtnFullScreenImage()
    }

    private fun updatePlayerWidthHeight() {
        when (mOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                updateVideoViewForLandscape()
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                updateVideoViewForPortrait()
            }
        }
    }

    private fun updateBtnFullScreenImage() {
        when (mOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                mBtnFullScreen?.setImageResource(R.drawable.exo_controls_fullscreen_exit)

            }
            Configuration.ORIENTATION_PORTRAIT -> {
                mBtnFullScreen?.setImageResource(R.drawable.exo_controls_fullscreen_enter)
            }
        }


    }

    fun updateVideoViewForPortrait() {
        val params = mTtnPlayerView.layoutParams
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = mPlayerViewInitialHeight
        mTtnPlayerView.layoutParams = params
    }

    fun updateVideoViewForLandscape() {
        val params = mTtnPlayerView.layoutParams
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.MATCH_PARENT
        mTtnPlayerView.layoutParams = params
    }


    fun updateVideoViewForPortrait(ttnPlayerView: TtnPlayerView?) {
        val params = ttnPlayerView?.layoutParams
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = mPlayerViewInitialHeight
        ttnPlayerView?.layoutParams = params
    }

    fun updateVideoViewForLandscape(ttnPlayerView: TtnPlayerView?) {
        val params = ttnPlayerView?.layoutParams
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
//        params?.height =getRealDisplayPoint(mContext).y
        params?.height = ViewGroup.LayoutParams.MATCH_PARENT

        ttnPlayerView?.layoutParams = params
    }


    fun changeOrientationToPortrait() {
        (mContext as Activity).requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mOrientation = Configuration.ORIENTATION_PORTRAIT
        setFullScreenFlag(mOrientation)
    }

    fun changeOrientationToLandscape() {
        (mContext as Activity).requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        mOrientation = Configuration.ORIENTATION_LANDSCAPE
        setFullScreenFlag(mOrientation)
    }

    fun setFullScreenFlag(orientation: Int) {
        when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                mIsInFullScreenMode = true
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                mIsInFullScreenMode = false
            }

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_UP && mTtnPlayerListener != null) {

            if (view.id == R.id.exo_play) {
                setOverLay(ContextCompat.getColor(mContext, R.color.transparent))
                if (mTtnPlayerListener!!.onPlayBtnTap()) {
                    return true
                }
            }
            if (view.id == R.id.exo_pause) {
                setOverLay(ContextCompat.getColor(mContext, R.color.black_50))
                setProgressVisible(false)
                if (mTtnPlayerListener!!.onPauseBtnTap()) {
                    return true
                }
            }

        }
        // Player block
        val layout = mTtnPlayerView.overlayFrameLayout
        return layout != null && view.id == layout.id
    }

    private fun setOverLay(color: Int) {
        if (mIsOverlayNeedToSetOnPause) {
            mFlParent?.setBackgroundColor(color)
        }
    }


    // Resume position saving
    private fun addSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            isAdWasShown = savedInstanceState.getBoolean(PARAM_IS_AD_WAS_SHOWN, false)
            isResumePlayWhenReady = savedInstanceState.getBoolean(PARAM_AUTO_PLAY, true)
            mResumeWindow = savedInstanceState.getInt(PARAM_WINDOW, C.INDEX_UNSET)
            mResumePosition = savedInstanceState.getLong(PARAM_POSITION, C.TIME_UNSET)
            trackSelectorParameters =
                savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS)
        }
    }

    private fun updateResumePosition() {
        isResumePlayWhenReady = mPlayer!!.playWhenReady
        mResumeWindow = mPlayer!!.currentWindowIndex
        mResumePosition = max(0, mPlayer!!.contentPosition)
    }

    private fun clearResumePosition() {
        mResumeWindow = C.INDEX_UNSET
        mResumePosition = C.TIME_UNSET
    }

    private val nextWindowIndex: Int
        get() = mPlayer!!.currentTimeline.getNextWindowIndex(
            mPlayer!!.currentWindowIndex,
            mPlayer!!.repeatMode,
            false
        )

    private val previousWindowIndex: Int
        get() = mPlayer!!.currentTimeline.getPreviousWindowIndex(
            mPlayer!!.currentWindowIndex,
            mPlayer!!.repeatMode,
            false
        )

    // Player events, internal handle
    private fun onPlayerBuffering() {
        e(TAG, mPlayer!!.playWhenReady.toString())
        if (mPlayer!!.playWhenReady) {
            setProgressVisible(true)
        }
    }

    private fun onPlayerPlaying() {
        setProgressVisible(false)
        hideErrorMessageView()
        removeThumbImageView()
    }

    private fun onPlayerLoadingChanged() {
        liveStreamCheck()
    }

    private fun hideErrorMessageView() {
        setOverLay(ContextCompat.getColor(mContext, R.color.transparent))
        mTvErrorMessage?.visibility = View.GONE
    }

    fun showErrorMessageView(msg: String) {
        setOverLay(ContextCompat.getColor(mContext, R.color.black_50))
        mTvErrorMessage?.visibility = View.VISIBLE
        mTvErrorMessage?.text = msg
    }

    private fun liveStreamCheck() {
        if (isLiveStreamSupportEnabled) {
//            isLiveStream =
//                (mPlayer!!.isCurrentWindowDynamic || !mPlayer!!.isCurrentWindowSeekable) && mPlayer!!.isCurrentWindowLive
            mTextViewLive?.visibility = if (isLiveStream) View.VISIBLE else View.GONE
            mTvRemainingDuration?.visibility = if (!isLiveStream) View.VISIBLE else View.INVISIBLE
        }
    }

    fun getDefaultSeekPosition() =
        mPlayer?.let {
            it.currentTimeline.getWindow(it.currentWindowIndex, Timeline.Window()).defaultPositionMs
        } ?: duration

    private fun onPlayerPaused() {
        setProgressVisible(false)
    }

    private fun onAdEnded() {
        isAdWasShown = true
    }


    private fun getPlayerViewHeight() {
        val params = mTtnPlayerView.layoutParams
        mPlayerViewInitialHeight = params?.height
    }

    /**
     * ExoPlayerControl interface methods
     */
    override fun setTtnThumbListener(TtnPlayerThumbListener: TtnPlayerThumbListener?) {
        isThumbImageViewEnabled = true
        mTtnPlayerThumbListener = TtnPlayerThumbListener
    }

    override fun setTtnPlayerEventsListener(pTtnPlayerListenerListener: TtnPlayerListener?) {
        mTtnPlayerListener = pTtnPlayerListenerListener
    }

    override fun setTtnAdListener(TtnPlayerAdListener: TtnPlayerAdListener?) {
        mTtnPlayerAdListener = TtnPlayerAdListener
    }

    override fun setMiniTimeBar(timeBar: TimeBar?) {
        mMiniTimeBar = timeBar
    }

    override fun hideAddToWatchlist() {
        mIvWatchList?.visibility = View.GONE
    }

    override fun showAddToWatchlist() {
        mIvWatchList?.visibility = View.VISIBLE
    }

    private fun createPlayerTrailer(url : String) {
        val trackSelector = DefaultTrackSelector()
        val mLoadBuilder : DefaultLoadControl.Builder = Builder()
        ProbeInterface.configBufferingProp(mLoadBuilder)
        ProbeInterface.configEstDownloadRate()
        if(mPlayer == null) {
            val renderersFactory = DefaultRenderersFactory(mContext)
            mPlayer = SimpleExoPlayer.Builder(mContext, renderersFactory)
                .setTrackSelector(trackSelector)
                .build()
        }
//        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector)
        val mediaDataSourceFactory = DefaultHttpDataSourceFactory(
            Util.getUserAgent(mContext, "TS")
        )
        val mediaSource: MediaSource = createMediaSource()
        mPlayer?.setHandleWakeLock(true)
        mPlayer?.setHandleAudioBecomingNoisy(true)
        //for handling audio focus
        val audioAttribute =
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MOVIE)
                .build()
        mPlayer?.setAudioAttributes(audioAttribute, true)
        mTtnPlayerView.requestFocus()
        mTtnPlayerView.player = mPlayer
        mTtnPlayerView.controllerShowTimeoutMs = mControllerTime.toInt()
        mTtnPlayerView.controllerAutoShow = false
        mTtnPlayerView.controllerHideOnTouch = true
        mTempCurrentVolume = mPlayer?.volume!!
        mPlayer?.repeatMode = if (isRepeatModeOn) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        mPlayer?.playWhenReady = isAutoPlayOn
        e(TAG, "inside createandplay isAutoPlayOn:$isAutoPlayOn")
        mPlayer?.addListener(this)
        mMiniTimeBar?.addListener(this)
        mPlayer?.addTextOutput(this)
        mPlayer?.addAnalyticsListener(EventLogger(trackSelector))
        mTtnPlayerView.setPlaybackPreparer(this)
        (mTtnPlayerView.videoSurfaceView as SurfaceView).setSecure(true)
        mPlayer?.prepare(mediaSource)
    }

    override fun createPlayer(isToPrepare: Boolean) {
        if (mPlayer != null) {
            return
        }
        if (isThumbImageViewEnabled) {
            addThumbImageView()
        }

        //load control
        val mLoadBuilder : DefaultLoadControl.Builder = Builder()
//        mLoadBuilder?.setAllocator(DefaultAllocator(true, 2 * 1024 * 1024))
        mLoadBuilder?.setBufferDurationsMs(
            30000, 120000,
            15000, DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
        )
//        mLoadBuilder?.setPrioritizeTimeOverSizeThresholds(false)//Commented because of Manorama Playback issue
        ProbeInterface.configBufferingProp(mLoadBuilder)
        val mLoadControl: DefaultLoadControl = mLoadBuilder?.createDefaultLoadControl()
        if (trackSelectorParameters == null) {
            e("SecurityLevel", securityLevel)
            val trackBuilder = DefaultTrackSelector.ParametersBuilder(mContext)
            trackSelectorParameters =
                if (enforceL1L3 && SECURITY_LEVEL_L3.equals(securityLevel, true)) {

                    if (Util.SDK_INT >= 21) {
                        trackBuilder.setTunnelingAudioSessionId(C.generateAudioSessionIdV21(mContext))
                        trackBuilder.setMaxVideoBitrate(L3_MAX_BITRATE)
                            .setMaxVideoSize(854, 480)
                    }
                    trackBuilder.build()
                }
                else
                    trackBuilder.build()
            ProbeInterface.configEstDownloadRate()
            trackSelectorParameters = if (enforceL1L3 && SECURITY_LEVEL_L3.equals(securityLevel, true)) {
                val trackBuilder = DefaultTrackSelector.ParametersBuilder(mContext)
                if (Util.SDK_INT >= 21) {
                    trackBuilder.setTunnelingAudioSessionId(C.generateAudioSessionIdV21(mContext))
                    trackBuilder.setMaxVideoBitrate(L3_MAX_BITRATE)
                        .setMaxVideoSize(854, 480)
                }
                trackBuilder.build()
            } else if (playbackQualityRestrictionsEnabled) {
                // Restrict auto video quality to < 4K Streaming
                val maxAllowedVideoWidth = PROVIDER_CHAUPAL_RESTRICTIONS.first
                val maxAllowedVideoHeight = PROVIDER_CHAUPAL_RESTRICTIONS.second
                val trackBuilder = DefaultTrackSelector.ParametersBuilder(mContext)
                trackBuilder
                    .setMaxVideoSize(maxAllowedVideoWidth, maxAllowedVideoHeight)
                    .build()
            } else {
                DefaultTrackSelector.ParametersBuilder(mContext).build()
            }
        }
        val bandwithFraction = .8f
        val minDurationForQualityIncreaseMs = 1000
        val maxDurationForQualityDecreaseMs = 1000
        val minDurationToRetainAfterDiscardMs = 1000
        val trackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(
            AdaptiveTrackSelection.DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS,
            AdaptiveTrackSelection.DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
            AdaptiveTrackSelection.DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS,
            AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION
        )
        trackSelector = DefaultTrackSelector(mContext, trackSelectionFactory)
        trackSelector.parameters = trackSelectorParameters as DefaultTrackSelector.Parameters
        if(mPlayer == null) {
            val renderersFactory = DefaultRenderersFactory(mContext)
            mPlayer = SimpleExoPlayer.Builder(mContext, renderersFactory)
                .setLoadControl(mLoadControl)
                .setTrackSelector(trackSelector)
                .build()
        }
//        mPlayer = SimpleExoPlayer.Builder(mContext)
//            .setLoadControl(mLoadControl).setTrackSelector(trackSelector).build()
        mPlayer?.setHandleWakeLock(true)
        mPlayer?.setHandleAudioBecomingNoisy(true)
        //for handling audio focus
        val audioAttribute =
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MOVIE)
                .build()
        mPlayer?.setAudioAttributes(audioAttribute, true)
        mTtnPlayerView.requestFocus()
        mTtnPlayerView.player = mPlayer
        mTtnPlayerView.controllerShowTimeoutMs = mControllerTime.toInt()
        mTtnPlayerView.controllerAutoShow = false
        mTtnPlayerView.controllerHideOnTouch = true
        mTempCurrentVolume = mPlayer?.volume!!
        mPlayer?.repeatMode = if (isRepeatModeOn) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        mPlayer?.playWhenReady = isAutoPlayOn
        e(TAG, "inside createandplay isAutoPlayOn:$isAutoPlayOn")
        mPlayer?.addListener(this)
        mMiniTimeBar?.addListener(this)
        mPlayer?.addTextOutput(this)
        mPlayer?.addAnalyticsListener(EventLogger(trackSelector))
        mTtnPlayerView.setPlaybackPreparer(this)
        (mTtnPlayerView.videoSurfaceView as SurfaceView).setSecure(true)
        mMediaSource = createMediaSource()
        if (isToPrepare) {
            preparePlayer()
        }
        if (mTtnPlayerListener != null) {
            mTtnPlayerListener!!.createTtnPlayerCalled(isToPrepare)
        }
    }

    fun showVideoQualityDialog(
        trackSelector: DefaultTrackSelector?,
        rendererIndex: Int,
        title: String?,
        showDisableOption: Boolean,
        allowAdaptiveSelections: Boolean
    ): Dialog {
        val dialog = Dialog(mContext, R.style.DialogThemeTransparent)
        dialog.setContentView(R.layout.popup_select_player_options)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        dialog.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        dialog.setOnDismissListener {
            mDialogShowing=false
            resumePlayer()
        }
        dialog.findViewById<ImageView>(R.id.iv_close).setOnClickListener { dialog.dismiss() }
        val textViewTitle = dialog.findViewById<TextView>(R.id.tv_title)
        textViewTitle.text = mContext.getString(R.string.video_quality)
        val trackSelectionListener = object : TtnTrackSelector.OnTrackSelectionListener {
            override fun onTrackSelectionApply() {
//                dialog.dismiss()
            }
        }
        val trackSelectionViewUtility = TtnTrackSelector(
            context = mContext,
            trackSelector = trackSelector!!,
            rendererIndex = rendererIndex,
            onTrackSelectionListener = trackSelectionListener
        )
        trackSelectionViewUtility.init(
            allowAdaptiveSelections = allowAdaptiveSelections,
            allowMultipleOverrides = false
        )
        val param = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1.0f
        )
        val view = trackSelectionViewUtility.createTrackSelectionView(title, showDisableOption)
        view.layoutParams = param
        param.marginEnd = dpToPx(96, mContext)
        param.marginStart = dpToPx(96, mContext)
        dialog.findViewById<RelativeLayout>(R.id.mock_demo_view).addView(view)
        dialog.show()
        mDialogShowing=true
        return dialog
    }


    private fun showTrackPopup(
        rendererIndex: Int,
        title: String?,
        showDisableOption: Boolean,
        allowAdaptiveSelections: Boolean
    ): PopupWindow {
        val popup = PopupWindow(mContext)
        val trackSelectionListener = object : TtnTrackSelector.OnTrackSelectionListener {
            override fun onTrackSelectionApply() {
                popup.dismiss()
            }
        }
        val trackSelectionViewUtility = TtnTrackSelector(
            context = mContext,
            trackSelector = trackSelector,
            rendererIndex = rendererIndex,
            onTrackSelectionListener = trackSelectionListener
        )
        trackSelectionViewUtility.init(
            allowAdaptiveSelections = allowAdaptiveSelections,
            allowMultipleOverrides = false
        )
        val view = trackSelectionViewUtility.createTrackSelectionView(title, showDisableOption)
        popup.isOutsideTouchable = false
        popup.height = WindowManager.LayoutParams.MATCH_PARENT
        popup.width = WindowManager.LayoutParams.MATCH_PARENT
        popup.setBackgroundDrawable(
            ContextCompat.getDrawable(
                mContext,
                android.R.color.black
            )
        )
        popup.contentView = view
        return popup
    }

    fun showLanguageAndSubtitleDialog(trackSelector: DefaultTrackSelector?): Dialog {
        val dialog = Dialog(mContext, R.style.DialogThemeTransparent)
        dialog.setContentView(R.layout.popup_select_player_options)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        dialog.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        dialog.setOnDismissListener {
            mDialogShowing=false
            resumePlayer()
        }
        val textViewTitle = dialog.findViewById<TextView>(R.id.tv_title)
        dialog.findViewById<ImageView>(R.id.iv_close).setOnClickListener { dialog.dismiss() }
        textViewTitle.text = mContext.getString(R.string.audio_setting)
        val trackSelectionListener = object : TtnTrackSelector.OnTrackSelectionListener {
            override fun onTrackSelectionApply() {
//                dialog.dismiss()
            }
        }
        val ttnTrackSelectorAudio = TtnTrackSelector(
            context = mContext,
            trackSelector = trackSelector!!,
            rendererIndex = AUDIO_TRACK,
            onTrackSelectionListener = trackSelectionListener
        )
        ttnTrackSelectorAudio.init(allowAdaptiveSelections = false, allowMultipleOverrides = false)
        val audioTrackView =
            ttnTrackSelectorAudio.createTrackSelectionView(
                mContext.getString(R.string.ttn_audio),
                false
            )
        val ttnTrackSelectorSubtitle = TtnTrackSelector(
            context = mContext,
            trackSelector = trackSelector!!,
            rendererIndex = SUBTITLE_TRACK,
            onTrackSelectionListener = trackSelectionListener
        )
        ttnTrackSelectorSubtitle.init(
            allowAdaptiveSelections = false,
            allowMultipleOverrides = false
        )
        val subtitleTrackView = ttnTrackSelectorSubtitle.createTrackSelectionView(
            mContext.getString(R.string.ttn_subtitle),
            true
        )
        val param = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1.0f
        )
        val audioParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1.0f
        )
        val subtitleParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1.0f
        )
        audioParam.marginEnd = dpToPx(12, mContext)
        subtitleParam.marginStart = dpToPx(12, mContext)
        audioTrackView.layoutParams = audioParam
        subtitleTrackView.layoutParams = subtitleParam

        val view =
            (mContext as Activity).layoutInflater.inflate(R.layout.multi_trackview_dialog, null)
        val llMultitracks = view.findViewById<LinearLayout>(R.id.ll_multi_tracks)
        //add AUDIO view
        llMultitracks.addView(audioTrackView)
        //add subtitle view
        llMultitracks.addView(subtitleTrackView)
        llMultitracks.layoutParams = param
        dialog.findViewById<RelativeLayout>(R.id.mock_demo_view).addView(llMultitracks)
        dialog.show()
        mDialogShowing=true
        return dialog
    }

    private fun showLanguagePopup(): PopupWindow {
        val contextWrapper: Context = ContextThemeWrapper(mContext, R.style.MyPopupMenu)
        val popup = PopupWindow(contextWrapper)
        val trackSelectionListener = object : TtnTrackSelector.OnTrackSelectionListener {
            override fun onTrackSelectionApply() {
                popup.dismiss()
            }
        }
        val ttnTrackSelectorAudio = TtnTrackSelector(
            context = mContext,
            trackSelector = trackSelector,
            rendererIndex = AUDIO_TRACK,
            onTrackSelectionListener = trackSelectionListener
        )
        ttnTrackSelectorAudio.init(allowAdaptiveSelections = false, allowMultipleOverrides = false)
        val audioTrackView =
            ttnTrackSelectorAudio.createTrackSelectionView(
                mContext.getString(R.string.ttn_audio),
                true
            )
        val ttnTrackSelectorSubtitle = TtnTrackSelector(
            context = mContext,
            trackSelector = trackSelector,
            rendererIndex = SUBTITLE_TRACK,
            onTrackSelectionListener = trackSelectionListener
        )
        ttnTrackSelectorSubtitle.init(
            allowAdaptiveSelections = false,
            allowMultipleOverrides = false
        )
        val subtitleTrackView = ttnTrackSelectorSubtitle.createTrackSelectionView(
            mContext.getString(R.string.ttn_subtitle),
            true
        )
        val param = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1.0f
        )
        audioTrackView.layoutParams = param
        subtitleTrackView.layoutParams = param

        val view =
            (mContext as Activity).layoutInflater.inflate(R.layout.multi_trackview_dialog, null)
        val llMultitracks = view.findViewById<LinearLayout>(R.id.ll_multi_tracks)
        //add AUDIO view
        llMultitracks.addView(audioTrackView)
        //add subtitle view
        llMultitracks.addView(subtitleTrackView)
        popup.contentView = view
        // Set content width and height
        popup.height = WindowManager.LayoutParams.MATCH_PARENT
        popup.width = WindowManager.LayoutParams.MATCH_PARENT
        // Closes the popup window when touch outside of it - when looses focus
        popup.isOutsideTouchable = false
        popup.isFocusable = true
        // Show anchored to button
        popup.setBackgroundDrawable(ContextCompat.getDrawable(mContext, android.R.color.black))
        popup.setOnDismissListener { hideNavigationAndStatusBar() }
        return popup
    }

    fun getTracks(rendererIndex: Int): TtnTrackSelector? {
        if (mPlayBackStateLoaded) {
            val ttnTrackSelector = TtnTrackSelector(
                context = mContext,
                trackSelector = trackSelector,
                rendererIndex = rendererIndex,
                onTrackSelectionListener = null
            )
            ttnTrackSelector.init(allowAdaptiveSelections = true, allowMultipleOverrides = false)
            return ttnTrackSelector
        }
        return null
    }

    fun getTtnPlayerListener():TtnPlayerListener?{
        return mTtnPlayerListener
    }

    /**
     * This method is to TtnPlayerTrackSelectionView of available tracks that can be used in any view (i.e. AlertDialog, PopupMenu, ListView etc)
     * @param rendererIndex TtnPlayerTrackSelectionView type that you want to get
     * AUDIO_TRACK to get available audio tracks, SUBTITLE_TRACK for available subtitle tracks, VIDEO_TRACK for available video tracks in video
     * @return TtnPlayerTrackSelectionView If no tracks available then it will return null
     * */
    fun getTrackSelectionView(
        rendererIndex: Int,
        onTrackSelectionListener: TtnTrackSelector.OnTrackSelectionListener,
        allowAdaptiveSelections: Boolean,
        showDisableOption: Boolean,
        title: String
    ): View {
        val trackSelectionViewUtility = TtnTrackSelector(
            context = mContext,
            trackSelector = trackSelector,
            rendererIndex = rendererIndex,
            onTrackSelectionListener = onTrackSelectionListener
        )
        trackSelectionViewUtility.init(
            allowAdaptiveSelections = allowAdaptiveSelections,
            allowMultipleOverrides = false
        )
        return trackSelectionViewUtility.createTrackSelectionView(title, showDisableOption)
    }

/*fun setSelectedTrack(rendererIndex: Int, text: String) {
    when (rendererIndex) {
        AUDIO_TRACK -> mSelectedAudioTrack = text
        VIDEO_TRACK -> mSelectedVideoTrack = text
        SUBTITLE_TRACK -> mSelectedSubtitleTrack = text
    }
}*/

/* private fun handleVisibilityOfTrackMenus() {
     for (x in 0..3) {
         val value = TtnPlayerTrackSelectionView.willHaveContent(trackSelector, x)
         if (x == SUBTITLE_TRACK) {
             mIvVideoLanguage?.visibility = if (value) View.VISIBLE else View.GONE
         } else if (x == VIDEO_TRACK) {
             mIvVideoQuality?.visibility = if (value) View.VISIBLE else View.GONE
         } else if (x == AUDIO_TRACK) {
             //todo
         }
     }
 }*/

    fun playVideo(videoUrl: String, licenseUrl: String?) {
        playerStop()
        mDrmLicenseUrl = licenseUrl
        updateVideoUrls(arrayListOf(videoUrl))
        hideErrorMessageView()
        preparePlayer()
    }

    private val mHandler = Handler()

    override fun preparePlayer() {
        if (mPlayer == null || isPlayerPrepared) {
            return
        }
        mPlayBackStateLoaded = false
        isPlayerPrepared = true
        mPlayer!!.prepare(mMediaSource!!)
        mHandler.postDelayed(updateUI, 1000)
        if (!isLiveStreamSupportEnabled && mResumeWindow != C.INDEX_UNSET && !mPlayer!!.isPlayingAd) {
            mPlayer!!.playWhenReady = isResumePlayWhenReady
            mPlayer!!.seekTo(mResumeWindow, mResumePosition + 100)
            if (mTtnPlayerListener != null) {
                mTtnPlayerListener!!.onVideoResumeDataLoaded(
                    mResumeWindow,
                    mResumePosition,
                    isResumePlayWhenReady
                )
            }
            //mExoPlayerView.postDelayed(checkFreeze, 1000);
        }
    }

    private val updateUI = Runnable {
        updateRemainingTime()
    }

    private fun updateRemainingTime() {
        mHandler.removeCallbacks(updateUI)
        val remainingTime = (mPlayer?.duration ?: 0L) - (mPlayer?.currentPosition ?: 0L)
        val remainingTimeVal = Util.getStringForTime(
            formatBuilder, Formatter(
                formatBuilder,
                Locale.getDefault()
            ), remainingTime
        )
        if (!remainingTimeVal.contains("-") && remainingTimeVal.length <= 8) {
            mTvRemainingDuration?.text = remainingTimeVal
        }
        mHandler.postDelayed(updateUI, 1000)
    }

    // It looks like the issue was solved and no need for this Runnable
    private val checkFreeze = Runnable {
        if (mPlayer != null && mPlayer!!.playbackState == Player.STATE_BUFFERING && mPlayer!!.playWhenReady) {
            e(TAG, "Player.STATE_BUFFERING stuck issue")
            mPlayer!!.seekTo(if (mPlayer!!.contentPosition > 500) mPlayer!!.contentPosition - 500 else 0)
        }
    }

    override fun releasePlayer() {
        isPlayerPrepared = false
        if (mTtnPlayerListener != null) {
            mTtnPlayerListener!!.releaseTtnPlayerCalled()
        }
        if (mPlayer != null) {
            updateResumePosition()
            removeThumbImageView()
            simpleCache?.release()
            mHandler.removeCallbacks(updateUI)
            mPlayer!!.release()
            mPlayer = null
            mDrmSessionManager?.release()
        }
    }

    override fun seekToDefaultPosition() {
        if (mPlayer != null) {
            mPlayer!!.seekToDefaultPosition()
        }
    }

    override fun seekToStartPosition() {
        if (mPlayer != null) {
            mPlayer!!.seekTo(START_POSITION)
        }
    }

    /**
     * This method will stop media player, set the seek position to default and reset isPlayerPrepared
     * flag to default so you can add new url to play and prepare again.
     * */
    override fun playerStop() {
        seekToDefaultPosition()
        clearResumePosition()
        isPlayerPrepared = false
        mPlayer?.stop()
    }

    override fun releaseAdsLoader() {
        if (mImaAdsLoader != null) {
            mImaAdsLoader!!.release()
            mImaAdsLoader = null
            val layout = mTtnPlayerView.overlayFrameLayout
            layout?.removeAllViews()
        }
    }

    override fun updateVideoUrls(urls: ArrayList<String>) {
        if (!isPlayerPrepared) {
            setVideoUrls(urls)
            mMediaSource = createMediaSource()
        } else {
            throw IllegalStateException("Can't update url's when player is prepared")
        }
    }

    override fun playerPause() {
        e(TAG, "inside playerPause")
        if (mPlayer != null) {
            mPlayer!!.playWhenReady = false
        }
    }

    fun isPlaying(): Boolean {
        return mPlayer?.isPlaying ?: false
    }

    override fun playerPlay() {
        e(TAG, "inside playerPlay")
        if (mPlayer != null) {
            mPlayer!!.playWhenReady = true
        }
    }

    override fun playerNext() {
        if (mPlayer != null) {
            seekTo(nextWindowIndex, 0)
        }
    }

    override fun playerPrevious() {
        if (mPlayer != null) {
            seekTo(previousWindowIndex, 0)
        }
    }

    override fun seekTo(windowIndex: Int, positionMs: Long) {
        if (mPlayer != null) {
            mPlayer!!.seekTo(windowIndex, positionMs)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun playerBlock() {
        if (mTtnPlayerView.overlayFrameLayout != null) {
            mTtnPlayerView.overlayFrameLayout!!.setOnTouchListener(this)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun playerUnBlock() {
        if (mTtnPlayerView.overlayFrameLayout != null) {
            mTtnPlayerView.overlayFrameLayout!!.setOnTouchListener(null)
        }
    }

    override fun setFullScreenBtnVisibility(isVisible: Boolean) {
        mBtnFullScreen!!.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun setVolumeControlListener(volumeControlListener: TtnPlayerVolumeControlListener?) {
        mVolumeControlListener = volumeControlListener
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(PARAM_IS_AD_WAS_SHOWN, isPlayingAd)
        outState.putBoolean(PARAM_AUTO_PLAY, isAutoPlayOn)
        outState.putInt(PARAM_WINDOW, mResumeWindow)
        outState.putLong(PARAM_POSITION, mResumePosition)
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters)
        outState.putString(KEY_SELECTED_AUDIO_TRACK, mSelectedAudioTrack)
        outState.putString(KEY_SELECTED_VIDEO_TRACK, mSelectedVideoTrack)
        outState.putString(KEY_SELECTED_SUBTITLE_TRACK, mSelectedSubtitleTrack)
    }

    override fun onActivityStart() {
        if (Util.SDK_INT > 23) {
            createPlayer(isToPrepareOnResume)
        }
    }

    override fun onBackPressed() {
        handleEnterExitFullScreenButton()

    }

    override fun onActivityResume() {
        //for securing content from capturing screenshot and recording video
        if(!mDialogShowing) {

//            (mContext as Activity).window.setFlags(
//                WindowManager.LayoutParams.FLAG_SECURE,
//                WindowManager.LayoutParams.FLAG_SECURE
//            )
            if (Util.SDK_INT <= 23 || mPlayer == null) {
                createPlayer(isToPrepareOnResume)
            }
        }
    }

    override fun onActivityPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        mOrientation = newConfig.orientation
        updatePlayerWidthHeight()
        updateBtnFullScreenImage()
        setFullScreenFlag(mOrientation)
        hideNavigationAndStatusBar()
    }

    private fun setOrientation() {
        when (mOrientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                changeOrientationToPortrait()
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                changeOrientationToLandscape()
            }
        }
    }

    private fun setOrientationFromEnterExitButton() {
        when (mOrientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                changeOrientationToLandscape()
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                changeOrientationToPortrait()
            }
        }
    }

    override fun onOrientationChanged(newOrientation: Int) {
        mOrientation = newOrientation
        setOrientation()
        updatePlayerWidthHeight()
        updateBtnFullScreenImage()
        setFullScreenFlag(mOrientation)
        hideNavigationAndStatusBar()

    }

    override fun onActivityStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onActivityDestroy() {
        (mContext as Activity).window.clearFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )
        releaseAdsLoader()
    }

    /**
     * ExoPlayerStatus interface methods
     */
    override val isPlayerCreated: Boolean
        get() = mPlayer != null

    override val currentWindowIndex: Int
        get() = if (mPlayer != null) {
            mPlayer!!.currentWindowIndex
        } else {
            0
        }

    override val currentPosition: Long
        get() = if (mPlayer != null) {
            mPlayer!!.currentPosition
        } else {
            0
        }

    override val duration: Long
        get() = if (mPlayer != null) {
            mPlayer!!.duration
        } else {
            0
        }

    override val isPlayingAd: Boolean
        get() = mPlayer != null && mPlayer!!.isPlayingAd

    /**
     * ExoPlayer Player.EventListener methods
     */
    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        if (mTtnPlayerListener != null) {
            mTtnPlayerListener!!.onTracksChanged(
                trackGroups,
                trackSelections
            )
        }
//        handleVisibilityOfTrackMenus()
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        onPlayerLoadingChanged()
        if (mTtnPlayerListener != null) {
            mTtnPlayerListener!!.onLoadingStatusChanged(
                isLoading,
                mPlayer?.bufferedPosition?:0,
                mPlayer?.bufferedPercentage?:0
            )
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (mTtnPlayerListener == null || mPlayer == null) {
            return
        }
        e(TAG, "onPlayerStateChanged unknown: $playbackState, playWhenReady:$playWhenReady")
        when (playbackState) {
            Player.STATE_READY -> {
                playerUnBlock()
                if (playWhenReady) {
                    mTtnPlayerView.controllerAutoShow = false
                    mTtnPlayerView.controllerHideOnTouch = true
                    hideUiControls()
                    mPlayBackStateLoaded = true
                    isBuffering = false
                    onPlayerPlaying()
                    mTtnPlayerListener!!.onPlayerPlaying(mPlayer!!.currentWindowIndex)
                } else {
                    mTtnPlayerView.controllerAutoShow = true
                    mTtnPlayerView.controllerHideOnTouch = false
                    onPlayerPaused()
                    mTtnPlayerListener!!.onPlayerPaused(mPlayer!!.currentWindowIndex)
                }
            }
            Player.STATE_BUFFERING -> {
                isBuffering = true
                onPlayerBuffering()
                mTtnPlayerListener!!.onPlayerBuffering(mPlayer!!.currentWindowIndex)
            }
            Player.STATE_ENDED ->{
                mTtnPlayerListener!!.onPlayerStateEnded(mPlayer!!.currentWindowIndex)
                mTtnPlayerView.controllerAutoShow = true
                mTtnPlayerView.controllerHideOnTouch = false
                showUiControls()
            }
            Player.STATE_IDLE -> mTtnPlayerListener!!.onPlayerStateIdle(mPlayer!!.currentWindowIndex)
            else -> e(TAG, "onPlayerStateChanged unknown: $playbackState")
        }
    }

    override fun onPlayerError(e: ExoPlaybackException) {
        setProgressVisible(false)
        var errorString: String? = null
        if(isLiveStreamSupportEnabled){
            if (isBehindLiveWindow(e)) {
                createPlayer(true)
            }
            else
                mTtnPlayerListener?.onTTNPlayerError(e)
        }
        else
            mTtnPlayerListener?.onTTNPlayerError(e)
//        when (e.type) {
//            ExoPlaybackException.TYPE_SOURCE -> {
//                val ex = e.sourceException
//                var msg = ex.localizedMessage
//                if (ex.cause is UnknownHostException) {
//                    msg = "Please check your internet connection"
//                } else if (ex is HttpDataSource.InvalidResponseCodeException) {
//                    msg = "Cannot play requested content, please try again"
//                } else if (e.cause is DrmSession.DrmSessionException) {
//                    msg = null
//                }
//                if (msg != null) {
//                    e(TAG, msg)
//                    errorString = msg
//                }
//            }
//            ExoPlaybackException.TYPE_RENDERER -> {
//                val exception = e.rendererException
//                if (exception.message != null) {
//                    e(TAG, "Renderer Exception: " + exception.message)
//                }
//            }
//            ExoPlaybackException.TYPE_UNEXPECTED -> {
//                val runtimeException = e.unexpectedException
//                e(TAG, "Runtime Exception: " + runtimeException.message)
//                if (runtimeException.message == null) {
//                    runtimeException.printStackTrace()
//                }
//                errorString = runtimeException.message
//            }
//            ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
//            }
//            ExoPlaybackException.TYPE_REMOTE -> {
//            }
//        }
//        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
//            val cause = e.rendererException
//            if (cause is DecoderInitializationException) { // Special case for decoder initialization failures.
//                errorString = if (cause.codecInfo == null) {
//                    if (cause.cause is DecoderQueryException) {
//                        mContext.getString(R.string.error_querying_decoders)
//                    } else if (cause.secureDecoderRequired) {
//                        mContext.getString(
//                            R.string.error_no_secure_decoder,
//                            cause.mimeType
//                        )
//                    } else {
//                        mContext.getString(
//                            R.string.error_no_decoder,
//                            cause.mimeType
//                        )
//                    }
//                } else {
//                    mContext.getString(
//                        R.string.error_instantiating_decoder,
//                        cause.diagnosticInfo
//                    )
//                }
//            }
//        }
//        if (errorString != null) {
//            e(TAG, "errorString: $errorString")
//            showErrorMessageView(errorString)
//            hideUiControls()
////            if (mTtnPlayerListener != null) {
////                mTtnPlayerListener!!.onPlayerError(errorString)
////            }
//        }

    }

    override fun onRepeatModeChanged(repeatMode: Int) {}
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
    override fun onPositionDiscontinuity(reason: Int) {}
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    override fun onSeekProcessed() {

    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {}

    override fun onVolumeChanged(p0: AdMediaInfo?, p1: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResume(p0: AdMediaInfo?) {
        if (mTtnPlayerAdListener != null) {
            mTtnPlayerAdListener!!.onAdResume()
        }
    }

    override fun onPause(p0: AdMediaInfo?) {
        if (mTtnPlayerAdListener != null) {
            mTtnPlayerAdListener!!.onAdPause()
        }
        mTtnPlayerView.removeCallbacks(checkFreeze)
    }

    override fun onAdProgress(p0: AdMediaInfo?, p1: VideoProgressUpdate?) {
        //TODO("Not yet implemented")
    }

    override fun onLoaded(p0: AdMediaInfo?) {
    }

    override fun onBuffering(p0: AdMediaInfo?) {
        if (mTtnPlayerAdListener != null) {
            mTtnPlayerAdListener!!.onBuffering()
        }
    }

    override fun onError(p0: AdMediaInfo?) {
        if (mTtnPlayerAdListener != null) {
            mTtnPlayerAdListener!!.onAdError()
        }
    }

    override fun onEnded(p0: AdMediaInfo?) {
        onAdEnded()
        if (mTtnPlayerAdListener != null) {
            mTtnPlayerAdListener!!.onAdEnded()
        }
    }

    override fun onContentComplete() {
        //TODO("Not yet implemented")
    }

    override fun onPlay(p0: AdMediaInfo?) {
        if (mTtnPlayerAdListener != null) {
            mTtnPlayerAdListener!!.onAdPlay()
        }
    }

    override fun setDrmSessionManager(drmSessionManager: DrmSessionManager<*>?): MediaSourceFactory {
        TODO("Not yet implemented")
    }

    /**
     * AdsMediaSource.MediaSourceFactory
     */
    override fun createMediaSource(uri: Uri): MediaSource {
        return buildMediaSource(uri)
    }

    override fun getSupportedTypes(): IntArray {
        return intArrayOf(C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER, C.TYPE_SS)
    }

    companion object {
        const val PARAM_AUTO_PLAY = "PARAM_AUTO_PLAY"
        const val PARAM_WINDOW = "PARAM_WINDOW"
        const val PARAM_POSITION = "PARAM_POSITION"
        const val PARAM_IS_AD_WAS_SHOWN = "PARAM_IS_AD_WAS_SHOWN"
        const val KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters"
        const val KEY_SELECTED_AUDIO_TRACK = "selected_audio_track"
        const val KEY_SELECTED_VIDEO_TRACK = "selected_video_track"
        const val KEY_SELECTED_SUBTITLE_TRACK = "selected_subtitle_track"
        const val VIDEO_TRACK: Int = 0
        const val AUDIO_TRACK: Int = 1
        const val SUBTITLE_TRACK: Int = 2
        const val START_POSITION: Long = 0
        const val DEFAULT_ORIENTATION: Int = Configuration.ORIENTATION_PORTRAIT
        const val SECURITY_LEVEL = "securityLevel"


        private fun isBehindLiveWindow(e: ExoPlaybackException): Boolean {
            if (e.type != ExoPlaybackException.TYPE_SOURCE) {
                return false
            }
            var cause: Throwable? = e.sourceException
            while (cause != null) {
                if (cause is BehindLiveWindowException) {
                    return true
                }
                cause = cause.cause
            }
            return false
        }

        private const val TAG: String = "TtnPlayerHelper"
    }

    private fun initOrientationManger() {
        orientationManager = OrientationManager.getInstance(mContext)
        orientationManager?.setOrientationChangedListener(this)
        orientationManager?.enable()
    }

    private fun getCurrentOrientation() {
        mOrientation = mContext.resources.configuration.orientation
    }

    override fun hideNavigationAndStatusBar() {
        if (mHidePhoneNavigationAndStatusBar) {
            val flagsLandScapeMode = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            val flagsPortraitScapeMode = View.SYSTEM_UI_FLAG_VISIBLE

            if (mIsInFullScreenMode) {
                (mContext as Activity).window?.decorView?.apply {
                    systemUiVisibility = flagsLandScapeMode
                }
            } else {
                (mContext as Activity).window?.decorView?.apply {
                    systemUiVisibility = flagsPortraitScapeMode
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return mTtnPlayerView.dispatchKeyEvent(event)
    }

    override fun hidePlayerSeekBar() {
        mDefaultTimeBar?.visibility = View.INVISIBLE
    }

    override fun showPlayerSeekBar() {
        mDefaultTimeBar?.visibility = View.VISIBLE
    }

    override fun setPlayerStartOverButtonVisibility(visibility: Int) {
        mIvStartOver?.visibility = visibility
    }

    override fun disableTimeSeekBarTouch(value: Boolean) {
        mDefaultTimeBar?.setOnTouchListener(OnTouchListener { v, event ->
            return@OnTouchListener value
        })
    }

    override fun onAudioFocusChange(focusChange: Int) {

    }

    override fun setHandleAudioBecomingNoisy(value: Boolean) {
        mPlayer?.setHandleAudioBecomingNoisy(value)
    }

    /**
     * AdEvent.AdEventListener
     */
    override fun onAdEvent(adEvent: AdEvent) {
        if (mTtnPlayerAdListener == null) {
            return
        }
        if (adEvent.type == AdEvent.AdEventType.TAPPED) {
            mTtnPlayerAdListener!!.onAdTapped()
        }

        if (adEvent.type == AdEvent.AdEventType.CLICKED) {
            mTtnPlayerAdListener!!.onAdClicked()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_full_screen_enter_exit -> {
                mTtnPlayerListener?.onFullScreenBtnTap()
                if (!mIsFullScreenButtonFunctionalityOverridden) {
                    handleEnterExitFullScreenButton()
                }
            }
            R.id.tv_video_quality -> {
                /*if (mPlayBackStateLoaded) {
                    showVideoQualityDialog(
                        trackSelector,
                        VIDEO_TRACK,
                        null,
                        false,
                        allowAdaptiveSelections = false
                    )
                    mTtnPlayerListener?.autoPauseOnDialog()
                }*/
//                mTtnPlayerListener?.tapOnAudioQuality()
            }
            R.id.tv_video_language -> {
                /*if (mPlayBackStateLoaded) {
                    showLanguageAndSubtitleDialog(trackSelector)
                    mTtnPlayerListener?.autoPauseOnDialog()
                }*/
//                mTtnPlayerListener?.tapOnVideoQuality()
            }
            R.id.iv_start_over -> {
                seekToStartPosition()
            }
            R.id.tv_add_to_watchlist -> {
                mTtnPlayerListener?.toggleWatchlisted()
            }
            R.id.tv_live -> {
                goLive()
            }
        }
    }

    private fun getLivePosition() =
        duration - DEFAULT_MIN_BUFFER_MS

    private fun goLive() {
        handleGoLiveButton(showGoLive = false)
        seekTo(currentWindowIndex, getLivePosition())
        playerPlay()
    }

    override fun preparePlayback() {
        hideErrorMessageView()
        mPlayer?.retry()
    }

    override fun onVisibilityChange(visibility: Int) {
        mTtnPlayerListener?.onPlayerUiControlVisibilityChange(visibility)
    }

    fun getPlayer(): SimpleExoPlayer? {
        return mPlayer
    }

    fun getBandwidthMeter(): DefaultBandwidthMeter{
        return bandwidthMeter
    }


    override fun onScrubMove(timeBar: TimeBar, position: Long) {
        mTtnPlayerListener?.onScrubMove(timeBar, position)
        updateCurrentDragPosition(position)
    }

    override fun onScrubStart(timeBar: TimeBar, position: Long) {
        mTtnPlayerListener?.onScrubStart(timeBar, position)
        mTtnPlayerView.controllerShowTimeoutMs = 0
        showUiControls()
        e(TAG, "inside onScrubStart")
//        mPlayer?.playWhenReady=false
    }

    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
        mTtnPlayerListener?.onScrubStop(timeBar, position, canceled)
        mTtnPlayerView.controllerShowTimeoutMs = mControllerTime.toInt()
        timeBar.setPosition(position)
        mTvCurrentDuration?.visibility = View.GONE
//        mPlayer?.playWhenReady=true
    }

    fun setPlayerView(ttnPlayerView: TtnPlayerView) {
        mTtnPlayerView = ttnPlayerView
        setVideoClickable()
        initViews()
        setControllerListener()
    }

    private fun updateCurrentDragPosition(position:Long){
        mTvCurrentDuration?.text =
            Util.getStringForTime(formatBuilder, Formatter(formatBuilder, Locale.getDefault()), position)
        val l =
            (position.toFloat() / (mPlayer?.duration ?: Long.MAX_VALUE))
        // position of thumb
        val currentTimeBar : View? = if(mIsInFullScreenMode)
            mDefaultTimeBar
        else
            mMiniTimeBar as View?
        val l1 = l * (currentTimeBar?.width ?: 1)

        val xOfExoPosition = ((currentTimeBar?.x
            ?: 0f) + l1) - ((mTvCurrentDuration?.width ?: 1) / 2)
        mTvCurrentDuration?.x =
            if (xOfExoPosition < (currentTimeBar?.x ?: 0f))
                currentTimeBar?.x ?: 0f
            else if (xOfExoPosition > (((currentTimeBar?.x?:0f) + (currentTimeBar?.width?:0)) - (mTvCurrentDuration?.width?:0)))
                (((currentTimeBar?.x?:0f) + (currentTimeBar?.width?:0)) - (mTvCurrentDuration?.width?:0))
            else
                xOfExoPosition
        currentTimeBar?.post {
            val point = IntArray(2)
            currentTimeBar.getLocationOnScreen(point)
            val yOfExoPosition = point[1] - ((mTvCurrentDuration?.height ?: 0) * 1.25)
            mTvCurrentDuration?.y = if(mIsInFullScreenMode)
                yOfExoPosition.toFloat()
            else
                currentTimeBar.y - ((mTvCurrentDuration?.height ?: 0) * 1.25).toFloat()
        }
        mTvCurrentDuration?.visibility = View.VISIBLE
    }

    override fun onContentMarkedFavourite(icon: Drawable?) {
        mIvWatchList?.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
    }

    fun getTrackSelector() : DefaultTrackSelector{
        return trackSelector
    }

    override fun onCues(cues: MutableList<Cue>) {
//        val newCues = cues.map {
//            Cue(
//                it.text!!,
//                it.textAlignment,
//                0.85F,
//                Cue.LINE_TYPE_FRACTION,
//                it.lineAnchor,
//                .1f,
//                it.positionAnchor,
//                it.size,
//                it.textSizeType,
//                it.textSize
//            )
//        }
//        mTtnPlayerView.subtitleView?.setCues(newCues)
        mTtnPlayerView.subtitleView?.onCues(cues)
    }
}
