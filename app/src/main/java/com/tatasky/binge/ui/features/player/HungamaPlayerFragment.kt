package com.tatasky.binge.ui.features.player

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.provider.Settings
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
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.text.CaptionStyleCompat
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.hungama.sdk.player.HungamaPlayerManager
import com.hungama.sdk.player.events.OnPlayerContentLoadListener
import com.hungama.sdk.player.events.OnPlayerStateChangeListener
import com.hungama.sdk.player.exceptions.PlaybackException
import com.hungama.sdk.player.models.ContentType
import com.hungama.sdk.player.models.PlayerState
import com.tatasky.binge.R
import com.tatasky.binge.analytics.ANALYTICS_TIME_FORMAT
import com.tatasky.binge.analytics.PARA_ERROR_TYPE_PLAYER
import com.tatasky.binge.analytics.PARA_PI_ERROR_ORIGIN
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.databinding.FragmentHungamaPlayerBinding
import com.tatasky.binge.databinding.ToastWatchlistBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.player.listeners.PlayerDurationWatcher
import com.tatasky.binge.ui.features.player.listeners.PlayerListener
import com.tatasky.binge.ui.features.player.model.Bitrate
import com.tatasky.binge.ui.features.player.model.VideoQuality
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.PlayerUtils.getCurrentSeekBarProgressInPercentage
import java.util.*
import kotlin.collections.ArrayList


class HungamaPlayerFragment : PlayerBaseFragment<FragmentHungamaPlayerBinding>(),
    OnPlayerContentLoadListener, OnPlayerStateChangeListener, PlayerListener.TimeChangeListener{

    private var mInitialized: Boolean = false
    private var seekPosition: Long = 0L
    private var isPausedState: Boolean = false
    private var isReleasePlayer: Boolean = false
    private var mPlayerController: HungamaPlayerManager? = null
    private var resumeDuration: Int = 0

    //    private var isPlaying: Boolean = true
    private var currentProgress: Long = 0
    private var totalLength: Long = 0
    private var playerDurationWatcher: PlayerDurationWatcher? = null

    //        private var mPlaybackController: PlaybackController? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var controllerHidingTime = 3000L

    private val REQUEST_PHONE_STATE_PERMISSION = 124
    private val controlsHidder = Runnable {
        playerBinding.playerController.hide()
        if (!mIsInFullScreenMode) {
            binding.imgBack.show()
        } else {
            binding.imgBack.hide()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(isContentSubscribed){
            HungamaPlayerManager.getInstance().setPartnerUniqueId(sharedPrefs.getPartnerUniqueIdInfo(
                PROVIDER_HUNGAMA
            ))
        }
        else
            HungamaPlayerManager.getInstance().setPartnerUniqueId(sharedPrefs.getAnonymousId()?:"48379sjd")
    }

    override fun calculateWatchDurationAndPost(runnable: Runnable, watchDurationlong: Long) {
        if (HungamaPlayerManager.getInstance() != null) {
            val watchDuration: Int
            if (watchDurationlong == 0L) {
                watchDuration = currentProgress.toInt() / 1000
            } else
                watchDuration = watchDurationlong.toInt() / 1000

            val totalDuration = totalLength / 1000
            publishWatchedContent(runnable, watchDuration, totalDuration.toInt(), viewModel)
        }
    }

    override fun getPlayerLayoutId(): Int {
        return R.layout.fragment_hungama_player
    }

    private fun initWithNetworkCheck(isReplay: Boolean) {
        if (!isNetworkConnected(requireContext())) {
            isNetworkAvailable = false
            showNetworkAlert()
        } else {
            trackOnThirdPartyPlayer(playerModel)
            initializePlayer()
        }
    }

    private var adExoPlayer: SimpleExoPlayer? = null
    private var adsLoader: ImaAdsLoader? = null
    private fun showAdView() {
        releaseAdPlayer()
        playerBinding.adPlayerView.show()
        // Create an AdsLoader.
        context?.let { it ->
            val adTagUri = Uri.parse(getString(R.string.ad_tag_url2))
            val uri = Uri.parse(getString(R.string.content_url))
            adsLoader =  ImaAdsLoader.Builder(it)
                .setAdEventListener { adEvent ->
//                    AdEvent.AdEventType.SKIPPED
                    e("setAdEventListener","Inside AdEvent : ${adEvent.type}")
                    if(adEvent.type == AdEvent.AdEventType.LOADED){
                        startProgressing(false)
                    }
                    if(adEvent.type == AdEvent.AdEventType.SKIPPED || adEvent.type == AdEvent.AdEventType.ALL_ADS_COMPLETED) {
                        e("setAdEventListener","Inside AdCompleted")
                        hideAdView()
                    }
                }
                .buildForAdTag(adTagUri)
            adsLoader?.adsLoader?.addAdErrorListener {
                e("setAdEventListener","Inside addAdErrorListener  $it")
                hideAdView()
            }
            // Set up the factory for media sources, passing the ads loader and ad view providers.

            // Set up the factory for media sources, passing the ads loader and ad view providers.
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                it,
                Util.getUserAgent(it, getString(R.string.app_name))
            )

            val mediaSource = DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            val mediaSourceWithAds: MediaSource = AdsMediaSource(
                mediaSource,
                dataSourceFactory,
                adsLoader,
                playerBinding.adPlayerView
            )
            // Create a SimpleExoPlayer and set it as the player for content and ads.
            adExoPlayer = SimpleExoPlayer.Builder(it)/*.setMediaSourceFactory(mediaSourceFactory)*/
                .build()
            playerBinding.adPlayerView.player = adExoPlayer
            adsLoader!!.setPlayer(adExoPlayer)
            // Prepare the content and ad to be played with the SimpleExoPlayer.
//            player.setMediaItem(mediaItem)
            adExoPlayer?.prepare(mediaSourceWithAds)
            // Set PlayWhenReady. If true, content and ads will autoplay.
            adExoPlayer?.playWhenReady = true
            adExoPlayer?.addListener(object  : Player.EventListener{
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if(playbackState == Player.STATE_IDLE){
                        //return to hungama player
                        e("setAdEventListener","Inside STATE_IDLE player")
                        hideAdView()
                    }
                    else if(playbackState == Player.STATE_READY){
                        if(isPausedState){
                            adExoPlayer?.playWhenReady = false
                        }
                    }
                    super.onPlayerStateChanged(playWhenReady, playbackState)
                }
                override fun onPlayerError(error: ExoPlaybackException) {
                    e("setAdEventListener","Inside SDK ")
                    hideAdView()
                    super.onPlayerError(error)
                }
            })
        }
    }

    private fun hideAdView() {
        startProgressing(false)
        releaseAdPlayer()
//        HungamaPlayerManager.getInstance().start()
        HungamaPlayerManager.getInstance().togglePlayPause()
    }

    /*
    * Release AdPlayer when adding new content or before initializing new ad
    * */
    fun releaseAdPlayer() {
        playerBinding.adPlayerView.hide()
        adsLoader?.setPlayer(null)
        adsLoader?.release()
        playerBinding.adPlayerView.player = null
        adExoPlayer?.release()
        adExoPlayer = null
    }

    override fun setObserver() {
        super.setObserver()
        viewModel.getNextEpisodePlayItem().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                playEpisode(it)
            }
        })
        viewModel.getLastWatchResponse().observe(viewLifecycleOwner, Observer { it ->
            e("fetchLastWatch", "inside getLastWatchResponse")
            handlePrimaryButtonText()
            it.getContentIfNotHandled()?.let {
                updateWatchlist(it.data?.favourite)
                if (it.data?.favourite == true) {
                    playerBinding.playerController.findViewById<TextView>(R.id.tv_add_to_watchlist)
                        ?.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_pi_watchlist_selected
                            ), null, null, null
                        )
                    playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)?.text =
                        getString(
                            R.string.add_to_watchlist
                        )
                } else {
                    playerBinding.playerController.findViewById<TextView>(R.id.tv_add_to_watchlist)
                        ?.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_pi_watchlist_unselected
                            ), null, null, null
                        )
                    playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)?.text =
                        getString(
                            R.string.add_to_watchlist
                        )
                }
            }
        })
        viewModel.getIsFavouriteContent().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                cancelFavToast()
                val view = DataBindingUtil.inflate<ToastWatchlistBinding>(
                    LayoutInflater.from(context),
                    R.layout.toast_watchlist,
                    null,
                    false
                )
                val imgResource:Int
                var msz: String = ""
                updateWatchlist(it)
                playerBinding.playerView.findViewById<TextView>(R.id.tv_add_to_watchlist)?.text =
                    getString(
                        R.string.add_to_watchlist
                    )
                if (it) {
                    imgResource = R.drawable.ic_pi_watchlist_selected
                    msz = getString(R.string.added_to_watchlist)
                    view.watchlistIcon.startAvd(true)
                    view.watchlistToastTv.text = getString(R.string.added_to_watchlist)
                    playerBinding.playerController.findViewById<TextView>(R.id.tv_add_to_watchlist)
                        ?.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_pi_watchlist_selected
                            ), null, null, null
                        )

                    binding.trailerView.setWatchlisted(true)
                    trackOnAddFavorite()
                } else {
                    imgResource = R.drawable.ic_pi_watchlist_unselected
                    msz = getString(R.string.remove_from_watchlist)
                    view.watchlistToastTv.text = getString(R.string.remove_from_watchlist)
                    playerBinding.playerController.findViewById<TextView>(R.id.tv_add_to_watchlist)
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
            e("pubnub", "action forceLogout inside Hungama")
            forceLogout()
        })
        viewModel.getPlayerModel().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                e("HungamaPlayerFragment", "inside getPlayerModel() : ${playerModel?.getTitle()}")
                releasePlayer()
                playerModel = it
                initializePlayerHelper(null)
                binding.playerFrame.show()
            }
        })
