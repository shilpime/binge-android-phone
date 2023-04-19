package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName


class ContentIdAndTypeRequest {
    @SerializedName("subscriberId")
    var subscriberId: String? = null

    @SerializedName("profileId")
    var profileId: String? = null

    var isLoggedIn : Boolean = false

    @SerializedName("contentIdAndType")
    var contentIdAndType: List<ContentIdAndType> = emptyList()

    data class ContentIdAndType(
        @SerializedName("contentId") var contentId: Int?,
        @SerializedName("contentType") var contentType: String?
    )
}