package com.tatasky.binge.voot.model

import com.google.gson.annotations.SerializedName

data class VootRequest(@SerializedName("contentId") val contentId: String,
                       @SerializedName("contentType") val contentType: String,
                       @SerializedName("partner") var partner: String = "",
                       @SerializedName("baId") var baId: String = "",
                       @SerializedName("type") var type: String = "")
