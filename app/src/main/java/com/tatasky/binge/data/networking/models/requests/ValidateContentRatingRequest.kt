package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class ValidateContentRatingRequest(
    @SerializedName("baId")
    val baId: String? = null,

    @SerializedName("mobileNumber")
    val mobileNumber: String? = null,

    @SerializedName("bingeSubscriberId")
    val bingeSubscriberId: String? = null,

    @SerializedName("isLogin")
    val isLogin: Boolean? = null,

    @SerializedName("contentAgeRating")
    val contentAgeRating: String? = null
)
