package com.tatasky.binge.customviews

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.widget.VideoView

class VideoPlayer : VideoView {

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        alpha = 0f // hides view
        setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                mp.setOnInfoListener(object : MediaPlayer.OnInfoListener {
                    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            alpha = 1f // shows view
                            return true
                        }
                        return false
                    }
                })
            }
        })
    }
}