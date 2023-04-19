package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class ViewActionRequest(
    @SerializedName("profileId")
    var profileId: String,
    @SerializedName("subscriberId")
    var subscriberId: String,
    @SerializedName("id")
    var contentId: String,
    @SerializedName("contentType")
    var contentType: String
)
