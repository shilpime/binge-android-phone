package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class ParentalPinRequest(
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("bingeSubscriberId") val bingeSubscriberId: String? = null,
    @SerializedName("baId") val baId: String? = null,
    @SerializedName("parentalLock") val parentalLock: String? = null,
    @SerializedName("otp") val otp: String? = null,
    @SerializedName("isLogin") val isLogin: Boolean = false,
    @SerializedName("ageRating") val ageRating: String? = null
)