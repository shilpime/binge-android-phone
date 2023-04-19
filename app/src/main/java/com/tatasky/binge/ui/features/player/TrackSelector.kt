package com.tatasky.binge.ui.features.player

import android.content.Context
import android.util.SparseArray
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.util.Assertions

class TrackSelector(
    var context: Context,
    var trackSelector: DefaultTrackSelector?,
    var rendererIndex: Int
) {
    private lateinit var trackGroups: TrackGroupArray
    private var mappedTrackInfo: MappingTrackSelector.MappedTrackInfo? =
        trackSelector?.currentMappedTrackInfo
    private var parameters: DefaultTrackSelector.Parameters? = trackSelector?.parameters
    private var allowAdaptiveSelections = true
    private var allowMultipleOverrides = false
    private var isDisabled = false

    fun init(
        allowAdaptiveSelections: Boolean,
        allowMultipleOverrides: Boolean
    ) {
        this.allowAdaptiveSelections = allowAdaptiveSelections
        this.allowMultipleOverrides = allowMultipleOverrides
        isDisabled = parameters?.getRendererDisabled( /* rendererIndex= */rendererIndex)?:false
//        val trackType = mappedTrackInfo.getRendererType( /* rendererIndex= */rendererIndex)
//        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
    }

    fun getFormats(): List<Format> {
        val mutableListOfFormats = mutableListOf<Format>()
        if(mappedTrackInfo!=null) {
            trackGroups = mappedTrackInfo!!.getTrackGroups(rendererIndex)
            for (groupIndex in 0 until trackGroups.length) {
                val group: TrackGroup = trackGroups[groupIndex]
                for (trackIndex in 0 until group.length) {
                    group.getFormat(trackIndex).language?.let {
                        mutableListOfFormats.add(group.getFormat(trackIndex))
                    }
                }
            }
        }
        return mutableListOfFormats
    }
}