//package com.tatasky.binge.ui.features.player
//
//import android.content.Intent
//import android.net.NetworkInfo
//import android.os.Bundle
//import android.os.CountDownTimer
//import android.view.Surface
//import android.view.View
//import android.view.WindowManager
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelStoreOwner
//import androidx.navigation.fragment.navArgs
//import com.google.android.exoplayer2.*
//import com.google.android.exoplayer2.analytics.AnalyticsListener
//import com.google.android.exoplayer2.decoder.DecoderCounters
//import com.google.android.exoplayer2.metadata.Metadata
//import com.google.android.exoplayer2.source.MediaSourceEventListener
//import com.google.android.exoplayer2.source.TrackGroupArray
//import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
//import com.google.android.exoplayer2.trackselection.TrackSelectionArray
//import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
//import com.google.android.exoplayer2.ui.DefaultTimeBar
//import com.google.android.exoplayer2.ui.PlayerControlView
//import com.google.android.exoplayer2.ui.TimeBar
//import com.google.android.exoplayer2.util.Util
//import com.tatasky.binge.R
//import com.tatasky.binge.analytics.ANALYTICS_TIME_FORMAT
//import com.tatasky.binge.analytics.PARA_ERROR_TYPE_PLAYER
//import com.tatasky.binge.analytics.PARA_PI_ERROR_ORIGIN
//import com.tatasky.binge.data.networking.models.ErrorModel
//import com.tatasky.binge.data.networking.models.response.ContentItem
//import com.tatasky.binge.data.networking.models.response.RrmSessionInfo
//import com.tatasky.binge.databinding.FragmentPlayerBinding
//import com.tatasky.binge.helper.imageLoad
//import com.tatasky.binge.interfaces.CommonDialogEventListener
//import com.tatasky.binge.shemaroo.helper.ShemarooHelper
//import com.tatasky.binge.ui.base.frameworks.extensions.hide
//import com.tatasky.binge.ui.base.frameworks.extensions.invisible
//import com.tatasky.binge.ui.base.frameworks.extensions.show
//import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
//import com.tatasky.binge.ui.features.dialog.DialogModel
//import com.tatasky.binge.ui.features.player.ExoPlayerImpl.PLAYER_CONTROLLER_SHOW_TIMEOUT_MS
//import com.tatasky.binge.ui.features.player.listeners.DeviceRegistrationListener
//import com.tatasky.binge.ui.features.player.listeners.PlayerDurationWatcher
//import com.tatasky.binge.ui.features.player.listeners.PlayerListener
//import com.tatasky.binge.ui.features.player.model.Bitrate
//import com.tatasky.binge.ui.features.player.model.VideoQuality
//import com.tatasky.binge.utils.*
//import kotlinx.android.synthetic.main.fragment_player.*
//import kotlinx.android.synthetic.main.player_controls_mobile.*
//import kotlinx.android.synthetic.main.player_view.*
//import java.io.IOException
//import java.util.*
//import kotlin.collections.ArrayList
//
//class PlayerFragment : PlayerBaseFragment<FragmentPlayerBinding>(),
//    PlaybackPreparer, PlayerListener.EventListener,
//    PlayerListener.DefaultDrmSessionEventListener, PlayerListener.AnalyticsEventListener,
//    DeviceRegistrationListener,
//    PlayerListener.TimeChangeListener, TimeBar.OnScrubListener,
//    PlayerControlView.VisibilityListener {
//
//    private var simpleExoPlayer: SimpleExoPlayer? = null
//    private var playerDurationWatcher: PlayerDurationWatcher? = null
//
//    override fun restartPlayerAfterNetworkAvailable() {
//        e("SessionId","Inside restartPlayerAfterNetworkAvailable")
//        val isDialog = dialog?.isShowing ?: false
//        activity?.runOnUiThread {
//            hideNetworkView()
//            if(simpleExoPlayer == null){
//                initializePlayer()
//            }
//            else if(!isDialog && isBuffering) {
//                playerModel?.let {
//                    exoPlayer?.reInitialize(it, getCurrentPosition())
//                }
//            }
//        }
//    }
//
//    override fun retryPlayer() {
//        activity?.runOnUiThread{
//            if(simpleExoPlayer == null){
//                initializePlayer()
//            }
//            else{
//                playerModel?.let {
//                    exoPlayer?.reInitialize(it, getCurrentPosition())
//                }
//            }
//        }
//    }
//
//    override fun toBeCalledOnce() {
//    }
//
//    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
//        e("PlayerFragment", "inside onTimelineChanged")
//    }
//
//    override fun onTracksChanged(
//        trackGroups: TrackGroupArray?,
//        trackSelections: TrackSelectionArray?
//    ) {
//        e("DefaultLanguage", "inside onTracksChanged")
////        exoPlayer?.defaultLanguage
//        if(exoPlayer?.availableVideoQuality == null)
//            exoPlayer?.initVideo(trackGroups, trackSelections)
//    }
//
//    private fun getCurrentPosition(): Long {
//        return (exoPlayer?.player?.currentPosition) ?: 0
//    }
//
//    private fun enableDisableAudioOption(view: View, isEnable: Boolean) {
//        if (isEnable) {
//            view.alpha = 1f
//        } else {
//            view.alpha = 0.3f
//        }
//        view.isEnabled = isEnable
//        view.isFocusable = isEnable
//    }
//
//    override fun initializePlayerHelper(savedInstanceState: Bundle?) {
//        networkErrorView = playerBinding.networkError
//        errorView = playerBinding.errorView
////        playerViewModel = viewModel
//        playerModel = args.playerData
//        //create instance of TtnPlayerHelper
//        var ticket = ""//""66A7A8CABDC87589"
//        var sessionId = ""//""1DDD9DD4D349A436"
//        //session_id :1DDD9DD4D349A436
//        //ticket : 66A7A8CABDC87589
//        rrmSessionInfo = viewModel.sharedPrefs.getRrmSessionInfo()
//        if (rrmSessionInfo != null) {
//            sessionId = rrmSessionInfo!!.sessionId.toString()
//            ticket = rrmSessionInfo!!.ticket.toString()
//        }
//        if (playerModel!!.isEncrypted()) {
//            playerModel!!.setDrmLicenseUrl(playerModel!!.getLA_URL() + "&SessionId=" + sessionId + "&Ticket=" + ticket)
//        }
//        //ttnPlayerHelper = TtnPlayerHelper(requireContext())
//    }
//
//    override fun onLoadingChanged(isLoading: Boolean) {
//    }
//
//    override fun onBuffering() {
//    }
//
//    override fun onBufferingEnded() {
//        activity?.runOnUiThread {
//            player_view.findViewById<ConstraintLayout>(R.id.widgetController).show()
//        }
//    }
//
//    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//        e(
//            "PlayerFrgament", "isResume : $isResume," +
//                    "isPause : $isPause, " +
//                    " playWhenReady : $playWhenReady, " +
//                    " playbackState: $playbackState"
//        )
//        if (playbackState == Player.STATE_IDLE) {
//            isBuffering = true
//        }
//        else if (playbackState == Player.STATE_READY) {
//            isBuffering = false
//            playerBinding.playerView.controllerShowTimeoutMs = CONTROLLER_HIDE_TIME_MS.toInt()
//            iv_thumbnail.hide()
//            if (firstTimeCW) {
//
//                playerDurationWatcher?.start()
//                player_view.findViewById<ConstraintLayout>(R.id.widgetController).show()
//                player_view.findViewById<ConstraintLayout>(R.id.llReplay).hide()
//                firstTimeCW = false
//                actionCWhandler.postDelayed(cwHitRunner, 10000)
//                intialBufferDuration = (System.currentTimeMillis() - startInitialBufferTime) / 1000
//                startTime = getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT)
//                firstPlayStartTime = System.currentTimeMillis()
//                trackInitialBuffering(
//                    intialBufferDuration.toString(),
//                    (intialBufferDuration / 60).toString(),
//                    playerModel
//                )
//                setDefaultSubtitle()
//            }
//        }
//        when {
//            playbackState == Player.STATE_BUFFERING -> {
//                startInitialBufferTime = System.currentTimeMillis()
//                isBuffering = true
//                viewModel.showLoader()
//                player_view.findViewById<ConstraintLayout>(R.id.llReplay).hide()
//                //ll_player_menu
//                player_view.findViewById<ConstraintLayout>(R.id.widgetController).invisible()
//            }
//            playbackState == Player.STATE_ENDED -> {
//                player_view.findViewById<ConstraintLayout>(R.id.widgetController).show()
//                viewModel.hideLoader()
//                playerEnded()
//                //parentActivity.finish()
//            }
//            playWhenReady && playbackState == Player.STATE_READY -> {//play state
//                if (isResume)
//                    trackResumeAndInitPlayer(playerModel!!)
//                isResume = false
//                isPause = true
//                viewModel.hideLoader()
//                player_view.findViewById<ConstraintLayout>(R.id.widgetController).show()
//            }
//            playWhenReady && playbackState != Player.STATE_READY -> {//play state but buffering time
//                player_view.findViewById<ConstraintLayout>(R.id.widgetController).invisible()
//            }
//            !playWhenReady && playbackState == Player.STATE_READY -> {//pause state
//                playerBinding.playerView.controllerShowTimeoutMs = 0
//                if (isPause)
//                    trackOnPause(playerModel!!)
//                isResume = true
//                isPause = false
//                viewModel.hideLoader()
//                player_view.findViewById<ConstraintLayout>(R.id.widgetController).show()
//            }
//        }
//    }
//
//    private fun playerEnded() {
//        isPlayerEnded = true
//        if (nextEpisodeAvailable) {
//            playerBinding.playerView.hide()
//            playerBinding.nextEpisodeScreen.clRoot.show()
//            viewModel.timer?.start()
//        } else {
//            playerModel?.getImage()?.let {
//                val url = getCloudinaryUrl(
//                    viewModel.sharedPrefs.getCloudenieryUrl(),
//                    getDeviceDimension(requireContext()).x, getDeviceDimension(requireContext()).y,
//                    it
//                )
//                imageLoad(iv_thumbnail, url)
//            }
//            iv_thumbnail.show()
//            player_view.findViewById<ConstraintLayout>(R.id.llReplay).show()
//            firstTimeCW = true
//        }
//        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.widgetController).hide()
//        actionCWhandler.removeCallbacks(cwHitRunner)
//        val totalDuration = exoPlayer!!.player.duration.toInt() / 1000
//        publishWatchedContent(null, totalDuration, totalDuration, viewModel)
////        releasePlayer()
//    }
//
//    override fun onRepeatModeChanged(repeatMode: Int) {
//    }
//
//    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
//    }
//
//    override fun onPlayerError(error: ExoPlaybackException?) {
//        handlePlayerError(error)
//    }
//
//    override fun onPositionDiscontinuity(reason: Int) {
//    }
//
//    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
//    }
//
//    override fun onSeekProcessed() {
//    }
//
//    override fun onPlayerStateChanged(
//        eventTime: AnalyticsListener.EventTime?,
//        playWhenReady: Boolean,
//        playbackState: Int
//    ) {
//    }
//
//    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime?, reason: Int) {
//    }
//
//    override fun onPositionDiscontinuity(eventTime: AnalyticsListener.EventTime?, reason: Int) {
//    }
//
//    override fun onSeekStarted(eventTime: AnalyticsListener.EventTime?) {
//    }
//
//    override fun onSeekProcessed(eventTime: AnalyticsListener.EventTime?) {
//    }
//
//    override fun onPlaybackParametersChanged(
//        eventTime: AnalyticsListener.EventTime?,
//        playbackParameters: PlaybackParameters?
//    ) {
//    }
//
//    override fun onRepeatModeChanged(eventTime: AnalyticsListener.EventTime?, repeatMode: Int) {
//    }
//
//    override fun onShuffleModeChanged(
//        eventTime: AnalyticsListener.EventTime?,
//        shuffleModeEnabled: Boolean
//    ) {
//    }
//
//    override fun onLoadingChanged(eventTime: AnalyticsListener.EventTime?, isLoading: Boolean) {
//    }
//
//    override fun onPlayerError(
//        eventTime: AnalyticsListener.EventTime?,
//        error: ExoPlaybackException?
//    ) {
//    }
//
//    override fun onTracksChanged(
//        eventTime: AnalyticsListener.EventTime?,
//        trackGroups: TrackGroupArray?,
//        trackSelections: TrackSelectionArray?
//    ) {
//    }
//
//    override fun onLoadStarted(
//        eventTime: AnalyticsListener.EventTime?,
//        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
//        mediaLoadData: MediaSourceEventListener.MediaLoadData?
//    ) {
//    }
//
//    override fun onLoadCompleted(
//        eventTime: AnalyticsListener.EventTime?,
//        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
//        mediaLoadData: MediaSourceEventListener.MediaLoadData?
//    ) {
//    }
//
//    override fun onLoadCanceled(
//        eventTime: AnalyticsListener.EventTime?,
//        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
//        mediaLoadData: MediaSourceEventListener.MediaLoadData?
//    ) {
//    }
//
//    override fun onLoadError(
//        eventTime: AnalyticsListener.EventTime?,
//        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
//        mediaLoadData: MediaSourceEventListener.MediaLoadData?,
//        error: IOException?,
//        wasCanceled: Boolean
//    ) {
//    }
//
//    override fun onDownstreamFormatChanged(
//        eventTime: AnalyticsListener.EventTime?,
//        mediaLoadData: MediaSourceEventListener.MediaLoadData?
//    ) {
//    }
//
//    override fun onUpstreamDiscarded(
//        eventTime: AnalyticsListener.EventTime?,
//        mediaLoadData: MediaSourceEventListener.MediaLoadData?
//    ) {
//    }
//
//    override fun onMediaPeriodCreated(eventTime: AnalyticsListener.EventTime?) {
//    }
//
//    override fun onMediaPeriodReleased(eventTime: AnalyticsListener.EventTime?) {
//    }
//
//    override fun onReadingStarted(eventTime: AnalyticsListener.EventTime?) {
//    }
//
//    override fun onBandwidthEstimate(
//        eventTime: AnalyticsListener.EventTime?,
//        totalLoadTimeMs: Int,
//        totalBytesLoaded: Long,
//        bitrateEstimate: Long
//    ) {
//    }
//
//    override fun onViewportSizeChange(
//        eventTime: AnalyticsListener.EventTime?,
//        width: Int,
//        height: Int
//    ) {
//    }
//
//    override fun onNetworkTypeChanged(
//        eventTime: AnalyticsListener.EventTime?,
//        networkInfo: NetworkInfo?
//    ) {
//    }
//
//    override fun onMetadata(eventTime: AnalyticsListener.EventTime?, metadata: Metadata?) {
//    }
//
//    override fun onDecoderEnabled(
//        eventTime: AnalyticsListener.EventTime?,
//        trackType: Int,
//        decoderCounters: DecoderCounters?
//    ) {
//    }
//
//    override fun onDecoderInitialized(
//        eventTime: AnalyticsListener.EventTime?,
//        trackType: Int,
//        decoderName: String?,
//        initializationDurationMs: Long
//    ) {
//    }
//
//    override fun onDecoderInputFormatChanged(
//        eventTime: AnalyticsListener.EventTime?,
//        trackType: Int,
//        format: Format?
//    ) {
//    }
//
//    override fun onDecoderDisabled(
//        eventTime: AnalyticsListener.EventTime?,
//        trackType: Int,
//        decoderCounters: DecoderCounters?
//    ) {
//    }
//
//    override fun onAudioSessionId(eventTime: AnalyticsListener.EventTime?, audioSessionId: Int) {
//    }
//
//    override fun onAudioUnderrun(
//        eventTime: AnalyticsListener.EventTime?,
//        bufferSize: Int,
//        bufferSizeMs: Long,
//        elapsedSinceLastFeedMs: Long
//    ) {
//    }
//
//    override fun onDroppedVideoFrames(
//        eventTime: AnalyticsListener.EventTime?,
//        droppedFrames: Int,
//        elapsedMs: Long
//    ) {
//    }
//
//    override fun onVideoSizeChanged(
//        eventTime: AnalyticsListener.EventTime?,
//        width: Int,
//        height: Int,
//        unappliedRotationDegrees: Int,
//        pixelWidthHeightRatio: Float
//    ) {
//    }
//
//    override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime?, surface: Surface?) {
//    }
//
//    override fun onDrmKeysLoaded(eventTime: AnalyticsListener.EventTime?) {
//    }
//
//    override fun onDrmSessionManagerError(
//        eventTime: AnalyticsListener.EventTime?,
//        error: java.lang.Exception?
//    ) {
//
//    }
//
//    override fun onDrmKeysRestored(eventTime: AnalyticsListener.EventTime?) {
//    }
//
//    override fun onDrmKeysRemoved(eventTime: AnalyticsListener.EventTime?) {
//    }
//
//    override fun onDrmKeysLoaded() {
//    }
//
//    override fun onDrmSessionManagerError(error: java.lang.Exception?) {
//        e(
//            TAG,
//            "${getErrorCode(error!!)} :onDrmSessionManagerError: $error.message"
//        )
//        activity?.runOnUiThread {
//            onBufferingEnded()
//            if (isDeviceLimitReached) {
//                releasePlayer()
//                updateErrorModel(
//                    ERROR_CODE_DEVICE_LIMIT,
//                    parseError(ERROR_CODE_DEVICE_LIMIT)
//                )
//            }
//            else {
//                isRRMError = true
//                getErrorCode(error)
//                error.message
//                updateErrorModel(
//                    ERROR_CODE_DEVICE_LIMIT,
//                    parseError(ERROR_CODE_DEVICE_LIMIT)
//                )
//                try {
//                    if (sessionRenewCount == 1) {
//                        releasePlayer()
////                fetchSubscriber(errorCode, message)
//                    } else {
//                        if (sessionRenewCount < 1) {
//                            sessionRenewCount++
////                    handleSessionExpire(this)
//                        }
//                    }
//                } catch (e: Exception) {
//                    e(TAG, " :onDrmSessionManagerError: Exception  catch")
//
//                }
//            }
//        }
//    }
//
//    override fun onDrmKeysRestored() {
//    }
//
//    override fun onDrmKeysRemoved() {
//    }
//
//    override fun onDeviceIdReceived(deviceId: String?) {
//    }
//
//    override fun onRRMError(rrmError: String?) {
//        e(
//            TAG,
//            "onRRMError: $rrmError"
//        )
//        if (isDeviceLimitReached) {
//            return
//        }
//        if (rrmError.equals("DEVICE_LIMIT_EXCEEDED", ignoreCase = true)) {
//            isDeviceLimitReached = true
//        }
//        if (isRRMError) {
//            return
//        }
//        isRRMError = true
//    }
//
//    override fun onScrubMove(timeBar: TimeBar, position: Long) {
//        // Percentage of Progress of width
//        val l =
//            (position.toFloat() / (exoPlayer?.player?.duration ?: Long.MAX_VALUE))
//
//        // position of thumb
//        val l1 =
//            l * (playerBinding.playerView.findViewById<DefaultTimeBar>(R.id.exo_progress)?.width ?: 1)
//        val xOfExoPosition = ((playerBinding.playerView.findViewById<DefaultTimeBar>(R.id.exo_progress)?.x
//            ?: 0f) + l1) - ((playerBinding.playerView.findViewById<TextView>(
//            R.id.exo_position
//        )?.width ?: 1) / 2)
//        playerBinding.playerView.findViewById<TextView>(R.id.exo_position)?.x =
//            if (xOfExoPosition < (playerBinding.playerView.findViewById<DefaultTimeBar>(R.id.exo_progress)?.x
//                    ?: 0f)
//            )
//                playerBinding.playerView.findViewById<DefaultTimeBar>(R.id.exo_progress)?.x ?: 0f
//            else
//                xOfExoPosition
//
//    }
//
//    override fun onScrubStart(timeBar: TimeBar, position: Long) {
//        playerBinding.playerView.findViewById<TextView>(R.id.exo_position)?.show()
//        if (exoPlayer?.isPlaying == true) {
//            isAutoPaused = true
//            exoPlayer?.pausePlayer()
//        }
//    }
//
//    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
//        playerBinding.playerView.findViewById<TextView>(R.id.exo_position)?.hide()
//        if (isAutoPaused) {
//            isAutoPaused = false
//            exoPlayer?.startPlayer()
//        }
//        calculateWatchDurationAndPost(cwHitRunner, position)
//    }
//
//    override fun preparePlayback() {
//    }
//
//    private fun startProgressing(shouldProgress: Boolean) {
//        playerBinding.playerProgress.startProgressAvd(shouldProgress)
//    }
//
//    override fun setObserver() {
//        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
//            if(!viewModel.sharedPrefs.isActivePack()){
//                releasePlayer()
//                updateErrorModel(null, getString(R.string.pack_expiry_message_on_player))
//            }
//        })
//        viewModel.getShemarooPlaybackUrls().observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let {
//                var playbackUrl  = ""
//                for (adaptiveUrl in it.adaptiveUrls!!) {
//                    if ("main".equals(adaptiveUrl.label, ignoreCase = true)) {
//                        playbackUrl = adaptiveUrl.playback_url ?: ""
//                        break
//                    }
//                }
//                playerModel?.setPlaybackUrl(playbackUrl)
//                navigateToPlayer(playerModel!!)
//            }
//
//        })
//
//        viewModel.playerProgressListener.observe(viewLifecycleOwner, Observer {
//            startProgressing(it)
//        })
////        viewModel.getFavResponse().observe(viewLifecycleOwner, Observer { it ->
////            it.getContentIfNotHandled()?.let {
////                if (it.data?.favourite == true) {
////                    playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)
////                        ?.setCompoundDrawablesWithIntrinsicBounds(
////                            ContextCompat.getDrawable(
////                                requireContext(),
////                                R.drawable.ic_star_selected
////                            ), null, null, null
////                        )
////                }
////                else {
////                    playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)
////                        ?.setCompoundDrawablesWithIntrinsicBounds(
////                            ContextCompat.getDrawable(
////                                requireContext(),
////                                R.drawable.ic_star_unselected
////                            ), null, null, null
////                        )
////                }
////
////            }
////        })
////        viewModel.getIsFavouriteContent().observe(viewLifecycleOwner, Observer {
////            it.getContentIfNotHandled()?.let {
////                if (it) {
////                    trackOnAddFavorite(playerModel)
////                    playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_wishlist)
////                        .setCompoundDrawablesWithIntrinsicBounds(
////                            ContextCompat.getDrawable(
////                                requireContext(),
////                                R.drawable.ic_star_selected
////                            ), null, null, null
////                        )
////                } else {
////                    trackOnDeleteFavorite(playerModel)
////                    playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_wishlist)
////                        .setCompoundDrawablesWithIntrinsicBounds(
////                            ContextCompat.getDrawable(
////                                requireContext(),
////                                R.drawable.ic_star_unselected
////                            ), null, null, null
////                        )
////                }
////            }
////        })
//
//        viewModel.getNextPreviousEpisodeDetails().observe(viewLifecycleOwner, Observer {
//            it?.getContentIfNotHandled()?.data?.let { episodeDetails ->
//                if (episodeDetails.nextEpisodeExists && episodeDetails.nextEpisode != null) {
//                    nextEpisodeAvailable = true
//                    setTimer(episodeDetails.nextEpisode!!)
//                    val url = getCloudinaryUrl(
//                        viewModel.sharedPrefs.getCloudenieryUrl(),
//                        getDeviceDimension(requireContext()).x,
//                        getDeviceDimension(requireContext()).y,
//                        episodeDetails.nextEpisode!!.getImageItem()
//                    )
//                    playerBinding.nextEpisode = episodeDetails.nextEpisode
//                    imageLoad(playerBinding.nextEpisodeScreen.ivPoster, url)
//                    playerBinding.playerView.findViewById<TextView>(R.id.tv_play_next).show()
//                    playerBinding.playerView.findViewById<TextView>(R.id.tv_play_next)
//                        .setOnClickListener { playEpisode(episodeDetails.nextEpisode!!) }
//                    playerBinding.nextEpisodeScreen.btnPlay.setOnClickListener {
//                        playEpisode(
//                            episodeDetails.nextEpisode!!
//                        )
//                    }
//                }
//                if (episodeDetails.previousEpisodeExists && episodeDetails.previousEpisode != null) {
//                    playerBinding.playerView.findViewById<ImageView>(R.id.iv_previous).show()
//                    playerBinding.playerView.findViewById<ImageView>(R.id.iv_previous)
//                        .setOnClickListener { playEpisode(episodeDetails.previousEpisode!!) }
//                }
//            }
//        })
//    }
//
//    override fun getViewModelOwner(): ViewModelStoreOwner = this
//
//    private val TAG = PlayerFragment::class.java.getName()
//    private var sessionRenewCount = -1
//    private var isRRMError: Boolean = false
//    private var isDeviceLimitReached: Boolean = false
//    private var exoPlayer: ExoPlayerImpl? = null
//    private var rrmSessionInfo: RrmSessionInfo? = null
//    private val args by navArgs<PlayerFragmentArgs>()
//
//    private var isDefaultLanguageSet = false
//    private var isResume = true
//    private var isPause = false
//
//    override fun lostConnection() {
//        if (isBuffering) {
//            activity?.runOnUiThread{
//                showNetworkAlert()
//            }
//
//        }
//    }
//
//    override fun calculateWatchDurationAndPost(runnable: Runnable, watchDurationlong: Long) {
//        if (exoPlayer != null && exoPlayer?.player != null) {
//            val watchDuration: Int
//            if (watchDurationlong == 0L) {
//                watchDuration = exoPlayer!!.player.currentPosition.toInt() / 1000
//            } else
//                watchDuration = watchDurationlong.toInt() / 1000
//
//            val totalDuration = exoPlayer!!.player.duration.toInt() / 1000
//
//            publishWatchedContent(runnable, watchDuration, totalDuration, viewModel)
//        }
//    }
//
//    override fun onVisibilityChange(visibility: Int) {
//
//    }
//
//    override fun onTimeChanged(currentTime: Long, totalTime: Long) {
//        val remainingTime = totalTime - currentTime
//        val remainingTimeVal = Util.getStringForTime(
//            formatBuilder,
//            Formatter(formatBuilder, Locale.getDefault()),
//            remainingTime
//        )
//        val bufferedPosition = exoPlayer?.bufferedPosition
//        if (remainingTimeVal.length <= 8) {
//            playerBinding.playerView.findViewById<TextView>(R.id.exo_remaining)?.text = remainingTimeVal
//            if((currentTime) > PLAYER_INCREMENT_DECREMENT_MS){
//                playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.show()
//            } else {
//                playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.hide()
//            }
////            if (remainingTime > PLAYER_INCREMENT_DECREMENT_MS) {
////                playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.show()
////            } else {
////                playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.hide()
////            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        activity?.let {
//            it.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        }
//    }
//
//    override fun getViewModelClass(): Class<PlayerViewModel> {
//        return PlayerViewModel::class.java
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        exoPlayer = ExoPlayerImpl(requireContext())
//        defaultPlayerViewSettings()
//        exoPlayer!!.onSaveInstance(savedInstanceState)
//    }
//
//    /**
//     * Used for Player Default configuration.
//     */
//    private fun defaultPlayerViewSettings() {
//
//
//        tv_video_quality.tag = VIDEO_TAG
//        setClickListener(tv_video_quality)
//
//        tv_video_language.tag = AUDIO_TAG
//        setClickListener(tv_video_language)
//
//        llReplay.tag = REPLAY_TAG
//        setClickListener(llReplay)
//
//        tv_ffwd?.tag = FWD_TAG
//        setClickListener(tv_ffwd)
//
//        tv_rew?.tag = REW_TAG
//        setClickListener(tv_rew)
//        //enableDisableAudioOption(im_audio, false)
//
//        player_view.useController = true
//        player_view.requestFocus()
//        player_view.setControllerVisibilityListener(this)
//        player_view.controllerAutoShow = true
//        player_view.controllerShowTimeoutMs = PLAYER_CONTROLLER_SHOW_TIMEOUT_MS
//        playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist).setOnClickListener {
////            toggleWatchlisted(viewModel)
//        }
//    }
//
//    private fun setClickListener(view: View) {
//        view.setOnClickListener { view ->
//            val `object` = view.tag
//            if (`object` is String) {
//                val tag = `object`
//                if (tag.equals(FWD_TAG, true)) {
//                    exoPlayer?.let {
//                        it.player.let { player ->
//                            player.seekTo(player.currentPosition + PLAYER_INCREMENT_DECREMENT_MS)
//                        }
//                    }
//                } else if (tag.equals(REW_TAG, true)) {
//                    exoPlayer?.let {
//                        it.player.let { player ->
//                            player.seekTo(player.currentPosition - PLAYER_INCREMENT_DECREMENT_MS)
//                        }
//                    }
//                } else if (tag.equals(AUDIO_TAG, ignoreCase = true)) {
//                    if (trackSelector == null) {
//                        trackSelector = exoPlayer!!.trackSelector
//                    }
//                    openAudioOption()
//                } else if (tag.equals(VIDEO_TAG, ignoreCase = true)) {
//                    if (trackSelector == null) {
//                        trackSelector = exoPlayer!!.trackSelector
//                    }
//                    openVideoOption()
//                } else if (tag.equals(REPLAY_TAG, ignoreCase = true)) {
//                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_rew)?.hide()
//                    playerBinding.playerView.findViewById<ImageView>(R.id.tv_ffwd)?.show()
//                    trackOnRestart(playerModel)
//                    playerModel?.setResumeTime(0L)
////                    initializePlayer()
//                    exoPlayer?.reInitialize(playerModel!!, 1)
//                    exoPlayer?.startPlayer()
//                }
//            }
//        }
//    }
//
//    override fun zoomIn() {
//        playerBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//    }
//
//    override fun zoomOut() {
//        playerBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
////        registerUnregisterNetworkCallback(true)
//        playerModel = args.playerData
//
//        if (PROVIDER_SHEMAROO.equals(playerModel?.getProvider(), true)) {
//            trackOnThirdPartyPlayer(playerModel)
//        }
//        viewModel.fetchLastWatchedFavourite(playerModel!!.getContentId()!!, playerModel!!.getContentType()!!)
//        viewModel.fetchNextAndPreviousEpisode(playerModel!!.getContentId()!!)
//
//        initializePlayer()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val isDialog = dialog?.isShowing ?: false
//        if (isAutoPaused && !isDialog && simpleExoPlayer != null) {
//            isAutoPaused = false
//            exoPlayer!!.startPlayer()
//        }
//    }
//
//    override fun onPause() {
//        if (exoPlayer != null && simpleExoPlayer != null) {
//            if (exoPlayer!!.isPlaying) {
//                isAutoPaused = true
//                exoPlayer!!.pausePlayer()
//            }
//
//        }
//        super.onPause()
//    }
//
//    override fun onStop() {
//        super.onStop()
////        releasePlayer()
//
//        playerDurationWatcher?.stop()
//    }
//
//
//    override  fun releasePlayer() {
//        try {
//            if (exoPlayer != null) {
//                exoPlayer!!.releasePlayer()
//                simpleExoPlayer = null
//                stopTime = getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT)
//                watchedDuration = System.currentTimeMillis() - firstPlayStartTime
//                trackOnPlayerPlay(playerModel,contentItem)
//            }
//        } catch (e: Exception) {
//            e("Exception", "Exception on Release player")
//        }
//
//    }
//
//
//    /**
//     * This is used to initiate video player
//     */
//    private fun initializePlayer() {
//        var ticket = ""//"66A7A8CABDC87589"
//        var sessionId = ""//"1DDD9DD4D349A436"
//        rrmSessionInfo = viewModel.sharedPrefs.getRrmSessionInfo()
//        if (rrmSessionInfo != null) {
//            sessionId = rrmSessionInfo!!.sessionId.toString()
//            ticket = rrmSessionInfo!!.ticket.toString()
//        }
//        e("SessionId", "sessionId $sessionId")
//        e("SessionId", "ticket : $ticket")
//        e("SessionId", "isEncrypted ${playerModel?.isEncrypted()}")
//        if (playerModel!!.isEncrypted()) {
//            playerModel!!.setDrmLicenseUrl(playerModel!!.getLA_URL() + "&SessionId=" + sessionId + "&Ticket=" + ticket)
//        }
//        e("SessionId", "getLA_URL ${playerModel?.getLA_URL()}")
//        player_view.findViewById<TextView>(R.id.tv_title).text = playerModel!!.getTitle()
//        startPlayerInitially()
//        playerDurationWatcher = PlayerDurationWatcher(exoPlayer!!.player, this)
//        playerBinding.playerView.findViewById<DefaultTimeBar>(R.id.exo_progress)?.addListener(this)
////        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setOnClickListener {
////            when(playerBinding.playerView.resizeMode){
////                AspectRatioFrameLayout.RESIZE_MODE_FIT -> zoomIn()
////                AspectRatioFrameLayout.RESIZE_MODE_FILL -> zoomOut()
////            }
////        }
//    }
//
//    private fun startPlayerInitially() {
//        if (!isNetworkConnected(requireContext())) {
//            isNetworkAvailable = false
//            if (exoPlayer?.isPlaying == true)
//                isAutoPaused = true
//            showNetworkAlert()
//            return
//        }
//        try {
//            simpleExoPlayer =
//                exoPlayer?.initializePlayer(requireActivity(), playerModel!!, player_view, this)
//            if (simpleExoPlayer == null) {
//                updateErrorModel(PLAYER_DEFAULT_ERROR_CODE, parseError(PLAYER_DEFAULT_ERROR_CODE))
//            }
//        } catch (ex: Exception) {
//            e("SessionId", "error exception sessionId $ex")
//            updateErrorModel(PLAYER_DEFAULT_ERROR_CODE, parseError(PLAYER_DEFAULT_ERROR_CODE))
//            ex.printStackTrace()
//        }
//    }
//
//
//    /**
//     * Used to set the error message and error is true or not.
//     *
//     * @param errorMessage
//     */
//    private fun updateErrorModel(errorCode: Int?, errorMessage: String?) {
//        trackOnPlayerFailure(playerModel, errorMessage?:"")
//        trackOnPlayerError(playerModel, errorCode.toString(), errorMessage, PARA_PI_ERROR_ORIGIN, PARA_ERROR_TYPE_PLAYER)
//        onError(ErrorModel())
//        /*if (errorMessage == null) return
//        tv_error_message!!.text = errorMessage
//        if (!TextUtils.isEmpty(errorMessage)) {
//            viewModel.hideLoader()
//            tv_error_message.show()
//            playerBinding.playerView.findViewById<ConstraintLayout>(R.id.widgetController).hide()
//        } else {
//            tv_error_message.hide()
//        }*/
//    }
//
//
//    /**
//     * Used to handle the Player Error.
//     *
//     * @param exception
//     */
//    private fun handlePlayerError(exception: ExoPlaybackException?) {
//        onBufferingEnded()
//        var errorMessage = ""
//        if (isRRMError) {
//            return
//        }
//        var errorCode = PLAYER_DEFAULT_ERROR_CODE
//        when (exception!!.type) {
//            ExoPlaybackException.TYPE_SOURCE -> {
//                errorCode = getErrorCode(exception.sourceException)
//                errorMessage = "SOURCE EXCEPTION: " + exception.sourceException.message
//            }
//            ExoPlaybackException.TYPE_RENDERER -> {
//                errorCode = getErrorCode(exception.rendererException)
//                errorMessage = "RENDERER EXCEPTION: " + exception.rendererException.message
//            }
//            ExoPlaybackException.TYPE_UNEXPECTED -> {
//                errorCode = getErrorCode(exception.unexpectedException)
//                errorMessage =
//                    "UNEXPECTED EXCEPTION: " + exception.unexpectedException.message
//            }
//
//            else -> errorMessage = getString(R.string.error_generic)
//        }
//
//        updateErrorModel(errorCode, parseError(errorCode))
//    }
//
//    /*private fun openAudioOption() {
//        if (exoPlayer != null) {
////            val availableLanguage = exoPlayer!!.availableLanguage
////            availableLanguage?.let { showLanguageAndSubtitleDialog(it) }
//            ttnPlayerHelper!!.showLanguageAndSubtitleDialog(trackSelector).setOnDismissListener {
//                playerModel?.let {
//                    exoPlayer?.startPlayer()
//                }
//            }
//            exoPlayer?.pausePlayer()
//        }
//    }*/
//
//
//    /* private fun openVideoOption() {
//         if (exoPlayer != null) {
//             ttnPlayerHelper!!.showVideoQualityDialog(
//                 trackSelector,
//                 TtnPlayerHelper.VIDEO_TRACK,
//                 null,
//                 false,
//                 allowAdaptiveSelections = false
//             ).setOnDismissListener {
//                 playerModel?.let {
//                     exoPlayer?.startPlayer()
//                 }
//             }
//             exoPlayer?.pausePlayer()
//         }
//     }*/
//
//    override fun changeAudioTrack(format: Format?) {
//        if (format?.language != null) {
//            val parametersBuilder = exoPlayer?.trackSelector?.buildUponParameters()
//            parametersBuilder?.let {
//                it.setPreferredAudioLanguage(format.language!!)
//                exoPlayer?.trackSelector?.setParameters(it)
//            }
//        }
//    }
//
//    override fun changeSubtitle(language: String?) {
//        val parametersBuilder = exoPlayer?.trackSelector?.buildUponParameters()
//        parametersBuilder?.let {
//            if (language != null) {
//                it.setPreferredTextLanguage(language)
//                it.setRendererDisabled(2, false)
//            } else {
//                it.setRendererDisabled(2, true)
//            }
//            exoPlayer?.trackSelector?.setParameters(it)
//        }
//    }
//
//    fun setDefaultSubtitle(){
//        val parametersBuilder = exoPlayer?.trackSelector?.buildUponParameters()
//        parametersBuilder?.let {
//            it.setRendererDisabled(2, true)
//            exoPlayer?.trackSelector?.setParameters(it)
//        }
//    }
//    //    private fun showLanguageAndSubtitleDialog() {
////        val dialog = Dialog(context!!, com.ttn.ttnplayer.R.style.DialogThemeTransparent)
////        dialog.setContentView(com.ttn.ttnplayer.R.layout.popup_select_player_options)
////        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
////        dialog.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
////        dialog.setOnCancelListener { }
////        val textViewTitle = dialog.findViewById<TextView>(com.ttn.ttnplayer.R.id.tv_title)
////        dialog.findViewById<ImageView>(com.ttn.ttnplayer.R.id.iv_close).setOnClickListener { dialog.dismiss() }
////        textViewTitle.text = context!!.getString(com.ttn.ttnplayer.R.string.audio_setting)
////        val trackSelectionListener = object : TtnTrackSelector.OnTrackSelectionListener {
////            override fun onTrackSelectionApply() {
////                dialog.dismiss()
////            }
////        }
////        val ttnTrackSelectorAudio = TtnTrackSelector(
////            context = context!!,
////            trackSelector = trackSelector!!,
////            rendererIndex = TtnPlayerHelper.AUDIO_TRACK,
////            onTrackSelectionListener = trackSelectionListener
////        )
////        ttnTrackSelectorAudio.init(allowAdaptiveSelections = false, allowMultipleOverrides = false)
////        val audioTrackView =
////            ttnTrackSelectorAudio.createTrackSelectionView(
////                getString(com.ttn.ttnplayer.R.string.ttn_audio),
////                true
////            )
////        val ttnTrackSelectorSubtitle = TtnTrackSelector(
////            context = context!!,
////            trackSelector = trackSelector!!,
////            rendererIndex = TtnPlayerHelper.SUBTITLE_TRACK,
////            onTrackSelectionListener = trackSelectionListener
////        )
////        ttnTrackSelectorSubtitle.init(
////            allowAdaptiveSelections = false,
////            allowMultipleOverrides = false
////        )
////        val subtitleTrackView = ttnTrackSelectorSubtitle.createTrackSelectionView(
////            context!!.getString(com.ttn.ttnplayer.R.string.ttn_subtitle),
////            true
////        )
////        val param = LinearLayout.LayoutParams(
////            LinearLayout.LayoutParams.MATCH_PARENT,
////            LinearLayout.LayoutParams.MATCH_PARENT,
////            1.0f
////        )
////        audioTrackView.layoutParams = param
////        subtitleTrackView.layoutParams = param
////
////        val view =
////            (context!! as Activity).layoutInflater.inflate(com.ttn.ttnplayer.R.layout.multi_trackview_dialog, null)
////        val llMultitracks = view.findViewById<LinearLayout>(com.ttn.ttnplayer.R.id.ll_multi_tracks)
////        //add AUDIO view
////        llMultitracks.addView(audioTrackView)
////        //add subtitle view
////        llMultitracks.addView(subtitleTrackView)
////        llMultitracks.layoutParams = param
////        dialog.findViewById<ConstraintLayout>(com.ttn.ttnplayer.R.id.mock_demo_view).addView(llMultitracks)
////        dialog.show()
////    }
////
////    private fun showVideoQualityDialog(
////        rendererIndex: Int,
////        title: String?,
////        showDisableOption: Boolean,
////        allowAdaptiveSelections: Boolean
////    ) {
////        val dialog = Dialog(context!!, com.ttn.ttnplayer.R.style.DialogThemeTransparent)
////        dialog.setContentView(com.ttn.ttnplayer.R.layout.popup_select_player_options)
////        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
////        dialog.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
////        dialog.setOnCancelListener { }
////        dialog.findViewById<ImageView>(com.ttn.ttnplayer.R.id.iv_close).setOnClickListener { dialog.dismiss() }
////        val textViewTitle = dialog.findViewById<TextView>(com.ttn.ttnplayer.R.id.tv_title)
////        textViewTitle.text = getString(com.ttn.ttnplayer.R.string.video_quality)
////        val trackSelectionListener = object : TtnTrackSelector.OnTrackSelectionListener {
////            override fun onTrackSelectionApply() {
////                dialog.dismiss()
////            }
////        }
////        val trackSelectionViewUtility = TtnTrackSelector(
////            context = context!!,
////            trackSelector = trackSelector!!,
////            rendererIndex = rendererIndex,
////            onTrackSelectionListener = trackSelectionListener
////        )
////        trackSelectionViewUtility.init(
////            allowAdaptiveSelections = allowAdaptiveSelections,
////            allowMultipleOverrides = false
////        )
////        val view = trackSelectionViewUtility.createTrackSelectionView(title, showDisableOption)
////        dialog.findViewById<ConstraintLayout>(com.ttn.ttnplayer.R.id.mock_demo_view).addView(view)
////        dialog.show()
////    }
//
//
//
//
//    override fun changeVideoQuality(bitrate: Bitrate) {
//        val mappedTrackInfo = exoPlayer!!.trackSelector.currentMappedTrackInfo ?: return
//        var rendererIndex = VIDEO_TRACK
//        /*loop@ for (i in 0 until mappedTrackInfo.rendererCount) {
//            val trackGroups = mappedTrackInfo.getTrackGroups(i)
//            if (trackGroups.length != 0) {
//                when (exoPlayer!!.player.getRendererType(i)) {
//                    C.TRACK_TYPE_AUDIO -> {
//                    }
//                    C.TRACK_TYPE_VIDEO -> rendererIndex = i
//                    C.TRACK_TYPE_TEXT -> {
//                    }
//                    else -> continue@loop
//                }
//            }
//        }*/
//        var override: DefaultTrackSelector.SelectionOverride? = null
//        val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
//
//        val parametersBuilder = exoPlayer!!.trackSelector.buildUponParameters()
//
//        if (!bitrate.getName().equals("Auto", true)) {
//            for (groupIndex in 0 until trackGroups.length) {
//                val group = trackGroups[groupIndex]
//                for (trackIndex in 0 until group.length) {
//                    val format = group.getFormat(trackIndex)
//                    if (mappedTrackInfo.getTrackSupport(
//                            rendererIndex,
//                            groupIndex,
//                            trackIndex
//                        ) == RendererCapabilities.FORMAT_HANDLED
//                    ) {
//                        if(trackIndex == bitrate.getTrackIndex())
//                            override = DefaultTrackSelector.SelectionOverride(
//                                groupIndex,
//                                bitrate.getTrackIndex()
//                            )
//                    }
//                }
//            }
//        }
//        if (override != null) {
//            exoPlayer!!.trackSelector.parameters =
//                DefaultTrackSelector.ParametersBuilder().build()
//            parametersBuilder.setSelectionOverride(rendererIndex, trackGroups, override)
//        } else {
//            parametersBuilder.clearSelectionOverrides(rendererIndex)
//        }
//        player_view.useArtwork = true
//        player_view.setKeepContentOnPlayerReset(true)
//        exoPlayer!!.trackSelector.setParameters(parametersBuilder)
//    }
//
//
//    private fun openVideoOption() {
//        if (exoPlayer != null) {
//            val availableVideoQuality: VideoQuality =
//                exoPlayer?.availableVideoQuality ?: VideoQuality()
//            val videoQuality: List<Bitrate?>? = availableVideoQuality.getBitrateArrayList()
//            if (videoQuality != null && videoQuality.size > 0) {
//                if (exoPlayer?.isPlaying == true) {
//                    isAutoPaused = true
//                    exoPlayer?.pausePlayer()
//                }
//                showVideoQualityPopUp(availableVideoQuality)
//            }
//        }
//    }
//
//
//    private fun openAudioOption() {
//        if (exoPlayer != null) {
//            val availableLanguage = exoPlayer?.availableLanguage
//
//            val audioLanguages = availableLanguage?.list
//            if (availableLanguage != null && audioLanguages != null && audioLanguages.size > 0) {
//                audioLanguage = availableLanguage
//            }
//            if (audioLanguage.list == null) {
//                val languages = ArrayList<String>()
//                languages.addAll(playerModel?.getAudioLanguages() ?: ArrayList())
//                audioLanguage.list = languages
////                Toast.makeText(context!!, "No Audio Available!", Toast.LENGTH_SHORT).show()
//            }
//            if(subtitleList.list == null){
//                val languages = ArrayList<String>()
//                languages.add("None")
//                subtitleList.list = languages
//            }
//            showPopUp(
//                trackSelector,
//                exoPlayer?.player?.currentTrackSelections,
//                audioLanguage,
//                subtitleList,
//                true
//            )
//            if (exoPlayer?.isPlaying == true) {
//                isAutoPaused = true
//                exoPlayer?.pausePlayer()
//            }
//
//        }
//    }
//
//    override fun audioVideoDialogClosed() {
//        if (isAutoPaused) {
//            isAutoPaused = false
//            exoPlayer?.startPlayer()
//        }
//    }
//
//    private fun setTimer(contentItem: ContentItem) {
//        viewModel.timer = object : CountDownTimer(NEXT_EPISODE_TIMER_MS, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                var secs = (millisUntilFinished / 1000)
//
//                if(secs == 0L){
//                    secs = 1L
//                }
//                playerBinding.nextEpisodeScreen.tvStartingTimer.text =
//                    String.format(
//                        Locale.US,
//                        getString(R.string.seconds_timer),
//                        (secs).toString()
//                    )
//            }
//
//            override fun onFinish() {
////                playerBinding.nextEpisodeScreen.tvStartingTimer.text = "..."
//                playerBinding.nextEpisodeScreen.tvStartingTimer.text =
//                    String.format(
//                        Locale.US,
//                        getString(R.string.seconds_timer),
//                        "..."
//                    )
//                playEpisode(contentItem)
//            }
//        }
//    }
//
//    private fun playEpisode(contentItem: ContentItem) {
//        if (PROVIDER_SHEMAROO.equals(contentItem.provider, true)) {
//            playerModel = null
//            playerModel = viewModel.generatePlayerModel(contentItem, playerModel?.getTAShowType() ?: "")
//            onPause()
//            val signedURL = ShemarooHelper.decryptMd5(contentItem.partnerDeepLinkUrl)
//            viewModel.fetchShemarooMeContentPlayback(signedURL, false)
//        }
//        else {
//            val playerModel =
//                viewModel.generatePlayerModel(contentItem, playerModel?.getTAShowType() ?: "")
//            navigateToPlayer(playerModel)
//        }
//    }
//
//    private fun navigateToPlayer(playerModel: PlayerModel) {
//        activity?.let { parentActivity ->
//            parentActivity.finish()
//            startActivity(
//                Intent(
//                    parentActivity,
//                    PlayerActivity::class.java
//                ).apply {
//                    putExtra("playerData", playerModel)
//                })
//            parentActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
//        }
//    }
//
//    override fun onDestroyView() {
//        viewModel.timer?.cancel()
//        releasePlayer()
//        super.onDestroyView()
//    }
//
//    override fun onError(errorModel: ErrorModel) {
//        showDialog(
//            DialogModel(false, null, errorModel.message, "Ok", null),
//            object : CommonDialogEventListener {
//                override fun onPrimaryButtonClick() {
//                    hideDialog()
//                    activity?.finish()
//                }
//
//                override fun onCloseButtonClick() {
//                    hideDialog()
//                }
//
//                override fun onSecondaryButtonClick() {
//                    hideDialog()
//                }
//            })
//    }
//
//    override fun resumePlayerAfterError() {
//        activity?.runOnUiThread {
//            if(isPlayerEnded){
//                activity?.finish()
//                /*isPlayerEnded = false
//                playerBinding.playerView.show()
//                playerBinding.nextEpisodeScreen.clRoot.hide()
//                playerModel?.getImage()?.let {
//                    val url = getCloudinaryUrl(
//                        viewModel.sharedPrefs.getCloudenieryUrl(),
//                        getDeviceDimension(requireContext()).x, getDeviceDimension(requireContext()).y,
//                        it
//                    )
//                    imageLoad(iv_thumbnail, url)
//                }
//                iv_thumbnail.show()
//                player_view.findViewById<ConstraintLayout>(R.id.llReplay).show()
//                firstTimeCW = true*/
//            }
//            else{
//                isAutoPaused = false
//                exoPlayer!!.startPlayer()
//            }
//        }
//    }
//
//    override fun getPlayerLayoutId(): Int {
//        return R.layout.fragment_player
//    }
//
//    override fun onPlayerMuteStateChanged(isMuted: Boolean) {
//    }
//}