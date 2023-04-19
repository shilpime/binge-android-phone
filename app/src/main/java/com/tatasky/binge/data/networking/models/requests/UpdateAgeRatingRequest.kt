package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class UpdateAgeRatingRequest(
    @SerializedName("bingeSubscriberId") val bingeSubscriberId: String? = null,
    @SerializedName("baId") val baId: String? = null,
    @SerializedName("parentalLock") val parentalLock: String? = null,
    @SerializedName("ageRating") val ageRating: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null
)