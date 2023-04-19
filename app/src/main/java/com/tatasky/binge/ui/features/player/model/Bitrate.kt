package com.tatasky.binge.ui.features.player.model

class Bitrate(name: String?, bitrate: Long, trackIndex : Int, groupIndex : Int) {
    private var name: String? = null
    private var bitrate: Long = 0
    private var trackIndex: Int = 0
    private var groupIndex: Int = 0
    init {
        this.name = name
        this.bitrate = bitrate
        this.trackIndex = trackIndex
        this.groupIndex = groupIndex
    }


    fun getTrackIndex(): Int {
        return trackIndex
    }

    fun getGroupIndex(): Int {
        return groupIndex
    }
    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getBitrate(): Long {
        return bitrate
    }

    fun setBitrate(bitrate: Long) {
        this.bitrate = bitrate
    }
}