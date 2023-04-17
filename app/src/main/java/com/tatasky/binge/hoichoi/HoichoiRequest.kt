package com.tatasky.binge.hoichoi

import com.google.gson.annotations.SerializedName

data class HoichoiRequest(
    @SerializedName("partner") var partner: String = ""

)
