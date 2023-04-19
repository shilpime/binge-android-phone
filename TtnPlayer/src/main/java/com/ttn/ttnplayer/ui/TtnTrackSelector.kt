package com.ttn.ttnplayer.ui

import android.content.Context
import android.util.Pair
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.RendererCapabilities
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.util.Assertions
import com.ttn.ttnplayer.R
import com.ttn.ttnplayer.player.TrackModel
import com.ttn.ttnplayer.player.TtnTrackNameProvider
import java.util.*

class TtnTrackSelector(var context: Context, var trackSelector: DefaultTrackSelector, var rendererIndex: Int, var onTrackSelectionListener: OnTrackSelectionListener?) : TtnTrackSelectionView.TrackSelectionListener {
    private lateinit var trackGroups: TrackGroupArray
    private var mappedTrackInfo: MappedTrackInfo = Assertions.checkNotNull(trackSelector.currentMappedTrackInfo)
    private var parameters: DefaultTrackSelector.Parameters = trackSelector.parameters
    private var allowAdaptiveSelections = false
    private var allowMultipleOverrides = false
    private var isDisabled = false
    private lateinit var overrides: List<SelectionOverride>
    private var selectionOverrideList: SparseArray<SelectionOverride> = SparseArray()
    private var trackNameProvider: TtnTrackNameProvider = TtnTrackNameProvider(context.resources)


    fun init(allowAdaptiveSelections: Boolean,
             allowMultipleOverrides: Boolean) {
        this.allowAdaptiveSelections = allowAdaptiveSelections
        this.allowMultipleOverrides = allowMultipleOverrides
        isDisabled = parameters.getRendererDisabled( /* rendererIndex= */rendererIndex)
//        val trackType = mappedTrackInfo.getRendererType( /* rendererIndex= */rendererIndex)
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
        val initialOverride = parameters.getSelectionOverride( /* rendererIndex= */rendererIndex, trackGroupArray)
        overrides = if (initialOverride == null) emptyList() else listOf(initialOverride)

//        val maxOverrides = if (allowMultipleOverrides) overrides.size else Math.min(overrides.size, 1)

        /* for (i in 0 until maxOverrides) {
             val selectionOverride = overrides[i]
             selectionOverrideList.put(selectionOverride.groupIndex, selectionOverride)
         }*/
    }

    private fun showViewForRenderer(mappedTrackInfo: MappedTrackInfo, rendererIndex: Int): Boolean {
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
        if (trackGroupArray.length == 0) {
            return false
        }
        val trackType = mappedTrackInfo.getRendererType(rendererIndex)
        return isSupportedTrackType(trackType)
    }

    private fun isSupportedTrackType(trackType: Int): Boolean {
        return when (trackType) {
            C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT -> true
            else -> false
        }
    }

    fun createTrackSelectionView(title: String?, showDisableOption: Boolean): View {
        val rootView: View = LayoutInflater.from(context).inflate(
                R.layout.ttn_track_dialog, null)
        val trackSelectionView: TtnTrackSelectionView = rootView.findViewById(R.id.ttn_track_view)
        trackSelectionView.setShowDisableOption(showDisableOption)
        trackSelectionView.setAllowMultipleOverrides(allowMultipleOverrides)
        trackSelectionView.setAllowAdaptiveSelections(allowAdaptiveSelections)
        trackSelectionView.init(
                mappedTrackInfo, rendererIndex, isDisabled, overrides,  /* listener= */this)
        trackSelectionView.addTitle(title)
        return rootView
    }

    // Private methods.
    fun getAvailableTracks(): List<TrackModel> { // Remove previous per-track views.
        trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
        val list = arrayListOf<TrackModel>()
        // Add per-track views.
        for (groupIndex in 0 until trackGroups.length) {
            val group: TrackGroup = trackGroups.get(groupIndex)
            for (trackIndex in 0 until group.length) {
                if (mappedTrackInfo.getTrackSupport(rendererIndex, groupIndex, trackIndex)
                        == RendererCapabilities.FORMAT_HANDLED) {
                    val format = group.getFormat(trackIndex)
                    val text = trackNameProvider.getTrackName(format)
                    if (!text.isEmpty()) {
                        val trackModel = TrackModel(text, Pair.create(groupIndex, trackIndex))
                        list.add(trackModel)
                    }
                }
            }
        }
        return list
    }

