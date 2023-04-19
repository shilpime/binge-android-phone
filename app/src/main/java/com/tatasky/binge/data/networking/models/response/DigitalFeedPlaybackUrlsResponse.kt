package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class DigitalFeedPlaybackUrlsResponse: BaseResponse() {

    @SerializedName("data")
    var data: Data? = null

    @SerializedName("localizedMessage")
    var localizedMessage: String? = null

    data class Data(
        @SerializedName("liveChannelId") var liveChannelId: Int? = null,
        @SerializedName("partner") var partner: String? = null,
        @SerializedName("contentId") var contentId: Int? = null,
        @SerializedName("dashUrl") var dashUrl: String? = null,
        @SerializedName("hlsUrl") var hlsUrl: String? = null,
        @SerializedName("expiry") var expiry: Int? = null,
        @SerializedName("ldashUrl") var ldashUrl: String? = null,
    )
}