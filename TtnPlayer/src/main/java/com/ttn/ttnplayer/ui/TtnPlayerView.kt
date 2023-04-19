package com.ttn.ttnplayer.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.exoplayer2.ui.PlayerView

class TtnPlayerView : PlayerView {

    constructor(context: Context) : super(context)

    constructor(
            context: Context,
            attributeSet: AttributeSet?
    ) : super(context, attributeSet)
}