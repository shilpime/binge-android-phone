package com.tatasky.binge.ui.features.player

import com.google.gson.annotations.SerializedName

data class ProbeInterfacePlayer(
    @SerializedName("videoId")var videoId : String = "",
    @SerializedName("provider")var provider : String="",
    @SerializedName("drm")var drm : String="",
    @SerializedName("videoUrl")var videoUrl : String=""
)
