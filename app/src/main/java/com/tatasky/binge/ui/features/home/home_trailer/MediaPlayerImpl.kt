package com.tatasky.binge.ui.features.home.home_trailer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.tatasky.binge.ui.features.player.PlayerModel
import com.tatasky.binge.utils.probePlayerEventInitSdk
import com.tatasky.binge.utils.probePlayerEventPlayClicked
import com.tatasky.binge.utils.probePlayerEventStopped


class MediaPlayerImpl : IMediaPlayer {

    private lateinit var mExoPlayer: ExoPlayer
    private lateinit var mContext: Context

    override fun play(url: String) {
        val mediaDataSourceFactory = DefaultHttpDataSourceFactory(
            Util.getUserAgent(mContext, "TS")
        )

        val mediaSource: MediaSource =
            when (Util.inferContentType(url)) {
                C.TYPE_HLS -> {
                    HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(url))
                }
                else -> {
                    DashMediaSource.Factory(
                        DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        mediaDataSourceFactory
                    ).createMediaSource(Uri.parse(url))
                }
            }

        mExoPlayer.prepare(mediaSource)
        mExoPlayer.audioComponent?.volume = 0F
        mExoPlayer.playWhenReady = true
    }

    override fun getPlayerImpl(context: Context): ExoPlayer {
        this.mContext = context
        if (!initialized)
            initializePlayer()
        return mExoPlayer
    }

    override fun releasePlayer() {
        if (this::mExoPlayer.isInitialized) {
            probePlayerEventStopped()
            mExoPlayer.stop()
//            mExoPlayer.release()
        }
    }

    public fun getExoPlayerInstance(): ExoPlayer? {
        if (this::mExoPlayer.isInitialized)
            return mExoPlayer
        else
            return null
    }

    var initialized = false
    private fun initializePlayer() {
        initialized=true
        val loadControl = DefaultLoadControl()
        val renderersFactory = DefaultRenderersFactory(mContext)
        mExoPlayer =
            SimpleExoPlayer.Builder(mContext, renderersFactory).setLoadControl(loadControl).build()
        sID?.let { it ->
            probePlayerEventInitSdk(
                mExoPlayer as SimpleExoPlayer, playerModel,
                bandWidthMeter = DefaultBandwidthMeter(), it
            )
            probePlayerEventPlayClicked()
        }
    }

    private var playerModel: PlayerModel? = null
    private var sID : String? = null
    fun setPlayerModel(iplayerModel : PlayerModel?, sId:String?) {
        this.playerModel = iplayerModel
        this.sID = sId
    }

}
