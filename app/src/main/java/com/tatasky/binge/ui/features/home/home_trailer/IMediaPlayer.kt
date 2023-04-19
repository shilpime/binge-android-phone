package com.tatasky.binge.ui.features.home.home_trailer

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer

interface IMediaPlayer {
    fun play(url: String)
    fun getPlayerImpl(context: Context): ExoPlayer
    fun releasePlayer()
}