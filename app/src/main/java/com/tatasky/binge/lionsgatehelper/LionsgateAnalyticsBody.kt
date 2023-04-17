package com.tatasky.binge.lionsgatehelper

import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.APPLICATION
import com.tatasky.binge.utils.REGION

data class LionsgateAnalyticsBody(

    @SerializedName("content_id") var content_id: String = "0", //provider content id
    @SerializedName("category") var contentType : String = "",//movie content type
    @SerializedName("video_language") var video_language: String = "",
    @SerializedName("genre") var genre: String = "",
    @SerializedName("percentage_complete") var percentage_complete: String = "",
    @SerializedName("seconds_watched") var seconds_watched: Long = 0,
    @SerializedName("title") var title: String = "",
    @SerializedName("video_length_in_seconds") var totalDuration: Long = 0,
    @SerializedName("subtitle") var subtitle: String = "off",
    @SerializedName("subtitle_language") var subtitle_language: String = "eng",//
    @SerializedName("subscription_status") var subscription_status: String = "",
    @SerializedName("source") var source: String = "TSMOBILE",//device_type
    @SerializedName("device_os") var device_os: String = "android",
    @SerializedName("device_type") var device_type: String = "Mobile",
    @SerializedName("partner") var partner: String = "TATAPLAY",
    @SerializedName("user_id") var partnerUniqueId: String = "",
    @SerializedName("is_trailer") var is_trailer: Boolean = false

)
