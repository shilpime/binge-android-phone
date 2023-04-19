package com.tatasky.binge.shemaroo.helper

import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.APPLICATION
import com.tatasky.binge.utils.REGION

data class ShemarooAnalyticsBody(
        @SerializedName("_id") var _id: String = "0",
        @SerializedName("rand") var rand:Long = 0,
        @SerializedName("dimension1") var dimension1: String = APPLICATION,
        @SerializedName("dimension2") var dimension2: String = "",
        @SerializedName("dimension5") var dimension5: String = "",
        @SerializedName("e_c") var e_c: String = "media",
        @SerializedName("e_a") var e_a: String = "",
        @SerializedName("e_n") var e_n: String = "",
        @SerializedName("e_v") var e_v: Int = 0,
        @SerializedName("dimension15") var providerContentId: String = "",
        @SerializedName("dimension16") var mediaTitle: String = "",
        @SerializedName("dimension17") var totalDuration: Long = 0,
        @SerializedName("dimension23") var deviceId: String = "",
        @SerializedName("dimension25") var region: String = REGION,
        @SerializedName("url") var url: String = "",
        @SerializedName("pv_id") var pvId: Long = 0,
        @SerializedName("idsite") var idsite: String = "17",
        @SerializedName("rec") var rec: Int = 1
)