//        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
//            if (!viewModel.sharedPrefs.isActivePack() && isPlayerStarted) {
//                releasePlayer()
//                updateErrorModel(getString(R.string.pack_expiry_message_on_player))
//                //stop player
//            }
//        })
        viewModel.playerProgressListener.observe(viewLifecycleOwner, Observer {
            e("HungamaPlayerFragmnet", "inside playerProgressListern : $it")
            startProgressing(it)
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
                    playerBinding.playerController.findViewById<TextView>(R.id.tv_play_next).show()
                    playerBinding.playerController.findViewById<TextView>(R.id.tv_play_next)
                        .setOnClickListener {
                            if(!isBuffering)
                                playEpisode(episodeDetails.nextEpisode!!)
                        }
                    playerBinding.nextEpisodeScreen.btnPlay.setOnClickListener {
                        playEpisode(
                            episodeDetails.nextEpisode!!
                        )
                    }
                }
                if (episodeDetails.previousEpisodeExists && episodeDetails.previousEpisode != null) {
                    playerBinding.playerController.findViewById<ImageView>(R.id.iv_previous).show()
                    playerBinding.playerController.findViewById<ImageView>(R.id.iv_previous)
                        .setOnClickListener {
                            if(!isBuffering)
                                playEpisode(episodeDetails.previousEpisode!!)
                        }
                }
            }
        })

    }

    override fun onPause() {
        isPausedState = true
        if (isPlayerStarted){
            if(adExoPlayer?.isPlayingAd == true){
                adExoPlayer?.playWhenReady = false
            }
            else if(HungamaPlayerManager.getInstance().isPlaying) {
                isAutoPaused = true
                HungamaPlayerManager.getInstance().togglePlayPause()
//            trackOnPause(playerModel!!)
            }
        }
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.timer?.cancel()
        e("HungamaPlayerFragment", "inside destroyView : ${playerModel?.getTitle()}")
        releasePlayer()
    }

    override fun releasePlayer() {
        releaseAdPlayer()
        isReleasePlayer = true
        totalLength = 0
        if (mPlayerController == null) return
        playerDurationWatcher?.stop()
        stopTime = getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT)
        if (startPlayTime != 0L)
            playDuration += (System.currentTimeMillis() - startPlayTime)
        watchedDuration = playDuration //System.currentTimeMillis() - firstPlayStartTime
        d("HungamaPlayerFragment", "releaseExoPlayerCalled playDuration: $playDuration , watchedDuration: $watchedDuration")
        if(watchedDuration > 1000) {
            val currentBitrate=HungamaPlayerManager.getInstance().currentPlaybackVariant
            val quality=fetchVideoQualityUsingBitrate(Integer.parseInt(currentBitrate))
            trackOnPlayerPlay(playerModel, contentItem,quality)
            trackOnPlayerPlayEnd(playerModel, contentItem,
                PlayerUtils.getCurrentSeekBarProgressInPercentage(
                    HungamaPlayerManager.getInstance().currentPosition,
                    HungamaPlayerManager.getInstance().totalDuration
                )
            )
        }
        HungamaPlayerManager.getInstance().setWakeMode(C.WAKE_MODE_NONE)
        HungamaPlayerManager.getInstance().stop()
        HungamaPlayerManager.getInstance().releasePlayer()
        if(callProbeEventOnce) {
            callProbeEventOnce = false
            probePlayerEventStopped()
        }
