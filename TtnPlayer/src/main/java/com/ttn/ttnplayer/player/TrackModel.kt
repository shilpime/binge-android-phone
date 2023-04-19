package com.ttn.ttnplayer.player

import android.util.Pair

class TrackModel(var track: String, groupIndexPair: Pair<Int, Int>) {
    var trackGroupIndexPair: Pair<Int, Int> = groupIndexPair
}