package com.tatasky.binge.ui.features.details.trailer

interface ITrailerListener {
    fun onPlayerEnded()
    fun onPlayerReady()
    fun onTrailerSoundChanged(sound:Boolean)
    fun switchToFullScreen(switchFullScreen:Boolean)
    fun onControllerShown()
    fun onControllerHidden()
    fun onPlayerPause()
    fun showHideLoader(isLoader : Boolean)
    fun addToWatchList()
    fun getContentTitle():String
    fun getNetWorkStatus():Boolean
    fun onPlayerFailure()
}