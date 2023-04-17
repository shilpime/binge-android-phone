package com.tatasky.binge.ui.features.home.home_trailer


interface ITrailerController {
    fun playUrl()
    fun pausePlayer()
    fun isPlaying(): Boolean
}