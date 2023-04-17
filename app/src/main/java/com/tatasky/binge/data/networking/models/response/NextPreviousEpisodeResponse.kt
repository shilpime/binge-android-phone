package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class NextPreviousEpisodeResponse : BaseResponse() {
    @SerializedName("data")
    var data: NextPreviousEpisodeDetails? = null

    class NextPreviousEpisodeDetails {
        @SerializedName("nextEpisodeExists")
        var nextEpisodeExists: Boolean = false
        @SerializedName("previousEpisodeExists")
        var previousEpisodeExists: Boolean = false
        @SerializedName("nextEpisode")
        var nextEpisode: ContentItem? = null
        @SerializedName("previousEpisode")
        var previousEpisode: ContentItem? = null
    }
}