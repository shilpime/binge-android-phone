package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName


data class ToggleFavouriteRequest(
    @SerializedName("profileId")
    var profileId: String,
    @SerializedName("subscriberId")
    var subscriberId: String,
    @SerializedName("contentId")
    var contentId: String,
    @SerializedName("contentType")
    var contentType: String,
    @SerializedName("uniqueId")
    var uniqueId: String,
    var isLoggedIn : Boolean = true,
    var subscriptionType : String = "freemium"
)
