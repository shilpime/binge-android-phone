package com.tatasky.binge.ui.features.player.model

import java.util.*

class VideoQuality {
    private var selectedQualityIndex = 0
    private var bitrateArrayList: List<Bitrate?>? = null

    fun getSelectedQualityIndex(): Int {
        return selectedQualityIndex
    }

    fun setSelectedQualityIndex(selectedQualityIndex: Int) {
        this.selectedQualityIndex = selectedQualityIndex
    }

    fun getBitrateArrayList(): List<Bitrate?>? {
        return bitrateArrayList
    }

    fun setBitrateArrayList(bitrateArrayList: List<Bitrate?>) {
        this.bitrateArrayList = bitrateArrayList
    }
}