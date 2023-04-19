package com.ttn.ttnplayer.listeners

interface TtnPlayerVolumeControlListener {
    fun onVolumeUp(volume: Int)
    fun onVolumeDown(volume: Int)
}