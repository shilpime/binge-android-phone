package com.ttn.ttnplayer.listeners

import com.ttn.ttnplayer.player.TrackModel

interface TtnTrackSelectionListener {
    fun onTrackSelected(trackModel: TrackModel)
}