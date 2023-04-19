package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class GetOtpGuestLoginRequest(
    @SerializedName("mobileNumber")
    val mobileNumber: String? = null,
)