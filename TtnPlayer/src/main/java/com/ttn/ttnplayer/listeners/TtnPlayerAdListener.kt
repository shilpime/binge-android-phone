package com.ttn.ttnplayer.listeners

interface TtnPlayerAdListener {
    fun onAdPlay()
    fun onAdPause()
    fun onAdResume()
    fun onAdEnded()
    fun onAdError()
    fun onAdClicked()
    fun onAdTapped()
    fun onBuffering()
}