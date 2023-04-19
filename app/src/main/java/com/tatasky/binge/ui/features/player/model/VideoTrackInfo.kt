package com.tatasky.binge.ui.features.player.model

class VideoTrackInfo {
    private var name: String? = null
    private var bitrate: Long = 0
    private var groupIndex = 0
    private var trackIndex = 0


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

    fun getGroupIndex(): Int {
        return groupIndex
    }

    fun setGroupIndex(groupIndex: Int) {
        this.groupIndex = groupIndex
    }

    fun getTrackIndex(): Int {
        return trackIndex
    }

    fun setTrackIndex(trackIndex: Int) {
        this.trackIndex = trackIndex
    }

}