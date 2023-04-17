package com.ttn.ttnplayer.listeners

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar

internal interface TtnPlayerControl {
    fun createPlayer(isToPrepare: Boolean)
    fun preparePlayer()
    fun releasePlayer()
    fun releaseAdsLoader()
    fun updateVideoUrls(urls: ArrayList<String>)
    fun playerPause()
    fun playerPlay()
    fun playerNext()
    fun playerPrevious()
    fun seekTo(windowIndex: Int, positionMs: Long)
    fun seekToDefaultPosition()
    fun seekToStartPosition()
    fun setTtnPlayerEventsListener(pTtnPlayerListenerListener: TtnPlayerListener?)
    fun setTtnAdListener(TtnPlayerAdListener: TtnPlayerAdListener?)
    fun setTtnThumbListener(TtnPlayerThumbListener: TtnPlayerThumbListener?)
    fun onActivityStart()
    fun onBackPressed()
    fun onActivityResume()
    fun onActivityPause()
    fun onActivityStop()
    fun onActivityDestroy()
    fun onSaveInstanceState(outState: Bundle)
    fun playerBlock()
    fun playerUnBlock()
    fun setFullScreenBtnVisibility(isVisible: Boolean)
    fun setVolumeControlListener(volumeControlListener: TtnPlayerVolumeControlListener?)
    fun playerStop()
    fun onConfigurationChanged(newConfig: Configuration)
    fun disableDeviceSoftNavigationIcons(value: Boolean)
    fun hideNavigationAndStatusBar()
    fun dispatchKeyEvent(event: KeyEvent): Boolean
    fun hidePlayerSeekBar()
    fun showPlayerSeekBar()
    fun onContentMarkedFavourite(icon:Drawable?)
    fun setPlayerStartOverButtonVisibility(visibility: Int)
    fun disableTimeSeekBarTouch(value: Boolean)
    fun onAudioFocusChange(focusChange: Int)
    fun setHandleAudioBecomingNoisy(value: Boolean)
    fun setEpisodeMode()
    fun setMiniTimeBar(timeBar:TimeBar?)
}