    private fun shouldEnableAdaptiveSelection(groupIndex: Int): Boolean {
        return (allowAdaptiveSelections
                && trackGroups.get(groupIndex).length > 1 && (mappedTrackInfo.getAdaptiveSupport(
                rendererIndex, groupIndex,  /* includeCapabilitiesExceededTracks= */false)
                != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED))
    }

    private fun shouldEnableMultiGroupSelection(): Boolean {
        return allowMultipleOverrides && trackGroups.length > 1
    }


    private fun getTracksAdding(tracks: IntArray, addedTrack: Int): IntArray {
        var tracks = tracks
        tracks = tracks.copyOf(tracks.size + 1)
        tracks[tracks.size - 1] = addedTrack
        return tracks
    }

    private fun getTracksRemoving(tracks: IntArray, removedTrack: Int): IntArray {
        val newTracks = IntArray(tracks.size - 1)
        var trackCount = 0
        for (track in tracks) {
            if (track != removedTrack) {
                newTracks[trackCount++] = track
            }
        }
        return newTracks
    }

    private fun getOverrides(): List<SelectionOverride> {
        val overrideList: MutableList<SelectionOverride> = ArrayList(selectionOverrideList.size())
        for (i in 0 until selectionOverrideList.size()) {
            overrideList.add(selectionOverrideList.valueAt(i))
        }
        return overrideList
    }

    fun apply(tracksList: List<TrackModel>) {
        tracksList.forEach {
            isDisabled = false
            val groupIndex = it.trackGroupIndexPair.first
            val trackIndex = it.trackGroupIndexPair.second
            val override = selectionOverrideList[groupIndex]
            Assertions.checkNotNull(mappedTrackInfo)
            if (override == null) { // Start new override.
                if (!allowMultipleOverrides && selectionOverrideList.size() > 0) { // Removed other overrides if we don't allow multiple overrides.
                    selectionOverrideList.clear()
                }
                selectionOverrideList.put(groupIndex, SelectionOverride(groupIndex, trackIndex))
            } else { // An existing override is being modified.
//                val overrideLength = override.length
                val overrideTracks = override.tracks
//            val isCurrentlySelected = (view as CheckedTextView).isChecked
                val isAdaptiveAllowed: Boolean = shouldEnableAdaptiveSelection(groupIndex)
//                val isUsingCheckBox = isAdaptiveAllowed || shouldEnableMultiGroupSelection()
//                if (isUsingCheckBox) { // Remove the track from the override.
//                    if (overrideLength == 1) { // The last track is being removed, so the override becomes empty.
//                        selectionOverrideList.remove(groupIndex)
//                    } else {
//                        val tracks = getTracksRemoving(overrideTracks, trackIndex)
//                        selectionOverrideList.put(groupIndex, SelectionOverride(groupIndex, *tracks))
//                    }
//                } else if (true) {
                if (isAdaptiveAllowed) { // Add new track to adaptive override.
                    val tracks = getTracksAdding(overrideTracks, trackIndex)
                    selectionOverrideList.put(groupIndex, SelectionOverride(groupIndex, *tracks))
                } else { // Replace existing track in override.
                    selectionOverrideList.put(groupIndex, SelectionOverride(groupIndex, trackIndex))
                }
//                }
            }
        }
        apply()
    }

    override fun onTrackSelectionChanged(isDisabled: Boolean, overrides: MutableList<SelectionOverride>) {
        this.isDisabled = isDisabled
        this.overrides = overrides
        applySelections()
        onTrackSelectionListener?.onTrackSelectionApply()
    }

    private fun apply() {
        if (!trackGroups.isEmpty) {
            this.overrides = getOverrides()
            applySelections()
        }
    }

    private fun applySelections() {
        val builder = parameters.buildUpon()
//                    for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
        //                    for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
        builder
                .clearSelectionOverrides( /* rendererIndex= */rendererIndex)
                .setRendererDisabled( /* rendererIndex= */
                        rendererIndex,
                        isDisabled)
        if (overrides.isNotEmpty()) {
            builder.setSelectionOverride( /* rendererIndex= */
                    rendererIndex,
                    mappedTrackInfo.getTrackGroups( /* rendererIndex= */rendererIndex),
                    overrides[0])
        }
//                    }
        //                    }
        trackSelector.setParameters(builder)
    }

    interface OnTrackSelectionListener {
        fun onTrackSelectionApply()
    }
}