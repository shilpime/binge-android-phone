package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName
import com.tatasky.binge.data.networking.models.response.Epid

data class ControlRequest(
    @SerializedName("action") val action: String = "Stream",
    @SerializedName("provider") val provider: String = "ReelDrama",
    @SerializedName("epids") val epids: List<Epid> = ArrayList())

