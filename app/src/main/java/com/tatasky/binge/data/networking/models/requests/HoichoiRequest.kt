package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class HoichoiRequest(
    @SerializedName("partner") var partner: String = ""

)
