package com.tatasky.binge.ui.features.home.home_trailer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.tatasky.binge.R
import com.tatasky.binge.utils.d
import kotlinx.android.synthetic.main.fragment_app_splash.view.*


class TrailerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs),
    ITrailerController {

    private lateinit var mVideoView: PlayerView
    private val mMediaPlayer = MediaPlayerImpl()
    public var mPlayUrl: String? = null
    private var mIsPlaying = false
    private var mTrailerFinishLambda: (() -> Unit)? = null
    private var mTrailerStartLambda: (() -> Unit)? = null


//    init {
//        val view = View.inflate(context, R.layout.fragment_video_view, this)
//        mVideoView = view.findViewById(R.id.ep_video_view)
//        mVideoView.player = mMediaPlayer.getPlayerImpl(context)
//        mVideoView.player?.repeatMode = Player.REPEAT_MODE_ONE
//        addListener()
//    }

    fun init() {
        val view = View.inflate(context, R.layout.fragment_video_view, this)
        mVideoView = view.findViewById(R.id.ep_video_view)
        mVideoView.player = mMediaPlayer.getPlayerImpl(context)
        mVideoView.player?.repeatMode = Player.REPEAT_MODE_ONE
        addListener()
    }


    private fun hidePlayerAndErrorHandling() {
        mIsPlaying = false
        mTrailerFinishLambda?.invoke()
    }

    private fun addListener() {
        mVideoView.player?.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                d("TrailerView","inside onPlayerStateChanged :$playWhenReady, playbackState:$playbackState")
                if (playbackState == Player.STATE_READY) {
                    mIsPlaying = true
                    mTrailerStartLambda?.invoke()

                } else /*if (playbackState == Player.STATE_ENDED
                    || playbackState == Player.STATE_IDLE
                ) */{
                    hidePlayerAndErrorHandling()
                }
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                hidePlayerAndErrorHandling()
            }

        })
    }

    fun setTrailerUrl(trailerUrl: String) {
        mPlayUrl = trailerUrl
    }

    fun setTrailerFinishLambda(trailerFinishLambda: (() -> Unit)) {
        mTrailerFinishLambda = trailerFinishLambda
    }

    fun setTrailerStartLambda(trailerStartLambda: (() -> Unit)) {
        mTrailerStartLambda = trailerStartLambda
    }

    override fun playUrl() {
//        pausePlayer()
        mMediaPlayer.getExoPlayerInstance()?.let {
            if (it.isPlaying) {
                mTrailerStartLambda?.invoke()
                return
            }

        }
        if (!this::mVideoView.isInitialized)
            init()
        mPlayUrl?.let { mMediaPlayer.play(it) }


    }

    override fun pausePlayer() {

        mMediaPlayer.releasePlayer()
    }

    override fun isPlaying(): Boolean {
        return mIsPlaying
    }

}
