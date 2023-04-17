package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.ArrayList


class EpisodeListingResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: List<Data>? = null

    class Data {
        @SerializedName(value = "subscriberId")
        var subscriberId: String? = ""

        @SerializedName("profileId")
        @Expose
        var profileId: String? = ""

        @SerializedName("contentId")
        @Expose
        var contentId: Int = 0

        @SerializedName("secondsWatched")
        @Expose
        var secondsWatched: Int? = 0

        @SerializedName("durationInSeconds")
        @Expose
        var durationInSeconds: Int? = 0

        @SerializedName("favourite")
        var favourite: Boolean? = false
    }
}