//        unRegisterDisplayListener()
        mPlayerController = null
//        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        mapOfVtrTriggerState.clear()
    }

    private fun initializePlayer() {
        firstTimeCW = true
        val contentVO = ContentVO(
            playerModel!!.getProviderContentId()!!,
            "",
            getHungamaContentType(playerModel?.getContentType())
        )
        playerModel?.let {
            viewModel.actionCW(
                it.getContentId()!!,
                getContentType(it.getContentType()!!),
                it.getResumeTime().toInt() / 1000,
                it.getTotalDuration().toInt()
            )
        }
        e("HungamaPlayerFragmnet", "inside initializePlayer")
        startProgressing(true)
        try {
            isReleasePlayer = false
            playerBinding.playerView.b.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            callProbeEventOnce = true
            HungamaPlayerManager.getInstance().loadContent(contentVO, this)
        } catch (e: SecurityException) {
            checkRuntimePermission(requireActivity())
        } catch (e: Exception){
            updateErrorModel(e.localizedMessage, null)
        }
        binding.btnPlayerController.tag = PLAY_TAG
        playerBinding.playerController.findViewById<DefaultTimeBar>(R.id.exo_progress)
            ?.addListener(miniTimeBarListener)
        binding.miniProgressPlayer.addListener(miniTimeBarListener)
    }

    private fun checkRuntimePermission(activity: Activity) {
        //check permission at runtime
        when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED -> {
                initializePlayer()
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_PHONE_STATE_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PHONE_STATE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializePlayer()
                }
                else if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    showDialog(
                        DialogModel(
                            false,
                            null,
                            getString(R.string.msg_state_permission),
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
                                updateErrorModel("Permission denied", null)
                                hideDialog()
                            }

                            override fun onCloseButtonClick() {
                                hideDialog()
                            }
                        })
                } else {
                    updateErrorModel("Permission denied", null)
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }



    private fun getHungamaContentType(contentType: String?): ContentType {
        if (TYPE_MOVIES.equals(contentType, true)) {
            return ContentType.MOVIE
        } else if (TYPE_WEB_SHORTS.equals(contentType, true)) {
            return ContentType.TV_SHOW_EPISODE
        } else if (TYPE_TV_SHOWS.equals(contentType, true)) {
            return ContentType.TV_SHOW_EPISODE
        } else
            return ContentType.MOVIE
    }

    private fun playerEnded() {
        e("HungamaPlayerFragment", "inside playerEnded : ${playerModel?.getTitle()}")
        isPlayerEnded = true
        startProgressing(false)
        if (nextEpisodeAvailable) {
//            playerBinding.playerView.hide()
            playerBinding.nextEpisodeScreen.clRoot.show()
            playerBinding.playerController.hide()
            binding.btnPlayerController.setText(getString(R.string.play_now), null)
            binding.btnPlayerController.setOnClickListener(nextEpisodeClickListener)
            viewModel.timer?.start()
        } else {
            playerBinding.playerController.show()
            playerBinding.ivThumbnail.show()
            playerBinding.playerController.findViewById<ConstraintLayout>(R.id.widgetController)?.hide()
            playerBinding.playerController.findViewById<ImageView>(R.id.tv_rew)?.hide()
            playerBinding.playerController.findViewById<ImageView>(R.id.tv_ffwd)?.show()
            Handler(Looper.getMainLooper()).postDelayed({
                playerBinding.playerController.findViewById<ConstraintLayout>(R.id.llReplay).show()
            }, 200)
            binding.btnPlayerController.tag = REPLAY_TAG
            binding.btnPlayerController.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_replay)
            binding.btnPlayerController.setText(getString(R.string.replay), null)
            firstTimeCW = true
        }
        releasePlayer()
        resumeDuration = 0
