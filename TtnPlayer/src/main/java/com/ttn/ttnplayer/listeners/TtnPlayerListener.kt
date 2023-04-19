package com.ttn.ttnplayer.listeners

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.TimeBar
import okhttp3.Response

interface TtnPlayerListener {
    /**
     * This method will be called when player loading status change.
     * @param isLoading
     * @param bufferedPosition
     * @param bufferedPercentage*/
    fun onLoadingStatusChanged(isLoading: Boolean, bufferedPosition: Long, bufferedPercentage: Int)

    /**This method will be called when player start playing a video.
     * @param currentWindowIndex current track playing index*/
    fun onPlayerPlaying(currentWindowIndex: Int)

    fun onPlayerPaused(currentWindowIndex: Int)
    /**This method will be called if a video stream is buffering.*/
    fun onPlayerBuffering(currentWindowIndex: Int)

    fun onBufferStart()
    fun onBufferEnd()

    /**This method will be called when a playing video end.*/
    fun onPlayerStateEnded(currentWindowIndex: Int)

    /**
     * Called if player state is idle*/
    fun onPlayerStateIdle(currentWindowIndex: Int)

    /**
     * Called if there is any error while playing videos or initializing player*/
    fun onTTNPlayerError(error:ExoPlaybackException?)

    /**Called when when creating TtnPlayer*/
    fun createTtnPlayerCalled(isToPrepare: Boolean)

    /**Called when releasing player*/
    fun releaseTtnPlayerCalled()

    /** Called when player resume from last position*/
    fun onVideoResumeDataLoaded(window: Int, position: Long, isResumeWhenReady: Boolean)

    /**Called when changing track*/
    fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    )

    fun onMuteStateChanged(isMuted: Boolean) {}
//    fun onVideoTapped()
    /**
     * @return - true to handle the tap
     */
    fun onPlayBtnTap(): Boolean

    /**
     * @return - true to handle the tap
     */
    fun onPauseBtnTap(): Boolean

    fun onFullScreenBtnTap()
    /**This method will be called when visibility of UI controls of player change.
     * @param visibility current visibility of UI controller.*/
    fun onPlayerUiControlVisibilityChange(visibility: Int) {}

    fun onScrubStart(timeBar: TimeBar, position: Long) {}
    fun onScrubMove(timeBar: TimeBar, position: Long) {}
    fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {}


    fun onDrmSessionAcquired()
    fun onDrmSessionManagerError(error: Exception)
    fun toggleWatchlisted()
    fun onPlayerResponse(response: Response) {}
}