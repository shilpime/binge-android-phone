package com.ttn.ttnplayer.listeners

internal interface TtnPlayerStatus {
    val isPlayerVideoMuted: Boolean
    val currentWindowIndex: Int
    val currentPosition: Long
    val duration: Long
    val isPlayerCreated: Boolean
    val isPlayerPrepared: Boolean
    val isPlayingAd: Boolean
}