//        playerBinding.playerView.removeAllViews()
        mHandler.removeCallbacks(controlsHidder)
        actionCWhandler.removeCallbacks(cwHitRunner)
        val totalDuration = (playerModel?.getTotalDuration() ?: totalLength / 1000).toInt()
        publishWatchedContent(
            null,
            totalDuration,
            totalDuration,
            viewModel
        )
    }

    override fun zoomIn() {
        playerBinding.playerView.b.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setImageResource(R.drawable.ic_zoom_out)

    }

    override fun zoomOut() {
        playerBinding.playerView.b.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setImageResource(R.drawable.ic_zoom_in)
    }

    override fun zoomInPinch() {
        playerBinding.playerView.b.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        playerBinding.playerView.findViewById<ImageView>(R.id.iv_zoom).setImageResource(R.drawable.ic_zoom_out)
    }

    private fun startProgressing(shouldProgress: Boolean) {
        playerBinding.playerProgress.startProgressAvd(shouldProgress)
        if (shouldProgress)
            playerBinding.playerController.findViewById<RelativeLayout>(R.id.rl_play_pause)
                ?.invisible()
        else
            playerBinding.playerController.findViewById<RelativeLayout>(R.id.rl_play_pause)
                ?.show()
    }

    private val miniTimeBarListener = object :
        TimeBar.OnScrubListener {
        override fun onScrubMove(timeBar: TimeBar, position: Long) {
            // Percentage of Progress of width
            updateCurrentDragPosition(position)
        }

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
            if (HungamaPlayerManager.getInstance().isPlaying
                || isBuffering
            ) {
                isAutoPaused = true
                HungamaPlayerManager.getInstance().togglePlayPause()
            }
            mHandler.removeCallbacks(controlsHidder)
            playerBinding.playerController.show()
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            playerBinding.playerController.findViewById<TextView>(R.id.exo_position)?.hide()
            if (!isNetworkConnected(requireContext())) {
                seekPosition = position
                isNetworkAvailable = false
                if (HungamaPlayerManager.getInstance().isPlaying) {
                    isAutoPaused = true
                    HungamaPlayerManager.getInstance().togglePlayPause()
                }
                showNetworkAlert()
            } else {
                HungamaPlayerManager.getInstance().seekTo(position)
                if (isAutoPaused)
                    HungamaPlayerManager.getInstance().togglePlayPause()
                isAutoPaused = false
                playerBinding.playerController.findViewById<DefaultTimeBar>(R.id.exo_progress)
                    ?.setPosition(position)
                binding.miniProgressPlayer.setPosition(position)
                calculateWatchDurationAndPost(cwHitRunner, position)
                handleFwdRewButtons(position, totalLength)
            }
            mHandler.postDelayed(controlsHidder, CONTROLLER_HIDE_TIME_MS)
        }
    }

    fun updateCurrentDragPosition(position: Long) {
        val currentTimeBar = if (mIsInFullScreenMode)
            playerBinding.playerController.findViewById<View>(R.id.exo_progress)
        else
            binding.miniProgressPlayer as View

        val exoPosition = playerBinding.playerController.findViewById<TextView?>(R.id.exo_position)

        exoPosition?.text =
            Util.getStringForTime(
                formatBuilder, Formatter(formatBuilder, Locale.getDefault()), position
            )
        val l =
            (position.toFloat() / (totalLength))

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
        playerBinding.playerController.findViewById<TextView>(R.id.exo_position)?.show()
    }

    private fun invalidateAndHideControls() {
        mHandler.removeCallbacks(controlsHidder)
        playerBinding.playerController.hide()
        if (mIsInFullScreenMode) {
            binding.imgBack.hide()
        }
    }

    private fun invalidateAndShowControls() {
        if (playerBinding.playerController.isVisibile()) {
            invalidateAndHideControls()
        } else {
            mHandler.removeCallbacks(controlsHidder)
            playerBinding.playerController.show()
            binding.imgBack.show()
            mHandler.postDelayed(controlsHidder, CONTROLLER_HIDE_TIME_MS)
        }
    }


    private fun handleFwdRewButtons(progress: Long, totalLength: Long) {
        playerBinding.playerController.findViewById<ImageView>(R.id.tv_ffwd)?.show()
        if (progress > PLAYER_INCREMENT_DECREMENT_MS) {
            playerBinding.playerController.findViewById<ImageView>(R.id.tv_rew)?.show()
        } else {
            playerBinding.playerController.findViewById<ImageView>(R.id.tv_rew)?.hide()
        }
//        if (totalLength - progress > PLAYER_INCREMENT_DECREMENT_MS) {
//            playerBinding.playerController.findViewById<TextView>(R.id.tv_ffwd)?.show()
//        } else {
//            playerBinding.playerController.findViewById<TextView>(R.id.tv_ffwd)?.hide()
//        }
    }


    /**
     * Used to set the error message and error is true or not.
     *
     * @param errorMessage
     */
    private fun updateErrorModel(errorMessage: String?, errorCode: String?) {
        if(errorView.root.isVisibile()) return
        context?.let {
            trackOnPlayerFailure(
                playerModel!!,
                errorMessage ?: "Unable to load content",
                contentItem
            )
            trackOnPlayerError(
                playerModel,
                errorCode,
                errorMessage,
                PARA_PI_ERROR_ORIGIN,
                PARA_ERROR_TYPE_PLAYER
            )

            if (errorMessage == null) return
            startProgressing(false)

            onError(ErrorModel())
        }
        e("HungamaPlayerFragment", "inside onError() errorMessage: $errorMessage")
        releasePlayer()
    }

    override fun initializePlayerHelper(savedInstanceState: Bundle?) {
        firstTimeCW = true
        isPlayerStarted = true
        nextEpisodeAvailable = false
        isAutoPaused = false
        isPausedState = false
        networkErrorView = playerBinding.networkError
        errorView = playerBinding.errorView
        playerBinding.nextEpisodeScreen.clRoot.hide()
        viewModel.timer?.cancel()
        registerDisplayListener()
        hideErrorView()
        //requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        viewModel.fetchNextAndPreviousEpisode(playerModel?.getContentId()!!)
        if (!mIsInFullScreenMode) {
            playerBinding.playerController.findViewById<View>(R.id.iv_zoom)?.hide()
//            playerBinding.playerController.findViewById<View>(R.id.tv_title)?.hide()
//            playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.invisible()
            playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
            binding.miniProgressPlayer.show()
            playerBinding.nextEpisodeScreen.isPortrait = true
        }
        playerBinding.playerController.findViewById<DefaultTimeBar>(R.id.exo_progress)
            .setPosition(0L)
        binding.miniProgressPlayer.setPosition(0L)
        initAudioLanguage()
        playerModel?.getImage()?.let {
            val url = getCloudinaryUrl(
                viewModel.sharedPrefs.getCloudenieryUrl(),
                getDeviceDimension(requireContext()).x, getDeviceDimension(requireContext()).y,
                it
            )
            imageLoad(playerBinding.ivThumbnail, url)
        }
        playerBinding.playerController.findViewById<TextView>(R.id.tv_title)?.isSelected = true
        playerBinding.playerController.findViewById<TextView>(R.id.tv_title)?.text =
            playerModel!!.getTitle()
        resumeDuration = (playerModel?.getResumeTime() ?: 0L).toInt()
//        trackOnPlayerPlay(playerModel)
        initWithNetworkCheck(false)
        initViews()
    }

    private fun initViews() {
        playerBinding.playerController.findViewById<ImageView>(R.id.imgBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        playerBinding.playerController.findViewById<TextView>(R.id.tv_video_quality)?.tag = VIDEO_TAG
        setClickListener(playerBinding.playerController.findViewById<TextView>(R.id.tv_video_quality))

        playerBinding.playerController.findViewById<TextView>(R.id.tv_video_language)?.tag = AUDIO_TAG
        setClickListener(playerBinding.playerController.findViewById<TextView>(R.id.tv_video_language))

        playerBinding.playerController.findViewById<TextView>(R.id.tv_add_to_watchlist)?.tag =
            WATCHLIST_TAG
        setClickListener(playerBinding.playerController.findViewById<TextView>(R.id.tv_add_to_watchlist))

        playerBinding.playerController.findViewById<ImageView>(R.id.tv_ffwd)?.tag = FWD_TAG
        setClickListener(playerBinding.playerController.findViewById<ImageView>(R.id.tv_ffwd))

        playerBinding.playerController.findViewById<ImageView>(R.id.tv_rew)?.tag = REW_TAG
        setClickListener(playerBinding.playerController.findViewById<ImageView>(R.id.tv_rew))

        playerBinding.playerController.findViewById<ImageView>(R.id.exo_play)?.tag = PLAY_TAG
        setClickListener(playerBinding.playerController.findViewById<ImageView>(R.id.exo_play))

        playerBinding.playerController.findViewById<ImageView>(R.id.exo_pause)?.tag = PLAY_TAG
        setClickListener(playerBinding.playerController.findViewById<ImageView>(R.id.exo_pause))

        playerBinding.playerController.findViewById<ConstraintLayout>(R.id.llReplay)?.tag = REPLAY_TAG
        setClickListener(playerBinding.playerController.findViewById<ConstraintLayout>(R.id.llReplay))

        playerBinding.playerController.findViewById<View>(R.id.exo_fullscreen_iv)?.tag = FULLSCREEN_TAG
        setClickListener(playerBinding.playerController.findViewById<View>(R.id.exo_fullscreen_iv))

        playerBinding.playerView.setOnClickListener {
            invalidateAndShowControls()
        }
        playerBinding.playerController.findViewById<ImageView>(R.id.iv_zoom)?.setOnClickListener {
            when (playerBinding.playerView.b.resizeMode) {
                AspectRatioFrameLayout.RESIZE_MODE_FIT -> zoomIn()
                else -> zoomOut()
            }
        }

        playerBinding.playerController.findViewById<CheckBox>(R.id.exo_sound)?.setOnCheckedChangeListener { buttonView, isChecked ->
            onPlayerMuteStateChanged(!isChecked)
        }
    }


    private fun setClickListener(view: View) {
        view.setOnClickListener { view ->
            val `object` = view.tag
            if (`object` is String) {
                val tag = `object`
                if (tag.equals(AUDIO_TAG, ignoreCase = true)) {
                    openAudioOption()
                } else if (tag.equals(VIDEO_TAG, ignoreCase = true)) {
                    openVideoOption()
                } else if (tag.equals(WATCHLIST_TAG, ignoreCase = true)) {
                    viewModel.markFavourite(parentId, getContentType(parentContentType), true)
                } else if (tag.equals(FWD_TAG, true)) {
                    if (!isNetworkConnected(requireContext())) {
                        isNetworkAvailable = false
                        seekPosition = HungamaPlayerManager.getInstance().currentPosition + 10000
                        if (HungamaPlayerManager.getInstance().isPlaying) {
                            isAutoPaused = true
                            HungamaPlayerManager.getInstance().togglePlayPause()
                        }
                        showNetworkAlert()
                    } else
                        HungamaPlayerManager.getInstance()
                            .seekTo(HungamaPlayerManager.getInstance().currentPosition + 10000L)
                } else if (tag.equals(REW_TAG, true)) {
                    if (!isNetworkConnected(requireContext())) {
                        isNetworkAvailable = false
                        seekPosition = HungamaPlayerManager.getInstance().currentPosition - 10000
                        if (HungamaPlayerManager.getInstance().isPlaying) {
                            isAutoPaused = true
                            HungamaPlayerManager.getInstance().togglePlayPause()
                        }
                        showNetworkAlert()
                    } else
                        HungamaPlayerManager.getInstance()
                            .seekTo(HungamaPlayerManager.getInstance().currentPosition - 10000L)
                } else if (tag.equals(PLAY_TAG, true)) {
                    isAutoPaused = false
                    isPausedState = false
                    if(adExoPlayer?.isPlayingAd == true){
                        if(adExoPlayer?.playWhenReady == false)
                            binding.btnPlayerController.setText("Pause", null)
                        else
                            binding.btnPlayerController.setText("Play", null)
                        adExoPlayer?.playWhenReady = !(adExoPlayer?.playWhenReady ?: true)
                    }
                    else {
                        updatePlayPauseStateBeforeToggle()
                        HungamaPlayerManager.getInstance().togglePlayPause()
                    }
                } else if (tag.equals(REPLAY_TAG, ignoreCase = true)) {
                    if (isNetworkConnected(requireContext())) {
                        playerBinding.playerController.findViewById<ConstraintLayout>(R.id.widgetController)
                            ?.show()
                        playerBinding.playerController.findViewById<ImageView>(R.id.tv_rew)?.hide()
                        playerBinding.playerController.findViewById<ImageView>(R.id.tv_ffwd)?.show()
                        playerBinding.playerController.findViewById<ConstraintLayout>(R.id.llReplay)
                            .hide()
                        trackOnRestart(playerModel)
                    }
                    if (!isContentPlayable(playerModel?.getPartnerSubType())) {
                        showContentPlaybackDialog()
                        return@setOnClickListener
                    }
                    initWithNetworkCheck(true)
                } else if (tag.equals(FULLSCREEN_TAG, true)) {
                    changeToLandscapeMode()
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val isDialog = dialog?.isShowing ?: false
        if (isPlayerStarted)
            try {
                activity?.window?.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
                isPausedState = false
                e("PlayerResume", "inside onResume ${adExoPlayer?.isPlayingAd} , $isNetworkAvailable")
                if(adExoPlayer?.isPlayingAd == true){
                    adExoPlayer?.playWhenReady = true
                    return
                }
                if (isExternalDisplayAvailable()) {
                    releasePlayerWithBack()
                } else if (!errorView.root.isVisible && !isDialog) {
                    e("networkCallback", "inside onResume $isPausedState , $isNetworkAvailable")

                    if (!HungamaPlayerManager.getInstance().isPlaying && isAutoPaused && !isDialog) {
                        isAutoPaused = false
                        HungamaPlayerManager.getInstance().togglePlayPause()
//                        trackResumeAndInitPlayer(playerModel!!)
                    }
                    if (!isSoundOn)
                        HungamaPlayerManager.getInstance().a.i?.volume = 0f
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }

    override fun changeToLandscapeMode() {
        super.changeToLandscapeMode()
        playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.show()
        playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
        playerBinding.playerController.findViewById<View>(R.id.exo_fullscreen_iv)?.hide()
        binding.miniProgressPlayer.hide()
        playerBinding.playerController.findViewById<ConstraintLayout>(R.id.viewProgress)?.show()
        playerBinding.nextEpisodeScreen.isPortrait = false
        playerBinding.playerController.findViewById<LinearLayout>(R.id.ll_player_menu)?.show()
    }

    override fun changeToPortraitMode() {
        super.changeToPortraitMode()
        playerBinding.playerController.findViewById<View>(R.id.exo_fullscreen_iv)?.show()
        if (isPlayerStarted) {
            binding.miniProgressPlayer.show()
        }
        playerBinding.nextEpisodeScreen.isPortrait = true
        playerBinding.playerController.findViewById<View>(R.id.iv_zoom)?.hide()
//        playerBinding.playerController.findViewById<View>(R.id.tv_title)?.hide()
//        playerBinding.playerController.findViewById<View>(R.id.iv_zoom)?.invisible()
        playerBinding.playerController.findViewById<View>(R.id.tv_title)?.show()
        playerBinding.playerController.findViewById<ConstraintLayout>(R.id.viewProgress)?.hide()
        playerBinding.playerController.findViewById<LinearLayout>(R.id.ll_player_menu)?.hide()
        dialog?.cancel()
    }

    override fun changeToTabletPortraitMode() {
        super.changeToTabletPortraitMode()
        if (isPlayerStarted) {
            binding.miniProgressPlayer.show()
        }
        playerBinding.nextEpisodeScreen.isPortrait = false
        playerBinding.playerView.findViewById<View>(R.id.iv_zoom)?.hide()
        playerBinding.playerView.findViewById<View>(R.id.tv_title)?.show()
        playerBinding.playerView.findViewById<View>(R.id.exo_fullscreen_iv)?.show()
        playerBinding.playerView.findViewById<ConstraintLayout>(R.id.viewProgress)?.hide()
        playerBinding.playerView.findViewById<LinearLayout>(R.id.ll_player_menu)?.hide()
        dialog?.cancel()
    }

    private fun updatePlayPauseStateBeforeToggle() {
        if (HungamaPlayerManager.getInstance().isPlaying) {
            trackOnPause(
                playerModel!!, contentItem,
                getCurrentSeekBarProgressInPercentage(
                    HungamaPlayerManager.getInstance().currentPosition,
                    HungamaPlayerManager.getInstance().totalDuration
                )
            )
        } else {
            trackResumeAndInitPlayer(
                playerModel!!,
                contentItem,
                getCurrentSeekBarProgressInPercentage(
                    HungamaPlayerManager.getInstance().currentPosition,
                    HungamaPlayerManager.getInstance().totalDuration
                )
            )
        }
    }

    private fun togglePlayPause() {
        if(!(adExoPlayer?.isPlayingAd ?:false)) {
            if (!HungamaPlayerManager.getInstance().isPlaying) {
                playerBinding.playerController.findViewById<ImageView>(R.id.exo_pause).hide()
                playerBinding.playerController.findViewById<ImageView>(R.id.exo_play).show()
                binding.btnPlayerController.setText(getString(R.string.resume), null)
                mHandler.removeCallbacks(controlsHidder)
            } else {
                playerBinding.playerController.findViewById<ImageView>(R.id.exo_pause).show()
                playerBinding.playerController.findViewById<ImageView>(R.id.exo_play).hide()
                binding.btnPlayerController.setText("Pause", null)
                mHandler.postDelayed(controlsHidder, CONTROLLER_HIDE_TIME_MS)
            }
        }
    }


    override fun retryPlayer() {
        activity?.runOnUiThread {
            hideNetworkView()
            val isDialog = dialog?.isShowing ?: false
            if (!isNetworkConnected(requireContext())) {
                isNetworkAvailable = false
                e("HungamaPlayerFragment", "inside retryPlayer $isNetworkAvailable")
                showNetworkAlert()
            } else if (!mInitialized) {
                e("HungamaPlayerFragment", "inside retryPlayer firstTimeCW:$firstTimeCW")
                activity?.runOnUiThread {
                    initializePlayer()
                }
            } else if (!isAutoPaused
                && !isDialog && isBuffering
            ) {
                e("HungamaPlayerFragment", "inside retryPlayer isAutoPaused:$isAutoPaused")
                resumeDuration = HungamaPlayerManager.getInstance().currentPosition.toInt()
                restartPlayback()
            } else if (seekPosition > 0) {
                e("HungamaPlayerFragment", "inside retryPlayer seekPosition:$seekPosition")
                if (isAutoPaused)
                    HungamaPlayerManager.getInstance().togglePlayPause()
                isAutoPaused = false
                HungamaPlayerManager.getInstance().seekTo(seekPosition)
                seekPosition = 0L
            }
        }
    }

    override fun restartPlayerAfterNetworkAvailable() {
        activity?.runOnUiThread {
            hideNetworkView()
            e("PlayerResume", "inside onResume ${adExoPlayer?.isPlayingAd} , $isNetworkAvailable")
            if(adExoPlayer?.isPlayingAd == true){
                adExoPlayer?.playWhenReady = true
            }
            else {
                val isDialog = dialog?.isShowing ?: false
                e("networkCallback", "inside restartPlayer $isPausedState isBuffering:$isBuffering")
                if (!mInitialized) {
                    initializePlayer()
                } else if (!isAutoPaused
                    && !isDialog && isBuffering
                ) {
                    resumeDuration = HungamaPlayerManager.getInstance().currentPosition.toInt()
                    restartPlayback()
                } else if (seekPosition > 0) {
                    if (isAutoPaused)
                        HungamaPlayerManager.getInstance().togglePlayPause()
                    isAutoPaused = false
                    HungamaPlayerManager.getInstance().seekTo(seekPosition)
                    seekPosition = 0L
                } else if (!HungamaPlayerManager.getInstance().isPlaying && isAutoPaused && !isDialog) {
                    resumeDuration = HungamaPlayerManager.getInstance().currentPosition.toInt()
                    restartPlayback()
                }
            }
        }
    }

    private fun openAudioOption() {
        e(
            "HungamaPlayerFragment",
            "subtitleLanguages : ${HungamaPlayerManager.getInstance().subtitleLanguages}"
        )
        if (subtitleList.list == null) {
            val languages = ArrayList<String>()
            languages.add("None")
            for (lan in HungamaPlayerManager.getInstance().subtitleLanguages) {
                languages.add(lan)
            }
            subtitleList.list = languages
        }
        val audioLanguages = audioLanguage.list
        if (audioLanguages == null || audioLanguages.size <= 0) {
            val languages = ArrayList<String>()
            languages.addAll(playerModel?.getAudioLanguages() ?: ArrayList())
            audioLanguage.list = languages
        }
        if (HungamaPlayerManager.getInstance().isPlaying) {
            isAutoPaused = true
            HungamaPlayerManager.getInstance().togglePlayPause()
        }
        showPopUp(null, null, audioLanguage, subtitleList, trackAudioEnable = false)
    }

    override fun audioVideoDialogClosed() {
        isPausedState = false
        if (!HungamaPlayerManager.getInstance().isPlaying && isAutoPaused) {
            isAutoPaused = false
            HungamaPlayerManager.getInstance().togglePlayPause()
        }
    }

    override fun changeAudioTrack(format: Format?) {
        // No audio feature in Hungama
    }

    override fun changeSubtitle(language: String?) {
        e("HungamaPlayerFragment", "changeSubtitle : $language")
        HungamaPlayerManager.getInstance().isSubtitleEnabled = language != null
        HungamaPlayerManager.getInstance().setSubtitleLanguage(language)
        preferredSubtitleLanguage = language
    }

    private fun openVideoOption() {
        e("VideoOption","HungamaPlayerManager.getInstance().playbackVariants: ${HungamaPlayerManager.getInstance().playbackVariants}")
        if (videoQuality == null) {
//            initVideo(HungamaPlayerManager.getInstance().a.i.currentTrackGroups, null)
            initVideoQuality(HungamaPlayerManager.getInstance().playbackVariants)
        }
        val availableLVideoQuality: List<Bitrate?>? = videoQuality?.getBitrateArrayList()
        if (availableLVideoQuality != null && availableLVideoQuality.size > 0) {
            showVideoQualityPopUp(videoQuality!!)
            if (HungamaPlayerManager.getInstance().isPlaying) {
                isAutoPaused = true
                HungamaPlayerManager.getInstance().togglePlayPause()
            }
        } else {
            showToast(context, "No Video Quality Available!")
        }
    }


    private fun initVideoQuality(varients: MutableList<String>?) {
        e("HungamPlayerFragment", "inside initVideoQuality : $varients")
        if (varients != null) {
            videoQuality = VideoQuality()
            val bitrates = getBitrateArrayList(varients)
            videoQuality?.setBitrateArrayList(bitrates)
            videoQuality?.setSelectedQualityIndex(0)
        }
    }

    fun getBitrateArrayList(
        varients: MutableList<String>
    ): List<Bitrate?> {
        val bitrates = ArrayList<Bitrate?>()
        var prevBitrate = "0p"
        for (title in varients) {
            if (title == "0") {
                bitrates.add(Bitrate("Auto", 0L, -1, -1))
            } else {
                try {
                    val bitrate = Integer.parseInt(title)
                    val quality = "${fetchVideoQualityUsingBitrate(bitrate)}p"
                    if(prevBitrate != quality) {
                        prevBitrate = quality
                        bitrates.add(Bitrate(quality, bitrate.toLong(), -1, -1))
                    }
                }
                catch (e : Exception){
                    e.printStackTrace()
                }
            }
        }
        return bitrates
    }

    protected override fun fetchVideoQualityUsingBitrate(bitrate: Int): Int {
        var quality = 1280
        if (bitrate < 100000)
            quality = 144
        else if (bitrate < 400000)
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

    override fun changeVideoQuality(bitrate: Bitrate) {
        e("VideoOption", "getBitrate $bitrate : ${bitrate.getBitrate()}")
        if (!bitrate.getName().equals("Auto", true)) {
            HungamaPlayerManager.getInstance().setPlaybackVariant(
                bitrate.getBitrate().toString()
            )
        }
        else
            HungamaPlayerManager.getInstance().setPlaybackVariant("0")
    }

    private fun initAudioLanguage() {
        val languages = ArrayList<String>()
        languages.addAll(playerModel?.getAudioLanguages() ?: ArrayList())
        audioLanguage.list = languages
    }

    override fun lostConnection() {
        if (isBuffering) {
            activity?.runOnUiThread {
                showNetworkAlert()
            }

        }
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
        val playerModel =
            viewModel.generatePlayerModel(contentItem, playerModel?.getTAShowType() ?: "")
        this.playerModel = playerModel
        releasePlayer()
        initializePlayerHelper(null)
    }

    override fun onContentLoadFailed(p0: String?) {
        updateErrorModel(p0, null)
        mInitialized = false
    }

    override fun onContentLoadSuccess() {
        e("HungamaPlayerFragment", "onContentLoadSuccess() $resumeDuration")
        mInitialized = true
        if (!isReleasePlayer)
            startPlayback(false)
    }

    fun startPlayback(isRestart: Boolean) {
        try {
            startInitialBufferTime = System.currentTimeMillis()
            mPlayerController = HungamaPlayerManager.getInstance()
            HungamaPlayerManager.getInstance().initializePlayer()
            HungamaPlayerManager.getInstance().preparePlayer(playerBinding.playerView, this)
            probePlayerEventInitSdk(HungamaPlayerManager.getInstance().a.i, playerModel,
                bandWidthMeter = DefaultBandwidthMeter(), sharedPrefs.getOriginalSubscriberId())
            /*AdView removed as per Nikhil's ask*/
            /*if(!isContentSubscribed && !isRestart && FREE_AVOD.equals(detailsResponse?.data?.metaDetails?.partnerSubscriptionType, true)) {
                showAdView()
                HungamaPlayerManager.getInstance().start()
                HungamaPlayerManager.getInstance().a.i?.playWhenReady = false
            }
            else*/
            HungamaPlayerManager.getInstance().start()
            probePlayerEventPlayClicked()
            HungamaPlayerManager.getInstance().setWakeMode(C.WAKE_MODE_NETWORK)
            defaultVolume = HungamaPlayerManager.getInstance().a.i?.volume ?: 0f
            if(!isSoundOn) {
                HungamaPlayerManager.getInstance().a.i?.volume = 0f
            }
            playerDurationWatcher = PlayerDurationWatcher(
                HungamaPlayerManager.getInstance().a.i,
                this
            )
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    fun restartPlayback() {
        try {
            releasePlayer()
            e("HungamaPlayerFragment", "restartPlayback() $resumeDuration")
            startPlayback(true)
//            HungamaPlayerManager.getInstance().initializePlayer()
//            HungamaPlayerManager.getInstance().start()
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    override fun onPlayerError(p0: PlaybackException?) {
        p0?.printStackTrace()
        e("HungamaPlayerFragment", "onPlayerError it : ${p0?.printStackTrace()}")
        e("HungamaPlayerFragment", "onPlayerError it : ${p0?.cause?.message}")
        e(
            "HungamaPlayerFragment",
            "onPlayerError it : ${HungamaPlayerManager.getInstance().isPlaying}"
        )
//        .
        val errorMsg = p0?.cause?.message ?: ""
        if (errorMsg.contains("Decoder init failed")) {
            //show loading
            retryPlayer()
        } else if (isNetworkAvailable && !ERROR_MSG_NETWORK.equals(
                p0?.cause?.message,
                ignoreCase = true
            )
        ) {
            //Can Use p0?.cause?.message instead of p0?.message
            updateErrorModel(errorMsg, null)
        } else {
            retryPlayer()
            //showNetworkAlert()
        }
    }

    override fun onPlayerStateChanged(it: PlayerState?) {
        e("HungamaPlayerFragment", "onPlayerStateChanged it : $it isPausedState : $isPausedState")
        when {
            it == PlayerState.IDLE ->
                isBuffering = true
            it == PlayerState.BUFFERING ||
                    it == PlayerState.LOADING -> {

                if (it == PlayerState.BUFFERING) {
                    if (startPlayTime != 0L) {
                        playDuration += (System.currentTimeMillis() - startPlayTime)
                        startPlayTime = 0
                    }
                    isBuffering = true
                    e("HungamaPlayerFragmnet", "inside Buffering")
                    startProgressing(true)
                    playerBinding.playerController.findViewById<ConstraintLayout>(R.id.llReplay).hide()
                    binding.btnPlayerController.setOnClickListener(null)
                }
                playerBinding.tvErrorMessage.hide()
            }

            it == PlayerState.ENDED -> {
                playerEnded()
            }

            it == PlayerState.READY -> {
                hideNetworkView()
                playerBinding.playerController.findViewById<ConstraintLayout>(R.id.widgetController)?.show()
                playerBinding.ivThumbnail.hide()
                setClickListener(binding.btnPlayerController)
                togglePlayPause()
                isBuffering = false
                if (resumeDuration > 0) {
                    HungamaPlayerManager.getInstance().seekTo(resumeDuration.toLong())
                    resumeDuration = 0
                }
                playerDurationWatcher?.start()
                totalLength = HungamaPlayerManager.getInstance().totalDuration
                startProgressing(false)
                e("HungamaPlayerFragment", "inside player Ready state firstTimeCW:$firstTimeCW")
                if (firstTimeCW) {
                    playerBinding.playerController.hide()
                    setSubtitleView()
                    activity?.window?.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                    mHandler.postDelayed(controlsHidder, CONTROLLER_HIDE_TIME_MS)
                    playerBinding.playerController.findViewById<RelativeLayout>(R.id.rl_play_pause)
                        .show()
                    playerBinding.playerController.findViewById<ConstraintLayout>(R.id.llReplay).hide()
                    firstTimeCW = false
                    actionCWhandler.postDelayed(cwHitRunner, 10000)
                    intialBufferDuration =
                        (System.currentTimeMillis() - startInitialBufferTime) / 1000
                    startTime = getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT)
                    firstPlayStartTime = System.currentTimeMillis()
                    val currentBitrate=HungamaPlayerManager.getInstance().currentPlaybackVariant
                    var selectedquality=0
                    currentBitrate?.let {
                        selectedquality=fetchVideoQualityUsingBitrate(Integer.parseInt(it))
                    }
                    trackInitialBuffering(
                        intialBufferDuration.toString(),
                        (intialBufferDuration / 60).toString(),
                        playerModel,
                        contentItem,
                        selectedquality,
                        getCurrentSeekBarProgressInPercentage(
                            HungamaPlayerManager.getInstance().currentPosition,
                            HungamaPlayerManager.getInstance().totalDuration
                        )
                    )
                }
                if ((isPausedState && HungamaPlayerManager.getInstance().isPlaying)
                    || (dialog?.isShowing == true && !isAutoPaused)) {
                    isAutoPaused = true
                    HungamaPlayerManager.getInstance().togglePlayPause()
                }
                if(!HungamaPlayerManager.getInstance().isPlaying){
                    if (startPlayTime != 0L) {
                        playDuration += (System.currentTimeMillis() - startPlayTime)
                        startPlayTime = 0
                    }
                }
                else{
                    startPlayTime = System.currentTimeMillis()
                }
            }
            it != PlayerState.READY -> {
                playerBinding.playerController.findViewById<RelativeLayout>(R.id.rl_play_pause)
                    ?.invisible()
            }
        }

    }

    private fun setSubtitleView() {
        e(
            "HungamaPlayerFragment",
            "isSubtitleAvailable : ${HungamaPlayerManager.getInstance().isSubtitleAvailable}"
        )
        if (!HungamaPlayerManager.getInstance().isSubtitleAvailable)
            return
        //HungamaPlayerManager.getInstance().isSubtitleEnabled = true
        val typeFace =
            Typeface.createFromAsset(activity?.assets, getString(R.string.medium_font))
        val captionStyleCompat =
            CaptionStyleCompat(
                Color.WHITE,
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                Color.BLACK,
                typeFace
            )

        playerBinding.playerView.subtitleView.setStyle(captionStyleCompat)
        playerBinding.playerView.subtitleView.setFixedTextSize(Dimension.SP, 16f)
        playerBinding.playerView.subtitleView.setApplyEmbeddedStyles(false)
//        playerBinding.playerView.subtitleView.enable()
        if (preferredSubtitleLanguage != null) {
            changeSubtitle(preferredSubtitleLanguage)
        }
    }

    override fun onTimeChanged(currentTime: Long, totalTime: Long) {
        if (this.totalLength <= 0) {
            playerBinding.playerController.findViewById<DefaultTimeBar>(R.id.exo_progress)
                .setDuration(totalTime)
            binding.miniProgressPlayer
                .setDuration(totalTime)
        }
        handleFwdRewButtons(currentTime, totalTime)
        val remainingTime = totalTime - currentTime
        if (remainingTime != totalTime && remainingTime >= 0) {
            val remainingTimeVal = Util.getStringForTime(
                formatBuilder,
                Formatter(formatBuilder, Locale.getDefault()),
                remainingTime
            )
            if (!remainingTimeVal.contains("-") && remainingTimeVal.length <= 8)
                playerBinding.playerController.findViewById<TextView>(R.id.exo_remaining)?.text =
                    remainingTimeVal
        }
        playerBinding.playerController.findViewById<DefaultTimeBar>(R.id.exo_progress)
            .setPosition(currentTime)
        binding.miniProgressPlayer.setPosition(currentTime)
        this.currentProgress = currentTime
        this.totalLength = totalTime
        if (contentItem.partnerSubscriptionType == null ||
            contentItem.partnerSubscriptionType?.contains(PREMIUM, true) == true)
            handleViewThroughRateEvents(currentTime, totalTime)
    }
    override fun resumePlayerAfterError() {
        activity?.runOnUiThread {
            if (isPlayerEnded) {
                activity?.onBackPressed()
            } else {
                isAutoPaused = false
                updatePlayPauseStateBeforeToggle()
                HungamaPlayerManager.getInstance().togglePlayPause()
            }
        }
    }

    override fun onPlayerMuteStateChanged(isMuted: Boolean) {
        if (!isMuted) {
            isSoundOn = true
            HungamaPlayerManager.getInstance().a.i?.volume = defaultVolume
        } else {
            isSoundOn = false
            defaultVolume = HungamaPlayerManager.getInstance().a.i?.volume ?: 0f
            HungamaPlayerManager.getInstance().a.i?.volume = 0f
        }
    }
}
